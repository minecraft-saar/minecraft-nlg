package de.saar.coli.minecraft;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Set;

import de.up.ling.irtg.Interpretation;
import de.up.ling.irtg.InterpretedTreeAutomaton;
import de.up.ling.irtg.TemplateInterpretedTreeAutomaton;
import de.up.ling.irtg.algebra.ParserException;
import de.up.ling.irtg.algebra.SetAlgebra;
import de.up.ling.irtg.automata.TreeAutomaton;
import de.up.ling.irtg.codec.IrtgInputCodec;
import de.up.ling.irtg.codec.TemplateIrtgInputCodec;
import de.up.ling.irtg.util.FirstOrderModel;
import de.up.ling.tree.Tree;

public class MinecraftRealizer {

    private final InterpretedTreeAutomaton irtg;
    private final Interpretation<List<String>> strI;
    private final Interpretation<Set<List<String>>> refI;
    private final SetAlgebra refA;

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

    public MinecraftRealizer(InterpretedTreeAutomaton irtg, FirstOrderModel mcModel) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        this.irtg = irtg;
        refI = (Interpretation<Set<List<String>>>) irtg.getInterpretation("ref");
        refA = (SetAlgebra)refI.getAlgebra();
        // Interpretation<Set<List<String>>> refI = irtg.getInterpretation("ref");
        // put inputs here
        strI = (Interpretation<List<String>>) irtg.getInterpretation("string");
        refA.setModel(mcModel);
    }

    public String generateStatement(String action, String location) throws ParserException {
        String ret = "**NONE**";
        Set<List<String>> refInput = refA.parseString("{"+location+"}");
        TreeAutomaton ta = irtg.parseSimple(refI, refInput);
        TreeAutomaton<List<String>> outputChart = irtg.decodeToAutomaton(strI, ta);
        Tree<String> bestTree = outputChart.viterbi();
        if (bestTree != null)
            ret = String.join(" ", strI.getAlgebra().evaluate(bestTree));
        return ret;
    }
    /*
    TemplateInterpretedTreeAutomaton tirtg = new TemplateIrtgInputCodec().read(MCTIRTG)
    FirstOrderModel mcModel = FirstOrderModel.read(new StringReader(TESTJSON))
    InterpretedTreeAutomaton irtg = tirtg.instantiate(mcModel)
    SetAlgebra ref = (SetAlgebra) irtg.getInterpretation("ref").getAlgebra()
    Interpretation<Set<List<String>>> refI = irtg.getInterpretation("ref")
    // put inputs here
    Set<List<String>> refInput = ref.parseString("{loc28}")
    Interpretation<List<String>> strI = irtg.getInterpretation("string")
        ref.setModel(mcModel);
    def ta = irtg.parseSimple(refI, refInput)
    def outputChart = irtg.decodeToAutomaton(strI, ta)
    Iterator<Tree<String>> it = outputChart.languageIterator();

        if (it.hasNext()) {
        print(strI.getAlgebra().evaluate(it.next()))
    }
    def chart = irtg.parseSimple(strI, ["put the block",  "on top of",  "the",  "orange block"])
    def derivTree = chart.viterbi()
    print(refI.interpret(derivTree))
*/

}
