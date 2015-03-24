package com.nhl.link.etl;

import java.util.List;
import java.util.Map;

/**
 * @since 1.3
 */
public class SourceKeysSegment {

	private List<Row> sourceRows;
	private List<Map<String, Object>> sources;

	public SourceKeysSegment(List<Row> sourceRows) {
		this.sourceRows = sourceRows;
	}

	public List<Row> getSourceRows() {
		return sourceRows;
	}

	public List<Map<String, Object>> getSources() {
		return sources;
	}

	public void setSources(List<Map<String, Object>> sources) {
		this.sources = sources;
	}

}
