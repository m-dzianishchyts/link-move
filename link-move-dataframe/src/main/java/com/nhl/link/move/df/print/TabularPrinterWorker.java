package com.nhl.link.move.df.print;

import com.nhl.link.move.df.Index;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class TabularPrinterWorker extends BasePrinterWorker {

    TabularPrinterWorker(StringBuilder out, int maxDisplayRows, int maxDisplayColumnWith) {
        super(out, maxDisplayRows, maxDisplayColumnWith);
    }

    StringBuilder print(Index columns, Iterator<Object[]> values) {

        int width = columns.size();
        if (width == 0) {
            return out;
        }

        int[] columnWidth = new int[width];
        List<String[]> data = new ArrayList<>();

        for (int i = 0; i < width; i++) {
            columnWidth[i] = columns.getNames()[i].length();
        }

        for (int i = 0; i < maxDisplayRows; i++) {
            if (!values.hasNext()) {
                break;
            }

            Object[] dr = values.next();
            String[] drValue = new String[width];

            for (int j = 0; j < width; j++) {
                drValue[j] = String.valueOf(dr[j]);
                columnWidth[j] = Math.max(columnWidth[j], drValue[j].length());
            }

            data.add(drValue);
        }

        // constrain column width
        for (int i = 0; i < width; i++) {
            columnWidth[i] = Math.min(columnWidth[i], maxDisplayColumnWith);
        }

        // print header
        for (int i = 0; i < width; i++) {
            if (i > 0) {
                append(" ");
            }
            appendFixedWidth(columns.getNames()[i], columnWidth[i]);
        }

        // print header separator
        appendNewLine();
        for (int i = 0; i < width; i++) {
            if (i > 0) {
                append(" ");
            }

            for (int j = 0; j < columnWidth[i]; j++) {
                append("-");
            }
        }


        // print data
        for (String[] row : data) {
            appendNewLine();
            for (int i = 0; i < width; i++) {
                if (i > 0) {
                    append(" ");
                }
                appendFixedWidth(row[i], columnWidth[i]);
            }
        }

        if (values.hasNext()) {
            appendNewLine().append("...");
        }

        return out;
    }

    StringBuilder append(String string) {
        return out.append(string);
    }

    StringBuilder appendFixedWidth(String value, int width) {

        if (value.length() <= width) {
            return out.append(String.format("%1$-" + width + "s", value));
        } else {
            return out.append(truncate(value, width));
        }
    }

    private StringBuilder appendNewLine() {
        return out.append(System.lineSeparator());
    }
}
