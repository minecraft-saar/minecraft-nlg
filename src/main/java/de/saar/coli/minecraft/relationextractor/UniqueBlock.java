package de.saar.coli.minecraft.relationextractor;

import java.util.HashSet;
import java.util.Set;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.factory.Lists;

public class UniqueBlock extends Block {
  public final String name;

  public UniqueBlock(String name, int x, int y, int z) {
    super(x,y,z);
    this.name = name;
  }

  @Override
  public MutableSet<Relation> generateRelationsTo(MinecraftObject other) {
    MutableSet result = super.generateRelationsTo(other);
    result.add(new Relation(name, this, Lists.immutable.empty()));
    return result;
  }
}
