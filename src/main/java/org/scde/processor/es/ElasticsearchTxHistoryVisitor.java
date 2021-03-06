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

    public static final String ES_ELASTIC_CLOUD = "elastic-cloud";

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchTxHistoryVisitor.class);

    private static final String ES_INDEX_PUBNET = "stellar-pubnet";
    private static final String ES_INDEX_TESTNET = "stellar-testnet";

    private static final String ES_TYPE_TRANSACTION = "transaction";
    private static final String ES_TYPE_PAYMENT = "payment";
    private static final String ES_TYPE_CREATE_ACCOUNT = "create-account";

    private static final String ES_TYPE_PATH_PAYMENT = "path-payment";
    private static final String ES_TYPE_OPERATION = "operation";


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
                    if(response.hasFailures()){
                        logger.equals("Bulk requests errors:" + response.buildFailureMessage());
                    }
                }

                @Override
                public void afterBulk(long executionId,
                                      BulkRequest request,
                                      Throwable failure) {
                    logger.error("Bulk request error", failure);
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
        IndexRequest indexRequest = getCreateAccountIndexRequest(txHistoryWrapper, operationIndex, createAccountOp);
        if (indexRequest != null) {
            bulkProcessor.add(indexRequest);
        }
    }

    @Override
    public void visitPaymentOp(PaymentOp paymentOp, TxHistoryWrapper txHistoryWrapper, int operationIndex) {
        IndexRequest indexRequest = getPaymentIndexRequest(txHistoryWrapper, operationIndex, paymentOp);
        if (indexRequest != null) {
            bulkProcessor.add(indexRequest);
        }
    }

    @Override
    public void visitPathPaymentOp(PathPaymentOp pathPaymentOp, TxHistoryWrapper txHistoryWrapper, int operationIndex) {
        IndexRequest indexRequest = getPathPaymentIndexRequest(txHistoryWrapper, operationIndex, pathPaymentOp);
        if (indexRequest != null) {
            bulkProcessor.add(indexRequest);
        }
    }

    @Override
    public void visitManageOffer(ManageOfferOp manageOfferOp, TxHistoryWrapper txHistoryWrapper, int operationIndex) {
        IndexRequest indexRequest = getManageOfferIndexRequest(txHistoryWrapper, operationIndex, manageOfferOp);
        if (indexRequest != null) {
            bulkProcessor.add(indexRequest);
        }
    }

    @Override
    public void visitCreatePassiveOffer(CreatePassiveOfferOp createPassiveOfferOp, TxHistoryWrapper txHistoryWrapper, int operationIndex) {
        IndexRequest indexRequest = getCreatePassiveOfferIndexRequest(txHistoryWrapper, operationIndex, createPassiveOfferOp);
        if (indexRequest != null) {
            bulkProcessor.add(indexRequest);
        }
    }


    @Override
    public void visitSetOptions(SetOptionsOp setOptionsOp, TxHistoryWrapper txHistoryWrapper, int operationIndex) {
        IndexRequest indexRequest = getSetOptionsIndexRequest(txHistoryWrapper, operationIndex, setOptionsOp);
        if (indexRequest != null) {
            bulkProcessor.add(indexRequest);
        }
    }


    @Override
    public void visitChangeTrust(ChangeTrustOp changeTrustOp, TxHistoryWrapper txHistoryWrapper, int operationIndex) {
        IndexRequest indexRequest = getChangeTrustIndexRequest(txHistoryWrapper, operationIndex, changeTrustOp);
        if (indexRequest != null) {
            bulkProcessor.add(indexRequest);
        }
    }

    @Override
    public void visitAllowTrust(AllowTrustOp allowTrustOp, TxHistoryWrapper txHistoryWrapper, int operationIndex) {
        IndexRequest indexRequest = getAllowTrustIndexRequest(txHistoryWrapper, operationIndex, allowTrustOp);
        if (indexRequest != null) {
            bulkProcessor.add(indexRequest);
        }
    }


