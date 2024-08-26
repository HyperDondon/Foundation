package org.mineacademy.fo.conversation;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationPrefix;
import org.mineacademy.fo.model.SimpleComponent;

import lombok.Getter;

/**
 * A simple conversation prefix with a static string
 */
public final class SimplePrefix implements ConversationPrefix {

	/**
	 * The conversation prefix
	 */
	@Getter
	private final String prefix;

	public SimplePrefix(String prefix) {
		this.prefix = SimpleComponent.fromMini(prefix).toLegacy();
	}

	@Override
	public String getPrefix(ConversationContext context) {
		return this.prefix;
	}
}