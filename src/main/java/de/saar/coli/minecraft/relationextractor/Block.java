package de.saar.coli.minecraft.relationextractor;

import de.saar.coli.minecraft.relationextractor.Relation.Orientation;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Sets;

public class Block extends MinecraftObject {

  public final int xpos;
  public final int ypos;
  public final int zpos;
  private boolean uniqueType;

  public static class CoordinatesTuple {

    public final int x1;
    public final int y1;
    public final int z1;

    public CoordinatesTuple(int x1, int y1, int z1) {
      this(x1, y1, z1, Orientation.ZPLUS);
    }

    public CoordinatesTuple(int x1, int y1, int z1, Orientation o) {
      this.y1 = y1;

      switch (o) {
        case ZPLUS:
          this.x1 = x1;
          this.z1 = z1;
          break;
        case ZMINUS:
          this.x1 = -x1;
          this.z1 = -z1;
          break;
        case XPLUS:
          this.x1 = -z1;
          this.z1 = x1;
          break;
        case XMINUS:
          this.x1 = z1;
          this.z1 = -x1;
          break;
        default:
          // to make the static code analyzer happy as the values are final.
          this.x1 = x1;
          this.z1 = z1;
          throw new IllegalStateException("Unexpected value: " + o);
      }
    }

    public boolean matchesLeftCoordinates(BigBlock.CoordinatesTuple tuple) {
      return x1 == tuple.x1 && y1 == tuple.y1 && z1 == tuple.z1;
    }

    public boolean matchesRightCoordinates(BigBlock.CoordinatesTuple tuple) {
      return x1 == tuple.x2 && y1 == tuple.y2 && z1 == tuple.z2;
    }
  }

  public CoordinatesTuple getRotatedCoords(Orientation orientation) {
    return new CoordinatesTuple(xpos,ypos,zpos, orientation);
  }

  /**
   * A block is the basic building block (hah) of the Minecraft domain.
   * @param x It's xpos coordinate
   * @param y It's ypos coordinate
   * @param z It's zpos coordinate
   */
  public Block(int x, int y, int z, String type) {
    super(type);

    children = new HashSet<>();
    this.xpos = x;
    this.ypos = y;
    this.zpos = z;
  }

  @Override
  public String getVerb() {
    return "put";
  }

  //TODO: this is useless, as we can just determine this with size_1, isn't it?
  public void setUnique() {
    uniqueType = true;
  }

  public void setNotUnique() {
    uniqueType = false;
  }

  public String getType() {
    return type;
  }

  @Override
  public Set<Block> getBlocks() {
    Set<Block> res = new HashSet<>();
    res.add(this);
    return res;
  }

  @Override
  public boolean sameShapeAs(MinecraftObject other) {
    // TODO: Blocks don't need shape description, but does this mean we can just return false?
    return false;
    // return other instanceof Block;
  }

  @Override
  public MutableSet<Relation> generateOwnUnaryRelations(Orientation o) {
    var result = Sets.mutable.of(new Relation("block", this));
    if (uniqueType) {
      result.add(new Relation("unique", this));
    } 
    return result;
  }

  @Override
  public boolean equals(Object o) { //TODO: a bit hacky .equal(UniqueBlock) different to .equal(Block)
    if (this == o) {
      return true;
    }
    if (o == null || !(o.getClass() == Block.class || o.getClass() == WildcardBlock.class)) {
      return false;
    }
    Block block = (Block) o;
    return xpos == block.xpos &&
        ypos == block.ypos &&
        zpos == block.zpos;
  }

  @Override
  public int hashCode() {
    return Objects.hash(xpos, ypos, zpos);
  }

  @Override
  public MutableSet<Relation> generateRelationsTo(MinecraftObject other, Orientation orientation) {
    MutableSet<Relation> result = Sets.mutable.empty();
    var thiscoord = this.getRotatedCoords(orientation);
    if (other instanceof Block) {
      Block ob = (Block) other;
      var othercoord = ob.getRotatedCoords(orientation);

      var xdistance = thiscoord.x1 - othercoord.x1;
      var ydistance = thiscoord.y1 - othercoord.y1;
      var zdistance = othercoord.z1 - thiscoord.z1;

      if (thiscoord.x1 == othercoord.x1
          && thiscoord.z1 == othercoord.z1
          && ydistance > 0) {
        result.add(new Relation("top-of"+ydistance,
            this, Lists.immutable.of(other)));
      } else if (thiscoord.y1 == othercoord.y1
          && thiscoord.z1 == othercoord.z1
          && xdistance > 0) {
        result.add(new Relation("left-of"+xdistance,
            this, Lists.immutable.of(other)));
      } else if (thiscoord.x1 == othercoord.x1
          && thiscoord.y1 == othercoord.y1
          && zdistance > 0) {
        result.add(new Relation("in-front-of"+zdistance,
            this, Lists.immutable.of(other)));
      }
    } // add also left-of/front-of/top-of-relations for Block-BigBlock relations
    else if (other instanceof BigBlock) {
      var oblock =(BigBlock) other;
      var ocoords = oblock.getRotatedCoords(orientation);

      var xdistance = thiscoord.x1 - ocoords.getMaxX();
      var ydistance = thiscoord.y1 - ocoords.getMinY();
      var zdistance = ocoords.getMinZ() - thiscoord.z1;

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


    }

    if (other instanceof Pillar) {
      Pillar p = (Pillar) other;
      for (var relation : generateRelationsTo(p.getTop(), orientation))
        result.add(relation);
      for (var relation : generateRelationsTo(p.getBottom(), orientation))
        result.add(relation);
    }
    return result;
  }

  @Override
  public String toString() {
    return "Block-" + xpos + "-" + ypos + "-" + zpos;
  }
}
