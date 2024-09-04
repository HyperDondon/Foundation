package org.mineacademy.fo;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.mineacademy.fo.MinecraftVersion.V;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReflectionUtil extends ReflectionUtilCore {

	/**
	 * The full package name for NMS
	 */
	public static final String NMS = "net.minecraft.server";

	/**
	 * The package name for Craftbukkit
	 */
	public static final String CRAFTBUKKIT = "org.bukkit.craftbukkit";

	/**
	 * Find a class automatically for older MC version (such as type EntityPlayer for oldName
	 * and we automatically find the proper NMS import) or if MC 1.17+ is used then type
	 * the full class path such as net.minecraft.server.level.EntityPlayer and we use that instead.
	 *
	 * @param oldName
	 * @param fullName1_17
	 * @return
	 */
	public static Class<?> getNMSClass(String oldName, String fullName1_17) {
		return MinecraftVersion.atLeast(V.v1_17) ? lookupClass(fullName1_17) : getNMSClass(oldName);
	}

	/**
	 * Find a class in net.minecraft.server package, adding the version
	 * automatically
	 *
	 * @deprecated Minecraft 1.17+ has a different path name,
	 *             use {@link #getNMSClass(String, String)} instead
	 *
	 * @param name
	 * @return
	 */
	@Deprecated
	public static Class<?> getNMSClass(final String name) {
		String version = MinecraftVersion.getServerVersion();

		if (!version.isEmpty())
			version += ".";

		return ReflectionUtil.lookupClass(NMS + "." + version + name);
	}

	/**
	 * Find a class in org.bukkit.craftbukkit package, adding the version
	 * automatically
	 *
	 * @param name
	 * @return
	 */
	public static Class<?> getOBCClass(final String name) {
		String version = MinecraftVersion.getServerVersion();

		if (!version.isEmpty())
			version += ".";

		return ReflectionUtil.lookupClass(CRAFTBUKKIT + "." + version + name);
	}

	/**
	 * Return a constructor for the given NMS class name (such as EntityZombie)
	 *
	 * @param nmsClassPath
	 * @param params
	 * @return
	 */
	public static Constructor<?> getConstructorNMS(@NonNull final String nmsClassPath, final Class<?>... params) {
		return getConstructor(getNMSClass(nmsClassPath), params);
	}

	/**
	 * Makes a new instanceo of the given NMS class with arguments,
	 * NB: Does not work on Minecraft 1.17+
	 *
	 * @param nmsPath
	 * @param params
	 * @return
	 */
	public static <T> T instantiateNMS(final String nmsPath, final Object... params) {
		return (T) instantiate(getNMSClass(nmsPath), params);
	}

	/**
	 * Return a tree set of classes from the plugin that extend the given class
	 *
	 * @param plugin
	 * @return
	 */
	public static List<Class<?>> getClasses(final Plugin plugin) {
		final List<Class<?>> found = new ArrayList<>();

		found.addAll(getClasses(plugin, null));

		return found;
	}

	/**
	 * Get all classes in the java plugin
	 *
	 * @param <T>
	 * @param plugin
	 * @param extendingClass
	 * @return
	 */
	@SneakyThrows
	public static <T> TreeSet<Class<T>> getClasses(@NonNull Plugin plugin, Class<T> extendingClass) {
		Valid.checkNotNull(plugin, "Plugin is null!");
		Valid.checkBoolean(JavaPlugin.class.isAssignableFrom(plugin.getClass()), "Plugin must be a JavaPlugin");

		// Get the plugin .jar
		final Method getFileMethod = JavaPlugin.class.getDeclaredMethod("getFile");
		getFileMethod.setAccessible(true);

		final File pluginFile = (File) getFileMethod.invoke(plugin);

		return ReflectionUtilCore.getClasses(pluginFile, extendingClass);
	}
}
