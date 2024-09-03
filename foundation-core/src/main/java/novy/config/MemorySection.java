package novy.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mineacademy.fo.ValidCore;

import lombok.NonNull;

/**
 * A type of configuration that is stored in memory.
 */
public class MemorySection {

	private static final char PATH_SEPARATOR = '.';

	protected final Map<String, SectionPathData> map = new LinkedHashMap<>();

	private final FileConfiguration root;
	private final MemorySection parent;
	private final String path;
	private final String fullPath;

	private final boolean copyDefaults = true;

	/**
	 * Creates an empty MemorySection for use as a root configuration
	 * section.
	 * <p>
	 * Note that calling this without being yourself a configuration
	 * will throw an exception!
	 *
	 * @throws IllegalStateException Thrown if this is not a {@link
	 *     Configuration} root.
	 */
	protected MemorySection() {
		if (!(this instanceof FileConfiguration))
			throw new IllegalStateException("Cannot use MemorySection without being a FileConfiguration");

		this.path = "";
		this.fullPath = "";
		this.parent = null;
		this.root = (FileConfiguration) this;
	}

	/**
	 * Creates an empty MemorySection with the specified parent and path.
	 *
	 * @param parent Parent section that contains this own section.
	 * @param path Path that you may access this section from via the root
	 *     configuration.
	 * @throws IllegalArgumentException Thrown is parent or path is null, or
	 *     if parent contains no root Configuration.
	 */
	protected MemorySection(@NonNull MemorySection parent, @NonNull String path) {
		this.path = path;
		this.parent = parent;
		this.root = parent.getRoot();

		ValidCore.checkNotNull(this.root, "Path cannot be orphaned");

		this.fullPath = createPath(parent, path);
	}

	public final Set<String> getKeys(boolean deep) {
		final Set<String> result = new LinkedHashSet<>();

		final MemorySection root = this.getRoot();
		if (root != null && this.copyDefaults) {
			final MemorySection defaults = this.getDefaultSection();

			if (defaults != null)
				result.addAll(defaults.getKeys(deep));
		}

		this.mapChildrenKeys(result, this, deep);

		return result;
	}

	public final Map<String, Object> getValues(boolean deep) {
		final Map<String, Object> result = new LinkedHashMap<>();

		final MemorySection root = this.getRoot();
		if (root != null && this.copyDefaults) {
			final MemorySection defaults = this.getDefaultSection();

			if (defaults != null)
				result.putAll(defaults.getValues(deep));
		}

		this.mapChildrenValues(result, this, deep);

		return result;
	}

	private boolean contains(String path) {
		return this.contains(path, false);
	}

	private boolean contains(String path, boolean ignoreDefault) {
		return ((ignoreDefault) ? this.getObject(path, null) : this.getObject(path)) != null;
	}

	public final boolean isSet(String path) {
		final MemorySection root = this.getRoot();

		if (root == null)
			return false;

		if (this.copyDefaults)
			return this.contains(path);

		return this.getObject(path, null) != null;
	}

	private String getName() {
		return this.path;
	}

	final String getFullPath() {
		return fullPath;
	}

	private FileConfiguration getRoot() {
		return this.root;
	}

	MemorySection getParent() {
		return this.parent;
	}

	private MemorySection getDefaultSection() {
		final FileConfiguration root = this.getRoot();
		final MemorySection defaults = root == null ? null : root.getDefaults();

		if (defaults != null)
			if (defaults.isConfigurationSection(this.fullPath))
				return defaults.getConfigurationSection(this.fullPath);

		return null;
	}

	public final void set(String path, Object value) {
		ValidCore.checkNotEmpty(path, "Cannot set to an empty path");

		final MemorySection root = this.getRoot();
		if (root == null)
			throw new IllegalStateException("Cannot use section without a root");

		// i1 is the leading (higher) index
		// i2 is the trailing (lower) index
		int i1 = -1, i2;
		MemorySection section = this;
		while ((i1 = path.indexOf(PATH_SEPARATOR, i2 = i1 + 1)) != -1) {
			final String node = path.substring(i2, i1);
			final MemorySection subSection = section.getConfigurationSection(node);
			if (subSection == null) {
				if (value == null)
					// no need to create missing sub-sections if we want to remove the value:
					return;
				section = section.createSection(node);
			} else
				section = subSection;
		}

		final String key = path.substring(i2);
		if (section == this) {
			if (value == null)
				this.map.remove(key);
			else {
				final SectionPathData entry = this.map.get(key);
				if (entry == null)
					this.map.put(key, new SectionPathData(value));
				else
					entry.setData(value);
			}
		} else
			section.set(key, value);
	}

	public final Object getObject(String path) {
		return this.getObject(path, this.getDefault(path));
	}

