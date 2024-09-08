package org.mineacademy.fo.settings;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.mineacademy.fo.CommonCore;
import org.mineacademy.fo.FileUtil;
import org.mineacademy.fo.ValidCore;
import org.mineacademy.fo.model.CaseNumberFormat;
import org.mineacademy.fo.model.SimpleComponent;
import org.mineacademy.fo.model.Variables;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Represents the new way of internalization, with the greatest
 * upside of saving development time.
 *
 * The downside is that keys are not checked during load so any
 * malformed or missing key will fail later and may be unnoticed.
 */
public final class Lang {

	/**
	 * The instance of this class
	 */
	private static final Lang instance = new Lang();

	/**
	 * List of URLs to overlay the default localization
	 */
	private static List<String> overlayUrls = new ArrayList<>();

	private final JsonObject dictionary;

	private Lang() {
		final JsonObject base = FileUtil.readJsonFromUrl("https://raw.githubusercontent.com/kangarko/Foundation/v7/lang/en_US.json");
		final JsonObject overlay = FileUtil.readJsonFromInternal("lang/en_US.json");

		for (final String key : overlay.keySet())
			base.add(key, overlay.get(key));

		this.dictionary = base;
	}

	private JsonArray retrieveList(String path) {
		final JsonElement element = this.retrieve(path);

		if (element.isJsonArray())
			return element.getAsJsonArray();

		final JsonArray array = new JsonArray();

		array.add(element.getAsString());
		return array;
	}

	private JsonElement retrieve(String path) {
		ValidCore.checkBoolean(this.dictionary.has(path), "Missing localization key '" + path + "'");

		return this.dictionary.get(path);
	}

	private boolean has(String path) {
		return this.dictionary.has(path);
	}

	// ------------------------------------------------------------------------------------------------------------
	// Getters
	// ------------------------------------------------------------------------------------------------------------

	/**
	 * Return a component list from the localization file with {0} {1} etc. variables replaced.
	 *
	 * @param path
	 * @param variables
	 * @return
	 */
	/*public static List<SimpleComponentCore> ofComponentList(String path, Object... variables) {
		return CommonCore.convert(ofList(path, variables), SimpleComponentCore::of);
	}*/

	/**
	 * Return a list from the localization file with {0} {1} etc. variables replaced.
	 *
	 * @param path
	 * @param variables
	 * @return
	 */
	/*public static List<String> ofList(String path, Object... variables) {
		return Arrays.asList(ofArray(path, variables));
	}*/

	/**
	 * Return an array from the localization file with {0} {1} etc. variables replaced.
	 *
	 * @param path
	 * @param variables
	 * @return
	 */
	/*public static String[] ofArray(String path, Object... variables) {
		return of(path, variables).split("\n");
	}*/

	/**
	 * Return the given key for the given amount automatically
	 * singular or plural form including the amount
	 *
	 * @param amount
	 * @param path
	 * @return
	 */
	public static String numberFormat(String path, long amount) {
		return CaseNumberFormat.fromString(plain(path)).formatWithCount(amount);
	}

	/**
	 * Return the given key for the given amount automatically
	 * singular or plural form excluding the amount
	 *
	 * @param amount
	 * @param path
	 * @return
	 */
	public static String numberFormatNoAmount(String path, long amount) {
		return CaseNumberFormat.fromString(plain(path)).formatWithoutCount(amount);
	}

	/**
	 * Return a key from the localization file
	 *
	 * @param path
	 * @param replacements
	 * @return
	 */
	public static String legacyVars(String path, Object... replacements) {
		final String value = legacy(path);

		return Variables.replace(value, null, CommonCore.newHashMap(replacements));
	}

	/**
	 * Return a key from the localization file
	 *
	 * @param path
	 * @return
	 */
	public static String legacy(String path) {
		return component(path).toLegacy();
	}

	/**
	 * Return a key from the localization file
	 *
	 * @param path
	 * @return
	 */
	public static String plain(String path) {
		return instance.retrieve(path).getAsString();
	}

	/**
	 * Return if the given key exists
	 *
	 * @param path
	 * @return
	 */
	public static boolean exists(String path) {
		return instance.has(path);
	}

