package de.saar.coli.minecraft.relationextractor.relations;

import de.saar.coli.minecraft.relationextractor.Aspects;
import de.saar.coli.minecraft.relationextractor.MinecraftObject;
import java.util.EnumSet;
import java.util.List;

public class Relation {
  public final String relationString;
  public final EnumSet<Aspects> fixes;
  public final List<MinecraftObject> otherobj;
  public final MinecraftObject obj;
  public Relation(String relationString, EnumSet<Aspects> fixes,
      MinecraftObject obj, List<MinecraftObject> otherob) {
    this.fixes = fixes;
    this.relationString = relationString;
    this.obj = obj;
    this.otherobj = otherob;
  }
}
