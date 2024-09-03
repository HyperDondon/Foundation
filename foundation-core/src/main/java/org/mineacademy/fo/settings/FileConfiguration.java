package org.mineacademy.fo.settings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.mineacademy.fo.CommonCore;
import org.mineacademy.fo.FileUtil;
import org.mineacademy.fo.ValidCore;

import lombok.NonNull;

/**
 * This is a base class for all File based configurations.
 */
public abstract class FileConfiguration extends MemorySection {

	/**
	 * A null, used for convenience in {@link #loadConfiguration(String, String)} where the "to" is null.
	 */
	public static final String NO_DEFAULT = null;

	/**
	 * The default configuration.
	 */
	private FileConfiguration defaults;

	/**
	 * The file this configuration is stored in.
	 */
	private File file;

	/**
	 * Creates an empty {@link FileConfiguration} with no default values.
	 */
	public FileConfiguration() {
		super();
	}

	/**
	 * Saves this {@link FileConfiguration} to the specified location.
	 * <p>
	 * If the file does not exist, it will be created. If already exists, it
	 * will be overwritten. If it cannot be overwritten or created, an
	 * exception will be thrown.
	 * <p>
	 * This method will save using the system default encoding, or possibly
	 * using UTF8.
	 *
	 * @throws IllegalArgumentException Thrown when file is null.
	 */
	public final void save() {
		ValidCore.checkNotNull(this.file, "Cannot save to a null file, call load() or setFile() first in " + this);

		try {
			final File parent = this.file.getCanonicalFile().getParentFile();

			if (parent != null)
				parent.mkdirs();

			this.onSave();

			final String data = this.saveToString();
			final Writer writer = new OutputStreamWriter(new FileOutputStream(this.file), StandardCharsets.UTF_8);

			try {
				writer.write(data);

			} finally {
				writer.close();
			}

		} catch (final IOException ex) {
			CommonCore.error(ex, "Failed to save " + this.file);
		}
	}

	/**
	 * Called before the configuration is saved
	 */
	protected void onSave() {
	}

	/**
	 * Saves this {@link FileConfiguration} to a string, and returns it.
	 *
	 * @return String containing this configuration.
	 */
	public abstract String saveToString();

	public final void loadConfiguration(String from, String to) {
		if (from != null) {
			final List<String> defaultContent = FileUtil.getInternalFileContent(from);
			ValidCore.checkNotNull(defaultContent, "Inbuilt " + from + " not found! Did you reload?");

			// Load main
			this.load(FileUtil.extract(defaultContent, to));

			// Load defaults
			this.defaults = new YamlConfig();
			this.defaults.loadFromString(String.join("\n", defaultContent));

		} else
			this.load(FileUtil.createIfNotExists(to));
	}

	/**
	 * Loads this {@link FileConfiguration} from the specified location.
	 * <p>
	 * All the values contained within this configuration will be removed,
	 * leaving only settings and defaults, and the new values will be loaded
	 * from the given file.
	 * <p>
	 * If the file cannot be loaded for any reason, an exception will be
	 * thrown.
	 *
	 * @param file File to load from.
	 */
	public final void load(@NonNull File file) {
		this.file = file;

		try {
			final FileInputStream stream = new FileInputStream(file);

			this.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
			this.onLoad();

		} catch (final Exception ex) {
			CommonCore.error(ex, "Cannot load " + file);
		}
	}

	/**
	 * Called automatically after the configuration is loaded
	 */
	protected void onLoad() {
	}

	/*
	 * Loads this {@link FileConfiguration} from the specified reader.
	 * <p>
	 * All the values contained within this configuration will be removed,
	 * leaving only settings and defaults, and the new values will be loaded
	 * from the given stream.
	 *
	 * @param reader the reader to load from
	 * @throws IOException thrown when underlying reader throws an IOException
	 */
	private void load(Reader reader) throws IOException {
		final BufferedReader input = reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
		final StringBuilder builder = new StringBuilder();

		try {
			String line;

			while ((line = input.readLine()) != null) {
				builder.append(line);
				builder.append('\n');
			}

		} finally {
			input.close();
		}

		this.loadFromString(builder.toString());
	}

	/**
	 * Loads this {@link FileConfiguration} from the specified string, as
	 * opposed to from file.
	 * <p>
	 * All the values contained within this configuration will be removed,
	 * leaving only settings and defaults, and the new values will be loaded
	 * from the given string.
	 * <p>
	 * If the string is invalid in any way, an exception will be thrown.
	 *
	 * @param contents Contents of a Configuration to load.
	 */
	public abstract void loadFromString(String contents);

	/**
	 * Sets the source of all default values for this configuration.
	 * <p>
	 * If a previous source was set, or previous default values were defined,
	 * then they will not be copied to the new source.
	 *
	 * @param defaults New source of default values for this configuration.
	 * @throws IllegalArgumentException Thrown if defaults is null or this.
	 */
	public final void setDefaults(@NonNull FileConfiguration defaults) {
		this.defaults = defaults;
	}

	/**
	 * Gets the source configuration for this configuration.
	 * <p>
	 * If no configuration source was set, but default values were added, then
	 * a {@link MemorySection} will be returned. If no source was set
	 * and no defaults were set, then this method will return null.
	 *
	 * @return Configuration source for default values, or null if none exist.
	 */
	public final FileConfiguration getDefaults() {
		return this.defaults;
	}

	/**
	 * Checks if this configuration has a source for default values.
	 *
	 * @return
	 */
	public final boolean hasDefaults() {
		return this.defaults != null;
	}

	/**
	 * Get the file this configuration is stored in.
	 *
	 * @return
	 */
	public final File getFile() {
		return file;
	}

	/**
	 * Return the file name without the extension
	 *
	 * @return
	 */
	public final String getName() {
		final String fileName = this.getFile().getName();

		if (fileName != null) {
			final int lastDot = fileName.lastIndexOf(".");

			if (lastDot != -1)
				return fileName.substring(0, lastDot);
		}

		return null;
	}

	/**
	 * Updates the file this configuration is stored in.
	 *
	 * @param file
	 */
	public final void setFile(File file) {
		this.file = file;
	}

	/**
	 * Removes the loaded file configuration from the disk.
	 */
	/*public final void deleteFile() {
		ValidCore.checkNotNull(this.file, "Cannot delete a null file");

		if (this.file.exists())
			this.file.delete();
	}*/

	@Override
	final MemorySection getParent() {
		return null;
	}

	@Override
	public int hashCode() {
		return this.file == null ? super.hashCode() : this.file.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof FileConfiguration) {
			final FileConfiguration other = (FileConfiguration) obj;

			if (other.file == null && this.file == null)
				return super.equals(obj);

			if (other.file == null && this.file != null)
				return false;

			if (other.file != null && this.file == null)
				return false;

			return other.file != null && other.file.equals(this.file);
		}

		return false;
	}

	@Override
	public String toString() {
		return "FileConfiguration{file=" + file + "}";
	}
}
