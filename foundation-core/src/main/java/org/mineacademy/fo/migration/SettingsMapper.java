package org.mineacademy.fo.migration;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.mineacademy.fo.FileUtil;
import org.mineacademy.fo.SerializeUtilCore;
import org.mineacademy.fo.SerializeUtilCore.Language;
import org.mineacademy.fo.ValidCore;
import org.mineacademy.fo.model.CaseNumberFormat;
import org.mineacademy.fo.model.ConfigStringSerializable;
import org.mineacademy.fo.model.SimpleComponent;
import org.mineacademy.fo.settings.ConfigSection;
import org.mineacademy.fo.settings.Lang;
import org.mineacademy.fo.settings.YamlConfig;
import org.mineacademy.fo.settings.YamlStaticConfig;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @deprecated only for use to developers to export their fields to JSON
 */
@Deprecated
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SettingsMapper {

	public static <T extends YamlStaticConfig> void mapFromClassAndHalt(Class<T> clazz) {
		try {
			final JsonObject json = SettingsMapper.mapClass(clazz);

			FileUtil.write("exported-lang.json", json.toString());

		} catch (final Throwable t) {
			t.printStackTrace();
		}

		Runtime.getRuntime().halt(-1);
	}

	public static void mapLocaleYamlAndHalt(String internalPath) {
		try {
			final JsonObject json = mapLocaleYaml(internalPath);

			FileUtil.write("exported-lang.json", json.toString());

		} catch (final Throwable t) {
			t.printStackTrace();
		}

		Runtime.getRuntime().halt(-1);
	}

	private static JsonObject mapLocaleYaml(String internalPath) {
		final JsonObject dictionary = new JsonObject();
		final YamlConfig config = YamlConfig.fromInternalPath(internalPath);

		for (final Map.Entry<String, Object> entry : config.getValues(true).entrySet()) {
			final String key = entry.getKey()
					.replace("Channels", "channel")
					.replace("Commands", "command")
					.replace("Placeholders", "placeholder")
					.replace("Skill", "skill")
					.replace("Pages", "page")
					.replace("Cases", "case");

			final Object value = entry.getValue();

			if (value instanceof ConfigSection)
				continue;

			fill(key, value, dictionary);
		}

		return dictionary;
	}

	public static <T extends YamlStaticConfig> JsonObject mapClass(Class<T> clazz) throws Exception {
		final JsonObject dictionary = new JsonObject();

		try {
			YamlStaticConfig.load(clazz);

			mapClass(clazz, dictionary);

		} catch (final Exception ex) {
			ex.printStackTrace();
		}

		return dictionary;
	}

	private static JsonObject mapClass(Class<?> clazz, JsonObject dictionary) throws Exception {
		mapClassFields(clazz, dictionary);

		for (final Class<?> subClazz : clazz.getDeclaredClasses())
			mapClass(subClazz, dictionary);

		return dictionary;
	}

	private static void mapClassFields(Class<?> clazz, JsonObject dictionary) throws Exception {
		if (clazz == YamlStaticConfig.class)
			return;

		final String[] subclassPath = clazz.toString().split("\\$");
		String prefix = null;

		if (subclassPath.length == 1)
			prefix = "";
		else
			prefix = clazz.toString().replace(subclassPath[0] + "$", "").trim().replace("$", ".").toLowerCase().trim();

		for (final Field field : clazz.getDeclaredFields()) {
			field.setAccessible(true);

			if (!Modifier.isPublic(field.getModifiers()))
				throw new IllegalArgumentException("Field " + field + " must be public in " + clazz);

			final String key = (prefix.isEmpty() ? "" : prefix + "-") + field.getName();
			final Object value = field.get(null);

			ValidCore.checkNotNull(value, "Null " + field.getType().getSimpleName() + " field '" + field.getName() + "' in " + clazz);
			fill(key, value, dictionary);
		}
	}

	private static void fill(String path, Object value, JsonObject dictionary) {

		path = path.replace(".", "-").replace("_", "-").toLowerCase();

		path = path.replace("commands", "command").replace("cases", "case").replace("parts", "part").replace("upgrades", "upgrade");

		if (path.equals("conversation-error") || path.equals("menu-tooltip-info") || path.equals("version") || path.equals("server-prefix") || Lang.exists(path)) {
			System.out.println("Skipping " + path);

			return;
		}

		if (value instanceof Collection || value.getClass().isArray()) {
			final JsonArray array = new JsonArray();
			final Collection<Object> collection = value instanceof Collection ? (Collection<Object>) value : Arrays.asList((Object[]) value);

			for (final Object collectionElement : collection) {
				if (collectionElement instanceof String || ValidCore.isPrimitiveWrapper(collectionElement) || collectionElement instanceof SimpleComponent || collectionElement instanceof CaseNumberFormat)
					array.add(collectionElement.toString());
				else
					throw new IllegalArgumentException("Unsupported collection element type " + collectionElement.getClass() + " at " + path);
			}

			System.out.println("Adding " + path + " as array " + array);
			dictionary.add(path, array);

		} else if (value instanceof String || ValidCore.isPrimitiveWrapper(value) || value instanceof SimpleComponent) {

			System.out.println("Adding " + path + " as object " + value);
			dictionary.addProperty(path, value.toString());

		} else if (value instanceof Enum) {
			value = SerializeUtilCore.serialize(Language.YAML, value);

			System.out.println("Adding " + path + " as enum " + value);
			dictionary.addProperty(path, value.toString());

		} else if (value instanceof ConfigStringSerializable) {

			System.out.println("Adding " + path + " as config string serializable " + value);
			dictionary.addProperty(path, ((ConfigStringSerializable) value).serialize());

		} else
			throw new IllegalArgumentException("Unsupported object " + (value == null ? null : value.getClass()) + " at " + path);
	}

}
