package org.scde.processor.es;

import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.scde.SCEIApplicationConfig;
import org.scde.model.TxHistoryWrapper;
import org.scde.processor.TxHistoryVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stellar.sdk.xdr.AllowTrustOp;
import org.stellar.sdk.xdr.ChangeTrustOp;
import org.stellar.sdk.xdr.CreateAccountOp;
import org.stellar.sdk.xdr.CreatePassiveOfferOp;
import org.stellar.sdk.xdr.ManageDataOp;
import org.stellar.sdk.xdr.ManageOfferOp;
import org.stellar.sdk.xdr.PathPaymentOp;
import org.stellar.sdk.xdr.PaymentOp;
import org.stellar.sdk.xdr.SetOptionsOp;

public class ElasticsearchTxHistoryVisitor implements TxHistoryVisitor {

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchTxHistoryVisitor.class);

    private static final String ES_INDEX_PUBNET = "stellar-pubnet";
    private static final String ES_INDEX_TESTNET = "stellar-testnet";

    private static final String ES_TYPE_TRANSACTION = "transaction";
    private static final String ES_TYPE_PAYMENT = "payment";
    public static final String ES_ELASTIC_CLOUD = "elastic-cloud";


    private final Client client;
    private final BulkProcessor bulkProcessor;
    private final SCEIApplicationConfig config;

    public ElasticsearchTxHistoryVisitor(SCEIApplicationConfig config) {
        this.config = config;
        client = ElasticsearchUtils.getClient(config);
        bulkProcessor = BulkProcessor.builder(
            client,
            new BulkProcessor.Listener() {
                @Override
                public void beforeBulk(long executionId,
                                       BulkRequest request) {
                }

                @Override
                public void afterBulk(long executionId,
                                      BulkRequest request,
                                      BulkResponse response) {
                }

                @Override
                public void afterBulk(long executionId,
                                      BulkRequest request,
                                      Throwable failure) {
                }
            })
            .build();
    }

    @Override
    public void visitTxHistory(TxHistoryWrapper txHistoryWrapper) {
        logger.debug("Elasticsearch indexing transaction with id " + txHistoryWrapper.getTransactionId());
        IndexRequest indexRequest = getTransactionIndexRequest(txHistoryWrapper);
        if (indexRequest != null) {
            bulkProcessor.add(indexRequest);
        }

    }

    @Override
    public void visitCreateAccount(CreateAccountOp createAccountOp, TxHistoryWrapper txHistoryWrapper, int operationIndex) {

    }

    @Override
    public void visitPaymentOp(PaymentOp paymentOp, TxHistoryWrapper txHistoryWrapper, int operationIndex) {
        IndexRequest indexRequest = getPaymentIndexRequest(txHistoryWrapper, operationIndex, paymentOp);
        if (indexRequest != null) {
            bulkProcessor.add(indexRequest);
        }
    }

    @Override
    public void visitPathPaymentOp(PathPaymentOp paymentOp, TxHistoryWrapper txHistoryWrapper, int operationIndex) {

    }

    @Override
    public void visitManageOffer(ManageOfferOp manageOfferOp, TxHistoryWrapper txHistoryWrapper, int operationIndex) {

    }

    @Override
    public void visitCreatePassiveOffer(CreatePassiveOfferOp createPassiveOfferOp, TxHistoryWrapper txHistoryWrapper, int operationIndex) {

    }

    @Override
    public void visitSetOptions(SetOptionsOp setOptionsOp, TxHistoryWrapper txHistoryWrapper, int operationIndex) {

    }

    @Override
    public void visitChangeTrust(ChangeTrustOp changeTrustOp, TxHistoryWrapper txHistoryWrapper, int operationIndex) {

    }

    @Override
    public void visitAllowTrust(AllowTrustOp allowTrustOp, TxHistoryWrapper txHistoryWrapper, int operationIndex) {

    }

//    @Override
//    public void visitAccountMerge(PathPaymentOp paymentOp, TxHistoryWrapper txHistoryWrapper, int operationIndex) {
//
//    }

    @Override
    public void visitManageData(ManageDataOp manageDataOp, TxHistoryWrapper txHistoryWrapper, int operationIndex) {

    }

    public IndexRequest getTransactionIndexRequest(TxHistoryWrapper txHistoryWrapper) {
        XContentBuilder source = ElasticsearchUtils.getTransactionSource(txHistoryWrapper);
        return new IndexRequest(ES_INDEX_PUBNET, ES_TYPE_TRANSACTION, txHistoryWrapper.getTransactionId()).source(source);
    }

    public IndexRequest getPaymentIndexRequest(TxHistoryWrapper txHistoryWrapper, int index, PaymentOp paymentOp) {
        IndexRequest indexRequest = null;
        XContentBuilder source = ElasticsearchUtils.getPaymentSource(txHistoryWrapper, paymentOp);
        if (source != null) {
            indexRequest = new IndexRequest(ES_INDEX_PUBNET, ES_TYPE_PAYMENT, txHistoryWrapper.getTransactionId() + "-" + index).source(source);
        }
        return indexRequest;
    }

    @Override
    public void finished() {
        bulkProcessor.close();
    }


}
