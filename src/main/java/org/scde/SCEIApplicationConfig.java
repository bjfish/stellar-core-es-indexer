package org.scde;

import org.elasticsearch.common.settings.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class SCEIApplicationConfig {

    private static final Logger logger = LoggerFactory.getLogger(SCEIApplicationConfig.class);

    private int refreshInterval;
    private int lastLedgerIndexed;

    // Database
    private String databaseServerName;
    private String databaseName;
    private int databasePort;
    private String databaseUser;
    private String databasePassword;

    // Elastic Search
    private String elasticsearchClusterId;
    private String elasticsearchRegion;
    private int elasticsearchPort;
    private String elasticsearchUserPassword;
    private String elasticsearchTransport;

    public int getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(int refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    public int getLastLedgerIndexed() {
        return lastLedgerIndexed;
    }

    public void setLastLedgerIndexed(int lastLedgerIndexed) {
        this.lastLedgerIndexed = lastLedgerIndexed;
    }

    public String getDatabaseServerName() {
        return databaseServerName;
    }

    public void setDatabaseServerName(String databaseServerName) {
        this.databaseServerName = databaseServerName;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public int getDatabasePort() {
        return databasePort;
    }

    public void setDatabasePort(int databasePort) {
        this.databasePort = databasePort;
    }

    public String getDatabaseUser() {
        return databaseUser;
    }

    public void setDatabaseUser(String databaseUser) {
        this.databaseUser = databaseUser;
    }

    public String getDatabasePassword() {
        return databasePassword;
    }

    public void setDatabasePassword(String databasePassword) {
        this.databasePassword = databasePassword;
    }

    public String getElasticsearchClusterId() {
        return elasticsearchClusterId;
    }

    public void setElasticsearchClusterId(String elasticsearchClusterId) {
        this.elasticsearchClusterId = elasticsearchClusterId;
    }

    public String getElasticsearchRegion() {
        return elasticsearchRegion;
    }

    public void setElasticsearchRegion(String elasticsearchRegion) {
        this.elasticsearchRegion = elasticsearchRegion;
    }

    public int getElasticsearchPort() {
        return elasticsearchPort;
    }

    public void setElasticsearchPort(int elasticsearchPort) {
        this.elasticsearchPort = elasticsearchPort;
    }

    public String getElasticsearchUserPassword() {
        return elasticsearchUserPassword;
    }

    public void setElasticsearchUserPassword(String elasticsearchUserPassword) {
        this.elasticsearchUserPassword = elasticsearchUserPassword;
    }

    public String getElasticsearchTransport() {
        return elasticsearchTransport;
    }

    public void setElasticsearchTransport(String elasticsearchTransport) {
        this.elasticsearchTransport = elasticsearchTransport;
    }

    public static SCEIApplicationConfig loadFromProperties(String fileName) {
        Properties prop = new Properties();
        InputStream input = null;

        SCEIApplicationConfig config = new SCEIApplicationConfig();

        try {

            input = new FileInputStream(fileName);
            prop.load(input);

            String refreshInterval = getPropertyWithError(prop, "REFRESH_INTERVAL");
            config.setRefreshInterval(Integer.valueOf(refreshInterval));

            String lastLedgerIndexed = getPropertyWithError(prop, "LAST_LEDGER_INDEXED");
            config.setLastLedgerIndexed(Integer.valueOf(lastLedgerIndexed));

            config.setDatabaseServerName(getPropertyWithError(prop, "DB_SERVER_NAME"));
            config.setDatabaseName(getPropertyWithError(prop, "DB_NAME"));
            config.setDatabaseUser(getPropertyWithError(prop, "DB_USER"));
            config.setDatabasePassword(getPropertyWithError(prop, "DB_PASSWORD"));

            String databasePort = getPropertyWithError(prop, "DB_PORT");
            config.setDatabasePort(Integer.valueOf(databasePort));

            config.setElasticsearchClusterId(getPropertyWithError(prop, "ES_EC_CLUSTER_ID"));
            config.setElasticsearchRegion(getPropertyWithError(prop, "ES_EC_REGION"));
            config.setElasticsearchUserPassword(getPropertyWithError(prop, "ES_EC_USER_PASSWORD"));

            String elasticSearchPort = getPropertyWithError(prop, "ES_PORT");
            config.setElasticsearchPort(Integer.valueOf(elasticSearchPort));

            config.setElasticsearchTransport(getPropertyWithError(prop, "ES_TRANSPORT"));

        } catch (IOException ex) {
            String message = "Error reading configuration file: " + fileName;
            logger.error(message, ex);
            throw new RuntimeException(message);

        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    logger.error("Close error", e);
                }
            }
        }
        return config;
    }

    private static String getPropertyWithError(Properties prop, String propertyName) {
        String result = prop.getProperty(propertyName);
        if (result == null) {
            String message = propertyName + " must be set";
            logger.error(message);
            throw new RuntimeException(message);
        }
        return result;
    }

}
