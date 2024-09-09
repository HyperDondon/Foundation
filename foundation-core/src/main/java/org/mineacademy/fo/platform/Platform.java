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
		return getPlatform().callEvent(event);
	}

	public static void checkCommandUse(SimpleCommandCore command) {
		getPlatform().checkCommandUse(command);
	}

	public static HoverEventSource<?> convertItemStackToHoverEvent(Object itemStack) {
		return getPlatform().convertItemStackToHoverEvent(itemStack);
	}

	public static void dispatchConsoleCommand(FoundationPlayer playerReplacement, String command) {
		getPlatform().dispatchConsoleCommand(playerReplacement, command);
	}

	public static FoundationPlatform getPlatform() {
		// Do not throw FoException to prevent race condition
		if (instance == null)
			throw new NullPointerException("Foundation instance not set yet.");

		return instance;
	}

	public static List<FoundationPlayer> getOnlinePlayers() {
		return getPlatform().getOnlinePlayers();
	}

	public static File getPluginFile(String pluginName) {
		return getPlatform().getPluginFile(pluginName);
	}

	public static String getCustomServerName() {
		return getPlatform().getCustomServerName();
	}

	public static boolean hasCustomServerName() {
		return getPlatform().hasCustomServerName();
	}

	public static void setCustomServerName(String serverName) {
		getPlatform().setCustomServerName(serverName);
	}

	public static List<Tuple<String, String>> getServerPlugins() {
		return getPlatform().getServerPlugins();
	}

	public static String getPlatformVersion() {
		return getPlatform().getPlatformVersion();
	}

	public static String getPlatformName() {
		return getPlatform().getPlatformName();
	}

	public static boolean hasHexColorSupport() {
		return getPlatform().hasHexColorSupport();
	}

	public static boolean isAsync() {
		return getPlatform().isAsync();
	}

	public static boolean isPlaceholderAPIHooked() {
		return getPlatform().isPlaceholderAPIHooked();
	}

	public static boolean isPluginInstalled(String name) {
		return getPlatform().isPluginInstalled(name);
	}

	public static void logToConsole(String message) {
		getPlatform().logToConsole(message);
	}

	public static void registerCommand(SimpleCommandCore command, boolean unregisterOldCommand, boolean unregisterOldAliases) {
		getPlatform().registerCommand(command, unregisterOldCommand, unregisterOldAliases);
	}

	public static void registerEvents(Object listener) {
		getPlatform().registerEvents(listener);
	}

	public static Task runTask(int delayTicks, Runnable runnable) {
		return getPlatform().runTask(delayTicks, runnable);
	}

	public static Task runTaskAsync(int delayTicks, Runnable runnable) {
		return getPlatform().runTaskAsync(delayTicks, runnable);
	}

	public static void sendPluginMessage(UUID senderUid, String channel, byte[] message) {
		getPlatform().sendPluginMessage(senderUid, channel, message);
	}

	public static void setInstance(FoundationPlatform instance) {
		Platform.instance = instance;
	}

	public static void unregisterCommand(SimpleCommandCore command) {
		getPlatform().unregisterCommand(command);
	}

	public static FoundationPlugin getPlugin() {
		return getPlatform().getPlugin();
	}

	public static String getNMSVersion() {
		return getPlatform().getNMSVersion();
	}

	public static FoundationPlayer toPlayer(Object player) {
		return getPlatform().toPlayer(player);
	}
}
