package de.saar.coli.minecraft.relationextractor;

import de.saar.coli.minecraft.relationextractor.Relation.Orientation;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.factory.Sets;

public class Wall extends BigBlock {

  /**
   * Returns the features that uniquely describe this type of object.
   */
  public Set<EnumSet<Features>> getFeatures() {
    if (x1 != x2) {
      // Wall is along X axis
      return(Set.of(
          EnumSet.of(Features.TYPE,
              Features.X1,
              Features.Y1,
              Features.Z1,
              Features.X2,
              Features.HEIGHT
              ),
          EnumSet.of(Features.TYPE,
              Features.X1,
              Features.Y1,
              Features.Z1,
              Features.X2,
              Features.Y2
          )));
    } else {
      // Wall is along Z axis
      return(Set.of(
          EnumSet.of(Features.TYPE,
              Features.X1,
              Features.Y1,
              Features.Z1,
              Features.HEIGHT,
              Features.Z2),
          EnumSet.of(Features.TYPE,
              Features.X1,
              Features.Y1,
              Features.Z1,
              Features.Y2,
              Features.Z2
          )));
    }
  }


  /**
   * A wall is a special cuboid with thickness == 1, standing upright.
   */
  public Wall(String name, int x1, int y1, int z1, int x2, int y2, int z2) {
    super(name, x1, y1, z1, x2, y2, z2);

    /* Assumptions about walls:
     * The have a width of 1
     * Have width and height > 1
     * Therefore: x1 == x2 xor z1 == z2
     * A wall needs a height, otherwise it is just a row (TODO discuss)
     */

    if (x1 == x2 && z1 == z2) {
      throw new RuntimeException("Wall does neither extend over the x nor the z axis!");
    }
    if (y1 == y2) {
      throw new RuntimeException("Wall has height of 1, this is not a proper wall");
    }
  }

  public int height() {
    return y2 - y1 + 1;
  }

  /**
   * returns the width along the X axis or the Y axis, whichever the wall is built along.
   */
  public int width() {
    if (x1 == x2) {
      return z2 - z1 + 1;
    }
    return x2 - x1 + 1;
  }

  @Override
  public boolean sameShapeAs(MinecraftObject other) {
    if (!(other instanceof Wall)) {
      return false;
    }
    Wall owall = (Wall) other;
    if (height() != owall.height() || width() != owall.width()) {
      return false;
    }
    return true;
  }

  @Override
  public MutableSet<Relation> generateUnaryRelations(Orientation o) {
    var result = Sets.mutable.of(
        new Relation("wall", this),
        new Relation("height" + height(), this),
        new Relation("length" + width(), this)
    );
    var coords = getRotatedCoords(o);
    if (coords.x1 != coords.x2) {
      result.add(new Relation("orientleftright", this));
    } else {
      result.add(new Relation("orientaway", this));
    }
    return result;
  }

  public static Wall parseObject(String objectDescription) {
    BigBlock bb = BigBlock.parseObject(objectDescription);
    return new Wall(bb.name, bb.x1, bb.y1, bb.z1, bb.x2, bb.y2, bb.z2);
  }
}
