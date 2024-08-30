package org.mineacademy.fo.command;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.mineacademy.fo.CommonCore;
import org.mineacademy.fo.MathUtilCore;
import org.mineacademy.fo.MinecraftVersion;
import org.mineacademy.fo.MinecraftVersion.V;
import org.mineacademy.fo.ReflectionUtilCore;
import org.mineacademy.fo.ValidCore;
import org.mineacademy.fo.collection.StrictList;
import org.mineacademy.fo.model.ChatPaginator;
import org.mineacademy.fo.model.SimpleComponent;
import org.mineacademy.fo.model.Variables;
import org.mineacademy.fo.platform.FoundationPlayer;
import org.mineacademy.fo.platform.Platform;
import org.mineacademy.fo.remain.CompChatColor;
import org.mineacademy.fo.settings.SimpleLocalization;
import org.mineacademy.fo.settings.SimpleSettings;

import lombok.Getter;

/**
 * A command group contains a set of different subcommands
 * associated with the main command, for example: /arena join, /arena leave etc.
 */
public abstract class SimpleCommandGroup {

	/**
	 * The list of sub-commands belonging to this command tree, for example
	 * the /boss command has subcommands /boss region, /boss menu etc.
	 */
	private final StrictList<SimpleSubCommandCore> subcommands = new StrictList<>();

	/**
	 * The registered main command, if any
	 */
	private SimpleCommandCore mainCommand;

	/**
	 * The label to execute subcommands in this group, example: for ChatControl it's /chatcontrol
	 */
	@Getter
	private String label;

	/**
	 * What other commands trigger this command group? Example: for ChatControl it's /chc and /chatc
	 */
	@Getter
	private List<String> aliases;

	/**
	 * The temporary sender that is currently about to see the command group, mostly used in
	 * compiling info messages such as in {@link #getNoParamsHeader()}
	 */
	@Getter
	protected FoundationPlayer sender;

	/**
	 * Create a new simple command group using {@link SimpleSettings#MAIN_COMMAND_ALIASES}
	 */
	protected SimpleCommandGroup() {
		this(findMainCommandAliases());
	}

	/*
	 * A helper method to aid developers implement this command properly.
	 */
	private static StrictList<String> findMainCommandAliases() {
		final StrictList<String> aliases = SimpleSettings.MAIN_COMMAND_ALIASES;

		ValidCore.checkBoolean(!aliases.isEmpty(), "Called SimpleCommandGroup with no args constructor which uses SimpleSettings' MAIN_COMMAND_ALIASES field WHICH WAS EMPTY."
				+ " To make this work, make a settings class extending SimpleSettings and write 'Command_Aliases: [/yourmaincommand]' key-value pair with a list of aliases to your settings.yml file.");

		return aliases;
	}

	/**
	 * Create a new simple command group with the given label and aliases bundled in a list
	 */
	protected SimpleCommandGroup(final StrictList<String> labelAndAliases) {
		this(labelAndAliases.get(0), (labelAndAliases.size() > 1 ? labelAndAliases.range(1) : new StrictList<String>()).getSource());
	}

	/**
	 * Create a new simple command group with the given label and aliases
	 */
	protected SimpleCommandGroup(final String label, final List<String> aliases) {
		this.label = label;
		this.aliases = aliases;
	}

	/**
	 * Create a new simple command group with the given label and aliases automatically
	 * separated by | or /
	 *
	 * Example: channel|ch will create a /channel command group that can also be called by using /ch
	 */
	protected SimpleCommandGroup(final String labelAndAliases) {
		final String[] split = labelAndAliases.split("(\\||\\/)");

		this.label = split[0];
		this.aliases = split.length > 0 ? Arrays.asList(Arrays.copyOfRange(split, 1, split.length)) : new ArrayList<>();
	}

	// ----------------------------------------------------------------------
	// Main functions
	// ----------------------------------------------------------------------

