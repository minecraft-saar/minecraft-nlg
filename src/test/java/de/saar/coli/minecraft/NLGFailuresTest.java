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


public class NLGFailuresTest {
  MinecraftRealizer mcr;

  //method to check if instruction contains all substrings needed (order can vary)
  public static boolean containsWords(String input, String[] words) {
    return Arrays.stream(words).allMatch(input::contains);
  }

  @BeforeEach
  public void setup() {
    mcr = MinecraftRealizer.createRealizer();
  }

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

}
