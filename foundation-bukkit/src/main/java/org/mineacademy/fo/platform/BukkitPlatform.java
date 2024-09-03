package org.mineacademy.fo.platform;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.MinecraftVersion;
import org.mineacademy.fo.MinecraftVersion.V;
import org.mineacademy.fo.ReflectionUtil;
import org.mineacademy.fo.SerializeUtil;
import org.mineacademy.fo.SerializeUtilCore.Language;
import org.mineacademy.fo.SerializeUtilCore.Serializer;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.ValidCore;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.fo.command.BukkitCommandImpl;
import org.mineacademy.fo.command.SimpleCommandCore;
import org.mineacademy.fo.exception.FoException;
import org.mineacademy.fo.jsonsimple.JSONArray;
import org.mineacademy.fo.jsonsimple.JSONParser;
import org.mineacademy.fo.model.HookManager;
import org.mineacademy.fo.model.SimpleComponent;
import org.mineacademy.fo.model.SimpleSound;
import org.mineacademy.fo.model.Task;
import org.mineacademy.fo.model.Tuple;
import org.mineacademy.fo.model.Variables;
import org.mineacademy.fo.plugin.BukkitVariableCollector;
import org.mineacademy.fo.plugin.SimplePlugin;
import org.mineacademy.fo.remain.CompEnchantment;
import org.mineacademy.fo.remain.CompPotionEffectType;
import org.mineacademy.fo.remain.JsonItemStack;
import org.mineacademy.fo.remain.Remain;
import org.mineacademy.fo.settings.YamlConfig;

import lombok.NonNull;
import net.kyori.adventure.text.event.HoverEventSource;
import novy.config.YamlConfiguration;

public class BukkitPlatform extends FoundationPlatform {

	public BukkitPlatform() {
		this.inject();
	}

