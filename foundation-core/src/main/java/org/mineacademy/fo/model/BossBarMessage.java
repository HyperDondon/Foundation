package org.mineacademy.fo.model;

import org.mineacademy.fo.ReflectionUtilCore;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.fo.platform.FoundationPlayer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.bossbar.BossBar;

/**
 * Represents a simple boss bar message
 */
@Getter
@RequiredArgsConstructor
public final class BossBarMessage implements ConfigSerializable {

	/**
	 * The bar color
	 */
	private final BossBar.Color color;

	/**
	 * The bar style
	 */
	private final BossBar.Overlay overlay;

	/**
	 * Seconds to show this bar
	 */
	private final int seconds;

	/**
	 * The percentage of this bar
	 */
	private final float progress;

	/**
	 * The message to show
	 */
	private final SimpleComponent message;

	/**
	 * Displays this boss bar to the given player
	 *
	 * @param sender
	 */
	public void displayTo(FoundationPlayer sender) {
		sender.sendBossbarTimed(this.message, this.seconds, this.progress, this.color, this.overlay);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.color + " " + this.overlay + " " + this.seconds + " " + this.message;
	}

	@Override
	public SerializedMap serialize() {
		return SerializedMap.ofArray(
				"Color", this.color,
				"Style", this.overlay,
				"Seconds", this.seconds,
				"Message", this.message);
	}

	public static BossBarMessage deserialize(SerializedMap map) {
		final BossBar.Color color = map.get("Color", BossBar.Color.class);
		final BossBar.Overlay overlay = ReflectionUtilCore.lookupEnum(BossBar.Overlay.class, map.getString("Style"));
		final int seconds = map.getInteger("Seconds");
		final float progress = map.getFloat("Progress", 1F);
		final SimpleComponent message = map.getComponent("Message");

		return new BossBarMessage(color, overlay, seconds, progress, message);
	}
}