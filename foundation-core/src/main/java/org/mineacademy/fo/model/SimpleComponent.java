package org.mineacademy.fo.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mineacademy.fo.CommonCore;
import org.mineacademy.fo.ValidCore;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.fo.debug.Debugger;
import org.mineacademy.fo.exception.FoScriptException;
import org.mineacademy.fo.platform.FoundationPlayer;
import org.mineacademy.fo.remain.RemainCore;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

/**
 * A very simple way of sending interactive chat messages
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SimpleComponent implements ConfigSerializable, ComponentLike {

	/**
	 * Stores legacy colors
	 */
	private static final Map<String, String> LEGACY_COLOR_MAP = new HashMap<>();

	/**
	 * The pattern for matching MiniMessage tags
	 */
	private static final Pattern MINIMESSAGE_PATTERN = Pattern.compile("<[!?#]?[a-z0-9_-]*>");

	static {
		LEGACY_COLOR_MAP.put("&0", "<black>");
		LEGACY_COLOR_MAP.put("&1", "<dark_blue>");
		LEGACY_COLOR_MAP.put("&2", "<dark_green>");
		LEGACY_COLOR_MAP.put("&3", "<dark_aqua>");
		LEGACY_COLOR_MAP.put("&4", "<dark_red>");
		LEGACY_COLOR_MAP.put("&5", "<dark_purple>");
		LEGACY_COLOR_MAP.put("&6", "<gold>");
		LEGACY_COLOR_MAP.put("&7", "<gray>");
		LEGACY_COLOR_MAP.put("&8", "<dark_gray>");
		LEGACY_COLOR_MAP.put("&9", "<blue>");
		LEGACY_COLOR_MAP.put("&a", "<green>");
		LEGACY_COLOR_MAP.put("&b", "<aqua>");
		LEGACY_COLOR_MAP.put("&c", "<red>");
		LEGACY_COLOR_MAP.put("&d", "<light_purple>");
		LEGACY_COLOR_MAP.put("&e", "<yellow>");
		LEGACY_COLOR_MAP.put("&f", "<white>");
		LEGACY_COLOR_MAP.put("&n", "<u>");
		LEGACY_COLOR_MAP.put("&m", "<st>");
		LEGACY_COLOR_MAP.put("&k", "<obf>");
		LEGACY_COLOR_MAP.put("&o", "<i>");
		LEGACY_COLOR_MAP.put("&l", "<b>");
		LEGACY_COLOR_MAP.put("&r", "<r>");
		LEGACY_COLOR_MAP.put("§0", "<black>");
		LEGACY_COLOR_MAP.put("§1", "<dark_blue>");
		LEGACY_COLOR_MAP.put("§2", "<dark_green>");
		LEGACY_COLOR_MAP.put("§3", "<dark_aqua>");
		LEGACY_COLOR_MAP.put("§4", "<dark_red>");
		LEGACY_COLOR_MAP.put("§5", "<dark_purple>");
		LEGACY_COLOR_MAP.put("§6", "<gold>");
		LEGACY_COLOR_MAP.put("§7", "<gray>");
		LEGACY_COLOR_MAP.put("§8", "<dark_gray>");
		LEGACY_COLOR_MAP.put("§9", "<blue>");
		LEGACY_COLOR_MAP.put("§a", "<green>");
		LEGACY_COLOR_MAP.put("§b", "<aqua>");
		LEGACY_COLOR_MAP.put("§c", "<red>");
		LEGACY_COLOR_MAP.put("§d", "<light_purple>");
		LEGACY_COLOR_MAP.put("§e", "<yellow>");
		LEGACY_COLOR_MAP.put("§f", "<white>");
		LEGACY_COLOR_MAP.put("§n", "<u>");
		LEGACY_COLOR_MAP.put("§m", "<st>");
		LEGACY_COLOR_MAP.put("§k", "<obf>");
		LEGACY_COLOR_MAP.put("§o", "<i>");
		LEGACY_COLOR_MAP.put("§l", "<b>");
		LEGACY_COLOR_MAP.put("§r", "<r>");
	}

	@Setter
	private static Builder builder;

	/**
	 * The component we are creating
	 */
	private final List<ConditionalComponent> components;

	/**
	 * Shall this component ignore empty components? Defaults to false
	 */
	@Getter
	private boolean ignoreEmpty = false;

	// --------------------------------------------------------------------
	// Events
	// --------------------------------------------------------------------

	/**
	 * Add a show text event
	 *
	 * @param lines
	 * @return
	 */
	public SimpleComponent onHover(final Collection<String> lines) {
		return this.onHover(String.join("\n", lines));
	}

	/**
	 * Add a show text hover event
	 *
	 * @param lines
	 * @return
	 */
	public SimpleComponent onHover(final String... lines) {
		return this.onHover(String.join("\n", lines));
	}

	/**
	 * Add a show text event
	 *
	 * @param hover
	 * @return
	 */
	public SimpleComponent onHover(final String hover) {
		this.modifyLastComponent(component -> component.hoverEvent(fromMini(hover).toAdventure()));

		return this;
	}

	/**
	 * Add a hover event
	 *
	 * @param components
	 * @return
	 */
	public SimpleComponent onHover(final SimpleComponent... components) {
		SimpleComponent joined = SimpleComponent.empty();

		for (int i = 0; i < components.length; i++) {
			joined = joined.append(components[i]);

			if (i < components.length - 1)
				joined = joined.appendNewLine();
		}

		final Component finalComponent = joined.asComponent();

		this.modifyLastComponent(component -> component.hoverEvent(finalComponent));

		return this;
	}

	/**
	 * Add a hover event
	 *
	 * @param hover
	 * @return
	 */
	public SimpleComponent onHover(final HoverEventSource<?> hover) {
		this.modifyLastComponent(component -> component.hoverEvent(hover));

		return this;
	}

	/**
	 * Add a run command event
	 *
	 * @param text
	 * @return
	 */
	public SimpleComponent onClickRunCmd(final String text) {
		this.modifyLastComponent(component -> component.clickEvent(ClickEvent.runCommand(text)));

		return this;
	}

	/**
	 * Add a suggest command event
	 *
	 * @param text
	 * @return
	 */
	public SimpleComponent onClickSuggestCmd(final String text) {
		this.modifyLastComponent(component -> component.clickEvent(ClickEvent.suggestCommand(text)));

		return this;
	}

	/**
	 * Open the given URL
	 *
	 * @param url
	 * @return
	 */
	public SimpleComponent onClickOpenUrl(final String url) {
		this.modifyLastComponent(component -> component.clickEvent(ClickEvent.openUrl(url)));

		return this;
	}

	/**
	 * Open the given URL
	 *
	 * @param url
	 * @return
	 */
	public SimpleComponent onClickCopyToClipboard(final String url) {
		this.modifyLastComponent(component -> component.clickEvent(ClickEvent.copyToClipboard(url)));

		return this;
	}

	/**
	 * Invoke SimpleComponent setInsertion
	 *
	 * @param insertion
	 * @return
	 */
	public SimpleComponent onClickInsert(final String insertion) {
		this.modifyLastComponent(component -> component.insertion(insertion));

		return this;
	}

	/**
	 * Set the view condition for this component
	 *
	 * @param viewCondition
	 * @return
	 */
	public SimpleComponent viewCondition(final String viewCondition) {
		this.getLastComponent().setViewCondition(viewCondition);

		return this;
	}

	/**
	 * Set the view permission for this component
	 *
	 * @param viewPermission
	 * @return
	 */
	public SimpleComponent viewPermission(final String viewPermission) {
		this.getLastComponent().setViewPermission(viewPermission);

		return this;
	}

	/**
	 * Quickly replaces an object in all parts of this component, adding
	 * {} around it.
	 *
	 * @param variable the bracket variable
	 * @param value
	 * @return
	 */
	public SimpleComponent replaceBracket(final String variable, final String value) {
		return this.replaceBracket(variable, fromMini(value));
	}

	/**
	 * Quickly replaces an object in all parts of this component, adding
	 * {} around it.
	 *
	 * @param variable the bracket variable
	 * @param value
	 * @return
	 */
	public SimpleComponent replaceBracket(final String variable, final SimpleComponent value) {
		return this.replaceLiteral("{" + variable + "}", value);
	}

	/**
	 * Quickly replaces an object in all parts of this component
	 *
	 * @param variable the bracket variable
	 * @param value
	 * @return
	 */
	public SimpleComponent replaceLiteral(final String variable, final String value) {
		return this.replaceLiteral(variable, fromMini(value));
	}

	/**
	 * Quickly replaces an object in all parts of this component
	 *
	 * @param variable the bracket variable
	 * @param value
	 * @return
	 */
	public SimpleComponent replaceLiteral(final String variable, final SimpleComponent value) {
		for (final ConditionalComponent part : this.components)
			part.setComponent(part.getComponent().replaceText(b -> b.matchLiteral(variable).replacement(value)));

		return this;
	}

	/**
	 * Quickly replaces a pattern in all parts of this component
	 * with the given replacement function
	 *
	 * @param pattern
	 * @param replacement
	 * @return
	 */
	public SimpleComponent replaceMatch(final Pattern pattern, final BiFunction<MatchResult, TextComponent.Builder, ComponentLike> replacement) {
		for (final ConditionalComponent part : this.components)
			part.setComponent(part.getComponent().replaceText(b -> b.match(pattern).replacement(replacement)));

		return this;
	}

	/**
	 * Quickly replaces a pattern in all parts of this component
	 * with the given replacement function
	 *
	 * @param pattern
	 * @param replacement
	 * @return
	 */
	public SimpleComponent replaceMatch(final Pattern pattern, String replacement) {
		for (final ConditionalComponent part : this.components)
			part.setComponent(part.getComponent().replaceText(b -> b.match(pattern).replacement(replacement)));

		return this;
	}

	/**
	 * Quickly replaces a pattern in all parts of this component
	 * with the given replacement function
	 *
	 * @param pattern
	 * @param replacement
	 * @return
	 */
	public SimpleComponent replaceMatch(final Pattern pattern, SimpleComponent replacement) {
		for (final ConditionalComponent part : this.components)
			part.setComponent(part.getComponent().replaceText(b -> b.match(pattern).replacement(replacement)));

		return this;
	}

	// --------------------------------------------------------------------
	// Building
	// --------------------------------------------------------------------

	/**
	 * Append text to this simple component
	 *
	 * @param text
	 * @return
	 */
	public SimpleComponent appendPlain(final String text) {
		return this.append(fromPlain(text));
	}

	/**
	 * Append text to this simple component
	 *
	 * @param text
	 * @return
	 */
	public SimpleComponent appendSection(final String text) {
		return this.append(fromSection(text));
	}

	/**
	 * Append text to this simple component
	 *
	 * @param text
	 * @return
	 */
	public SimpleComponent appendMini(final String text) {
		return this.append(fromMini(text));
	}

	/**
	 * Append a new simple component
	 *
	 * @param component
	 * @return
	 */
	public SimpleComponent append(final SimpleComponent component) {
		for (final ConditionalComponent part : component.components)
			this.components.add(part);

		return this;
	}

	/**
	 * Append a new component on the end of this one
	 *
	 * @param component
	 * @return
	 */
	/*public SimpleComponent append(final SimpleComponent component) {
		this.components.add(ConditionalComponent.fromComponent(component));
	
		return this;
	}*/

	/**
	 * Append a new line on the end of this component
	 *
	 * @return
	 */
	public SimpleComponent appendNewLine() {
		this.components.add(ConditionalComponent.fromComponent(Component.newline()));

		return this;
	}

	/**
	 * Return if this component is empty
	 *
	 * @return
	 */
	public boolean isEmpty() {
		return this.components.isEmpty() || this.toPlain().isEmpty();
	}

	/**
	 * Return the plain colorized message combining all components into one
	 * without click/hover events
	 *
	 * @return
	 */
	public String toLegacy() {
		return LegacyComponentSerializer.legacySection().serialize(this.buildToAdventure(null, null));
	}

	/**
	 * Return the plain colorless message combining all components into one
	 * without click/hover events.
	 *
	 * This effectivelly removes all & and § colors as well as MiniMessage tags.
	 *
	 * @return
	 */
	public String toPlain() {
		return PlainTextComponentSerializer.plainText().serialize(this.buildToAdventure(null, null));
	}

	/**
	 *
	 * @return
	 */
	public String toAdventureJson() {
		return GsonComponentSerializer.gson().serialize(this.buildToAdventure(null, null));
	}

	@Override
	public Component asComponent() {
		return this.toAdventure();
	}

	/**
	 * Return the component
	 *
	 * @return
	 */
	public Component toAdventure() {
		return this.buildToAdventure(null, null);
	}

	/**
	 * Return the component
	 *
	 * @param receiver
	 * @return
	 */
	public Component toAdventure(FoundationPlayer receiver) {
		return this.buildToAdventure(null, receiver);
	}

	/**
	 * Return the component
	 *
	 * @param sender
	 * @param receiver
	 * @return
	 */
	public Component toAdventure(FoundationPlayer sender, FoundationPlayer receiver) {
		return this.buildToAdventure(sender, receiver);
	}

	/*
	 * Convert into Adventure component
	 */
	private Component buildToAdventure(FoundationPlayer sender, FoundationPlayer receiver) {
		Component main = Component.empty();

		for (final ConditionalComponent part : this.components) {
			final Component component = part.build(receiver);

			if (component != null)
				main = main.append(component);
		}

		return builder == null ? main : builder.onBuild(sender, receiver, main);
	}

	// --------------------------------------------------------------------
	// Sending
	// --------------------------------------------------------------------

	/**
	 * Attempts to send the complete {@link SimpleComponent} to the given
	 * command senders. If they are players, we send them interactive elements.
	 * <p>
	 * If they are console, they receive a plain text message.
	 *
	 * @param receivers
	 */
	public final void send(final FoundationPlayer... receivers) {
		this.send(Arrays.asList(receivers));
	}

	/**
	 * Attempts to send the complete {@link SimpleComponent} to the given
	 * command senders. If they are players, we send them interactive elements.
	 * <p>
	 * If they are console, they receive a plain text message.
	 *
	 * @param receivers
	 */
	public void send(final Iterable<FoundationPlayer> receivers) {
		if (this.ignoreEmpty && this.isEmpty())
			return;

		for (final FoundationPlayer receiver : receivers)
			receiver.sendRawMessage(this.buildToAdventure(null, receiver));
	}

	/**
	 * Set if this component should ignore empty components? Defaults to false
	 *
	 * @param ignoreEmpty
	 * @return
	 */
	public SimpleComponent setIgnoreEmpty(final boolean ignoreEmpty) {
		this.ignoreEmpty = ignoreEmpty;

		return this;
	}

	/*
	 * Get the last component or throws an error if none found
	 */
	private ConditionalComponent getLastComponent() {
		ValidCore.checkBoolean(this.components.size() > 0, "No components found!");

		return this.components.get(this.components.size() - 1);
	}

	/*
	 * Helper method to modify the last component
	 */
	protected void modifyLastComponent(Function<Component, Component> editor) {
		final ConditionalComponent last = this.getLastComponent();

		last.setComponent(editor.apply(last.getComponent()));
	}

	/**
	 * @see org.mineacademy.fo.model.ConfigSerializable#serialize()
	 */
	@Override
	public SerializedMap serialize() {
		return SerializedMap.ofArray(
				"Components", this.components,
				"Ignore_Empty", this.ignoreEmpty);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.toAdventureJson();
	}

	// --------------------------------------------------------------------
	// Static
	// --------------------------------------------------------------------

	/**
	 * Create a new interactive chat component
	 * You can then build upon your text to add interactive elements
	 *
	 * @return
	 */
	public static SimpleComponent empty() {
		return new SimpleComponent(Arrays.asList(ConditionalComponent.fromPlain("")));
	}

	/**
	 * Replaces & color codes and MiniMessage tags in the message.
	 * Also replaces {prefix}, {plugin_name} and {plugin_version} with their respective values.
	 *
	 * @param message
	 * @return
	 */
	public static SimpleComponent fromMini(String message) {
		if (message == null || message.trim().isEmpty())
			return SimpleComponent.empty();

		// First, replace legacy & color codes
		final StringBuilder result = new StringBuilder();

		for (int i = 0; i < message.length(); i++) {
			if (i + 1 < message.length() && (message.charAt(i) == '&' || message.charAt(i) == '§')) {
				final String code = message.substring(i, i + 2);

				if (LEGACY_COLOR_MAP.containsKey(code)) {
					result.append(LEGACY_COLOR_MAP.get(code));
					i++;

					continue;
				}

				if (i + 7 < message.length() && message.charAt(i + 1) == '#' && message.substring(i + 2, i + 8).matches("[0-9a-fA-F]{6}")) {
					result.append("<#").append(message.substring(i + 2, i + 8)).append(">");
					i += 7;

					continue;
				}
			}

			result.append(message.charAt(i));
		}

		message = result.toString();
		message = escapeInvalidTags(message);

		Component mini;

		try {
			mini = MiniMessage.miniMessage().deserialize(message);

		} catch (final Throwable t) {
			Debugger.printStackTrace("Error parsing mini message tags in: " + message);

			RemainCore.sneaky(t);
			return null;
		}

		// if message ends with color code from the above map, add an empty component at the end with the same color
		if (!message.endsWith(" "))
			for (final String value : LEGACY_COLOR_MAP.values()) {
				if (message.endsWith(value)) {
					mini = mini.append(Component.text(" ").color(MiniMessage.miniMessage().deserialize(value).color()));

					break;
				}
			}

		return Variables.replace(fromAdventure(mini), null);
	}

	/**
	 * Create a new interactive chat component from a legacy text with & color codes
	 *
	 * @param message
	 * @return
	 */
	public static SimpleComponent fromAndCharacter(String message) {
		return fromAdventure(LegacyComponentSerializer.legacyAmpersand().deserialize(message));
	}

	/*
	 * Escapes invalid minimessage tags in the message.
	 */
	private static String escapeInvalidTags(String input) {
		final Matcher matcher = Pattern.compile("<[^>]*>").matcher(input);
		final StringBuffer buffer = new StringBuffer();

		while (matcher.find()) {
			String match = matcher.group(0);

			if (!MINIMESSAGE_PATTERN.matcher(match).matches())
				match = match.replace("<", "\\\\<").replace(">", "\\>");

			matcher.appendReplacement(buffer, match);
		}

		matcher.appendTail(buffer);
		return buffer.toString();
	}

	/**
	 * Create a new interactive chat component supporting § variables
	 *
	 * @param legacyText
	 * @return
	 */
	public static SimpleComponent fromSection(String legacyText) {
		return new SimpleComponent(Arrays.asList(ConditionalComponent.fromSection(legacyText)));
	}

	/**
	 * Create a new interactive chat component
	 *
	 * @param component
	 * @return
	 */
	public static SimpleComponent fromAdventure(Component component) {
		return new SimpleComponent(Arrays.asList(ConditionalComponent.fromComponent(component)));
	}

	/**
	 * Create a new interactive chat component from plain text
	 *
	 * @param plainText
	 * @return
	 */
	public static SimpleComponent fromPlain(String plainText) {
		return new SimpleComponent(Arrays.asList(ConditionalComponent.fromPlain(plainText)));
	}

	/**
	 * CCreate a new interactive chat component from json
	 *
	 * @param json
	 * @return
	 */
	public static SimpleComponent fromJson(String json) {
		return deserialize(SerializedMap.fromJson(json));
	}

	/**
	 * Create a new interactive chat component from children
	 *
	 * @param components
	 * @return
	 */
	public static SimpleComponent fromChildren(SimpleComponent... components) {
		final List<ConditionalComponent> children = new ArrayList<>();

		for (final SimpleComponent component : components)
			children.addAll(component.components);

		return new SimpleComponent(children);
	}

	/**
	 *
	 * @param map
	 * @return
	 */
	public static SimpleComponent deserialize(SerializedMap map) {
		final List<ConditionalComponent> components = map.getList("Components", ConditionalComponent.class);
		final boolean ignoreEmpty = map.getBoolean("Ignore_Empty");

		return new SimpleComponent(components).setIgnoreEmpty(ignoreEmpty);
	}

	// --------------------------------------------------------------------
	// Classes
	// --------------------------------------------------------------------

	@Setter
	@Getter
	@NoArgsConstructor(access = AccessLevel.PRIVATE)
	static final class ConditionalComponent implements ConfigSerializable {

		private Component component;
		private String viewPermission;
		private String viewCondition;

		@Override
		public SerializedMap serialize() {
			final SerializedMap map = new SerializedMap();

			map.put("Component", this.component);
			map.putIf("Permission", this.viewPermission);
			map.putIf("Condition", this.viewCondition);

			return map;
		}

		public static ConditionalComponent deserialize(final SerializedMap map) {
			final ConditionalComponent part = new ConditionalComponent();

			part.component = map.get("Component", Component.class);
			part.viewPermission = map.getString("Permission");
			part.viewCondition = map.getString("Condition");

			return part;
		}

		/*
		 * Build the component for the given receiver
		 */
		private Component build(FoundationPlayer receiver) {

			if (this.viewPermission != null && !this.viewPermission.isEmpty() && (receiver == null || !receiver.hasPermission(this.viewPermission)))
				return null;

			if (this.viewCondition != null && !this.viewCondition.isEmpty()) {
				if (receiver == null)
					return null;

				try {
					final Object result = JavaScriptExecutor.run(Variables.replace(this.viewCondition, receiver), receiver);

					if (result != null) {
						ValidCore.checkBoolean(result instanceof Boolean, "View condition must return Boolean not " + (result == null ? "null" : result.getClass()) + " for component: " + this);

						if (!((boolean) result))
							return null;
					}

				} catch (final FoScriptException ex) {
					CommonCore.logFramed(
							"Failed parsing view condition for component!",
							"",
							"The view condition must be a JavaScript code that returns a boolean!",
							"Component: " + this,
							"Line: " + ex.getErrorLine(),
							"Error: " + ex.getMessage());

					throw ex;
				}
			}

			return this.component;
		}

		@Override
		public String toString() {
			return this.serialize().toStringFormatted();
		}

		/**
		 * Create a new component from a legacy text
		 *
		 * @param text
		 * @return
		 */
		static ConditionalComponent fromSection(String text) {
			final ConditionalComponent part = new ConditionalComponent();

			part.component = LegacyComponentSerializer.legacySection().deserialize(text);

			return part;
		}

		/**
		 * Create a new component from a component
		 *
		 * @param component
		 * @return
		 */
		static ConditionalComponent fromComponent(Component component) {
			final ConditionalComponent part = new ConditionalComponent();

			part.component = component;

			return part;
		}

		/**
		 * Create a new component from plain text, fastest
		 *
		 * @param component
		 * @return
		 */
		static ConditionalComponent fromPlain(String plainText) {
			final ConditionalComponent part = new ConditionalComponent();

			part.component = Component.text(plainText);

			return part;
		}
	}

	public interface Builder {
		Component onBuild(FoundationPlayer sender, FoundationPlayer receiver, Component component);
	}
}
