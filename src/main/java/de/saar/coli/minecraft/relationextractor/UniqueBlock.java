package de.saar.coli.minecraft.relationextractor;

import de.saar.coli.minecraft.relationextractor.relations.Relation;
import java.util.EnumSet;
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
    result.add(new Relation(name, EnumSet.noneOf(Aspects.class), this, Lists.immutable.empty()));
    return result;
  }


  @Override
  public Set<Relation> describe(ImmutableSet<MinecraftObject> possibleReferents) {
    Set<Relation> result = new HashSet<>();
    result.add(new Relation(name,
        EnumSet.of(Aspects.X1, Aspects.Y1, Aspects.Z1),
        this, Lists.immutable.empty()
        ));
    return result;
  }
}
