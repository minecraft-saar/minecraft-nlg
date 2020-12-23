package de.saar.coli.minecraft.relationextractor;

import de.saar.coli.minecraft.relationextractor.Relation.Orientation;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Sets;

public class BigBlock extends MinecraftObject {

  public final int x1;
  public final int y1;
  public final int z1;
  public final int x2;
  public final int y2;
  public final int z2;
  String name;

  public static class CoordinatesTuple {

    public final int x1;
    public final int y1;
    public final int z1;
    public final int x2;
    public final int y2;
    public final int z2;

    public CoordinatesTuple(int x1, int y1, int z1, int x2, int y2, int z2, Orientation o) {
      this.y1 = y1;
      this.y2 = y2;

      switch (o) {
        case ZPLUS:
          this.x1 = x1;
          this.z1 = z1;
          this.x2 = x2;
          this.z2 = z2;
          break;
        case ZMINUS:
          this.x1 = -x1;
          this.z1 = -z1;
          this.x2 = -x2;
          this.z2 = -z2;
          break;
        case XPLUS:
          this.x1 = -z1;
          this.x2 = -z2;
          this.z1 = x1;
          this.z2 = x2;
          break;
        case XMINUS:
          this.x1 = z1;
          this.x2 = z2;
          this.z1 = -x1;
          this.z2 = -x2;
          break;
        default:
          // to make the static code analyzer happy as the values are final.
          this.x1 = x1;
          this.x2 = x2;
          this.z1 = z1;
          this.z2 = z2;
          throw new IllegalStateException("Unexpected value: " + o);
      }
    }

    public int getMinX() {
      if (x1 < x2) {
        return x1;
      }
      return x2;
    }
    public int getMaxX() {
      if (x1 > x2) {
        return x1;
      }
      return x2;
    }
    public int getMinY() {
      if (y1 < y2) {
        return y1;
      }
      return y2;
    }
    public int getMaxY() {
      if (y1 > y2) {
        return y1;
      }
      return y2;
    }
    public int getMinZ() {
      if (z1 < z2) {
        return z1;
      }
      return z2;
    }
    public int getMaxZ() {
      if (z1 > z2) {
        return z1;
      }
      return z2;
    }
  }

  // direction of the normal vector
  // public final Direction n;

  /**
   * A cuboid consisting of several blocks.
   * x1, y1, z1 are the minimal coordinates, x2, y2, z2 are the maximal ones.
   */
  public BigBlock(String name, int x1, int y1, int z1, int x2, int y2, int z2) {
    this.name = name;

    this.x1 = x1;
    this.y1 = y1;
    this.z1 = z1;

    this.x2 = x2;
    this.y2 = y2;
    this.z2 = z2;
    children = new HashSet<>();
    for (Block i: getBlocks()) {
      children.add(i);
    }
  }

  private static final Set<EnumSet<Features>> features = Set.of(
      EnumSet.of(Features.TYPE,
          Features.X1,
          Features.Y1,
          Features.Z1,
          Features.X2,
          Features.Y2,
          Features.Z2),
      EnumSet.of(Features.TYPE,
          Features.X1,
          Features.Y1,
          Features.Z1,
          Features.X2,
          Features.HEIGHT,
          Features.Z2)
  );

  /**
   * Returns the features that uniquely describe this type of object.
   */
  public Set<EnumSet<Features>> getFeatures() {
    return features;
  }

  @Override
  public Set<Block> getBlocks() {
    Set<Block> res = new HashSet<>();
    for (int x = x1; x <= x2; x++) {
      for (int y = y1; y <= y2; y++) {
        for (int z = z1; z <= z2; z++) {
          res.add(new Block(x, y, z));
        }
      }
    }
    return res;
  }

  @Override
  public boolean sameShapeAs(MinecraftObject other) {
    if (!(other instanceof BigBlock)) {
      return false;
    }
    BigBlock oblock = (BigBlock) other;
    if (x1 - x2 != oblock.x1 - oblock.x2
        || y1 - y2 != oblock.y1 - oblock.y2
        || z1 - z2 != oblock.z1 - oblock.z2
    ) {
      return false;
    }
    return true;
  }

  public CoordinatesTuple getRotatedCoords(Orientation orientation) {
    return new CoordinatesTuple(x1,y1,z1,x2,y2,z2, orientation);
  }

