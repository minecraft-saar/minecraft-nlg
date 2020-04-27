package de.saar.coli.minecraft.relationextractor;

import de.saar.coli.minecraft.relationextractor.Relation.Orientation;
import java.util.EnumSet;
import java.util.Set;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.factory.Sets;

public class Floor extends BigBlock {

  public Floor(String name, int x1, int z1, int x2, int z2, int y) {
    super(name, x1, y, z1, x2, y, z2);
  }

    private static final Set<EnumSet<Features>> features = Set.of(
      EnumSet.of(Features.TYPE,
          Features.X1,
          Features.Y1,
          Features.Z1,
          Features.X2,
          Features.Z2),
      EnumSet.of(Features.TYPE,
          Features.X1,
          Features.Y1,
          Features.Z1,
          Features.WIDTH,
          Features.LENGTH),
        EnumSet.of(Features.TYPE,
            Features.X2,
            Features.Y2,
            Features.Z2,
            Features.WIDTH,
            Features.LENGTH)
  );

  @Override
  public Set<EnumSet<Features>> getFeatures() {
    return features;
  }

  @Override
  public MutableSet<Relation> generateUnaryRelations(Orientation orientation) {
    return Sets.mutable.of(new Relation("floor", this));
  }
}
