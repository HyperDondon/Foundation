package org.mineacademy.fo.platform;

import java.io.File;
import java.util.List;
import java.util.UUID;

import org.mineacademy.fo.command.SimpleCommandCore;
import org.mineacademy.fo.model.Task;
import org.mineacademy.fo.model.Tuple;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.event.HoverEventSource;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Platform {

	private static FoundationPlatform instance;

	public static boolean callEvent(Object event) {
		return getInstance().callEvent(event);
	}

	public static void checkCommandUse(SimpleCommandCore command) {
		getInstance().checkCommandUse(command);
	}

	public static HoverEventSource<?> convertItemStackToHoverEvent(Object itemStack) {
		return getInstance().convertItemStackToHoverEvent(itemStack);
	}

	public static void dispatchConsoleCommand(String command) {
		getInstance().dispatchConsoleCommand(command);
	}

	private static FoundationPlatform getInstance() {
		// Do not throw FoException to prevent race condition
		if (instance == null)
			throw new NullPointerException("Foundation instance not set yet.");

		return instance;
	}

	public static List<FoundationPlayer> getOnlinePlayers() {
		return getInstance().getOnlinePlayers();
	}

	public static File getPluginFile(String pluginName) {
		return getInstance().getPluginFile(pluginName);
	}

	public static String getCustomServerName() {
		return getInstance().getCustomServerName();
	}

	public static boolean hasCustomServerName() {
		return getInstance().hasCustomServerName();
	}

	public static void setCustomServerName(String serverName) {
		getInstance().setCustomServerName(serverName);
	}

	public static List<Tuple<String, String>> getServerPlugins() {
		return getInstance().getServerPlugins();
	}

	public static String getPlatformVersion() {
		return getInstance().getPlatformVersion();
	}

	public static String getPlatformName() {
		return getInstance().getPlatformName();
	}

	public static boolean hasHexColorSupport() {
		return getInstance().hasHexColorSupport();
	}

	public static boolean isAsync() {
		return getInstance().isAsync();
	}

	public static boolean isPlaceholderAPIHooked() {
		return getInstance().isPlaceholderAPIHooked();
	}

	public static boolean isPluginInstalled(String name) {
		return getInstance().isPluginInstalled(name);
	}

	public static void logToConsole(String message) {
		getInstance().logToConsole(message);
	}

	public static void registerCommand(SimpleCommandCore command, boolean unregisterOldCommand, boolean unregisterOldAliases) {
		getInstance().registerCommand(command, unregisterOldCommand, unregisterOldAliases);
	}

	public static void registerEvents(Object listener) {
		getInstance().registerEvents(listener);
	}

	public static Task runTask(int delayTicks, Runnable runnable) {
		return getInstance().runTask(delayTicks, runnable);
	}

	public static Task runTaskAsync(int delayTicks, Runnable runnable) {
		return getInstance().runTaskAsync(delayTicks, runnable);
	}

	public static void sendPluginMessage(UUID senderUid, String channel, byte[] message) {
		getInstance().sendPluginMessage(senderUid, channel, message);
	}

	public static void setInstance(FoundationPlatform instance) {
		Platform.instance = instance;
	}

	public static void unregisterCommand(SimpleCommandCore command) {
		getInstance().unregisterCommand(command);
	}

	public static FoundationPlugin getPlugin() {
		return getInstance().getPlugin();
	}

	public static String getNMSVersion() {
		return getInstance().getNMSVersion();
	}

	public static FoundationPlayer toPlayer(Object player) {
		return getInstance().toPlayer(player);
	}
}
