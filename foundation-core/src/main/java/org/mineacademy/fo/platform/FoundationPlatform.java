package org.mineacademy.fo.platform;

import java.io.File;
import java.util.List;
import java.util.UUID;

import org.mineacademy.fo.MessengerCore;
import org.mineacademy.fo.ValidCore;
import org.mineacademy.fo.command.SimpleCommandCore;
import org.mineacademy.fo.model.Task;
import org.mineacademy.fo.model.Tuple;
import org.mineacademy.fo.remain.CompChatColor;

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

	/**
	 * Runs the given command (without /) as the console, replacing {player} with sender
	 *
	 * You can prefix the command with @(announce|warn|error|info|question|success) to send a formatted
	 * message to playerReplacement directly.
	 *
	 * @param playerReplacement
	 * @param command
	 */
	public final void dispatchConsoleCommand(FoundationPlayer playerReplacement, String command) {
		if (command.isEmpty() || command.equalsIgnoreCase("none"))
			return;

		if (command.startsWith("@announce ")) {
			ValidCore.checkNotNull(playerReplacement, "Cannot use @announce without a player in: " + command);

			MessengerCore.announce(playerReplacement, command.replace("@announce ", ""));
		}

		else if (command.startsWith("@warn ")) {
			ValidCore.checkNotNull(playerReplacement, "Cannot use @warn without a player in: " + command);

			MessengerCore.warn(playerReplacement, command.replace("@warn ", ""));
		}

		else if (command.startsWith("@error ")) {
			ValidCore.checkNotNull(playerReplacement, "Cannot use @error without a player in: " + command);

			MessengerCore.error(playerReplacement, command.replace("@error ", ""));
		}

		else if (command.startsWith("@info ")) {
			ValidCore.checkNotNull(playerReplacement, "Cannot use @info without a player in: " + command);

			MessengerCore.info(playerReplacement, command.replace("@info ", ""));
		}

		else if (command.startsWith("@question ")) {
			ValidCore.checkNotNull(playerReplacement, "Cannot use @question without a player in: " + command);

			MessengerCore.question(playerReplacement, command.replace("@question ", ""));
		}

		else if (command.startsWith("@success ")) {
			ValidCore.checkNotNull(playerReplacement, "Cannot use @success without a player in: " + command);

			MessengerCore.success(playerReplacement, command.replace("@success ", ""));
		}

		else {
			command = command.startsWith("/") && !command.startsWith("//") ? command.substring(1) : command;
			command = command.replace("{player}", playerReplacement == null ? "" : playerReplacement.getName());

			// Workaround for JSON in tellraw getting HEX colors replaced
			if (!command.startsWith("tellraw"))
				command = CompChatColor.translateColorCodes(command);

			this.dispatchConsoleCommand0(command);
		}
	}

	protected abstract void dispatchConsoleCommand0(String command);

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
