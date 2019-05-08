package de.saar.coli.minecraft.relationextractor;

import java.util.HashSet;
import java.util.Set;

public class Bridge extends MinecraftObject {
  public enum BridgeDirection {ALONGX, ALONGY}

  public final Railing railing1, railing2;
  public final Floor floor;
  public final String name;
  public final BridgeDirection dir;
  int x1, x2, z1, z2, y;

  private Set<Block> blocks;

  public Bridge(String name, int x1, int z1, int x2, int z2, int y, BridgeDirection dir) {
    this.x1 = x1;
    this.x2 = x2;
    this.z1 = z1;
    this.z2 = z2;
    this.y = y;
    this.name = name;
    this.dir = dir;
    floor = new Floor("floor-"+name, x1, z1, x2, z2, y);
    if (dir == BridgeDirection.ALONGX) {
      railing1 = new Railing("railing-" + name, x1, z1, x2, z1, y);
      railing2 = new Railing("railing-" + name, x1, z2, x2, z2, y);
    } else {
      railing1 = new Railing("railing-" + name, x1, z1, x1, z2, y);
      railing2 = new Railing("railing-" + name, x2, z1, x2, z2, y);
    }

    blocks = new HashSet<Block>();
    blocks.addAll(floor.getBlocks());
    blocks.addAll(railing1.getBlocks());
    blocks.addAll(railing2.getBlocks());
  }

  @Override
  public Set<Block> getBlocks() {
    return blocks;
  }

  @Override
  public boolean sameShapeAs(MinecraftObject other) {
    if (!(other instanceof Bridge))
      return false;
    Bridge ob = (Bridge) other;
    if (dir == ob.dir) {
      return (x1 - x2 == ob.x1 - ob.x2 && z1 - z2 == ob.z1 - ob.z2);
    }
    return (x1 - x2 == ob.z1 - ob.z2 && z1 - z2 == ob.x1 - ob.x2);
  }
}
