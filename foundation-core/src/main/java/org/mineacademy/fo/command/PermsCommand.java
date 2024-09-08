package org.mineacademy.fo.command;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.mineacademy.fo.CommonCore;
import org.mineacademy.fo.ValidCore;
import org.mineacademy.fo.command.annotation.Permission;
import org.mineacademy.fo.command.annotation.PermissionGroup;
import org.mineacademy.fo.exception.FoException;
import org.mineacademy.fo.model.ChatPaginator;
import org.mineacademy.fo.model.SimpleComponent;
import org.mineacademy.fo.platform.Platform;
import org.mineacademy.fo.settings.Lang;

import lombok.NonNull;

/**
 * A simple predefined command for quickly listing all permissions
 * the plugin uses, given they are stored in a class.
 */
public final class PermsCommand extends SimpleSubCommandCore {

	/**
	 * Classes with permissions listed as fields
	 */
	private final Class<?> classToList;

	/**
	 * The function to replace variables in the annotation
	 */
	private final Function<String, String> variableReplacer;

	/**
	 * Create a new "permisions|perms" subcommand using the given class with
	 * the given variables to replace in the \@PermissionGroup annotation in that class.
	 *
	 * @param classToList
	 */
	public PermsCommand(@NonNull Class<?> classToList) {
		this(classToList, null);
	}

	/**
	 * Create a new "permisions|perms" subcommand using the given class with
	 * the given variables to replace in the \@PermissionGroup annotation in that class.
	 *
	 * @param classToList
	 * @param variableReplacer
	 */
	public PermsCommand(@NonNull Class<?> classToList, Function<String, String> variableReplacer) {
		super("permissions|perms");

		this.classToList = classToList;
		this.variableReplacer = variableReplacer;

		this.setDescription(Lang.component("command-perms-description"));
		this.setUsage(Lang.component("command-perms-usage"));

		// Invoke to check for errors early
		this.list();
	}

	@Override
	protected void onCommand() {

		final String phrase = this.args.length > 0 ? this.joinArgs(0) : null;

		new ChatPaginator(15)
				.setFoundationHeader(Lang.legacy("command-perms-header"))
				.setPages(this.list(phrase))
				.send(this.audience);
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
				.fromMini("&7- ").append(messages.isEmpty() ? Lang.component("command-perms-main") : SimpleComponent.fromPlain(group.value()))
				.onClickOpenUrl(""));

		for (final Field field : clazz.getDeclaredFields()) {
			if (!field.isAnnotationPresent(Permission.class))
				continue;

			final Permission annotation = field.getAnnotation(Permission.class);

			String info = String.join("\n", CommonCore.split(annotation.value(), 50));

			if (info.contains("{label}")) {
				final SimpleCommandGroup defaultGroup = Platform.getPlugin().getDefaultCommandGroup();
				ValidCore.checkNotNull(defaultGroup, "Found {label} in @Permission under " + field + " while no default command group is set!");

				info = info.replace("{label}", defaultGroup.getLabel());
			}

			if (variableReplacer != null)
				info = variableReplacer.apply(info);

			final boolean def = annotation.def();

			if (info.contains("{plugin_name}") || info.contains("{plugin}"))
				throw new FoException("Forgotten unsupported variable in " + info + " for field " + field + " in " + clazz);

			final String node = (String) field.get(null);

			if (node.contains("{plugin_name}") || node.contains("{plugin}"))
				throw new FoException("Forgotten unsupported variable in " + info + " for field " + field + " in " + clazz);

			final boolean has = this.audience == null ? false : this.hasPerm(node.replaceAll("\\.\\{.*?\\}", ""));

			if (phrase == null || node.contains(phrase))
				messages.add(SimpleComponent
						.fromMini("  " + (has ? "&a" : "&7") + node).append(def ? SimpleComponent.fromPlain(" ").append(Lang.component("command-perms-true-by-default")) : SimpleComponent.empty())
						.onClickOpenUrl("")
						.onHover(Lang.legacy("command-perms-info") + info,
								Lang.component("command-perms-default").append(def ? Lang.component("command-perms-yes") : Lang.component("command-perms-no")).toLegacy(),
								Lang.component("command-perms-applied").append(has ? Lang.component("command-perms-yes") : Lang.component("command-perms-no")).toLegacy()));
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