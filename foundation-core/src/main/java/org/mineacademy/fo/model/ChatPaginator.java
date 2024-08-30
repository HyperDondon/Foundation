package org.mineacademy.fo.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mineacademy.fo.ChatUtil;
import org.mineacademy.fo.CommonCore;
import org.mineacademy.fo.ValidCore;
import org.mineacademy.fo.command.PermsCommand;
import org.mineacademy.fo.platform.FoundationPlayer;
import org.mineacademy.fo.platform.Platform;
import org.mineacademy.fo.remain.CompChatColor;
import org.mineacademy.fo.settings.SimpleLocalization;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * A draft API for enumerating chat messages into pages.
 *
 * See {@link PermsCommand} for an early implementation.
 */
@Getter
@RequiredArgsConstructor
public final class ChatPaginator {

	/**
	 * This is the height that will fill all chat lines (20)
	 * if you use {@link #setFoundationHeader(String)}.
	 *
	 * It is 17 because our header is 3 lines wide.
	 */
	public static final int FOUNDATION_HEIGHT = 15;

	/**
	 * Set the custom sending mechanism. You will need to implement how the given audience
	 * receives the given page and return true if sending was successful.
	 */
	@Setter
	private static Sender customSender;

	/**
	 * How many lines per page? Maximum on screen is 20 minus header and footer.
	 */
	private final int linesPerPage;

	/**
	 * The color used in header and footer
	 */
	private final CompChatColor themeColor;

	/**
	 * The header included on every page.
	 */
	private final List<SimpleComponent> header = new ArrayList<>();

	/**
	 * The pages with their content.
	 */
	private final Map<Integer, List<? extends SimpleComponent>> pages = new HashMap<>();

	/**
	 * The footer included on every page.
	 */
	private final List<SimpleComponent> footer = new ArrayList<>();

	/**
	 * Construct chat pages taking the entire visible
	 * chat portion when chat is maximize given {@link #setFoundationHeader(String)}
	 * is used and there is no footer. We use {@link #FOUNDATION_HEIGHT} for height
	 * and {@link org.mineacademy.fo.settings.SimpleLocalization.Commands#HEADER_COLOR} for color.
	 */
	public ChatPaginator() {
		this(FOUNDATION_HEIGHT, SimpleLocalization.Commands.HEADER_COLOR);
	}

	/**
	 * Construct chat pages taking the entire visible
	 * chat portion when chat is maximize given {@link #setFoundationHeader(String)}
	 * is used and there is no footer. We use {@link #FOUNDATION_HEIGHT} for height.
	 *
	 * @param themeColor
	 */
	public ChatPaginator(CompChatColor themeColor) {
		this(FOUNDATION_HEIGHT, themeColor);
	}

	/**
	 * Creates a paginator with the given lines per page. Maximum on screen is 20 minus header and footer.
	 * The {@link org.mineacademy.fo.settings.SimpleLocalization.Commands#HEADER_COLOR} color is used.
	 *
	 * @param linesPerPage
	 */
	public ChatPaginator(int linesPerPage) {
		this(linesPerPage, SimpleLocalization.Commands.HEADER_COLOR);
	}

	/**
	 * Sets the standard Foundation header used across plugins.
	 * ----------------
	 * \<center\>title
	 * ---------------
	 *
	 * @param title
	 * @return
	 */
	public ChatPaginator setFoundationHeader(String title) {
		final String format = SimpleLocalization.Commands.HEADER_FORMAT
				.replace("{theme_color}", this.themeColor.toString())
				.replace("{title}", title);

		final List<String> messages = new ArrayList<>();

		for (String message : format.split("\n")) {

			// Support centering inside the message itself
			final String[] centeredParts = message.split("\\<center\\>");

			if (centeredParts.length > 1) {
				ValidCore.checkBoolean(centeredParts.length == 2, "Cannot use <center> more than once in: " + title);

				message = centeredParts[0] + ChatUtil.center(centeredParts[1], SimpleLocalization.Commands.HEADER_CENTER_LETTER.charAt(0), SimpleLocalization.Commands.HEADER_CENTER_PADDING);
			}

			messages.add(message);
		}

		return this.setHeader(CommonCore.toArray(messages));
	}

	/**
	 * Set the content type
	 *
	 * @param components
	 * @return
	 */
	public ChatPaginator setHeader(SimpleComponent... components) {
		Collections.addAll(this.header, components);

		return this;
	}

	/**
	 * Set the content type
	 *
	 * @param messages
	 * @return
	 */
	public ChatPaginator setHeader(String... messages) {
		for (final String message : messages)
			this.header.add(SimpleComponent.fromMini(message));

		return this;
	}

	/**
	 * Set the content type
	 *
	 * @param messages
	 * @return
	 */
	public ChatPaginator setPages(String... messages) {
		final List<SimpleComponent> pages = new ArrayList<>();

		for (final String message : messages)
			pages.add(SimpleComponent.fromMini(message));

		return this.setPages(pages.toArray(new SimpleComponent[pages.size()]));
	}

	/**
	 * Set the content type
	 *
	 * @param components
	 * @return
	 */
	public ChatPaginator setPages(SimpleComponent... components) {
		this.pages.clear();
		this.pages.putAll(CommonCore.fillPages(this.linesPerPage, Arrays.asList(components)));

		return this;
	}

	/**
	 * Set the content type
	 *
	 * @param components
	 * @return
	 */
	public ChatPaginator setPages(List<SimpleComponent> components) {
		this.pages.clear();
		this.pages.putAll(CommonCore.fillPages(this.linesPerPage, components));

		return this;
	}

	/**
	 * Set the content type
	 *
	 * @param messages
	 * @return
	 */
	public ChatPaginator setFooter(String... messages) {
		for (final String message : messages)
			this.footer.add(SimpleComponent.fromMini(message));

		return this;
	}

	/**
	 * Set the content type
	 *
	 * @param components
	 * @return
	 */
	public ChatPaginator setFooter(SimpleComponent... components) {
		Collections.addAll(this.footer, components);

		return this;
	}

	/**
	 * Start showing the first page to the sender
	 *
	 * @param audience
	 */
	public void send(FoundationPlayer audience) {
		this.send(audience, 1);
	}

	/**
	 * Show the given page to the sender, either paginated or a full dumb when this is a console
	 *
	 * @param audience
	 * @param page
	 */
	public void send(FoundationPlayer audience, int page) {
		if (Platform.isAsync())
			Platform.runTask(0, () -> this.send0(audience, page));
		else
			this.send0(audience, page);
	}

	private void send0(FoundationPlayer audience, int page) {
		if (customSender != null && customSender.send(audience, page, this)) {
			// Successful sending upstream

		} else {
			for (final SimpleComponent component : this.header)
				component.send(audience);

			int amount = 1;

			for (final List<? extends SimpleComponent> components : this.pages.values())
				for (final SimpleComponent component : components)
					component.replaceBracket("count", String.valueOf(amount++)).send(audience);

			for (final SimpleComponent component : this.footer)
				component.send(audience);
		}
	}

	public interface Sender {
		boolean send(FoundationPlayer audience, int page, ChatPaginator instance);
	}
}
