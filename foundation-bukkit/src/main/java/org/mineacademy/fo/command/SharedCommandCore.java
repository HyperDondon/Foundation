package org.mineacademy.fo.command;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Consumer;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.CommonCore;
import org.mineacademy.fo.PlayerUtil;
import org.mineacademy.fo.exception.CommandException;
import org.mineacademy.fo.model.SimpleComponent;
import org.mineacademy.fo.model.Task;
import org.mineacademy.fo.platform.BukkitPlayer;
import org.mineacademy.fo.platform.FoundationPlayer;
import org.mineacademy.fo.remain.CompMaterial;
import org.mineacademy.fo.remain.Remain;
import org.mineacademy.fo.settings.Lang;

public interface SharedCommandCore {

	void checkBoolean(boolean flag, SimpleComponent falseMessage);

	/**
	 * Checks if the player is a console and throws an error if he is
	 *
	 * @throws CommandException
	 */
	default void checkConsole() throws CommandException {
		if (!this.isPlayer())
			throw new CommandException(Lang.component("command-requires-player"));
	}

	void checkNotNull(Object object, SimpleComponent nullMessage);

	<T> List<String> completeLastWord(final Iterable<T> suggestions);

	default List<String> completeLastWordPlayerNames() {
		return this.isPlayer() ? Common.getPlayerNames(false) : Common.getPlayerNames();
	}

	/**
	 * Convenience method for completing all world names
	 *
	 * @return
	 */
	default List<String> completeLastWordWorldNames() {
		return this.completeLastWord(Common.getWorldNames());
	}

	/**
	 * Attempts to parse the given name into a CompMaterial, will work for both modern
	 * and legacy materials: MONSTER_EGG and SHEEP_SPAWN_EGG
	 * <p>
	 * You can use the {enum} or {item} variable to replace with the given name
	 *
	 * @param name
	 * @param falseMessage
	 * @return
	 * @throws CommandException
	 */
	default CompMaterial findMaterial(final String name, final SimpleComponent falseMessage) throws CommandException {
		final CompMaterial found = CompMaterial.fromString(name);

		this.checkNotNull(found, falseMessage
				.replaceBracket("enum", name)
				.replaceBracket("item", name));

		return found;
	}

	/**
	 * Attempts to find the offline player by name or string UUID, sends an error message to sender if he did not play before
	 * or runs the specified callback on successful retrieval.
	 *
	 * The offline player lookup is done async, the callback is synchronized.
	 *
	 * @param name or string UUID
	 * @param syncCallback
	 * @throws CommandException
	 */
	default void findOfflinePlayer(final String name, final Consumer<OfflinePlayer> syncCallback) throws CommandException {
		if (name.length() == 36 && name.charAt(8) == '-' && name.charAt(13) == '-' && name.charAt(18) == '-' && name.charAt(23) == '-') {
			UUID uuid = null;

			try {
				uuid = UUID.fromString(name);

			} catch (final IllegalArgumentException ex) {
				this.returnTell(Lang.componentVars("command-invalid-uuid", "uuid", name));
			}

			this.findOfflinePlayer(uuid, syncCallback);

		} else
			this.runAsync(() -> {
				final OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(name);
				this.checkBoolean(targetPlayer != null && (targetPlayer.isOnline() || targetPlayer.hasPlayedBefore()), Lang.componentVars("player-not-played-before", "player", name));

				this.runLater(() -> syncCallback.accept(targetPlayer));
			});
	}

	/**
	 * Attempts to find the offline player by UUID, this will fire the callback
	 *
	 * @param uniqueId
	 * @param syncCallback
	 * @throws CommandException
	 */
	default void findOfflinePlayer(final UUID uniqueId, final Consumer<OfflinePlayer> syncCallback) throws CommandException {
		this.runAsync(() -> {
			final OfflinePlayer targetPlayer = Remain.getOfflinePlayerByUUID(uniqueId);
			this.checkBoolean(targetPlayer != null && (targetPlayer.isOnline() || targetPlayer.hasPlayedBefore()), Lang.componentVars("player-invalid-uuid", "uuid", uniqueId.toString()));

			this.runLater(() -> syncCallback.accept(targetPlayer));
		});
	}

