package de.saar.coli.minecraft;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.saar.basic.Pair;
import de.saar.coli.minecraft.relationextractor.BigBlock;
import de.saar.coli.minecraft.relationextractor.MinecraftObject;
import de.saar.coli.minecraft.relationextractor.Relation;
import de.saar.coli.minecraft.relationextractor.Relation.Orientation;
import de.saar.coli.minecraft.relationextractor.UniqueBlock;
import de.up.ling.irtg.Interpretation;
import de.up.ling.irtg.InterpretedTreeAutomaton;
import de.up.ling.irtg.TemplateInterpretedTreeAutomaton;
import de.up.ling.irtg.algebra.SetAlgebra;
import de.up.ling.irtg.automata.TreeAutomaton;
import de.up.ling.irtg.codec.TemplateIrtgInputCodec;
import de.up.ling.irtg.util.FirstOrderModel;
import de.up.ling.tree.Tree;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.eclipse.collections.impl.factory.Lists;
import org.junit.jupiter.api.Test;

/**
 * A simple tes using pre-generated irtg and world.
 * @author Arne Köhn
 */
public class MinecraftWorldTest {

  @Test
  public void testRunByHand() throws Exception {
    InputStream mcstream = Thread.currentThread().getContextClassLoader()
        .getResourceAsStream("minecraft.irtg");
    InputStream worldstream = Thread.currentThread().getContextClassLoader()
        .getResourceAsStream("testworld.json");
    TemplateInterpretedTreeAutomaton tirtg = new TemplateIrtgInputCodec().read(mcstream);
     FirstOrderModel mcModel = FirstOrderModel.read(new InputStreamReader(worldstream));
    InterpretedTreeAutomaton irtg = tirtg.instantiate(mcModel);
    SetAlgebra ref = (SetAlgebra) irtg.getInterpretation("ref").getAlgebra();
    Interpretation<Set<List<String>>> refI = (Interpretation<Set<List<String>>>) irtg
        .getInterpretation("ref");
    // put inputs here
    Set<List<String>> refInput = ref.parseString("{loc28}");
    Interpretation<List<String>> strI = (Interpretation<List<String>>) irtg
        .getInterpretation("string");
    ref.setModel(mcModel);
    TreeAutomaton<Pair<String, Set<List<String>>>> ta = irtg.parseSimple(refI, refInput);
    TreeAutomaton<List<String>> outputChart = irtg.decodeToAutomaton(strI, ta);
    Iterator<Tree<String>> it = outputChart.languageIterator();

    assert (it.hasNext());
    // assert strI.getAlgebra().evaluate(it.next()

    if (it.hasNext()) {
      System.out.println(strI.getAlgebra().evaluate(it.next()));
    }
    //def chart = irtg.parseSimple(strI, ["put the block",  "on top of",  "the",  "orange block"])
    //def derivTree = chart.viterbi()
    //print(refI.interpret(derivTree))
  }
  @Test
  public void testRealizer() throws Exception {
    var mcr = MinecraftRealizer.createRealizer();
    List<MinecraftObject> objects = new ArrayList<>();
    // anchors
    final UniqueBlock ub = new UniqueBlock("blue", 4, 1, 1);
    objects.add(ub);
    final UniqueBlock ub2 = new UniqueBlock("red", 1,1,5);
    objects.add(ub2);
    // the bridge
    BigBlock bridge = new BigBlock("bridge", 1, 1, 1, 4, 1, 5);
    objects.add(bridge);
    // relations that will be re-used
    List<Relation> unaryRelations = new ArrayList<>();
    unaryRelations.add(new Relation("bridge", bridge, Lists.immutable.empty()));

    List<Relation> relations = Relation.generateAllRelationsBetweeen(objects, Orientation.ZPLUS);
    relations.addAll(unaryRelations);

    mcr.setRelations(relations);

    var instr = mcr.generateStatement("build", bridge.toString(),
        Set.of("type+x1+y1+z1+x2+z2"));
    boolean corr = "build a bridge to the red block from the blue block".equals(instr)
        || "build a bridge from the blue block to the red block".equals(instr);
    assertTrue(corr, "Did not expect: " + instr);

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

    relations = Relation.generateAllRelationsBetweeen(objects, Orientation.ZPLUS);
    relations.addAll(unaryRelations);
    mcr.setRelations(relations);

    instr = mcr.generateStatement(
        "build",
        railing2.toString(),
        Set.of("type+x1+y1+z1+x2+z2"));
    assertEquals("build a railing on the other side of the bridge", instr);
  }
}

