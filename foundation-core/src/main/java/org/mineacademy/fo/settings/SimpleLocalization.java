package org.mineacademy.fo.settings;

import org.mineacademy.fo.CommonCore;
import org.mineacademy.fo.FileUtil;
import org.mineacademy.fo.ValidCore;
import org.mineacademy.fo.command.DebugCommand;
import org.mineacademy.fo.command.PermsCommand;
import org.mineacademy.fo.command.ReloadCommand;
import org.mineacademy.fo.model.SimpleComponent;
import org.mineacademy.fo.platform.Platform;
import org.mineacademy.fo.settings.FileConfig.AccusativeHelper;

/**
 * A simple implementation of a basic localization file.
 * We create the localization/messages_LOCALEPREFIX.yml file
 * automatically and fill it with values from your localization/messages_LOCALEPREFIX.yml
 * file placed within in your plugin's jar file.
 */
@SuppressWarnings("unused")
public class SimpleLocalization extends YamlStaticConfig {

	/**
	 * A flag indicating that this class has been loaded
	 * <p>
	 * You can place this class to {@link SimplePlugin#getSettings()} to make
	 * it load automatically
	 */
	private static boolean localizationClassCalled;

	// --------------------------------------------------------------------
	// Loading
	// --------------------------------------------------------------------

	/**
	 * Create and load the localization/messages_LOCALEPREFIX.yml file.
	 * <p>
	 * See {@link SimpleSettings#LOCALE_PREFIX} for the locale prefix.
	 * <p>
	 * The locale file is extracted from your plugins jar to the localization/ folder
	 * if it does not exists, or updated if it is out of date.
	 */
	@Override
	protected final void onLoad() throws Exception {
		final String localePath = "localization/messages_" + SimpleSettings.LOCALE_PREFIX + ".yml";
		final Object content = FileUtil.getInternalFileContent(localePath);

		ValidCore.checkNotNull(content, Platform.getPlugin().getName() + " does not support the localization: messages_" + SimpleSettings.LOCALE_PREFIX
				+ ".yml (For custom locale, set the Locale to 'en' and edit your English file instead)");

		this.loadConfiguration(localePath);

		Lang.setInstance(TEMPORARY_INSTANCE); // pass thru and make permanent, this is null right after this call
	}

	// --------------------------------------------------------------------
	// Version
	// --------------------------------------------------------------------

	/**
	 * The configuration version number, found in the "Version" key in the file.,
	 *
	 * Defaults to 1 if not set in the file.
	 */
	public static Integer VERSION = 1;

	/**
	 * Set and update the config version automatically, however the {@link #VERSION} will
	 * contain the older version used in the file on the disk so you can use
	 * it for comparing in the init() methods
	 * <p>
	 * Please call this as a super method when overloading this!
	 */
	@Override
	protected final void preLoad() {
		// Load version first so we can use it later
		setPathPrefix(null);

		if (isSetDefault("Version"))
			if ((VERSION = getInteger("Version")) != this.getConfigVersion())
				set("Version", this.getConfigVersion());
	}

	/**
	 * Return the very latest config version
	 * <p>
	 * Any changes here must also be made to the "Version" key in your settings file.
	 *
	 * @return
	 */
	protected int getConfigVersion() {
		return 1;
	}

	/**
	 * Always keep the lang file up to date.
	 */
	@Override
	protected final boolean alwaysSaveOnLoad() {
		return true;
	}

	// --------------------------------------------------------------------
	// Shared values
	// --------------------------------------------------------------------

	// NB: Those keys are optional - you do not have to write them into your messages_X.yml files
	// but if you do, we will use your values instead of the default ones!

	/**
	 * Locale keys related to your plugin commands
	 */
	public static final class Commands {

		/**
		 * The message at "No_Console" key shown when console is denied executing a command.
		 */
		public static SimpleComponent NO_CONSOLE = SimpleComponent.fromMini("&cYou may only use this command as a player");

