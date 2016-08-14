package org.scde.dao;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.List;

@RegisterMapper(TxHistoryMapper.class)
public interface TxHistoryDAO {

    @SqlQuery("select txid, txbody, txresult, txmeta, tx.ledgerseq, lh.closetime from txhistory tx, ledgerheaders lh where tx.ledgerseq = lh.ledgerseq and tx.ledgerseq > :lastledgerseq order by tx.ledgerseq asc limit :limit;")
    List<TxHistoryRow> findTxHistories(@Bind("lastledgerseq") int lastledgerseq, @Bind("limit") int limit);

}
