package org.mineacademy.fo.command;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.mineacademy.fo.platform.Platform;
import org.mineacademy.fo.settings.SimpleLocalization;
import org.mineacademy.fo.settings.SimpleLocalization.Commands;
import org.mineacademy.fo.settings.YamlConfig;

/**
 * A simple predefined sub-command for quickly reloading the plugin
 * using /{label} reload|rl
 */
public final class ReloadCommand extends SimpleSubCommandCore {

	/**
	 * Create a new reload sub-command with the given permission.
	 *
	 * @param permission
	 */
	public ReloadCommand(String permission) {
		this();

		this.setPermission(permission);
	}

	/**
	 * Create a new reload sub-command
	 */
	public ReloadCommand() {
		super("reload|rl");

		this.setDescription(Commands.RELOAD_DESCRIPTION);
	}

	@Override
	protected void onCommand() {
		try {
			this.tellInfo(Commands.RELOAD_STARTED);

			// Syntax check YML files before loading
			boolean syntaxParsed = true;

			final List<File> yamlFiles = new ArrayList<>();

			this.collectYamlFiles(Platform.getPlugin().getDataFolder(), yamlFiles);

			for (final File file : yamlFiles)
				try {
					YamlConfig.fromFile(file);

				} catch (final Throwable t) {
					t.printStackTrace();

					syntaxParsed = false;
				}

			if (!syntaxParsed) {
				this.tellError(SimpleLocalization.Commands.RELOAD_FILE_LOAD_ERROR);

				return;
			}

			Platform.getPlugin().reload();
			this.tellSuccess(SimpleLocalization.Commands.RELOAD_SUCCESS);

		} catch (final Throwable t) {
			this.tellError(SimpleLocalization.Commands.RELOAD_FAIL.replaceBracket("error", t.getMessage() != null ? t.getMessage() : "unknown"));

			t.printStackTrace();
		}
	}

	/*
	 * Get a list of all files ending with "yml" in the given directory
	 * and its subdirectories
	 */
	private List<File> collectYamlFiles(File directory, List<File> list) {

		if (directory.exists())
			for (final File file : directory.listFiles()) {
				if (file.getName().endsWith("yml"))
					list.add(file);

				if (file.isDirectory())
					this.collectYamlFiles(file, list);
			}

		return list;
	}

	/**
	 * @see org.mineacademy.fo.command.SimpleCommandCore#tabComplete()
	 */
	@Override
	protected List<String> tabComplete() {
		return NO_COMPLETE;
	}
}