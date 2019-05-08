package de.saar.coli.minecraft.relationextractor;

import de.saar.coli.minecraft.relationextractor.relations.Relation;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public class Block extends MinecraftObject {

  public final int x, y, z;

  public Block(int x, int y, int z) {
    children = new HashSet<>();
    this.x = x;
    this.y = y;
    this.z = z;
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
  public Set<Relation> generateRelationsTo(MinecraftObject other) {
    Set<Relation> result = new HashSet<>();
    if (other instanceof Block) {
      Block ob = (Block) other;
      if (ob.x == x && ob.z == z && ob.y == y+1) {
        result.add(new Relation("top-of",
            EnumSet.of(Aspects.X1, Aspects.Y1, Aspects.Z1),));
      }
    }
    return result;
  }
}