	public final Object getObject(@NonNull String path, Object def) {
		if (path.length() == 0)
			return this;

		final MemorySection root = this.getRoot();
		if (root == null)
			throw new IllegalStateException("Cannot access section without a root");

		// i1 is the leading (higher) index
		// i2 is the trailing (lower) index
		int i1 = -1, i2;
		MemorySection section = this;
		while ((i1 = path.indexOf(PATH_SEPARATOR, i2 = i1 + 1)) != -1) {
			final String currentPath = path.substring(i2, i1);
			if (!section.contains(currentPath, true))
				return def;
			section = section.getConfigurationSection(currentPath);
			if (section == null)
				return def;
		}

		final String key = path.substring(i2);
		if (section == this) {
			final SectionPathData result = this.map.get(key);
			return (result == null) ? def : result.getData();
		}
		return section.getObject(key, def);
	}

	MemorySection createSection(String path) {
		ValidCore.checkNotEmpty(path, "Cannot create section at empty path");

		final MemorySection root = this.getRoot();
		if (root == null)
			throw new IllegalStateException("Cannot create section without a root");

		// i1 is the leading (higher) index
		// i2 is the trailing (lower) index
		int i1 = -1, i2;
		MemorySection section = this;
		while ((i1 = path.indexOf(PATH_SEPARATOR, i2 = i1 + 1)) != -1) {
			final String node = path.substring(i2, i1);
			final MemorySection subSection = section.getConfigurationSection(node);
			if (subSection == null)
				section = section.createSection(node);
			else
				section = subSection;
		}

		final String key = path.substring(i2);
		if (section == this) {
			final MemorySection result = new MemorySection(this, key);
			this.map.put(key, new SectionPathData(result));
			return result;
		}
		return section.createSection(key);
	}

	MemorySection createSection(String path, Map<?, ?> map) {
		final MemorySection section = this.createSection(path);

		for (final Map.Entry<?, ?> entry : map.entrySet())
			if (entry.getValue() instanceof Map)
				section.createSection(entry.getKey().toString(), (Map<?, ?>) entry.getValue());
			else
				section.set(entry.getKey().toString(), entry.getValue());

		return section;
	}

	// Primitives

	public final String getString(String path) {
		final Object def = this.getDefault(path);
		return this.getString(path, def != null ? def.toString() : null);
	}

	public final String getString(String path, String def) {
		final Object val = this.getObject(path, def);
		return (val != null) ? val.toString() : def;
	}

	public final int getInt(String path) {
		final Object def = this.getDefault(path);

		return this.getInt(path, (def instanceof Number) ? toInt(def) : 0);
	}

	public final int getInt(String path, int def) {
		final Object val = this.getObject(path, def);

		return (val instanceof Number) ? toInt(val) : def;
	}

	public final boolean getBoolean(String path) {
		final Object def = this.getDefault(path);

		return this.getBoolean(path, (def instanceof Boolean) ? (Boolean) def : false);
	}

	public final boolean getBoolean(String path, boolean def) {
		final Object val = this.getObject(path, def);

		return (val instanceof Boolean) ? (Boolean) val : def;
	}

	public final double getDouble(String path) {
		final Object def = this.getDefault(path);

		return this.getDouble(path, (def instanceof Number) ? toDouble(def) : 0);
	}

	public final double getDouble(String path, double def) {
		final Object val = this.getObject(path, def);

		return (val instanceof Number) ? toDouble(val) : def;
	}

	public final long getLong(String path) {
		final Object def = this.getDefault(path);

		return this.getLong(path, (def instanceof Number) ? toLong(def) : 0);
	}

	public final long getLong(String path, long def) {
		final Object val = this.getObject(path, def);

		return (val instanceof Number) ? toLong(val) : def;
	}

	// Java

	public final List<?> getList(String path) {
		final Object def = this.getDefault(path);
		return this.getList(path, (def instanceof List) ? (List<?>) def : null);
	}

	public final List<?> getList(String path, List<?> def) {
		final Object val = this.getObject(path, def);
		return (List<?>) ((val instanceof List) ? val : def);
	}

	public final List<String> getStringList(String path) {
		final List<?> list = this.getList(path);

		if (list == null)
			return new ArrayList<>(0);

		final List<String> result = new ArrayList<>();

		for (final Object object : list)
			if ((object instanceof String) || (this.isPrimitiveWrapper(object)))
				result.add(String.valueOf(object));

		return result;
	}

