package de.saar.coli.minecraft;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.saar.coli.minecraft.relationextractor.Block;
import de.saar.coli.minecraft.relationextractor.Floor;
import de.saar.coli.minecraft.relationextractor.MinecraftObject;
import de.saar.coli.minecraft.relationextractor.Railing;
import de.saar.coli.minecraft.relationextractor.Relation.Orientation;
import de.saar.coli.minecraft.relationextractor.Row;
import de.saar.coli.minecraft.relationextractor.UniqueBlock;
import de.saar.coli.minecraft.relationextractor.Wall;
import de.up.ling.irtg.Interpretation;
import de.up.ling.irtg.InterpretedTreeAutomaton;
import de.up.ling.irtg.algebra.SubsetAlgebra;
import de.up.ling.irtg.semiring.LogDoubleArithmeticSemiring;
import de.up.ling.tree.ParseException;
import de.up.ling.tree.Tree;
import de.up.ling.tree.TreeParser;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.BitSet;
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
    // enabling this line changes the optimal trees and the tests are expected to fail in that case.
    // modifiyWeights();
  }

  public void modifiyWeights() {
    mcr.readExpectedDurationsFromStream(
        new InputStreamReader(InstructionGivingTest.class.getResourceAsStream("/weights.txt")),true);
  }

  @Test
  public void testBlockInstruction(){
    var world = createWorld();
    var res = mcr.generateInstruction(world,
        new Block(0,0,1),
        new HashSet<>(),
        Orientation.ZMINUS);
    assertTrue(mcr.isDerivable(List.of("a", "block", "in front of", "the", "blue", "block")));
    List<String> incorr = List.of("a", "block", "in front of", "the", "blue", "blue", "block");
    assertTrue(mcr.isDerivable(incorr));
    System.out.println(mcr.getTreeForInstruction(incorr));
    assertEquals("put a block in front of the blue block", res);
    res = mcr.generateInstruction(world,
        new Block(0,0,1),
        new HashSet<>(),
        Orientation.ZPLUS);
    assertEquals("put a block behind the blue block", res);
  }

  @Test
  public void testDisjointUnion()
      throws NoSuchFieldException, IllegalAccessException, ParseException {
    Field f = MinecraftRealizer.class.getDeclaredField("irtg");
    f.setAccessible(true);
    InterpretedTreeAutomaton irtg = (InterpretedTreeAutomaton) f.get(mcr);
    Interpretation<BitSet> interpetation = (Interpretation<BitSet>) irtg.getInterpretation("sem");
    SubsetAlgebra algebra = (SubsetAlgebra) interpetation.getAlgebra();
    String testTree = "dnp(obj(blue(blue(block))))";
    String weirdTree = "np(loc(obj(block),front(dnp(obj(blue(blue(block)))))))";
    Tree<String> semTree = interpetation.getHomomorphism().apply(TreeParser.parse(testTree));
    System.out.println(semTree);
    System.out.println(algebra.evaluate(semTree));
  }

  @Test
  public void testHLOInstruction() {
    var world = createWorld();
    var res = mcr.generateInstruction(world,
        new Wall("wall2", 0,0,0,3,1,0),
        new HashSet<>(),
        Orientation.ZPLUS
        );
    String correctOption = "build a wall to the blue block from the black block of height two";
    assertEquals(correctOption.length(), res.length());
    assertTrue(res.contains("a wall"));
    assertTrue(res.contains("to the blue block"));
    assertTrue(res.contains("from the black block"));
  }

  @Test
  public void testRow() {
    var world = createWorld();
    var res = mcr.generateInstruction(world,
        new Row("row", 3,3,5,3,1),
        new HashSet<>(),
        Orientation.ZPLUS
    );
    String exampleInstruction = "build a row of length three to the right to the top of the red block";
    assertEquals(exampleInstruction.length(), res.length(), "Incorrect instruction:" + res);
    assertTrue(res.contains("build a row"));
    assertTrue(res.contains("of length three"));
    assertTrue(res.contains("to the right"));
    assertTrue(res.contains("to the top of the red block"));
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
    
    List<String> expected = List.of("a", "floor", "from the top of", "the", "black", "block", "to the top of", "the", "yellow", "block");
    assertTrue(mcr.isDerivable(expected));
    mcr.getTreeForInstruction(expected);

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
  public void testRailingOtherSide() throws NoSuchFieldException, IllegalAccessException {
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
    List<String> goldInstruction = List.of("a", "railing", "on the other side of", "the", "floor");
    List<String> realInstruction = List.of("a", "railing", "on the other side of", "the", "floor", "of length four");
    assertTrue(mcr.isDerivable(goldInstruction));
    var tree = mcr.getTreeForInstruction(goldInstruction);
    Field tafield = MinecraftRealizer.class.getDeclaredField("irtg");
    tafield.setAccessible(true);
    InterpretedTreeAutomaton irtg = (InterpretedTreeAutomaton) tafield.get(mcr);
    System.out.println(irtg.getAutomaton().getWeight(mcr.getTreeForInstruction(goldInstruction),
        LogDoubleArithmeticSemiring.INSTANCE));
    System.out.println(irtg.getAutomaton().getWeight(mcr.getTreeForInstruction(realInstruction),
        LogDoubleArithmeticSemiring.INSTANCE));
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
    assertEquals(var1, res, "Incorrect instruction:" + res);
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
    res.add(new Wall("wall1",0,0,0, 3,3,0));
    return res;
  }

}
