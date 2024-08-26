package org.mineacademy.fo.settings;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.constructor.StandardConstructor;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.Node;

public class PlatformNeutralYamlConstructor extends StandardConstructor {

	public PlatformNeutralYamlConstructor(@NotNull LoadSettings loadSettings) {
		super(loadSettings);
	}

	@Override
	public void flattenMapping(@NotNull final MappingNode node) {
		super.flattenMapping(node);
	}

	@Override
	@Nullable
	public Object construct(@NotNull Node node) {
		return constructObject(node);
	}
}