	public final List<Integer> getIntegerList(String path) {
		final List<?> list = this.getList(path);

		if (list == null)
			return new ArrayList<>(0);

		final List<Integer> result = new ArrayList<>();

		for (final Object object : list)
			if (object instanceof Integer)
				result.add((Integer) object);

			else if (object instanceof String)
				try {
					result.add(Integer.valueOf((String) object));
				} catch (final Exception ex) {
				}

			else if (object instanceof Character)
				result.add((int) ((Character) object).charValue());

			else if (object instanceof Number)
				result.add(((Number) object).intValue());

		return result;
	}

	public final List<Boolean> getBooleanList(String path) {
		final List<?> list = this.getList(path);

		if (list == null)
			return new ArrayList<>(0);

		final List<Boolean> result = new ArrayList<>();

		for (final Object object : list)
			if (object instanceof Boolean)
				result.add((Boolean) object);

			else if (object instanceof String)
				if (Boolean.TRUE.toString().equals(object))
					result.add(true);

				else if (Boolean.FALSE.toString().equals(object))
					result.add(false);

		return result;
	}

	public final List<Double> getDoubleList(String path) {
		final List<?> list = this.getList(path);

		if (list == null)
			return new ArrayList<>(0);

		final List<Double> result = new ArrayList<>();

		for (final Object object : list)
			if (object instanceof Double)
				result.add((Double) object);

			else if (object instanceof String)
				try {
					result.add(Double.valueOf((String) object));
				} catch (final Exception ex) {
				}

			else if (object instanceof Character)
				result.add((double) ((Character) object).charValue());

			else if (object instanceof Number)
				result.add(((Number) object).doubleValue());

		return result;
	}

	public final List<Long> getLongList(String path) {
		final List<?> list = this.getList(path);

		if (list == null)
			return new ArrayList<>(0);

		final List<Long> result = new ArrayList<>();

		for (final Object object : list)
			if (object instanceof Long)
				result.add((Long) object);

			else if (object instanceof String)
				try {
					result.add(Long.valueOf((String) object));
				} catch (final Exception ex) {
				}

			else if (object instanceof Character)
				result.add((long) ((Character) object).charValue());

			else if (object instanceof Number)
				result.add(((Number) object).longValue());

		return result;
	}

	public final List<Map<?, ?>> getMapList(String path) {
		final List<?> list = this.getList(path);
		final List<Map<?, ?>> result = new ArrayList<>();

		if (list == null)
			return result;

		for (final Object object : list)
			if (object instanceof Map)
				result.add((Map<?, ?>) object);

		return result;
	}

	// Bukkit

	/*public final <T extends ConfigurationSerializable> T getSerializable(String path, Class<T> clazz) {
		return this.getObject(path, clazz);
	}

	public final <T extends ConfigurationSerializable> T getSerializable(String path, Class<T> clazz, T def) {
		return this.getObject(path, clazz, def);
	}*/

	public final MemorySection getConfigurationSection(String path) {
		Object val = this.getObject(path, null);

		if (val != null)
			return (val instanceof MemorySection) ? (MemorySection) val : null;

		val = this.getObject(path, this.getDefault(path));

		return (val instanceof MemorySection) ? this.createSection(path) : null;
	}

	public final boolean isConfigurationSection(String path) {
		final Object val = this.getObject(path);

		return val instanceof MemorySection;
	}

	private boolean isPrimitiveWrapper(Object input) {
		return input instanceof Integer || input instanceof Boolean || input instanceof Character || input instanceof Byte || input instanceof Short || input instanceof Double || input instanceof Long || input instanceof Float;
	}

	private Object getDefault(@NonNull String path) {
		final FileConfiguration root = this.getRoot();
		final MemorySection defaults = root == null ? null : root.getDefaults();

		return (defaults == null) ? null : defaults.getObject(createPath(this, path));
	}

	private void mapChildrenKeys(Set<String> output, MemorySection section, boolean deep) {
		if (section instanceof MemorySection) {
			final MemorySection sec = section;

			for (final Map.Entry<String, SectionPathData> entry : sec.map.entrySet()) {
				output.add(createPath(section, entry.getKey(), this));

				if ((deep) && (entry.getValue().getData() instanceof MemorySection)) {
					final MemorySection subsection = (MemorySection) entry.getValue().getData();
					this.mapChildrenKeys(output, subsection, deep);
				}
			}

		} else {
			final Set<String> keys = section.getKeys(deep);

			for (final String key : keys)
				output.add(createPath(section, key, this));
		}
	}

	private void mapChildrenValues(Map<String, Object> output, MemorySection section, boolean deep) {
		if (section instanceof MemorySection) {
			final MemorySection sec = section;

			for (final Map.Entry<String, SectionPathData> entry : sec.map.entrySet()) {

				// Because of the copyDefaults call potentially copying out of order, we must remove and then add in our saved order
				// This means that default values we haven't set end up getting placed first
				final String childPath = createPath(section, entry.getKey(), this);
				output.remove(childPath);
				output.put(childPath, entry.getValue().getData());

				if (entry.getValue().getData() instanceof MemorySection)
					if (deep)
						this.mapChildrenValues(output, (MemorySection) entry.getValue().getData(), deep);
			}
		} else {
			final Map<String, Object> values = section.getValues(deep);

			for (final Map.Entry<String, Object> entry : values.entrySet())
				output.put(createPath(section, entry.getKey(), this), entry.getValue());
		}
	}

