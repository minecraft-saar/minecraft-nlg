package de.saar.coli.minecraft.relationextractor;

import de.saar.coli.minecraft.relationextractor.relations.Relation;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Sets;

public class BigBlock extends MinecraftObject {

  public final int x1;
  public final int y1;
  public final int z1;
  public final int x2;
  public final int y2;
  public final int z2;
  String name;

  // direction of the normal vector
  // public final Direction n;

  /**
   * A cuboid consisting of several blocks.
   * x1, y1, z1 are the minimal coordinates, x2, y2, z2 are the maximal ones.
   */
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

  @Override
  public MutableSet<Relation> generateRelationsTo(MinecraftObject other) {
    MutableSet<Relation> result = Sets.mutable.empty();
    if ((other instanceof Block)) {
      Block ob = (Block) other;
      if (ob.xpos == this.x1 && ob.ypos == this.y1 && ob.zpos == this.z1) {
        result.add(new Relation("from",
            EnumSet.of(Aspects.X1, Aspects.Y1, Aspects.Z1),
            this, Lists.immutable.of(ob)));
      }
      if (ob.xpos == this.x2 && ob.ypos == this.y2 && ob.zpos == this.z2) {
        result.add(new Relation("to",
            EnumSet.of(Aspects.X2, Aspects.Y2, Aspects.Z2),
            this, Lists.immutable.of(ob)));
      }
    }
    return result;
  }

  @Override
  public MutableSet<Relation> generateRelationsTo(MinecraftObject other, MinecraftObject other2) {
    MutableSet<Relation> result = Sets.mutable.empty();
    // make an otherside relation if this is on the other side of other from other2
    // and other2 has the same shape as other2
    if (other instanceof BigBlock && other2 instanceof BigBlock) {
      BigBlock oblock = (BigBlock) other2;
      BigBlock ofbloc = (BigBlock) other;
      // check whether this and oblock have the same dimensions
      // and the same orientation
      if (this.x1 - this.x2 == oblock.x1 - oblock.x2
          && this.y1 - this.y2 == oblock.y1 - oblock.y2
          && this.z1 - this.z2 == oblock.z1 - oblock.z2
      ) {
        if (this.x1 == ofbloc.x1
            && this.x2 == ofbloc.x1
            && this.z1 == ofbloc.z1
            && this.z2 == ofbloc.z2
            && oblock.x1 == ofbloc.x2
            && oblock.x2 == ofbloc.x2
            && oblock.z1 == ofbloc.z1
            && oblock.z2 == ofbloc.z2) {
          result.add(new Relation("otherside",
              EnumSet.allOf(Aspects.class),
              this,
              Lists.immutable.of(ofbloc, oblock)));
        }
      }
    }
    return result;
  }

  @Override
  public String toString() {
    return name;
  }

}
