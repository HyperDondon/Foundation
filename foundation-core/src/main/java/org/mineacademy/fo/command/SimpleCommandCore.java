package org.mineacademy.fo.command;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.mineacademy.fo.CommonCore;
import org.mineacademy.fo.MessengerCore;
import org.mineacademy.fo.ReflectionUtilCore;
import org.mineacademy.fo.TabUtil;
import org.mineacademy.fo.ValidCore;
import org.mineacademy.fo.collection.expiringmap.ExpiringMap;
import org.mineacademy.fo.exception.CommandException;
import org.mineacademy.fo.model.SimpleComponent;
import org.mineacademy.fo.model.SimpleTime;
import org.mineacademy.fo.model.Task;
import org.mineacademy.fo.platform.FoundationPlayer;
import org.mineacademy.fo.platform.Platform;
import org.mineacademy.fo.settings.SimpleLocalization;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * A simple command used to replace all Bukkit/Spigot command functionality
 * across any plugin that utilizes this.
 */
public abstract class SimpleCommandCore {

	/**
	 * Denotes an empty list used to disable tab-completion
	 */
	protected static final List<String> NO_COMPLETE = Collections.unmodifiableList(new ArrayList<>());

	/**
	 * You can set the cooldown time before executing the command again. This map
	 * stores the player uuid and his last execution of the command.
	 */
	private final ExpiringMap<FoundationPlayer, Long> cooldownMap = ExpiringMap.builder().expiration(30, TimeUnit.MINUTES).build();

	/**
	 * The command label, eg. boss for /boss
	 */
	private final String label;

	/**
	 * Command aliases
	 */
	private List<String> aliases = new ArrayList<>();

	/**
	 * The command usage
	 */
	private SimpleComponent usage = null;

	/**
	 * The command description
	 */
	private SimpleComponent description = null;

	/**
	 * Has this command been already registered?
	 */
	private boolean registered = false;

	/**
	 * The {@link CommonCore#getTellPrefix()} custom prefix only used for sending messages in {@link #onCommand()} method
	 * for this command, null to use the one in Common#getTellPrefix or empty to force no prefix.
	 */
	private SimpleComponent tellPrefix = null;

	/**
	 * Minimum arguments required to run this command
	 */
	@Getter
	private int minArguments = 0;

	/**
	 * Maximum arguments this command can have, or -1 for unlimited
	 */
	@Getter
	private int maxArguments = -1;

	/**
	 * The command cooldown before we can run this command again
	 */
	@Getter
	private int cooldownSeconds = 0;

	/**
	 * The permission for players to bypass this command's cooldown, if it is set
	 */
	@Getter
	private String cooldownBypassPermission = null;

	/**
	 * A custom message when the player attempts to run this command
	 * within {@link #cooldownSeconds}. By default we use the one found in
	 * {@link SimpleLocalization.Commands#COOLDOWN_WAIT}
	 * <p>
	 * TIP: Use {duration} to replace the remaining time till next run
	 */
	private SimpleComponent cooldownMessage = null;

	/**
	 * The permission to run this command. Set to null to always allow.
	 *
	 * Defaults to {@link #getDefaultPermission()}
	 */
	private String permission = null;

	/**
	 * The permission message to send when the player does not have the permission,
	 * defaults to SimpleLocalization.Commands.NO_PERMISSION
	 */
	private SimpleComponent permissionMessage = null;

	/**
	 * Should we automatically send usage message when the first argument
	 * equals to "help" or "?" ?
	 */
	private boolean autoHandleHelp = true;

	// ----------------------------------------------------------------------
	// Temporary variables
	// ----------------------------------------------------------------------

	/**
	 * The command sender, or null if does not exist
	 * <p>
	 * This variable is updated dynamically when the command is run with the
	 * last known sender
	 */
	protected FoundationPlayer sender;

	/**
	 * The arguments used when the command was last executed
	 * <p>
	 * This variable is updated dynamically when the command is run with the
	 * last known arguments
	 */
	protected String[] args;

	// ----------------------------------------------------------------------

	/**
	 * Create a new simple command with the given label.
	 * <p>
	 * Separate the label with | to split between label and aliases.
	 * Example: remove|r|rm will create a /remove command that can
	 * also be run by typing /r and /rm as its aliases.
	 *
	 * @param label
	 */
	protected SimpleCommandCore(final String label) {
		this(parseLabel0(label), parseAliases0(label));
	}

	/**
	 * Create a new simple command from the list. The first
	 * item in the list is the main label and the other ones are the aliases.
	 */
	protected SimpleCommandCore(final List<String> labels) {
		this(parseLabelList0(labels), labels.size() > 1 ? labels.subList(1, labels.size()) : null);
	}

	/**
	 * Create a new simple command
	 *
	 * @param label
	 * @param aliases
	 */
	protected SimpleCommandCore(final String label, final List<String> aliases) {
		Platform.checkCommandUse(this);

		this.label = label;

		if (aliases != null)
			this.aliases = aliases;

		// Set a default permission for this command
		this.permission = Platform.getPlugin().getName().toLowerCase() + ".command." + label;
	}

	/*
	 * Split the given label by | and get the first part, used as the main label
	 */
	private static String parseLabel0(final String label) {
		ValidCore.checkNotNull(label, "Label must not be null!");

		return label.split("(\\||\\/)")[0];
	}

	/*
	 * Split the given label by | and use the second and further parts as aliases
	 */
	private static List<String> parseAliases0(final String label) {
		final String[] aliases = label.split("(\\||\\/)");

		return aliases.length > 0 ? Arrays.asList(Arrays.copyOfRange(aliases, 1, aliases.length)) : new ArrayList<>();
	}

	/*
	 * Return the first index from the list or thrown an error if list empty
	 */
	private static String parseLabelList0(final List<String> labels) {
		ValidCore.checkBoolean(!labels.isEmpty(), "Command label must not be empty!");

		return labels.get(0);
	}

	// ----------------------------------------------------------------------
	// Registration
	// ----------------------------------------------------------------------

