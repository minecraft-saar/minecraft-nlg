package de.saar.coli.minecraft;

import de.saar.basic.Pair;

import de.saar.coli.minecraft.relationextractor.relations.Relation;
import de.up.ling.irtg.Interpretation;
import de.up.ling.irtg.InterpretedTreeAutomaton;
import de.up.ling.irtg.algebra.ParserException;
import de.up.ling.irtg.algebra.SetAlgebra;
import de.up.ling.irtg.algebra.SubsetAlgebra;
import de.up.ling.irtg.automata.Intersectable;
import de.up.ling.irtg.automata.TreeAutomaton;
import de.up.ling.irtg.codec.IrtgInputCodec;
import de.up.ling.irtg.util.FirstOrderModel;
import de.up.ling.tree.Tree;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class MinecraftRealizer {

  private static final String ASPECTS =
      "corner1+corner2+corner3+corner4+type+color+X1+Z1+X2+Z2+Y1+Y2+SHAPE+HEIGHT";

  private final InterpretedTreeAutomaton irtg;
  private final Interpretation<List<String>> strI;
  private final Interpretation<Set<List<String>>> refI;
  private final Interpretation<BitSet> semI;
  private final SetAlgebra refA;
  private SubsetAlgebra semA;

  /**
   * Builds a realizer from given model and grammar files.
   * This essentially handles reading the model and grammar for you.
   * @throws Exception if something goes wrong
   */
  public static MinecraftRealizer createRealizer(File tirtgFile, File modelFile) throws Exception {
    MinecraftRealizer mcr = createRealizer(tirtgFile);
    try (
        FileInputStream modelin = new FileInputStream(modelFile);
    ) {
      FirstOrderModel mcModel = FirstOrderModel.read(new InputStreamReader(modelin));
      mcr.setModel(mcModel);
      return mcr;
    }
  }


  /**
   * Builds a realizer from given model and grammar files.
   * This essentially handles reading the model and grammar for you.
   * @throws Exception if something goes wrong
   */
  public static MinecraftRealizer createRealizer(File tirtgFile) throws Exception {
    try (
        FileInputStream tirtgin = new FileInputStream(tirtgFile);
    ) {
      InterpretedTreeAutomaton irtg = new IrtgInputCodec().read(tirtgin);
      return new MinecraftRealizer(irtg);
    }
  }


  /**
   * Builds a realizer from given model as InputStream.
   * This essentially handles reading the model and grammar for you.
   * @throws Exception if something goes wrong
   */
  public static MinecraftRealizer createRealizer(InputStream irtgStream) throws Exception {
    InterpretedTreeAutomaton irtg = new IrtgInputCodec().read(irtgStream);
    return new MinecraftRealizer(irtg);
  }


  /**
   * Builds a realizer from an irtg and a model.
   */
  public MinecraftRealizer(InterpretedTreeAutomaton irtg, FirstOrderModel mcModel) {
    this(irtg);
    refA.setModel(mcModel);
  }


  /**
   * Build a realizer with only an irtg, the model has to be set later on.
   */
  public MinecraftRealizer(InterpretedTreeAutomaton irtg) {
    this.irtg = irtg;
    refI = (Interpretation<Set<List<String>>>) irtg.getInterpretation("ref");
    refA = (SetAlgebra) refI.getAlgebra();
    semI = (Interpretation<BitSet>) irtg.getInterpretation("sem");
    if (semI != null) {
      semA = (SubsetAlgebra) semI.getAlgebra();
      try {
        semA.readOptions(new StringReader(ASPECTS));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    strI = (Interpretation<List<String>>) irtg.getInterpretation("string");
  }


  private void setModel(FirstOrderModel model) {
    refA.setModel(model);
  }


  /**
   * Sets the first order model of the referential interpretation
   * to the given relations.
   */
  public void setRelations(List<Relation> rels) {
    if (refA.getModel() == null) {
      refA.setModel(new FirstOrderModel());
    }
    Map<String, Set<List<String>>> fom = new HashMap<>();
    for (Relation rel: rels) {
      rel.addToModel(fom);
    }
    this.refA.setAtomicInterpretations(fom);
  }


  /**
   * Builds a statement that represents building objName using the action.
   * The aspects define how objName should be described.
   */
  public String generateStatement(String action, String objName, String aspects)
      throws ParserException {
    System.err.println(refA.getModel().toString());
    String ret = "**NONE**";
    Set<List<String>> refInput = refA.parseString("{" + objName + "}");
    Intersectable<BitSet> semO = null;
    if (semI != null) {
      BitSet semInput = semA.parseString(aspects);
      semO = semI.parse(semInput);
    }

    TreeAutomaton<String> automaton = irtg.getAutomaton();
    Intersectable<Set<List<String>>> refO = refI.parse(refInput);

    Tree<String> bestTree;

    if (semO != null) {
      TreeAutomaton<Pair<Pair<String, Set<List<String>>>, BitSet>> ta =
          automaton.intersect(refO).intersect(semO);
      bestTree = ta.viterbi();
    } else {
      TreeAutomaton<Pair<String, Set<List<String>>>> ta =
          automaton.intersect(refO);
      bestTree = ta.viterbi();
    }
    // TODO: Ask alexander what this was supposed to do and why it resulted in different
    // outputs than the line above together with building the stringTree below.
    // TreeAutomaton<List<String>> outputChart = irtg.decodeToAutomaton(strI, ta);
    // Tree<String> bestTree = outputChart.viterbi();
    if (bestTree != null) {
      Tree<String> stringTree = strI.getHomomorphism().apply(bestTree);
      ret = String.join(" ", strI.getAlgebra().evaluate(stringTree));
    }

    return action + " " + ret;
  }
}
