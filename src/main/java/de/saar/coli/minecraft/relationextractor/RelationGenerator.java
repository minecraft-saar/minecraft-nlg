package de.saar.coli.minecraft.relationextractor;

import de.saar.coli.minecraft.relationextractor.Bridge.BridgeDirection;
import de.saar.coli.minecraft.relationextractor.relations.Relation;
import it.unimi.dsi.fastutil.Hash;
import java.util.HashSet;
import java.util.Set;

public class RelationGenerator {

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

    Bridge bridge = new Bridge("b", 1, 1, 4, 5, 1, BridgeDirection.ALONGX);
    Set<MinecraftObject> allObjects = bridge.getChildren();
    Set<MinecraftObject> alreadyBuilt = new HashSet<>();
    UniqueBlock blueblock = new UniqueBlock("blue block", 1,0, 1);
    UniqueBlock greenblock = new UniqueBlock("green block", 4,0, 5);
    alreadyBuilt.add(blueblock);
    alreadyBuilt.add(greenblock);
    describe(bridge.floor, alreadyBuilt);
  }


}
