package org.mineacademy.fo;

import java.awt.Color;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.fo.collection.StrictCollection;
import org.mineacademy.fo.collection.StrictMap;
import org.mineacademy.fo.exception.FoException;
import org.mineacademy.fo.exception.SerializeFailedException;
import org.mineacademy.fo.jsonsimple.JSONArray;
import org.mineacademy.fo.jsonsimple.JSONObject;
import org.mineacademy.fo.jsonsimple.JSONParseException;
import org.mineacademy.fo.jsonsimple.JSONParser;
import org.mineacademy.fo.jsonsimple.Jsonable;
import org.mineacademy.fo.model.ConfigSerializable;
import org.mineacademy.fo.model.IsInList;
import org.mineacademy.fo.model.RangedSimpleTime;
import org.mineacademy.fo.model.RangedValue;
import org.mineacademy.fo.model.SimpleComponent;
import org.mineacademy.fo.model.SimpleTime;
import org.mineacademy.fo.remain.CompChatColor;
import org.mineacademy.fo.settings.MemorySection;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;

/**
 * Utility class for serializing objects to writeable YAML data and back.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class SerializeUtilCore {

	/**
	 * A list of custom serializers
	 */
	@Setter
	private static List<Serializer> serializers = new ArrayList<>();

	/**
	 * Converts the given object into something you can safely save in file as a string
	 *
	 * @param language determines the file that the object originated from, if unsure just set to YAML
	 * @param object
	 * @return
	 */
	public static Object serialize(Language language, Object object) {
		if (object == null)
			return null;

		for (final Serializer serializer : serializers) {
			final Object result = serializer.serialize(language, object);

			if (result != null)
				return result;
		}

		if (object instanceof ConfigSerializable)
			return serialize(language, ((ConfigSerializable) object).serialize().serialize());

		else if (object instanceof StrictCollection)
			return serialize(language, ((StrictCollection) object).serialize());

		else if (object instanceof CompChatColor)
			return ((CompChatColor) object).toSaveableString();

		else if (object instanceof UUID)
			return object.toString();

		else if (object instanceof Enum<?>)
			return object.getClass().getSimpleName().equals("ChatColor") ? ((Enum<?>) object).name() : object.toString();

		else if (object instanceof SimpleTime)
			return ((SimpleTime) object).getRaw();

		else if (object instanceof Color)
			return "#" + ((Color) object).getRGB();

		else if (object instanceof RangedValue)
			return ((RangedValue) object).toLine();

		else if (object instanceof RangedSimpleTime)
			return ((RangedSimpleTime) object).toLine();

		else if (object instanceof SimpleComponent)
			throw new FoException("Serializing SimpleComponent is ambigious, if you want to serialize it literally, call SimpleComponent#serialize().toJson(), otherwise call SimpleComponent#toAdventureJson()");

		else if (object instanceof Style) {
			final Map<String, Object> map = new HashMap<>();
			final Style style = (Style) object;

			if (style.color() != null)
				map.put("Color", style.color().asHexString());

			final List<String> decorations = new ArrayList<>();

			for (final Map.Entry<TextDecoration, State> entry : style.decorations().entrySet())
				if (entry.getValue() == State.TRUE)
					decorations.add(entry.getKey().name());

			map.put("Decorations", decorations);

			return map;
		}

		else if (object instanceof Path)
			throw new FoException("Cannot serialize Path " + object + ", did you mean to convert it into a name?");

		else if (object instanceof MemorySection) {
			return object;

		} else if (object instanceof Iterable || object.getClass().isArray() || object instanceof IsInList) {

			if (language == Language.JSON) {
				final JSONArray jsonList = new JSONArray();

				if (object instanceof Iterable || object instanceof IsInList) {
					for (Object element : object instanceof IsInList ? ((IsInList<?>) object).getList() : (Iterable<?>) object) {
						if (element == null)
							jsonList.add(null);

						if (element instanceof Jsonable)
							jsonList.add(element);

						else {
							element = serialize(Language.JSON, element);

							// Assume the element is a JSON string
							try {
								jsonList.add(JSONParser.deserialize(element.toString()));

							} catch (final JSONParseException ex) {
								final String message = ex.getMessage();

								// Apparently not a json string :/
								if (message.contains("The unexpected character") && (message.contains("was found at position 0") || message.contains("was found at position 1")))
									jsonList.add(element.toString());
								else
									CommonCore.error(ex, "Failed to deserialize JSON collection from string: " + element);
							}
						}
					}

				} else {
					final Object[] array = (Object[]) object;

					for (int i = 0; i < array.length; i++) {
						Object element = array[i];

						if (element != null) {
							if (element instanceof Jsonable)
								jsonList.add(element);

							else {
								element = serialize(Language.JSON, element);

								// Assume the element is a JSON string
								try {
									jsonList.add(JSONParser.deserialize(element.toString()));

								} catch (final JSONParseException ex) {
									final String message = ex.getMessage();

									// Apparently not a json string :/
									if (message.contains("The unexpected character") && (message.contains("was found at position 0") || message.contains("was found at position 1")))
										jsonList.add(element.toString());
									else
										CommonCore.error(ex, "Failed to deserialize JSON collection from string: " + element);
								}
							}
						}

						jsonList.add(null);
					}
				}

				return jsonList;
			}

			else {
				if (object instanceof Iterable || object instanceof IsInList) {
					final List<Object> serialized = new ArrayList<>();

					for (final Object element : object instanceof IsInList ? ((IsInList<?>) object).getList() : (Iterable<?>) object) {
						serialized.add(serialize(language, element));
					}

					return serialized;

				} else {
					// Supports Object[] as well as primitive arrays
					final int length = Array.getLength(object);
					final Object[] serialized = new Object[length];

					for (int i = 0; i < length; i++) {
						final Object element = Array.get(object, i);

						serialized[i] = serialize(language, element);
					}

					return serialized;
				}
			}

		} else if (object instanceof Map || object instanceof StrictMap) {
			final Map<Object, Object> oldMap = object instanceof StrictMap ? ((StrictMap<Object, Object>) object).getSource() : (Map<Object, Object>) object;

			if (language == Language.JSON) {
				final JSONObject json = new JSONObject();

				for (final Map.Entry<Object, Object> entry : oldMap.entrySet()) {
					final Object key = serialize(language, entry.getKey());
					final Object value = serialize(language, entry.getValue());

					if (key != null)
						ValidCore.checkBoolean(key instanceof String || key instanceof Number,
								"JSON requires Map to be translated into keys that are String or Numbers, found " + key.getClass().getSimpleName() + " key: " + key + " with value '" + value + "'");

					if (value != null)
						ValidCore.checkBoolean(value instanceof String || value instanceof Boolean || value instanceof Character || value instanceof Number || value instanceof List
								|| value instanceof JSONObject || value instanceof JSONArray,
								"JSON requires Map to be translated into values that are String or List only, found " + value.getClass().getSimpleName() + ": " + value + " for key " + key);

					if (value instanceof List) {
						final JSONArray array = new JSONArray();

						for (final Object listValue : (List<?>) value)
							if (listValue == null || listValue instanceof Boolean || listValue instanceof Character || listValue instanceof String || listValue instanceof Number
									|| listValue instanceof JSONArray || listValue instanceof JSONObject)
								array.add(listValue);

							else
								throw new FoException("JSON requires List to only contain primitive types or strings, found " + listValue.getClass().getSimpleName() + ": " + listValue);

						json.put(key == null ? null : key.toString(), array);

					} else
						json.put(key == null ? null : key.toString(), value == null ? null : value);
				}

				return json;
			}

			else {
				final Map<Object, Object> newMap = new LinkedHashMap<>();

				for (final Map.Entry<Object, Object> entry : oldMap.entrySet())
					newMap.put(serialize(language, entry.getKey()), serialize(language, entry.getValue()));

				return newMap;
			}
		}

		else if (object instanceof Pattern)
			return ((Pattern) object).pattern();

		else if (object instanceof Integer || object instanceof Double || object instanceof Float || object instanceof Long || object instanceof Short
				|| object instanceof String || object instanceof Boolean || object instanceof Character)
			return object;

		else if (object instanceof BigDecimal) {
			final BigDecimal big = (BigDecimal) object;

			return big.toPlainString();
		}

		throw new SerializeFailedException("Does not know how to serialize " + object.getClass().getSimpleName() + "! Does it extends ConfigSerializable? Data: " + object);
	}

	/**
	 * Attempts to convert the given object saved in the given mode (i.e. in a .yml file) back
	 * into its Java class, i.e. a Location.
	 *
	 * @param <T>
	 * @param language
	 * @param classOf
	 * @param object
	 * @return
	 */
	public static <T> T deserialize(@NonNull Language language, @NonNull final Class<T> classOf, @NonNull final Object object) {
		return deserialize(language, classOf, object, (Object[]) null);
	}

	/**
	 * Attempts to convert the given object saved in the given mode (i.e. in a .yml file) back
	 * into its Java class, i.e. a Location.
	 *
	 * @param <T>
	 * @param language
	 * @param classOf
	 * @param object
	 * @param parameters
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static <T> T deserialize(@NonNull Language language, @NonNull final Class<T> classOf, @NonNull Object object, final Object... parameters) {

		final boolean isJson = language == Language.JSON;

		for (final Serializer serializer : serializers) {
			final Object result = serializer.deserialize(language, classOf, object, parameters);

			if (result != null)
				return (T) result;
		}

		if (classOf == String.class)
			object = object.toString();

		else if (classOf == Integer.class)
			object = Integer.parseInt(object.toString());

		else if (classOf == Long.class)
			object = Long.decode(object.toString());

		else if (classOf == Double.class)
			object = Double.parseDouble(object.toString());

		else if (classOf == Float.class)
			object = Float.parseFloat(object.toString());

		else if (classOf == Boolean.class)
			object = Boolean.parseBoolean(object.toString());

		else if (classOf == SerializedMap.class)
			object = isJson ? SerializedMap.fromJson(object.toString()) : SerializedMap.of(object);

		else if (classOf == SimpleTime.class)
			object = SimpleTime.from(object.toString());

		else if (classOf == RangedValue.class)
			object = RangedValue.parse(object.toString());

		else if (classOf == RangedSimpleTime.class)
			object = RangedSimpleTime.parse(object.toString());

		else if (classOf == CompChatColor.class)
			object = CompChatColor.of(object.toString());

		else if (classOf == UUID.class)
			object = UUID.fromString(object.toString());

		else if (classOf == SimpleComponent.class)
			throw new FoException("Deserializing SimpleComponent is ambigious, if you want to deserialize it literally from JSON, "
					+ "use SimpleComponent$deserialize(SerializedMap.fromJson(object.toString())), otherwise call SimpleComponent#fromMini");

		else if (classOf == Style.class) {
			final SerializedMap map = SerializedMap.of(object);
			Style.Builder style = Style.style();

			if (map.containsKey("Color"))
				style = style.color(TextColor.fromHexString(map.getString("Color")));

			if (map.containsKey("Decorations"))
				for (final String decoration : map.getStringList("Decorations"))
					style = style.decorate(TextDecoration.valueOf(decoration));

			object = style.build();
		}

		else if (Enum.class.isAssignableFrom(classOf)) {
			object = ReflectionUtilCore.lookupEnum((Class<Enum>) classOf, object.toString());

			if (object == null)
				return null;
		}

		else if (Color.class.isAssignableFrom(classOf))
			object = CompChatColor.of(object.toString()).getColor();

		else if (List.class.isAssignableFrom(classOf) && object instanceof List) {
			// Good

		} else if (Map.class.isAssignableFrom(classOf)) {
			if (object instanceof Map)
				return (T) object;

			if (isJson)
				return (T) SerializedMap.fromJson(object.toString()).asMap();

			throw new SerializeFailedException("Does not know how to turn " + object.getClass().getSimpleName() + " into a Map! (Keep in mind we can only serialize into Map<String, Object> Data: " + object);

		} else if (classOf.isArray()) {
			final Class<?> arrayType = classOf.getComponentType();
			T[] array;

			if (object instanceof List) {
				final List<?> rawList = (List<?>) object;
				array = (T[]) Array.newInstance(classOf.getComponentType(), rawList.size());

				for (int i = 0; i < rawList.size(); i++) {
					final Object element = rawList.get(i);

					array[i] = element == null ? null : (T) deserialize(language, arrayType, element, (Object[]) null);
				}
			}

			else {
				final Object[] rawArray = (Object[]) object;
				array = (T[]) Array.newInstance(classOf.getComponentType(), rawArray.length);

				for (int i = 0; i < array.length; i++)
					array[i] = rawArray[i] == null ? null : (T) deserialize(language, classOf.getComponentType(), rawArray[i], (Object[]) null);
			}

			return (T) array;
		}

		// Try to call our own serializers
		else if (ConfigSerializable.class.isAssignableFrom(classOf)) {
			if (parameters != null && parameters.length > 0) {
				final List<Class<?>> argumentClasses = new ArrayList<>();
				final List<Object> arguments = new ArrayList<>();

				// Build parameters
				argumentClasses.add(SerializedMap.class);
				for (final Object param : parameters)
					argumentClasses.add(param.getClass());

				// Build parameter instances
				arguments.add(isJson ? SerializedMap.fromJson(object.toString()) : SerializedMap.of(object));
				Collections.addAll(arguments, parameters);

				// Find deserialize(SerializedMap, args[]) method
				final Method deserialize = ReflectionUtilCore.getMethod(classOf, "deserialize", argumentClasses.toArray(new Class[argumentClasses.size()]));

				ValidCore.checkNotNull(deserialize,
						"Expected " + classOf.getSimpleName() + " to have a public static deserialize(SerializedMap, " + CommonCore.join(argumentClasses) + ") method to deserialize: " + object + " when params were given: " + CommonCore.join(parameters));

				ValidCore.checkBoolean(argumentClasses.size() == arguments.size(),
						classOf.getSimpleName() + "#deserialize(SerializedMap, " + argumentClasses.size() + " args) expected, " + arguments.size() + " given to deserialize: " + object);

				return ReflectionUtilCore.invokeStatic(deserialize, arguments.toArray());
			}

			final Method deserialize = ReflectionUtilCore.getMethod(classOf, "deserialize", SerializedMap.class);

			if (deserialize != null)
				return ReflectionUtilCore.invokeStatic(deserialize, isJson ? SerializedMap.fromJson(object.toString()) : SerializedMap.of(object));

			throw new SerializeFailedException("Unable to deserialize " + classOf.getSimpleName()
					+ ", please write 'public static deserialize(SerializedMap map) or deserialize(SerializedMap map, X arg1, Y arg2, etc.) method to deserialize: " + object);
		}

		// Step 3 - Search for "getByName" method used by us or some Bukkit classes such as Enchantment
		else if (object instanceof String) {
			final Method method = ReflectionUtilCore.getMethod(classOf, "getByName", String.class);

			if (method != null)
				return ReflectionUtilCore.invokeStatic(method, object);
		}

		else if (classOf == Object.class) {
			// Good
		}

		else if (MemorySection.class.isAssignableFrom(classOf)) {
			// Good

		} else
			throw new SerializeFailedException("Does not know how to turn " + classOf + " into a serialized object from data: " + object);

		return (T) object;
	}

	/**
	 * Adds a custom serializer for serializing objects into strings
	 *
	 * @param <T>
	 * @param handler
	 */
	public static <T> void addSerializer(Serializer handler) {
		serializers.add(handler);
	}

	/**
	 * A custom serializer for serializing objects into strings
	 */
	public interface Serializer {

		/**
		 * Turn the given object into something we can save inside the given config language, for most cases this is a string.
		 *
		 * @param language
		 * @param object
		 * @return
		 */
		Object serialize(Language language, Object object);

		<T> T deserialize(@NonNull Language language, @NonNull final Class<T> classOf, @NonNull Object object, final Object... parameters);
	}

	/**
	 * The markup language the objects should be serialized to or deserialized from
	 */
	public enum Language {
		JSON,
		YAML
	}
}
