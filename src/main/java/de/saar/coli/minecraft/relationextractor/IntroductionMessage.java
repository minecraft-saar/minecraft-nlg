package de.saar.coli.minecraft.relationextractor;

import de.saar.coli.minecraft.relationextractor.Block;
import de.saar.coli.minecraft.relationextractor.MinecraftObject;
import de.saar.coli.minecraft.relationextractor.Relation.Orientation;
import de.saar.coli.minecraft.relationextractor.Relation;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.factory.Sets;

import java.util.Set;

public class IntroductionMessage extends MinecraftObject {

    public final MinecraftObject object;
    public final boolean starting;
    public final String name;

    public IntroductionMessage(MinecraftObject object, boolean starting, String name){
        super(name); //TODO: is this correct?

        this.object = object;
        this.starting = starting;
        this.name = name;
    }

    @Override
    public Set<Block> getBlocks() {
        return object.getBlocks();
    }

    @Override
    public boolean sameShapeAs(MinecraftObject other) {
        return object.sameShapeAs(other);
    }

    public MutableSet<Relation> generateOwnUnaryRelations(Orientation orientation) {
      return Sets.mutable.empty();
    }

    @Override
    public MutableSet<Relation> generateRelationsTo(MinecraftObject other, Relation.Orientation orientation) {
        return object.generateRelationsTo(other, orientation);
    }

    @Override
    public String toString() {
        return "Introduction Object for: " + object.toString();
    }
}
