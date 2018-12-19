package com.nhl.link.move.df.print;

import com.nhl.link.move.df.ArrayDataRow;
import com.nhl.link.move.df.DataRow;
import com.nhl.link.move.df.Index;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

public class TabularPrinterWorkerTest {

    private Index columns;
    private List<DataRow> rows;

    @Before
    public void initDataFrameParts() {
        this.columns = new Index("col1", "column2");
        this.rows = asList(
                new ArrayDataRow(columns, "one", 1),
                new ArrayDataRow(columns, "two", 2),
                new ArrayDataRow(columns, "three", 3),
                new ArrayDataRow(columns, "four", 4));
    }

    @Test
    public void testAppendFixedWidth() {
        assertEquals("a  ", new TabularPrinterWorker(new StringBuilder(), 3, 20).appendFixedWidth("a", 3).toString());
        assertEquals("a ", new TabularPrinterWorker(new StringBuilder(), 3, 20).appendFixedWidth("a", 2).toString());
        assertEquals("a", new TabularPrinterWorker(new StringBuilder(), 3, 20).appendFixedWidth("a", 1).toString());
        assertEquals("..", new TabularPrinterWorker(new StringBuilder(), 3, 20).appendFixedWidth("abc", 2).toString());
    }

    @Test
    public void testPrint_Full() {
        TabularPrinterWorker w = new TabularPrinterWorker(new StringBuilder(), 5, 10);

        assertEquals("" +
                "col1  column2" + System.lineSeparator() +
                "----- -------" + System.lineSeparator() +
                "one   1      " + System.lineSeparator() +
                "two   2      " + System.lineSeparator() +
                "three 3      " + System.lineSeparator() +
                "four  4      ", w.print(columns, rows.iterator()).toString());
    }

    @Test
    public void testPrint_TruncateRows() {
        TabularPrinterWorker w = new TabularPrinterWorker(new StringBuilder(), 2, 10);

        assertEquals("" +
                "col1 column2" + System.lineSeparator() +
                "---- -------" + System.lineSeparator() +
                "one  1      " + System.lineSeparator() +
                "two  2      " + System.lineSeparator() +
                "...", w.print(columns, rows.iterator()).toString());
    }

    @Test
    public void testPrint_TruncateColumns() {
        TabularPrinterWorker w = new TabularPrinterWorker(new StringBuilder(), 5, 4);

        assertEquals("" +
                "col1 c..2" + System.lineSeparator() +
                "---- ----" + System.lineSeparator() +
                "one  1   " + System.lineSeparator() +
                "two  2   " + System.lineSeparator() +
                "t..e 3   " + System.lineSeparator() +
                "four 4   ", w.print(columns, rows.iterator()).toString());
    }
}
