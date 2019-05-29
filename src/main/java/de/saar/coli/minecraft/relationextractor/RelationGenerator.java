package de.saar.coli.minecraft.relationextractor;

import de.saar.coli.minecraft.relationextractor.Bridge.BridgeDirection;
import de.saar.coli.minecraft.relationextractor.relations.Relation;
import it.unimi.dsi.fastutil.Hash;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.impl.factory.Sets;

public class RelationGenerator {

  /**
   * A proof of concept main method running
   * the relation generator not in use anymore.
   */
  public static void main(String[] args) {
    /* need to keep track which aspect is defined by a relation
     implement the following scenario:

    w = water rgbo= red,green,blue,orange
    start:
        r   g
    wwwwwwwwwwwwwwwww
    wwwwwwwwwwwwwwwww
        b   o

    target: 5x4 bridge floor, railing on each side
    Railing: one block on each side, row on top connecting those blocks
    */

    final UniqueBlock ub = new UniqueBlock("blue-block", 1,1,1);
    final UniqueBlock ub2 = new UniqueBlock("green-block", 4,1,5);
    System.out.println("Trying to build a block at 1,2,1:");
    Block newBlock = new Block(1,2,1);
    ImmutableSet<MinecraftObject> possibleReferents = Sets.immutable.of(ub);
    Set<Relation> result = newBlock.describe(possibleReferents);
    result.forEach((Relation x) -> System.out.println(x.toString()));


    Bridge bridge = new Bridge("b", 1, 1, 4, 5, 1, BridgeDirection.ALONGX);
    System.out.println("Trying to build a bridge:");
    Set<Relation> bridgeDesc = bridge.describe(Sets.immutable.of(ub, ub2));
    bridgeDesc.forEach((Relation r) -> System.out.println(r.toString()));
    //Set<MinecraftObject> allObjects = bridge.getChildren();
    //Set<MinecraftObject> alreadyBuilt = new HashSet<>();
    //UniqueBlock blueblock = new UniqueBlock("blue block", 1,0, 1);
    //UniqueBlock greenblock = new UniqueBlock("green block", 4,0, 5);
    //alreadyBuilt.add(blueblock);
    //alreadyBuilt.add(greenblock);
    // describe(bridge.floor, alreadyBuilt);
  }


}
