package de.saar.coli.minecraft.relationextractor;

import java.util.HashSet;
import java.util.Set;

public class BigBlock extends MinecraftObject {

  public final int x1, y1, z1, x2, y2, z2;
  String name;

  // direction of the normal vector
  // public final Direction n;
  public BigBlock(String name, int x1, int y1, int z1, int x2, int y2, int z2) {
    this.name = name;

    this.x1 = x1;
    this.y1 = y1;
    this.z1 = z1;

    this.x2 = x2;
    this.y2 = y2;
    this.z2 = z2;
    children = new HashSet<>();
    for (Block i: getBlocks()) {
      children.add(i);
    }
  }

  @Override
  public Set<Block> getBlocks() {
    Set<Block> res = new HashSet<>();
    for (int x = x1; x <= x2; x++) {
      for (int y = y1; y <= y2; y++) {
        for (int z = z1; z <= z2; z++) {
          res.add(new Block(x, y, z));
        }
      }
    }
    return res;
  }

  @Override
  public boolean sameShapeAs(MinecraftObject other) {
    if (!(other instanceof BigBlock)) {
      return false;
    }
    BigBlock oblock = (BigBlock) other;
    if (x1 - x2 != oblock.x1 - oblock.x2
        || y1 - y2 != oblock.y1 - oblock.y2
        || z1 - z2 != oblock.z1 - oblock.z2
    ) {
      return false;
    }
    return true;
  }
}