	/**
	 * Register this command group into Bukkit and start using it
	 */
	public final void register() {
		ValidCore.checkBoolean(!this.isRegistered(), "Command group already registered as: " + this.mainCommand);

		this.mainCommand = new MainCommand(this.label);

		if (this.aliases != null)
			this.mainCommand.setAliases(this.aliases);

		this.mainCommand.register();
		this.registerSubcommands();

		// Sort A-Z
		Collections.sort(this.subcommands.getSource(), Comparator.comparing(SimpleSubCommandCore::getSublabel));

		// Check for collision
		this.checkSubCommandAliasesCollision();
	}

	/*
	 * Enforce non-overlapping aliases for subcommands
	 */
	private void checkSubCommandAliasesCollision() {
		final List<String> aliases = new ArrayList<>();

		for (final SimpleSubCommandCore subCommand : this.subcommands)
			for (final String alias : subCommand.getSublabels()) {
				ValidCore.checkBoolean(!aliases.contains(alias), "Subcommand '/" + this.getLabel() + " " + subCommand.getSublabel() + "' has alias '" + alias + "' that is already in use by another subcommand!");

				aliases.add(alias);
			}
	}

	/**
	 * Remove this command group from Bukkit. Takes immediate changes in the game.
	 */
	public final void unregister() {
		ValidCore.checkBoolean(this.isRegistered(), "Main command not registered!");

		this.mainCommand.unregister();
		this.mainCommand = null;
	}

	/**
	 * Has the command group been registered yet?
	 *
	 * @return
	 */
	public final boolean isRegistered() {
		return this.mainCommand != null;
	}

	/**
	 * Extending method to register subcommands, call
	 * {@link #registerSubcommand(SimpleSubCommandCore)} and {@link #registerHelpLine(String...)}
	 * there for your command group.
	 */
	protected abstract void registerSubcommands();

	/**
	 * Registers a new subcommand for this group
	 *
	 * @param command
	 */
	protected final void registerSubcommand(final SimpleSubCommandCore command) {
		ValidCore.checkNotNull(this.mainCommand, "Cannot add subcommands when main command is missing! Call register()");

		// Fixes reloading issue where all subcommands are cleared
		if (this.subcommands.contains(command))
			this.subcommands.remove(command);

		this.subcommands.add(command);
	}

	/**
	 * Automatically registers all extending classes for the given parent class into this command group.
	 * We automatically ignore abstract classes for you. ENSURE TO MAKE YOUR CHILDREN CLASSES FINAL.
	 *
	 * @param parentClass
	 */
	protected final void registerSubcommand(final Class<? extends SimpleSubCommandCore> parentClass) {
		for (final Class<? extends SimpleSubCommandCore> clazz : ReflectionUtilCore.getClasses(Platform.getPlugin().getFile(), parentClass)) {
			if (Modifier.isAbstract(clazz.getModifiers()))
				continue;

			ValidCore.checkBoolean(Modifier.isFinal(clazz.getModifiers()), "Make child of " + parentClass.getSimpleName() + " class " + clazz.getSimpleName() + " final to auto register it!");
			this.registerSubcommand(ReflectionUtilCore.instantiate(clazz));
		}
	}

	// ----------------------------------------------------------------------
	// Setters
	// ----------------------------------------------------------------------

	/**
	 * Updates the command label, only works if the command is not registered
	 *
	 * @param label the label to set
	 */
	public void setLabel(final String label) {
		ValidCore.checkBoolean(!this.isRegistered(), "Cannot use setLabel(" + label + ") for already registered command /" + this.getLabel());

		this.label = label;
	}

	/**
	 * Updates the command aliases, only works if the command is not registered
	 *
	 * @param aliases the aliases to set
	 */
	public void setAliases(final List<String> aliases) {
		ValidCore.checkBoolean(!this.isRegistered(), "Cannot use setAliases(" + aliases + ") for already registered command /" + this.getLabel());

		this.aliases = aliases;
	}

	// ----------------------------------------------------------------------
	// Functions
	// ----------------------------------------------------------------------