		/**
		 * The message shown when console runs a command without specifying target player name
		 */
		public static SimpleComponent CONSOLE_MISSING_PLAYER_NAME = SimpleComponent.fromMini("When running from console, specify player name.");

		/**
		 * The message shown when there is a fatal error running this command
		 */
		public static SimpleComponent COOLDOWN_WAIT = SimpleComponent.fromMini("Wait {duration} second(s) before using this command again.");

		/**
		 * Keys below indicate an invalid action or input
		 */
		public static SimpleComponent INVALID_ARGUMENT = SimpleComponent.fromMini("Invalid argument(s) '{arguments}'. Run <gold>{help_command} <red>for help.");
		public static SimpleComponent INVALID_TIME = SimpleComponent.fromMini("Expected time such as '3 hours' or '15 minutes'. Got: '{input}'");
		public static SimpleComponent INVALID_NUMBER = SimpleComponent.fromMini("The number must be a whole or a decimal number. Got: '{input}'");
		public static SimpleComponent INVALID_STRING = SimpleComponent.fromMini("Invalid string. Got: '{input}'");
		public static SimpleComponent INVALID_WORLD = SimpleComponent.fromMini("Invalid world '{world}'. Available: {available}");
		public static SimpleComponent INVALID_UUID = SimpleComponent.fromMini("Invalid UUID '{uuid}'");
		public static SimpleComponent INVALID_ENUM = SimpleComponent.fromMini("No such {type} '{value}'. Available: {available}");

		/**
		 * The authors label
		 */
		public static String LABEL_AUTHORS = "Made by";

		/**
		 * The description label
		 */
		public static SimpleComponent LABEL_DESCRIPTION = SimpleComponent.fromMini("<red>Description: {description}");

		/**
		 * The optional arguments label
		 */
		public static String LABEL_OPTIONAL_ARGS = "optional arguments";

		/**
		 * The required arguments label
		 */
		public static String LABEL_REQUIRED_ARGS = "required arguments";

		/**
		 * The usage label
		 */
		public static SimpleComponent LABEL_USAGE = SimpleComponent.fromMini("&cUsage: {usage}");

		/**
		 * The help for label
		 */
		public static SimpleComponent LABEL_HELP_FOR = SimpleComponent.fromMini("Help for /{label}");

		/**
		 * The keys below are shown as hover tooltip on /command help menu.
		 */
		public static SimpleComponent HELP_TOOLTIP_DESCRIPTION = SimpleComponent.fromMini("&7Description: &f{description}");
		public static SimpleComponent HELP_TOOLTIP_PERMISSION = SimpleComponent.fromMini("&7Permission: &f{permission}");
		public static SimpleComponent HELP_TOOLTIP_USAGE = SimpleComponent.fromMini("&7Usage: &f{usage}");

		/**
		 * The keys below are used in the {@link ReloadCommand}
		 */
		public static SimpleComponent RELOAD_DESCRIPTION = SimpleComponent.fromMini("Reload the configuration.");
		public static SimpleComponent RELOAD_STARTED = SimpleComponent.fromMini("Reloading plugin's data, please wait..");
		public static SimpleComponent RELOAD_SUCCESS = SimpleComponent.fromMini("{plugin_name} {plugin_version} has been reloaded.");
		public static SimpleComponent RELOAD_FILE_LOAD_ERROR = SimpleComponent.fromMini("&4Oups, &cthere was a problem loading files from your disk! See the console for more information. {plugin_name} has not been reloaded.");
		public static SimpleComponent RELOAD_FAIL = SimpleComponent.fromMini("&4Oups, &creloading failed! See the console for more information. Error: {error}");

		/**
		 * The message shown when there is a fatal error running this command
		 */
		public static SimpleComponent ERROR = SimpleComponent.fromMini("<red><bold>Oups! <reset><red>The command failed :( Check the console and report the error.");

		/**
		 * The message shown when player has no permissions to view ANY subcommands in group command.
		 */
		public static SimpleComponent HEADER_NO_SUBCOMMANDS = SimpleComponent.fromMini("&cThere are no arguments for this command.");

