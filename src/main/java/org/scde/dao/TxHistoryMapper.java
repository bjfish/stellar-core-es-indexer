package org.scde.dao;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class TxHistoryMapper implements ResultSetMapper<TxHistoryRow> {

    public static final int TIME_MULTIPLIER = 1000;

    @Override
    public TxHistoryRow map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        TxHistoryRow result = new TxHistoryRow();
        result.setTxId(r.getString("txid"));
        result.setTxBody(r.getString("txbody"));
        result.setTxResult(r.getString("txresult"));
        result.setTxMeta(r.getString("txmeta"));
        result.setLedgerSeq(r.getInt("ledgerseq"));
        result.setLedgerCloseTime(new Date(r.getLong("closetime") * TIME_MULTIPLIER));
        return result;
    }

}
