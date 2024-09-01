package org.mineacademy.fo;

import static org.mineacademy.fo.settings.SimpleLocalization.Prefix.ANNOUNCE;
import static org.mineacademy.fo.settings.SimpleLocalization.Prefix.ERROR;
import static org.mineacademy.fo.settings.SimpleLocalization.Prefix.INFO;
import static org.mineacademy.fo.settings.SimpleLocalization.Prefix.QUESTION;
import static org.mineacademy.fo.settings.SimpleLocalization.Prefix.SUCCESS;
import static org.mineacademy.fo.settings.SimpleLocalization.Prefix.WARN;

import org.mineacademy.fo.model.SimpleComponent;
import org.mineacademy.fo.platform.FoundationPlayer;
import org.mineacademy.fo.platform.Platform;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Streamlines the process of sending themed messages to players
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MessengerCore {

	/**
	 * Send a message prepended with the {@link org.mineacademy.fo.settings.SimpleLocalization.Prefix} prefixes.
	 *
	 * @param message
	 */
	public static void broadcastInfo(final String message) {
		for (final FoundationPlayer online : Platform.getOnlinePlayers())
			tell(online, INFO, message);
	}

	/**
	 * Send a message prepended with the {@link org.mineacademy.fo.settings.SimpleLocalization.Prefix} prefixes.
	 *
	 * @param component
	 */
	public static void broadcastInfo(final SimpleComponent component) {
		for (final FoundationPlayer online : Platform.getOnlinePlayers())
			tell(online, INFO, component);
	}

	/**
	 * Send a message prepended with the {@link org.mineacademy.fo.settings.SimpleLocalization.Prefix} prefixes.
	 *
	 * @param message
	 */
	public static void broadcastSuccess(final String message) {
		for (final FoundationPlayer online : Platform.getOnlinePlayers())
			tell(online, SUCCESS, message);
	}

	/**
	 * Send a message prepended with the {@link org.mineacademy.fo.settings.SimpleLocalization.Prefix} prefixes.
	 *
	 * @param component
	 */
	public static void broadcastSuccess(final SimpleComponent component) {
		for (final FoundationPlayer online : Platform.getOnlinePlayers())
			tell(online, SUCCESS, component);
	}

	/**
	 * Send a message prepended with the {@link org.mineacademy.fo.settings.SimpleLocalization.Prefix} prefixes.
	 *
	 * @param message
	 */
	public static void broadcastWarn(final String message) {
		for (final FoundationPlayer online : Platform.getOnlinePlayers())
			tell(online, WARN, message);
	}

	/**
	 * Send a message prepended with the {@link org.mineacademy.fo.settings.SimpleLocalization.Prefix} prefixes.
	 *
	 * @param component
	 */
	public static void broadcastWarn(final SimpleComponent component) {
		for (final FoundationPlayer online : Platform.getOnlinePlayers())
			tell(online, WARN, component);
	}

	/**
	 * Send a message prepended with the {@link org.mineacademy.fo.settings.SimpleLocalization.Prefix} prefixes.
	 *
	 * @param message
	 */
	public static void broadcastError(final String message) {
		for (final FoundationPlayer online : Platform.getOnlinePlayers())
			tell(online, ERROR, message);
	}

	/**
	 * Send a message prepended with the {@link org.mineacademy.fo.settings.SimpleLocalization.Prefix} prefixes.
	 *
	 * @param component
	 */
	public static void broadcastError(final SimpleComponent component) {
		for (final FoundationPlayer online : Platform.getOnlinePlayers())
			tell(online, ERROR, component);
	}

	/**
	 * Send a message prepended with the {@link org.mineacademy.fo.settings.SimpleLocalization.Prefix} prefixes.
	 *
	 * @param message
	 */
	public static void broadcastQuestion(final String message) {
		for (final FoundationPlayer online : Platform.getOnlinePlayers())
			tell(online, QUESTION, message);
	}

	/**
	 * Send a message prepended with the {@link org.mineacademy.fo.settings.SimpleLocalization.Prefix} prefixes.
	 *
	 * @param component
	 */
	public static void broadcastQuestion(final SimpleComponent component) {
		for (final FoundationPlayer online : Platform.getOnlinePlayers())
			tell(online, QUESTION, component);
	}

	/**
	 * Send a message prepended with the {@link org.mineacademy.fo.settings.SimpleLocalization.Prefix} prefixes.
	 *
	 * @param message
	 */
	public static void broadcastAnnounce(final String message) {
		for (final FoundationPlayer online : Platform.getOnlinePlayers())
			tell(online, ANNOUNCE, message);
	}

	/**
	 * Send a message prepended with the {@link org.mineacademy.fo.settings.SimpleLocalization.Prefix} prefixes.
	 *
	 * @param component
	 */
	public static void broadcastAnnounce(final SimpleComponent component) {
		for (final FoundationPlayer online : Platform.getOnlinePlayers())
			tell(online, ANNOUNCE, component);
	}

	/**
	 * Send a message prepended with the {@link org.mineacademy.fo.settings.SimpleLocalization.Prefix} prefixes.
	 *
	 * @param player
	 * @param message
	 */
	public static void info(final FoundationPlayer player, final String message) {
		tell(player, INFO, message);
	}

	/**
	 * Send a message prepended with the {@link org.mineacademy.fo.settings.SimpleLocalization.Prefix} prefixes.
	 *
	 * @param player
	 * @param component
	 */
	public static void info(final FoundationPlayer player, final SimpleComponent component) {
		tell(player, INFO, component);
	}

	/**
	 * Send a message prepended with the {@link org.mineacademy.fo.settings.SimpleLocalization.Prefix} prefixes.
	 *
	 * @param player
	 * @param message
	 */
	public static void success(final FoundationPlayer player, final String message) {
		tell(player, SUCCESS, message);
	}

	/**
	 * Send a message prepended with the {@link org.mineacademy.fo.settings.SimpleLocalization.Prefix} prefixes.
	 *
	 * @param player
	 * @param component
	 */
	public static void success(final FoundationPlayer player, final SimpleComponent component) {
		tell(player, SUCCESS, component);
	}

	/**
	 * Send a message prepended with the {@link org.mineacademy.fo.settings.SimpleLocalization.Prefix} prefixes.
	 *
	 * @param player
	 * @param message
	 */
	public static void warn(final FoundationPlayer player, final String message) {
		tell(player, WARN, message);
	}

	/**
	 * Send a message prepended with the {@link org.mineacademy.fo.settings.SimpleLocalization.Prefix} prefixes.
	 *
	 * @param player
	 * @param component
	 */
	public static void warn(final FoundationPlayer player, final SimpleComponent component) {
		tell(player, WARN, component);
	}

	/**
	 * Send a message prepended with the {@link org.mineacademy.fo.settings.SimpleLocalization.Prefix} prefixes.
	 *
	 * @param player
	 * @param message
	 */
	public static void error(final FoundationPlayer player, final String message) {
		tell(player, ERROR, message);
	}

	/**
	 * Send a message prepended with the {@link org.mineacademy.fo.settings.SimpleLocalization.Prefix} prefixes.
	 *
	 * @param player
	 * @param component
	 */
	public static void error(final FoundationPlayer player, final SimpleComponent component) {
		tell(player, ERROR, component);
	}

	/**
	 * Send a message prepended with the {@link org.mineacademy.fo.settings.SimpleLocalization.Prefix} prefixes.
	 *
	 * @param player
	 * @param message
	 */
	public static void question(final FoundationPlayer player, final String message) {
		tell(player, QUESTION, message);
	}

	/**
	 * Send a message prepended with the {@link org.mineacademy.fo.settings.SimpleLocalization.Prefix} prefixes.
	 *
	 * @param player
	 * @param component
	 */
	public static void question(final FoundationPlayer player, final SimpleComponent component) {
		tell(player, QUESTION, component);
	}

	/**
	 * Send a message prepended with the {@link org.mineacademy.fo.settings.SimpleLocalization.Prefix} prefixes.
	 *
	 * @param player
	 * @param message
	 */
	public static void announce(final FoundationPlayer player, final String message) {
		tell(player, ANNOUNCE, message);
	}

	/**
	 * Send a message prepended with the {@link org.mineacademy.fo.settings.SimpleLocalization.Prefix} prefixes.
	 *
	 * @param player
	 * @param component
	 */
	public static void announce(final FoundationPlayer player, final SimpleComponent component) {
		tell(player, ANNOUNCE, component);
	}

	/*
	 * Perform the sending
	 */
	private static void tell(final FoundationPlayer sender, final SimpleComponent prefix, @NonNull String message) {
		tell(sender, prefix, SimpleComponent.fromMini(message));
	}

	/*
	 * Internal method to perform the sending
	 */
	private static void tell(final FoundationPlayer sender, final SimpleComponent prefix, @NonNull SimpleComponent component) {
		prefix.appendPlain(" ").append(component).send(sender);
	}
}
