package de.saar.coli.minecraft.relationextractor;

import de.saar.coli.minecraft.relationextractor.Relation.Orientation;
import java.util.EnumSet;
import java.util.Set;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.factory.Lists;
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

  @Override
  public MutableSet<Relation> generateRelationsTo(MinecraftObject other, Orientation orientation) {
    MutableSet<Relation> result = Sets.mutable.empty();
    var coord = getRotatedCoords(orientation);
    if ((other instanceof Block)) {
      var oc =  ((Block) other).getRotatedCoords(orientation);

      // from is if block is at minimal positions

      /*
       * diagonal 1 runs from front left to back right corner
       * diagonal 2 runs from front right corner to back left corner
       */

      if (oc.x1 == coord.getMaxX() && oc.y1 == coord.getMinY() && oc.z1 == coord.getMinZ()) {
        result.add(new Relation("from-diagonal1",
            this, other));
      }
      if (oc.x1 == coord.getMinX() && oc.y1 == coord.getMinY() && oc.z1 == coord.getMinZ()
          && coord.getMaxZ() != coord.getMinZ()) {
        result.add(new Relation("from-diagonal2",
            this, other));
      }
      if (oc.x1 == coord.getMinX() && oc.y1 == coord.getMaxY() && oc.z1 == coord.getMaxZ()) {
        result.add(new Relation("to-diagonal1", this, other));
      }
      if (oc.x1 == coord.getMaxX() && oc.y1 == coord.getMaxY() && oc.z1 == coord.getMaxZ()
          && coord.getMinZ() != coord.getMaxZ()) {
        result.add(new Relation("to-diagonal2", this, other));
      }
      if (oc.x1 == coord.getMinX() && oc.y1 +1 == coord.getMaxY() && oc.z1 == coord.getMaxZ()) {
        result.add(new Relation("topof-to-diagonal1",
            this, Lists.immutable.of(other)));
      }
      if (oc.x1 == coord.getMaxX() && oc.y1 +1 == coord.getMaxY() && oc.z1 == coord.getMinZ()) {
        result.add(new Relation("topof-from-diagonal1",
            this, Lists.immutable.of(other)));
      }
      if (oc.x1 == coord.getMaxX() && oc.y1 +1== coord.getMaxY() && oc.z1 == coord.getMaxZ()
          && coord.getMinZ() != coord.getMaxZ()) {
        result.add(new Relation("topof-to-diagonal2", this, other));
      }
      if (oc.x1 == coord.getMinX() && oc.y1 == coord.getMinY() && oc.z1 == coord.getMinZ()
          && coord.getMaxZ() != coord.getMinZ()) {
        result.add(new Relation("topof-from-diagonal2",
            this, other));
      }

      // add also left-of-relations for BigBlock-Block relations
      var xdistance = coord.getMinX() - oc.x1;
      var ydistance = coord.getMinY() - oc.y1;
      var zdistance = oc.z1 - coord.getMaxZ();

      if (xdistance > 0
          && ydistance == 0
          && zdistance == 0
      ) {
        result.add(new Relation("left-of"+xdistance+"-BigBlock-Block", this, other));
      }
      if (xdistance == 0
          && ydistance > 0
          && zdistance == 0
      ) {
        result.add(new Relation("top-of"+ydistance+"-BigBlock-Block", this, other));
      }
      if (xdistance == 0
          && ydistance == 0
          && zdistance > 0
      ) {
        result.add(new Relation("in-front-of"+zdistance+"-BigBlock-Block", this, other));
      }

    } // end other==Block
    else if (other instanceof BigBlock) {
      var oblock = (BigBlock) other;
      var ocoords = oblock.getRotatedCoords(orientation);

      var xdistance = coord.getMinX() - ocoords.getMaxX();
      var ydistance = coord.getMinY() - ocoords.getMaxY();
      var zdistance = ocoords.getMinZ() - coord.getMaxZ();

      if (this.sameShapeAs(oblock)) {

        if (coord.getMinX() == ocoords.getMinX()
            && ydistance > 0
            && ocoords.getMinZ() == coord.getMinZ()
        ) {
          result.add(new Relation("top-of-same-shape" + ydistance, this, other));
        }
        if (xdistance > 0
            && ocoords.getMinY() == coord.getMinY() //don't care about y distance, but about minimal y-coordinate being the same
            && ocoords.getMinZ() == coord.getMinZ()
        ) {
          result.add(new Relation("left-of" + xdistance, this, other));
        }
        if (coord.getMinX() == ocoords.getMinX()
            && ocoords.getMinY() == coord.getMinY() //don't care about y distance, but about minimal y-coordinate being the same
            && zdistance > 0
        ) {
          result.add(new Relation("in-front-of" + zdistance, this, other));
        }
      }
      // if length and width are the same and only height differs
      if (ocoords.x1 - ocoords.x2 == coord.x1 - coord.x2
          && ocoords.z1 - ocoords.z2 == coord.z1 - coord.z2 ) {
        if (coord.getMinX() == ocoords.getMinX()
            && ydistance > 0
            && ocoords.getMinZ() == coord.getMinZ()
        ) {
          result.add(new Relation("top-of"+ydistance, this, other));
        }
      }
    }
    return result;
  }
}
