package org.mineacademy.fo.settings;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mineacademy.fo.CommonCore;
import org.mineacademy.fo.ValidCore;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.fo.model.CaseNumberFormat;
import org.mineacademy.fo.model.IsInList;
import org.mineacademy.fo.model.SimpleComponent;
import org.mineacademy.fo.model.SimpleTime;
import org.mineacademy.fo.remain.RemainCore;

/**
 * A special case {@link YamlConfig} that allows static access to config.
 * <p>
 * You can only load or set values during initialization. Write "private static void init()"
 * methods in your class (and inner classes), we will invoke it automatically!
 * <p>
 * You cannot set values after the class has been loaded!
 */
public abstract class YamlStaticConfig {

	/**
	 * Represents "null" which you can use as convenience shortcut in loading config
	 * that has no internal from path.
	 */
	public static final String NO_DEFAULT = null;

	/**
	 * The temporary {@link YamlConfig} instance we store here to get values from
	 */
	private static YamlConfig TEMPORARY_INSTANCE;

	/**
	 * Internal use only: Create a new {@link YamlConfig} instance and link it to load fields via
	 * reflection.
	 */
	protected YamlStaticConfig() {
	}

	// -----------------------------------------------------------------------------------------------------
	// Main
	// -----------------------------------------------------------------------------------------------------

	/**
	 * Load the given static config class
	 *
	 * @param clazz
	 */
	public static final void load(Class<? extends YamlStaticConfig> clazz) {
		try {
			final YamlStaticConfig config = clazz.newInstance();

			TEMPORARY_INSTANCE = new YamlConfig();
			TEMPORARY_INSTANCE.setUncommentedSections(config.getUncommentedSections());

			config.load();
			config.invokeInitMethods();

			TEMPORARY_INSTANCE.save();
			TEMPORARY_INSTANCE = null;

		} catch (final Throwable t) {
			CommonCore.throwError(t, "Failed to load static settings " + clazz);
		}
	}

	/**
	 * Load your configuration here.
	 *
	 * @throws Exception
	 */
	protected abstract void load() throws Exception;

	/**
	 * See {@link #saveComments()}
	 *
	 * @return
	 */
	protected List<String> getUncommentedSections() {
		return new ArrayList<>();
	}

	/*
	 * Loads the class via reflection, scanning for "private static void init()" methods to run
	 */
	private void invokeInitMethods() {
		ValidCore.checkNotNull(TEMPORARY_INSTANCE, "Instance cannot be null " + getFileName());
		ValidCore.checkNotNull(TEMPORARY_INSTANCE.hasDefaults(), "Default config cannot be null for " + getFileName());

		try {
			// Parent class if applicable.
			if (YamlStaticConfig.class.isAssignableFrom(this.getClass().getSuperclass())) {
				final Class<?> superClass = this.getClass().getSuperclass();

				this.invokeAll(superClass);
			}

			// The class itself.
			this.invokeAll(this.getClass());

		} catch (Throwable t) {
			if (t instanceof InvocationTargetException && t.getCause() != null)
				t = t.getCause();

			RemainCore.sneaky(t);
		}
	}

	/*
	 * Invoke all "private static void init()" methods in the class and its subclasses
	 */
	private void invokeAll(final Class<?> clazz) throws Exception {
		this.invokeMethodsIn(clazz);

		// All sub-classes in superclass.
		for (final Class<?> subClazz : clazz.getDeclaredClasses())
			this.invokeAll(subClazz);
	}

	/*
	 * Invoke all "private static void init()" methods in the class
	 */
	private void invokeMethodsIn(final Class<?> clazz) throws Exception {
		for (final Method method : clazz.getDeclaredMethods()) {

			// After each invocation check if the invocation broke the plugin and ignore
			// TODO if (!Platform.getPlugin().isEnabled())
			//	return;

			final int mod = method.getModifiers();

			if (method.getName().equals("init")) {
				ValidCore.checkBoolean(Modifier.isPrivate(mod) &&
						Modifier.isStatic(mod) &&
						method.getReturnType() == Void.TYPE &&
						method.getParameterTypes().length == 0,
						"Method '" + method.getName() + "' in " + clazz + " must be 'private static void init()'");

				method.setAccessible(true);
				method.invoke(null);
			}
		}

		this.checkFields(clazz);
	}

