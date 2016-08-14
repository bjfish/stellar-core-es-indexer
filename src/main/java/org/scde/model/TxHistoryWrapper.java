package org.scde.model;

import org.stellar.sdk.xdr.TransactionEnvelope;
import org.stellar.sdk.xdr.TransactionMeta;
import org.stellar.sdk.xdr.TransactionResultPair;

import java.util.Date;

public class TxHistoryWrapper {

    private TransactionEnvelope transactionEnvelope;
    private TransactionMeta transactionMeta;
    private TransactionResultPair transactionResultPair;
    private String transactionId;
    private int ledgerSeq;
    private Date ledgerCloseTime;

    public TxHistoryWrapper(TransactionEnvelope transactionEnvelope, TransactionMeta transactionMeta, TransactionResultPair transactionResultPair, String transactionId, int ledgerSeq, Date ledgerCloseTime) {
        this.transactionEnvelope = transactionEnvelope;
        this.transactionMeta = transactionMeta;
        this.transactionResultPair = transactionResultPair;
        this.transactionId = transactionId;
        this.ledgerSeq = ledgerSeq;
        this.ledgerCloseTime = ledgerCloseTime;
    }

    public TransactionEnvelope getTransactionEnvelope() {
        return transactionEnvelope;
    }

    public void setTransactionEnvelope(TransactionEnvelope transactionEnvelope) {
        this.transactionEnvelope = transactionEnvelope;
    }

    public TransactionMeta getTransactionMeta() {
        return transactionMeta;
    }

    public void setTransactionMeta(TransactionMeta transactionMeta) {
        this.transactionMeta = transactionMeta;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public int getLedgerSeq() {
        return ledgerSeq;
    }

    public void setLedgerSeq(int ledgerSeq) {
        this.ledgerSeq = ledgerSeq;
    }

    public TransactionResultPair getTransactionResultPair() {
        return transactionResultPair;
    }

    public void setTransactionResultPair(TransactionResultPair transactionResultPair) {
        this.transactionResultPair = transactionResultPair;
    }

    public Date getLedgerCloseTime() {
        return ledgerCloseTime;
    }

    public void setLedgerCloseTime(Date ledgerCloseTime) {
        this.ledgerCloseTime = ledgerCloseTime;
    }
}
