package de.saar.coli.minecraft.relationextractor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.factory.Lists;

public class Relation {
  public final String relationString;
  public final ImmutableList<MinecraftObject> otherobj;
  public final MinecraftObject obj;

  /**
   * Orientation is defined along the axes: if the player
   * primarily looks along the X axis into increasing values of X, it is XPLUS etc.
   */
  public enum Orientation{XPLUS, XMINUS, ZPLUS, ZMINUS}

  /**
   * A relation between one main object and some other objects.
   * @param relationString The name of the relation
   * @param obj the main object
   * @param otherob the other objects needed to describe this relation
   */
  public Relation(String relationString,
      MinecraftObject obj, ImmutableList<MinecraftObject> otherob) {
    this.relationString = relationString;
    this.obj = obj;
    this.otherobj = otherob;
  }

  public Relation(String relationString,
      MinecraftObject obj, MinecraftObject otherobj) {
    this.relationString = relationString;
    this.obj = obj;
    this.otherobj = Lists.immutable.of(otherobj);
  }

  public Relation(String relationString,
      MinecraftObject obj) {
    this.relationString = relationString;
    this.obj = obj;
    this.otherobj = Lists.immutable.empty();
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

  public static List<Relation> generateAllRelationsBetweeen(Iterable<MinecraftObject> mcobjects,
                                                            Orientation orientation) {
    List<Relation> result = new ArrayList<>();
    for (MinecraftObject obj: mcobjects) {
      result.addAll(obj.generateUnaryRelations(orientation));
      result.addAll(obj.generateRelationsTo(mcobjects, orientation));
    }
    return result;
  }
}
