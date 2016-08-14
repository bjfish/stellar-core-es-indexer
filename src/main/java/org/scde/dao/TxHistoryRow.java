package org.scde.dao;

import java.util.Date;

/**
 * One row in the txhistory table
 */
public class TxHistoryRow {

    private String txId;
    private String txBody;
    private String txResult;
    private String txMeta;
    private int ledgerSeq;
    private Date ledgerCloseTime;

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public String getTxBody() {
        return txBody;
    }

    public void setTxBody(String txBody) {
        this.txBody = txBody;
    }

    public String getTxResult() {
        return txResult;
    }

    public void setTxResult(String txResult) {
        this.txResult = txResult;
    }

    public String getTxMeta() {
        return txMeta;
    }

    public void setTxMeta(String txMeta) {
        this.txMeta = txMeta;
    }

    public int getLedgerSeq() {
        return ledgerSeq;
    }

    public void setLedgerSeq(int ledgerSeq) {
        this.ledgerSeq = ledgerSeq;
    }

    public Date getLedgerCloseTime() {
        return ledgerCloseTime;
    }

    public void setLedgerCloseTime(Date ledgerCloseTime) {
        this.ledgerCloseTime = ledgerCloseTime;
    }
}
