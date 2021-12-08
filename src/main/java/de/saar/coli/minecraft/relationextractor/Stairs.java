package de.saar.coli.minecraft.relationextractor;

import de.saar.coli.minecraft.relationextractor.BigBlock.CoordinatesTuple;
import de.saar.coli.minecraft.relationextractor.Relation.Orientation;
import java.awt.Point;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Sets;

public class Stairs extends MinecraftObject {

  private Row row;
  private Wall lowerWall;
  private Wall higherWall;
  private Set<Block> blocks;

  // Each diagonal goes from one corner of the lowest step to a block at the same Y-level
  // below a corner of the highest step. There are two of these, so we have an array of size 2.
  private BigBlock.CoordinatesTuple[] baselineDiagonal;


  /**
   * A set of stairs consists if a row and two walls behind the row.
   *
   * The blocks (x1,y1,z1) and (x2,y1,z2) are the endpoints of the row, i.e. of the staircase step
   * of height one. If x1 = x2, the steps are parallel to the z-axis; if z1 = z2, the steps are
   * parallel to the x-axis. Either x1 or z1 is lower than x2 or z2, respectively.
   *
   * x3,y3,z3 is one of the endpoints at the top of the highest step of the staircase, i.e. y3-y1 =
   * 2 (= the height of the staircase). Depending on the orientation of the staircase, either x3 =
   * x1 or z3 = z1; the other coordinate is two higher than the respective coordinate of x1,z1 (=
   * depth of the staircase).
   */
  public Stairs(String name, int x1, int y1, int z1, int x2, int z2, int x3, int y3, int z3) {
    row = new Row("row-" + name, x1, z1, x2, z2, y1);

    if (x1 == x3) {
      // steps are parallel to the z-axis
      if (z1 < z3) {
        lowerWall = new Wall("lowerWall-" + name, x1, y1, z1 + 1, x2, y1 + 1, z1 + 1);
      } else {
        lowerWall = new Wall("lowerWall-" + name, x1, y1, z1 - 1, x2, y1 + 1, z1 - 1);
      }
      higherWall = new Wall("higherWall-" + name, x1, y1, z3, x2, y3, z3);

      BigBlock.CoordinatesTuple diagonal1 = new CoordinatesTuple(x1, y1, z1, x2, y1, z3);
      BigBlock.CoordinatesTuple diagonal2 = new CoordinatesTuple(x2, y1, z2, x1, y1, z3);
      baselineDiagonal = new CoordinatesTuple[]{diagonal1, diagonal2};
    } else if (z1 == z3) {
      // steps are parallel to the x-axis
      if (x1 < x3) {
        lowerWall = new Wall("lowerWall-" + name, x1 + 1, y1, z1, x1 + 1, y1 + 1, z2);
      } else {
        lowerWall = new Wall("lowerWall-" + name, x1 - 1, y1, z1, x1 - 1, y1 + 1, z2);
      }
      higherWall = new Wall("higherWall-" + name, x3, y1, z1, x3, y3, z2);

      BigBlock.CoordinatesTuple diagonal1 = new CoordinatesTuple(x1, y1, z1, x3, y1, z2);
      BigBlock.CoordinatesTuple diagonal2 = new CoordinatesTuple(x2, y1, z2, x3, y1, x3);
      baselineDiagonal = new CoordinatesTuple[]{diagonal1, diagonal2};
    } else {
      throw new RuntimeException("Stairs does neither extend over the x nor the z axis!");
    }

    // add blocks
    blocks = new HashSet<>();
    blocks.addAll(row.getBlocks());
    blocks.addAll(lowerWall.getBlocks());
    blocks.addAll(higherWall.getBlocks());

    // add child high-level objects
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
          Features.X2, Features.Z2
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

    if ((other instanceof Block)) {
      Block.CoordinatesTuple blockCoordsUnrotated = ((Block) other).getRotatedCoords(
          Orientation.ZPLUS); // unrotated coordinates
      Block.CoordinatesTuple blockCoordinatesRotated = ((Block) other).getRotatedCoords(
          orientation);

//      System.err.printf("Compare to block %s at %s\n", other, blockCoordsUnrotated);

      for (int i = 0; i < 2; i++) {
        BigBlock.CoordinatesTuple diagonal = baselineDiagonal[i].orient(orientation);
        int ix = i + 1;
//        System.err.printf("Diagonal %d at %s\n", ix, baselineDiagonal[i]);

        if (blockCoordinatesRotated.matchesLeftCoordinates(diagonal)) {
          result.add(new Relation("from-diagonal" + ix, this, other));
        } else if (blockCoordinatesRotated.matchesRightCoordinates(diagonal)) {
          result.add(new Relation("to-diagonal" + ix, this, other));
        }

        // It is also possible to describe a diagonal by saying "from top of the xy block to ..."
        // or by saying "from .. to the top of the xy block" (cf. BigBlock#generateRelationsTo).
        Block.CoordinatesTuple oneAboveBlock = new Block.CoordinatesTuple(blockCoordsUnrotated.x1,
            blockCoordsUnrotated.y1 + 1, blockCoordsUnrotated.z1, orientation);

        // TODO This is probably incorrect; for the stairs, it matters which corner is "from" and which is "to"
        // (because "from" should be the lower end), and so we need to double-check the grammar to make
        // sure that topof-diagonal is used correctly.
        if (oneAboveBlock.matchesLeftCoordinates(diagonal)) {
          result.add(new Relation("topof-diagonal" + ix, this, other));
        } else if (oneAboveBlock.matchesRightCoordinates(diagonal)) {
          result.add(new Relation("topof-diagonal" + ix, this, other));
        }
      }


    }

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


  private static final Pattern PARSING_PATTERN = Pattern.compile(
      "(.*)-(row.*)-(lowerWall.*)-(higherWall.*)");

  protected static Stairs parseObject(String objectDescription) {
    Matcher m = PARSING_PATTERN.matcher(objectDescription);

    if (m.matches()) {
      String name = m.group(1);
      Row row = Row.parseObject(m.group(2));
      Wall lowerWall = Wall.parseObject(m.group(3));
      Wall higherWall = Wall.parseObject(m.group(4));

      if (lowerWall.z1 == lowerWall.z2 && higherWall.z1 == higherWall.z2) {
        // case "x1 == x3" in constructor
        return new Stairs(name, row.x1, row.y1, row.z1, row.x2, row.z2, row.x1, higherWall.y2,
            higherWall.z2);
      } else if (lowerWall.x1 == lowerWall.x2 && higherWall.x1 == higherWall.x2) {
        // case "z1 == z3" in constructor
        return new Stairs(name, row.x1, row.y1, row.z1, row.x2, row.z2, higherWall.x1,
            higherWall.y2, row.z1);
      } else {
        throw new UnsupportedOperationException("Stairs do not extend over the x or z axis!");
      }
    } else {
      return null;
    }
  }
}
