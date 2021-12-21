package de.saar.coli.minecraft;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import de.up.ling.irtg.codec.IrtgInputCodec;
import de.up.ling.irtg.semiring.LogDoubleArithmeticSemiring;
import de.up.ling.irtg.util.FirstOrderModel;
import de.up.ling.tree.ParseException;
import de.up.ling.tree.Tree;
import de.up.ling.tree.TreeParser;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class NLGFailuresTest {
  MinecraftRealizer mcr;

  //method to check if instruction contains all substrings needed (order can vary)
  public static boolean containsWords(String input, String[] words) {
    return Arrays.stream(words).allMatch(input::contains);
  }

  @BeforeEach
  public void setup() throws IOException {
    InterpretedTreeAutomaton irtg = new IrtgInputCodec().read(getClass().getResourceAsStream("/de/saar/coli/minecraft/minecraft.irtg"));
    mcr = new MinecraftRealizer(irtg);
  }

  // this has never failed, just to check that the test setup is correct
  @Test
  public void testRow() throws Exception {
    testRealizerWithRelationGenerator("row6-66-6-8-66-6",
        "a row to the right of length three to the blue block",
        FANCY_BRIDGE_WORLD);
  }

  @Test
  public void testStairs() throws Exception {
    testRealizerWithRelationGenerator("Stairs-row-stairs6-66-6-8-66-6-lowerWall-stairs6-66-7-8-67-7-higherWall-stairs6-66-8-8-68-8",
        "a staircase from the blue block to the yellow block of height three",
        FANCY_BRIDGE_WORLD);
  }

  @Test
  public void parseStairs() throws Exception {
    String s = "Stairs-row-stairs6-66-6-8-66-6-lowerWall-stairs6-66-7-8-67-7-higherWall-stairs6-66-8-8-68-8";
    MinecraftObject o = MinecraftObject.fromString(s);
    assertEquals(s.toLowerCase(), o.toString().toLowerCase());
  }

  private void testRealizerWithRelationGenerator(String targetObject, String intendedString, String worldStr)
      throws Exception {
    Set<MinecraftObject> world = MinecraftRealizer.createRealizer().readWorld(new StringReader(worldStr));
    MinecraftObject o = MinecraftObject.fromString(targetObject);
    Tree<String> derivationTree = mcr.generateReferringExpressionTree(world, o, new HashSet<>(), Orientation.ZPLUS);
    assertNotNull(derivationTree, "No derivation tree found.");

    String s = mcr.treeToReferringExpression(derivationTree);
    assertEquals(intendedString, s);
  }

  // shared-resources/.../worlds/fancy-bridge.csv
  private static final String FANCY_BRIDGE_WORLD = ""
      + "6,66,6,BLUE_WOOL\n"
      + "8,66,8,YELLOW_WOOL\n"
      + "8,66,12,RED_WOOL\n"
      + "6,66,14,BLACK_WOOL\n"
      + "1,65,10,WATER\n"
      + "2,65,10,WATER\n"
      + "3,65,10,WATER\n"
      + "4,65,10,WATER\n"
      + "5,65,10,WATER\n"
      + "6,65,10,WATER\n"
      + "7,65,10,WATER\n"
      + "8,65,10,WATER\n"
      + "9,65,10,WATER\n"
      + "10,65,10,WATER\n";

  /*
  * commenting out, this test cannot fail anyway - AK
  @Test
  public void testBlockInstructionBridge(){
    var blueblock = new UniqueBlock("blue_wool", 6,66,6);
    var block1 = new Block(7,66,6);
    var block2 = new Block(8,66,6);
    var block3 = new Block(9,66,6);
    var block4 = new Block(10,66,6);
    var block5 = new Block(6,66,7);
    var block6 = new Block(7,66,7);
    var block7 = new Block(8,66,7);
    var block8 = new Block(9,66,7);
    var block9 = new Block(10,66,7);
    var block10 = new Block(6,66,8);
    var block11 = new Block(7,66,8);
    var block12 = new Block(8,66,8);
    var block13 = new Block(9,66,8);
    var yellowblock = new UniqueBlock("yellow_wool",10,66,8);
    var targetblock = new Block(10,67,6);
    Set<MinecraftObject> world = Set.of(blueblock, block1, block2, block3, block4,
        block5, block6, block7, block8, block9, block10, block11, block12, block13, yellowblock);
    var res = mcr.generateInstruction(world,
        targetblock,
        Set.of(block13),
        Orientation.ZMINUS);
    System.out.println(res);
  }

   */

}
