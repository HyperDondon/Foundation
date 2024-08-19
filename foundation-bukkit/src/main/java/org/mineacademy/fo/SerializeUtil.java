package org.mineacademy.fo;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.mineacademy.fo.exception.InvalidWorldException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SerializeUtil extends SerializeUtilCore {

	/**
	 * Converts a {@link Location} into "world x y z yaw pitch" string
	 *
	 * @param loc
	 * @return
	 */
	public static String serializeLoc(final Location loc) {
		return loc.getWorld().getName() + " " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ() + (loc.getPitch() != 0F || loc.getYaw() != 0F ? " " + Math.round(loc.getYaw()) + " " + Math.round(loc.getPitch()) : "");
	}

	/**
	 * Converts a {@link Location} into "world x y z yaw pitch" string with decimal support
	 * Unused, you have to call this in your save() method otherwise we remove decimals and use the above method
	 *
	 * @param loc
	 * @return
	 */
	/*private static String serializeLocD(final Location loc) {
		return loc.getWorld().getName() + " " + loc.getX() + " " + loc.getY() + " " + loc.getZ() + (loc.getPitch() != 0F || loc.getYaw() != 0F ? " " + loc.getYaw() + " " + loc.getPitch() : "");
	}*/

	/**
	 * Converts a string into location
	 *
	 * @param line
	 * @return
	 */
	public static Location deserializeLocation(String line) {
		if (line == null)
			return null;

		line = line.toString().replace("\"", "");

		final String[] parts = line.toString().contains(", ") ? line.toString().split(", ") : line.toString().split(" ");
		ValidCore.checkBoolean(parts.length == 4 || parts.length == 6, "Expected location (String) but got " + line.getClass().getSimpleName() + ": " + line);

		final String world = parts[0];
		final World bukkitWorld = Bukkit.getWorld(world);
		if (bukkitWorld == null)
			throw new InvalidWorldException("Location with invalid world '" + world + "': " + line + " (Doesn't exist)", world);

		final double x = Double.parseDouble(parts[1]), y = Double.parseDouble(parts[2]), z = Double.parseDouble(parts[3]);
		final float yaw = Float.parseFloat(parts.length == 6 ? parts[4] : "0"), pitch = Float.parseFloat(parts.length == 6 ? parts[5] : "0");

		return new Location(bukkitWorld, x, y, z, yaw, pitch);
	}

	/**
	 * Converts a string into a location with decimal support
	 * Unused but you can use this for your own parser storing exact decimals
	 *
	 * @param raw
	 * @return
	 */
	/*private static Location deserializeLocationD(Object raw) {
		if (raw == null)
			return null;

		if (raw instanceof Location)
			return (Location) raw;

		raw = raw.toString().replace("\"", "");

		final String[] parts = raw.toString().contains(", ") ? raw.toString().split(", ") : raw.toString().split(" ");
		ValidCore.checkBoolean(parts.length == 4 || parts.length == 6, "Expected location (String) but got " + raw.getClass().getSimpleName() + ": " + raw);

		final String world = parts[0];
		final World bukkitWorld = Bukkit.getWorld(world);

		if (bukkitWorld == null)
			throw new InvalidWorldException("Location with invalid world '" + world + "': " + raw + " (Doesn't exist)", world);

		final double x = Double.parseDouble(parts[1]), y = Double.parseDouble(parts[2]), z = Double.parseDouble(parts[3]);
		final float yaw = Float.parseFloat(parts.length == 6 ? parts[4] : "0"), pitch = Float.parseFloat(parts.length == 6 ? parts[5] : "0");

		return new Location(bukkitWorld, x, y, z, yaw, pitch);
	}*/
}
