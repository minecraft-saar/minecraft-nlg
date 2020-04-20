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
  public MutableSet<Relation> generateUnaryRelations() {
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

      // First we compute the X and Z axis offsets that are currently seen as "left" and "infront"
      // by the user

      int left_x_offset = 0;
      int left_z_offset = 0;
      int front_x_offset = 0;
      int front_z_offset = 0;
      switch (orientation) {
        case XPLUS:
          left_z_offset = 1;
          front_x_offset = 1;
          break;
        case XMINUS:
          left_z_offset = -1;
          front_x_offset = -1;
          break;
        case ZPLUS:
          left_x_offset = -1;
          front_z_offset = 1;
          break;
        case ZMINUS:
          left_x_offset = 1;
          front_z_offset = -1;
      }


      // Top-of (always Y axis)
      if (ob.xpos == xpos && ob.zpos == zpos && ob.ypos == ypos - 1) {
        result.add(new Relation("top-of",
            this, Lists.immutable.of(other)));
      } else if (ob.xpos == xpos + left_x_offset
          && ob.zpos == zpos + left_z_offset
          && ob.ypos == ypos) {
        result.add(new Relation("left-of",
            this, Lists.immutable.of(other)));
      } else if (ob.xpos == xpos + front_x_offset
          && ob.zpos == zpos + front_z_offset
          && ob.ypos == ypos) {
        result.add(new Relation("in-front-of",
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
