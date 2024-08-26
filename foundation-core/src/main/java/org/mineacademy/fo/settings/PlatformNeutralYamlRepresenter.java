package org.mineacademy.fo.settings;

import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.representer.StandardRepresenter;

public class PlatformNeutralYamlRepresenter extends StandardRepresenter {

	public PlatformNeutralYamlRepresenter(DumpSettings settings) {
		super(settings);

		this.parentClassRepresenters.put(ConfigSection.class, new RepresentConfigurationSection());
	}

	private class RepresentConfigurationSection extends RepresentMap {

		@Override
		public Node representData(Object data) {
			return super.representData(((ConfigSection) data).getValues(false));
		}
	}
}
