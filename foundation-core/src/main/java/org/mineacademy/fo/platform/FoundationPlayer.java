package org.mineacademy.fo.platform;

import java.net.InetSocketAddress;

import org.mineacademy.fo.ChatUtil;
import org.mineacademy.fo.CommonCore;
import org.mineacademy.fo.exception.FoException;
import org.mineacademy.fo.model.CompToastStyle;
import org.mineacademy.fo.model.SimpleComponent;
import org.mineacademy.fo.settings.SimpleLocalization;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;

public abstract class FoundationPlayer {

	public final boolean hasPermission(String permission) {
		if (permission.contains("{") || permission.contains("}"))
			throw new FoException("Permission cannot contain variables: " + permission);

		return this.hasPermission0(permission);
	}

	protected abstract boolean hasPermission0(String permission);

	public abstract boolean isPlayer();

	public abstract boolean isConsole();

	public abstract boolean isConversing();

	public abstract boolean isDiscord();

	public abstract boolean isOnline();

	public final String getName() {
		return this.isConsole() ? SimpleLocalization.CONSOLE_NAME : this.getSenderName0();
	}

	protected abstract String getSenderName0();

	/**
	 * Runs the given command (without /) as the player, replacing {player} with his name.
	 *
	 * You can prefix the command with @(announce|warn|error|info|question|success) to send a formatted
	 * message to playerReplacement directly.
	 *
	 * @param command
	 */
	public final void dispatchCommand(String command) {
		if (command.isEmpty() || command.equalsIgnoreCase("none"))
			return;

		// Remove trailing /
		if (command.startsWith("/") && !command.startsWith("//"))
			command = command.substring(1);

		final String replacedCommand = command.replace("{player}", this.getName());

		Platform.runTask(0, () -> {
			if (isPlayer())
				this.performPlayerCommand0(replacedCommand);
			else
				Platform.dispatchConsoleCommand(replacedCommand);
		});
	}

	protected abstract void performPlayerCommand0(String replacedCommand);

	public final void sendActionBar(String message) {
		this.sendActionBar(SimpleComponent.fromMini(message));
	}

	public abstract void sendActionBar(SimpleComponent message);

	public final void sendBossbarPercent(String message, float progress, BossBar.Color color, BossBar.Overlay overlay) {
		this.sendBossbarPercent(SimpleComponent.fromMini(message), progress, color, overlay);
	}

	public abstract void sendBossbarPercent(SimpleComponent message, float progress, BossBar.Color color, BossBar.Overlay overlay);

	public final void sendBossbarTimed(String message, int seconds, float progress, BossBar.Color color, BossBar.Overlay overlay) {
		this.sendBossbarTimed(SimpleComponent.fromMini(message), seconds, progress, color, overlay);
	}

	public abstract void sendBossbarTimed(SimpleComponent message, int seconds, float progress, BossBar.Color color, BossBar.Overlay overlay);

	public final void sendToast(String message) {
		this.sendToast(SimpleComponent.fromMini(message));
	}

	public final void sendToast(String message, CompToastStyle style) {
		this.sendToast(SimpleComponent.fromMini(message), style);
	}

	public final void sendToast(SimpleComponent message) {
		this.sendToast(message, CompToastStyle.TASK);
	}

	public abstract void sendToast(SimpleComponent message, CompToastStyle style);

	public final void sendTablist(String header, String footer) {
		this.sendTablist(SimpleComponent.fromMini(header), SimpleComponent.fromMini(footer));
	}

	/**
	 * Sets tab-list header and/or footer. Header or footer can be null. (1.8+)
	 * Texts will be colorized.
	 *
	 * @param header the header
	 * @param footer the footer
	 */
	public abstract void sendTablist(final SimpleComponent header, final SimpleComponent footer);

	public final void sendMessage(SimpleComponent component) {
		if (component == null)
			return;

		final String plainMessage = component.toPlain();

		// Avoid sending empty messages
		if (plainMessage.isEmpty() || "none".equals(plainMessage))
			return;

		// Replace player variable
		component = component.replaceBracket("player", this.getName());

		if (plainMessage.startsWith("<actionbar>")) {
			this.sendActionBar(component.replaceLiteral("<actionbar>", ""));

		} else if (plainMessage.startsWith("<toast>")) {
			this.sendToast(component.replaceLiteral("<toast>", ""));

		} else if (plainMessage.startsWith("<title>")) {
			final String stripped = component.toLegacy().replace("<title>", "").trim();

			if (!stripped.isEmpty()) {
				final String[] split = stripped.split("\\|");
				final String title = split[0];
				final String subtitle = split.length > 1 ? CommonCore.joinRange(1, split) : null;

				this.sendTitle(0, 60, 0, title, subtitle);
			}

		} else if (plainMessage.startsWith("<bossbar>")) {
			this.sendBossbarTimed(component.replaceLiteral("<bossbar>", ""), 10, 1F, BossBar.Color.WHITE, BossBar.Overlay.PROGRESS);

		} else {
			if (plainMessage.startsWith("<center>")) {
				final String centeredLegacyMessage = ChatUtil.center(component.toLegacy().replaceAll("\\<center\\>(\\s|)", ""));

				this.sendRawMessage(SimpleComponent.fromSection(centeredLegacyMessage).toAdventure());

			} else
				this.sendRawMessage(component.toAdventure());
		}
	}

	public abstract void sendRawMessage(Component component);

	public abstract void sendMessage(String message);

	/**
	 * Sends a JSON component message to the player
	 *
	 * @param json
	 */
	public final void sendJson(String json) {
		this.sendMessage(SimpleComponent.fromJson(json));
	}

	/**
	 * Sends a title to the player (1.8+) for three seconds
	 *
	 * @param title
	 * @param subtitle
	 */
	public final void sendTitle(final String title, final String subtitle) {
		this.sendTitle(20, 3 * 20, 20, title, subtitle);
	}

	/**
	 * Sends a title to the player (1.8+) for three seconds
	 *
	 * @param title
	 * @param subtitle
	 */
	public final void sendTitle(final SimpleComponent title, final SimpleComponent subtitle) {
		this.sendTitle(20, 3 * 20, 20, title, subtitle);
	}

	/**
	 * Sends a title to the player (1.8+) Texts will be colorized.
	 *
	 * @param fadeIn   how long to fade in the title (in ticks)
	 * @param stay     how long to make the title stay (in ticks)
	 * @param fadeOut  how long to fade out (in ticks)
	 * @param title    the title, will be colorized
	 * @param subtitle the subtitle, will be colorized
	 */
	public final void sendTitle(final int fadeIn, final int stay, final int fadeOut, final String title, final String subtitle) {
		this.sendTitle(fadeIn, stay, fadeOut, SimpleComponent.fromMini(title), SimpleComponent.fromMini(subtitle));
	}

	/**
	 * Sends a title to the player (1.8+) Texts will be colorized.
	 *
	 * @param fadeIn   how long to fade in the title (in ticks)
	 * @param stay     how long to make the title stay (in ticks)
	 * @param fadeOut  how long to fade out (in ticks)
	 * @param title    the title, will be colorized
	 * @param subtitle the subtitle, will be colorized
	 */
	public abstract void sendTitle(final int fadeIn, final int stay, final int fadeOut, final SimpleComponent title, final SimpleComponent subtitle);

	/**
	 * Resets the title that is being displayed to the player (1.8+)
	 */
	public abstract void resetTitle();

	public abstract InetSocketAddress getAddress();
}
