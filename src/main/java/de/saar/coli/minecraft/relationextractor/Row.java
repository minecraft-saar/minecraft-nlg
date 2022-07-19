package de.saar.coli.minecraft.relationextractor;

import de.saar.coli.minecraft.relationextractor.Relation.Orientation;
import java.util.EnumSet;
import java.util.Set;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.factory.Sets;

public class Row extends BigBlock {
  public Row(String name, int x1, int z1, int x2, int z2, int y, String type) {
    super(name, x1, y, z1, x2, y, z2, type);
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
          Features.LENGTH,
          Features.ORIENTATION),
      EnumSet.of(Features.TYPE,
          Features.X2,
          Features.Y2,
          Features.Z2,
          Features.LENGTH,
          Features.ORIENTATION)
  );

  public static Row parseObject(String objectDescription, String type) {
    BigBlock bb = BigBlock.parseObject(objectDescription, type);
    return new Row(bb.name, bb.x1, bb.z1, bb.x2, bb.z2, bb.y1, type);
  }

  @Override
  public Set<EnumSet<Features>> getFeatures() {
    return features;
  }

  @Override
  public MutableSet<Relation> generateOwnUnaryRelations(Orientation o) {
    var coords = getRotatedCoords(o);
    Relation orientation;
    var result = Sets.mutable.of(
        new Relation("row", this)
    );
    if (coords.x1 != coords.x2) {
      result.add(new Relation("orientleftright", this));
    } else {
      result.add(new Relation("orientaway", this));
    }
    int length = Integer.max(
        coords.getMaxX() - coords.getMinX(),
        coords.getMaxZ() - coords.getMinZ()
    ) + 1;
    result.add(new Relation("length"+length, this));
    return result;
  }
}
