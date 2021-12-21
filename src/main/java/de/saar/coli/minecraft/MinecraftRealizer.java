package de.saar.coli.minecraft;

import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import de.saar.basic.Pair;

import de.saar.coli.minecraft.relationextractor.Features;
import de.saar.coli.minecraft.relationextractor.Relation;
import de.saar.coli.minecraft.relationextractor.MinecraftObject;
import de.saar.coli.minecraft.relationextractor.Relation.Orientation;
import de.saar.coli.minecraft.relationextractor.UniqueBlock;
import de.up.ling.irtg.Interpretation;
import de.up.ling.irtg.InterpretedTreeAutomaton;
import de.up.ling.irtg.algebra.ParserException;
import de.up.ling.irtg.algebra.SetAlgebra;
import de.up.ling.irtg.algebra.SubsetAlgebra;
import de.up.ling.irtg.automata.DepthLimitingTreeAutomaton;
import de.up.ling.irtg.automata.Intersectable;
import de.up.ling.irtg.automata.TreeAutomaton;
import de.up.ling.irtg.codec.IrtgInputCodec;
import de.up.ling.irtg.semiring.LogDoubleArithmeticSemiring;
import de.up.ling.irtg.util.FirstOrderModel;
import de.up.ling.tree.Tree;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.tinylog.Logger;


public class MinecraftRealizer {
  private static final String FEATURES = Arrays.stream(Features.values())
      .map(Features::toString)
      .map(String::toLowerCase)
      .collect(Collectors.joining("+")) + "+indef";

  private final InterpretedTreeAutomaton irtg;
  private final Interpretation<List<String>> strI;
  private final Interpretation<Set<List<String>>> refI;
  private final Interpretation<BitSet> semI;
  private final SetAlgebra refA;
  private SubsetAlgebra semA;
  private Map<String, Double> weights;

  /**
   * Builds a realizer from given model and grammar files.
   * This essentially handles reading the model and grammar for you.
   * @throws Exception if something goes wrong
   */
  public static MinecraftRealizer createRealizer(File irtgFile, File modelFile) throws Exception {
    MinecraftRealizer mcr = createRealizer(irtgFile);
    try (
        FileInputStream modelin = new FileInputStream(modelFile);
    ) {
      FirstOrderModel mcModel = FirstOrderModel.read(new InputStreamReader(modelin));
      mcr.setModel(mcModel);
      return mcr;
    }
  }

  /**
   * Builds a realizer from a given model and grammar,
   * which are being read from input streams.
   *
   * @param irtgInputStream
   * @param modelReader
   * @return
   * @throws Exception
   */
  public static MinecraftRealizer createRealizer(InputStream irtgInputStream, Reader modelReader) throws Exception {
    MinecraftRealizer mcr = createRealizer(irtgInputStream);
    FirstOrderModel mcModel = FirstOrderModel.read(modelReader);
    mcr.setModel(mcModel);
    return mcr;
  }