	/**
	 * Registers this command into Bukkit.
	 *
	 * Throws an error if the command is registered already.
	 */
	public final void register() {
		this.register(true);
	}

	/**
	 * Registers this command into Bukkit.
	 *
	 * Throws an error if the command is registered already.
	 *
	 * @param unregisterOldAliases If a command with the same label is already present, should
	 *                             we remove associated aliases with the old command? This solves a problem
	 *                             in ChatControl where unregistering /tell from the Essentials plugin would also
	 *                             unregister /t from Towny, which is undesired.
	 */
	public final void register(final boolean unregisterOldAliases) {
		this.register(true, unregisterOldAliases);
	}

	/**
	 * Registers this command into Bukkit.
	 *
	 * Throws an error if the command is registered already.
	 *
	 * @param unregisterOldCommand Unregister old command if exists with the same label?
	 * @param unregisterOldAliases If a command with the same label is already present, should
	 *                             we remove associated aliases with the old command? This solves a problem
	 *                             in ChatControl where unregistering /tell from the Essentials plugin would also
	 *                             unregister /t from Towny, which is undesired.
	 */
	public final void register(final boolean unregisterOldCommand, final boolean unregisterOldAliases) {
		ValidCore.checkBoolean(!(this instanceof SimpleSubCommandCore), "Sub commands cannot be registered!");
		ValidCore.checkBoolean(!this.registered, "The command /" + this.getLabel() + " has already been registered!");

		if (this.canRegister()) {
			Platform.registerCommand(this, unregisterOldCommand, unregisterOldAliases);

			this.registered = true;
		}
	}

	/**
	 * Removes the command from Bukkit.
	 * <p>
	 * Throws an error if the command is not registered.
	 */
	public final void unregister() {
		ValidCore.checkBoolean(!(this instanceof SimpleSubCommandCore), "Sub commands cannot be unregistered!");
		ValidCore.checkBoolean(this.registered, "The command /" + this.getLabel() + " is not registered!");

		Platform.unregisterCommand(this);
		this.registered = false;
	}

	/**
	 * Return true if this command can be registered through {@link #register()} methods.
	 * By default true.
	 *
	 * @return
	 */
	protected boolean canRegister() {
		return true;
	}

	// ----------------------------------------------------------------------
	// Execution
	// ----------------------------------------------------------------------

	/**
	 * Execute this command, updates the sender, label and args variables,
	 * checks permission and returns if the sender lacks it,
	 * checks minimum arguments and finally passes the command to the child class.
	 *
	 * @deprecated internal use only
	 */
	@Deprecated
	public final boolean delegateExecute(final FoundationPlayer sender, final String label, final String[] args) {

		if (!Platform.getPlugin().isEnabled()) {
			sender.sendMessage(SimpleLocalization.Commands.CANNOT_USE_WHILE_NULL.replaceBracket("state", SimpleLocalization.Commands.DISABLED));

			return true;
		}

		// Set variables to re-use later
		this.sender = sender;
		this.args = args;

		try {
			// Check if sender has the proper permission
			if (this.getPermission() != null)
				this.checkPerm(this.getPermission());

			// Check if we can run this command in time
			if (this.cooldownSeconds > 0)
				this.handleCooldown();

			// Check for maximum arguments
			if (this.getMaxArguments() != -1 && args.length > this.getMaxArguments())
				this.returnInvalidArgs(this.joinArgs(this.getMaxArguments()));

			// Check for minimum required arguments and print help
			if (args.length < this.getMinArguments() || this.autoHandleHelp && args.length == 1 && ("help".equals(args[0]) || "?".equals(args[0]))) {

				final String[] legacyUsage = this.getMultilineUsageMessage();
				final SimpleComponent[] newUsage = this.getMultilineUsage();

				if (legacyUsage != null || newUsage != null)
					this.tellNoPrefix("<dark_gray>" + CommonCore.chatLineSmooth());

				if (this.getDescription() != null)
					this.tellNoPrefix(SimpleLocalization.Commands.LABEL_DESCRIPTION.replaceBracket("description", this.getDescription()));

				if (legacyUsage != null || newUsage != null || this.getUsage() != null) {
					this.tellNoPrefix(SimpleLocalization.Commands.LABEL_USAGE.replaceBracket("usage",
							SimpleComponent.fromPlain(this.getEffectiveCommand() + " ").append(CommonCore.getOrDefault(this.getUsage(), SimpleComponent.empty()))));

					if (legacyUsage != null || newUsage != null) {
						this.tellNoPrefix("<dark_gray>" + CommonCore.chatLineSmooth());

						if (legacyUsage != null)
							for (final String legacyLine : legacyUsage)
								this.replacePlaceholders(SimpleComponent.fromMini(this.colorizeUsage(legacyLine))).send(this.sender);

						else if (newUsage != null)
							for (final SimpleComponent newLine : newUsage)
								this.replacePlaceholders(newLine).send(this.sender);

						this.tellNoPrefix("<dark_gray>" + CommonCore.chatLineSmooth());
					}
				}

				return true;
			}

			this.onCommand();

		} catch (final InvalidCommandArgException ex) {
			this.tellError(SimpleLocalization.Commands.INVALID_ARGUMENT
					.replaceBracket("arguments", ex.getInvalidArgument())
					.replaceBracket("help_command", SimpleComponent
							.fromPlain(this.getEffectiveCommand() + " ?")
							.onHover("Click to execute.")
							.onClickRunCmd(this.getEffectiveCommand() + " ?")));

		} catch (final CommandException ex) {
			if (ex.getComponent() != null)
				this.tellError(ex.getComponent());

		} catch (final Throwable t) {
			this.tellError(SimpleLocalization.Commands.ERROR.replaceBracket("error", t.toString()));

			CommonCore.error(t, "Error executing " + this.getEffectiveCommand() + " " + String.join(" ", args));
		}

		return true;
	}

	/*
	 * Get the effective command with sublabel if applicable
	 */
	private String getEffectiveCommand() {
		return "/" + this.getLabel() + (this instanceof SimpleSubCommandCore ? " " + ((SimpleSubCommandCore) this).getSublabel() : "");
	}

