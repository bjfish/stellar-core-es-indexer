package org.scde;

import org.apache.commons.codec.binary.Base64;
import org.postgresql.ds.PGPoolingDataSource;
import org.scde.dao.TxHistoryDAO;
import org.scde.dao.TxHistoryRow;
import org.scde.model.TxHistoryWrapper;
import org.scde.processor.TxHistoryVisitor;
import org.scde.processor.es.ElasticsearchTxHistoryVisitor;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stellar.sdk.xdr.Operation;
import org.stellar.sdk.xdr.TransactionEnvelope;
import org.stellar.sdk.xdr.TransactionMeta;
import org.stellar.sdk.xdr.TransactionResultPair;
import org.stellar.sdk.xdr.XdrDataInputStream;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StellarCoreElasticsearchIndexer {

    private static final Logger logger = LoggerFactory.getLogger(StellarCoreElasticsearchIndexer.class);
    public static final int BATCH_SIZE = 100;

    public static void main(String[] args) {

        final String configFileName;
        if(args.length > 0){
           configFileName = args[0];
        } else {
           configFileName = "config.properties";
        }

        SCEIApplicationConfig config = SCEIApplicationConfig.loadFromProperties(configFileName);

        logger.info("Starting indexing");

        TxHistoryDAO txHistoryDAO = getTxHistoryDAO(config);
        TxHistoryVisitor txHistoryVisitor = new ElasticsearchTxHistoryVisitor(config);

        final int refreshIntervalSeconds = config.getRefreshInterval();

        int lastLedgerSequenceNumber = config.getLastLedgerIndexed();

        do {
            List<TxHistoryWrapper> txHistories = decodeAndMap(txHistoryDAO.findTxHistories(lastLedgerSequenceNumber, BATCH_SIZE));
            while (txHistories.size() > 0) {
                logger.info("Processing " + txHistories.size() + " transaction histories, after ledgerseq: " + lastLedgerSequenceNumber);
                for (TxHistoryWrapper txHistoryWrapper : txHistories) {
                    txHistoryVisitor.visitTxHistory(txHistoryWrapper);

                    int operationIndex = 0;
                    for (Operation operation : txHistoryWrapper.getTransactionEnvelope().getTx().getOperations()) {
                        switch (operation.getBody().getDiscriminant()) {
                            case CREATE_ACCOUNT:
                                txHistoryVisitor.visitCreateAccount(operation.getBody().getCreateAccountOp(), txHistoryWrapper, operationIndex);
                                break;
                            case PAYMENT:
                                txHistoryVisitor.visitPaymentOp(operation.getBody().getPaymentOp(), txHistoryWrapper, operationIndex);
                                break;
                            case PATH_PAYMENT:
                                txHistoryVisitor.visitPathPaymentOp(operation.getBody().getPathPaymentOp(), txHistoryWrapper, operationIndex);
                                break;
                            case MANAGE_OFFER:
                                txHistoryVisitor.visitManageOffer(operation.getBody().getManageOfferOp(), txHistoryWrapper, operationIndex);
                                break;
                            case CREATE_PASSIVE_OFFER:
                                txHistoryVisitor.visitCreatePassiveOffer(operation.getBody().getCreatePassiveOfferOp(), txHistoryWrapper, operationIndex);
                                break;
                            case SET_OPTIONS:
                                txHistoryVisitor.visitSetOptions(operation.getBody().getSetOptionsOp(), txHistoryWrapper, operationIndex);
                                break;
                            case CHANGE_TRUST:
                                txHistoryVisitor.visitChangeTrust(operation.getBody().getChangeTrustOp(), txHistoryWrapper, operationIndex);
                                break;
                            case ALLOW_TRUST:
                                txHistoryVisitor.visitAllowTrust(operation.getBody().getAllowTrustOp(), txHistoryWrapper, operationIndex);
                                break;
                            case ACCOUNT_MERGE:
//                            txHistoryVisitor.visitAccountMerge(operation.getBody().getAccountMergeOp(), txHistoryWrapper, operationIndex);
                                break;
                            case INFLATION:
                                // txHistoryVisitor.visitInflation(operation.getBody().getInflationOp(), txHistoryWrapper, operationIndex);
                                break;
                            case MANAGE_DATA:
                                txHistoryVisitor.visitManageData(operation.getBody().getManageDataOp(), txHistoryWrapper, operationIndex);
                                break;
                            default:
                                logger.debug("Unsupported operation type: " + operation.getBody().getDiscriminant().name());
                        }
                        operationIndex += 1;
                    }

                    lastLedgerSequenceNumber = txHistoryWrapper.getLedgerSeq();
                }

                txHistories = decodeAndMap(txHistoryDAO.findTxHistories(lastLedgerSequenceNumber, BATCH_SIZE));
            }

            txHistoryVisitor.afterBatchesProcessed();

            if (refreshIntervalSeconds != 0) {
                try {
                    logger.info("Indexed through ledger " + lastLedgerSequenceNumber + ", waiting " + refreshIntervalSeconds + " to poll again");
                    Thread.sleep(refreshIntervalSeconds * 1000);
                } catch (InterruptedException e) {
                    return;
                }
            }
        } while (refreshIntervalSeconds != 0);

        txHistoryVisitor.finished();

        logger.info("Finished indexing");
    }

    /**
     * Converts a list of txHistoryRows into TxHistoryWrappers
     *
     * @param rows
     * @return
     */
    private static List<TxHistoryWrapper> decodeAndMap(List<TxHistoryRow> rows) {
        List<TxHistoryWrapper> txHistoryWrappers = new ArrayList<>();
        for (TxHistoryRow txHistory : rows) {
            TxHistoryWrapper txHistoryWrapper = null;
            try {
                TransactionMeta transactionMeta = TransactionMeta.decode(stringToXdrStream(txHistory.getTxMeta()));
                TransactionResultPair transactionResultPair = TransactionResultPair.decode(stringToXdrStream(txHistory.getTxResult()));
                TransactionEnvelope transactionEnvelope = TransactionEnvelope.decode(stringToXdrStream(txHistory.getTxBody()));
                txHistoryWrapper = new TxHistoryWrapper(transactionEnvelope, transactionMeta,
                    transactionResultPair, txHistory.getTxId(), txHistory.getLedgerSeq(), txHistory.getLedgerCloseTime());
            } catch (IOException e) {
                logger.error("Error de-serializing txn: " + txHistory.getTxId(), e);
            }
            if (txHistoryWrapper != null) {
                txHistoryWrappers.add(txHistoryWrapper);
            }
        }
        return txHistoryWrappers;
    }

    private static TxHistoryDAO getTxHistoryDAO(SCEIApplicationConfig config) {
        DataSource ds = getDataSource(config);
        DBI dbi = new DBI(ds);
        return dbi.open(TxHistoryDAO.class);
    }

    private static DataSource getDataSource(SCEIApplicationConfig config) {
        final PGPoolingDataSource source = new PGPoolingDataSource();
        source.setDataSourceName("PostgresDB");
        source.setServerName(config.getDatabaseServerName());
        source.setDatabaseName(config.getDatabaseName());
        source.setPortNumber(config.getDatabasePort());
        source.setUser(config.getDatabaseUser());
        source.setPassword(config.getDatabasePassword());
        source.setMaxConnections(10);
        return source;
    }

    private static XdrDataInputStream stringToXdrStream(String raw) {
        final Base64 base64Codec = new Base64();
        final byte[] bytes = base64Codec.decode(raw);
        return new XdrDataInputStream(new ByteArrayInputStream(bytes));
    }
}
