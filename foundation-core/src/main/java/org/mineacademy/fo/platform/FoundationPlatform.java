package org.mineacademy.fo.platform;

import java.io.File;
import java.util.List;
import java.util.UUID;

import org.mineacademy.fo.command.SimpleCommandCore;
import org.mineacademy.fo.model.Task;
import org.mineacademy.fo.model.Tuple;

import net.kyori.adventure.text.event.HoverEventSource;

public abstract class FoundationPlatform {

	/**
	 * The server-name from server.properties (is lacking on new Minecraft version so we have to readd it back)
	 */
	private String customServerName;

	// ----------------------------------------------------------------------------------------------------
	// Server name
	// ----------------------------------------------------------------------------------------------------

	/**
	 * Return the server name identifier
	 *
	 * @return
	 */
	public final String getCustomServerName() {
		if (!this.hasCustomServerName())
			throw new IllegalArgumentException("Please instruct developer of " + Platform.getPlugin().getName() + " to call Platform#setCustomServerName");

		return this.customServerName;
	}

	/**
	 * Return true if the server-name property in server.properties got modified
	 *
	 * @return
	 */
	public final boolean hasCustomServerName() {
		return this.customServerName != null && !this.customServerName.isEmpty() && !this.customServerName.contains("mineacademy.org/server-properties") && !"undefined".equals(this.customServerName) && !"Unknown Server".equals(this.customServerName);
	}

	/**
	 * Set the server name identifier
	 *
	 * @param serverName
	 */
	public final void setCustomServerName(String serverName) {
		this.customServerName = serverName;
	}

	public abstract FoundationPlayer toPlayer(Object sender);

	public abstract boolean callEvent(Object event);

	public abstract void checkCommandUse(SimpleCommandCore command);

	public abstract HoverEventSource<?> convertItemStackToHoverEvent(Object itemStack);

	public abstract void dispatchConsoleCommand(String command);

	public abstract List<FoundationPlayer> getOnlinePlayers();

	public abstract FoundationPlugin getPlugin();

	public abstract File getPluginFile(String pluginName);

	public abstract List<Tuple<String, String>> getServerPlugins();

	public abstract String getPlatformVersion();

	public abstract String getPlatformName();

	public abstract String getNMSVersion();

	public abstract boolean hasHexColorSupport();

	public abstract boolean isAsync();

	public abstract boolean isPlaceholderAPIHooked();

	public abstract boolean isPluginInstalled(String name);

	public abstract void logToConsole(String message);

	public abstract void registerCommand(SimpleCommandCore command, boolean unregisterOldCommand, boolean unregisterOldAliases);

	public abstract void registerEvents(Object listener);

	public abstract Task runTask(int delayTicks, Runnable runnable);

	public abstract Task runTaskAsync(int delayTicks, Runnable runnable);

	public abstract void sendPluginMessage(UUID senderUid, String channel, byte[] array);

	public abstract void unregisterCommand(SimpleCommandCore command);
}