	/**
	 * Check if the command cooldown is active and if the command
	 * is run within the given limit, we stop it and inform the player
	 */
	private void handleCooldown() {
		if (this.cooldownBypassPermission != null && this.hasPerm(this.cooldownBypassPermission))
			return;

		if (!this.isCooldownApplied(this.sender))
			return;

		final long lastRun = this.cooldownMap.getOrDefault(this.sender, 0L);
		final long difference = (System.currentTimeMillis() - lastRun) / 1000;

		// Check if the command was run earlier within the wait threshold
		if (lastRun != 0)
			this.checkBoolean(difference > this.cooldownSeconds, CommonCore.getOrDefault(this.cooldownMessage, SimpleLocalization.Commands.COOLDOWN_WAIT)
					.replaceBracket("duration", String.valueOf(this.cooldownSeconds - difference + 1)));

		// Update the last try with the current time
		this.cooldownMap.put(this.sender, System.currentTimeMillis());
	}

	/**
	 * Override this if you need to customize if the specific player should have the cooldown
	 * for this command.
	 *
	 * @param audience
	 * @return
	 */
	protected boolean isCooldownApplied(final FoundationPlayer audience) {
		return true;
	}

	/**
	 * Executed when the command is run. You can get the variables sender and args directly,
	 * and use convenience checks in the simple command class.
	 */
	protected abstract void onCommand();

	// ----------------------------------------------------------------------
	// Convenience checks
	//
	// Here is how they work: When you command is executed, simply call any
	// of these checks. If they fail, an error will be thrown inside of
	// which will be a message for the player.
	//
	// We catch that error and send the message to the player without any
	// harm or console errors to your plugin. That is intended and saves time.
	// ----------------------------------------------------------------------

	/**
	 * Checks if the current sender has the given permission
	 *
	 * @param permission
	 * @throws CommandException
	 */
	public final void checkPerm(@NonNull final String permission) throws CommandException {
		if (!this.hasPerm(permission))
			throw new CommandException(this.getPermissionMessage().replaceBracket("permission", permission));
	}

	/**
	 * Checks if the given sender has the given permission
	 *
	 * @param sender
	 * @param permission
	 * @throws CommandException
	 */
	public final void checkPerm(@NonNull final FoundationPlayer sender, @NonNull final String permission) throws CommandException {
		if (!this.hasPerm(sender, permission))
			throw new CommandException(this.getPermissionMessage().replaceBracket("permission", permission));
	}

	/**
	 * Checks if the given sender has the given permission
	 *
	 * @param minimumLength
	 * @param falseMessage
	 * @throws CommandException
	 */
	protected final void checkArgs(final int minimumLength, final String falseMessage) throws CommandException {
		this.checkArgs(minimumLength, SimpleComponent.fromMini(falseMessage));
	}

	/**
	 * Check if the command arguments are of the minimum length
	 *
	 * @param minimumLength
	 * @param falseMessage
	 * @throws CommandException
	 */
	protected final void checkArgs(final int minimumLength, final SimpleComponent falseMessage) throws CommandException {
		if (this.args.length < minimumLength)
			this.returnTell(falseMessage);
	}

	/**
	 * Convenience method for returning the command with the
	 * message for player if the condition does not meet
	 */
	/*public final void checkArgs(final boolean condition) {
		this.checkBoolean(condition, SimpleLocalization.Commands.INVALID_ARGUMENT);
	}*/

	/**
	 * Checks if the given boolean is true
	 *
	 * @param value
	 * @param falseMessage
	 * @throws CommandException
	 */
	public final void checkBoolean(final boolean value, final String falseMessage) throws CommandException {
		this.checkBoolean(value, SimpleComponent.fromMini(falseMessage));
	}

	/**
	 * Checks if the given boolean is true
	 *
	 * @param value
	 * @param falseMessage
	 * @throws CommandException
	 */
	public final void checkBoolean(final boolean value, final SimpleComponent falseMessage) throws CommandException {
		if (!value)
			this.returnTell(falseMessage);
	}

	/**
	 * Check if the given boolean is true or returns {@link #returnInvalidArgs()}
	 *
	 * @param value
	 *
	 * @throws CommandException
	 */
	/*protected final void checkUsage(final boolean value) throws CommandException {
		if (!value)
			this.returnInvalidArgs();
	}*/

	/**
	 * Checks if the given object is not null
	 *
	 * @param value
	 * @param messageIfNull
	 * @throws CommandException
	 */
	public final void checkNotNull(final Object value, final String messageIfNull) throws CommandException {
		this.checkNotNull(value, SimpleComponent.fromMini(messageIfNull));
	}

	/**
	 * Checks if the given object is not null
	 *
	 * @param value
	 * @param messageIfNull
	 * @throws CommandException
	 */
	public final void checkNotNull(final Object value, final SimpleComponent messageIfNull) throws CommandException {
		if (value == null)
			this.returnTell(messageIfNull);
	}

	/**
	 * Attempts to convert the given input (such as 1 hour) into
	 * a {@link SimpleTime} object
	 *
	 * @param raw
	 * @return
	 */
	protected final SimpleTime findTime(final String raw) {
		try {
			return SimpleTime.from(raw);

		} catch (final IllegalArgumentException ex) {
			this.returnTell(SimpleLocalization.Commands.INVALID_TIME.replaceBracket("input", raw));

			return null;
		}
	}

	/**
	 * Finds an enumeration of a certain type, if it fails it prints a false message to the player
	 * You can use the {enum} variable in the false message for the name parameter
	 *
	 * @param <T>
	 * @param enumType
	 * @param enumValue
	 * @return
	 * @throws CommandException
	 */
	protected final <T extends Enum<T>> T findEnum(final Class<T> enumType, final String enumValue) throws CommandException {
		return this.findEnum(enumType, enumValue, null);
	}

