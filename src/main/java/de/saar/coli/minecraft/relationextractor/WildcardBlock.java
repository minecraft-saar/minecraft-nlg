package de.saar.coli.minecraft.relationextractor;

import de.saar.coli.minecraft.relationextractor.Relation.Orientation;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Sets;

public class WildcardBlock extends Block {

  public WildcardBlock(int x, int y, int z) {
    super(x, y, z, "doesn't matter");
  }

  @Override
  public MutableSet<Relation> generateOwnUnaryRelations(Orientation o) {
    throw new UnsupportedOperationException("Can't generate Relations for Wildcard blocks.");
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || !(o instanceof Block)) {
      return false;
    }
    Block block = (Block) o;
    return xpos == block.xpos &&
        ypos == block.ypos &&
        zpos == block.zpos;
  }

  @Override
  public String toString() {
    return "WildCard-" + xpos + ypos + zpos;
  }
}