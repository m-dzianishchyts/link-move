package com.nhl.link.move.df;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;

public class FilteredDataFrameTest {

    @Test
    public void testIterator() {

        Index i = new Index("a");
        List<DataRow> rows = asList(
                new ArrayDataRow(i, 1),
                new ArrayDataRow(i, 4));

        FilteredDataFrame df = new FilteredDataFrame(i, rows, r -> ((int) r.get(0)) > 2);

        new DFAsserts(df, "a")
                .assertLength(1)
                .assertRow(0, 4);
    }

    @Test
    public void testIterator_Empty() {

        Index i = new Index("a");
        List<DataRow> rows = Collections.emptyList();

        FilteredDataFrame df = new FilteredDataFrame(i, rows, r -> ((int) r.get(0)) > 2);

        new DFAsserts(df, "a").assertLength(0);
    }

    @Test
    public void testIterator_NoMatch() {

        Index i = new Index("a");
        List<DataRow> rows = asList(
                new ArrayDataRow(i, 1),
                new ArrayDataRow(i, 4));

        FilteredDataFrame df = new FilteredDataFrame(i, rows, r -> ((int) r.get(0)) > 4);

        new DFAsserts(df, "a").assertLength(0);
    }

    @Test
    public void testMap() {

        Index i = new Index("a");
        DataFrame df = new FilteredDataFrame(i, asList(
                new ArrayDataRow(i, "one"),
                new ArrayDataRow(i, "two")),
                r -> r.get(0).equals("two")).map(i, r -> r.mapColumn(0, v -> v + "_"));

        new DFAsserts(df, i)
                .assertLength(1)
                .assertRow(0, "two_");
    }

    @Test
    public void testRenameColumn() {
        Index i = new Index("a", "b");

        DataFrame df = new FilteredDataFrame(i, asList(
                new ArrayDataRow(i, "one", 1),
                new ArrayDataRow(i, "two", 2)), r -> true).renameColumn("b", "c");

        new DFAsserts(df, "a", "c")
                .assertLength(2)
                .assertRow(0, "one", 1)
                .assertRow(1, "two", 2);
    }
}