//    @Override
//    public void visitAccountMerge(PathPaymentOp paymentOp, TxHistoryWrapper txHistoryWrapper, int operationIndex) {
//
//    }

    @Override
    public void visitManageData(ManageDataOp manageDataOp, TxHistoryWrapper txHistoryWrapper, int operationIndex) {
        IndexRequest indexRequest = getManageDataIndexRequest(txHistoryWrapper, operationIndex, manageDataOp);
        if (indexRequest != null) {
            bulkProcessor.add(indexRequest);
        }
    }

    @Override
    public void afterBatchesProcessed() {
        bulkProcessor.flush();
    }

    private IndexRequest getManageDataIndexRequest(TxHistoryWrapper txHistoryWrapper, int operationIndex, ManageDataOp manageDataOp) {
        IndexRequest indexRequest = null;
        XContentBuilder source = ElasticsearchUtils.getManageDataSource(txHistoryWrapper, manageDataOp);
        if (source != null) {
            indexRequest = new IndexRequest(ES_INDEX_PUBNET, ES_TYPE_OPERATION, txHistoryWrapper.getTransactionId() + "-" + operationIndex).source(source);
        }
        return indexRequest;
    }

    public IndexRequest getTransactionIndexRequest(TxHistoryWrapper txHistoryWrapper) {
        IndexRequest indexRequest = null;
        XContentBuilder source = ElasticsearchUtils.getTransactionSource(txHistoryWrapper);
        if (source != null) {
            indexRequest = new IndexRequest(ES_INDEX_PUBNET, ES_TYPE_TRANSACTION, txHistoryWrapper.getTransactionId()).source(source);
        }
        return indexRequest;
    }

    public IndexRequest getPaymentIndexRequest(TxHistoryWrapper txHistoryWrapper, int index, PaymentOp paymentOp) {
        IndexRequest indexRequest = null;
        XContentBuilder source = ElasticsearchUtils.getPaymentSource(txHistoryWrapper, paymentOp);
        if (source != null) {
            indexRequest = new IndexRequest(ES_INDEX_PUBNET, ES_TYPE_OPERATION, txHistoryWrapper.getTransactionId() + "-" + index).source(source);
        }
        return indexRequest;
    }

    private IndexRequest getCreateAccountIndexRequest(TxHistoryWrapper txHistoryWrapper, int operationIndex, CreateAccountOp createAccountOp) {
        IndexRequest indexRequest = null;
        XContentBuilder source = ElasticsearchUtils.getCreateAccountSource(txHistoryWrapper, operationIndex, createAccountOp);
        if (source != null) {
            indexRequest = new IndexRequest(ES_INDEX_PUBNET, ES_TYPE_OPERATION, txHistoryWrapper.getTransactionId() + "-" + operationIndex).source(source);
        }
        return indexRequest;
    }

    private IndexRequest getPathPaymentIndexRequest(TxHistoryWrapper txHistoryWrapper, int operationIndex, PathPaymentOp pathPaymentOp) {
        IndexRequest indexRequest = null;
        XContentBuilder source = ElasticsearchUtils.getPathPaymentSource(txHistoryWrapper, pathPaymentOp);
        if (source != null) {
            indexRequest = new IndexRequest(ES_INDEX_PUBNET, ES_TYPE_OPERATION, txHistoryWrapper.getTransactionId() + "-" + operationIndex).source(source);
        }
        return indexRequest;
    }


    private IndexRequest getAllowTrustIndexRequest(TxHistoryWrapper txHistoryWrapper, int operationIndex, AllowTrustOp allowTrustOp) {
        IndexRequest indexRequest = null;
        XContentBuilder source = ElasticsearchUtils.getAllowTrustSource(txHistoryWrapper, allowTrustOp);
        if (source != null) {
            indexRequest = new IndexRequest(ES_INDEX_PUBNET, ES_TYPE_OPERATION, txHistoryWrapper.getTransactionId() + "-" + operationIndex).source(source);
        }
        return indexRequest;
    }

    private IndexRequest getManageOfferIndexRequest(TxHistoryWrapper txHistoryWrapper, int operationIndex, ManageOfferOp manageOfferOp) {
        IndexRequest indexRequest = null;
        XContentBuilder source = ElasticsearchUtils.getManageOfferSource(txHistoryWrapper, manageOfferOp);
        if (source != null) {
            indexRequest = new IndexRequest(ES_INDEX_PUBNET, ES_TYPE_OPERATION, txHistoryWrapper.getTransactionId() + "-" + operationIndex).source(source);
        }
        return indexRequest;
    }


    private IndexRequest getCreatePassiveOfferIndexRequest(TxHistoryWrapper txHistoryWrapper, int operationIndex, CreatePassiveOfferOp createPassiveOfferOp) {
        IndexRequest indexRequest = null;
        XContentBuilder source = ElasticsearchUtils.getCreatePassiveOfferSource(txHistoryWrapper, createPassiveOfferOp);
        if (source != null) {
            indexRequest = new IndexRequest(ES_INDEX_PUBNET, ES_TYPE_OPERATION, txHistoryWrapper.getTransactionId() + "-" + operationIndex).source(source);
        }
        return indexRequest;
    }

    private IndexRequest getSetOptionsIndexRequest(TxHistoryWrapper txHistoryWrapper, int operationIndex, SetOptionsOp setOptionsOp) {
        IndexRequest indexRequest = null;
        XContentBuilder source = ElasticsearchUtils.getSetOptionsSource(txHistoryWrapper, setOptionsOp);
        if (source != null) {
            indexRequest = new IndexRequest(ES_INDEX_PUBNET, ES_TYPE_OPERATION, txHistoryWrapper.getTransactionId() + "-" + operationIndex).source(source);
        }
        return indexRequest;
    }

    private IndexRequest getChangeTrustIndexRequest(TxHistoryWrapper txHistoryWrapper, int operationIndex, ChangeTrustOp changeTrustOp) {
        IndexRequest indexRequest = null;
        XContentBuilder source = ElasticsearchUtils.getChangeTrustSource(txHistoryWrapper, changeTrustOp);
        if (source != null) {
            indexRequest = new IndexRequest(ES_INDEX_PUBNET, ES_TYPE_OPERATION, txHistoryWrapper.getTransactionId() + "-" + operationIndex).source(source);
        }
        return indexRequest;
    }


    @Override
    public void finished() {
        bulkProcessor.close();
    }


}