	/**
	 * Finds an enumeration of a certain type, if it fails it prints a false message to the player
	 * You can use the {enum} variable in the false message for the name parameter
	 *
	 * You can also use the condition to filter certain enums and act as if they did not existed
	 * if your function returns false for such
	 *
	 * @param <T>
	 * @param enumType
	 * @param enumValue
	 * @param condition

	 * @return
	 * @throws CommandException
	 */
	protected final <T extends Enum<T>> T findEnum(final Class<T> enumType, final String enumValue, final Function<T, Boolean> condition) throws CommandException {
		T found = null;

		try {
			found = ReflectionUtilCore.lookupEnum(enumType, enumValue);

			if (!condition.apply(found))
				found = null;

		} catch (final Throwable t) {
			// Not found, pass through below to error out
		}

		this.checkNotNull(found, SimpleLocalization.Commands.INVALID_ENUM
				.replaceBracket("type", enumType.getSimpleName().replaceAll("([a-z])([A-Z]+)", "$1 $2").toLowerCase())
				.replaceBracket("value", enumValue)
				.replaceBracket("available", CommonCore.join(enumType.getEnumConstants(), constant -> constant.name().toLowerCase())));

		return found;
	}

	/**
	 * A convenience method for parsing a number at the given args index
	 *
	 * @param index
	 * @param falseMessage
	 * @return
	 */
	protected final int findNumber(final int index, final String falseMessage) {
		return this.findNumber(index, SimpleComponent.fromMini(falseMessage));
	}

	/**
	 * A convenience method for parsing a number at the given args index
	 *
	 * @param index
	 * @param falseMessage
	 * @return
	 */
	protected final int findNumber(final int index, final SimpleComponent falseMessage) {
		return this.findNumber(Integer.class, index, falseMessage);
	}

	/**
	 * A convenience method for parsing a number that is between two bounds
	 * You can use {min} and {max} in the message to be automatically replaced
	 *
	 * @param index
	 * @param min
	 * @param max
	 * @param falseMessage
	 * @return
	 */
	protected final int findNumber(final int index, final int min, final int max, final String falseMessage) {
		return this.findNumber(index, min, max, SimpleComponent.fromMini(falseMessage));
	}

	/**
	 * A convenience method for parsing a number that is between two bounds
	 * You can use {min} and {max} in the message to be automatically replaced
	 *
	 * @param index
	 * @param min
	 * @param max
	 * @param falseMessage
	 * @return
	 */
	protected final int findNumber(final int index, final int min, final int max, final SimpleComponent falseMessage) {
		return this.findNumber(Integer.class, index, min, max, falseMessage);
	}

	/*
	 * A convenience method for parsing any number type that is between two bounds
	 * Number can be of any type, that supports method valueOf(String)
	 * You can use {min} and {max} in the message to be automatically replaced
	 */
	private final <T extends Number & Comparable<T>> T findNumber(final Class<T> numberType, final int index, final T min, final T max, SimpleComponent falseMessage) {
		falseMessage = falseMessage
				.replaceBracket("min", String.valueOf(min))
				.replaceBracket("max", String.valueOf(max));

		final T number = this.findNumber(numberType, index, falseMessage);
		this.checkBoolean(number.compareTo(min) >= 0 && number.compareTo(max) <= 0, falseMessage);

		return number;
	}

	/*
	 * A convenience method for parsing any number type at the given args index
	 * Number can be of any type, that supports method valueOf(String)
	 */
	private final <T extends Number> T findNumber(final Class<T> numberType, final int index, final SimpleComponent falseMessage) {
		this.checkBoolean(index < this.args.length, falseMessage);

		try {
			return (T) numberType.getMethod("valueOf", String.class).invoke(null, this.args[index]); // Method valueOf is part of all main Number sub classes, eg. Short, Integer, Double, etc.
		}

		catch (final IllegalAccessException | NoSuchMethodException e) {
			e.printStackTrace();

		} catch (final InvocationTargetException e) {

			// Print stack trace for all exceptions, except NumberFormatException
			// NumberFormatException is expected to happen, in this case we just want to display falseMessage without stack trace
			if (!(e.getCause() instanceof NumberFormatException))
				e.printStackTrace();
		}

		throw new CommandException(this.replacePlaceholders(falseMessage.replaceBracket("number", this.args[index])));
	}

	/**
	 * A convenience method for parsing a boolean at the given args index
	 *
	 * @param index
	 * @param invalidMessage
	 * @return
	 */
	protected final boolean findBoolean(final int index, final String invalidMessage) {
		return this.findBoolean(index, SimpleComponent.fromMini(invalidMessage));
	}

	/**
	 * A convenience method for parsing a boolean at the given args index
	 *
	 * @param index
	 * @param invalidMessage
	 * @return
	 */
	protected final boolean findBoolean(final int index, final SimpleComponent invalidMessage) {
		this.checkBoolean(index < this.args.length, invalidMessage);

		if (this.args[index].equalsIgnoreCase("true"))
			return true;

		else if (this.args[index].equalsIgnoreCase("false"))
			return false;

		this.returnTell(invalidMessage);
		return false;
	}

	// ----------------------------------------------------------------------
	// Other checks
	// ----------------------------------------------------------------------

	/**
	 * A convenience check for quickly determining if the sender has a given
	 * permission.
	 *
	 * TIP: For a more complete check use {@link #checkPerm(String)} that
	 * will automatically return your command if they lack the permission.
	 *
	 * @param permission
	 * @return
	 */
	protected final boolean hasPerm(final String permission) {
		return this.hasPerm(this.sender, permission);
	}

	/**
	 * A convenience check for quickly determining if the sender has a given
	 * permission.
	 *
	 * TIP: For a more complete check use {@link #checkPerm(String)} that
	 * will automatically return your command if they lack the permission.
	 *
	 * @param sender
	 * @param permission
	 * @return
	 */
	protected final boolean hasPerm(final FoundationPlayer sender, final String permission) {
		return permission == null || sender.hasPermission(permission);
	}

	// ----------------------------------------------------------------------
	// Messaging
	// ----------------------------------------------------------------------

	/**
	 * Sends a message to the player
	 *
	 * @see Replacer#replaceArray
	 *
	 * @param message
	 * @param replacements
	 */
	/*protected final void tellReplaced(final String message, final Object... replacements) {
		this.tell(Replacer.replaceArray(message, replacements));
	}*/

