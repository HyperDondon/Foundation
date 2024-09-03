package org.mineacademy.fo.settings;

import java.util.LinkedHashMap;
import java.util.Map;

import org.mineacademy.fo.SerializeUtilCore;
import org.mineacademy.fo.SerializeUtilCore.Language;
import org.mineacademy.fo.model.ConfigSerializable;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.exceptions.YamlEngineException;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.representer.StandardRepresenter;

public class PlatformNeutralYamlRepresenter extends StandardRepresenter {

	public PlatformNeutralYamlRepresenter(DumpSettings settings) {
		super(settings);

		this.parentClassRepresenters.put(MemorySection.class, new RepresentMemorySection());
		this.parentClassRepresenters.put(ConfigSerializable.class, new RepresentConfigSerializable());

		// We could just switch YamlConstructor to extend Constructor rather than SafeConstructor, however there is a very small risk of issues with plugins treating config as untrusted input
		// So instead we will just allow future plugins to have their enums extend ConfigurationSerializable
		this.parentClassRepresenters.remove(Enum.class);
	}

	@Override
	public Node represent(Object data) {

		data = SerializeUtilCore.serialize(Language.YAML, data);

		try {
			return super.represent(data);

		} catch (final YamlEngineException ex) {
			if (ex.getMessage().startsWith("Representer is not defined for"))
				throw new YamlEngineException("Does not know how to serialize " + data.getClass() + ", make it implement ConfigSerializable!");

			throw ex;
		}
	}

	// Used by configuration sections that are nested within lists or maps.
	private class RepresentMemorySection extends RepresentMap {

		@Override
		public Node representData(Object data) {
			return super.representData(((MemorySection) data).getValues(false));
		}
	}

	private class RepresentConfigSerializable extends RepresentMap {

		@Override
		public Node representData(Object data) {
			final ConfigSerializable serializable = (ConfigSerializable) data;
			final Map<String, Object> values = new LinkedHashMap<>();

			values.putAll(serializable.serialize().asMap());

			return super.representData(values);
		}
	}
}