		/**
		 * The message shown when player has no permissions to view ANY subcommands in group command.
		 */
		public static SimpleComponent HEADER_NO_SUBCOMMANDS_PERMISSION = SimpleComponent.fromMini("&cYou don't have permissions to view any subcommands.");

		/**
		 * The center character of the format in case \<center\> is used
		 */
		public static String HEADER_CENTER_LETTER = "-";

		/**
		 * The padding of the header in case \<center\> is used
		 */
		public static Integer HEADER_CENTER_PADDING = 130;

		/**
		 * Key for when plugin is disabled
		 */
		public static SimpleComponent DISABLED = SimpleComponent.fromMini("disabled");

		/**
		 * The message shown when plugin is reloading or was disabled and player attempts to run command
		 */
		public static SimpleComponent CANNOT_USE_WHILE_NULL = SimpleComponent.fromMini("<red>Cannot use this command while the plugin is {state}.");

		/**
		 * The message shown in SimpleCommand.findWorld()
		 */
		public static SimpleComponent CANNOT_AUTODETECT_WORLD = SimpleComponent.fromMini("Only living players can use ~ for their world!");

		/**
		 * The keys below are used in the {@link DebugCommand}
		 */
		public static SimpleComponent DEBUG_DESCRIPTION = SimpleComponent.fromMini("ZIP your settings for reporting bugs.");
		public static SimpleComponent DEBUG_PREPARING = SimpleComponent.fromMini("Preparing debug log...");
		public static SimpleComponent DEBUG_SUCCESS = SimpleComponent.fromMini("Successfuly copied {amount} file(s) to debug.zip. Your sensitive MySQL information has been removed from yml files. Please upload it via ufile.io and send it to us for review.");
		public static SimpleComponent DEBUG_COPY_FAIL = SimpleComponent.fromMini("Copying files failed on file {file} and it was stopped. See console for more information.");
		public static SimpleComponent DEBUG_ZIP_FAIL = SimpleComponent.fromMini("Creating a ZIP of your files failed, see console for more information. Please ZIP debug/ folder and send it to us via ufile.io manually.");

		/**
		 * The keys below are used in the {@link PermsCommand}
		 */
		public static SimpleComponent PERMS_DESCRIPTION = SimpleComponent.fromMini("List all permissions the plugin has.");
		public static String PERMS_USAGE = "[phrase]";
		public static SimpleComponent PERMS_HEADER = SimpleComponent.fromMini("Listing All {plugin_name} Permissions");
		public static SimpleComponent PERMS_MAIN = SimpleComponent.fromMini("Main Permissions");
		public static SimpleComponent PERMS_TRUE_BY_DEFAULT = SimpleComponent.fromMini("&7[true by default]");
		public static SimpleComponent PERMS_INFO = SimpleComponent.fromMini("&7Info: &f");
		public static SimpleComponent PERMS_DEFAULT = SimpleComponent.fromMini("&7Default? ");
		public static SimpleComponent PERMS_APPLIED = SimpleComponent.fromMini("&7Do you have it? ");
		public static SimpleComponent PERMS_YES = SimpleComponent.fromMini("&2yes");
		public static SimpleComponent PERMS_NO = SimpleComponent.fromMini("&cno");

		/**
		 * The keys below are used in RegionTool
		 */
		public static SimpleComponent REGION_SET_PRIMARY = SimpleComponent.fromMini("Set the primary region point.");
		public static SimpleComponent REGION_BLOCK_ALREADY_PRIMARY = SimpleComponent.fromMini("This block is already a primary point.");
		public static SimpleComponent REGION_REMOVE_PRIMARY = SimpleComponent.fromMini("Removed the primary region point.");
		public static SimpleComponent REGION_SET_SECONDARY = SimpleComponent.fromMini("Set the secondary region point.");
		public static SimpleComponent REGION_BLOCK_ALREADY_SECONDARY = SimpleComponent.fromMini("This block is already a secondary point.");
		public static SimpleComponent REGION_REMOVE_SECONDARY = SimpleComponent.fromMini("Removed the secondary region point.");

