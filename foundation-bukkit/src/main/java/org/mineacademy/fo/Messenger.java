package org.mineacademy.fo;

import org.bukkit.command.CommandSender;
import org.mineacademy.fo.model.SimpleComponent;
import org.mineacademy.fo.platform.Platform;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Messenger extends MessengerCore {

	/**
	 * Send a message prepended with the appropriate prefixes.
	 *
	 * @param sender
	 * @param message
	 */
	public static void info(final CommandSender sender, final String message) {
		info(Platform.toPlayer(sender), message);
	}

	/**
	 * Send a message prepended with the appropriate prefixes.
	 *
	 * @param sender
	 * @param component
	 */
	public static void info(final CommandSender sender, final SimpleComponent component) {
		info(Platform.toPlayer(sender), component);
	}

	/**
	 * Send a message prepended with the appropriate prefixes.
	 *
	 * @param sender
	 * @param message
	 */
	public static void success(final CommandSender sender, final String message) {
		success(Platform.toPlayer(sender), message);
	}

	/**
	 * Send a message prepended with the appropriate prefixes.
	 *
	 * @param sender
	 * @param component
	 */
	public static void success(final CommandSender sender, final SimpleComponent component) {
		success(Platform.toPlayer(sender), component);
	}

	/**
	 * Send a message prepended with the appropriate prefixes.
	 *
	 * @param sender
	 * @param message
	 */
	public static void warn(final CommandSender sender, final String message) {
		warn(Platform.toPlayer(sender), message);
	}

	/**
	 * Send a message prepended with the appropriate prefixes.
	 *
	 * @param sender
	 * @param component
	 */
	public static void warn(final CommandSender sender, final SimpleComponent component) {
		warn(Platform.toPlayer(sender), component);
	}

	/**
	 * Send a message prepended with the appropriate prefixes.
	 *
	 * @param sender
	 * @param message
	 */
	public static void error(final CommandSender sender, final String message) {
		error(Platform.toPlayer(sender), message);
	}

	/**
	 * Send a message prepended with the appropriate prefixes.
	 *
	 * @param sender
	 * @param component
	 */
	public static void error(final CommandSender sender, final SimpleComponent component) {
		error(Platform.toPlayer(sender), component);
	}

	/**
	 * Send a message prepended with the appropriate prefixes.
	 *
	 * @param sender
	 * @param message
	 */
	public static void question(final CommandSender sender, final String message) {
		question(Platform.toPlayer(sender), message);
	}

	/**
	 * Send a message prepended with the appropriate prefixes.
	 *
	 * @param sender
	 * @param component
	 */
	public static void question(final CommandSender sender, final SimpleComponent component) {
		question(Platform.toPlayer(sender), component);
	}

	/**
	 * Send a message prepended with the appropriate prefixes.
	 *
	 * @param sender
	 * @param message
	 */
	public static void announce(final CommandSender sender, final String message) {
		announce(Platform.toPlayer(sender), message);
	}

	/**
	 * Send a message prepended with the appropriate prefixes.
	 *
	 * @param sender
	 * @param component
	 */
	public static void announce(final CommandSender sender, final SimpleComponent component) {
		announce(Platform.toPlayer(sender), component);
	}
}
