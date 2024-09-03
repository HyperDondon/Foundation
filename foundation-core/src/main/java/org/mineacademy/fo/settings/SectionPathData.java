package org.mineacademy.fo.settings;

import java.util.Collections;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents a single node in a configuration.
 */
@Getter
@Setter
final class SectionPathData {

	/**
	 * The data that is stored in this section.
	 */
	private Object data;

	/**
	 * If no comments exist, an empty list will be returned. A null entry in the
	 * list represents an empty line and an empty String represents an empty
	 * comment line.
	 */
	private List<String> comments = Collections.emptyList();

	/**
	 * If no comments exist, an empty list will be returned. A null entry in the
	 * list represents an empty line and an empty String represents an empty
	 * comment line.
	 */
	private List<String> inlineComments = Collections.emptyList();

	/**
	 * Creates a new instance of SectionPathData.
	 *
	 * @param data
	 */
	public SectionPathData(Object data) {
		this.data = data;
	}
}