		/**
		 * Load the values -- this method is called automatically by reflection in the {@link YamlStaticConfig} class!
		 */
		private static void init() {
			setPathPrefix("Commands");

			if (isSetDefault("No_Console"))
				NO_CONSOLE = getComponent("No_Console");

			if (isSetDefault("Console_Missing_Player_Name"))
				CONSOLE_MISSING_PLAYER_NAME = getComponent("Console_Missing_Player_Name");

			if (isSetDefault("Cooldown_Wait"))
				COOLDOWN_WAIT = getComponent("Cooldown_Wait");

			if (isSetDefault("Invalid_Argument"))
				INVALID_ARGUMENT = getComponent("Invalid_Argument");

			if (isSetDefault("Invalid_Time"))
				INVALID_TIME = getComponent("Invalid_Time");

			if (isSetDefault("Invalid_Number"))
				INVALID_NUMBER = getComponent("Invalid_Number");

			if (isSetDefault("Invalid_String"))
				INVALID_STRING = getComponent("Invalid_String");

			if (isSetDefault("Invalid_World"))
				INVALID_WORLD = getComponent("Invalid_World");

			if (isSetDefault("Invalid_UUID"))
				INVALID_UUID = getComponent("Invalid_UUID");

			if (isSetDefault("Invalid_Enum"))
				INVALID_ENUM = getComponent("Invalid_Enum");

			if (isSetDefault("Label_Authors"))
				LABEL_AUTHORS = getString("Label_Authors");

			if (isSetDefault("Label_Description"))
				LABEL_DESCRIPTION = getComponent("Label_Description");

			if (isSetDefault("Label_Optional_Args"))
				LABEL_OPTIONAL_ARGS = getString("Label_Optional_Args");

			if (isSetDefault("Label_Required_Args"))
				LABEL_REQUIRED_ARGS = getString("Label_Required_Args");

			if (isSetDefault("Label_Usage"))
				LABEL_USAGE = getComponent("Label_Usage");

			if (isSetDefault("Label_Help_For"))
				LABEL_HELP_FOR = getComponent("Label_Help_For");

			if (isSetDefault("Help_Tooltip_Description"))
				HELP_TOOLTIP_DESCRIPTION = getComponent("Help_Tooltip_Description");

			if (isSetDefault("Help_Tooltip_Permission"))
				HELP_TOOLTIP_PERMISSION = getComponent("Help_Tooltip_Permission");

			if (isSetDefault("Help_Tooltip_Usage"))
				HELP_TOOLTIP_USAGE = getComponent("Help_Tooltip_Usage");

			if (isSetDefault("Reload_Description"))
				RELOAD_DESCRIPTION = getComponent("Reload_Description");

			if (isSetDefault("Reload_Started"))
				RELOAD_STARTED = getComponent("Reload_Started");

			if (isSetDefault("Reload_Success"))
				RELOAD_SUCCESS = getComponent("Reload_Success");

			if (isSetDefault("Reload_File_Load_Error"))
				RELOAD_FILE_LOAD_ERROR = getComponent("Reload_File_Load_Error");

			if (isSetDefault("Reload_Fail"))
				RELOAD_FAIL = getComponent("Reload_Fail");

			if (isSetDefault("Error"))
				ERROR = getComponent("Error");

			if (isSetDefault("Header_No_Subcommands"))
				HEADER_NO_SUBCOMMANDS = getComponent("Header_No_Subcommands");

			if (isSetDefault("Header_No_Subcommands_Permission"))
				HEADER_NO_SUBCOMMANDS_PERMISSION = getComponent("Header_No_Subcommands_Permission");

			if (isSetDefault("Header_Center_Letter")) {
				HEADER_CENTER_LETTER = getString("Header_Center_Letter");

				ValidCore.checkBoolean(HEADER_CENTER_LETTER.length() == 1, "Header_Center_Letter must only have 1 letter, not " + HEADER_CENTER_LETTER.length() + ":" + HEADER_CENTER_LETTER);
			}

			if (isSetDefault("Header_Center_Padding"))
				HEADER_CENTER_PADDING = getInteger("Header_Center_Padding");

			if (isSet("Disabled"))
				DISABLED = getComponent("Disabled");

			if (isSet("Use_While_Null"))
				CANNOT_USE_WHILE_NULL = getComponent("Use_While_Null");

			if (isSet("Cannot_Autodetect_World"))
				CANNOT_AUTODETECT_WORLD = getComponent("Cannot_Autodetect_World");

			if (isSetDefault("Debug_Description"))
				DEBUG_DESCRIPTION = getComponent("Debug_Description");

			if (isSetDefault("Debug_Preparing"))
				DEBUG_PREPARING = getComponent("Debug_Preparing");

			if (isSetDefault("Debug_Success"))
				DEBUG_SUCCESS = getComponent("Debug_Success");

			if (isSetDefault("Debug_Copy_Fail"))
				DEBUG_COPY_FAIL = getComponent("Debug_Copy_Fail");

			if (isSetDefault("Debug_Zip_Fail"))
				DEBUG_ZIP_FAIL = getComponent("Debug_Zip_Fail");

			if (isSetDefault("Perms_Description"))
				PERMS_DESCRIPTION = getComponent("Perms_Description");

			if (isSetDefault("Perms_Usage"))
				PERMS_USAGE = getString("Perms_Usage");

			if (isSetDefault("Perms_Header"))
				PERMS_HEADER = getComponent("Perms_Header");

			if (isSetDefault("Perms_Main"))
				PERMS_MAIN = getComponent("Perms_Main");

			if (isSetDefault("Perms_True_By_Default"))
				PERMS_TRUE_BY_DEFAULT = getComponent("Perms_True_By_Default");

			if (isSetDefault("Perms_Info"))
				PERMS_INFO = getComponent("Perms_Info");

			if (isSetDefault("Perms_Default"))
				PERMS_DEFAULT = getComponent("Perms_Default");

			if (isSetDefault("Perms_Applied"))
				PERMS_APPLIED = getComponent("Perms_Applied");

			if (isSetDefault("Perms_Yes"))
				PERMS_YES = getComponent("Perms_Yes");

			if (isSetDefault("Perms_No"))
				PERMS_NO = getComponent("Perms_No");

			if (isSetDefault("Region_Set_Primary"))
				REGION_SET_PRIMARY = getComponent("Region_Set_Primary");

			if (isSetDefault("Region_Set_Secondary"))
				REGION_SET_SECONDARY = getComponent("Region_Set_Secondary");
		}
	}

