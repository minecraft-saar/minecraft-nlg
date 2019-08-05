package de.saar.coli.minecraft.experiments;

import de.saar.coli.minecraft.MinecraftRealizer;
import de.saar.coli.minecraft.relationextractor.Aspects;
import de.saar.coli.minecraft.relationextractor.BigBlock;
import de.saar.coli.minecraft.relationextractor.MinecraftObject;
import de.saar.coli.minecraft.relationextractor.UniqueBlock;
import de.saar.coli.minecraft.relationextractor.relations.Relation;
import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.factory.Lists;

public class Inlg2019 {

  /**
   * Generates the referring expressions in our inlg 2019 paper.
   */
  public static void main(String[] args) throws Exception {
    /* need to keep track which aspect is defined by a relation
     implement the following scenario:

    w = water r, b= red, blue
    start:
            r
    wwwwwwwwwwwwwwwww
    wwwwwwwwwwwwwwwww
        b

    target: 5x4 bridge floor, railing on each side
    Railing: one block on each side, row on top connecting those blocks
    */

    final MinecraftRealizer mcr = MinecraftRealizer.createRealizer(
        Inlg2019.class.getResourceAsStream("inlg2019.irtg"));
    
    List<MinecraftObject> objects = new ArrayList<>();
    // anchors
    final UniqueBlock ub = new UniqueBlock("blue", 1, 1, 1);
    objects.add(ub);
    final UniqueBlock ub2 = new UniqueBlock("red", 4,1,5);
    objects.add(ub2);
    // the bridge
    BigBlock bridge = new BigBlock("bridge", 1, 1, 1, 4, 1, 5);
    objects.add(bridge);
    // relations that will be re-used
    List<Relation> unaryRelations = new ArrayList<>();
    unaryRelations.add(new Relation("bridge", bridge, Lists.immutable.empty()));
    unaryRelations.add(new Relation("block", ub, Lists.immutable.empty()));
    unaryRelations.add(new Relation("block", ub2, Lists.immutable.empty()));
    
    List<Relation> relations = generateAllRelationsBetweeen(objects);
    relations.addAll(unaryRelations);
    
    mcr.setRelations(relations);

    System.out.println(mcr.generateStatement("build", "bridge", "type+corner1+corner3"));

    // right and left railing
    BigBlock railing1 = new BigBlock("railing1", 4, 2, 1, 4, 2, 5);
    objects.add(railing1);
    BigBlock railing2 = new BigBlock("railing2", 1,2,1,1,2,5);
    objects.add(railing2);

    // the previous railing (right), can be referred to as "it".
    unaryRelations.add(new Relation("railing", railing1, Lists.immutable.empty()));
    unaryRelations.add(new Relation("it", railing1, Lists.immutable.empty()));
    // the new railing to be built
    unaryRelations.add(new Relation("railing", railing2, Lists.immutable.empty()));

    relations = generateAllRelationsBetweeen(objects);
    relations.addAll(unaryRelations);
    mcr.setRelations(relations);

    System.out.println(mcr.generateStatement("build", "railing2", "type+corner1+corner3"));
  }


  private static List<Relation> generateAllRelationsBetweeen(List<MinecraftObject> mcobjects) {
    List<Relation> result = new ArrayList<>();
    for (MinecraftObject obj: mcobjects) {
      result.addAll(obj.generateRelationsTo(mcobjects));
    }
    return result;
  }
}
