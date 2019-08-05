package de.saar.coli.minecraft.relationextractor;

import de.saar.coli.minecraft.relationextractor.relations.Relation;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.factory.Sets;

public abstract class MinecraftObject {
  protected Set<MinecraftObject> children;

  protected EnumSet<Aspects> aspects;

  public abstract Set<Block> getBlocks();


  public Set<MinecraftObject> getChildren() {
    return children;
  }

  /**
   * Returns a new set of all children transitively contained in this object.
   */
  public Set<MinecraftObject> getChildrenTransitive() {
    Set<MinecraftObject> result = new HashSet<>();
    for (MinecraftObject c: children) {
      result.addAll(c.getChildrenTransitive());
    }
    result.addAll(children);
    return result;
  }

  public abstract boolean sameShapeAs(MinecraftObject other);

  /**
   * Checks whether other is (transitively) contained in
   * this object.
   */
  public boolean contains(MinecraftObject other) {
    if (children.contains(other)) {
      return true;
    }
    for (MinecraftObject c: children) {
      if (c.contains(other)) {
        return true;
      }
    }
    return false;
  }

  private static <E extends Enum<E>> boolean intersects(EnumSet<E> a, EnumSet<E>  b) {
    for (E belem: b) {
      if (a.contains(belem)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Generate set of relations uniqely describing this objects.
   * Relations are both to this object as well as to all others
   * needed for description.
   * @param possibleReferents objects which may be used for the description
   * @return set of relations, transitively describing this all all other objects needed.
   */
  public Set<Relation> describe(ImmutableSet<MinecraftObject> possibleReferents) {
    Set<Relation> result = new HashSet<>();

    EnumSet<Aspects> aspectsNeeded = EnumSet.copyOf(aspects);
    EnumSet<Aspects> aspectsFixed = EnumSet.noneOf(Aspects.class);
    loop:
    for (MinecraftObject other: possibleReferents) {
      Set<Relation> relationCandidates = this.generateRelationsTo(other);
      // relationCandidates.addAll(other.generateRelationsTo(this));
      for (Relation rel: relationCandidates) {
        if (intersects(aspectsNeeded, rel.fixes)) {
          for (MinecraftObject obj: rel.otherobj) {
            ImmutableSet<MinecraftObject> prefNew = possibleReferents.newWithout(obj);
            result.addAll(obj.describe(prefNew));
          }
          result.add(rel);
          aspectsFixed.addAll(rel.fixes);
          aspectsNeeded.removeAll(rel.fixes);
          if (aspectsNeeded.isEmpty()) {
            break loop;
          }
        }
      }
    }
    return result;
  }

  public abstract MutableSet<Relation> generateRelationsTo(MinecraftObject other);

  public MutableSet<Relation> generateRelationsTo(MinecraftObject other,
      MinecraftObject other2) {
    MutableSet<Relation> result = Sets.mutable.empty();
    return result;
  }

  /**
   * Generates and returns all relations from this object to the objects in other.
   */
  public MutableSet<Relation> generateRelationsTo(List<MinecraftObject> other) {
    MutableSet<Relation> result = Sets.mutable.empty();
    for (MinecraftObject o: other) {
      if (this.equals(o)) {
        continue;
      }
      result.addAll(generateRelationsTo(o));
      for (MinecraftObject o2: other) {
        if (this.equals(o2) || o.equals(o2)) {
          continue;
        }
        result.addAll(generateRelationsTo(o,o2));
      }
    }
    return result;
  }
}
