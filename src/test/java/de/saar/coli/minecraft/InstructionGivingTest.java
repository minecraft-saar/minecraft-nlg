package de.saar.coli.minecraft;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.saar.coli.minecraft.relationextractor.Block;
import de.saar.coli.minecraft.relationextractor.Floor;
import de.saar.coli.minecraft.relationextractor.MinecraftObject;
import de.saar.coli.minecraft.relationextractor.Railing;
import de.saar.coli.minecraft.relationextractor.Relation.Orientation;
import de.saar.coli.minecraft.relationextractor.Row;
import de.saar.coli.minecraft.relationextractor.UniqueBlock;
import de.saar.coli.minecraft.relationextractor.Wall;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class InstructionGivingTest {

  MinecraftRealizer mcr;

  @BeforeEach
  public void setup() {
    mcr = MinecraftRealizer.createRealizer();
  }


  @Test
  public void testBlockInstruction(){
    var world = createWorld();
    var res = mcr.generateInstruction(world,
        new Block(0,0,1),
        new HashSet<>(),
        Orientation.ZMINUS);
    // now have two alternatives to reference to block at (x:0,y:0,z:1)
    String var1 = "put a block in front of the blue block";
    String var2 = "put a block two blocks behind the yellow block";

    boolean correct = res.equals(var1) || res.equals(var2);
    assertTrue(correct, "instruction incorrect, was " + res);
    System.out.println(res);
    res = mcr.generateInstruction(world,
        new Block(0,0,1),
        new HashSet<>(),
        Orientation.ZPLUS);
    var1 = "put a block behind the blue block";
    var2 = "put a block two blocks in front of the yellow block";

    correct = res.equals(var1) || res.equals(var2);
    assertTrue(correct, "instruction incorrect, was " + res);
    System.out.println(res);
    // checking left-of
    res = mcr.generateInstruction(world,
        new Block(2,0,3),
        new HashSet<>(),
        Orientation.ZPLUS);
    var1 = "put a block to the right of the red block";
    var2 = "put a block two blocks left of the yellow block";

    correct = res.equals(var1) || res.equals(var2);
    assertTrue(correct, "instruction incorrect, was " + res);
    System.out.println(res);
  }

  @Test
  public void testTwoLeftOf(){
    var world = createWorld();
    var res = mcr.generateInstruction(world, new Block(5, 0,0),
        new HashSet<>(),
        Orientation.ZPLUS);
    assertEquals("put a block two blocks left of the black block", res);
    System.out.println(res);
    res = mcr.generateInstruction(world, new Block(-2, 0,0),
        new HashSet<>(),
        Orientation.ZPLUS);
    assertEquals("put a block two blocks right of the blue block", res);
    System.out.println(res);
    res = mcr.generateInstruction(world, new Block(0, 0,-2),
        new HashSet<>(),
        Orientation.ZPLUS);
    assertEquals("put a block two blocks in front of the blue block", res);
    System.out.println(res);
    res = mcr.generateInstruction(world, new Block(0, 0,-2),
        new HashSet<>(),
        Orientation.ZMINUS);
    assertEquals("put a block two blocks behind the blue block", res);
    System.out.println(res);
    res = mcr.generateInstruction(world, new Block(3, 2,3),
        new HashSet<>(),
        Orientation.ZMINUS);
    assertEquals("put a block two blocks above the red block", res);
    System.out.println(res);
  }

    @Test
  public void testHLOInstruction() {
    var world = createWorld();
    var res = mcr.generateInstruction(world,
        new Wall("wall2", 0,0,0,3,1,0),
        new HashSet<>(),
        Orientation.ZPLUS
        );
    String var1 = "build a wall to the blue block from the black block of height two";
    String var2 = "build a wall from the black block to the blue block of height two";

    boolean correct = res.equals(var1) || res.equals(var2);
    assertTrue(correct, "wall instruction incorrect, was " + res);
    System.out.println(res);
  }

  @Test
  public void testRow() {
    var world = createWorld();
    var res = mcr.generateInstruction(world,
        new Row("row", 3,3,5,3,1),
        new HashSet<>(),
        Orientation.ZPLUS
    );
    String var1 = "build a row of length three to the right to the top of the red block";
    String var2 = "build a row of length three to the top of the red block to the right";
    String var3 = "build a row to the right to the top of the red block of length three";
    assertTrue(res.equals(var1) || res.equals(var2) || res.equals(var3),
        "Test row incorrectly was: " + res);
    System.out.println(res);
  }

  @Test
  public void testFloor() {
    var world = createWorld();
    var res = mcr.generateInstruction(world,
        new Floor("floor", 0,0,3,3,1),
        new HashSet<>(),
        Orientation.ZPLUS
    );
    String var1 = "build a floor to the top of the yellow block from the top of the black block";
    String var2 = "build a floor from the top of the black block to the top of the yellow block";
    boolean correct = res.equals(var1) || res.equals(var2);
    assertTrue(correct, "floor instruction incorrect, was: " + res);
  }

  @Test
  public void testFloorAlternateAnchors() {
    var world = createWorld();
    world.add(new UniqueBlock("orange_wool",6,0,6));
    var res = mcr.generateInstruction(world,
        new Floor("floor", 6,6,3,3,0),
        new HashSet<>(),
        Orientation.ZPLUS
    );
    String var1 = "build a floor to the red block from the orange block";
    String var2 = "build a floor from the orange block to the red block";
    boolean correct = res.equals(var1) || res.equals(var2);
    assertTrue(correct, "floor instruction incorrect, was: " + res);
  }

  @Test
  public void testRailing() {
    var world = createWorld();
    var res = mcr.generateInstruction(world,
        new Railing("railing", 0,0,3,0,1),
        new HashSet<>(),
        Orientation.XPLUS);
    String var1 = "build a railing to the top of the black block from the top of the blue block";
    String var2 = "build a railing from the top of the blue block to the top of the black block";
    boolean correct = res.equals(var1) || res.equals(var2);
    assertTrue(correct, "railing instruction incorrect, was: " + res);
  }

  @Test
  public void testRailingOtherSide() {
    var otherRailing = new Railing("railing1", 0,3,3,3,1);
    Set<MinecraftObject> world = Set.of(
        new Floor("floor", 0,0,3,3,0),
        otherRailing
    );
    var res = mcr.generateInstruction(world,
        new Railing("railing2", 0,0,3,0,1),
        Set.of(otherRailing),
        Orientation.XPLUS);
    String var1 = "build a railing on the other side";
    String var2 = "build a railing on the other side of the floor";
    boolean correct = res.equals(var1) || res.equals(var2);
    assertTrue(correct, "railing instruction incorrect, was: " + res);
  }

  @Test
  public void testRowNext() {
    var otherRailing = new Row("row1", 0,3,3,3,1);
    Set<MinecraftObject> world = Set.of(
        otherRailing
    );
    var res = mcr.generateInstruction(world,
        new Row("row2", 0,2,3,2,1),
        Set.of(otherRailing),
        Orientation.ZPLUS);

    String var1 = "build a row in front of the previous row";
    //assertTrue(mcr.isDerivable(List.of("a", "row",  "in front of",  "the", "previous", "row")));
    //assertTrue(mcr.isDerivable(List.of("a", "row",  "to the right", "of length four", "in front of", "the", "previous", "row")));
    assertEquals(var1, res);
  }

  private Set<MinecraftObject> createWorld(){
    var res = new HashSet<MinecraftObject>();
    res.add(new Block(1,1,1));
    // Remember: Minecraft uses right-handed coordinate system
    // blue is front-right
    res.add(new UniqueBlock("blue_wool", 0,0,0));
    // black is front-left
    res.add(new UniqueBlock("black_wool", 3,0,0));
    res.add(new UniqueBlock("red_wool", 3,0,3));
    res.add(new UniqueBlock("yellow_wool", 0,0,3));
    res.add(new Block(3,1,0));
    res.add(new Block(3,1,3));
    res.add(new Wall("wall1",0,0,0, 3,3,0));
    return res;
  }

}
