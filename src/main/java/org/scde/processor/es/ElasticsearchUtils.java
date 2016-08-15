package org.scde.processor.es;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.shield.ShieldPlugin;
import org.scde.SCEIApplicationConfig;
import org.scde.model.TxHistoryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.xdr.AccountID;
import org.stellar.sdk.xdr.Asset;
import org.stellar.sdk.xdr.AssetType;
import org.stellar.sdk.xdr.CreateAccountOp;
import org.stellar.sdk.xdr.Int64;
import org.stellar.sdk.xdr.PathPaymentOp;
import org.stellar.sdk.xdr.PaymentOp;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

public class ElasticsearchUtils {

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchUtils.class);

    public static final BigDecimal AMOUNT_SCALE_FACTOR = BigDecimal.valueOf(10_000_000);

    public static XContentBuilder getTransactionSource(TxHistoryWrapper txHistoryWrapper) {
        XContentBuilder source = null;
        try {
            source = jsonBuilder()
                .startObject()
                .field("status", getStatus(txHistoryWrapper))
                .field("created_at", txHistoryWrapper.getLedgerCloseTime())
                .field("source_account", accountIdToString(txHistoryWrapper.getTransactionEnvelope().getTx().getSourceAccount()))
                .field("operation_count", txHistoryWrapper.getTransactionEnvelope().getTx().getOperations().length)
                .endObject();
        } catch (IOException e) {
            logger.error("Error serializing transaction", e);
        }
        return source;
    }

    public static XContentBuilder getPaymentSource(TxHistoryWrapper txHistoryWrapper, PaymentOp paymentOp) {
        XContentBuilder source = null;
        try {
            source = jsonBuilder()
                .startObject()
                .field("status", getStatus(txHistoryWrapper))
                .field("created_at", txHistoryWrapper.getLedgerCloseTime())
                .field("amount",  scaleAmount(paymentOp.getAmount()))
                .field("source_account", accountIdToString(txHistoryWrapper.getTransactionEnvelope().getTx().getSourceAccount()))
                .field("to", accountIdToString(paymentOp.getDestination()));
            setAssetFields(source, paymentOp.getAsset());
            source.endObject();
        } catch (IOException e) {
            logger.error("Error serializing payment", e);
        }
        return source;
    }

    public static XContentBuilder getPathPaymentSource(TxHistoryWrapper txHistoryWrapper, PathPaymentOp pathPaymentOp) {
        XContentBuilder source = null;
        try {
            source = jsonBuilder()
                .startObject()
                .field("status", getStatus(txHistoryWrapper))
                .field("created_at", txHistoryWrapper.getLedgerCloseTime())
                .field("amount",  scaleAmount(pathPaymentOp.getDestAmount())) // TODO Normalize with horizon, add send amount, idea -> add send amount to payment
                .field("source_account", accountIdToString(txHistoryWrapper.getTransactionEnvelope().getTx().getSourceAccount()))
                .field("to", accountIdToString(pathPaymentOp.getDestination()));
            setAssetFields(source, pathPaymentOp.getDestAsset());
            source.endObject();
        } catch (IOException e) {
            logger.error("Error serializing payment", e);
        }
        return source;
    }

    public static XContentBuilder getCreateAccountSource(TxHistoryWrapper txHistoryWrapper, int operationIndex, CreateAccountOp createAccountOp) {
        XContentBuilder source = null;
        try {
            source = jsonBuilder()
                .startObject()
                .field("status", getStatus(txHistoryWrapper))
                .field("created_at", txHistoryWrapper.getLedgerCloseTime())
                .field("starting_balance",  scaleAmount(createAccountOp.getStartingBalance()) )
                .field("source_account", accountIdToString(txHistoryWrapper.getTransactionEnvelope().getTx().getSourceAccount()))
                .field("account", accountIdToString(createAccountOp.getDestination()))
                .endObject();
        } catch (IOException e) {
            logger.error("Error serializing payment", e);
        }
        return source;
    }

    /**
     * Native - "XLM"
     * Other - "<AlphaNum>-<Issuer>"
     *
     * @param asset
     * @return
     */
    public static void setAssetFields(XContentBuilder source, Asset asset) throws IOException {
        if (asset.getDiscriminant() == AssetType.ASSET_TYPE_NATIVE) {
            source.field("asset_type", "native");
        } else if (asset.getDiscriminant() == AssetType.ASSET_TYPE_CREDIT_ALPHANUM4) {
            source.field("asset_issuer", accountIdToString(asset.getAlphaNum4().getIssuer()));
            source.field("asset_code", new String(asset.getAlphaNum4().getAssetCode()));
            source.field("asset_type", "credit_alphanum4");
        } else {
            source.field("asset_issuer", accountIdToString(asset.getAlphaNum12().getIssuer()));
            source.field("asset_code", new String(asset.getAlphaNum4().getAssetCode()));
            source.field("asset_type", "credit_alphanum12");
        }
    }

    public static String getStatus(TxHistoryWrapper txHistoryWrapper) {
        return txHistoryWrapper.getTransactionResultPair().getResult().getResult().getDiscriminant().name();
    }

    public static String accountIdToString(AccountID accountID) {
        return KeyPair.fromPublicKey(accountID.getAccountID().getEd25519().getUint256()).getAccountId();
    }

    public static BigDecimal scaleAmount(Int64 amount){
        return BigDecimal.valueOf(amount.getInt64()).divide(AMOUNT_SCALE_FACTOR);
    }

    public static Client getClient(SCEIApplicationConfig config) {
        if(ElasticsearchTxHistoryVisitor.ES_ELASTIC_CLOUD.equals(config.getElasticsearchTransport())){
           return getElasticCloudClient(config);
        } else {
            throw new RuntimeException("Unsupported ES_TRANSPORT: " + config.getElasticsearchTransport());
        }
    }

    public static Client getElasticCloudClient(SCEIApplicationConfig config) {
        final boolean enableSsl = true;
        Settings settings = Settings.settingsBuilder()
            .put("transport.ping_schedule", "5s")
            .put("cluster.name", config.getElasticsearchClusterId())
            .put("action.bulk.compress", false)
            .put("shield.transport.ssl", enableSsl)
            .put("request.headers.X-Found-Cluster", config.getElasticsearchClusterId())
            .put("shield.user", config.getElasticsearchUserPassword())
            .build();

        final String hostname = config.getElasticsearchClusterId() + "." + config.getElasticsearchRegion() + ".aws.found.io";
        final int port = 9343;

        Client client = null;
        try {
            client = TransportClient.builder()
                .addPlugin(ShieldPlugin.class)
                .settings(settings)
                .build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(hostname), port));
        } catch (UnknownHostException e) {
            logger.error("Unknown host:" + hostname, e);
        }
        return client;
    }
}
