package de.saar.coli.minecraft.relationextractor;

import de.saar.coli.minecraft.MinecraftRealizer;
import de.saar.coli.minecraft.relationextractor.Bridge.BridgeDirection;
import de.saar.coli.minecraft.relationextractor.Relation.Orientation;
import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.factory.Lists;

public class RelationGenerator {

  /**
   * A proof of concept main method running
   * the relation generator not in use anymore.
   */
  public static void main(String[] args) throws Exception {
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

    List<MinecraftObject> objects = new ArrayList<>();
    final Block ub = new Block(1, 1, 1, "blue");
    final Block ub2 = new Block(4, 1, 5, "orange");
    final Block ub3 = new Block(4, 3, 5, "yellow");

    objects.add(ub);
    objects.add(ub2);
    Bridge bridge = new Bridge("b", 1, 1, 4, 5, 1, BridgeDirection.ALONGX, "stone");
    objects.add(bridge);

    List<Relation> relations = generateAllRelationsBetweeen(objects);
    relations.add(new Relation("indefbridge",
        bridge, Lists.immutable.empty()));

    MinecraftRealizer mcr = MinecraftRealizer.createRealizer(new File("minecraft-indefinite.irtg"));

    mcr.setRelations(relations);

    System.out.println(mcr.generateStatement("build", "b", Set.of("X1+Z1+X2+Z2")));



    // TODO make sure to use uni-directional relations for elements that still need to be built
    /*System.out.println("Trying to build a block at 1,2,1:");
    Block newBlock = new Block(1,2,1);
    ImmutableSet<MinecraftObject> possibleReferents = Sets.immutable.of(ub);
    Set<Relation> result = newBlock.describe(possibleReferents);
    result.forEach((Relation x) -> System.out.println(x.toString()));


    // Bridge bridge = new Bridge("b", 1, 1, 4, 5, 1, BridgeDirection.ALONGX);
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

     */

    buildFloor();
    buildRailing();
  }

  /**
   * Generates a new scene in which a floor should be built between two blocks.
   * @throws Exception when something goes wrong
   */
  public static void buildFloor() throws Exception {
    List<MinecraftObject> objects = new ArrayList<>();
    final Block ub = new Block(1, 1, 1, "blue");
    final Block ub2 = new Block(4, 1, 5, "orange");

    objects.add(ub);
    objects.add(ub2);
    BigBlock floor = new BigBlock("floor", 1,1,1, 4,1,5, "stone");
    objects.add(floor);

    List<Relation> relations = generateAllRelationsBetweeen(objects);
    relations.add(new Relation("indeffloor",
        floor, Lists.immutable.empty()));

    MinecraftRealizer mcr = MinecraftRealizer.createRealizer(new File("minecraft-indefinite.irtg"));

    mcr.setRelations(relations);

    System.out.println(mcr.generateStatement("build", "floor", Set.of("X1+Z1+X2+Z2")));

  }

  /**
   * Build a scene in which a railing should be built and generates the instruction.
   */
  public static void buildRailing() throws Exception {
    List<MinecraftObject> objects = new ArrayList<>();
    final Block ub = new Block(1, 1, 1, "blue");
    final Block ub2 = new Block(4, 1, 5, "orange");

    objects.add(ub);
    objects.add(ub2);
    BigBlock floor = new BigBlock("floor", 1,1,1, 4,1,5, "stone");
    BigBlock rail2 = new BigBlock("rail2", 1,2,1, 1,2,5, "stone");
    BigBlock rail1 = new BigBlock("rail1", 4,2,1, 4,2,5, "stone");
    objects.add(floor);
    objects.add(rail1);
    objects.add(rail2);

    MutableSet<Relation> foo = rail1.generateRelationsTo(floor, rail2, Orientation.ZPLUS);
    List<Relation> relations = generateAllRelationsBetweeen(objects);
    relations.add(new Relation("indefrailing",
        rail2, Lists.immutable.empty()));
    relations.add(new Relation("it",
        rail1,
        Lists.immutable.empty()));

    MinecraftRealizer mcr = MinecraftRealizer.createRealizer(new File("minecraft-indefinite.irtg"));

    mcr.setRelations(relations);

    System.out.println(mcr.generateStatement("build", "rail2", Set.of("X1+Z1+X2+Z2")));

  }

  /**
   * Generates all relations between given objects.
   */
  public static List<Relation> generateAllRelationsBetweeen(List<MinecraftObject> mcobjects) {
    List<Relation> result = new ArrayList<>();
    for (MinecraftObject obj: mcobjects) {
      result.addAll(obj.generateRelationsTo(mcobjects, Orientation.ZPLUS));
    }
    return result;
  }


}
