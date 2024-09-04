package org.mineacademy.fo;

import org.mineacademy.fo.model.SimpleComponent;
import org.mineacademy.fo.platform.FoundationPlayer;
import org.mineacademy.fo.platform.Platform;
import org.mineacademy.fo.settings.Lang;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Streamlines the process of sending themed messages to players
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MessengerCore {

	public static SimpleComponent getSuccessPrefix() {
		return Lang.component("prefix-success");
	}

	public static SimpleComponent getInfoPrefix() {
		return Lang.component("prefix-info");
	}

	public static SimpleComponent getWarnPrefix() {
		return Lang.component("prefix-warn");
	}

	public static SimpleComponent getErrorPrefix() {
		return Lang.component("prefix-error");
	}

	public static SimpleComponent getQuestionPrefix() {
		return Lang.component("prefix-question");
	}

	public static SimpleComponent getAnnouncePrefix() {
		return Lang.component("prefix-announce");
	}

	/**
	 * Send a message prepended with the appropriate prefixes.
	 *
	 * @param message
	 */
	public static void broadcastInfo(final String message) {
		for (final FoundationPlayer online : Platform.getOnlinePlayers())
			info(online, message);
	}

	/**
	 * Send a message prepended with the appropriate prefixes.
	 *
	 * @param component
	 */
	public static void broadcastInfo(final SimpleComponent component) {
		for (final FoundationPlayer online : Platform.getOnlinePlayers())
			info(online, component);
	}

	/**
	 * Send a message prepended with the appropriate prefixes.
	 *
	 * @param message
	 */
	public static void broadcastSuccess(final String message) {
		for (final FoundationPlayer online : Platform.getOnlinePlayers())
			success(online, message);
	}

	/**
	 * Send a message prepended with the appropriate prefixes.
	 *
	 * @param component
	 */
	public static void broadcastSuccess(final SimpleComponent component) {
		for (final FoundationPlayer online : Platform.getOnlinePlayers())
			success(online, component);
	}

	/**
	 * Send a message prepended with the appropriate prefixes.
	 *
	 * @param message
	 */
	public static void broadcastWarn(final String message) {
		for (final FoundationPlayer online : Platform.getOnlinePlayers())
			warn(online, message);
	}

	/**
	 * Send a message prepended with the appropriate prefixes.
	 *
	 * @param component
	 */
	public static void broadcastWarn(final SimpleComponent component) {
		for (final FoundationPlayer online : Platform.getOnlinePlayers())
			warn(online, component);
	}

	/**
	 * Send a message prepended with the appropriate prefixes.
	 *
	 * @param message
	 */
	public static void broadcastError(final String message) {
		for (final FoundationPlayer online : Platform.getOnlinePlayers())
			error(online, message);
	}

	/**
	 * Send a message prepended with the appropriate prefixes.
	 *
	 * @param component
	 */
	public static void broadcastError(final SimpleComponent component) {
		for (final FoundationPlayer online : Platform.getOnlinePlayers())
			error(online, component);
	}

	/**
	 * Send a message prepended with the appropriate prefixes.
	 *
	 * @param message
	 */
	public static void broadcastQuestion(final String message) {
		for (final FoundationPlayer online : Platform.getOnlinePlayers())
			question(online, message);
	}

	/**
	 * Send a message prepended with the appropriate prefixes.
	 *
	 * @param component
	 */
	public static void broadcastQuestion(final SimpleComponent component) {
		for (final FoundationPlayer online : Platform.getOnlinePlayers())
			question(online, component);
	}

	/**
	 * Send a message prepended with the appropriate prefixes.
	 *
	 * @param message
	 */
	public static void broadcastAnnounce(final String message) {
		for (final FoundationPlayer online : Platform.getOnlinePlayers())
			announce(online, message);
	}

	/**
	 * Send a message prepended with the appropriate prefixes.
	 *
	 * @param component
	 */
	public static void broadcastAnnounce(final SimpleComponent component) {
		for (final FoundationPlayer online : Platform.getOnlinePlayers())
			announce(online, component);
	}

	/**
	 * Send a message prepended with the appropriate prefixes.
	 *
	 * @param player
	 * @param message
	 */
	public static void info(final FoundationPlayer player, final String message) {
		tell(player, getInfoPrefix(), message);
	}

	/**
	 * Send a message prepended with the appropriate prefixes.
	 *
	 * @param player
	 * @param component
	 */
	public static void info(final FoundationPlayer player, final SimpleComponent component) {
		tell(player, getInfoPrefix(), component);
	}

	/**
	 * Send a message prepended with the appropriate prefixes.
	 *
	 * @param player
	 * @param message
	 */
	public static void success(final FoundationPlayer player, final String message) {
		tell(player, getSuccessPrefix(), message);
	}

	/**
	 * Send a message prepended with the appropriate prefixes.
	 *
	 * @param player
	 * @param component
	 */
	public static void success(final FoundationPlayer player, final SimpleComponent component) {
		tell(player, getSuccessPrefix(), component);
	}

	/**
	 * Send a message prepended with the appropriate prefixes.
	 *
	 * @param player
	 * @param message
	 */
	public static void warn(final FoundationPlayer player, final String message) {
		tell(player, getWarnPrefix(), message);
	}

	/**
	 * Send a message prepended with the appropriate prefixes.
	 *
	 * @param player
	 * @param component
	 */
	public static void warn(final FoundationPlayer player, final SimpleComponent component) {
		tell(player, getWarnPrefix(), component);
	}

	/**
	 * Send a message prepended with the appropriate prefixes.
	 *
	 * @param player
	 * @param message
	 */
	public static void error(final FoundationPlayer player, final String message) {
		tell(player, getErrorPrefix(), message);
	}

	/**
	 * Send a message prepended with the appropriate prefixes.
	 *
	 * @param player
	 * @param component
	 */
	public static void error(final FoundationPlayer player, final SimpleComponent component) {
		tell(player, getErrorPrefix(), component);
	}

	/**
	 * Send a message prepended with the appropriate prefixes.
	 *
	 * @param player
	 * @param message
	 */
	public static void question(final FoundationPlayer player, final String message) {
		tell(player, getQuestionPrefix(), message);
	}

	/**
	 * Send a message prepended with the appropriate prefixes.
	 *
	 * @param player
	 * @param component
	 */
	public static void question(final FoundationPlayer player, final SimpleComponent component) {
		tell(player, getQuestionPrefix(), component);
	}

	/**
	 * Send a message prepended with the appropriate prefixes.
	 *
	 * @param player
	 * @param message
	 */
	public static void announce(final FoundationPlayer player, final String message) {
		tell(player, getAnnouncePrefix(), message);
	}

	/**
	 * Send a message prepended with the appropriate prefixes.
	 *
	 * @param player
	 * @param component
	 */
	public static void announce(final FoundationPlayer player, final SimpleComponent component) {
		tell(player, getAnnouncePrefix(), component);
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