	/**
	 * Strings related to player-server conversation waiting for his chat input
	 */
	public static final class Conversation {

		/**
		 * The key used when the player wants to converse but he is not conversing.
		 */
		public static SimpleComponent NOT_CONVERSING = SimpleComponent.fromMini("You can only use this command when you are asked to type something in the chat.");

		/**
		 * Called in the try-catch handling when an error occurs
		 */
		public static SimpleComponent CONVERSATION_ERROR = SimpleComponent.fromMini("Oups! There was a problem in this conversation! Please contact the administrator to review the console for details.");

		/**
		 * Called in SimplePrompt
		 */
		public static SimpleComponent CANCELLED = SimpleComponent.fromMini("Your pending chat answer has been canceled.");

		/**
		 * Called in SimplePrompt
		 */
		public static SimpleComponent CANCELLED_INACTIVE = SimpleComponent.fromMini("Your pending chat answer has been canceled because you were inactive.");

		private static void init() {
			setPathPrefix("Conversation");

			if (isSetDefault("Not_Conversing"))
				NOT_CONVERSING = getComponent("Not_Conversing");

			if (isSetDefault("Conversation_Error"))
				CONVERSATION_ERROR = getComponent("Error");

			if (isSetDefault("Cancelled"))
				CANCELLED = getComponent("Cancelled");

			if (isSetDefault("Cancelled_Inactive"))
				CANCELLED_INACTIVE = getComponent("Cancelled_Inactive");
		}
	}

