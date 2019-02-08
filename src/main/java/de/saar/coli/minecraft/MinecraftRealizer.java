package de.saar.coli.minecraft;

import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.up.ling.irtg.Interpretation;
import de.up.ling.irtg.InterpretedTreeAutomaton;
import de.up.ling.irtg.TemplateInterpretedTreeAutomaton;
import de.up.ling.irtg.algebra.ParserException;
import de.up.ling.irtg.algebra.SetAlgebra;
import de.up.ling.irtg.automata.TreeAutomaton;
import de.up.ling.irtg.codec.TemplateIrtgInputCodec;
import de.up.ling.irtg.util.FirstOrderModel;
import de.up.ling.tree.Tree;

public class MinecraftRealizer {

    private final InterpretedTreeAutomaton irtg;
    private final Interpretation<List<String>> strI;
    private final Interpretation<Set<List<String>>> refI;
    private final SetAlgebra ref;

    public static MinecraftRealizer createRealizer(File tirtgFile, File modelFile) throws Exception {
        try (
                FileInputStream tirtgin = new FileInputStream(tirtgFile);
                FileInputStream modelin = new FileInputStream(modelFile);
        ) {
            TemplateInterpretedTreeAutomaton tirtg = new TemplateIrtgInputCodec().read(tirtgin);
            FirstOrderModel mcModel = FirstOrderModel.read(new InputStreamReader(modelin));
            return new MinecraftRealizer(tirtg, mcModel);
        }
    }

    public MinecraftRealizer(TemplateInterpretedTreeAutomaton tirtg, FirstOrderModel mcModel) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        irtg = tirtg.instantiate(mcModel);
        refI = (Interpretation<Set<List<String>>>) irtg.getInterpretation("ref");
        ref = (SetAlgebra)refI.getAlgebra();
        // Interpretation<Set<List<String>>> refI = irtg.getInterpretation("ref");
        // put inputs here
        strI = (Interpretation<List<String>>) irtg.getInterpretation("string");
        ref.setModel(mcModel);
    }

    public String generateStatement(String location) throws ParserException {
        String ret = "**NONE**";
        Set<List<String>> refInput = ref.parseString("{"+location+"}");
        TreeAutomaton ta = irtg.parseSimple(refI, refInput);
        TreeAutomaton outputChart = irtg.decodeToAutomaton(strI, ta);
        Iterator<Tree<String>> it = outputChart.languageIterator();
        if (it.hasNext()) {
            ret = String.join(" ", strI.getAlgebra().evaluate(it.next()));
        }
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
