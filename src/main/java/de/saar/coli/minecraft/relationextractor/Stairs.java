package de.saar.coli.minecraft.relationextractor;

import de.saar.coli.minecraft.relationextractor.Relation.Orientation;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Sets;

public class Railing extends MinecraftObject {
  public Row row;
  public Wall lowerWall;
  public Wall higherWall;
  public Set<Block> blocks;

  /**
   * A set of stairs consists if a row and two walls behind the row.
   * @param name internal name of the railing
   * @param x1 one corner of the row together with z1 take minimal value
   * @param z1 one corner of the row together with z1 take minimal value
   * @param x2 other corner of the row with z2
   * @param z2 other corner of the row with z2
   * @param y1 height at which the set of stairs start
   * @param x3 gives upper corner of set of stairs with y3 and z3
   * @param y3 gives upper corner of set of stairs with x3 and z3
   * @param z3 gives upper corner of set of stairs with x3 and y3
   * The corner given by x1 z1 is at height y1 and has the minimal x or z value for the row
   * The corner given by x2 z2 is also at height y1 and has the maximal x or z value for the row
   * the corner given by x3 z3 is at height y3 and is at either equal x or z value to the first
   * corner (this depends on the orientation of the stairs)
   * So if corner 1 and 2 share the x value, corner 1 and 3 share the z value or vice versa
   */
  public Stairs(String name, int x1, int y1, int z1, int x2, int z2, int x3, int y3, int z3) {
    row = new Row("row-" + name, x1, z1, x2, z2, y1);
    if(x1 == x3){
      lowerWall = new Wall("lowerWall-" + name, x1, y1, z1+1, x2, y1+1, z1+1);
      higherWall = new Wall("higherWall-" + name, x1, y1, z3, x2, y3, z3);
    } else if (z1 == z3){
      lowerWall = new Wall("lowerWall-" + name, x1+1, y1, z1, x1+1, y1+1, z2);
      higherWall = new Wall("higherWall-" + name, x3, y1, z1, x3, y3, z2);
    } else {
      throw new RuntimeException("Stairs does neither extend over the x nor the z axis!");
    }
    blocks.addAll(row.getBlocks());
    blocks.addAll(lowerWall.getBlocks());
    blocks.addAll(higherWall.getBlocks());
    children = new HashSet<>();
    children.add(row);
    children.add(lowerWall);
    children.add(higherWall);
  }

  @Override
  public Set<Block> getBlocks() {
    return blocks;
  }

  @Override
  public boolean sameShapeAs(MinecraftObject other) {
    if (!(other instanceof Stairs)) {
      return false;
    }
    Stairs os = (Stairs) other;
    return os.row.sameShapeAs(this.row);
  }


  private static final Set<EnumSet<Features>> features = Set.of(
      EnumSet.of(Features.TYPE,
          Features.X1,
          Features.Y1,
          Features.Z1,
          Features.X2,
          Features.Z2//,
          //Features.X3,
          //Features.Y3,
          //Fetures.Z3
      ),
      EnumSet.of(Features.TYPE,
          Features.X1,
          Features.Y1,
          Features.Z1,
          Features.X2,
          Features.Z2//,
          //Features.X3,
          //Features.HEIGHT,
          //Fetures.Z3
      )
  );

  /**
   * Returns the features that uniquely describe this type of object.
   */
  public Set<EnumSet<Features>> getFeatures() {
    return features;
  }
/*
  @Override
  public MutableSet<Relation> generateRelationsTo(MinecraftObject other, Orientation orientation) {
    MutableSet<Relation> result = Sets.mutable.empty();
    var coord = new BigBlock.CoordinatesTuple(block1.xpos, block1.ypos, block1.zpos,
        block2.xpos, block2.ypos+1, block2.zpos, orientation);
    if (other instanceof Block) {
      var oc = ((Block) other).getRotatedCoords(orientation);
      // railing needs only one from and one to relation
      // use topof-relations because in instructions want to say e.g.
      // "build a railing from the top of the black block to the top of the blue block"
      if (oc.x1 == coord.getMaxX() && oc.y1 +1== coord.getMinY() && oc.z1 == coord.getMinZ()) {
        result.add(new Relation("topof-from-diagonal1",
            this, other));
      }
      if (oc.x1 == coord.getMinX() && oc.y1 +1== coord.getMinY() && oc.z1 == coord.getMaxZ()) {
        result.add(new Relation("topof-to-diagonal1",
            this, other));
      }
    }
    return result;
  } */


  public int height() {
    return higherWall.y2 - higherWall.y1 + 1;
  }

  @Override
  public MutableSet<Relation> generateUnaryRelations(Orientation o) {
    return Sets.mutable.of(
        new Relation("stairs", this),
        new Relation("height" + height(), this)
    );
  }

  @Override
  public String toString() {
    return "Stairs-" + row + '-' + lowerWall + '-' + higherWall;
  }
}
