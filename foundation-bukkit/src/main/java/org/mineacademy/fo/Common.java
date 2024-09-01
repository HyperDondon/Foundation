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
import org.bukkit.util.Vector;
import org.mineacademy.fo.model.HookManager;
import org.mineacademy.fo.model.SimpleComponent;
import org.mineacademy.fo.model.Task;
import org.mineacademy.fo.platform.FoundationPlayer;
import org.mineacademy.fo.platform.Platform;
import org.mineacademy.fo.remain.Remain;
import org.mineacademy.fo.settings.SimpleSettings;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Common extends CommonCore {

	/**
	 * Sends a message to the player
	 *
	 * @param player
	 * @param messages
	 */
	public static void tell(@NonNull Player player, String... messages) {
		final FoundationPlayer sender = Platform.toPlayer(player);

		for (final String message : messages)
			SimpleComponent.fromMini(message).send(sender);
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
	 * Sends a message to the audience. Supports {plugin_prefix} and {player} variable.
	 * Supports \<actionbar\>, \<toast\>, \<title\>, \<bossbar\> and \<center\>.
	 * Properly sends the message to the player if he is conversing with the server.
	 *
	 * @param sender
	 * @param messages
	 */
	/*public static void tell(@NonNull final CommandSender sender, String... messages) {
		final FoundationPlayer foundationSender = Platform.toPlayer(sender);
	
		for (final String message : messages)
			foundationSender.sendMessage(message);
	}*/

	/**
	 * Sends a message to the audience. Supports {plugin_prefix} and {player} variable.
	 * Supports \<actionbar\>, \<toast\>, \<title\>, \<bossbar\> and \<center\>.
	 * Properly sends the message to the player if he is conversing with the server.
	 *
	 * @param sender
	 * @param messages
	 */
	/*public static void tellNoPrefix(@NonNull final CommandSender sender, String... messages) {
		final FoundationPlayer audience = Platform.toPlayer(sender);
	
		for (final String message : messages)
			CommonCore.tellNoPrefix(audience, message);
	}*/

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
			return shortLocation((Location) arg);

		else if (arg instanceof ChatColor)
			return ((ChatColor) arg).name().toLowerCase();

		else if (arg instanceof net.md_5.bungee.api.ChatColor)
			return ((net.md_5.bungee.api.ChatColor) arg).name().toLowerCase();

		return CommonCore.simplify(arg);
	}

	/**
	 * Formats the vector location to one digit decimal points
	 *
	 * DO NOT USE FOR SAVING, ONLY INTENDED FOR DEBUGGING
	 * Use SerializeUtil to save a vector
	 *
	 * @param vec
	 * @return
	 */
	// TODO can we get rid of this?
	public static String shortLocation(final Vector vec) {
		return " [" + MathUtilCore.formatOneDigit(vec.getX()) + ", " + MathUtilCore.formatOneDigit(vec.getY()) + ", " + MathUtilCore.formatOneDigit(vec.getZ()) + "]";
	}

	/**
	 * Formats the given location to block points without decimals
	 *
	 * DO NOT USE FOR SAVING, ONLY INTENDED FOR DEBUGGING
	 * Use SerializeUtil to save a location
	 *
	 * @param location
	 * @return
	 */
	// TODO can we get rid of this?
	public static String shortLocation(final Location location) {
		if (location == null)
			return "Location(null)";

		if (location.equals(new Location(null, 0, 0, 0)))
			return "Location(null, 0, 0, 0)";

		Valid.checkNotNull(location.getWorld(), "Cannot shorten a location with null world!");

		return SimpleSettings.LOCATION_FORMAT
				.replace("{world}", location.getWorld().getName())
				.replace("{x}", String.valueOf(location.getBlockX()))
				.replace("{y}", String.valueOf(location.getBlockY()))
				.replace("{z}", String.valueOf(location.getBlockZ()));
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

	/**
	 * Resolves the inner Map in a Bukkit's {@link MemorySection}
	 *
	 * @param mapOrSection
	 * @return
	 */
	/*public static Map<String, Object> getMapFromSection(@NonNull Object mapOrSection) {
		mapOrSection = Remain.getRootOfSectionPathData(mapOrSection);
	
		final Map<String, Object> map = mapOrSection instanceof ConfigSection ? ((ConfigSection) mapOrSection).getValues(false)
				: mapOrSection instanceof Map ? (Map<String, Object>) mapOrSection
						: mapOrSection instanceof MemorySection ? ReflectionUtil.getFieldContent(mapOrSection, "map") : null;
	
		Valid.checkNotNull(map, "Unexpected " + mapOrSection.getClass().getSimpleName() + " '" + mapOrSection + "'. Must be Map or MemorySection! (Do not just send config name here, but the actual section with get('section'))");
	
		final Map<String, Object> copy = new LinkedHashMap<>();
	
		for (final Map.Entry<String, Object> entry : map.entrySet()) {
			final String key = entry.getKey();
			final Object value = entry.getValue();
	
			copy.put(key, Remain.getRootOfSectionPathData(value));
		}
	
		return copy;
	}*/

	/**
	 * Runs the given command (without /) as the console, replacing {player} with sender
	 *
	 * You can prefix the command with @(announce|warn|error|info|question|success) to send a formatted
	 * message to playerReplacement directly.
	 *
	 * @param playerReplacement
	 * @param command
	 */
	/*public static void dispatchCommand(CommandSender playerReplacement, @NonNull String command) {
		CommonCore.dispatchCommand(Platform.toFoundationPlayer(playerReplacement), command);
	}*/

	/**
	 * Runs the given command (without /) as if the sender would type it, replacing {player} with his name
	 *
	 * @param sender
	 * @param command
	 */
	/*public static void dispatchCommandAsPlayer(@NonNull final CommandSender sender, @NonNull String command) {
		CommonCore.dispatchCommandAsPlayer(Platform.toFoundationPlayer(sender), command);
	}*/

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

	/**
	 * Attempts to cancel all tasks of this plugin
	 */
	/*public static void cancelTasks() {
		Remain.cancelTasks();
	}*/
}
