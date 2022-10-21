package com.nhl.link.move;

import com.nhl.link.move.extractor.model.ExtractorName;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A single execution of an {@link LmTask}. Tracks task parameters and execution statistics.
 */
public class Execution implements AutoCloseable {

    protected final String taskName;
    protected final ExtractorName extractorName;
    protected final Map<String, ?> parameters;
    protected final Map<String, Object> attributes;
    protected final ExecutionStats stats;

    /**
     * @since 3.0
     */
    public Execution(String taskName, ExtractorName extractorName, Map<String, ?> params) {
        this.taskName = taskName;
        this.extractorName = extractorName;
        this.parameters = params;
        this.attributes = new ConcurrentHashMap<>();
        this.stats = new ExecutionStats();

        this.stats.executionStarted();
    }

    @Override
    public void close() {
        stats.executionStopped();
    }

    @Override
    public String toString() {
        return createReport().toString();
    }

    /**
     * @since 2.8
     * @deprecated since 3.0 {@link #getExtractorName()} and {@link #getTaskName()} are used instead to identify the
     * execution.
     */
    @Deprecated(since = "3.0")
    public String getName() {
        return taskName + ":" + extractorName;
    }

    /**
     * @since 3.0
     */
    public String getTaskName() {
        return taskName;
    }

    /**
     * @since 3.0
     */
    public ExtractorName getExtractorName() {
        return extractorName;
    }

    /**
     * Creates task execution report as a map of labels vs. values.
     */
    public Map<String, Object> createReport() {

        // let's keep order of insertion consistent so that the report is easily
        // printable
        Map<String, Object> report = new LinkedHashMap<>();

        report.put("Task", getName());

        for (Entry<String, ?> p : parameters.entrySet()) {
            report.put("Parameter[" + p.getKey() + "]", p.getValue());
        }

        DateFormat format = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");

        if (stats.isStopped()) {
            report.put("Status", "finished");
            report.put("Duration", stats.getDuration());
        } else {
            report.put("Status", "in progress");
            report.put("Started on ", format.format(new Date(stats.getStarted())));
        }

        report.put("Extracted", stats.getExtracted());
        report.put("Created", stats.getCreated());
        report.put("Updated", stats.getUpdated());
        report.put("Deleted", stats.getDeleted());

        return report;
    }

    /**
     * @since 1.3
     */
    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    /**
     * @since 1.3
     */
    public void setAttribute(String key, Object value) {
        if (value == null) {
            attributes.remove(key);
        } else {
            attributes.put(key, value);
        }
    }

    /**
     * @since 1.3
     */
    public Map<String, ?> getParameters() {
        return parameters;
    }

    public ExecutionStats getStats() {
        return stats;
    }

}
