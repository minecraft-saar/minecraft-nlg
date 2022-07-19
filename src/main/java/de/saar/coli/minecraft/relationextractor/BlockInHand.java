package de.saar.coli.minecraft.relationextractor;

import de.saar.coli.minecraft.relationextractor.Relation.Orientation;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Sets;

/** The player always has a block in their hand. 
 We want to track that to identify unique objects correctly.
 
 This class is just a glorified object.*/
public class BlockInHand extends MinecraftObject {
    public static final BlockInHand theBlock = new BlockInHand();    

    private BlockInHand() {
        // TODO: this seems a bit hacky, but okay 
        // as the block type is implicity also tracked by the target object
        super("none"); 
    }

    @Override
    public MutableSet<Relation> generateOwnUnaryRelations(Orientation o) {
        return Sets.mutable.empty();
    }

    /**
     * The only relevant method. This will prohibit unique references of "the block".
     * 
     * @param other
     * @param o
     * @return
     */
    @Override
    public MutableSet<Relation> generateRelationsTo(MinecraftObject other, Orientation o) {
        return Sets.mutable.of(new Relation("block", this));
    }

    @Override
    public boolean sameShapeAs(MinecraftObject other) {
      return other == this;
    }

    @Override
    public Set<Block> getBlocks() {
      return Set.of();
    }
  
    @Override
    public String toString() {
        return "Block-In-Hand";
    }
}