	/**
	 * Return a key from the localization file, replacing variables
	 * by their index, i.e. {0} is replaced from replacements[0], etc.
	 *
	 * @param path
	 * @return
	 */
	/*public static SimpleComponent ofNumericVars(String path, Object... replacements) {
		final List<Object> replacementsList = new ArrayList<>();
	
		for (int i = 0; i < replacements.length; i++) {
			replacementsList.add(String.valueOf(i));
			replacementsList.add(replacements[i]);
		}
	
		return ofVars(path, replacementsList.toArray());
	}*/

	/**
	 * Return a key from the localization file
	 *
	 * @param path
	 * @param replacements
	 * @return
	 */
	public static SimpleComponent componentVars(String path, Object... replacements) {
		final SimpleComponent component = component(path);

		return Variables.replace(component, null, CommonCore.newHashMap(replacements));
	}

	/**
	 * Return a key from the localization file
	 *
	 * @param path
	 * @return
	 */
	public static SimpleComponent component(String path) {
		return SimpleComponent.fromMini(plain(path));
	}

	/**
	 * Return an array from the localization file with {0} {1} etc. variables replaced.
	 *
	 * @param path
	 * @return
	 */
	public static SimpleComponent[] componentArray(String path) {
		return componentArrayVars(path);
	}

	/**
	 * Return an array from the localization file with {0} {1} etc. variables replaced.
	 *
	 * @param path
	 * @param replacements
	 * @return
	 */
	public static SimpleComponent[] componentArrayVars(String path, Object... replacements) {
		final List<SimpleComponent> components = new ArrayList<>();

		for (final JsonElement listElement : instance.retrieveList(path))
			components.add(Variables.replace(SimpleComponent.fromMini(listElement.getAsString()), null, CommonCore.newHashMap(replacements)));

		return components.toArray(new SimpleComponent[components.size()]);
	}

	/**
	 * Return an array from the localization file with {0} {1} etc. variables replaced.
	 *
	 * @param path
	 * @param replacements
	 * @return
	 */
	public static String[] legacyArrayVars(String path, Object... replacements) {
		final List<String> lines = new ArrayList<>();

		for (final JsonElement listElement : instance.retrieveList(path))
			lines.add(Variables.replace(listElement.getAsString(), null, CommonCore.newHashMap(replacements)));

		return CommonCore.toArray(lines);
	}

	public static void addOverlayUrl(String url) {
		overlayUrls.add(url);
	}

	/**
	 * The default keys from the main overlay.
	 */
	public static final class Default {

		/**
		 * The {timestamp} and {date}, {date_short} and {date_month} formats.
		 */
		private static DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		private static DateFormat dateFormatShort = new SimpleDateFormat("dd.MM.yyyy HH:mm");
		private static DateFormat dateFormatMonth = new SimpleDateFormat("dd.MM HH:mm");
		private static String locationFormat = "{world} [{x}, {y}, {z}]";

		public static DateFormat getDateFormat() {
			if (dateFormat == null)
				dateFormat = makeFormat("format-date");

			return dateFormat;
		}

		public static DateFormat getDateFormatShort() {
			if (dateFormatShort == null)
				dateFormatShort = makeFormat("format-date-short");

			return dateFormatShort;
		}

		/**
		 * The format used in the {timestamp} placeholder.
		 *
		 * @return
		 */
		public static DateFormat getDateFormatMonth() {
			if (dateFormatMonth == null)
				dateFormatMonth = makeFormat("format-date-month");

			return dateFormatMonth;
		}

		/*
		 * A helper method to create a date format from the given plain lang key.
		 */
		private static DateFormat makeFormat(String key) {
			final String raw = plain(key);

			try {
				return new SimpleDateFormat(raw);

			} catch (final IllegalArgumentException ex) {
				CommonCore.throwError(ex, "Date format at '" + key + "' is invalid: '" + raw + "'! See https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html for syntax'");

				return null;
			}
		}

		/**
		 * The format used in the {location} placeholder.
		 * Supprots {world}, {x}, {y}, {z} placeholders inside of it.
		 *
		 * @return
		 */
		public static String getLocationFormat() {
			if (locationFormat == null)
				locationFormat = plain("format-location");

			return locationFormat;
		}
	}
}
