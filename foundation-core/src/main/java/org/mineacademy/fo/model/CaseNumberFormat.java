package org.mineacademy.fo.model;

import org.mineacademy.fo.exception.FoException;

/**
 * Language-specific helper to deal with different cases when i.e. counting:
 *
 * "Please wait 1 second before your next message."
 *
 * In flexible languages such as Slovak, the case is changed three times:
 * 0 or 5+ seconds = 5 sekúnd
 * 1 = 1 sekundu
 * 2-4 = 2 sekundy
 *
 * This helper is used to automatically determine and get the right case. We
 * save all three values on a single line split by a comma.
 */
public final class CaseNumberFormat implements ConfigStringSerializable {

	private final String raw;
	private final String accusativeSingural; // 1 second (Slovak case - sekundu)
	private final String accusativePlural; // 2-4 seconds (Slovak case - sekundy, not in English)
	private final String genitivePlural; // 0 or 5+ seconds (Slovak case - sekund)

	private CaseNumberFormat(final String raw) {
		this.raw = raw;

		final String[] values = raw.split(", ");

		if (values.length == 2) {
			this.accusativeSingural = values[0];
			this.accusativePlural = values[1];
			this.genitivePlural = this.accusativePlural;

			return;
		}

		if (values.length != 3)
			throw new FoException("Malformed type, use format: 'second, seconds' OR 'sekundu, sekundy, sekund' (if your language has it)");

		this.accusativeSingural = values[0];
		this.accusativePlural = values[1];
		this.genitivePlural = values[2];
	}

	public String getPlural() {
		return this.genitivePlural;
	}

	public String formatWithCount(final long count) {
		return count + " " + this.formatWithoutCount(count);
	}

	public String formatWithoutCount(final long count) {
		if (count == 1)
			return this.accusativeSingural;

		if (count > 1 && count < 5)
			return this.accusativePlural;

		return this.genitivePlural;
	}

	@Override
	public String toString() {
		return this.serialize();
	}

	@Override
	public String serialize() {
		return this.raw;
	}

	public static CaseNumberFormat fromString(String line) {
		return new CaseNumberFormat(line);
	}

	public static CaseNumberFormat of(String singular, String plural) {
		return new CaseNumberFormat(singular + ", " + plural);
	}
}