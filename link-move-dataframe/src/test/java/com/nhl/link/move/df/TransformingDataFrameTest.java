package com.nhl.link.move.df;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

public class TransformingDataFrameTest {

    @Test
    public void testIterator() {

        Index i = new Index("a", "b");
        List<DataRow> rows = asList(
                new ArrayDataRow(i, "one", 1),
                new ArrayDataRow(i, "two", 2));

        TransformingDataFrame df = new TransformingDataFrame(i, rows, DataRow::copyValues);

        new DFAsserts(df, "a", "b")
                .assertLength(2)
                .assertRow(0, "one", 1)
                .assertRow(1, "two", 2);
    }

    @Test
    public void testHead() {

        Index columns = new Index("a", "b");

        DataFrame df = new TransformingDataFrame(columns, asList(
                new ArrayDataRow(columns, "one", 1),
                new ArrayDataRow(columns, "two", 2),
                new ArrayDataRow(columns, "three", 3)), DataRow::copyValues).head(2);

        new DFAsserts(df, columns)
                .assertLength(2)
                .assertRow(0, "one", 1)
                .assertRow(1, "two", 2);
    }

    @Test
    public void testRenameColumn() {
        Index i = new Index("a", "b");

        DataFrame df = new TransformingDataFrame(i, asList(
                new ArrayDataRow(i, "one", 1),
                new ArrayDataRow(i, "two", 2)), DataRow::copyValues).renameColumn("b", "c");

        new DFAsserts(df, "a", "c")
                .assertLength(2)
                .assertRow(0, "one", 1)
                .assertRow(1, "two", 2);
    }

    @Test
    public void testMapColumn() {

        Index i = new Index("a", "b");

        DataFrame df = new TransformingDataFrame(i, asList(
                new ArrayDataRow(i, "one", 1),
                new ArrayDataRow(i, "two", 2)), DataRow::copyValues).mapColumn("b", Object::toString);

        new DFAsserts(df, "a", "b")
                .assertLength(2)
                .assertRow(0, "one", "1")
                .assertRow(1, "two", "2");
    }

    @Test
    public void testMap() {

        Index i = new Index("a", "b");

        DataFrame df = new TransformingDataFrame(i, asList(
                new ArrayDataRow(i, "one", 1),
                new ArrayDataRow(i, "two", 2)), DataRow::copyValues)
                .map(i, r -> r.mapColumn(0, (String v) -> v + "_"));

        new DFAsserts(df, "a", "b")
                .assertLength(2)
                .assertRow(0, "one_", 1)
                .assertRow(1, "two_", 2);
    }

    @Test
    public void testMap_ChangeRowStructure() {

        Index i = new Index("a", "b");
        Index i1 = new Index("c", "d", "e");

        DataFrame df = new TransformingDataFrame(i, asList(
                new ArrayDataRow(i, "one", 1),
                new ArrayDataRow(i, "two", 2)), DataRow::copyValues)
                .map(i1, r -> DataRow.values(
                        r.get(0),
                        ((int) r.get(1)) * 10,
                        r.get(1)));

        new DFAsserts(df, i1)
                .assertLength(2)
                .assertRow(0, "one", 10, 1)
                .assertRow(1, "two", 20, 2);
    }

    @Test
    public void testMap_ChangeRowStructure_Chained() {

        Index i = new Index("a", "b");
        Index i1 = new Index("c", "d", "e");
        Index i2 = new Index("f", "g");

        DataFrame df = new TransformingDataFrame(i, asList(
                new ArrayDataRow(i, "one", 1),
                new ArrayDataRow(i, "two", 2)), DataRow::copyValues)
                .map(i1, r -> DataRow.values(
                        r.get(0),
                        ((int) r.get(1)) * 10,
                        r.get(1)))
                .map(i2, r -> DataRow.values(
                        r.get(0),
                        r.get(1)));

        new DFAsserts(df, i2)
                .assertLength(2)
                .assertRow(0, "one", 10)
                .assertRow(1, "two", 20);
    }