  @Override
  public MutableSet<Relation> generateRelationsTo(MinecraftObject other, Orientation orientation) {
    MutableSet<Relation> result = Sets.mutable.empty();
    var coord = getRotatedCoords(orientation);
    if ((other instanceof Block)) {
      var oc =  ((Block) other).getRotatedCoords(orientation);
      // from is if block is at minimal positions
      if (oc.x1 == coord.getMaxX() && oc.y1 == coord.getMinY() && oc.z1 == coord.getMinZ()) {
        result.add(new Relation("from",
            this, other));
      }
      if (oc.x1 == coord.getMaxX() && oc.y1 == coord.getMinY() && oc.z1 == coord.getMaxZ()
          && coord.getMaxZ() != coord.getMinZ()) {
        result.add(new Relation("fromaway",
            this, other));
      }
      if (oc.x1 == coord.getMinX() && oc.y1 == coord.getMaxY() && oc.z1 == coord.getMaxZ()) {
        result.add(new Relation("to", this, other));
      }
      if (oc.x1 == coord.getMinX() && oc.y1 == coord.getMaxY() && oc.z1 == coord.getMinZ()
          && coord.getMinZ() != coord.getMaxZ()) {
        result.add(new Relation("tohere", this, other));
      }
      if (oc.x1 == coord.getMinX() && oc.y1 == coord.getMinY() && oc.z1 == coord.getMaxZ()) {
        result.add(new Relation("tobottom", this, other));
      }
      if (oc.x1 == coord.getMaxX() && oc.y1 +1 == coord.getMinY() && oc.z1 == coord.getMinZ()) {
        result.add(new Relation("fromtopof",
            this, Lists.immutable.of(other)));
      }
      if (oc.x1 == coord.getMinX() && oc.y1 +1 == coord.getMaxY() && oc.z1 == coord.getMaxZ()) {
        result.add(new Relation("totopof", this, other));
      }

      // add also left-of-relations for BigBlock-Block relations
      var xdistance = coord.getMinX() - oc.x1;
      var ydistance = coord.getMinY() - oc.y1;
      var zdistance = oc.z1 - coord.z1;

      if (xdistance > 0
          && ydistance == 0
          && zdistance == 0
      ) {
        result.add(new Relation("left-of-BigBlock-Block"+xdistance, this, other));
      }
      if (xdistance == 0
          && ydistance > 0
          && zdistance == 0
      ) {
        result.add(new Relation("top-of-BigBlock-Block"+ydistance, this, other));
      }
      if (xdistance == 0
          && ydistance == 0
          && zdistance > 0
      ) {
        result.add(new Relation("in-front-of-BigBlock-Block"+zdistance, this, other));
      }

    } // end other==Block
    else if (other instanceof BigBlock) {
      var oblock =(BigBlock) other;
      var ocoords = oblock.getRotatedCoords(orientation);

      var xdistance = coord.getMinX() - ocoords.getMinX();
      var ydistance = coord.getMinY() - ocoords.getMaxY();
      var zdistance = ocoords.getMinZ() - coord.getMinZ();

      if (this.sameShapeAs(oblock)) {
        if (xdistance > 0
            && ydistance == 0
            && zdistance == 0
        ) {
          result.add(new Relation("left-of"+xdistance, this, other));
        }
        if (xdistance == 0
            && ydistance > 0
            && zdistance == 0
        ) {
          result.add(new Relation("top-of-same-shape"+ydistance, this, other));
        }
        if (xdistance == 0
            && ydistance == 0
            && zdistance > 0
        ) {
          result.add(new Relation("in-front-of"+zdistance, this, other));
        }
      }
      // if length and width are the same and only height differs
      if (ocoords.x1 - ocoords.x2 == coord.x1 - coord.x2
          && ocoords.z1 - ocoords.z2 == coord.z1 - coord.z2 ) {
        if (xdistance == 0
            && ydistance > 0
            && zdistance == 0
        ) {
          result.add(new Relation("top-of"+ydistance, this, other));
        }
      }
    return result;
  }

  @Override
  public MutableSet<Relation> generateRelationsTo(MinecraftObject other,
      MinecraftObject other2,
      Orientation orientation) {
    // TODO handle orientation
    MutableSet<Relation> result = Sets.mutable.empty();
    // make an otherside relation if this is on the other side of other from other2
    // and other2 has the same shape as other2
    if (other instanceof BigBlock && other2 instanceof BigBlock) {
      BigBlock oblock = (BigBlock) other2;
      BigBlock ofbloc = (BigBlock) other;
      // check whether this and oblock have the same dimensions
      // and the same orientation
      if (this.x1 - this.x2 == oblock.x1 - oblock.x2
          && this.y1 - this.y2 == oblock.y1 - oblock.y2
          && this.z1 - this.z2 == oblock.z1 - oblock.z2
      ) {
        if (this.x1 == ofbloc.x1
            && this.x2 == ofbloc.x1
            && this.z1 == ofbloc.z1
            && this.z2 == ofbloc.z2
            && oblock.x1 == ofbloc.x2
            && oblock.x2 == ofbloc.x2
            && oblock.z1 == ofbloc.z1
            && oblock.z2 == ofbloc.z2) {
          result.add(new Relation("otherside",
              this,
              Lists.immutable.of(ofbloc, oblock)));
        }
      }
    }
    return result;
  }

  @Override
  public String toString() {
    return name + x1 + '-' + y1 + '-' + z1 + '-' + x2+ '-' + y2+ '-' + z2;
  }
}
