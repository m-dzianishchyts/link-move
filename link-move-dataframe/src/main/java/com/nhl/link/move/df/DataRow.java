package com.nhl.link.move.df;

public interface DataRow {

    Object get(String columnName);

    Object get(int position);

    Index getIndex();

    DataRow reindex(Index columns);

    <V, VR> DataRow mapColumn(int position, ValueMapper<V, VR> m);

    default int size() {
        return getIndex().size();
    }
}
