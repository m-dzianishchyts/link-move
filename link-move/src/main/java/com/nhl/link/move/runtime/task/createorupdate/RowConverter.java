package com.nhl.link.move.runtime.task.createorupdate;

import com.nhl.dflib.DataFrame;
import com.nhl.dflib.Index;
import com.nhl.dflib.IndexPosition;
import com.nhl.dflib.row.RowBuilder;
import com.nhl.dflib.row.RowProxy;
import com.nhl.link.move.RowAttribute;
import com.nhl.link.move.runtime.targetmodel.TargetAttribute;
import com.nhl.link.move.runtime.targetmodel.TargetEntity;
import com.nhl.link.move.valueconverter.ValueConverterFactory;

import java.util.Optional;

/**
 * Re-maps a list of source rows to a Map with keys in the target namespace.
 *
 * @since 1.3
 */
public class RowConverter {

    private TargetEntity targetEntity;
    private ValueConverterFactory converterFactory;

    public RowConverter(TargetEntity targetEntity, ValueConverterFactory converterFactory) {
        this.targetEntity = targetEntity;
        this.converterFactory = converterFactory;
    }

    public DataFrame convert(RowAttribute[] rowHeader, DataFrame df) {
        return df.map(
                convertColumns(rowHeader, df.getColumns()),
                (f, t) -> convert(rowHeader, f, t));
    }

    private Index convertColumns(RowAttribute[] rowHeader, Index columns) {

        IndexPosition[] positions = columns.getPositions();
        String[] names = new String[positions.length];

        for (int i = 0; i < positions.length; i++) {

            String targetPath = rowHeader[i].getTargetPath();
            Optional<TargetAttribute> attribute = targetEntity.getAttribute(targetPath);
            names[i] = attribute.map(TargetAttribute::getNormalizedPath).orElse(targetPath);
        }

        return Index.withNames(names);
    }

    private void convert(RowAttribute[] rowHeader, RowProxy from, RowBuilder to) {

        IndexPosition[] positions = from.getIndex().getPositions();
        int len = positions.length;
        from.copy(to);

        for (int i = 0; i < len; i++) {

            // TODO: should we "compile" this info for the header upfront and apply it to every row? In many cases
            //  we won't even need to copy rows ...

            String targetPath = rowHeader[i].getTargetPath();
            Optional<TargetAttribute> attribute = targetEntity.getAttribute(targetPath);
            if (attribute.isPresent()) {
                to.set(i, convertValue(attribute.get(), from.get(i)));
            }
        }
    }

    private Object convertValue(TargetAttribute attribute, Object value) {
        return value != null
                ? converterFactory.getConverter(attribute.getJavaType()).convert(value, attribute.getScale())
                : null;
    }
}
