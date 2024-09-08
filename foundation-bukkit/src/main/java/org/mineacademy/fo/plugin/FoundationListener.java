package org.mineacademy.fo.plugin;

import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServiceRegisterEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.MathUtilCore;
import org.mineacademy.fo.Messenger;
import org.mineacademy.fo.MinecraftVersion;
import org.mineacademy.fo.MinecraftVersion.V;
import org.mineacademy.fo.constants.FoConstants;
import org.mineacademy.fo.model.ChatPaginator;
import org.mineacademy.fo.model.HookManager;
import org.mineacademy.fo.model.SimpleComponent;
import org.mineacademy.fo.model.SimpleScoreboard;
import org.mineacademy.fo.platform.FoundationPlayer;
import org.mineacademy.fo.platform.Platform;
import org.mineacademy.fo.settings.Lang;

/**
 * Listens for some events we handle for you automatically
 */
final class FoundationListener implements Listener {

	FoundationListener() {
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onQuit(PlayerQuitEvent event) {
		SimpleScoreboard.clearBoardsFor(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onServiceRegister(ServiceRegisterEvent event) {
		HookManager.updateVaultIntegration();
	}

	/**
	 * Handler for {@link ChatPaginator}
	 *
	 * @param event
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void onCommand(PlayerCommandPreprocessEvent event) {

		final Player player = event.getPlayer();
		final FoundationPlayer sender = Platform.toPlayer(player);
		final String message = event.getMessage();

		if (!message.startsWith("/#flp"))
			return;

		final String[] args = message.split(" ");

		if (args.length != 2) {
			Lang.component("page-no-page-number").send(sender);

			event.setCancelled(true);
			return;
		}

		if (!player.hasMetadata(FoConstants.NBT.PAGINATION)) {
			event.setCancelled(true);

			return;
		}

		final String numberRaw = args[1];
		int page = -1;

		try {
			page = Integer.parseInt(numberRaw) - 1;

		} catch (final NumberFormatException ex) {
			Lang.componentVars("page-invalid-page", "input", numberRaw).send(sender);

			event.setCancelled(true);
			return;
		}

		final ChatPaginator chatPages = (ChatPaginator) player.getMetadata(FoConstants.NBT.PAGINATION).get(0).value();
		final Map<Integer, List<SimpleComponent>> pages = chatPages.getPages();

		// Remove empty lines
		pages.entrySet().removeIf(entry -> entry.getValue().isEmpty());

		if (pages.isEmpty() || !pages.containsKey(page)) {
			Messenger.error(player, pages.isEmpty()
					? Lang.component("page-no-pages")
					: Lang.component("page-no-page"));

			event.setCancelled(true);

			return;
		}

		{ // Send the message body
			for (final SimpleComponent component : chatPages.getHeader())
				component.send(sender);

			final List<SimpleComponent> messagesOnPage = pages.get(page);
			int count = 1;

			for (final SimpleComponent comp : messagesOnPage)
				comp.replaceBracket("count", String.valueOf(page + count++)).send(sender);

			int whiteLines = chatPages.getLinesPerPage();

			if (whiteLines == 15 && pages.size() == 1)
				if (messagesOnPage.size() < 17)
					whiteLines = 7;
				else
					whiteLines += 2;

			for (int i = messagesOnPage.size(); i < whiteLines; i++)
				SimpleComponent.fromPlain(" ").send(sender);

			for (final SimpleComponent component : chatPages.getFooter())
				component.send(sender);
		}

		// Fill in the pagination line
		if (MinecraftVersion.atLeast(V.v1_7) && pages.size() > 1) {
			player.sendMessage(" ");

			final int pagesDigits = (int) (Math.log10(pages.size()) + 1);
			final int multiply = 23 - (int) MathUtilCore.ceiling(pagesDigits);

			SimpleComponent clickableFooter = SimpleComponent
					.fromMini("&8&m" + Common.duplicate("-", multiply) + "&r");

			if (page == 0)
				clickableFooter = clickableFooter.appendMini(" &7« ");
			else
				clickableFooter = clickableFooter
						.appendMini(" &6« ")
						.onHover(Lang.componentVars("page-go-to-page", "page", String.valueOf(page)))
						.onClickRunCmd("/#flp " + page);

			clickableFooter = clickableFooter
					.appendMini("&f" + (page + 1)).onHover(Lang.component("page-go-to-first-page")).onClickRunCmd("/#flp 1")
					.appendMini("&7/").onHover(Lang.componentArray("page-tooltip"))
					.appendMini("&f" + pages.size() + "").onHover(Lang.component("page-go-to-last-page")).onClickRunCmd("/#flp " + pages.size());

			if (page + 1 >= pages.size())
				clickableFooter = clickableFooter.appendMini(" &7» ");
			else
				clickableFooter = clickableFooter
						.appendMini(" &6» ")
						.onHover(Lang.componentVars("page-go-to-page", "page", String.valueOf(page + 2)))
						.onClickRunCmd("/#flp " + (page + 2));

			clickableFooter
					.appendMini("&8&m" + Common.duplicate("-", multiply))
					.send(sender);
		}

		// Prevent "Unknown command message"
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onJoin(PlayerJoinEvent event) {
		final Player player = event.getPlayer();

		// Workaround for Essentials and CMI bug where they report "vanished" metadata when
		// the /vanish command is run, but forgot to do so after reload, despite player still
		// being vanished. So we just set the metadata on join back manually.
		//
		// Saves tons of performance when we check if a player is vanished.
		if (!player.hasMetadata("vanished")) {
			final boolean essVanished = HookManager.isVanishedEssentials(player);
			final boolean cmiVanished = HookManager.isVanishedCMI(player);
			final boolean advVanished = HookManager.isVanishedAdvancedVanish(player);
			final boolean premiumVanishVanished = HookManager.isVanishedPremiumVanish(player);

			if (essVanished || cmiVanished || advVanished || premiumVanishVanished) {
				final Plugin plugin = Bukkit.getPluginManager().getPlugin(essVanished ? "Essentials" : cmiVanished ? "CMI" : advVanished ? "AdvancedVanish" : "PremiumVanish");

				player.setMetadata("vanished", new FixedMetadataValue(plugin, true));
			}
		}
	}

}