    @Test
    public void testMap_ChangeRowStructure_EmptyDF() {

        Index i = new Index("a", "b");
        Index i1 = new Index("c", "d", "e");

        DataFrame df = new TransformingDataFrame(i, Collections.emptyList(), DataRow::copyValues)
                .map(i1, r -> DataRow.values(
                        r.get(0),
                        ((int) r.get(1)) * 10,
                        r.get(1)));

        assertSame(i1, df.getColumns());

        new DFAsserts(df, i1).assertLength(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMap_Index_Row_SizeMismatch() {

        Index i = new Index("a", "b");
        Index i1 = new Index("c", "d", "e");

        new TransformingDataFrame(i, asList(
                new ArrayDataRow(i, "one", 1),
                new ArrayDataRow(i, "two", 2)), DataRow::copyValues)
                .map(i1, r -> DataRow.values(
                        r.get(0),
                        r.get(1)))
                // must throw when iterating due to inconsistent mapped row structure...
                .forEach(r -> {
                });
    }

    @Test
    public void testToString() {
        Index i = new Index("a", "b");
        DataFrame df = new TransformingDataFrame(i, asList(
                new ArrayDataRow(i, "one", 1),
                new ArrayDataRow(i, "two", 2),
                new ArrayDataRow(i, "three", 3),
                new ArrayDataRow(i, "four", 4)), DataRow::copyValues);

        assertEquals("LazyDataFrame [{a:one,b:1},{a:two,b:2},{a:three,b:3},...]", df.toString());
    }

    @Test
    public void testZip() {

        Index i1 = new Index("a");
        DataFrame df1 = new TransformingDataFrame(i1, asList(
                new ArrayDataRow(i1, 1),
                new ArrayDataRow(i1, 2)), DataRow::copyValues);

        Index i2 = new Index("b");
        DataFrame df2 = new TransformingDataFrame(i2, asList(
                new ArrayDataRow(i2, 10),
                new ArrayDataRow(i2, 20)), DataRow::copyValues);

        DataFrame df = df1.zip(df2);
        new DFAsserts(df, "a", "b")
                .assertLength(2)
                .assertRow(0, 1, 10)
                .assertRow(1, 2, 20);
    }

    @Test
    public void testZip_Self() {

        Index i1 = new Index("a");
        DataFrame df1 = new TransformingDataFrame(i1, asList(
                new ArrayDataRow(i1, 1),
                new ArrayDataRow(i1, 2)), DataRow::copyValues);

        DataFrame df = df1.zip(df1);

        new DFAsserts(df, "a", "a_")
                .assertLength(2)
                .assertRow(0, 1, 1)
                .assertRow(1, 2, 2);
    }

    @Test
    public void testZip_LeftIsShorter() {

        Index i1 = new Index("a");
        DataFrame df1 = new TransformingDataFrame(i1, asList(
                new ArrayDataRow(i1, 2)), DataRow::copyValues);

        Index i2 = new Index("b");
        DataFrame df2 = new TransformingDataFrame(i2, asList(
                new ArrayDataRow(i2, 10),
                new ArrayDataRow(i2, 20)), DataRow::copyValues);

        DataFrame df = df1.zip(df2);
        new DFAsserts(df, "a", "b")
                .assertLength(1)
                .assertRow(0, 2, 10);
    }

    @Test
    public void testZip_RightIsShorter() {

        Index i1 = new Index("a");
        DataFrame df1 = new TransformingDataFrame(i1, asList(
                new ArrayDataRow(i1, 2)), DataRow::copyValues);

        Index i2 = new Index("b");
        DataFrame df2 = new TransformingDataFrame(i2, asList(
                new ArrayDataRow(i2, 10),
                new ArrayDataRow(i2, 20)), DataRow::copyValues);

        DataFrame df = df2.zip(df1);
        new DFAsserts(df, "b", "a")
                .assertLength(1)
                .assertRow(0, 10, 2);
    }

    @Test
    public void testFilter() {

        Index i1 = new Index("a");
        DataFrame df1 = new TransformingDataFrame(i1, asList(
                new ArrayDataRow(i1, 10),
                new ArrayDataRow(i1, 20)), DataRow::copyValues);

        DataFrame df = df1.filter(r -> ((int) r.get(0)) > 15);
        new DFAsserts(df, "a")
                .assertLength(1)
                .assertRow(0, 20);
    }
}
