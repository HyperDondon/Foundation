package org.mineacademy.fo.command;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.mineacademy.fo.CommonCore;
import org.mineacademy.fo.command.annotation.Permission;
import org.mineacademy.fo.command.annotation.PermissionGroup;
import org.mineacademy.fo.exception.FoException;
import org.mineacademy.fo.model.ChatPaginator;
import org.mineacademy.fo.model.SimpleComponent;
import org.mineacademy.fo.platform.Platform;
import org.mineacademy.fo.settings.SimpleLocalization.Commands;

import lombok.NonNull;

/**
 * A simple predefined command for quickly listing all permissions
 * the plugin uses, given they are stored in a class.
 */
public final class PermsCommand extends SimpleSubCommandCore {

	/*
	 * Classes with permissions listed as fields
	 */
	private final Class<?> classToList;

	/**
	 * Create a new "permisions|perms" subcommand using the given class
	 * that automatically replaces {label} in the \@PermissionGroup annotation in that class
	 * and your own permission to use this command.
	 *
	 * @param classToList
	 * @param permission
	 */
	public PermsCommand(@NonNull Class<?> classToList, String permission) {
		this(classToList);

		this.setPermission(permission);
	}

	/**
	 * Create a new "permisions|perms" subcommand using the given class with
	 * the given variables to replace in the \@PermissionGroup annotation in that class.
	 *
	 * @param classToList
	 */
	public PermsCommand(@NonNull Class<?> classToList) {
		super("permissions|perms");

		this.classToList = classToList;

		this.setPermission(Platform.getPlugin().getName().toLowerCase() + ".command.permissions");
		this.setDescription(Commands.PERMS_DESCRIPTION);
		this.setUsage(Commands.PERMS_USAGE);

		// Invoke to check for errors early
		this.list();
	}

	@Override
	protected void onCommand() {

		final String phrase = this.args.length > 0 ? this.joinArgs(0) : null;

		new ChatPaginator(15)
				.setFoundationHeader(Commands.PERMS_HEADER.toLegacy())
				.setPages(this.list(phrase))
				.send(this.sender);
	}

	/*
	 * Iterate through all classes and superclasses in the given classes and fill their permissions
	 */
	private List<SimpleComponent> list() {
		return this.list(null);
	}

	/*
	 * Iterate through all classes and superclasses in the given classes and fill their permissions
	 * that match the given phrase
	 */
	private List<SimpleComponent> list(String phrase) {
		final List<SimpleComponent> messages = new ArrayList<>();
		Class<?> iteratedClass = this.classToList;

		try {
			do
				this.listIn(iteratedClass, messages, phrase);
			while (!(iteratedClass = iteratedClass.getSuperclass()).isAssignableFrom(Object.class));

		} catch (final Exception ex) {
			ex.printStackTrace();
		}

		return messages;
	}

	/*
	 * Find annotations and compile permissions list from the given class and given existing
	 * permissions that match the given phrase
	 */
	private void listIn(Class<?> clazz, List<SimpleComponent> messages, String phrase) throws ReflectiveOperationException {

		final PermissionGroup group = clazz.getAnnotation(PermissionGroup.class);

		if (!messages.isEmpty() && !clazz.isAnnotationPresent(PermissionGroup.class))
			throw new FoException("Please place @PermissionGroup over " + clazz);

		messages.add(SimpleComponent
				.fromMini("&7- ").append(messages.isEmpty() ? Commands.PERMS_MAIN : SimpleComponent.fromPlain(group.value()))
				.onClickOpenUrl(""));

		for (final Field field : clazz.getDeclaredFields()) {
			if (!field.isAnnotationPresent(Permission.class))
				continue;

			final Permission annotation = field.getAnnotation(Permission.class);

			final String info = String.join("\n", CommonCore.split(annotation.value(), 50));
			final boolean def = annotation.def();

			if (info.contains("{plugin_name}") || info.contains("{plugin}"))
				throw new FoException("Forgotten unsupported variable in " + info + " for field " + field + " in " + clazz);

			final String node = (String) field.get(null);

			if (node.contains("{plugin_name}") || node.contains("{plugin}"))
				throw new FoException("Forgotten unsupported variable in " + info + " for field " + field + " in " + clazz);

			final boolean has = this.sender == null ? false : this.hasPerm(node.replaceAll("\\.\\{.*?\\}", ""));

			if (phrase == null || node.contains(phrase))
				messages.add(SimpleComponent
						.fromMini("  " + (has ? "&a" : "&7") + node).append(def ? SimpleComponent.fromPlain(" ").append(Commands.PERMS_TRUE_BY_DEFAULT) : SimpleComponent.empty())
						.onClickOpenUrl("")
						.onHover(Commands.PERMS_INFO.toLegacy() + info,
								Commands.PERMS_DEFAULT.append(def ? Commands.PERMS_YES : Commands.PERMS_NO).toLegacy(),
								Commands.PERMS_APPLIED.append(has ? Commands.PERMS_YES : Commands.PERMS_NO).toLegacy()));
		}

		for (final Class<?> inner : clazz.getDeclaredClasses()) {
			messages.add(SimpleComponent.fromMini("&r "));

			this.listIn(inner, messages, phrase);
		}
	}

	/**
	 * @see org.mineacademy.fo.command.SimpleCommandCore#tabComplete()
	 */
	@Override
	protected List<String> tabComplete() {
		return NO_COMPLETE;
	}
}