	/**
	 * Key related to players
	 */
	public static final class Player {

		/**
		 * Message shown when the player is not online on this server
		 */
		public static SimpleComponent NOT_ONLINE = SimpleComponent.fromMini("&cPlayer {player} &cis not online on this server.");

		/**
		 * Message shown when Bukkit#getOfflinePlayer(String) returns that the player has not played before
		 */
		public static SimpleComponent NOT_PLAYED_BEFORE = SimpleComponent.fromMini("&cPlayer {player} &chas not played before or we could not locate his disk data.");

		/**
		 * Message shown the an offline player is returned null from a given UUID.
		 */
		public static SimpleComponent INVALID_UUID = SimpleComponent.fromMini("&cCould not find a player from UUID {uuid}.");

		/**
		 * Load the values -- this method is called automatically by reflection in the {@link YamlStaticConfig} class!
		 */
		private static void init() {
			setPathPrefix("Player");

			if (isSetDefault("Not_Online"))
				NOT_ONLINE = getComponent("Not_Online");

			if (isSetDefault("Not_Played_Before"))
				NOT_PLAYED_BEFORE = getComponent("Not_Played_Before");

			if (isSetDefault("Invalid_UUID"))
				INVALID_UUID = getComponent("Invalid_UUID");
		}
	}

	/**
	 * Keys related to ChatPaginator
	 */
	public static final class Pages {

		public static SimpleComponent NO_PAGE_NUMBER = SimpleComponent.fromMini("&cPlease specify the page number for this command.");
		public static SimpleComponent NO_PAGES = SimpleComponent.fromMini("There are no results to list.");
		public static SimpleComponent NO_PAGE = SimpleComponent.fromMini("Pages do not contain the given page number.");
		public static SimpleComponent INVALID_PAGE = SimpleComponent.fromMini("&cYour input '{input}' is not a valid number.");
		public static SimpleComponent GO_TO_PAGE = SimpleComponent.fromMini("&7Go to page {page}");
		public static SimpleComponent GO_TO_FIRST_PAGE = SimpleComponent.fromMini("&7Go to the first page");
		public static SimpleComponent GO_TO_LAST_PAGE = SimpleComponent.fromMini("&7Go to the last page");
		public static String[] TOOLTIP = {
				"&7You can also navigate using the",
				"&7hidden /#flp <page> command."
		};

		/**
		 * Load the values -- this method is called automatically by reflection in the {@link YamlStaticConfig} class!
		 */
		private static void init() {
			setPathPrefix("Pages");

			if (isSetDefault("No_Page_Number"))
				NO_PAGE_NUMBER = getComponent("No_Page_Number");

			if (isSetDefault("No_Pages"))
				NO_PAGES = getComponent("No_Pages");

			if (isSetDefault("No_Page"))
				NO_PAGE = getComponent("No_Page");

			if (isSetDefault("Invalid_Page"))
				INVALID_PAGE = getComponent("Invalid_Page");

			if (isSetDefault("Go_To_Page"))
				GO_TO_PAGE = getComponent("Go_To_Page");

			if (isSetDefault("Go_To_First_Page"))
				GO_TO_FIRST_PAGE = getComponent("Go_To_First_Page");

			if (isSetDefault("Go_To_Last_Page"))
				GO_TO_LAST_PAGE = getComponent("Go_To_Last_Page");

			if (isSetDefault("Tooltip"))
				TOOLTIP = CommonCore.toArray(getStringList("Tooltip"));
		}
	}

	/**
	 * Keys related to the GUI system
	 */
	public static final class Menu {

		/**
		 * Message shown when the player is not online on this server
		 */
		public static SimpleComponent ITEM_DELETED = SimpleComponent.fromMini("&2The {item} has been deleted.");

		/**
		 * Message shown when the player tries to open menu, but has an ongoing conversation.
		 */
		public static SimpleComponent CANNOT_OPEN_DURING_CONVERSATION = SimpleComponent.fromMini("&cType 'exit' to quit your conversation before opening menu.");

