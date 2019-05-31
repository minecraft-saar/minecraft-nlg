package de.saar.coli.minecraft;

import de.saar.basic.Pair;

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
import java.io.InputStreamReader;
import java.io.StringReader;

import java.util.BitSet;
import java.util.List;
import java.util.Set;


public class MinecraftRealizer {

  private final InterpretedTreeAutomaton irtg;
  private final Interpretation<List<String>> strI;
  private final Interpretation<Set<List<String>>> refI;
  private final Interpretation<BitSet> semI;
  private final SetAlgebra refA;
  private final SubsetAlgebra semA;

  /**
   * Builds a realizer from given model and grammar files.
   * This essentially handles reading the model and grammar for you.
   * @throws Exception if something goes wrong
   */
  public static MinecraftRealizer createRealizer(File tirtgFile, File modelFile) throws Exception {
    try (
        FileInputStream tirtgin = new FileInputStream(tirtgFile);
        FileInputStream modelin = new FileInputStream(modelFile);
    ) {
      FirstOrderModel mcModel = FirstOrderModel.read(new InputStreamReader(modelin));
      InterpretedTreeAutomaton irtg = new IrtgInputCodec().read(tirtgin);

      return new MinecraftRealizer(irtg, mcModel);
    }
  }

  /**
   * Builds a realizer from an irtg and a model.
   */
  public MinecraftRealizer(InterpretedTreeAutomaton irtg, FirstOrderModel mcModel)
      throws InstantiationException, IllegalAccessException, ClassNotFoundException {
    this.irtg = irtg;
    refI = (Interpretation<Set<List<String>>>) irtg.getInterpretation("ref");
    refA = (SetAlgebra) refI.getAlgebra();
    semI = (Interpretation<BitSet>) irtg.getInterpretation("sem");
    semA = (SubsetAlgebra) semI.getAlgebra();

    // Interpretation<Set<List<String>>> refI = irtg.getInterpretation("ref");
    // put inputs here
    strI = (Interpretation<List<String>>) irtg.getInterpretation("string");
    refA.setModel(mcModel);
    try {
      semA.readOptions(new StringReader("X1+Z1+X2+Z2"));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Builds a statement that represents building objName using the action.
   * The aspects define how objName should be described.
   */
  public String generateStatement(String action, String objName, String aspects)
      throws ParserException {
    String ret = "**NONE**";
    Set<List<String>> refInput = refA.parseString("{" + objName + "}");
    BitSet semInput = semA.parseString(aspects);

    TreeAutomaton<String> automaton = irtg.getAutomaton();
    Intersectable<Set<List<String>>> refO = refI.parse(refInput);
    Intersectable<BitSet> semO = semI.parse(semInput);
    TreeAutomaton<Pair<Pair<String, Set<List<String>>>, BitSet>> ta =
        automaton.intersect(refO).intersect(semO);

    Tree<String> bestTree = ta.viterbi();
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
