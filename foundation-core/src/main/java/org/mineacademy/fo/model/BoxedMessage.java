package org.mineacademy.fo.model;

import java.util.Arrays;
import java.util.List;

import org.mineacademy.fo.CommonCore;
import org.mineacademy.fo.platform.FoundationPlayer;
import org.mineacademy.fo.platform.Platform;

import lombok.Getter;
import lombok.NonNull;

/**
 * Represents a chat message surrounded by chat-wide line on the top and bottom:
 * <p>
 * -----------------------------------
 * Hello this is a test!
 * -----------------------------------
 * <p>
 * You can also specify &lt;center&gt; in front of the text to center it.
 */
public final class BoxedMessage {

	/**
	 * The top and bottom line itself
	 */
	private static final SimpleComponent LINE = SimpleComponent.fromMini("<dark_gray>" + CommonCore.chatLineSmooth());

	/**
	 * All message recipients
	 */
	private final Iterable<FoundationPlayer> recipients;

	/**
	 * The sender of the message
	 */
	private final FoundationPlayer sender;

	/**
	 * The messages to send
	 */
	@Getter
	private final SimpleComponent[] messages;

	/**
	 * Create a new boxed message from the given messages
	 * without sending it to any player
	 *
	 * @param messages
	 */
	public BoxedMessage(@NonNull SimpleComponent... messages) {
		this(null, null, messages);
	}

	/**
	 * Create a new boxed message
	 *
	 * @param recipients
	 * @param sender
	 * @param messages
	 */
	private BoxedMessage(Iterable<FoundationPlayer> recipients, FoundationPlayer sender, @NonNull SimpleComponent[] messages) {
		this.recipients = recipients == null ? null : CommonCore.toList(recipients); // Make a copy to prevent changes in the list on send
		this.sender = sender;
		this.messages = messages;
	}

	// ------------------------------------------------------------------------------------------------------------
	// Helper methods
	// ------------------------------------------------------------------------------------------------------------

	private void launch() {
		Platform.runTask(2, () -> {
			this.sendFrame();
		});
	}

	private void sendFrame() {
		this.send(LINE);

		for (int i = 0; i < this.getTopLines(); i++)
			this.send(SimpleComponent.empty());

		for (final SimpleComponent message : this.messages)
			this.send(message);

		for (int i = 0; i < this.getBottomLines(); i++)
			this.send(SimpleComponent.empty());

		this.send(LINE);
	}

	private int getTopLines() {
		switch (this.length()) {
			case 1:
				return 2;
			case 2:
			case 3:
			case 4:
				return 1;
			default:
				return 0;
		}
	}

	private int getBottomLines() {
		switch (this.length()) {
			case 1:
			case 2:
				return 2;
			case 3:
				return 1;
			default:
				return 0;
		}
	}

	@SuppressWarnings("unused")
	private int length() {
		int length = 0;

		for (final SimpleComponent message : this.messages)
			length++;

		return length;
	}

	private void send(SimpleComponent message) {
		if (this.recipients == null)
			this.broadcast0(message);

		else
			this.tell0(message);
	}

	private void broadcast0(SimpleComponent message) {
		if (this.sender != null)
			CommonCore.broadcast(message, this.sender);
		else
			CommonCore.broadcastTo(Platform.getOnlinePlayers(), message);
	}

	private void tell0(SimpleComponent message) {
		if (this.sender != null)
			message = message.replaceBracket("player", this.sender.getName());

		CommonCore.broadcastTo(this.recipients, message);
	}

	@Override
	public String toString() {
		return "Boxed{" + SimpleComponent.fromChildren(this.messages).toLegacy() + "}";
	}

	// ------------------------------------------------------------------------------------------------------------
	// Static
	// ------------------------------------------------------------------------------------------------------------

	/**
	 * Send this message to everyone
	 *
	 * @param messages
	 */
	public static void broadcast(String... messages) {
		broadcast(null, messages);
	}

	/**
	 * Send this message to everyone
	 *
	 * @param messages
	 */
	public static void broadcast(SimpleComponent... messages) {
		broadcast(null, messages);
	}

	/**
	 * Sends this message to the all players as the sender
	 *
	 * @param sender
	 * @param messages
	 */
	public static void broadcast(FoundationPlayer sender, String... messages) {
		final List<SimpleComponent> converted = CommonCore.convert(messages, SimpleComponent::fromMini);

		broadcast(sender, converted.toArray(new SimpleComponent[converted.size()]));
	}

	/**
	 * Sends this message to the all players as the sender
	 *
	 * @param sender
	 * @param messages
	 */
	public static void broadcast(FoundationPlayer sender, SimpleComponent... messages) {
		new BoxedMessage(null, sender, messages).launch();
	}

	/**
	 * Sends the message to the recipient
	 *
	 * @param recipient
	 * @param messages
	 */
	public static void tell(FoundationPlayer recipient, String... messages) {
		final List<SimpleComponent> converted = CommonCore.convert(messages, SimpleComponent::fromMini);

		tell(recipient, converted.toArray(new SimpleComponent[converted.size()]));
	}

	/**
	 * Sends the message to the recipient
	 *
	 * @param recipient
	 * @param messages
	 */
	public static void tell(FoundationPlayer recipient, SimpleComponent... messages) {
		tell(null, Arrays.asList(recipient), messages);
	}

	/**
	 * Sends the message to the given recipients
	 *
	 * @param recipients
	 * @param messages
	 */
	public static void tell(Iterable<FoundationPlayer> recipients, SimpleComponent... messages) {
		tell(null, recipients, messages);
	}

	/**
	 * Sends this message to a recipient as sender
	 *
	 * @param sender
	 * @param receiver
	 * @param messages
	 */
	public static void tell(FoundationPlayer sender, FoundationPlayer receiver, SimpleComponent... messages) {
		tell(sender, Arrays.asList(receiver), messages);
	}

	/**
	 * Sends this message to recipients as sender
	 *
	 * @param sender
	 * @param receivers
	 * @param messages
	 */
	public static void tell(FoundationPlayer sender, Iterable<FoundationPlayer> receivers, SimpleComponent... messages) {
		new BoxedMessage(receivers, sender, messages).launch();
	}
}