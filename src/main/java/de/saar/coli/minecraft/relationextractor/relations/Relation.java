package de.saar.coli.minecraft.relationextractor.relations;

import de.saar.coli.minecraft.relationextractor.Aspects;
import de.saar.coli.minecraft.relationextractor.MinecraftObject;
import java.util.EnumSet;
import java.util.List;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.ListIterable;

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

  @Override
  public String toString() {
    return relationString + "(" + obj.toString() + "," + otherobj.makeString(", ") + ")";
  }
}