	/**
	 * Sends a interactive chat component to the sender, not replacing any special
	 * variables just executing the {@link SimpleComponentCore#send(CommandSender...)} method
	 * as a shortcut
	 *
	 * @param components
	 */
	/*protected final void tell(final List<SimpleComponentCore> components) {
		if (SimpleComponents != null)
			this.tell(SimpleComponents.toArray(new SimpleComponentCore[components.size()]));
	}*/

	/**
	 * Sends a interactive chat component to the sender, not replacing any special
	 * variables just executing the {@link SimpleComponentCore#send(CommandSender...)} method
	 * as a shortcut
	 *
	 * @param components
	 */
	/*protected final void tell(final SimpleComponentCore... components) {
		if (SimpleComponents != null)
			for (final SimpleComponentCore component : components)
				component.send(this.sender);
	}*/

	/**
	 * Send a list of messages to the player
	 *
	 * @param messages
	 */
	/*protected final void tell(final Collection<String> messages) {
		if (messages != null)
			this.tell(messages.toArray(new String[messages.size()]));
	}*/

	/**
	 * Sends a multiline message to the player without plugin's prefix.
	 *
	 * @param messages
	 */
	/*protected final void tellNoPrefix(final Collection<String> messages) {
		this.tellNoPrefix(messages.toArray(new String[messages.size()]));
	}*/

	/**
	 * Sends a message to the player without plugin's prefix.
	 *
	 * @param message
	 */
	protected final void tellNoPrefix(final String message) {
		this.tellNoPrefix(SimpleComponent.fromMini(message));
	}

	/**
	 * Sends a message to the player without plugin's prefix.
	 *
	 * @param messages
	 */
	protected final void tellNoPrefix(final SimpleComponent message) {
		final SimpleComponent oldLocalPrefix = this.tellPrefix;

		this.tellPrefix = SimpleComponent.empty();

		this.tell(message);

		this.tellPrefix = oldLocalPrefix;
	}

	/**
	 * Sends a multiline message to the player, avoiding prefix if 3 lines or more
	 *
	 * @param messages
	 */
	/*protected final void tell(String... messages) {

		if (messages == null)
			return;

		final String oldTellPrefix = CommonCore.getTellPrefix();

		if (this.tellPrefix != null)
			CommonCore.setTellPrefix(this.tellPrefix);

		try {
			messages = this.replacePlaceholders(messages);

			if (messages.length > 2)
				CommonCore.tellNoPrefix(this.sender, messages);
			else
				CommonCore.tell(this.sender, messages);

		} finally {
			CommonCore.setTellPrefix(oldTellPrefix);
		}
	}*/

	/**
	 * Sends a message to the player
	 *
	 * @param message
	 */
	protected final void tell(String message) {
		this.tell(SimpleComponent.fromMini(message));
	}

	/**
	 * Sends a message to the player
	 *
	 * @param component
	 */
	protected final void tell(SimpleComponent component) {
		if (component == null)
			return;

		component = this.replacePlaceholders(component);
		this.sender.sendMessage(this.tellPrefix != null ? this.tellPrefix.append(component) : component);
	}

	/**
	 * Sends a success message to the player
	 *
	 * @param component
	 */
	protected final void tellSuccess(String component) {
		this.tellSuccess(SimpleComponent.fromMini(component));
	}

	/**
	 * Sends a success message to the player
	 *
	 * @param component
	 */
	protected final void tellSuccess(SimpleComponent component) {
		if (component != null)
			MessengerCore.success(this.sender, this.replacePlaceholders(component));
	}

	/**
	 * Sends an info message to the player
	 *
	 * @param message
	 */
	protected final void tellInfo(String message) {
		this.tellInfo(SimpleComponent.fromMini(message));
	}

	/**
	 * Sends an info message to the player
	 *
	 * @param message
	 */
	protected final void tellInfo(SimpleComponent message) {
		if (message != null)
			MessengerCore.info(this.sender, this.replacePlaceholders(message));
	}

	/**
	 * Sends a warning message to the player
	 *
	 * @param message
	 */
	protected final void tellWarn(String message) {
		this.tellWarn(SimpleComponent.fromMini(message));
	}

	/**
	 * Sends a warning message to the player
	 *
	 * @param message
	 */
	protected final void tellWarn(SimpleComponent message) {
		if (message != null)
			MessengerCore.warn(this.sender, this.replacePlaceholders(message));
	}

	/**
	 * Sends an error message to the player
	 *
	 * @param message
	 */
	protected final void tellError(String message) {
		this.tellError(SimpleComponent.fromMini(message));
	}

	/**
	 * Sends an error message to the player
	 *
	 * @param message
	 */
	protected final void tellError(SimpleComponent message) {
		if (message != null)
			MessengerCore.error(this.sender, this.replacePlaceholders(message));
	}

	/**
	 * Sends a question-prefixed message to the player
	 *
	 * @param message
	 */
	protected final void tellQuestion(String message) {
		this.tellQuestion(SimpleComponent.fromMini(message));
	}

	/**
	 * Sends a question-prefixed message to the player
	 *
	 * @param message
	 */
	protected final void tellQuestion(SimpleComponent message) {
		if (message != null)
			MessengerCore.question(this.sender, this.replacePlaceholders(message));
	}

	/**
	 * Convenience method for returning the invalid arguments message for the player.
	 */
	protected final void returnInvalidArgs(String invalidArgs) {
		throw new InvalidCommandArgException(invalidArgs);
	}

	/**
	 * Sends a message to the player and throws a message error, preventing further execution
	 *
	 * @param messages
	 * @throws CommandException
	 */
	/*protected final void returnTell(final Collection<String> messages) throws CommandException {
		this.returnTell(messages.toArray(new String[messages.size()]));
	}*/

	/**
	 * Sends a message to the player and throws a message error, preventing further execution
	 *
	 * @param messages
	 * @throws CommandException
	 */
	public final void returnTell(final String... messages) throws CommandException {
		SimpleComponent component = SimpleComponent.empty();

		for (int i = 0; i < messages.length; i++) {
			final String message = messages[i];

			component = component.append(SimpleComponent.fromMini(message));

			if (i + 1 < messages.length)
				component = component.appendNewLine();
		}

		this.returnTell(component);
	}

