package org.mineacademy.fo.command;

import java.util.List;

public final class DumpLocaleCommand extends SimpleSubCommandCore {

	public DumpLocaleCommand() {
		super("dumplocale|dumploc");

		this.setValidArguments(1, 1);
		this.setUsage("<language>");
		this.setDescription("Copy cloud language file to lang/ folder so you can edit it.");
	}

	@Override
	protected void onCommand() {

	}

	/**
	 * @see org.mineacademy.fo.command.SimpleCommandCore#tabComplete()
	 */
	@Override
	protected List<String> tabComplete() {

		if (args.length == 1)
			return completeLastWord("en_US");

		return NO_COMPLETE;
	}
}
