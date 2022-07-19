package de.saar.coli.minecraft.relationextractor;

import de.saar.coli.minecraft.relationextractor.Relation.Orientation;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Sets;

public class Railing extends MinecraftObject {
  public Block block1;
  public Block block2;
  public Row row;
  public Set<Block> blocks;

  /**
   * A railing consists of a bigblock and a block underneath on each end.
   * @param name internal name of the railing
   * @param x1 minimal x
   * @param z1 minimal z
   * @param x2 maximal x
   * @param z2 maximal z
   * @param y height at which the railing is
   */
  public Railing(String name, int x1, int z1, int x2, int z2, int y, String type) {
    super(type);

    if (x1 != x2 && z1 != z2) {
      throw new RuntimeException("Railing must be along xpos or zpos axis!");
    }
    block1 = new Block(x1, y, z1, type);
    block2 = new Block(x2, y, z2, type);
    row = new Row("row-" + name, x1, z1, x2, z2, y + 1, type);
    blocks = new HashSet<>();
    blocks.add(block1);
    blocks.add(block2);
    blocks.addAll(row.getBlocks());
    children = new HashSet<>();
    children.add(row);
    children.add(block1);
    children.add(block2);
  }

  @Override
  public Set<Block> getBlocks() {
    return blocks;
  }

  @Override
  public boolean sameShapeAs(MinecraftObject other) {
    if (!(other instanceof Railing)) {
      return false;
    }
    Railing or = (Railing) other;
    return or.row.sameShapeAs(this.row);
  }


  private static final Set<EnumSet<Features>> features = Set.of(
      EnumSet.of(Features.TYPE,
          Features.X1,
          Features.Y1,
          Features.Z1,
          Features.X2,
          Features.Z2),
      EnumSet.of(Features.TYPE,
          Features.X1,
          Features.Y1,
          Features.Z1,
          Features.X2,
          Features.LENGTH,
          Features.ORIENTATION),
      EnumSet.of(Features.TYPE,
          Features.X1,
          Features.Y1,
          Features.Z1,
          Features.Z2,
          Features.LENGTH,
          Features.ORIENTATION)
  );

  /**
   * Returns the features that uniquely describe this type of object.
   */
  public Set<EnumSet<Features>> getFeatures() {
    return features;
  }

  @Override
  public MutableSet<Relation> generateRelationsTo(MinecraftObject other,
      MinecraftObject other2,
      Orientation orientation) {
    MutableSet<Relation> result = Sets.mutable.empty();
    // make an otherside relation if this is on the other side of other from other2
    // and other2 has the same shape as other2
    if (other instanceof BigBlock && other2 instanceof Railing) {
      Railing orailing = (Railing) other2;
      BigBlock obloc = (BigBlock) other;
      var ocoords = orailing.row.getRotatedCoords(orientation);
      var thiscoords = this.row.getRotatedCoords(orientation);
      var blockcoords = obloc.getRotatedCoords(orientation);
      if (//0: on same height
          (ocoords.getMinY() == thiscoords.getMinY())
	  &&
	  (// 1. case: same, only shifted on z axis
	   (ocoords.getMaxX() == thiscoords.getMaxX()
	    && ocoords.getMinX() == thiscoords.getMinX()
	    && ocoords.getMinX() != ocoords.getMaxX()
	    && ocoords.getMinX() != ocoords.getMaxX()
	    )
	   ||
	   (// 2. case: same, only shifted on x axis
	    ocoords.getMaxZ() == thiscoords.getMaxZ()
	    && ocoords.getMinZ() == thiscoords.getMinZ()
	    && ocoords.getMinZ() != ocoords.getMaxZ()
	    && ocoords.getMinZ() != ocoords.getMaxZ()
	    )
	   )
	  &&
	  // they are mirrored across block
	  (thiscoords.getMinX() - blockcoords.getMinX() == blockcoords.getMaxX() - ocoords.getMaxX()
	   ||
	   thiscoords.getMinZ() - blockcoords.getMinZ() == blockcoords.getMaxZ() - ocoords.getMaxZ()
	   )
	  &&
	  // is on an edge
	  (thiscoords.getMinZ() == blockcoords.getMinZ() || thiscoords.getMinX() == blockcoords.getMinX())
	  ) {
          result.add(new Relation("otherside",
              this,
              Lists.immutable.of(obloc, orailing)));
        }
      }
    return result;
  }


  @Override
  public MutableSet<Relation> generateRelationsTo(MinecraftObject other, Orientation orientation) {
    MutableSet<Relation> result = Sets.mutable.empty();
    var coord = new BigBlock.CoordinatesTuple(block1.xpos, block1.ypos, block1.zpos,
        block2.xpos, block2.ypos+1, block2.zpos, orientation);
    if (other instanceof Block) {
      var oc = ((Block) other).getRotatedCoords(orientation);
      // railing needs only one from and one to relation
      // use topof-relations because in instructions want to say e.g.
      // "build a railing from the top of the black block to the top of the blue block"
      if (oc.x1 == coord.getMaxX() && oc.y1 +1== coord.getMinY() && oc.z1 == coord.getMinZ()) {
        result.add(new Relation("topof-from-diagonal1",
            this, other));
      }
      if (oc.x1 == coord.getMinX() && oc.y1 +1== coord.getMinY() && oc.z1 == coord.getMaxZ()) {
        result.add(new Relation("topof-to-diagonal1",
            this, other));
      }
    }
    return result;
  }

  public int length() {
    if (block1.xpos == block2.xpos) {
      return block2.zpos - block1.zpos + 1;
    }
    return block2.xpos - block1.xpos + 1;
  }

  @Override
  public MutableSet<Relation> generateOwnUnaryRelations(Orientation o) {
    return Sets.mutable.of(
        new Relation("railing", this),
        new Relation("length" + length(), this)
    );
  }

  @Override
  public String toString() {
    return "Railing-" + block1 + '-' + block2;
  }
}
