package de.saar.coli.minecraft.relationextractor;

import de.saar.coli.minecraft.relationextractor.relations.Relation;
import java.util.EnumSet;
import java.util.HashSet;
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
    aspects = EnumSet.of(Aspects.X1, Aspects.Y1, Aspects.Z1);
    children = new HashSet<>();
    this.xpos = x;
    this.ypos = y;
    this.zpos = z;
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
  public MutableSet<Relation> generateRelationsTo(MinecraftObject other) {
    MutableSet<Relation> result = Sets.mutable.empty();
    if (other instanceof Block) {
      Block ob = (Block) other;
      if (ob.xpos == xpos && ob.zpos == zpos && ob.ypos == ypos - 1) {

        result.add(new Relation("top-of",
            EnumSet.of(Aspects.X1, Aspects.Y1, Aspects.Z1), this, Lists.immutable.of(other)));
      }
    }
    return result;
  }

  @Override
  public String toString() {
    return "Block(" + xpos + ", " + ypos + ", " + zpos + ")";
  }
}
