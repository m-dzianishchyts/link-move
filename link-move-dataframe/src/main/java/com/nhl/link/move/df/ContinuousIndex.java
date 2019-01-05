package com.nhl.link.move.df;

import com.nhl.link.move.df.map.ValueMapper;

import java.util.Map;

public class ContinuousIndex extends Index {

    protected ContinuousIndex(IndexPosition... positions) {
        super(positions);
    }

    @Override
    public Index compactIndex() {
        return this;
    }

    @Override
    public Object[] compactCopy(Object[] row, Object[] to, int toOffset) {
        System.arraycopy(row, 0, to, toOffset, positions.length);
        return to;
    }

    @Override
    public <VR> Object[] mapColumn(Object[] row, IndexPosition position, ValueMapper<Object[], VR> m) {
        Object[] newValues = compactCopy(row, new Object[size()], 0);
        position.write(newValues, m.map(row));
        return newValues;
    }

    public Index rename(Map<String, String> oldToNewNames) {

        int len = size();

        IndexPosition[] newPositions = new IndexPosition[len];
        for (int i = 0; i < len; i++) {
            IndexPosition pos = positions[i];
            String newName = oldToNewNames.get(pos.name());
            newPositions[i] = newName != null ? new IndexPosition(i, i, newName) : pos;
        }

        return new ContinuousIndex(newPositions);
    }
}
