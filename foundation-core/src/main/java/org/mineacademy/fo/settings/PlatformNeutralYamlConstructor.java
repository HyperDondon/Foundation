package org.mineacademy.fo.settings;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.constructor.ConstructScalar;
import org.snakeyaml.engine.v2.constructor.StandardConstructor;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.nodes.Tag;

public class PlatformNeutralYamlConstructor extends StandardConstructor {

	public PlatformNeutralYamlConstructor(@NotNull LoadSettings loadSettings) {
		super(loadSettings);

		this.tagConstructors.put(Tag.COMMENT, new ConstructComment());
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

	private static class ConstructComment extends ConstructScalar {
		@Override
		public Object construct(Node node) {
			// Handle the comment node - For now, we'll just return null or some marker.
			return null;
		}
	}
}
