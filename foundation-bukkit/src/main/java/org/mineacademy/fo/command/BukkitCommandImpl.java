package org.mineacademy.fo.command;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.mineacademy.fo.platform.Platform;
import org.mineacademy.fo.remain.RemainCore;

/**
 * Represents a Bukkit command
 *
 * @deprecated internal use only
 */
@Deprecated
public final class BukkitCommandImpl extends Command {

	private final SimpleCommandCore delegate;

	public BukkitCommandImpl(SimpleCommandCore delegate) {
		super(delegate.getLabel(), RemainCore.convertAdventureToLegacy(delegate.getDescription()), RemainCore.convertAdventureToLegacy(delegate.getUsage()), delegate.getAliases());

		this.delegate = delegate;
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		delegate.delegateExecute(Platform.toAudience(sender), commandLabel, args);

		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String label, String[] args) throws IllegalArgumentException {
		return delegate.delegateTabComplete(Platform.toAudience(sender), label, args);
	}

	// TODO implement other stuff like no permision message etc
}