	/**
	 * Return the message displayed when no parameter is given, by
	 * default we give credits
	 * <p>
	 * If you specify "author" in your plugin.yml we display author information
	 * If you override {@link SimplePlugin#getFoundedYear()} we display copyright
	 *
	 * @param sender the command sender that requested this to be shown to him
	 *               may be null
	 * @return
	 */
	protected List<SimpleComponent> getNoParamsHeader() {
		final int foundedYear = Platform.getPlugin().getFoundedYear();
		final int yearNow = Calendar.getInstance().get(Calendar.YEAR);

		final List<SimpleComponent> messages = new ArrayList<>();

		messages.add(SimpleComponent.fromAndCharacter("&8" + CommonCore.chatLineSmooth()));
		messages.add(SimpleComponent.fromMini("   " + this.getHeaderPrefix() + Platform.getPlugin().getName() + "&r" + this.getTrademark() + " &7" + Platform.getPlugin().getVersion()));
		messages.add(SimpleComponent.newLine());

		final String authors = Platform.getPlugin().getAuthors();

		if (!authors.isEmpty())
			messages.add(SimpleComponent.fromAndCharacter("   &7").append(SimpleLocalization.Commands.LABEL_AUTHORS).appendMini(" &f" + authors + (foundedYear != -1 ? " &7\u00A9 " + foundedYear + (yearNow != foundedYear ? " - " + yearNow : "") : "")));

		final String credits = this.getCredits();

		if (credits != null && !credits.isEmpty())
			messages.add(SimpleComponent.fromMini("   " + credits));

		messages.add(SimpleComponent.fromAndCharacter("&8" + CommonCore.chatLineSmooth()));

		return messages;
	}

	/**
	 * Should we send command helps instead of no-param header?
	 *
	 * @return
	 */

	protected boolean sendHelpIfNoArgs() {
		return false;
	}

	// Return the TM symbol in case we have it for kangarko's plugins
	private String getTrademark() {
		return Platform.getPlugin().getAuthors().contains("kangarko") ? "&8\u2122" : "";
	}

	/**
	 * Get a part of the {@link #getNoParamsHeader()} typically showing
	 * your website where the user can find more information about this command
	 * or your plugin in general
	 *
	 * @return
	 */
	protected String getCredits() {
		return "&7Visit &fmineacademy.org &7for more information.";
	}

	/**
	 * Return which subcommands should trigger the automatic help
	 * menu that shows all subcommands sender has permission for.
	 * <p>
	 * Also see {@link #getHelpHeader()}
	 * <p>
	 * Default: help and ?
	 *
	 * @return
	 */
	protected List<String> getHelpLabel() {
		return Arrays.asList("help", "?");
	}

	/**
	 * Return the header messages used in /{label} help|? typically
	 * used to tell all available subcommands from this command group
	 *
	 * @return
	 */
	protected SimpleComponent[] getHelpHeader() {
		return new SimpleComponent[] {
				SimpleComponent.empty(),
				SimpleComponent.fromAndCharacter("&8" + CommonCore.chatLineSmooth()),
				SimpleComponent.fromMini(this.getHeaderPrefix() + "  " + Platform.getPlugin().getName() + "&r" + this.getTrademark() + " &7" + Platform.getPlugin().getVersion()),
				SimpleComponent.empty(),
				SimpleComponent.fromAndCharacter("&2  [] &f= ").append(SimpleLocalization.Commands.LABEL_OPTIONAL_ARGS),
				SimpleComponent.fromAndCharacter(this.getTheme() + "  <> &f= ").append(SimpleLocalization.Commands.LABEL_REQUIRED_ARGS),
				SimpleComponent.empty()
		};
	}

	/**
	 * Return the subcommand description when listing all commands using the "help" or "?" subcommand
	 * @return
	 */
	protected SimpleComponent getSubcommandDescription() {
		return SimpleLocalization.Commands.LABEL_SUBCOMMAND_DESCRIPTION;
	}

	/**
	 * Return the default color in the {@link #getHelpHeader()},
	 * GOLD + BOLD colors by default
	 *
	 * @return
	 */
	protected String getHeaderPrefix() {
		return this.getTheme() + "" + CompChatColor.BOLD;
	}

