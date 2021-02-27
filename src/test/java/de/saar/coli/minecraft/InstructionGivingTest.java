package de.saar.coli.minecraft;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.saar.coli.minecraft.relationextractor.Block;
import de.saar.coli.minecraft.relationextractor.Floor;
import de.saar.coli.minecraft.relationextractor.MinecraftObject;
import de.saar.coli.minecraft.relationextractor.Railing;
import de.saar.coli.minecraft.relationextractor.Relation;
import de.saar.coli.minecraft.relationextractor.Relation.Orientation;
import de.saar.coli.minecraft.relationextractor.Row;
import de.saar.coli.minecraft.relationextractor.UniqueBlock;
import de.saar.coli.minecraft.relationextractor.Wall;
import de.up.ling.irtg.Interpretation;
import de.up.ling.irtg.InterpretedTreeAutomaton;
import de.up.ling.irtg.algebra.ParserException;
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
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class InstructionGivingTest {

  MinecraftRealizer mcr;

  //method to check if instruction contains all substrings needed (order can vary)
  public static boolean containsWords(String input, String[] words) {
    return Arrays.stream(words).allMatch(input::contains);
  }

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

    String var1 = "put a block in front of the blue block";

    boolean correct = res.equals(var1);
    assertTrue(correct, "instruction incorrect, was " + res);
    System.out.println(res);


    res = mcr.generateInstruction(world,
        new Block(0,0,1),
        new HashSet<>(),
        Orientation.ZPLUS);
    var1 = "put a block behind the blue block";

    correct = res.equals(var1);
    assertTrue(correct, "instruction incorrect, was " + res);
    System.out.println(res);

    // checking left-of
    res = mcr.generateInstruction(world,
        new Block(2,0,3),
        new HashSet<>(),
        Orientation.ZPLUS);
    var1 = "put a block to the right of the red block";

    correct = res.equals(var1);
    assertTrue(correct, "instruction incorrect, was " + res);
    System.out.println(res);
  }

  @Test
  public void testBigBlockInstruction(){
    var wall1 = new Wall("wall1", 1,0,0,4,3,0);
    var wall2 = new Wall("wall2", 1,0,1,4,3,1);
    var row1 = new Row("row1", 0,0,-3,0,0);
    var row2 = new Row("row2",0,1,0,4,0);
    var row3 = new Row("row3",0,5,0,8,0);
    Set<MinecraftObject> world = Set.of(wall1, row2);

    // test wall behind wall
    var res = mcr.generateInstruction(world,
        wall2,
        Set.of(wall1),
        Orientation.ZPLUS);
    String[] substrings = {"build a wall", "of height four", "behind", "the", "wall"};
    assertTrue(containsWords(res, substrings));

    // test row behind row orientaway
    //TODO distinguish between orientations?
    res = mcr.generateInstruction(world,
        row3,
        Set.of(row2),
        Orientation.ZPLUS);
    String var1 = "build a row behind the previous row";
    System.out.println(res);
    boolean correct = res.equals(var1);
    assertTrue(correct, "instruction incorrect, was "+res);

    // test row left of row orientleftright
    res = mcr.generateInstruction(world,
        row3,
        Set.of(row2),
        Orientation.XPLUS);
    var1 = "build a row to the right of the previous row";
    System.out.println(res);
    correct = res.equals(var1);
    assertTrue(correct, "instruction incorrect, was "+res);

    //test row left of row orientaway
    res = mcr.generateInstruction(world,
        new Row("row4",-1,1,-1,4,0),
        Set.of(row2),
        Orientation.ZPLUS);
    var1 = "build a row to the right of the previous row";
    System.out.println(res);
    correct = res.equals(var1);
    assertTrue(correct, "instruction incorrect, was "+res);

  }

  @Test
  public void testNextTo(){
    var blueBlock = new UniqueBlock("blue_wool", 0,0,0);
    var block1 = new Block(1, 0,0);
    Set<MinecraftObject> world = Set.of(blueBlock, block1);
    var res = mcr.generateInstruction(world,
        new Block(-1,0,0),
        new HashSet<>(),
        Orientation.ZPLUS);

    String var1 = "put a block to the right of the blue block";
    String var2 = "put a block next to the blue block";

    boolean correct = res.equals(var1)||res.equals(var2);
    assertTrue(correct, "instruction incorrect, was "+res);

    var redBlock = new UniqueBlock("red_wool", 0,0,0);
    var block2 = new Block(-1, 0,0);
    world = Set.of(redBlock, block2);
    res = mcr.generateInstruction(world,
        new Block(1,0,0),
        new HashSet<>(),
        Orientation.ZPLUS
        );

    var1 = "put a block to the left of the red block";
    var2 = "put a block next to the red block";

    correct = res.equals(var1)||res.equals(var2);
    assertTrue(correct, "instruction incorrect, was "+res);

  }

  @Test
  public void testXLeftOf(){
    var world = createWorld();

    // test 2-blocks distance relation for all locations
    var res = mcr.generateInstruction(world, new Block(5, 0,0),
        new HashSet<>(),
        Orientation.ZPLUS);
    String var1 = "put a block two blocks left of the black block";
    boolean correct = res.equals(var1);
    assertTrue(correct, "instruction incorrect, was "+res);
    System.out.println(res);
    res = mcr.generateInstruction(world, new Block(-2, 0,0),
        new HashSet<>(),
        Orientation.ZPLUS);
    var1 = "put a block two blocks right of the blue block";
    correct = res.equals(var1);
    assertTrue(correct, "instruction incorrect, was "+res);
    System.out.println(res);
    res = mcr.generateInstruction(world, new Block(0, 0,-2),
        new HashSet<>(),
        Orientation.ZPLUS);
    var1 ="put a block two blocks in front of the blue block";
    correct = res.equals(var1);
    assertTrue(correct, "instruction incorrect, was "+res);
    System.out.println(res);
    res = mcr.generateInstruction(world, new Block(0, 0,-2),
        new HashSet<>(),
        Orientation.ZMINUS);
    var1 = "put a block two blocks behind the blue block";
    correct = res.equals(var1);
    assertTrue(correct, "instruction incorrect, was "+res);
    System.out.println(res);
    res = mcr.generateInstruction(world, new Block(3, 2,3),
        new HashSet<>(),
        Orientation.ZMINUS);
    var1 = "put a block two blocks on top of the red block";
    correct = res.equals(var1);
    assertTrue(correct,"instruction incorrect, was "+res);
    System.out.println(res);

    // test "three/four/five blocks left of"
    res = mcr.generateInstruction(world, new Block(-3, 0,0),
        new HashSet<>(),
        Orientation.ZPLUS);
    assertEquals("put a block three blocks right of the blue block", res);
    System.out.println(res);
    res = mcr.generateInstruction(world, new Block(-4, 0,0),
        new HashSet<>(),
        Orientation.ZPLUS);
    assertEquals("put a block four blocks right of the blue block", res);
    System.out.println(res);
    res = mcr.generateInstruction(world, new Block(-5, 0,0),
        new HashSet<>(),
        Orientation.ZPLUS);
    assertEquals("put a block five blocks right of the blue block", res);
    System.out.println(res);

    // test x-blocks distance relation for BigBlock objects
    var row1 = new Row("row1", 0,0,3,0,0);
    var row2 = new Row("row2", 0,2,3,2,0);
    res = mcr.generateInstruction(Set.of(row1),
        row2,
        Set.of(row1),
        Orientation.ZPLUS);
    assertEquals("build a row two blocks behind the previous row", res);
    System.out.println(res);
  }

  @Test
  public void testBigBlockBlockRelations() throws ParserException {
    // orientleftright
    // Block position is uniquely specified
    var row1 = new Row("row1", 0,0,3,0,0);
    Set<MinecraftObject> world = Set.of(row1);
    var res = mcr.generateInstruction(world, new Block(-1,0,0),
        new HashSet<>(),
        Orientation.ZPLUS);
    String var1 = "put a block to the right of the row";
    boolean correct = res.equals(var1);
    assertTrue(correct, "instruction incorrect, was " + res);

    // orientaway
    // only z-coordinate and y-coordinate specified
    // no instruction can be generated with current grammar
    var target = new Block(0,0,1);
    var tree = mcr.generateStatementTree(target.toString(), target.getFeaturesStrings());
    assertEquals(null, tree);

    // for front of everything should be specified in orientaway scenario
    world = Set.of(new Row("row2",0,0,0,3,0));
    res = mcr.generateInstruction(world, new Block(0,0,4),
        new HashSet<>(),
        Orientation.ZPLUS);
    var1 = "put a block behind the row";
    correct = res.equals(var1);
    assertTrue(correct, "instruction incorrect, was "+ res);

  }

  @Test
  public void testBlockBigBlockRelations() {
    var row1 = new Row("row1", 0, 0, 3, 0, 0);
    Set<MinecraftObject> world = Set.of(row1);
    var res = mcr.generateInstruction(world, new Block(4, 0, 0),
        new HashSet<>(),
        Orientation.ZPLUS);
    String var1 = "put a block to the left of the row";
    boolean correct = res.equals(var1);
    assertTrue(correct, "instruction incorrect, was " + res);

    var wall1 = new Wall("wall1", 0,0,0,3,3,0);
    world = Set.of(wall1);
    res = mcr.generateInstruction(world, new Block(4, 0, 0),
        new HashSet<>(),
        Orientation.ZPLUS);
    var1 = "put a block to the left of the wall";
    correct = res.equals(var1);
    assertTrue(correct, "instruction incorrect, was "+res);

    res = mcr.generateInstruction(Set.of(new UniqueBlock("blue_wool",4,0,0)),row1,
        new HashSet<>(),
        Orientation.ZPLUS);
    System.out.println(res);
    String[] substrings = {"build a row", "to the right", "of length four", "to the right of","the", "block"};
    assertTrue(containsWords(res,substrings));

  }

  @Test
  public void testBetween(){
    var world = createWorldBetween();
    var res = mcr.generateInstruction(world, new Block(1, 0,0),
        new HashSet<>(),
        Orientation.ZPLUS);
    String var1 = "put a block between the black block and the blue block";
    String var2 = "put a block between the blue block and the black block";

    boolean correct = res.equals(var1) || res.equals(var2);
    assertTrue(correct, "instruction incorrect, was " + res);
    System.out.println("between: "+res);
    //testing between front-behind
    res = mcr.generateInstruction(world, new Block(0, 0,1),
        new HashSet<>(),
        Orientation.ZPLUS);
    var1 = "put a block between the blue block and the yellow block";
    var2 = "put a block between the yellow block and the blue block";

    correct = res.equals(var1) || res.equals(var2);
    assertTrue(correct, "instruction incorrect, was " + res);
    System.out.println("between: "+res);
  }

  @Test
  public void testBetweenNotSameShape(){
    //test row between blocks
    var blueBlock = new UniqueBlock("blue_wool", 0,0,0);
    var yellowBlock = new UniqueBlock("yellow_wool", 4,0,0);
    var row1 = new Row("row1", 1,0,3,0,0);
    Set<MinecraftObject> world = Set.of(blueBlock,yellowBlock);
    var res = mcr.generateInstruction(world, row1,
        new HashSet<>(),
        Orientation.ZPLUS);

    String var1 = "build a row between the blue block and the yellow block";
    String var2 = "build a row between the yellow block and the blue block";
    boolean correct = res.equals(var1)||res.equals(var2);
    System.out.println(res);
    assertTrue(correct, "instruction incorrect, was " + res);
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
    System.out.println(res);
    String correctOption = "build a wall from the blue block to the black block of height two";
    assertEquals(correctOption.length(), res.length());
    assertTrue(res.contains("a wall"));
    assertTrue(res.contains("to the blue block"));
    assertTrue(res.contains("from the black block"));
  }

  @Test
  public void testWalltoherefrom(){

    var previouswall = new Wall("previouswall", 0,0,0,0,1,3);
    var wall2 = new Wall("wall2", 0,0,0,3,1,0);
    var block1 = new Block(0,1,0);
    var blueBlock = new UniqueBlock("blue_wool",3,0,0);


    Set<MinecraftObject> world = Set.of(previouswall,block1,blueBlock);
    var res = mcr.generateInstruction(world, wall2,
        Set.of(previouswall, block1), Orientation.XPLUS);

    String var1 = "build a wall from the blue block to the previous block";
    System.out.println(res);
    boolean correct = res.equals(var1);
    assertTrue(correct,"instruction incorrect was " + res);

    world = Set.of(previouswall,block1,blueBlock);
    res = mcr.generateInstruction(world, wall2,
        Set.of(previouswall, block1), Orientation.XMINUS);
    System.out.println(res);
    assertTrue(correct,"instruction with orientation XMINUS incorrect was " + res);


    world = Set.of(previouswall,block1,blueBlock);
    res = mcr.generateInstruction(world, wall2,
        Set.of(previouswall, block1), Orientation.ZPLUS);
    System.out.println(res);
    assertTrue(correct,"instruction with orientation ZPLUS incorrect was " + res);
  }

  @Test
  public void testfromtoRelations(){
    var wall = new Wall("wall", 0,0,0,3,1,0);
    var blueblock = new UniqueBlock("blue_wool",3,0,0);
    var yellowblock = new UniqueBlock("yellow_wool", 3,1,0);
    var redblock = new UniqueBlock("red_wool",0,0,0);
    var blackblock = new UniqueBlock("black_wool", 0,1,0);

    var from_diagonal1 = new Relation("from-diagonal1",wall,blueblock);
    var from_diagonal2 = new Relation("from-diagonal2",wall,redblock);
    var to_diagonal1 = new Relation("to-diagonal1",wall,blackblock);
    var to_diagonal2 = new Relation("to-diagonal2",wall,yellowblock);
    var topof_diagonal1 = new Relation("topof-diagonal1",wall,redblock);
    var topof_diagonal2 = new Relation("topof-diagonal2",wall,blueblock);

    Set<MinecraftObject> world = Set.of(wall,redblock,blueblock,blackblock,yellowblock);
    Set<Relation> gold_standard_relations = Set.of(from_diagonal1, from_diagonal2, to_diagonal1,
        to_diagonal2, topof_diagonal1, topof_diagonal2);

    Set<Relation> relations = new HashSet<>(Relation.generateAllRelationsBetweeen(world, Orientation.ZPLUS));

    assertTrue(relations.containsAll(gold_standard_relations),
        "For orientation ZPLUS from_diagonal1 not generated correctly");

    // check with other orientation

    relations = new HashSet<>(Relation.generateAllRelationsBetweeen(world, Orientation.XMINUS));

    assertTrue(relations.containsAll(gold_standard_relations),
        "For orientation XMINUS from_diagonal1 not generated correctly");

    // check with other orientation
    // with XPLUS and ZMINUS orientation diagonal1 and diagonal2 are switched,
    // therefore define new relations

    from_diagonal1 = new Relation("from-diagonal1",wall,redblock);
    from_diagonal2 = new Relation("from-diagonal2",wall,blueblock);
    to_diagonal1 = new Relation("to-diagonal1",wall,yellowblock);
    to_diagonal2 = new Relation("to-diagonal2",wall,blackblock);
    topof_diagonal1 = new Relation("topof-diagonal1",wall,blueblock);
    topof_diagonal2 = new Relation("topof-diagonal2",wall,redblock);

    gold_standard_relations = Set.of(from_diagonal1, from_diagonal2, to_diagonal1,
        to_diagonal2, topof_diagonal1, topof_diagonal2);

    relations = new HashSet<>(Relation.generateAllRelationsBetweeen(world, Orientation.XPLUS));
    assertTrue(relations.containsAll(gold_standard_relations),
        "For orientation XPLUS from_diagonal1 not generated correctly");

    // check for ZMINUS

    relations = new HashSet<>(Relation.generateAllRelationsBetweeen(world, Orientation.ZMINUS));
    assertTrue(relations.containsAll(gold_standard_relations),
        "For orientation ZMINUS from_diagonal1 not generated correctly");
  }


  @Test
  public void testWallCorner(){
    var otherWall = new Wall("wall1",0,0,0,3,1,0);
    var block1 = new Block(3, 1,0);
    var block2 = new Block(3,0,0);
    Set<MinecraftObject> world = Set.of(
        otherWall, block1, block2
    );

    var res = mcr.generateInstruction(world, new Block(3,2, 0),
        new HashSet<>(),
        Orientation.ZPLUS
    );
    String var1 = "put a block on top of the upper left corner of the wall";

    boolean correct = res.equals(var1);
    assertTrue(correct, "wall instruction incorrect, was " + res);
    System.out.println(res);

    res = mcr.generateInstruction(world, new Block(4,0,0),
        new HashSet<>(),
        Orientation.ZPLUS
    );
    var1 = "put a block to the left of the lower left corner of the wall";
    correct = res.equals(var1);
    assertTrue(correct, "wall instruction incorrect, was "+ res);
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
    String exampleInstruction = "build a row of length three to the right to the top of the red block";
    assertTrue( exampleInstruction.length() == res.length(),
        "Incorrect instruction:" + res);
    assertTrue(res.contains("build a row"));
    assertTrue(res.contains("of length three"));
    assertTrue(res.contains("to the right"));
    assertTrue(res.contains("to the top of the red block"));
    System.out.println(res);

    // check other direction
    res = mcr.generateInstruction(world,
        new Row("row", 0,3,-2,3,1),
        new HashSet<>(),
        Orientation.ZPLUS
    );
    exampleInstruction = "build a row of length three to the right to the top of the yellow block";
    assertTrue( exampleInstruction.length() == res.length(),
        "Incorrect instruction:" + res);
    assertTrue(res.contains("build a row"));
    assertTrue(res.contains("of length three"));
    assertTrue(res.contains("to the right"));
    assertTrue(res.contains("to the top of the yellow block"));
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
    String var1 = "build a floor from the top of the black block to the top of the yellow block";
    
    List<String> expected = List.of("a", "floor", "from the top of", "the", "black", "block", "to the top of", "the", "yellow", "block");
    assertTrue(mcr.isDerivable(expected));
    mcr.getTreeForInstruction(expected);
    System.out.print(res);
    boolean correct = res.equals(var1);
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
    String var1 = "build a floor from the red block to the orange block";
    boolean correct = res.equals(var1);
    assertTrue(correct, "floor instruction incorrect, was: " + res);
  }

  @Test
  public void testRailing() {
    var world = createWorld();
    var res = mcr.generateInstruction(world,
        new Railing("railing", 0,0,3,0,1),
        new HashSet<>(),
        Orientation.XPLUS);
    String var1 = "build a railing from the top of the blue block to the top of the black block";
    boolean correct = res.equals(var1);
    assertTrue(correct, "railing instruction incorrect, was: " + res);
    // test for railing along x-axis
    res = mcr.generateInstruction(world,
        new Railing("railing", 0,0,3,0,1),
        new HashSet<>(),
        Orientation.ZMINUS);
    var1 = "build a railing from the top of the blue block to the top of the black block";
    correct = res.equals(var1);
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

  @Test
  public void testWalltopof(){
    var wall1 = new Wall("wall1", 0,0,0,3,3,0);
    var row1 = new Row("row1", 0,0,3,0,4);
    Set<MinecraftObject> world = Set.of(
        wall1
    );
    var res = mcr.generateInstruction(world,
        row1,
        Set.of(wall1),
        Orientation.ZPLUS
    );

    String correct = "build a row on top of the wall";
    assertEquals(correct, res);
  }

  @Test
  public void testRowTopOfRow() {
    var row1 = new Row("row1", 0, 0, 3, 0, 0);
    var row2 = new Row("row2", 0, 0, 3, 0, 1);

    Set<MinecraftObject> world = Set.of(
        row1
    );
    var res = mcr.generateInstruction(world,
        row2,
        Set.of(row1),
        Orientation.ZPLUS
    );

    String var1 = "build a row on top of the previous row";
    boolean correct = res.equals(var1);
    System.out.println(res);
    assertTrue(correct, "instruction incorrect, was " + res);
  }

  @Test
  public void testWalltopofRow(){
    var row1 = new Row("row1", 0, 0, 3, 0, 0);
    var wall1 = new Wall("wall1", 0,1,0,3,4,0);
    Set<MinecraftObject> world = Set.of(
        row1
    );
    var res = mcr.generateInstruction(world,
        wall1,
        Set.of(row1),
        Orientation.ZPLUS
    );

    String var1 = "build a wall of height four on top of the row";
    String var2 = "build a wall on top of the row of height four";
    boolean correct = var1.equals(res) || var2.equals(res);
    assertTrue(correct, "instruction incorrect, was: " + res);
  }

  @Test
  public void testIndefandDefRules(){
    Set<MinecraftObject> world = createWorld();
    Tree tree = mcr.generateReferringExpressionTree(world,
        new Block(0,0,1),
        new HashSet<>(),
        Orientation.ZMINUS);
    String treestring = tree.toString();
    String var1 = "np(loc(obj(iblock),twobehind(dnp(obj(yelloww(block))))))";
    String var2 = "np(loc(obj(iblock),front(dnp(obj(bluew(block))))))";
    System.out.println(treestring);
    boolean correct = var1.equals(treestring) || var2.equals(treestring);
    assertTrue(correct, "tree incorrect, was: " + treestring);

    var row1 = new Row("row1", 0, 0, 3, 0, 0);
    var row2 = new Row("row2", 0, 0, 3, 0, 1);

    world = Set.of(
        row1
    );
    tree = mcr.generateReferringExpressionTree(world,
        row2,
        Set.of(row1),
        Orientation.ZPLUS);
    treestring = tree.toString();
    var1 = "np(loc(obj(irow),top(dnp(obj(prev(row))))))";
    var2 = "np(loc(obj(irow),topsameshape(dnp(obj(prev(row))))))";
    System.out.println(treestring);
    correct = var1.equals(treestring) || var2.equals(treestring);
    assertTrue(correct, "tree incorrect, was: " + treestring);
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

  private Set<MinecraftObject> createWorldBetween(){
    var res = new HashSet<MinecraftObject>();
    res.add(new Block(1,1,1));
    // Remember: Minecraft uses right-handed coordinate system
    // blue is front-right
    res.add(new UniqueBlock("blue_wool", 0,0,0));
    // black is front-left
    res.add(new UniqueBlock("black_wool", 2,0,0));
    res.add(new UniqueBlock("yellow_wool", 0,0,2));
    return res;
  }

}
