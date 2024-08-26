package org.mineacademy.fo.platform;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.conversations.Conversable;
import org.bukkit.entity.Player;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.CommonCore;
import org.mineacademy.fo.MinecraftVersion;
import org.mineacademy.fo.MinecraftVersion.V;
import org.mineacademy.fo.ReflectionUtil;
import org.mineacademy.fo.model.CompToastStyle;
import org.mineacademy.fo.model.DiscordSender;
import org.mineacademy.fo.model.SimpleComponent;
import org.mineacademy.fo.remain.Remain;
import org.mineacademy.fo.remain.internal.BossBarInternals;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import lombok.Getter;
import lombok.NonNull;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.Title.Times;
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
		this.player.performCommand(replacedCommand);
	}

	@Override
	public void sendActionBar(SimpleComponent message) {
		try {
			this.sender.sendActionBar(message);

		} catch (final NoSuchMethodError err) {

			if (MinecraftVersion.olderThan(V.v1_7))
				this.sender.sendMessage(message.toLegacy());

			else if (MinecraftVersion.equals(V.v1_7) && Remain.getChatMessageConstructor() != null && this.isPlayer) {
				try {
					final Object iChatBaseComponent = Remain.convertAdventureToIChatBase(message.toAdventure());
					final Object packet;
					final byte type = 2;

					if (MinecraftVersion.atLeast(V.v1_12)) {
						final Class<?> chatMessageTypeEnum = ReflectionUtil.getNMSClass("ChatMessageType", "net.minecraft.network.chat.ChatMessageType");

						packet = Remain.getChatMessageConstructor().newInstance(iChatBaseComponent, chatMessageTypeEnum.getMethod("a", byte.class).invoke(null, type));

					} else
						packet = Remain.getChatMessageConstructor().newInstance(iChatBaseComponent, type);

					Remain.sendPacket((Player) sender, packet);

				} catch (final ReflectiveOperationException ex) {
					CommonCore.error(ex, "Failed to send action bar to " + ((Player) sender).getName() + ", message: " + message);
				}

			} else {
				if (this.isPlayer)
					this.player.spigot().sendMessage(ChatMessageType.ACTION_BAR, Remain.convertAdventureToBungee(message.toAdventure()));
				else
					this.sender.sendMessage(message.toLegacy());
			}
		}
	}

	@Override
	public void sendBossbarPercent(SimpleComponent message, float progress, Color color, Overlay overlay) {
		try {
			if (MinecraftVersion.olderThan(V.v1_9))
				throw new NoSuchMethodError(); // Adventure has a bug in 1.8.8, use our own handler

			this.sender.showBossBar(BossBar.bossBar(message, progress, CommonCore.getOrDefault(color, BossBar.Color.WHITE), CommonCore.getOrDefault(overlay, BossBar.Overlay.PROGRESS)));

		} catch (final NoSuchMethodError err) {
			if (this.isPlayer) {
				BossBarInternals.getInstance().sendMessage(this.player, message.toLegacy(), progress, color, overlay);

			} else
				this.sender.sendMessage(message.toLegacy());
		}
	}

	@Override
	public void sendBossbarTimed(SimpleComponent message, int secondsToShow, float progress, Color color, Overlay overlay) {
		try {
			if (MinecraftVersion.olderThan(V.v1_9))
				throw new NoSuchMethodError(); // Adventure has a bug in 1.8.8, use our own handler

			final BossBar bar = BossBar.bossBar(message, progress, CommonCore.getOrDefault(color, BossBar.Color.WHITE), CommonCore.getOrDefault(overlay, BossBar.Overlay.PROGRESS));

			this.sender.showBossBar(bar);
			Common.runLater(secondsToShow * 20, () -> this.sender.hideBossBar(bar));

		} catch (final NoSuchMethodError err) {
			if (this.isPlayer) {
				BossBarInternals.getInstance().sendTimedMessage(this.player, message.toLegacy(), secondsToShow, progress, color, overlay);

			} else
				this.sender.sendMessage(message.toLegacy());
		}
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
		try {
			this.sender.sendPlayerListHeaderAndFooter(header, footer);

		} catch (final NoSuchMethodError err) {
			if (this.isPlayer)
				try {
					this.player.setPlayerListHeaderFooter(header.toLegacy(), footer.toLegacy());
				} catch (final NoSuchMethodError ex) {
				}
		}
	}

	@Override
	public void sendTitle(int fadeIn, int stay, int fadeOut, SimpleComponent title, SimpleComponent subtitle) {
		try {
			this.sender.showTitle(Title.title(title.toAdventure(), subtitle.toAdventure(), Times.times(Duration.ofMillis(fadeIn * 50), Duration.ofMillis(stay * 50), Duration.ofMillis(fadeOut * 50))));

		} catch (final NoSuchMethodError err) {

			if (!this.isPlayer) {
				this.sendMessage(title);
				this.sendMessage(subtitle);
			} else {
				try {
					this.player.sendTitle(title.toLegacy(), subtitle.toLegacy(), fadeIn, stay, fadeOut);
				} catch (final NoSuchMethodError ex) {

					try {
						if (titleConstructor == null)
							return;

						resetTitleLegacy(player);

						if (titleTimesConstructor != null) {
							final Object packet = titleTimesConstructor.newInstance(fadeIn, stay, fadeOut);

							Remain.sendPacket(player, packet);
						}

						if (title != null) {
							final Object chatTitle = serializeText(title);
							final Object packet = titleConstructor.newInstance(enumTitle, chatTitle);

							Remain.sendPacket(player, packet);
						}

						if (subtitle != null) {
							final Object chatSubtitle = serializeText(subtitle);
							final Object packet = subtitleConstructor.newInstance(enumSubtitle, chatSubtitle);

							Remain.sendPacket(player, packet);
						}
					} catch (final ReflectiveOperationException ex) {
						throw new ReflectionException(ex, "Error sending title to: " + player.getName() + ", title: " + title + ", subtitle: " + subtitle);
					}
				}

			}
		}
	}

	@Override
	public void resetTitle() {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendRawMessage(Component component) {

		// Ugly hack since most conversations prevent players from receiving messages through other API calls
		if (this.isConversing()) {
			((Conversable) this.player).sendRawMessage(LegacyComponentSerializer.legacySection().serialize(component));

			return;
		}

		// Try native first for best performance
		try {
			if (MinecraftVersion.olderThan(V.v1_8))
				throw new NoSuchMethodError(); // Adventure has a bug in 1.7.10, use our own handler

			this.sender.sendMessage(component);

		} catch (final NoSuchMethodError err) {
			final boolean is1_7 = MinecraftVersion.equals(V.v1_7);
			final String legacy = LegacyComponentSerializer.legacySection().serialize(component);

			if (MinecraftVersion.olderThan(V.v1_7) || !this.isPlayer) {
				this.sender.sendMessage(legacy);

				return;
			}

			final List<String> jsonMessages = new ArrayList<>();
			final JsonObject json = GsonComponentSerializer.gson().serializeToTree(component).getAsJsonObject();

			if (is1_7) {

				// Convert incompatible hover event
				this.convertHoverEvent(json);

				// SPECIAL CASE: If we have {RIADOK} in the message, split the message by it and send each part separately
				// Used in ChatControl to patch 1.7.10 not recognizing the \n newline symbol
				if (json.toString().contains("{RIADOK}")) {
					int count = 0;
					final Map<Integer, JsonArray> split = new HashMap<>();

					for (final JsonElement extraElement : json.get("extra").getAsJsonArray()) {
						final JsonObject extra = extraElement.getAsJsonObject();
						final String text = extra.get("text").getAsString();

						if (text.equals("{RIADOK}"))
							count++;
						else {
							if (!split.containsKey(count))
								split.put(count, new JsonArray());

							split.get(count).add(extra);
						}
					}

					for (final JsonArray array : split.values())
						jsonMessages.add(array.toString());

				} else
					jsonMessages.add(json.toString());
			} else
				jsonMessages.add(json.toString());

			for (final String jsonMessage : jsonMessages)
				try {
					this.player.spigot().sendMessage(Remain.convertJsonToBungee(jsonMessage));

				} catch (final NoSuchMethodError ex) {
					this.sender.sendMessage(legacy);
				}
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
}
