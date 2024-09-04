package org.mineacademy.fo.command;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.mineacademy.fo.platform.Platform;

/**
 * Represents a Bukkit command
 *
 * @deprecated internal use only
 */
@Deprecated
public final class BukkitCommandImpl extends Command {

	private final SimpleCommandCore delegate;

	public BukkitCommandImpl(SimpleCommandCore delegate) {
		super(delegate.getLabel());

		if (delegate.getAliases() != null)
			this.setAliases(delegate.getAliases());

		if (delegate.getUsage() != null)
			this.setUsage(delegate.getUsage().toLegacy());

		if (delegate.getDescription() != null)
			this.setDescription(delegate.getDescription().toLegacy());

		this.delegate = delegate;
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		delegate.delegateExecute(Platform.toPlayer(sender), commandLabel, args);

		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String label, String[] args) throws IllegalArgumentException {
		return delegate.delegateTabComplete(Platform.toPlayer(sender), label, args);
	}
}
