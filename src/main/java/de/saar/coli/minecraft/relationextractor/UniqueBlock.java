package de.saar.coli.minecraft.relationextractor;

import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Sets;

public class UniqueBlock extends Block {
  public final String name;

  public UniqueBlock(String name, int x, int y, int z) {
    super(x,y,z);
    this.name = name;
  }

  @Override
  public MutableSet<Relation> generateUnaryRelations() {
    return Sets.mutable.of(new Relation("block", this),
                           new Relation(name, this, Lists.immutable.empty()));
  }

  @Override
  public String toString() {
    return "UniqueBlock-" + name + xpos + ypos + zpos;
  }
}