	private void inject() {

		// Inject Yaml
		YamlConfig.setCustomConstructor(settings -> new BukkitYamlConstructor(settings));
		YamlConfig.setCustomRepresenter(settings -> new BukkitYamlRepresenter(settings));

		YamlConfiguration.setCustomConstructor(settings -> new BukkitYamlConstructor(settings));
		YamlConfiguration.setCustomRepresenter(settings -> new BukkitYamlRepresenter(settings));

		// Expand simple component
		SimpleComponent.setBuilder(HookManager::replaceRelationPlaceholders);

		/**
		 * Shows the item on hover if it is not air.
		 * <p>
		 * NB: Some colors from lore may get lost as a result of Minecraft/Spigot bug.
		 *
		 * @param item
		 * @return
		 */
		/*public SimpleComponent onHover(@NonNull final ItemStack item) {
			if (CompMaterial.isAir(item.getType()))
				return this.onHover("Air");

			try {
				this.modifyLastComponent(component -> component.hoverEvent(Remain.convertItemStackToHoverEvent(item)));

			} catch (final Throwable t) {
				CommonCore.logFramed(
						"Error parsing ItemStack to simple component!",
						"Item: " + item,
						"Error: " + t.getMessage());

				t.printStackTrace();
			}

			return this;
		}*/

		// Initialize platform-specific variables
		Variables.setCollector(new BukkitVariableCollector());

		// Add platform-specific helpers to translate values to a config and back
		SerializeUtil.addSerializer(new Serializer() {

			@Override
			public Object serialize(Language language, Object object) {
				if (object instanceof World)
					return ((World) object).getName();

				else if (object instanceof Location)
					return SerializeUtil.serializeLoc((Location) object);

				else if (object instanceof PotionEffectType)
					return ((PotionEffectType) object).getName();

				else if (object instanceof PotionEffect) {
					final PotionEffect effect = (PotionEffect) object;

					return effect.getType().getName() + " " + effect.getDuration() + " " + effect.getAmplifier();
				}

				else if (object instanceof Enchantment)
					return ((Enchantment) object).getName();

				else if (object instanceof SimpleSound)
					return object.toString();

				else if (object instanceof ItemStack) {
					if (language == Language.JSON)
						return JsonItemStack.toJson((ItemStack) object);
					else
						return object;
				}

				else if (object instanceof ItemStack[]) {
					if (language == SerializeUtil.Language.JSON) {
						final JSONArray jsonList = new JSONArray();

						for (final ItemStack item : (ItemStack[]) object)
							jsonList.add(item == null ? null : JsonItemStack.toJson(item));

						return jsonList.toJson();

					} else
						return SerializeUtil.serialize(language, Arrays.asList((ItemStack[]) object));
				}

				else if (object instanceof ConfigurationSerializable)
					return object; // will pack in BukkitYamlRepresenter

				return null;
			}

			@Override
			public <T> T deserialize(@NonNull Language language, @NonNull Class<T> classOf, @NonNull Object object, Object... parameters) {

				if (classOf == Location.class)
					return (T) SerializeUtil.deserializeLocation((String) object);

				else if (classOf == World.class) {
					final World world = Bukkit.getWorld((String) object);
					Valid.checkNotNull(world, "World " + object + " not found. Available: " + Bukkit.getWorlds());

					return (T) world;
				}

				else if (classOf == PotionEffectType.class) {
					final PotionEffectType type = CompPotionEffectType.getByName((String) object);
					Valid.checkNotNull(type, "Potion effect type " + object + " not found. Available: " + CompPotionEffectType.getPotionNames());

					return (T) type;
				}

				else if (classOf == PotionEffect.class) {
					final String[] parts = object.toString().split(" ");
					ValidCore.checkBoolean(parts.length == 3, "Expected PotionEffect (String) but got " + object.getClass().getSimpleName() + ": " + object);

					final String typeRaw = parts[0];
					final PotionEffectType type = PotionEffectType.getByName(typeRaw);

					final int duration = Integer.parseInt(parts[1]);
					final int amplifier = Integer.parseInt(parts[2]);

					return (T) new PotionEffect(type, duration, amplifier);
				}

				else if (classOf == Enchantment.class) {
					final Enchantment enchant = CompEnchantment.getByName((String) object);
					Valid.checkNotNull(enchant, "Enchantment " + object + " not found. Available: " + CompEnchantment.getEnchantmentNames());

					return (T) enchant;
				}

				else if (classOf == SimpleSound.class)
					return (T) new SimpleSound((String) object);

				else if (classOf == ItemStack.class) {
					if (object instanceof ItemStack)
						return (T) object;

					if (language == Language.JSON)
						return (T) JsonItemStack.fromJson(object.toString());

					else {
						final SerializedMap map = SerializedMap.of(object);

						final ItemStack item = ItemStack.deserialize(map.asMap());
						final SerializedMap meta = map.getMap("meta");

						if (meta != null)
							try {
								final Class<?> metaClass = ReflectionUtil.getOBCClass("inventory." + (meta.containsKey("spawnedType") ? "CraftMetaSpawnEgg" : "CraftMetaItem"));
								final Constructor<?> constructor = metaClass.getDeclaredConstructor(Map.class);
								constructor.setAccessible(true);

								final Object craftMeta = constructor.newInstance((Map<String, ?>) meta.serialize());

								if (craftMeta instanceof ItemMeta)
									item.setItemMeta((ItemMeta) craftMeta);

							} catch (final Throwable t) {

								// We have to manually deserialize metadata :(
								final ItemMeta itemMeta = item.getItemMeta();

								final String display = meta.containsKey("display-name") ? meta.getString("display-name") : null;

								if (display != null)
									itemMeta.setDisplayName(display);

								final List<String> lore = meta.containsKey("lore") ? meta.getStringList("lore") : null;

								if (lore != null)
									itemMeta.setLore(lore);

								final SerializedMap enchants = meta.containsKey("enchants") ? meta.getMap("enchants") : null;

								if (enchants != null)
									for (final Map.Entry<String, Object> entry : enchants.entrySet()) {
										final Enchantment enchantment = Enchantment.getByName(entry.getKey());
										final int level = (int) entry.getValue();

										itemMeta.addEnchant(enchantment, level, true);
									}

								final List<String> itemFlags = meta.containsKey("ItemFlags") ? meta.getStringList("ItemFlags") : null;

								if (itemFlags != null)
									for (final String flag : itemFlags)
										try {
											itemMeta.addItemFlags(ItemFlag.valueOf(flag));
										} catch (final Exception ex) {
											// Likely not MC compatible, ignore
										}

								item.setItemMeta(itemMeta);
							}

						return (T) item;
					}
				}

				else if (classOf == ItemStack[].class) {
					if (object instanceof ItemStack[])
						return (T) object;

					final List<ItemStack> list = new ArrayList<>();

					if (language == SerializeUtil.Language.JSON) {
						final JSONArray jsonList = JSONParser.deserialize(object.toString(), new JSONArray());

						for (final Object element : jsonList)
							list.add(element == null ? null : JsonItemStack.fromJson(element.toString()));

					} else {
						Valid.checkBoolean(object instanceof List, "When deserializing ItemStack[] from YAML, expected the oject to be a List, but got " + object.getClass().getSimpleName() + ": " + object);
						final List<?> rawList = (List<?>) object;

						for (final Object element : rawList)
							list.add(element == null ? null : SerializeUtil.deserialize(language, ItemStack.class, object));
					}

					return (T) list.toArray(new ItemStack[list.size()]);
				}

				else if (ConfigurationSerializable.class.isAssignableFrom(classOf))
					return (T) object; // Already unpacked in BukkitYamlConstructor

				return null;
			}
		});
	}