		/**
		 * Message shown on error
		 */
		public static SimpleComponent ERROR = SimpleComponent.fromMini("&cOups! There was a problem with this menu! Please contact the administrator to review the console for details.");

		/**
		 * Keys related to menu pagination
		 */
		public static String PAGE_PREVIOUS = "&8<< &fPage {page}";
		public static String PAGE_NEXT = "Page {page} &8>>";
		public static String PAGE_FIRST = "&7First Page";
		public static String PAGE_LAST = "&7Last Page";

		/**
		 * Keys related to menu titles and tooltips
		 */
		public static String TITLE_TOOLS = "Tools Menu";
		public static String TOOLTIP_INFO = "&fMenu Information";
		public static String BUTTON_RETURN_TITLE = "&4&lReturn";
		public static String[] BUTTON_RETURN_LORE = { "", "Return back." };

		/**
		 * Load the values -- this method is called automatically by reflection in the {@link YamlStaticConfig} class!
		 */
		private static void init() {
			setPathPrefix("Menu");

			if (isSetDefault("Item_Deleted"))
				ITEM_DELETED = getComponent("Item_Deleted");

			if (isSetDefault("Cannot_Open_During_Conversation"))
				CANNOT_OPEN_DURING_CONVERSATION = getComponent("Cannot_Open_During_Conversation");

			if (isSetDefault("Error"))
				ERROR = getComponent("Error");

			if (isSetDefault("Page_Previous"))
				PAGE_PREVIOUS = getString("Page_Previous");

			if (isSetDefault("Page_Next"))
				PAGE_NEXT = getString("Page_Next");

			if (isSetDefault("Page_First"))
				PAGE_FIRST = getString("Page_First");

			if (isSetDefault("Page_Last"))
				PAGE_LAST = getString("Page_Last");

			if (isSetDefault("Title_Tools"))
				TITLE_TOOLS = getString("Title_Tools");

			if (isSetDefault("Tooltip_Info"))
				TOOLTIP_INFO = getString("Tooltip_Info");

			if (isSetDefault("Button_Return_Title"))
				BUTTON_RETURN_TITLE = getString("Button_Return_Title");

			if (isSetDefault("Button_Return_Lore"))
				BUTTON_RETURN_LORE = CommonCore.toArray(getStringList("Button_Return_Lore"));
		}
	}

	/**
	 * Keys related to tools
	 */
	public static final class Tool {

		/**
		 * The message shown when a tool errors out.
		 */
		public static String ERROR = "&cOups! There was a problem with this tool! Please contact the administrator to review the console for details.";

		/**
		 * Load the values -- this method is called automatically by reflection in the {@link YamlStaticConfig} class!
		 */
		private static void init() {
			setPathPrefix("Tool");

			if (isSetDefault("Error"))
				ERROR = getString("Error");
		}
	}

	/**
	 * Keys related to cases
	 */
	public static class Cases {

		public static AccusativeHelper SECOND = AccusativeHelper.of("second", "seconds");
		public static AccusativeHelper MINUTE = AccusativeHelper.of("minute", "minutes");
		public static AccusativeHelper HOUR = AccusativeHelper.of("hour", "hours");
		public static AccusativeHelper DAY = AccusativeHelper.of("day", "days");
		public static AccusativeHelper WEEK = AccusativeHelper.of("week", "weeks");
		public static AccusativeHelper MONTH = AccusativeHelper.of("month", "months");
		public static AccusativeHelper YEAR = AccusativeHelper.of("year", "years");

		private static void init() {
			setPathPrefix("Cases");

			if (isSetDefault("Second"))
				SECOND = getCasus("Second");

			if (isSetDefault("Minute"))
				MINUTE = getCasus("Minute");

			if (isSetDefault("Hour"))
				HOUR = getCasus("Hour");

			if (isSetDefault("Day"))
				DAY = getCasus("Day");

			if (isSetDefault("Week"))
				WEEK = getCasus("Week");

			if (isSetDefault("Month"))
				MONTH = getCasus("Month");

			if (isSetDefault("Year"))
				YEAR = getCasus("Year");
		}
	}

