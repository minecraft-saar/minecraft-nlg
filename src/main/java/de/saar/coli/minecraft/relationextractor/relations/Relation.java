package de.saar.coli.minecraft.relationextractor.relations;

import de.saar.coli.minecraft.relationextractor.Aspects;
import de.saar.coli.minecraft.relationextractor.MinecraftObject;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.collections.api.list.ImmutableList;

public class Relation {
  public final String relationString;
  public final EnumSet<Aspects> fixes;
  public final ImmutableList<MinecraftObject> otherobj;
  public final MinecraftObject obj;

  /**
   * A relation between one main object and some other objects.
   * @param relationString The name of the relation
   * @param fixes which aspects are fixed for the main object if this relation is described
   * @param obj the main object
   * @param otherob the other objects needed to describe this relation
   */
  public Relation(String relationString, EnumSet<Aspects> fixes,
      MinecraftObject obj, ImmutableList<MinecraftObject> otherob) {
    this.fixes = fixes;
    this.relationString = relationString;
    this.obj = obj;
    this.otherobj = otherob;
  }

  /**
   * Constructs a relation with an empty set of aspects it fixes.
   */
  public Relation(String relationString, MinecraftObject obj,
                  ImmutableList<MinecraftObject> otherob) {
    this(relationString, EnumSet.noneOf(Aspects.class), obj, otherob);
  }

  @Override
  public String toString() {
    return relationString + "(" + obj.toString() + "," + otherobj.makeString(", ") + ")";
  }

  /**
   * Adds this relation to an alto model.
   * @param fom the first order model to add this relation to
   */
  public void addToModel(Map<String, Set<List<String>>> fom) {
    if (! fom.containsKey(relationString)) {
      fom.put(relationString, new HashSet<>());
    }
    List<String> args = new ArrayList<>();
    args.add(obj.toString());
    for (MinecraftObject o: otherobj) {
      args.add(o.toString());
    }
    fom.get(relationString).add(args);
  }
}