	/*
	 * Creates a full path to the given {@link MemorySection} from its
	 * root configuration.
	 * <p>
	 * You may use this method for any given {@link MemorySection}, not
	 * only {@link MemorySection}.
	 *
	 * @param section Section to create a path for.
	 * @param key Name of the specified section.
	 * @return Full path of the section from its root.
	 */
	private static String createPath(MemorySection section, String key) {
		return createPath(section, key, (section == null) ? null : section.getRoot());
	}

	/*
	 * Creates a relative path to the given {@link MemorySection} from
	 * the given relative section.
	 * <p>
	 * You may use this method for any given {@link MemorySection}, not
	 * only {@link MemorySection}.
	 *
	 * @param section Section to create a path for.
	 * @param key Name of the specified section.
	 * @param relativeTo Section to create the path relative to.
	 * @return Full path of the section from its root.
	 */
	private static String createPath(@NonNull MemorySection section, String key, MemorySection relativeTo) {
		final MemorySection root = section.getRoot();

		if (root == null)
			throw new IllegalStateException("Cannot create path without a root");

		final StringBuilder builder = new StringBuilder();

		for (MemorySection parent = section; (parent != null) && (parent != relativeTo); parent = parent.getParent()) {
			if (builder.length() > 0)
				builder.insert(0, PATH_SEPARATOR);
			builder.insert(0, parent.getName());
		}

		if ((key != null) && (key.length() > 0)) {
			if (builder.length() > 0)
				builder.append(PATH_SEPARATOR);

			builder.append(key);
		}

		return builder.toString();
	}

	public final List<String> getComments(final String path) {
		final SectionPathData pathData = this.getSectionPathData(path);

		return pathData == null ? Collections.emptyList() : pathData.getComments();
	}

	public final List<String> getInlineComments(final String path) {
		final SectionPathData pathData = this.getSectionPathData(path);

		return pathData == null ? Collections.emptyList() : pathData.getInlineComments();
	}

	public final void setComments(final String path, final List<String> comments) {
		final SectionPathData pathData = this.getSectionPathData(path);

		if (pathData != null)
			pathData.setComments(comments);
	}

	public final void setInlineComments(final String path, final List<String> comments) {
		final SectionPathData pathData = this.getSectionPathData(path);

		if (pathData != null)
			pathData.setInlineComments(comments);
	}

	private SectionPathData getSectionPathData(@NonNull String path) {
		final MemorySection root = this.getRoot();

		if (root == null)
			throw new IllegalStateException("Cannot access section without a root");

		// i1 is the leading (higher) index
		// i2 is the trailing (lower) index
		int i1 = -1, i2;
		MemorySection section = this;
		while ((i1 = path.indexOf(PATH_SEPARATOR, i2 = i1 + 1)) != -1) {
			section = section.getConfigurationSection(path.substring(i2, i1));
			if (section == null)
				return null;
		}

		final String key = path.substring(i2);
		if (section == this) {
			final SectionPathData entry = this.map.get(key);
			if (entry != null)
				return entry;
		} else if (section instanceof MemorySection)
			return section.getSectionPathData(key);

		return null;
	}

	@Override
	public String toString() {
		final MemorySection root = this.getRoot();
		return new StringBuilder()
				.append(this.getClass().getSimpleName())
				.append("[path='")
				.append(this.fullPath)
				.append("', root='")
				.append(root == null ? null : root.getClass().getSimpleName())
				.append("']")
				.toString();
	}

	private static int toInt(Object object) {
		if (object instanceof Number)
			return ((Number) object).intValue();

		try {
			return Integer.parseInt(object.toString());
		} catch (final NumberFormatException e) {
		} catch (final NullPointerException e) {
		}
		return 0;
	}

	private static double toDouble(Object object) {
		if (object instanceof Number)
			return ((Number) object).doubleValue();

		try {
			return Double.parseDouble(object.toString());
		} catch (final NumberFormatException e) {
		} catch (final NullPointerException e) {
		}
		return 0;
	}

	private static long toLong(Object object) {
		if (object instanceof Number)
			return ((Number) object).longValue();

		try {
			return Long.parseLong(object.toString());
		} catch (final NumberFormatException e) {
		} catch (final NullPointerException e) {
		}
		return 0;
	}
}