	/**
	 * Sends a message to the player and throws a message error, preventing further execution
	 *
	 * @param component
	 * @throws CommandException
	 */
	public final void returnTell(final SimpleComponent component) throws CommandException {
		throw new CommandException(this.replacePlaceholders(component));
	}

	/**
	 * Sends a message to the player and throws a message error, preventing further execution
	 *
	 * @param messages
	 * @throws CommandException
	 */
	/*public final void returnTell(final String... messages) throws CommandException {
		throw new CommandException(this.replacePlaceholders(messages));
	}*/

	/**
	 * Ho ho ho, returns the command usage to the sender
	 *
	 * @throws InvalidCommandArgException
	 */
	/*public final void returnUsage() throws InvalidCommandArgException {
		throw new InvalidCommandArgException();
	}*/

	// ----------------------------------------------------------------------
	// Placeholder
	// ----------------------------------------------------------------------

	/**
	 * Replaces placeholders in all messages
	 * To change them override {@link #replacePlaceholders(String)}
	 *
	 * @param messages
	 * @return
	 */
	/*public final String[] replacePlaceholders(final String[] messages) {
		for (int i = 0; i < messages.length; i++)
			messages[i] = this.replacePlaceholders(messages[i]);

		return messages;
	}

	// TODO get rid of
	protected String replacePlaceholders(String legacy) {
		return RemainCore.convertAdventureToLegacy(replacePlaceholders(RemainCore.convertLegacyToAdventure(legacy)));
	}*/

	/**
	 * Replaces placeholders in the message
	 *
	 * @param component
	 * @return
	 */
	protected SimpleComponent replacePlaceholders(SimpleComponent component) {
		component = this.replaceBasicPlaceholders0(component);

		// Replace {X} with arguments
		for (int i = 0; i < this.args.length; i++)
			component = component.replaceBracket(String.valueOf(i), CommonCore.getOrEmpty(this.args[i]));

		return component;
	}

	// TODO get rid of
	/*private String replaceBasicPlaceholders0(String component) {
		return RemainCore.convertAdventureToLegacy(this.replaceBasicPlaceholders0(RemainCore.convertLegacyToAdventure(SimpleComponent)));
	}*/

	/**
	 * Internal method for replacing label and sublabel variables
	 *
	 * @param component
	 * @return
	 */
	private SimpleComponent replaceBasicPlaceholders0(SimpleComponent component) {

		component = component.replaceBracket("plugin_name", Platform.getPlugin().getName());
		component = component.replaceBracket("plugin_version", Platform.getPlugin().getVersion());
		component = component.replaceBracket("label", this.label);
		component = component.replaceBracket("sublabel", this instanceof SimpleSubCommandCore ? ((SimpleSubCommandCore) this).getSublabel() : this.args != null && this.args.length > 0 ? this.args[0] : this.label);

		return component;
	}

	/**
	 * Utility method to safely update the args, increasing them if the position is too high
	 * <p>
	 * Used in placeholders
	 *
	 * @param position
	 * @param value
	 */
	/*protected final void setArg(final int position, final String value) {
		if (this.args.length <= position)
			this.args = Arrays.copyOf(this.args, position + 1);

		this.args[position] = value;
	}*/

	/**
	 * Copies and returns the arguments {@link #args} from the given range
	 * to their end
	 *
	 * @param from
	 * @return
	 */
	protected final String[] rangeArgs(final int from) {
		return this.rangeArgs(from, this.args.length);
	}

	/**
	 * Copies and returns the arguments {@link #args} from the given range
	 * to the given end
	 *
	 * @param from
	 * @param to
	 * @return
	 */
	protected final String[] rangeArgs(final int from, final int to) {
		return Arrays.copyOfRange(this.args, from, to);
	}

	/**
	 * Copies and returns the arguments {@link #args} from the given range
	 * to their end joined by spaces
	 *
	 * @param from
	 * @return
	 */
	protected final String joinArgs(final int from) {
		return this.joinArgs(from, this.args.length);
	}

	/**
	 * Copies and returns the arguments {@link #args} from the given range
	 * to the given end joined by spaces
	 *
	 * @param from
	 * @param to
	 * @return
	 */
	protected final String joinArgs(final int from, final int to) {
		String message = "";

		for (int i = from; i < this.args.length && i < to; i++)
			message += this.args[i] + (i + 1 == this.args.length ? "" : " ");

		return message;
	}

	// ----------------------------------------------------------------------
	// Tab completion
	// ----------------------------------------------------------------------

	/**
	 * Show tab completion suggestions when the given sender
	 * writes the command with the given arguments
	 * <p>
	 * Tab completion is only shown if the sender has {@link #getPermission()}
	 *
	 * @param sender
	 * @param label
	 * @param args
	 *
	 * @deprecated internal use only
	 * @return
	 */
	@Deprecated
	public final List<String> delegateTabComplete(final FoundationPlayer sender, final String label, final String[] args) {
		this.sender = sender;
		this.args = args;

		if (this.hasPerm(this.getPermission())) {
			final List<String> suggestions = this.tabComplete();

			return suggestions == null ? NO_COMPLETE : suggestions;
		}

		return NO_COMPLETE;
	}

	/**
	 * Override this method to support tab completing in your command.
	 * <p>
	 * You can then use "sender", "label" or "args" fields from {@link SimpleCommandCore}
	 * class normally and return a list of tab completion suggestions.
	 * <p>
	 * We already check for {@link #getPermission()} and only call this method if the
	 * sender has it.
	 * <p>
	 * TIP: Use {@link #completeLastWord(Iterable)} and {@link #getLastArg()} methods
	 * in {@link SimpleCommandCore} for your convenience
	 *
	 * @return the list of suggestions to complete, or null to complete player names automatically
	 */
	protected List<String> tabComplete() {
		return null;
	}

