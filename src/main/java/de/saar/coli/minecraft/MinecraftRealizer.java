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
import de.up.ling.irtg.semiring.LogDoubleArithmeticSemiring;
import de.up.ling.irtg.util.FirstOrderModel;
import de.up.ling.tree.Tree;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import java.util.regex.Pattern;
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
      try {
        semA.readOptions(new StringReader(FEATURES));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    strI = (Interpretation<List<String>>) irtg.getInterpretation("string");
    if (! semI.getHomomorphism().isNonDeleting()) {
      throw new RuntimeException("SEM interpretation is not nondeleting, aborting!");
    }
    if (! refI.getHomomorphism().isNonDeleting()) {
      throw new RuntimeException("REF interpretation is not nondeleting, aborting!");
    }
  }

  private void setModel(FirstOrderModel model) {
    refA.setModel(model);
  }
  
  private static final Pattern spacere =  Pattern.compile("[ \\t]+");
  
  public void readExpectedDurationsFromStream(Reader reader, boolean enforceCompleteUpdate) {
    var weightMap = new HashMap<String, Double>();
    new BufferedReader(reader).lines().forEach((x) -> {
      var result = spacere.split(x.strip());
      if (result.length != 2) {
        return;
      }
      weightMap.put(result[0], Double.parseDouble(result[1]));
    });
    setExpectedDurations(weightMap, enforceCompleteUpdate);
  }

  /**
   * Sets the weights of the IRTG to the negative expected duration (as we solve a
   * maximization problem)
   * @param durations A map from IRTG rule names to expected durations.
   * @param enforceCompleteUpdate If an incomplete setting of durations should fail
   */
  public void setExpectedDurations(Map<String, Double> durations, boolean enforceCompleteUpdate) {
    Map<String, Double> weights = new HashMap<>();
    for (var entry: durations.entrySet()) {
      weights.put(entry.getKey(), - entry.getValue());
    }
    irtg.getAutomaton().setWeights(weights, enforceCompleteUpdate);
  }

  public void randomizeExpectedDurations() {
    var auto = irtg.getAutomaton();
    var symbols = auto.getSignature().resolveSymbolIDs(auto.getAllLabels());
    var rand = new Random();
    Map<String, Double> durations = new HashMap<>();
    for (String symbol: symbols) {
      durations.put(symbol,  rand.nextDouble() * 5 + 1);
    }
    setExpectedDurations(durations, true);
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
   *
   * This assumes that the world is already set and is mainly used for testing
   * purposes.
   */
  public String generateStatement(String action, MinecraftObject obj, Collection<String> features)
      throws ParserException {
    return generateStatement(action, obj.toString(), features);
  }

  /**
   * Builds an instruction for the target object in the given world.
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
    return target.getVerb() + " " +
        treeToReferringExpression(generateReferringExpressionTree(world, target, it, lastOrientation));
  }

  /**
   * Builds an RE tree for the target object in the given world.
   * @param world A set of objects already in the world.
   * @param target The object (not yet part of the world) that should be created by the user
   * @param it Every object of the world that could be described by "it"
   * @param lastOrientation The last observed orientation of the user.
   * @return A derivation tree from which the string interpretation can generate the correct RE
   */
  public Tree<String> generateReferringExpressionTree(Set<MinecraftObject> world,
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
    relations.add(new Relation("target", target));
    try {
      setRelations(relations);
      return generateStatementTree(target.toString(), target.getFeaturesStrings());
    } catch (ParserException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Converts the derivation tree to an instruction via the
   * homomorphism to the string interpretation.
   */
  public String treeToReferringExpression(Tree<String> tree) {
    Tree<String> stringTree = strI.getHomomorphism().apply(tree);
    return String.join(" ", strI.getAlgebra().evaluate(stringTree));
  }

  /**
   * Builds a statement that represents building objName using the action.
   * The features define how objName should be described.
   */
  public String generateStatement(String action, String objName, Collection<String> features)
      throws ParserException {
    var bestTree = generateStatementTree(objName, features);
    String ret = "**NONE**";
    if (bestTree != null) {
      ret = treeToReferringExpression(bestTree);
    }
    return action + " " + ret;
  }
  
  /**
   * Produces a derivation tree that encodes an indefinite referential expression
   * to the object objName, describing features of objName.
   * Features are a set of possible feature combinations to describe objName.
   * Assumes that the current state of the world was already set via {@link #setRelations}.
   */
  protected Tree<String> generateStatementTree(String objName, Collection<String> features)
      throws ParserException {
    
    logger.debug("generating a statement for this model: " + (refA.getModel().toString()));
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
      var ta = automaton.intersect(refO).intersect(semO);
      ta = ta.asConcreteTreeAutomaton();
      bestTree =  ta.languageIterator(LogDoubleArithmeticSemiring.INSTANCE).next();
    } else {
      TreeAutomaton<Pair<String, Set<List<String>>>> ta =
          automaton.intersect(refO);
      ta = ta.asConcreteTreeAutomaton();
      // bestTree = ta.viterbi(AdditiveMinCostViterbiSemiring.INSTANCE);
      bestTree = ta.languageIterator(LogDoubleArithmeticSemiring.INSTANCE).next();
    }
    // TODO: Ask alexander what this was supposed to do and why it resulted in different
    // outputs than the line above together with building the stringTree below.
    // TreeAutomaton<List<String>> outputChart = irtg.decodeToAutomaton(strI, ta);
    // Tree<String> bestTree = outputChart.viterbi();
    return bestTree;
  }

  /*
  ===================================
  BEGIN DEBUG AND TEST HELPER METHODS
  ===================================
   */


  /**
   * Returns a tree that could have generated the instruction.
   * This is a debug method and not meant for production use.
   */
  public Tree<String> getTreeForInstruction(List<String> instruction) {
    Intersectable<List<String>> invhom = strI.parse(instruction);
    var ta = irtg.getAutomaton().intersect(invhom);
    if (ta.languageIterator().hasNext()) {
      return ta.languageIterator().next();
    } else {
      return null;
    }
  }

  /**
   * Checks whether the instruciton is in principle derivable from the grammar.
   * does not check whether the instruction fits the current world; mainly used
   * for debug.
   */
  public boolean isDerivable(List<String> instruction) {
    return isDerivable(instruction, irtg.getAutomaton());
  }

  /**
   * Checks whether the instruction can be derived in the automaton.
   * Note: The instruction needs to be tokenized according to the rules in the automaton,
   * i.e. if a rule has *("to the left", ?1), "to the left" has to be a single entry
   * in the instruction list.
   *
   * Used for debugging.
   */
  public boolean isDerivable(List<String> instruction, TreeAutomaton<String> automaton) {
    Intersectable<List<String>> invhom = strI.parse(instruction);
    var ta = automaton.intersect(invhom);
    ta.analyze();
    for (Tree<String> tree :ta.languageIterable()) {
      System.out.println(semA.representAsString(semI.interpret(tree)));
      System.out.println(refA.representAsString(refI.interpret(tree)));
      System.out.println("weight: " + ta.getWeight(tree, LogDoubleArithmeticSemiring.INSTANCE));
    }
    //System.out.println(ta.getWeight(ta.languageIterable().iterator().next()));
    return ta.languageIterable().iterator().hasNext();
  }
}
