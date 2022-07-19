package de.saar.coli.minecraft.relationextractor;

import com.google.gson.Gson;
import de.saar.coli.minecraft.MinecraftRealizer;
import de.saar.coli.minecraft.relationextractor.Relation.Orientation;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.factory.Sets;

public abstract class MinecraftObject {
  protected final String type;
  protected Set<MinecraftObject> children;

  protected MinecraftObject(String type) {
    this.type = type;
  }

  private static final Set<EnumSet<Features>> features = Set.of(EnumSet.of(Features.TYPE,
                                                               Features.X1,
                                                               Features.Y1,
                                                               Features.Z1));

  public abstract Set<Block> getBlocks();

  /**
   * Returns the features that uniquely describe this type of object.
   */
  public Set<EnumSet<Features>> getFeatures() {
    return features;
  }

  /**
   * Returns the features that uniquely describe this type of object as a String for alto.
   */
  public Set<String> getFeaturesStrings() {
    return getFeatures()
        .stream()
        .map((set) ->
            set.stream()
                .map((elem) -> elem.name().toLowerCase())
                .collect(Collectors.joining("+"))
        )
        .collect(Collectors.toSet());
  }

  /**
   * returns a verb appropriate for instructing to build this object.
   */
  public String getVerb() {
    return "build";
  }

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


  protected abstract MutableSet<Relation> generateOwnUnaryRelations(Orientation orientation);

  public MutableSet<Relation> generateUnaryRelations(Orientation orientation) {
    var result = generateOwnUnaryRelations(orientation);
    result.add(new Relation(type, this));
    return result;
  }

  public abstract MutableSet<Relation> generateRelationsTo(MinecraftObject other,
      Orientation orientation);

  public MutableSet<Relation> generateRelationsTo(MinecraftObject other,
      MinecraftObject other2,
      Orientation orientation) {
    MutableSet<Relation> result = Sets.mutable.empty();
    return result;
  }

  /**
   * Generates and returns all relations from this object to the objects in other.
   */
  public MutableSet<Relation> generateRelationsTo(Iterable<MinecraftObject> other, Orientation orientation) {
    MutableSet<Relation> result = Sets.mutable.empty();
    for (MinecraftObject o: other) {
      if (this.equals(o)) {
        continue;
      }
      result.addAll(generateRelationsTo(o, orientation));
      for (MinecraftObject o2: other) {
        if (this.equals(o2) || o.equals(o2)) {
          continue;
        }
        result.addAll(generateRelationsTo(o,o2, orientation));
      }
    }
    return result;
  }

  @Override
  public abstract String toString();

  /**
   * Generate a json String representing this object.
   */
  public String asJson() {
    var json = new Gson()
        .toJsonTree(this)
        .getAsJsonObject();
    json.addProperty("type", this.getClass().getSimpleName());
    return json.toString();
  }

  /**
   * Parses a string representation of the MinecraftObject (as produced
   * by toString) into the MinecraftObject itself.
   *
   * @param objectDescription
   * @return
   */
  public static MinecraftObject fromString(String objectDescription, String type) {
    if( objectDescription.startsWith("row")) {
      return Row.parseObject(objectDescription, type);
    } else if( objectDescription.startsWith("wall")) {
      return Wall.parseObject(objectDescription, type);
    } else if( objectDescription.startsWith("Stairs")) {
      return Stairs.parseObject(objectDescription, type);
    } else {
      throw new UnsupportedOperationException("Cannot resolve " + objectDescription + " to a MinecraftObject.");
    }
  }
}