	public static class Prefix {

		/**
		 * The announce prefix
		 */
		public static SimpleComponent ANNOUNCE = SimpleComponent.fromMini("<dark_gray>[<dark_purple><bold>!<reset><dark_gray>]<light_purple>");

		/**
		 * The error prefix
		 */
		public static SimpleComponent ERROR = SimpleComponent.fromMini("<dark_gray>[<dark_red><bold>\u2715<reset><dark_gray>]<red>");

		/**
		 * The info prefix
		 */
		public static SimpleComponent INFO = SimpleComponent.fromMini("<dark_gray>[<blue><bold>i<reset><dark_gray>]<gray>");

		/**
		 * The question prefix
		 */
		public static SimpleComponent QUESTION = SimpleComponent.fromMini("<dark_gray>[<green><bold>?<reset><dark_gray>]<gray>");

		/**
		 * The success prefix
		 */
		public static SimpleComponent SUCCESS = SimpleComponent.fromMini("<dark_gray>[<dark_green><bold>\u2714<reset><dark_gray>]<gray>");

		/**
		 * The warn prefix
		 */
		public static SimpleComponent WARN = SimpleComponent.fromMini("<dark_gray>[<gold><bold>!<reset><dark_gray>]<gold>");

		/**
		 * Load the values -- this method is called automatically by reflection in the {@link YamlStaticConfig} class!
		 */
		private static void init() {
			setPathPrefix("Prefix");

			if (isSetDefault("Announce"))
				ANNOUNCE = getComponent("Announce");

			if (isSetDefault("Error"))
				ERROR = getComponent("Error");

			if (isSetDefault("Info"))
				INFO = getComponent("Info");

			if (isSetDefault("Question"))
				QUESTION = getComponent("Question");

			if (isSetDefault("Success"))
				SUCCESS = getComponent("Success");

			if (isSetDefault("Warn"))
				WARN = getComponent("Warn");
		}
	}

	/**
	 * Denotes the "none" message
	 */
	public static String NONE = "None";

	/**
	 * The message for player if they lack a permission.
	 */
	public static SimpleComponent NO_PERMISSION = SimpleComponent.fromMini("<red>Insufficient permission ({permission}).");

	/**
	 * The server prefix. Example: you have to use it manually if you are sending messages
	 * from the console to players
	 */
	public static String SERVER_PREFIX = "[Server]";

	/**
	 * The console localized name. Example: Console
	 */
	public static String CONSOLE_NAME = "Console";

	/**
	 * The message when a section is missing from data file (the one ending in .db) (typically we use
	 * this file to store serialized values such as arenas from minigame plugins).
	 */
	public static String DATA_MISSING = "&c{name} lacks database information! Please only create {type} in-game! Skipping..";

	/**
	 * Load the values -- this method is called automatically by reflection in the {@link YamlStaticConfig} class!
	 */
	private static void init() {
		setPathPrefix(null);
		ValidCore.checkBoolean(!localizationClassCalled, "Localization class already loaded!");

		if (isSetDefault("No_Permission"))
			NO_PERMISSION = getComponent("No_Permission");

		if (isSetDefault("Server_Prefix"))
			SERVER_PREFIX = getString("Server_Prefix");

		if (isSetDefault("Console_Name"))
			CONSOLE_NAME = getString("Console_Name");

		if (isSetDefault("Data_Missing"))
			DATA_MISSING = getString("Data_Missing");

		if (isSetDefault("None"))
			NONE = getString("None");

		localizationClassCalled = true;
	}

	/**
	 * Was this class loaded?
	 *
	 * @return
	 */
	public static final Boolean isLocalizationCalled() {
		return localizationClassCalled;
	}

	/**
	 * Reset the flag indicating that the class has been loaded,
	 * used in reloading.
	 */
	public static final void resetLocalizationCall() {
		localizationClassCalled = false;
	}
}