  /**
   * Builds a realizer from given model and grammar files.
   * This essentially handles reading the model and grammar for you.
   * @throws Exception if something goes wrong
   */
  public static MinecraftRealizer createRealizer(File irtgFile) throws Exception {
    try (
        FileInputStream irtgin = new FileInputStream(irtgFile);
    ) {
      return createRealizer(irtgin);
//      InterpretedTreeAutomaton irtg = new IrtgInputCodec().read(irtgin);
//      return new MinecraftRealizer(irtg);
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

  public void setModel(FirstOrderModel model) {
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
    this.weights = weights;
    irtg.getAutomaton().setWeights(weights, enforceCompleteUpdate);
  }

  public Map<String, Double> getWeights() {
    return weights;
  }

  public String getWeightsAsJson() {
    if (weights == null) {
      return "{}";
    }
    return new Gson().toJson(weights);
  }

  public void randomizeExpectedDurations() {
    randomizeExpectedDurations(10);
  }

  public void randomizeExpectedDurations(int span) {
    var auto = irtg.getAutomaton();
    var symbols = auto.getSignature().resolveSymbolIDs(auto.getAllLabels());
    var rand = new Random();
    Map<String, Double> durations = new HashMap<>();
    for (String symbol: symbols) {
      durations.put(symbol,  rand.nextDouble() * span + 1);
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
  public String generateStatement(String action, MinecraftObject obj, Collection<String> features) throws ParserException {
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
  protected Tree<String> generateStatementTree(String objName, Collection<String> features) throws ParserException {
    Logger.debug("generating a statement for this model: {}", refA.getModel());

    Set<List<String>> refInput = refA.parseString("{" + objName + "}");

    Intersectable<BitSet> semO = null;

    if (semI != null) {
      Set<BitSet> semInputs = features.stream().map((String x) -> {
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

    Iterator<Tree<String>> langIt;

    /*
      We use a depth limiting automaton to not have cycles.  Initially we
       use a depth of 8, which is enough for most derivations.
       Sometimes the only existing trees have a depth of more than 8.
       We therefore increase the depth by two until we find a tree.
       A depth of 20 is already very deep so after that we give up.

       We initially use a smaller depth because the search space grows
       with depth and therefore the time it takes to decode.

       We might sometimes not get the optimal tree (e.g. optim has
       depth 9 but there exists a tree with depth 8) but that should
       be pretty rare.
    */
    
    Tree<String> result = null;
    TreeAutomaton ta = null;
    int minMaxdepth = 0;

    if (semO != null) {
      ta = automaton.intersect(refO).intersect(semO);
      minMaxdepth = 8;
    } else {
      ta = automaton.intersect(refO);
      minMaxdepth = 6;
    }

    for (int maxdepth = 8; maxdepth < 20; maxdepth +=2) {
      var dlta = new DepthLimitingTreeAutomaton<>(ta, maxdepth);
      langIt = dlta.languageIterator(LogDoubleArithmeticSemiring.INSTANCE);
      if (langIt.hasNext()) {
        return langIt.next();
      }
    }


    return null;
  }

  /**
   * Returns the weight (i.e. expected completion duration) of a derivation tree.
   */
  public double getWeightForTree(Tree<String> tree) {
    if (tree == null) {
      return Double.NaN;
    }
    return irtg.getAutomaton().getWeight(tree, LogDoubleArithmeticSemiring.INSTANCE);
  }

  private static int NEXT_ID = 1;
  private static final boolean LOG_REALIZER_IN_FILE = false;

  /**
   * computes an instruction for building {@code mco} in {@code world} and returns
   * the predicted cost in terms of negative rule weights for the instruction tree.
   * @param world set of objects already in the world (includes blocks of HLOs)
   * @param mco the MineCraft Object to be built
   * @param it all objects that can be referred to as "it" (e.g. "the previous wall")
   * @return predicted cost in seconds to completion of the instruction
   */
  public double estimateCostForPlanningSystem(Set <MinecraftObject>world, MinecraftObject mco, Set<MinecraftObject> it) {
    java.io.PrintWriter f = null;
    double ret = Double.MAX_VALUE;

    try {
      if (LOG_REALIZER_IN_FILE) {
        String filename = "nlg-input-" + (NEXT_ID++) + ".txt";
        f = new java.io.PrintWriter(new java.io.FileWriter(filename));
        System.err.printf("%s -> %s\n", mco, filename);
        f.printf("realize: %s\n", mco);
        f.printf("it: %s\n", it);
      }

      Tree<String> derivationTree = generateReferringExpressionTree(world, mco, it, Orientation.ZMINUS);

      if (derivationTree == null) {
        if (LOG_REALIZER_IN_FILE) {
          f.println(" -> null\n[null]\n");
        }
      } else {
        ret = -getWeightForTree(derivationTree);
        String s = treeToReferringExpression(derivationTree);
        if (LOG_REALIZER_IN_FILE) {
          f.printf(" -> %s\n", derivationTree);
          f.printf("%s\n\n", s);
        }
      }
    } catch(java.io.IOException e) {
    } finally {
      if( LOG_REALIZER_IN_FILE ) {
        f.println(world);
        f.println(refA.getModel());
        f.flush();
        f.close();
      }

      return ret;
    }
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

  /**
   * Reads a world in CSV format and converts it into MinecraftObjects.
   *
   * Each of these objects is a UniqueBlock of the material that is specified in the
   * fourth column.
   *
   * The method is suitable for the CSV format used in shared-resources/.../*.csv.
   *
   * @param reader
   * @return
   * @throws IOException
   */
  public Set<MinecraftObject> readWorld(Reader reader) throws IOException {
    BufferedReader bufferedReader = new BufferedReader(reader);
    String line = null;
    Set<MinecraftObject> ret = new HashSet<>();

    while( (line = bufferedReader.readLine()) != null ) {
      String[] data = line.split(",");
      int x = Integer.parseInt(data[0]);
      int y = Integer.parseInt(data[1]);
      int z = Integer.parseInt(data[2]);
      String blockType = data[3].toLowerCase();

      if (x < 0 || y < 0 || z < 0) {
        continue;
      }

      if( ! "water".equals(blockType) ) {
        ret.add(new UniqueBlock(blockType, x, y, z));
      }
    }

    return ret;
  }

}
