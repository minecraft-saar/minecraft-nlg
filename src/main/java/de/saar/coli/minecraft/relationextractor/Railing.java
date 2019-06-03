package de.saar.coli.minecraft.relationextractor;

import de.saar.coli.minecraft.relationextractor.relations.Relation;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.collections.api.set.MutableSet;

public class Railing extends MinecraftObject {
  public Block block1;
  public Block block2;
  public Row row;
  public Set<Block> blocks;

  /**
   * A railing consists of a bigblock and a block underneath on each end.
   * @param name internal name of the railing
   * @param x1 minimal x
   * @param z1 minimal z
   * @param x2 maximal x
   * @param z2 maximal z
   * @param y height at which the railing is
   */
  public Railing(String name, int x1, int z1, int x2, int z2, int y) {
    if (x1 != x2 && z1 != z2) {
      throw new RuntimeException("Railing must be along xpos or zpos axis!");
    }
    block1 = new Block(x1, y, z1);
    block2 = new Block(x2, y, z2);
    row = new Row("row-" + name, x1, z1, x2, z2, y + 1);
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

  @Override
  public MutableSet<Relation> generateRelationsTo(MinecraftObject other) {
    // TODO
    return null;
  }
}