	@Override
	public boolean callEvent(final Object event) {
		Valid.checkBoolean(event instanceof Event, "Object must be an instance of Bukkit Event, not " + event.getClass());

		Bukkit.getPluginManager().callEvent((Event) event);
		return event instanceof Cancellable ? !((Cancellable) event).isCancelled() : true;
	}

	@Override
	public void dispatchConsoleCommand(String command) {
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
	}

	@Override
	public List<FoundationPlayer> getOnlinePlayers() {
		final List<FoundationPlayer> players = new ArrayList<>();

		for (final Player player : Remain.getOnlinePlayers())
			players.add(toPlayer(player));

		return players;
	}

	@Override
	public File getPluginFile(String pluginName) {
		final Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
		Valid.checkNotNull(plugin, "Plugin " + pluginName + " not found!");
		Valid.checkBoolean(plugin instanceof JavaPlugin, "Plugin " + pluginName + " is not a JavaPlugin. Got: " + plugin.getClass());

		return (File) ReflectionUtil.invoke(ReflectionUtil.getMethod(JavaPlugin.class, "getFile"), plugin);
	}

	@Override
	public String getPlatformVersion() {
		return Bukkit.getBukkitVersion();
	}

	@Override
	public String getPlatformName() {
		return Bukkit.getName();
	}

	@Override
	public String getNMSVersion() {
		final String packageName = Bukkit.getServer() == null ? "" : Bukkit.getServer().getClass().getPackage().getName();
		final String curr = packageName.substring(packageName.lastIndexOf('.') + 1);

		return !"craftbukkit".equals(curr) && !"".equals(packageName) ? curr : "";
	}

	@Override
	public List<Tuple<String, String>> getServerPlugins() {
		return Common.convert(Bukkit.getPluginManager().getPlugins(), plugin -> new Tuple<>(plugin.getName(), plugin.getDescription().getVersion()));
	}

	@Override
	public boolean hasHexColorSupport() {
		return MinecraftVersion.atLeast(V.v1_16);
	}

