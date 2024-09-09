package org.mineacademy.fo;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.mineacademy.fo.model.HookManager;
import org.mineacademy.fo.model.SimpleComponent;
import org.mineacademy.fo.model.Task;
import org.mineacademy.fo.platform.FoundationPlayer;
import org.mineacademy.fo.platform.Platform;
import org.mineacademy.fo.remain.Remain;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Common extends CommonCore {

	/**
	 * Sends a message to the sender with a given delay, colors & are supported
	 *
	 * @param sender
	 * @param delayTicks
	 * @param message
	 */
	public static void tellLater(final int delayTicks, final CommandSender sender, final String message) {
		Common.tellLater(delayTicks, Platform.toPlayer(sender), message);
	}

	/**
	 * Sends a message to the sender with a given delay, colors & are supported
	 *
	 * @param sender
	 * @param delayTicks
	 * @param message
	 */
	public static void tellLater(final int delayTicks, final CommandSender sender, final SimpleComponent message) {
		Common.tellLater(delayTicks, Platform.toPlayer(sender), message);
	}

	/**
	* Sends a message to the player and saves the time when it was sent.
	* The delay in seconds is the delay between which we won't send player the
	* same message, in case you call this method again.
	*
	* @param delaySeconds
	* @param sender
	* @param message
	*/
	public static void tellTimed(final int delaySeconds, final CommandSender sender, final String message) {
		CommonCore.tellTimed(delaySeconds, Platform.toPlayer(sender), message);
	}

	/**
	* Sends a message to the player and saves the time when it was sent.
	* The delay in seconds is the delay between which we won't send player the
	* same message, in case you call this method again.
	*
	* @param delaySeconds
	* @param sender
	* @param message
	*/
	public static void tellTimed(final int delaySeconds, final CommandSender sender, final SimpleComponent message) {
		CommonCore.tellTimed(delaySeconds, Platform.toPlayer(sender), message);
	}

	/**
	 * Sends a message to the player
	 *
	 * @param sender
	 * @param messages
	 */
	public static void tell(@NonNull CommandSender sender, String... messages) {
		final FoundationPlayer audience = Platform.toPlayer(sender);

		CommonCore.tell(audience, messages);
	}

	/**
	 * Sends a message to the audience. Supports {plugin_prefix} and {player} variable.
	 * Supports \<actionbar\>, \<toast\>, \<title\>, \<bossbar\> and \<center\>.
	 * Properly sends the message to the player if he is conversing with the server.
	 *
	 * @param sender
	 * @param message
	 */
	public static void tell(@NonNull final CommandSender sender, SimpleComponent message) {
		Platform.toPlayer(sender).sendMessage(message);
	}

	/**
	 * Replace some common classes such as entity to name automatically
	 *
	 * @param arg
	 * @return
	 */
	public static String simplify(Object arg) {
		if (arg == null)
			return "";

		else if (arg instanceof Entity)
			return Remain.getEntityName((Entity) arg);

		else if (arg instanceof CommandSender)
			return ((CommandSender) arg).getName();

		else if (arg instanceof World)
			return ((World) arg).getName();

		else if (arg instanceof Location)
			return SerializeUtil.serializeLoc((Location) arg);

		else if (arg instanceof ChatColor)
			return ((ChatColor) arg).name().toLowerCase();

		else if (arg instanceof net.md_5.bungee.api.ChatColor)
			return ((net.md_5.bungee.api.ChatColor) arg).name().toLowerCase();

		return CommonCore.simplify(arg);
	}

	/**
	 * Convenience method for getting a list of world names
	 *
	 * @return
	 */
	public static List<String> getWorldNames() {
		final List<String> worlds = new ArrayList<>();

		for (final World world : Bukkit.getWorlds())
			worlds.add(world.getName());

		return worlds;
	}

	/**
	 * Convenience method for getting a list of player names
	 *
	 * @return
	 */
	public static List<String> getPlayerNames() {
		return getPlayerNames(true, null);
	}

	/**
	 * Convenience method for getting a list of player names
	 * that optionally, are vanished
	 *
	 * @param includeVanished
	 * @return
	 */
	public static List<String> getPlayerNames(final boolean includeVanished) {
		return getPlayerNames(includeVanished, null);
	}

	/**
	 * Convenience method for getting a list of player names
	 * that optionally, the other player can see
	 *
	 * @param includeVanished
	 * @param otherPlayer
	 *
	 * @return
	 */
	public static List<String> getPlayerNames(final boolean includeVanished, Player otherPlayer) {
		final List<String> found = new ArrayList<>();

		for (final Player online : Remain.getOnlinePlayers()) {
			if (PlayerUtil.isVanished(online, otherPlayer) && !includeVanished)
				continue;

			found.add(online.getName());
		}

		return found;
	}

	/**
	 * Return nicknames of online players
	 *
	 * @param includeVanished
	 * @return
	 */
	public static List<String> getPlayerNicknames(final boolean includeVanished) {
		return getPlayerNicknames(includeVanished, null);
	}

	/**
	 * Return nicknames of online players
	 *
	 * @param includeVanished
	 * @param otherPlayer
	 * @return
	 */
	public static List<String> getPlayerNicknames(final boolean includeVanished, Player otherPlayer) {
		final List<String> found = new ArrayList<>();

		for (final Player online : Remain.getOnlinePlayers()) {
			if (PlayerUtil.isVanished(online, otherPlayer) && !includeVanished)
				continue;

			found.add(HookManager.getNickColorless(online));
		}

		return found;
	}

	/**
	 * Find the plugin command from the command.
	 *
	 * The command can either just be the label such as "/give" or "give"
	 * or the full command such as "/give kangarko diamonds", in which case
	 * we will find the label and just match against "/give"
	 *
	 * @param command
	 * @return
	 */
	public static Command findCommand(final String command) {
		final String[] args = command.split(" ");

		if (args.length > 0) {
			String label = args[0].toLowerCase();

			if (label.startsWith("/"))
				label = label.substring(1);

			for (final Plugin otherPlugin : Bukkit.getPluginManager().getPlugins()) {
				final JavaPlugin plugin = (JavaPlugin) otherPlugin;

				if (plugin instanceof JavaPlugin) {
					final Command pluginCommand = plugin.getCommand(label);

					if (pluginCommand != null)
						return pluginCommand;
				}
			}

			final Command serverCommand = Remain.getCommandMap().getCommand(label);

			if (serverCommand != null)
				return serverCommand;
		}

		return null;
	}

	// ------------------------------------------------------------------------------------------------------------
	// Scheduling
	// ------------------------------------------------------------------------------------------------------------

	/**
	 * Runs the task if the plugin is enabled correctly
	 *
	 * @param task the task
	 * @return the task or null
	 */
	public static Task runLater(final Runnable task) {
		return runLater(1, task);
	}

	/**
	 * Runs the task even if the plugin is disabled for some reason.
	 *
	 * @param delayTicks
	 * @param runnable
	 * @return the task or null
	 */
	public static Task runLater(final int delayTicks, Runnable runnable) {
		return Remain.runLater(delayTicks, runnable);
	}

	/**
	 * Runs the task async even if the plugin is disabled for some reason.
	 * <p>
	 * Schedules the run on the next tick.
	 *
	 * @param task
	 * @return
	 */
	public static Task runAsync(final Runnable task) {
		return runLaterAsync(0, task);
	}

	/**
	 * Runs the task async even if the plugin is disabled for some reason.
	 *
	 * @param delayTicks
	 * @param runnable
	 * @return the task or null
	 */
	public static Task runLaterAsync(final int delayTicks, Runnable runnable) {
		return Remain.runLaterAsync(delayTicks, runnable);
	}

	/**
	 * Runs the task timer even if the plugin is disabled.
	 *
	 * @param repeatTicks the delay between each execution
	 * @param task        the task
	 * @return the bukkit task or null
	 */
	public static Task runTimer(final int repeatTicks, final Runnable task) {
		return runTimer(0, repeatTicks, task);
	}

	/**
	 * Runs the task timer even if the plugin is disabled.
	 *
	 * @param delayTicks  the delay before first run
	 * @param repeatTicks the delay between each run
	 * @param runnable        the task
	 * @return the bukkit task or null if error
	 */
	public static Task runTimer(final int delayTicks, final int repeatTicks, Runnable runnable) {
		return Remain.runTimer(delayTicks, repeatTicks, runnable);
	}

	/**
	 * Runs the task timer async even if the plugin is disabled.
	 *
	 * @param repeatTicks
	 * @param task
	 * @return
	 */
	public static Task runTimerAsync(final int repeatTicks, final Runnable task) {
		return runTimerAsync(0, repeatTicks, task);
	}

	/**
	 * Runs the task timer async even if the plugin is disabled.
	 *
	 * @param delayTicks
	 * @param repeatTicks
	 * @param runnable
	 * @return
	 */
	public static Task runTimerAsync(final int delayTicks, final int repeatTicks, Runnable runnable) {
		return Remain.runTimerAsync(delayTicks, repeatTicks, runnable);
	}
}