	/**
	 * Convenience method for completing all player names that the sender can see
	 * and that are not vanished
	 * <p>
	 * TIP: You can simply return null for the same behaviour
	 *
	 * @return
	 */
	public List<String> completeLastWordPlayerNames() {
		return TabUtil.complete(this.getLastArg(), CommonCore.convert(Platform.getOnlinePlayers(), FoundationPlayer::getName));
	}

	/**
	 * Convenience method for automatically completing the last word
	 * with the given suggestions. We sort them and only select ones
	 * that the last word starts with.
	 *
	 * @param <T>
	 * @param suggestions
	 * @return
	 */
	@SafeVarargs
	public final <T> List<String> completeLastWord(final T... suggestions) {
		return TabUtil.complete(this.getLastArg(), suggestions);
	}

	/**
	 * Convenience method for automatically completing the last word
	 * with the given suggestions. We sort them and only select ones
	 * that the last word starts with.
	 *
	 * @param <T>
	 * @param suggestions
	 * @return
	 */
	public final <T> List<String> completeLastWord(final Iterable<T> suggestions) {
		final List<T> list = new ArrayList<>();

		for (final T suggestion : suggestions)
			list.add(suggestion);

		return TabUtil.complete(this.getLastArg(), list.toArray());
	}

	/**
	 * Convenience method for automatically completing the last word
	 * with the given suggestions converting them to a string. We sort them and only select ones
	 * that the last word starts with.
	 *
	 * @param <T>
	 * @param suggestions
	 * @param toString
	 * @return
	 */
	protected final <T> List<String> completeLastWord(final Iterable<T> suggestions, final Function<T, String> toString) {
		final List<String> list = new ArrayList<>();

		for (final T suggestion : suggestions)
			list.add(toString.apply(suggestion));

		return TabUtil.complete(this.getLastArg(), list.toArray());
	}

	/*
	 * Convenience method for returning the last word in arguments
	 *
	 * @return
	 */
	private final String getLastArg() {
		return this.args.length > 0 ? this.args[this.args.length - 1] : "";
	}

	// ----------------------------------------------------------------------
	// Temporary variables and safety
	// ----------------------------------------------------------------------

	/**
	 * Sets a custom prefix used in tell messages for this command.
	 * This overrides {@link CommonCore#getTellPrefix()} however won't work if
	 * {@link #addTellPrefix} is disabled
	 *
	 * @param tellPrefix
	 */
	protected final void setTellPrefix(final String tellPrefix) {
		this.setTellPrefix(SimpleComponent.fromMini(tellPrefix));
	}

	/**
	 * Sets a custom prefix used in tell messages for this command.
	 * This overrides {@link CommonCore#getTellPrefix()} however won't work if
	 * {@link #addTellPrefix} is disabled
	 *
	 * @param tellPrefix
	 */
	protected final void setTellPrefix(final SimpleComponent tellPrefix) {
		this.tellPrefix = tellPrefix;
	}

	/**
	 * Shortcut method for setting the min-max arguments range
	 * to automatically perform command argument validation.
	 *
	 * @param min
	 * @param max
	 */
	protected final void setValidArguments(int min, int max) {
		this.setMinArguments(min);
		this.setMaxArguments(max);
	}

	/**
	 * Sets the minimum number of arguments to run this command
	 *
	 * @param minArguments
	 */
	protected final void setMinArguments(final int minArguments) {
		ValidCore.checkBoolean(minArguments >= 0, "Minimum arguments must be 0 or greater");

		this.minArguments = minArguments;
	}

	/**
	 * Sets the maximum number of arguments to run this command
	 *
	 * @param maxArguments
	 */
	protected final void setMaxArguments(final int maxArguments) {
		ValidCore.checkBoolean(maxArguments >= 0, "Maximum arguments must be 0 or greater");
		ValidCore.checkBoolean(maxArguments >= this.minArguments, "Maximum arguments must be >= minimum arguments, got " + maxArguments + " < " + this.minArguments + " for " + this);

		this.maxArguments = maxArguments;
	}

	/**
	 * Set the time before the same player can execute this command again
	 *
	 * @param cooldown
	 * @param unit
	 */
	protected final void setCooldown(final int cooldown, final TimeUnit unit) {
		ValidCore.checkBoolean(cooldown >= 0, "Cooldown must be >= 0 for /" + this.getLabel());

		this.cooldownSeconds = (int) unit.toSeconds(cooldown);
	}

	/**
	 * Set the permission to bypass the cooldown, only works if the {@link #setCooldown(int, TimeUnit)} is set
	 *
	 * @param cooldownBypassPermission
	 */
	protected final void setCooldownBypassPermission(final String cooldownBypassPermission) {
		this.cooldownBypassPermission = cooldownBypassPermission;
	}

	/**
	 * Set the cooldown message for this command
	 *
	 * @return
	 */
	protected final SimpleComponent getCooldownMessage() {
		return cooldownMessage;
	}

	/**
	 * Set a custom cooldown message, by default we use the one found in {@link SimpleLocalization.Commands#COOLDOWN_WAIT}
	 * <p>
	 * Use {duration} to dynamically replace the remaining time
	 *
	 * @param cooldownMessage
	 */
	protected final void setCooldownMessage(final String cooldownMessage) {
		this.cooldownMessage = SimpleComponent.fromMini(cooldownMessage);
	}

	/**
	 * Set a custom cooldown message, by default we use the one found in {@link SimpleLocalization.Commands#COOLDOWN_WAIT}
	 * <p>
	 * Use {duration} to dynamically replace the remaining time
	 *
	 * @param cooldownMessage
	 */
	protected final void setCooldownMessage(final SimpleComponent cooldownMessage) {
		this.cooldownMessage = cooldownMessage;
	}

	/**
	 * Get the permission for this command, either the one you set or our from Localization
	 */
	public final SimpleComponent getPermissionMessage() {
		return CommonCore.getOrDefault(this.permissionMessage, SimpleLocalization.NO_PERMISSION);
	}

	/**
	 * Set the permission message
	 *
	 * @param permissionMessage
	 */
	protected final void setPermissionMessage(SimpleComponent permissionMessage) {
		this.permissionMessage = permissionMessage;
	}

