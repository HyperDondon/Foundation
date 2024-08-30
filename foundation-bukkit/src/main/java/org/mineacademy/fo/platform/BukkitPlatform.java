package org.mineacademy.fo.platform;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
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
import org.mineacademy.fo.SerializeUtilCore.Mode;
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
import org.mineacademy.fo.model.Variables;
import org.mineacademy.fo.plugin.BukkitVariableCollector;
import org.mineacademy.fo.plugin.SimplePlugin;
import org.mineacademy.fo.remain.CompEnchantment;
import org.mineacademy.fo.remain.CompPotionEffectType;
import org.mineacademy.fo.remain.JsonItemStack;
import org.mineacademy.fo.remain.Remain;
import org.mineacademy.fo.settings.YamlConfig;

import net.kyori.adventure.text.event.HoverEventSource;

public class BukkitPlatform extends FoundationPlatform {

	public BukkitPlatform() {
		this.inject();
	}

	private void inject() {

		// Inject Yaml
		YamlConfig.setCustomConstructor(settings -> new BukkitYamlConstructor(settings));
		YamlConfig.setCustomRepresenter(settings -> new BukkitYamlRepresenter(settings));

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

		SerializeUtil.addCustomSerializer(Location.class, new Serializer<Location>() {

			@Override
			public Object serialize(Mode mode, Location object) {
				return SerializeUtil.serializeLoc(object);
			}

			@Override
			public Location deserialize(Mode mode, Object object) {
				return SerializeUtil.deserializeLocation((String) object);
			}
		});

		SerializeUtil.addCustomSerializer(World.class, new Serializer<World>() {

			@Override
			public Object serialize(Mode mode, World object) {
				return object.getName();
			}

			@Override
			public World deserialize(Mode mode, Object object) {
				final World world = Bukkit.getWorld((String) object);
				Valid.checkNotNull(world, "World " + object + " not found. Available: " + Bukkit.getWorlds());

				return world;
			}
		});

		SerializeUtil.addCustomSerializer(PotionEffectType.class, new Serializer<PotionEffectType>() {

			@Override
			public Object serialize(Mode mode, PotionEffectType object) {
				return object.getName();
			}

			@Override
			public PotionEffectType deserialize(Mode mode, Object object) {
				final PotionEffectType type = CompPotionEffectType.getByName((String) object);
				Valid.checkNotNull(type, "Potion effect type " + object + " not found. Available: " + CompPotionEffectType.getPotionNames());

				return type;
			}
		});

		SerializeUtil.addCustomSerializer(PotionEffect.class, new Serializer<PotionEffect>() {

			@Override
			public Object serialize(Mode mode, PotionEffect object) {
				return object.getType().getName() + " " + object.getDuration() + " " + object.getAmplifier();
			}

			@Override
			public PotionEffect deserialize(Mode mode, Object object) {
				final String[] parts = object.toString().split(" ");
				ValidCore.checkBoolean(parts.length == 3, "Expected PotionEffect (String) but got " + object.getClass().getSimpleName() + ": " + object);

				final String typeRaw = parts[0];
				final PotionEffectType type = PotionEffectType.getByName(typeRaw);

				final int duration = Integer.parseInt(parts[1]);
				final int amplifier = Integer.parseInt(parts[2]);

				return new PotionEffect(type, duration, amplifier);
			}
		});

		SerializeUtil.addCustomSerializer(Enchantment.class, new Serializer<Enchantment>() {

			@Override
			public Object serialize(Mode mode, Enchantment object) {
				return object.getName();
			}

			@Override
			public Enchantment deserialize(Mode mode, Object object) {
				final Enchantment enchant = CompEnchantment.getByName((String) object);
				Valid.checkNotNull(enchant, "Enchantment " + object + " not found. Available: " + CompEnchantment.getEnchantmentNames());

				return enchant;
			}
		});

		SerializeUtil.addCustomSerializer(SimpleSound.class, new Serializer<SimpleSound>() {

			@Override
			public Object serialize(Mode mode, SimpleSound object) {
				return object.toString();
			}

			@Override
			public SimpleSound deserialize(Mode mode, Object object) {
				return new SimpleSound((String) object);
			}
		});

		SerializeUtil.addCustomSerializer(ItemStack.class, new Serializer<ItemStack>() {

			@Override
			public Object serialize(Mode mode, ItemStack object) {
				if (mode == Mode.JSON)
					return JsonItemStack.toJson(object);
				else
					return object;
			}

			@Override
			public ItemStack deserialize(Mode mode, Object object) {
				if (object instanceof ItemStack)
					return (ItemStack) object;

				if (mode == Mode.JSON)
					return JsonItemStack.fromJson(object.toString());

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

					return item;
				}
			}
		});

		SerializeUtil.addCustomSerializer(ItemStack[].class, new Serializer<ItemStack[]>() {

			@Override
			public Object serialize(Mode mode, ItemStack[] object) {
				if (mode == SerializeUtil.Mode.JSON) {
					final JSONArray jsonList = new JSONArray();

					for (final ItemStack item : object)
						jsonList.add(item == null ? null : JsonItemStack.toJson(item));

					return jsonList.toJson();

				} else
					throw new FoException("Cannot deserialize non-JSON ItemStack[]");
			}

			@Override
			public ItemStack[] deserialize(Mode mode, Object object) {

				if (object instanceof ItemStack[])
					return (ItemStack[]) object;

				final List<ItemStack> list = new ArrayList<>();

				if (mode == SerializeUtil.Mode.JSON) {
					final JSONArray jsonList = JSONParser.deserialize(object.toString(), new JSONArray());

					for (final Object element : jsonList)
						list.add(element == null ? null : JsonItemStack.fromJson(element.toString()));
				} else
					throw new FoException("Cannot deserialize non-JSON ItemStack[]");

				return list.toArray(new ItemStack[list.size()]);
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
	public List<String> getServerPlugins() {
		return Common.convert(Bukkit.getPluginManager().getPlugins(), Plugin::getName);
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