	/**
	 * Checks if a plugin is enabled. We also schedule an async task to make
	 * sure the plugin is loaded correctly when the server is done booting
	 * <p>
	 * Return true if it is loaded (this does not mean it works correctly)
	 *
	 * @param name
	 * @return
	 */
	@Override
	public boolean isPluginInstalled(String name) {
		Plugin lookup = null;

		for (final Plugin otherPlugin : Bukkit.getPluginManager().getPlugins())
			if (otherPlugin.getDescription().getName().equals(name)) {
				lookup = otherPlugin;

				break;
			}

		final Plugin found = lookup;

		if (found == null)
			return false;

		if (!found.isEnabled())
			Common.runLaterAsync(0, () -> Valid.checkBoolean(found.isEnabled(),
					SimplePlugin.getInstance().getName() + " could not hook into " + name + " as the plugin is disabled! (DO NOT REPORT THIS TO " + SimplePlugin.getInstance().getName() + ", look for errors above and contact support of '" + name + "')"));

		return true;
	}

	@Override
	public void logToConsole(String message) {
		Bukkit.getConsoleSender().sendMessage(message);
	}

	@Override
	public void registerEvents(final Object listener) {
		Valid.checkBoolean(listener instanceof Listener, "Listener must extend Bukkit's Listener, not " + listener.getClass());

		Bukkit.getPluginManager().registerEvents((Listener) listener, SimplePlugin.getInstance());
	}

	@Override
	public Task runTask(int delayTicks, Runnable runnable) {
		return Common.runLater(delayTicks, runnable);
	}

	@Override
	public Task runTaskAsync(int delayTicks, Runnable runnable) {
		return Common.runLaterAsync(delayTicks, runnable);
	}

	@Override
	public void sendPluginMessage(UUID senderUid, String channel, byte[] array) {
		final Player player = Remain.getPlayerByUUID(senderUid);
		Valid.checkNotNull(player, "Unable to find player by UUID: " + senderUid);

		player.sendPluginMessage(SimplePlugin.getInstance(), channel, array);
	}

	@Override
	public HoverEventSource<?> convertItemStackToHoverEvent(Object itemStack) {
		ValidCore.checkBoolean(itemStack instanceof ItemStack, "Expected item stack, got: " + itemStack);

		return Remain.convertItemStackToHoverEvent((ItemStack) itemStack);
	}

	@Override
	public void checkCommandUse(SimpleCommandCore command) {
		// Navigate developers on proper simple command class usage.
		ValidCore.checkBoolean(!(command instanceof CommandExecutor), "Please do not write 'implements CommandExecutor' for /" + command + " command since it's already registered.");
		ValidCore.checkBoolean(!(command instanceof TabCompleter), "Please do not write 'implements TabCompleter' for /" + command + " command, simply override the tabComplete() method");
	}

	@Override
	public void registerCommand(SimpleCommandCore command, boolean unregisterOldCommand, boolean unregisterOldAliases) {
		final PluginCommand oldCommand = Bukkit.getPluginCommand(command.getLabel());

		if (oldCommand != null && unregisterOldCommand)
			Remain.unregisterCommand(oldCommand.getLabel(), unregisterOldAliases);

		Remain.registerCommand(new BukkitCommandImpl(command));
	}

	@Override
	public void unregisterCommand(SimpleCommandCore command) {
		Remain.unregisterCommand(command.getLabel());
	}

	@Override
	public boolean isPlaceholderAPIHooked() {
		return HookManager.isPlaceholderAPILoaded();
	}

	@Override
	public boolean isAsync() {
		return !Bukkit.isPrimaryThread();
	}

	@Override
	public FoundationPlugin getPlugin() {
		return SimplePlugin.getInstance();
	}

	@Override
	public FoundationPlayer toPlayer(Object sender) {
		if (sender == null)
			throw new FoException("Cannot convert null sender to FoundationPlayer!");

		if (!(sender instanceof CommandSender))
			throw new FoException("Can only convert CommandSender to FoundationPlayer, got " + sender.getClass().getSimpleName() + ": " + sender);

		return new BukkitPlayer((CommandSender) sender);
	}
}