	/**
	 * By default we check if the player has the permission you set in setPermission.
	 * Defaults to {plugin}.command.{label}. Variables in permission are not supported.
	 *
	 * @return
	 */
	public final String getPermission() {
		return this.permission;
	}

	/**
	 * Sets the permission required for this command to run. If you set the
	 * permission to null we will not require any permission (unsafe).
	 *
	 * @param permission
	 */
	protected final void setPermission(final String permission) {
		this.permission = permission;
	}

	/**
	 * Get the sender of this command
	 *
	 * @return
	 */
	public final FoundationPlayer getSender() {
		ValidCore.checkNotNull(this.sender, "Sender cannot be null");

		return this.sender;
	}

	/**
	 * Get aliases for this command
	 */
	public final List<String> getAliases() {
		return this.aliases;
	}

	/**
	 * Set the command aliases
	 *
	 * @param aliases
	 */
	protected final void setAliases(List<String> aliases) {
		this.aliases = aliases;
	}

	/**
	 * Get description for this command
	 */
	public final SimpleComponent getDescription() {
		return this.description;
	}

	/**
	 * Get the usage message of this command
	 */
	public final SimpleComponent getUsage() {
		return this.usage;
	}

	/**
	 * Get a custom multilined usage message to be shown instead of the one line one.
	 * Defaults to null.
	 *
	 * @return the multiline custom usage message, or null
	 */
	protected SimpleComponent[] getMultilineUsage() {
		return null;
	}

	/**
	 * Get a custom usage message to be shown instead of the one line one. This is
	 * prioritized over {@link #getMultilineUsage()}. Defaults to null.
	 *
	 * @deprecated use {@link #getMultilineUsage()}
	 * @return
	 */
	@Deprecated
	protected String[] getMultilineUsageMessage() {
		return null;
	}

	/**
	 * Get the most recent label for this command
	 */
	public final String getLabel() {
		return this.label;
	}

	/**
	 * Set whether we automatically show usage params in {@link #getMinArguments()}
	 * and when the first arg == "help" or "?"
	 * <p>
	 * True by default
	 *
	 * @param autoHandleHelp
	 */
	protected final void setAutoHandleHelp(final boolean autoHandleHelp) {
		this.autoHandleHelp = autoHandleHelp;
	}

	/**
	 * Set the command usage
	 *
	 * @param usage
	 */
	protected final void setUsage(String usage) {
		this.usage = usage == null || usage.isEmpty() ? null : SimpleComponent.fromMini(this.colorizeUsage(usage));
	}

	/**
	 * Replace <> and [] with appropriate color codes, you can return the given string
	 * without modification to disable this functionality.
	 *
	 * @param usage
	 * @return
	 */
	final String colorizeUsage(String usage) {
		return usage
				.replace("<", "§6<")
				.replace(">", "§6>§f")
				.replace("[", "§2[")
				.replace("]", "§2]§f")
				.replace("-", "§7-");
	}

	/**
	 * Set the command usage
	 *
	 * @param usage
	 */
	protected final void setUsage(SimpleComponent usage) {
		this.usage = usage == null || usage.isEmpty() ? null : usage;
	}

	/**
	 * Set the command label
	 *
	 * @param description
	 */
	protected final void setDescription(String description) {
		this.description = description == null || description.isEmpty() ? null : SimpleComponent.fromMini(description);
	}

	/**
	 * Set the command description
	 *
	 * @param description
	 */
	protected final void setDescription(SimpleComponent description) {
		this.description = description == null || description.isEmpty() ? null : description;
	}

	/**
	 * Get the command arguments
	 *
	 * @return
	 */
	public final String[] getArgs() {
		return args;
	}

	// ----------------------------------------------------------------------
	// Scheduling
	// ----------------------------------------------------------------------

	/**
	 * Runs the given task later, this supports checkX methods
	 * where we handle sending messages to player automatically
	 *
	 * @param runnable
	 * @return
	 */
	public final Task runLater(final Runnable runnable) {
		return runLater(0, runnable);
	}

	/**
	 * Runs the given task later, this supports checkX methods
	 * where we handle sending messages to player automatically
	 *
	 * @param delayTicks
	 * @param runnable
	 * @return
	 */
	protected final Task runLater(final int delayTicks, final Runnable runnable) {
		return Platform.runTask(delayTicks, () -> this.delegateTask(runnable));
	}

	/**
	 * Runs the given task asynchronously, this supports checkX methods
	 * where we handle sending messages to player automatically
	 *
	 * @param runnable
	 * @return
	 */
	public final Task runAsync(final Runnable runnable) {
		return this.runAsync(0, runnable);
	}

	/**
	 * Runs the given task asynchronously, this supports checkX methods
	 * where we handle sending messages to player automatically
	 *
	 * @param delayTicks
	 * @param runnable
	 * @return
	 */
	protected final Task runAsync(final int delayTicks, final Runnable runnable) {
		return Platform.runTaskAsync(delayTicks, () -> this.delegateTask(runnable));
	}

	/*
	 * A helper method to catch command-related exceptions from runnables
	 */
	private void delegateTask(final Runnable runnable) {
		try {
			runnable.run();

		} catch (final CommandException ex) {
			if (ex.getComponent() != null)
				MessengerCore.error(this.sender, ex.getComponent());

		} catch (final Throwable t) {
			MessengerCore.error(this.sender, SimpleLocalization.Commands.ERROR.replaceBracket("error", t.toString()));

			throw t;
		}
	}

	@Override
	public boolean equals(final Object obj) {
		return obj instanceof SimpleCommandCore ? ((SimpleCommandCore) obj).getLabel().equals(this.getLabel()) && ((SimpleCommandCore) obj).getAliases().equals(this.getAliases()) : false;
	}

	@Override
	public String toString() {
		return "Command{/" + this.label + "}";
	}

	/**
	 * Thrown when a command has invalid argument
	 */
	@Getter
	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	private final class InvalidCommandArgException extends CommandException {
		private static final long serialVersionUID = 1L;
		private final String invalidArgument;
	}
}