	/*
	 * Safety check whether all fields have been set
	 */
	private void checkFields(final Class<?> clazz) throws Exception {
		if (clazz == YamlStaticConfig.class)
			return;

		for (final Field field : clazz.getDeclaredFields()) {
			field.setAccessible(true);

			if (Modifier.isPublic(field.getModifiers()))
				ValidCore.checkBoolean(!field.getType().isPrimitive(), "Field '" + field.getName() + "' in " + clazz + " must not be primitive!");

			Object result = null;

			try {
				result = field.get(null);
			} catch (final NullPointerException ex) {
			}

			ValidCore.checkNotNull(result, "Null " + field.getType().getSimpleName() + " field '" + field.getName() + "' in " + clazz);
		}
	}

	// -----------------------------------------------------------------------------------------------------
	// Delegate methods
	// -----------------------------------------------------------------------------------------------------

	protected final void loadConfiguration(String internalPath) {
		TEMPORARY_INSTANCE.loadConfiguration(internalPath, internalPath);
	}

	protected final void loadConfiguration(String from, String to) {
		TEMPORARY_INSTANCE.loadConfiguration(from, to);
	}

	protected static final void set(final String path, final Object value) {
		TEMPORARY_INSTANCE.set(path, value);
	}

	protected static final boolean isSet(final String path) {
		return TEMPORARY_INSTANCE.isSet(path);
	}

	protected static final boolean isSetDefault(final String path) {
		return TEMPORARY_INSTANCE.isSetDefault(path);
	}

	protected static final void move(final String fromRelative, final String toAbsolute) {
		TEMPORARY_INSTANCE.move(fromRelative, toAbsolute);
	}

	protected static final void setPathPrefix(final String pathPrefix) {
		TEMPORARY_INSTANCE.setPathPrefix(pathPrefix);
	}

	protected static final String getPathPrefix() {
		return TEMPORARY_INSTANCE.getPathPrefix();
	}

	protected static final String getFileName() {
		return TEMPORARY_INSTANCE.getFile().getName();
	}

	// -----------------------------------------------------------------------------------------------------
	// Config manipulators
	// -----------------------------------------------------------------------------------------------------

	protected static final List<String> getCommandList(final String path) {
		return TEMPORARY_INSTANCE.getCommandList(path);
	}

	protected static final List<String> getStringList(final String path) {
		return TEMPORARY_INSTANCE.getStringList(path);
	}

	protected static final <E> Set<E> getSet(final String path, Class<E> typeOf) {
		return TEMPORARY_INSTANCE.getSet(path, typeOf);
	}

	protected static final <E> List<E> getList(final String path, final Class<E> listType) {
		return TEMPORARY_INSTANCE.getList(path, listType);
	}

	protected static final List<SerializedMap> getMapList(final String path) {
		return TEMPORARY_INSTANCE.getMapList(path);
	}

	protected static final <K, V> Map<K, List<V>> getMapList(final String path, final Class<K> keyType, Class<V> setType, Object setDeserializerParams) {
		return TEMPORARY_INSTANCE.getMapList(path, keyType, setType, setDeserializerParams);
	}

	protected static final <E> IsInList<E> getIsInList(final String path, final Class<E> listType) {
		return TEMPORARY_INSTANCE.getIsInList(path, listType);
	}

	protected static final boolean getBoolean(final String path) {
		return TEMPORARY_INSTANCE.getBoolean(path);
	}

	protected static final SimpleComponent getComponent(final String path) {
		return TEMPORARY_INSTANCE.getComponent(path);
	}

	protected static final ZoneId getTimezone(final String path) {
		return TEMPORARY_INSTANCE.getTimezone(path);
	}

	protected static final String getString(final String path) {
		return TEMPORARY_INSTANCE.getString(path);
	}

	protected static final int getInteger(final String path) {
		return TEMPORARY_INSTANCE.getInteger(path);
	}

	protected static final double getDouble(final String path) {
		return TEMPORARY_INSTANCE.getDouble(path);
	}

	protected static final CaseNumberFormat getCaseNumberFormat(final String path) {
		return TEMPORARY_INSTANCE.getCaseNumberFormat(path);
	}

	protected static final SimpleTime getTime(final String path) {
		return TEMPORARY_INSTANCE.getTime(path);
	}

	protected static final Double getPercentage(String path) {
		return TEMPORARY_INSTANCE.getPercentage(path);
	}

	protected static final <E> E get(final String path, final Class<E> typeOf) {
		return TEMPORARY_INSTANCE.get(path, typeOf);
	}

	protected static final Object getObject(final String path) {
		return TEMPORARY_INSTANCE.getObject(path);
	}

	protected static final SerializedMap getMap(final String path) {
		return TEMPORARY_INSTANCE.getMap(path);
	}

	protected static final <Key, Value> LinkedHashMap<Key, Value> getMap(final String path, final Class<Key> keyType, final Class<Value> valueType) {
		return TEMPORARY_INSTANCE.getMap(path, keyType, valueType);
	}
}