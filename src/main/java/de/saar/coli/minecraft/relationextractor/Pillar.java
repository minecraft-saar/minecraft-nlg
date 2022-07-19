package de.saar.coli.minecraft.relationextractor;

import de.saar.coli.minecraft.relationextractor.Relation.Orientation;
import java.util.EnumSet;
import java.util.Set;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.factory.Sets;

public class Pillar extends BigBlock {
    private final int height;
    private final Block top;
    private final Block bottom;

    public Pillar(int x, int y, int z, int height, String type) {
        super("pillar"+x+"-"+y+"-"+z, x, y, z, x, y+height-1, z, type);
        this.height = height;

        Block top = null;
        Block bottom = null;

        for (MinecraftObject o : children) {
          Block b = (Block) o;
          if (top == null || top.ypos < b.ypos)
            top = b;
          if (bottom == null || bottom.ypos > b.ypos)
            bottom = b;
        }

        System.out.println("Children amount: " + children.size());
        System.out.println("z1: " + bottom.ypos);
        System.out.println("z2: " + top.ypos);

        this.top = top;
        this.bottom = bottom;
    }

    private static final Set<EnumSet<Features>> features = Set.of(
        EnumSet.of(Features.TYPE,
          Features.X1,
          Features.Y1,
          Features.Z1,
          Features.HEIGHT)
    );  

    @Override
    public Set<EnumSet<Features>> getFeatures() {
      return features;
    }

    //TODO: is it appropriate to force relations like this ?
    @Override
    public MutableSet<Relation> generateRelationsTo(MinecraftObject other, Orientation orientation) {
      var result = super.generateRelationsTo(other, orientation);
      
      for (var relation : bottom.generateRelationsTo(other, orientation))
        result.add(relation);
      for (var relation : top.generateRelationsTo(other, orientation))
        result.add(relation);

      return result;
    }
  
    @Override
    public MutableSet<Relation> generateOwnUnaryRelations(Orientation o) {
      Relation orientation;
      var result = Sets.mutable.of(
          new Relation("pillar", this)
      );
      result.add(new Relation("height"+this.height, this));

      //TODO: this is not unary, but it still seems like the right function
      result.add(new Relation("pillarbottom", this, bottom));
      result.add(new Relation("pillartop", this, top));

      for (var relation : bottom.generateOwnUnaryRelations(o))
        result.add(relation);
      for (var relation : top.generateOwnUnaryRelations(o))
        result.add(relation);

      return result;
    }

    public Block getTop() {
      return top;
    }

    public Block getBottom() {
      return bottom;
    }
}
