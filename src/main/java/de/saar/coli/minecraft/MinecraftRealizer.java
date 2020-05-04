package de.saar.coli.minecraft;

import com.google.common.collect.Iterables;
import de.saar.basic.Pair;

import de.saar.coli.minecraft.relationextractor.Features;
import de.saar.coli.minecraft.relationextractor.Relation;
import de.saar.coli.minecraft.relationextractor.MinecraftObject;
import de.saar.coli.minecraft.relationextractor.Relation.Orientation;
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

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class MinecraftRealizer {

  private static Logger logger = LogManager.getLogger(MinecraftRealizer.class);

  private static final String FEATURES = Arrays.stream(Features.values())
      .map(Features::toString)
      .map(String::toLowerCase)
      .collect(Collectors.joining("+"));

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
   * Builds a realizer using the default IRTG.
   * @throws Exception if something goes wrong
   */
  public static MinecraftRealizer createRealizer() {
    try {
      var irtg = new IrtgInputCodec().read(
          MinecraftRealizer.class.getResourceAsStream("minecraft.irtg"));
      return new MinecraftRealizer(irtg);
    } catch (Exception e) {
      throw new RuntimeException("could not read included minecraft irtg");
    }
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
      semA.setEmptyStatesAllowed(false);
      try {
        semA.readOptions(new StringReader(FEATURES));
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
   * The features define how objName should be described.
   */
  public String generateStatement(String action, MinecraftObject obj, Collection<String> features)
      throws ParserException {
    return generateStatement(action, obj.toString(), features);
  }

  /**
   * Builds an instuction for the target object in the given world.
   * @param world A set of objects already in the world.
   * @param target The object (not yet part of the world) that should be created by the user
   * @param it Every object of the world that could be described by "it"
   * @param lastOrientation The last observed orientation of the user.
   * @return A string that correctly instructs the user.
   */
  public String generateInstruction(Set<MinecraftObject> world,
      MinecraftObject target,
      Set<MinecraftObject> it,
      Orientation lastOrientation) {
    var relations = Relation.generateAllRelationsBetweeen(
        Iterables.concat(world,
            org.eclipse.collections.impl.factory.Iterables.iList(target)
        ),
        lastOrientation
    );
    for (var elem : it) {
      relations.add(new Relation("it", elem));
    }
    var response = "";
    try {
      setRelations(relations);
      response = generateStatement(target.getVerb(), target, target.getFeaturesStrings());
    } catch (ParserException e) {
      e.printStackTrace();
    }
    return response;
  }

  /**
   * Builds a statement that represents building objName using the action.
   * The features define how objName should be described.
   */
  public String generateStatement(String action, String objName, Collection<String> features)
      throws ParserException {
    logger.debug("generating a statement for this model: " + (refA.getModel().toString()));
    String ret = "**NONE**";
    Set<List<String>> refInput = refA.parseString("{" + objName + "}");
    Intersectable<BitSet> semO = null;
    if (semI != null) {
      Set<BitSet> semInputs = features.stream().map((x) -> {
        try {
          return semA.parseString(x);
        } catch (ParserException e) {
          // convert checked to unchecked exception to make map work
          throw new RuntimeException(e);
        }
      }).collect(Collectors.toSet());
      var ta = semA.decompose(semInputs);
      semO = semI.invhom(ta);
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
      logger.debug("best tree: " + bestTree);
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
