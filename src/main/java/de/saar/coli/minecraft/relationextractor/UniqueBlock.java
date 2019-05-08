package de.saar.coli.minecraft.relationextractor;

import de.saar.coli.minecraft.relationextractor.relations.Relation;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public class UniqueBlock extends Block {
  public final String name;
  public UniqueBlock(String name, int x, int y, int z) {
    super(x,y,z);
    this.name = name;
  }

  @Override
  public Set<Relation> describe(Set<MinecraftObject> possibleReferents) {
    Set<Relation> result = new HashSet<>();
    result.add(new Relation("color",
        EnumSet.of(Aspects.X1, Aspects.Y1, Aspects.Z1),
        this,
        new ArrayList<>()));
    return result;
  }
}
