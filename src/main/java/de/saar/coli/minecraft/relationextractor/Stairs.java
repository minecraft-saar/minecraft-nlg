package de.saar.coli.minecraft.relationextractor;

import de.saar.coli.minecraft.relationextractor.Relation.Orientation;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Sets;

public class Stairs extends MinecraftObject {
  public Row row;
  public Wall lowerWall;
  public Wall higherWall;
  public Set<Block> blocks;

  /**
   * A set of stairs consists if a row and two walls behind the row.
   *
   * The blocks (x1,y1,z1) and (x2,y1,z2) are the endpoints of the row,
   * i.e. of the staircase step of height one. If x1 = x2, the steps are
   * parallel to the z-axis; if z1 = z2, the steps are parallel to the x-axis.
   * Either x1 or z1 is lower than x2 or z2, respectively.
   *
   * x3,y3,z3 is one of the endpoints at the top of the highest step of the staircase,
   * i.e. y3-y1 = 2 (= the height of the staircase). Depending on the orientation
   * of the staircase, either x3 = x1 or z3 = z1; the other coordinate is two higher
   * than the respective coordinate of x1,z1 (= depth of the staircase).
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
    blocks = new HashSet<>();
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
          Features.HEIGHT,
          Features.X1, Features.Z1, // one corner
          Features.Z2
//          Features.X1,
//          Features.Y1,
//          Features.Z1,
//          Features.X2,
//          Features.Z2


          //,
          //Features.X3,
          //Features.Y3,
          //Fetures.Z3
      )/*,
      EnumSet.of(Features.TYPE,
          Features.X1,
          Features.Y1,
          Features.Z1,
          Features.X2,
          Features.Z2//,
          //Features.X3,
          //Features.HEIGHT,
          //Fetures.Z3
      )*/
  );

  /**
   * Returns the features that uniquely describe this type of object.
   */
  public Set<EnumSet<Features>> getFeatures() {
    return features;
  }

  @Override
  public MutableSet<Relation> generateRelationsTo(MinecraftObject other, Orientation orientation) {
    MutableSet<Relation> result = Sets.mutable.empty();
    /*
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
    }*/
    return result;
  }


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


  private static final Pattern PARSING_PATTERN = Pattern.compile("(.*)-(row.*)-(lowerWall.*)-(higherWall.*)");

  protected static Stairs parseObject(String objectDescription) {
    Matcher m = PARSING_PATTERN.matcher(objectDescription);

    if( m.matches() ) {
      String name = m.group(1);
      Row row = Row.parseObject(m.group(2));
      Wall lowerWall = Wall.parseObject(m.group(3));
      Wall higherWall = Wall.parseObject(m.group(4));

      if( lowerWall.z1 == lowerWall.z2 && higherWall.z1 == higherWall.z2 ) {
        // case "x1 == x3" in constructor
        return new Stairs(name, row.x1, row.y1, row.z1, row.x2, row.z2, row.x1, higherWall.y2, higherWall.z2);
      } else if( lowerWall.x1 == lowerWall.x2 && higherWall.x1 == higherWall.x2 ) {
        // case "z1 == z3" in constructor
        return new Stairs(name, row.x1, row.y1, row.z1, row.x2, row.z2, higherWall.x1, higherWall.y2, row.z1);
      } else {
        throw new UnsupportedOperationException("Stairs do not extend over the x or z axis!");
      }
    } else {
      return null;
    }
  }
}
