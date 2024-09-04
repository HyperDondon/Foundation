package org.mineacademy.fo.settings;

import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.exceptions.YamlEngineException;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.nodes.Tag;

public class BukkitYamlConstructor extends YamlConfig.YamlConstructor {

	public BukkitYamlConstructor(LoadSettings loadSettings) {
		super(loadSettings);

		this.tagConstructors.put(Tag.MAP, new ConstructCustomObject());
	}

	private class ConstructCustomObject extends ConstructYamlMap {

		@Override
		public Object construct(Node node) {
			if (node.isRecursive())
				throw new YamlEngineException("Unexpected referential mapping structure. Node: " + node);

			final Map<?, ?> raw = (Map<?, ?>) super.construct(node);

			if (raw.containsKey(ConfigurationSerialization.SERIALIZED_TYPE_KEY)) {
				final Map<String, Object> typed = new LinkedHashMap<>(raw.size());

				for (final Map.Entry<?, ?> entry : raw.entrySet())
					typed.put(entry.getKey().toString(), entry.getValue());

				try {
					return ConfigurationSerialization.deserializeObject(typed);

				} catch (final IllegalArgumentException ex) {
					throw new YamlEngineException("Could not deserialize object", ex);
				}
			}

			return raw;
		}

		@Override
		public void constructRecursive(Node node, Object object) {
			throw new YamlEngineException("Unexpected referential mapping structure. Node: " + node);
		}
	}
}
