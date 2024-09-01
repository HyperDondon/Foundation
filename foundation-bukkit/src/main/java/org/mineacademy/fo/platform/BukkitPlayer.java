package org.mineacademy.fo.platform;

import java.net.InetSocketAddress;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.conversations.Conversable;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.mineacademy.fo.MinecraftVersion;
import org.mineacademy.fo.MinecraftVersion.V;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.model.CompToastStyle;
import org.mineacademy.fo.model.DiscordSender;
import org.mineacademy.fo.model.SimpleComponent;
import org.mineacademy.fo.plugin.SimplePlugin;
import org.mineacademy.fo.remain.Remain;
import org.mineacademy.fo.remain.internal.BossBarInternals;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import lombok.Getter;
import lombok.NonNull;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.md_5.bungee.api.ChatMessageType;

@Getter
public class BukkitPlayer extends FoundationPlayer {

	private final CommandSender sender;
	private final boolean isPlayer;
	private final Player player;

	public BukkitPlayer(@NonNull CommandSender sender) {
		this.sender = sender;
		this.isPlayer = sender instanceof Player;
		this.player = isPlayer ? (Player) sender : null;
	}

	@Override
	protected boolean hasPermission0(String permission) {
		return this.sender.hasPermission(permission);
	}

	@Override
	public boolean isConsole() {
		return this.sender instanceof ConsoleCommandSender;
	}

	@Override
	public boolean isConversing() {
		return this.isPlayer && this.player.isConversing();
	}

	@Override
	public boolean isDiscord() {
		return this.sender instanceof DiscordSender;
	}

	@Override
	public boolean isOnline() {
		return this.isPlayer && this.player.isOnline();
	}

	@Override
	public boolean isPlayer() {
		return this.isPlayer;
	}

	@Override
	protected String getSenderName0() {
		return this.sender.getName();
	}

	@Override
	protected void performPlayerCommand0(String replacedCommand) {
		this.player.chat("/" + replacedCommand);
	}

	@Override
	public void sendActionBar(SimpleComponent message) {
		if (!this.isPlayer || MinecraftVersion.olderThan(V.v1_8))
			this.sender.sendMessage(message.toLegacy());

		else
			try {
				this.player.spigot().sendMessage(ChatMessageType.ACTION_BAR, Remain.convertAdventureToBungee(message.toAdventure()));

			} catch (final NoSuchMethodError err) {
				Remain.sendActionBarLegacyPacket(this.player, message);
			}
	}

	@Override
	public void sendBossbarPercent(SimpleComponent message, float progress, Color color, Overlay overlay) {
		if (this.isPlayer) {
			BossBarInternals.getInstance().sendMessage(this.player, message.toLegacy(), progress, color, overlay);

		} else
			this.sender.sendMessage(message.toLegacy());
	}

	@Override
	public void sendBossbarTimed(SimpleComponent message, int secondsToShow, float progress, Color color, Overlay overlay) {
		if (this.isPlayer) {
			BossBarInternals.getInstance().sendTimedMessage(this.player, message.toLegacy(), secondsToShow, progress, color, overlay);

		} else
			this.sender.sendMessage(message.toLegacy());
	}

	@Override
	public void sendToast(SimpleComponent message, CompToastStyle style) {
		if (this.isPlayer)
			Remain.sendToast(this.player, message.toLegacy(), style);
		else
			this.sendMessage(message);
	}

	@Override
	public void sendTablist(SimpleComponent header, SimpleComponent footer) {
		if (this.isPlayer && MinecraftVersion.atLeast(V.v1_8))
			try {
				this.player.setPlayerListHeaderFooter(header.toLegacy(), footer.toLegacy());

			} catch (final NoSuchMethodError ex) {
				Remain.sendTablistLegacyPacket(player, header, footer);
			}
	}

	@Override
	public void sendTitle(int fadeIn, int stay, int fadeOut, SimpleComponent title, SimpleComponent subtitle) {
		if (!this.isPlayer || MinecraftVersion.olderThan(V.v1_8)) {
			this.sendMessage(title);
			this.sendMessage(subtitle);

		} else
			try {
				this.player.sendTitle(title.toLegacy(), subtitle.toLegacy(), fadeIn, stay, fadeOut);

			} catch (final NoSuchMethodError ex) {
				Remain.sendTitleLegacyPacket(this.player, fadeIn, stay, fadeOut, title, subtitle);
			}
	}

	@Override
	public void resetTitle() {
		if (this.isPlayer && MinecraftVersion.atLeast(V.v1_8))
			try {
				this.player.resetTitle();

			} catch (final NoSuchMethodError ex) {
				Remain.resetTitleLegacy(this.player);
			}
	}

	@Override
	public void sendMessage(String message) {

		// Ugly hack since most conversations prevent players from receiving messages through other API calls
		if (this.isConversing())
			((Conversable) this.player).sendRawMessage(message);

		else
			this.sender.sendMessage(message);
	}

	@Override
	public void sendRawMessage(Component component) {

		// Ugly hack since most conversations prevent players from receiving messages through other API calls
		if (this.isConversing()) {
			((Conversable) this.player).sendRawMessage(Remain.convertAdventureToLegacy(component));

			return;
		}

		if (!this.isPlayer) {
			final String legacy = Remain.convertAdventureToLegacy(component);

			// Console does not send empty messages so we add a space
			this.sender.sendMessage(legacy.isEmpty() ? " " : legacy);
			return;
		}

		final long nanoTime = System.nanoTime();

		try {
			if (MinecraftVersion.olderThan(V.v1_16)) {

				if (MinecraftVersion.olderThan(V.v1_7))
					this.sender.sendMessage(Remain.convertAdventureToLegacy(component));

				else {
					String json = GsonComponentSerializer.gson().serialize(component);

					// different hover event key in legacy and adventure conversion is broken, again
					json = json.replace("\"action\":\"show_text\",\"contents\"", "\"action\":\"show_text\",\"value\"");

					try {
						this.player.spigot().sendMessage(Remain.convertJsonToBungee(json));

					} catch (final NoSuchMethodError ex) {
						this.sender.sendMessage(Remain.convertAdventureToLegacy(component));
					}
				}

			} else
				this.sender.spigot().sendMessage(Remain.convertAdventureToBungee(component));

		} finally {
			//System.out.println("sendRawMessage took " + String.format("%.3f", (System.nanoTime() - nanoTime) / 1_000_000D) + "ms");
		}
	}

	/*
	 * Helper method to rename the component's contents to value for legacy MC versions
	 */
	private void convertHoverEvent(JsonObject map) {
		if (map.has("hoverEvent")) {
			final JsonObject hoverEvent = map.get("hoverEvent").getAsJsonObject();
			JsonObject value = null;

			if (hoverEvent.has("contents")) {
				value = hoverEvent.get("contents").getAsJsonObject();

				hoverEvent.remove("contents");
			}

			if (value != null)
				hoverEvent.add("value", value);
		}

		if (map.has("extra")) {
			final JsonArray extraArray = map.get("extra").getAsJsonArray();

			for (final JsonElement key : extraArray)
				convertHoverEvent(key.getAsJsonObject());
		}
	}

	@Override
	public InetSocketAddress getAddress() {
		return this.isPlayer ? this.player.getAddress() : null;
	}

	@Override
	public void setTempMetadata(String key, Object value) {
		Valid.checkBoolean(this.isPlayer, "Cannot set temp metadata for non-players!");

		this.player.setMetadata(key, new FixedMetadataValue(SimplePlugin.getInstance(), value));
	}
}