	/**
	 * Attempts to find a non-vanished online player, failing with the message
	 * found at SimpleLocalization
	 *
	 * @param name
	 * @return
	 * @throws CommandException
	 */
	default Player findPlayer(final String name) throws CommandException {
		return this.findPlayer(name, Lang.component("player-not-online"));
	}

	/**
	 * Attempts to find a non-vanished online player, failing with a false message
	 *
	 * @param name
	 * @param falseMessage
	 * @return
	 * @throws CommandException
	 */
	default Player findPlayer(final String name, final SimpleComponent falseMessage) throws CommandException {
		final Player player = this.findPlayerInternal(name);
		this.checkBoolean(player != null && player.isOnline() && !PlayerUtil.isVanished(player), falseMessage.replaceBracket("player", name));

		return player;
	}

	/**
	 * A simple call to Bukkit.getPlayer(name) meant to be overriden
	 * if you have a custom implementation of getting players by name.
	 *
	 * Example use: ChatControl can find players by their nicknames too
	 *
	 * @param name
	 * @return
	 */
	default Player findPlayerInternal(final String name) {
		return Bukkit.getPlayer(name);
	}

	/**
	 * Return the player by the given args index, and, when the args are shorter, return the sender if sender is player.
	 *
	 * @param name
	 * @return
	 * @throws CommandException
	 */
	default Player findPlayerOrSelf(final int argsIndex) throws CommandException {
		if (argsIndex >= this.getArgs().length) {
			this.checkBoolean(this.isPlayer(), Lang.component("command-console-missing-player-name"));

			return this.getPlayer();
		}

		final String name = this.getArgs()[argsIndex];
		final Player player = this.findPlayerInternal(name);
		this.checkBoolean(player != null && player.isOnline(), Lang.componentVars("player-not-online", "player", name));

		return player;
	}

	/**
	 * Return the player by the given name, and, when the name is null, return the sender if sender is player.
	 *
	 * @param name
	 * @return
	 * @throws CommandException
	 */
	default Player findPlayerOrSelf(final String name) throws CommandException {
		if (name == null) {
			this.checkBoolean(this.isPlayer(), Lang.component("command-console-missing-player-name"));

			return this.getPlayer();
		}

		final Player player = this.findPlayerInternal(name);
		this.checkBoolean(player != null && player.isOnline(), Lang.componentVars("player-not-online", "player", name));

		return player;
	}

	/**
	 * Attempts to convert the given name into a bukkit world,
	 * sending localized error message if such world does not exist.
	 *
	 * @param name
	 * @return
	 */
	default World findWorld(final String name) {
		if ("~".equals(name)) {
			this.checkBoolean(this.isPlayer(), Lang.component("command-cannot-autodetect-world"));

			return this.getPlayer().getWorld();
		}

		final World world = Bukkit.getWorld(name);
		this.checkNotNull(world, Lang.componentVars("command-invalid-world",
				"world", name,
				"available", CommonCore.join(Bukkit.getWorlds(), otherWorld -> otherWorld.getName())));

		return world;
	}

	String[] getArgs();

	/**
	 * Attempts to get the sender as player, only works if the sender is actually a player,
	 * otherwise we return null
	 *
	 * @return
	 */
	default Player getPlayer() {
		return this.isPlayer() ? ((BukkitPlayer) this.getSender()).getPlayer() : null;
	}

	/**
	 * Return the command sender as Bukkit's CommandSender
	 *
	 * @return
	 */
	default CommandSender getCommandSender() {
		return ((BukkitPlayer) this.getSender()).getSender();
	}

	FoundationPlayer getSender();

	/**
	 * Return whether the sender is a living player
	 *
	 * @return
	 */
	default boolean isPlayer() {
		return this.getSender().isPlayer();
	}

	void returnTell(SimpleComponent message);

	Task runAsync(Runnable task);

	Task runLater(Runnable task);
}
