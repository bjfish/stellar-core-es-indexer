package org.scde.processor;

import org.scde.model.TxHistoryWrapper;
import org.stellar.sdk.xdr.AllowTrustOp;
import org.stellar.sdk.xdr.ChangeTrustOp;
import org.stellar.sdk.xdr.CreateAccountOp;
import org.stellar.sdk.xdr.CreatePassiveOfferOp;
import org.stellar.sdk.xdr.ManageDataOp;
import org.stellar.sdk.xdr.ManageOfferOp;
import org.stellar.sdk.xdr.PathPaymentOp;
import org.stellar.sdk.xdr.PaymentOp;
import org.stellar.sdk.xdr.SetOptionsOp;

public interface TxHistoryVisitor {

    public void visitTxHistory(TxHistoryWrapper txHistoryWrapper);

    public void visitCreateAccount(CreateAccountOp createAccountOp, TxHistoryWrapper txHistoryWrapper, int operationIndex);

    public void visitPaymentOp(PaymentOp paymentOp, TxHistoryWrapper txHistoryWrapper, int operationIndex);

    public void visitPathPaymentOp(PathPaymentOp paymentOp, TxHistoryWrapper txHistoryWrapper, int operationIndex);

    public void visitManageOffer(ManageOfferOp manageOfferOp, TxHistoryWrapper txHistoryWrapper, int operationIndex);

    public void visitCreatePassiveOffer(CreatePassiveOfferOp createPassiveOfferOp, TxHistoryWrapper txHistoryWrapper, int operationIndex);

    public void visitSetOptions(SetOptionsOp setOptionsOp, TxHistoryWrapper txHistoryWrapper, int operationIndex);

    public void visitChangeTrust(ChangeTrustOp changeTrustOp, TxHistoryWrapper txHistoryWrapper, int operationIndex);

    public void visitAllowTrust(AllowTrustOp allowTrustOp, TxHistoryWrapper txHistoryWrapper, int operationIndex);

//    public void visitAccountMerge(PathPaymentOp paymentOp, TxHistoryWrapper txHistoryWrapper, int operationIndex);

//    public void visitInflation(PathPaymentOp paymentOp, TxHistoryWrapper txHistoryWrapper, int operationIndex);

    public void visitManageData(ManageDataOp manageDataOp, TxHistoryWrapper txHistoryWrapper, int operationIndex);

    public void afterBatchesProcessed();

    public void finished();
}
