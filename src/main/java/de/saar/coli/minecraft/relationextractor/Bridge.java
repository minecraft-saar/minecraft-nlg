package de.saar.coli.minecraft.relationextractor;

import de.saar.coli.minecraft.relationextractor.relations.Relation;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Sets;

public class Bridge extends MinecraftObject {
  public enum BridgeDirection {
    ALONGX,
    ALONGY
  }

  public final Railing railing1;
  public final Railing railing2;
  public final Floor floor;
  public final String name;
  public final BridgeDirection dir;
  private int x1pos;
  private int x2pos;
  private int z1pos;
  private int z2pos;
  private int ypos;

  private Set<Block> blocks;

  /**
   * A bridge consists of a floor (BigBlock) and two railings (two blocks + BigBlock).
   * @param name how this bridge is named *internally*
   * @param x1 minimum x coordinate
   * @param z1 minimum z coordinate
   * @param x2 maximum x coordinate
   * @param z2 maximum z coordinate
   * @param y at which height the bridge is in
   * @param dir The orientation of the bridge (along X axis or Y axis)
   */
  public Bridge(String name, int x1, int z1, int x2, int z2, int y, BridgeDirection dir) {
    this.aspects = EnumSet.of(Aspects.ORIENTATION,
        Aspects.X1, Aspects.Z1,
        Aspects.X2, Aspects.Z2,
        Aspects.Y1);
    this.x1pos = x1;
    this.x2pos = x2;
    this.z1pos = z1;
    this.z2pos = z2;
    this.ypos = y;
    this.name = name;
    this.dir = dir;
    floor = new Floor("floor-" + name, x1, z1, x2, z2, y);
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
    if (!(other instanceof Bridge)) {
      return false;
    }
    Bridge ob = (Bridge) other;
    if (dir == ob.dir) {
      return (x1pos - x2pos == ob.x1pos - ob.x2pos && z1pos - z2pos == ob.z1pos - ob.z2pos);
    }
    return (x1pos - x2pos == ob.z1pos - ob.z2pos && z1pos - z2pos == ob.x1pos - ob.x2pos);
  }

  @Override
  public MutableSet<Relation> generateRelationsTo(MinecraftObject other) {
    MutableSet<Relation> result = Sets.mutable.empty();
    if ((other instanceof Block)) {
      Block ob = (Block) other;
      if (ob.xpos == this.x1pos && ob.ypos == this.ypos && ob.zpos == this.z1pos) {
        result.add(new Relation("from",
            EnumSet.of(Aspects.X1, Aspects.Y1, Aspects.Z1),
            this, Lists.immutable.of(ob)));
      }
      if (ob.xpos == this.x2pos && ob.ypos == this.ypos && ob.zpos == this.z2pos) {
        result.add(new Relation("to",
            EnumSet.of(Aspects.X2, Aspects.Y1, Aspects.Z2),
            this, Lists.immutable.of(ob)));
      }
    }
    String orientation = this.dir == BridgeDirection.ALONGX ? "along_x_axis" : "along_z_axis";
    result.add(new Relation(orientation,
        EnumSet.of(Aspects.ORIENTATION),
        this, Lists.immutable.empty()));
    return result;
  }

  @Override
  public String toString() {
    return name;
  }
}