	/**
	 * Return the color used in some places of the automatically generated command help
	 *
	 * @return
	 */
	protected CompChatColor getTheme() {
		return CompChatColor.GOLD;
	}

	/**
	 * How many commands shall we display per page by default?
	 *
	 * Defaults to 12
	 */
	protected int getCommandsPerPage() {
		return 12;
	}

	// ----------------------------------------------------------------------
	// Execution
	// ----------------------------------------------------------------------

	/**
	 * The main command handling this command group
	 */
	public final class MainCommand extends SimpleCommandCore {

		/**
		 * Create new main command with the given label
		 *
		 * @param label
		 */
		private MainCommand(final String label) {
			super(label);

			// Let everyone view credits of this command when they run it without any sublabels
			this.setPermission(null);

			// We handle help ourselves
			this.setAutoHandleHelp(false);
		}

		/**
		 * Handle this command group, print a special message when no arguments are given,
		 * execute subcommands, handle help or ? argument and more.
		 */
		@Override
		protected void onCommand() {

			// Pass through sender to the command group itself
			SimpleCommandGroup.this.sender = this.sender;

			// Print a special message on no arguments
			if (this.args.length == 0) {
				if (SimpleCommandGroup.this.sendHelpIfNoArgs())
					this.tellSubcommandsHelp();
				else
					for (final SimpleComponent component : SimpleCommandGroup.this.getNoParamsHeader())
						this.tell(component);

				return;
			}

			final String argument = this.args[0];
			final SimpleSubCommandCore command = this.findSubcommand(argument);

			// Handle subcommands
			if (command != null) {
				final String oldSublabel = command.getSublabel();

				try {
					// Simulate our main label
					command.setSublabel(argument);

					// Run the command
					command.delegateExecute(this.sender, this.getCurrentLabel(), this.args.length == 1 ? new String[] {} : Arrays.copyOfRange(this.args, 1, this.args.length));

				} finally {
					// Restore old sublabel after the command has been run
					command.setSublabel(oldSublabel);
				}
			}

			// Handle help argument
			else if (!SimpleCommandGroup.this.getHelpLabel().isEmpty() && ValidCore.isInList(argument, SimpleCommandGroup.this.getHelpLabel()))
				this.tellSubcommandsHelp();
			else
				this.returnInvalidArgs();
		}

