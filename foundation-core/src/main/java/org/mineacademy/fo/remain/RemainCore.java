package org.mineacademy.fo.remain;

import java.util.Collection;
import java.util.List;

import org.mineacademy.fo.exception.FoException;

import com.google.gson.Gson;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

/**
 * Our main cross-version compatibility class.
 * <p>
 * Look up for many methods enabling you to make your plugin
 * compatible with MC 1.8.8 up to the latest version.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class RemainCore {

	/**
	 * The Google Json instance
	 */
	private final static Gson GSON = new Gson();

	// ----------------------------------------------------------------------------------------------------
	// Misc
	// ----------------------------------------------------------------------------------------------------

	/**
	 * Converts a component to JSON
	 *
	 * @param component
	 * @return
	 */
	public static String convertAdventureToJson(ComponentLike component) {
		return GsonComponentSerializer.gson().serialize(component.asComponent());
	}

	/**
	 * Serializes the component into legacy text
	 *
	 * @param component
	 * @return
	 */
	public static String convertAdventureToLegacy(ComponentLike component) {
		return LegacyComponentSerializer.legacySection().serialize(component.asComponent());
	}

	/**
	 * Serializes the component into mini message
	 *
	 * @param component
	 * @return
	 */
	public static String convertAdventureToMini(ComponentLike component) {
		return MiniMessage.miniMessage().serialize(component.asComponent());
	}

	/**
	 * Serializes the component into plain text
	 *
	 * @param component
	 * @return
	 */
	public static String convertAdventureToPlain(ComponentLike component) {
		return PlainTextComponentSerializer.plainText().serialize(component.asComponent());
	}

	/**
	 * Converts a json string to Adventure component
	 *
	 * @param json
	 * @return
	 */
	public static Component convertJsonToAdventure(String json) {
		return GsonComponentSerializer.gson().deserialize(json);
	}

	/**
	 *
	 * @param componentJson
	 * @return
	 */
	public static String convertJsonToLegacy(String componentJson) {
		return convertAdventureToLegacy(convertJsonToAdventure(componentJson));
	}

	/**
	 * Creates a new adventure component from legacy text with {@link CompChatColor#COLOR_CHAR} colors replaced
	 *
	 * @param legacyText
	 * @return
	 */
	public static Component convertLegacyToAdventure(String legacyText) {
		return LegacyComponentSerializer.legacySection().deserialize(legacyText);
	}

	/**
	 * Converts chat message with color codes to Json chat components e.g. &6Hello
	 * world converts to {text:"Hello world",color="gold"}
	 *
	 * @param message
	 * @return
	 */
	public static String convertLegacyToJson(String message) {
		return GsonComponentSerializer.gson().serialize(convertLegacyToAdventure(message));
	}

	/**
	 * Convert the given json into list
	 *
	 * @param json
	 * @return
	 */
	public static List<String> convertJsonToList(String json) {
		return GSON.fromJson(json, List.class);
	}

	/**
	 * Return the corresponding major Java version such as 8 for Java 1.8, or 11 for Java 11.
	 *
	 * @return
	 */
	public static int getJavaVersion() {
		String version = System.getProperty("java.version");

		if (version.startsWith("1."))
			version = version.substring(2, 3);

		else {
			final int dot = version.indexOf(".");

			if (dot != -1)
				version = version.substring(0, dot);
		}

		if (version.contains("-"))
			version = version.split("\\-")[0];

		return Integer.parseInt(version);
	}

	/**
	 * Converts an unchecked exception into checked
	 *
	 * @param throwable
	 */
	public static void sneaky(final Throwable throwable) {
		try {
			SneakyThrows.sneaky(throwable);

		} catch (final NoClassDefFoundError | NoSuchFieldError | NoSuchMethodError err) {
			throw new FoException(throwable);
		}
	}

	/**
	 * Return the given list as JSON
	 *
	 * @param list
	 * @return
	 */
	public static String convertListToJson(final Collection<String> list) {
		return GSON.toJson(list);
	}
}

/**
 * A wrapper for Spigot
 */
final class SneakyThrows {

	public static void sneaky(final Throwable t) {
		throw SneakyThrows.<RuntimeException>superSneaky(t);
	}

	private static <T extends Throwable> T superSneaky(final Throwable t) throws T {
		throw (T) t;
	}
}