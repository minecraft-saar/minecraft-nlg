package de.saar.coli.minecraft.relationextractor;

import java.util.HashSet;
import java.util.Set;

public class Railing extends MinecraftObject {
  public Block block1, block2;
  public Row row;
  public Set<Block> blocks;
  public Railing(String name, int x1, int z1, int x2, int z2, int y) {
    if (x1 != x2 && z1 != z2) {
      throw new RuntimeException("Railing must be along x or z axis!");
    }
    block1 = new Block(x1, y, z1);
    block2 = new Block(x2, y, z2);
    row = new Row("row-"+name, x1, z1, x2, z2, y+1);
    blocks = new HashSet<>();
    blocks.add(block1);
    blocks.add(block2);
    blocks.addAll(row.getBlocks());
    children = new HashSet<>();
    children.add(row);
    children.add(block1);
    children.add(block2);
  }

  @Override
  public Set<Block> getBlocks() {
    return blocks;
  }

  @Override
  public boolean sameShapeAs(MinecraftObject other) {
    if (!(other instanceof Railing)) {
      return false;
    }
    Railing or = (Railing) other;
    return or.row.sameShapeAs(this.row);
  }
}
