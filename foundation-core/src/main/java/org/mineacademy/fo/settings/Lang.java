package org.mineacademy.fo.settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mineacademy.fo.CommonCore;
import org.mineacademy.fo.MessengerCore;
import org.mineacademy.fo.ValidCore;
import org.mineacademy.fo.exception.FoException;
import org.mineacademy.fo.model.SimpleComponent;
import org.mineacademy.fo.model.Variables;

/**
 * Represents the new way of internalization, with the greatest
 * upside of saving development time.
 *
 * The downside is that keys are not checked during load so any
 * malformed or missing key will fail later and may be unnoticed.
 */
public final class Lang extends YamlConfig {

	/**
	 * The instance of this class
	 */
	private static Lang instance;

	/*
	 * Create a new instance and load the given file
	 */
	private Lang(String filePath) {
		this.loadConfiguration(filePath);
	}

	// ------------------------------------------------------------------------------------------------------------
	// Static access - loading
	// ------------------------------------------------------------------------------------------------------------

	/**
	 * Call this method in your onPluginPreStart to use the Lang features,
	 * the Lang class will use the given file in the path below:
	 * "localization/messages_" + SimpleSettings.LOCALE_PREFIX ".yml"
	 */
	public static void init() {
		init("localization/messages_" + SimpleSettings.LOCALE_PREFIX + ".yml");
	}

	/**
	 * Call this method in your onPluginPreStart to use the Lang features,
	 * the Lang class will use the given file in the given path.
	 *
	 * Example: "localization/messages_" + SimpleSettings.LOCALE_PREFIX ".yml"
	 * @param filePath
	 */
	public static void init(String filePath) {
		instance = new Lang(filePath);

		loadPrefixes();
	}

	/**
	 * Reload the language file
	 *
	 * @deprecated internal use only
	 */
	@Deprecated
	public static void reloadLang() {
		if (instance != null) {
			instance.reload();
			instance.save();

			loadPrefixes();
		}
	}

	/**
	 * Reload prefixes from the locale file
	 *
	 * @deprecated internal use only
	 */
	@Deprecated
	public static void loadPrefixes() {
		if (instance != null) {
			if (instance.isSet("Prefix.Announce"))
				MessengerCore.setAnnouncePrefix(Lang.of("Prefix.Announce"));

			if (instance.isSet("Prefix.Error"))
				MessengerCore.setErrorPrefix(Lang.of("Prefix.Error"));

			if (instance.isSet("Prefix.Info"))
				MessengerCore.setInfoPrefix(Lang.of("Prefix.Info"));

			if (instance.isSet("Prefix.Question"))
				MessengerCore.setQuestionPrefix(Lang.of("Prefix.Question"));

			if (instance.isSet("Prefix.Success"))
				MessengerCore.setSuccessPrefix(Lang.of("Prefix.Success"));

			if (instance.isSet("Prefix.Warn"))
				MessengerCore.setWarnPrefix(Lang.of("Prefix.Warn"));

			instance.save();
		}
	}

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
		final Map<String, Object> replacementMap = new HashMap<>();

		for (int i = 0; i < replacements.length; i++)
			replacementMap.put(String.valueOf(i), replacements[i]);

		return ofVars(path, replacementMap);
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

		final SimpleComponent component = instance.getComponent(path);

		if (component == null)
			throw new FoException("Missing localization key '" + path + "' from " + instance.getFileName());

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
			throw new FoException("Missing localization key '" + path + "' from " + instance.getFileName());

		final List<SimpleComponent> components = new ArrayList<>();

		for (final SimpleComponent component : instance.getList(path, SimpleComponent.class))
			components.add(Variables.replace(component, null, CommonCore.newHashMap(replacements)));

		return components.toArray(new SimpleComponent[components.size()]);
	}

	/*
	 * Check if this class has properly been initialized
	 */
	private static void checkInit() {

		// Automatically load when not loaded in onPluginPreStart
		if (instance == null)
			init();
	}
}
