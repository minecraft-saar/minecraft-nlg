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

  public static class CoordinatesTuple {

    public final int x1;
    public final int y1;
    public final int z1;

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
  public Block(int x, int y, int z) {
    children = new HashSet<>();
    this.xpos = x;
    this.ypos = y;
    this.zpos = z;
  }

  @Override
  public String getVerb() {
    return "put";
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
  public MutableSet<Relation> generateUnaryRelations(Orientation o) {
    return Sets.mutable.of(new Relation("block", this));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
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
    if (other instanceof Block) {
      Block ob = (Block) other;

      var thiscoord = this.getRotatedCoords(orientation);
      var othercoord = ob.getRotatedCoords(orientation);

      // Top-of (always Y axis)
      if (thiscoord.x1 == othercoord.x1
          && thiscoord.z1 == othercoord.z1
          && thiscoord.y1 == othercoord.y1 + 1) {
        result.add(new Relation("top-of",
            this, Lists.immutable.of(other)));
      } else if (thiscoord.x1 == othercoord.x1 + 1
          && thiscoord.z1 == othercoord.z1
          && thiscoord.y1 == othercoord.y1) {
        result.add(new Relation("left-of",
            this, Lists.immutable.of(other)));
      } else if (thiscoord.x1 == othercoord.x1
          && thiscoord.z1 == othercoord.z1 - 1
          && thiscoord.y1 == othercoord.y1) {
        result.add(new Relation("in-front-of",
            this, Lists.immutable.of(other)));
      } else if (thiscoord.x1 == othercoord.x1 + 2
          && thiscoord.z1 == othercoord.z1
          && thiscoord.y1 == othercoord.y1) {
        result.add(new Relation("two-left-of",
            this, Lists.immutable.of(other)));
      } else if (thiscoord.x1 == othercoord.x1
          && thiscoord.z1 == othercoord.z1 - 2
          && thiscoord.y1 == othercoord.y1) {
        result.add(new Relation("two-in-front-of",
            this, Lists.immutable.of(other)));
      } else if (thiscoord.x1 == othercoord.x1
          && thiscoord.z1 == othercoord.z1
          && thiscoord.y1 == othercoord.y1 + 2) {
        result.add(new Relation("two-above",
            this, Lists.immutable.of(other)));
      }
    }
    return result;
  }

  @Override
  public String toString() {
    return "Block-" + xpos + "-" + ypos + "-" + zpos;
  }
}
