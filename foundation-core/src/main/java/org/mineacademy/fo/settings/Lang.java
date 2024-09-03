package org.mineacademy.fo.settings;

import java.util.ArrayList;
import java.util.List;

import org.mineacademy.fo.CommonCore;
import org.mineacademy.fo.ValidCore;
import org.mineacademy.fo.exception.FoException;
import org.mineacademy.fo.model.SimpleComponent;
import org.mineacademy.fo.model.Variables;

import lombok.NoArgsConstructor;

/**
 * Represents the new way of internalization, with the greatest
 * upside of saving development time.
 *
 * The downside is that keys are not checked during load so any
 * malformed or missing key will fail later and may be unnoticed.
 */
@NoArgsConstructor
public final class Lang {

	/**
	 * The instance of this class
	 */
	private static YamlConfig instance;

	// ------------------------------------------------------------------------------------------------------------
	// Getters
	// ------------------------------------------------------------------------------------------------------------

	/**
	 * Return a boolean at path
	 *
	 * @param path
	 * @return
	 */
	public static boolean getOption(String path) {
		checkInit();

		return instance.getBoolean(path);
	}

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
	public static String ofCase(long amount, String path) {
		return amount + " " + ofCaseNoAmount(amount, path);
	}

	/**
	 * Return the given key for the given amount automatically
	 * singular or plural form excluding the amount
	 *
	 * @param amount
	 * @param path
	 * @return
	 */
	public static String ofCaseNoAmount(long amount, String path) {
		final String key = ofLegacy(path);
		final String[] split = key.split(", ");

		ValidCore.checkBoolean(split.length == 1 || split.length == 2, "Invalid syntax of key at '" + path + "', this key is a special one and "
				+ "it needs singular and plural form separated with , such as: second, seconds");

		final String singular = split[0];
		final String plural = split[split.length == 2 ? 1 : 0];

		return amount == 0 || amount > 1 ? plural : singular;
	}

	/**
	 * Return a key from the localization file
	 *
	 * @param path
	 * @return
	 */
	public static String ofLegacy(String path) {
		return ofLegacyVars(path);
	}

	/**
	 * Return a key from the localization file
	 *
	 * @param path
	 * @param replacements
	 * @return
	 */
	public static String ofLegacyVars(String path, Object... replacements) {
		return ofVars(path, replacements).toLegacy();
	}

	/**
	 * Return a key from the localization file, replacing variables
	 * by their index, i.e. {0} is replaced from replacements[0], etc.
	 *
	 * @param path
	 * @return
	 */
	public static SimpleComponent ofNumericVars(String path, Object... replacements) {
		final List<Object> replacementsList = new ArrayList<>();

		for (int i = 0; i < replacements.length; i++) {
			replacementsList.add(String.valueOf(i));
			replacementsList.add(replacements[i]);
		}

		return ofVars(path, replacementsList.toArray());
	}

	/**
	 * Return a key from the localization file
	 *
	 * @param path
	 * @return
	 */
	public static SimpleComponent of(String path) {
		return ofVars(path);
	}

	/**
	 * Return a key from the localization file
	 *
	 * @param path
	 * @param replacements
	 * @return
	 */
	public static SimpleComponent ofVars(String path, Object... replacements) {
		checkInit();

		final SimpleComponent component = instance.get(path, SimpleComponent.class);

		if (component == null)
			throw new FoException("Missing localization key '" + path + "' from " + instance.getFile());

		return Variables.replace(component, null, CommonCore.newHashMap(replacements));
	}

	/**
	 * Return an array from the localization file with {0} {1} etc. variables replaced.
	 *
	 * @param path
	 * @return
	 */
	public static SimpleComponent[] ofArray(String path) {
		return ofArrayVars(path);
	}

	/**
	 * Return an array from the localization file with {0} {1} etc. variables replaced.
	 *
	 * @param path
	 * @param replacements
	 * @return
	 */
	public static SimpleComponent[] ofArrayVars(String path, Object... replacements) {
		checkInit();

		if (!instance.isSet(path))
			throw new FoException("Missing localization key '" + path + "' from " + instance.getFile());

		final List<SimpleComponent> components = new ArrayList<>();

		for (final String mini : instance.getStringList(path))
			components.add(Variables.replace(SimpleComponent.fromMini(mini), null, CommonCore.newHashMap(replacements)));

		return components.toArray(new SimpleComponent[components.size()]);
	}

	/**
	 * Return an array from the localization file with {0} {1} etc. variables replaced.
	 *
	 * @param path
	 * @param replacements
	 * @return
	 */
	public static String[] ofLegacyArrayVars(String path, Object... replacements) {
		checkInit();

		if (!instance.isSet(path))
			throw new FoException("Missing localization key '" + path + "' from " + instance.getFile());

		final List<String> lines = new ArrayList<>();

		for (final String line : instance.getStringList(path))
			lines.add(Variables.replace(line, null, CommonCore.newHashMap(replacements)));

		return CommonCore.toArray(lines);
	}

	/*
	 * Check if this class has properly been initialized
	 */
	private static void checkInit() {
		ValidCore.checkNotNull(instance, "Cannot use Lang class without localization/messages_x.yml file in your src/main/resources folder!");
	}

	/*
	 * Sets the lang instance from the other instance we borrow from SimpleLocalization
	 */
	static void setInstance(YamlConfig instance) {
		instance.setPathPrefix(null);

		Lang.instance = instance;
	}
}