		/**
		 * Automatically tells all help for all subcommands
		 */
		protected void tellSubcommandsHelp() {

			// Building help can be heavy so do it off of the main thread
			Platform.runTaskAsync(0, () -> {
				
				if (SimpleCommandGroup.this.subcommands.isEmpty()) {
					this.tellError(SimpleLocalization.Commands.HEADER_NO_SUBCOMMANDS);

					return;
				}

				final List<SimpleComponent> lines = new ArrayList<>();

				final boolean atLeast17 = MinecraftVersion.atLeast(V.v1_7);
				final boolean atLeast113 = MinecraftVersion.atLeast(V.v1_13);

				for (final SimpleSubCommandCore subcommand : SimpleCommandGroup.this.subcommands) {
					
					if (subcommand.showInHelp() && this.hasPerm(subcommand.getPermission())) {

						// Simulate the sender to enable permission checks in getMultilineHelp for ex.
						subcommand.sender = this.sender;

						final SimpleComponent usage = subcommand.getUsage();
						final SimpleComponent desc = subcommand.getDescription() == null ? SimpleComponent.empty() : subcommand.getDescription();
						final SimpleComponent plainMessage = Variables.replace(SimpleCommandGroup.this.getSubcommandDescription(), null, CommonCore.newHashMap(
								"label", this.getLabel(),
								"sublabel", (atLeast17 ? "&n" : "") + subcommand.getSublabel() + (atLeast17 ? "&r" : ""),
								"usage", usage.toLegacy(),
								"description", !desc.isEmpty() && !atLeast17 ? desc.toLegacy() : "",
								"dash", !desc.isEmpty() && !atLeast17 ? "&e-" : ""));

						if (!desc.isEmpty() && atLeast17) {
							final String command = plainMessage.toPlain().substring(1);
							final List<SimpleComponent> hover = new ArrayList<>();

							hover.add(SimpleLocalization.Commands.HELP_TOOLTIP_DESCRIPTION.replaceBracket("description", desc));

							if (subcommand.getPermission() != null)
								hover.add(SimpleLocalization.Commands.HELP_TOOLTIP_PERMISSION.replaceBracket("permission", subcommand.getPermission()));

							if (subcommand.getMultilineUsage() != null) {
								hover.add(SimpleLocalization.Commands.HELP_TOOLTIP_USAGE);

								hover.add(SimpleComponent.fromAndCharacter("&f").append(this.replacePlaceholders(subcommand.getMultilineUsage())));

							} else
								hover.add(this.replacePlaceholders(SimpleLocalization.Commands.HELP_TOOLTIP_USAGE.append(usage.isEmpty() ? SimpleComponent.fromPlain(command) : usage)));

							final List<String> hoverShortened = new ArrayList<>();

							for (final SimpleComponent hoverLine : hover)
								for (final String hoverLineSplit : CommonCore.split(hoverLine.toLegacy(), atLeast113 ? 100 : 55))
									hoverShortened.add(hoverLineSplit);

							plainMessage.onHover(hoverShortened);
							plainMessage.onClickSuggestCmd("/" + this.getLabel() + " " + subcommand.getSublabel());
						}

						System.out.println("Adding: " + plainMessage);
						lines.add(plainMessage);
					}
				}

				if (!lines.isEmpty()) {
					final ChatPaginator pages = new ChatPaginator(MathUtilCore.range(0, lines.size(), SimpleCommandGroup.this.getCommandsPerPage()), CompChatColor.DARK_GRAY);

					if (SimpleCommandGroup.this.getHelpHeader() != null)
						pages.setHeader(SimpleCommandGroup.this.getHelpHeader());

					pages.setPages(lines);

					// Allow "? <page>" page parameter
					final int page = (this.args.length > 1 && ValidCore.isInteger(this.args[1]) ? Integer.parseInt(this.args[1]) : 1);

					// Send the component on the main thread
					Platform.runTask(0, () -> pages.send(this.sender, page));

				} else
					this.tellError(SimpleLocalization.Commands.HEADER_NO_SUBCOMMANDS_PERMISSION);
			});
		}

		/**
		 * Finds a subcommand by label
		 *
		 * @param label
		 * @return
		 */
		private SimpleSubCommandCore findSubcommand(final String label) {
			for (final SimpleSubCommandCore command : SimpleCommandGroup.this.subcommands)
				for (final String alias : command.getSublabels())
					if (alias.equalsIgnoreCase(label))
						return command;

			return null;
		}

		/**
		 * Handle tabcomplete for subcommands and their tabcomplete
		 */
		@Override
		public List<String> tabComplete() {
			if (this.args.length == 1)
				return this.tabCompleteSubcommands(this.sender, this.args[0]);

			if (this.args.length > 1) {
				final SimpleSubCommandCore cmd = this.findSubcommand(this.args[0]);

				if (cmd != null)
					return cmd.delegateTabComplete(this.sender, this.getLabel(), Arrays.copyOfRange(this.args, 1, this.args.length));
			}

			return null;
		}

		/**
		 * Automatically tab-complete subcommands
		 *
		 * @param sender
		 * @param param
		 * @return
		 */
		private List<String> tabCompleteSubcommands(final FoundationPlayer sender, String param) {
			param = param.toLowerCase();

			final List<String> tab = new ArrayList<>();

			for (final SimpleSubCommandCore subcommand : SimpleCommandGroup.this.subcommands)
				if (subcommand.showInHelp() && this.hasPerm(subcommand.getPermission()))
					for (final String label : subcommand.getSublabels())
						if (!label.trim().isEmpty() && label.startsWith(param))
							tab.add(label);

			return tab;
		}
	}
}
