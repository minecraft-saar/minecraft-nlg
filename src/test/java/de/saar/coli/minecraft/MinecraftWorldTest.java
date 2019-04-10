package de.saar.coli.minecraft;

import de.saar.basic.Pair;
import de.up.ling.irtg.automata.TreeAutomaton;
import org.junit.Test;

import de.up.ling.irtg.Interpretation;
import de.up.ling.irtg.InterpretedTreeAutomaton;
import de.up.ling.irtg.TemplateInterpretedTreeAutomaton;
import de.up.ling.irtg.algebra.SetAlgebra;
import de.up.ling.irtg.codec.TemplateIrtgInputCodec;
import de.up.ling.irtg.util.FirstOrderModel;
import de.up.ling.tree.Tree;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Arne Köhn
 */
public class MinecraftWorldTest {

    @Test
    public void testRunByHand() throws Exception {
		InputStream mcstream = Thread.currentThread().getContextClassLoader().getResourceAsStream("minecraft.irtg");
		InputStream worldstream = Thread.currentThread().getContextClassLoader().getResourceAsStream("testworld.json");
        TemplateInterpretedTreeAutomaton tirtg = new TemplateIrtgInputCodec().read(mcstream);
        FirstOrderModel mcModel = FirstOrderModel.read(new InputStreamReader(worldstream));
        InterpretedTreeAutomaton irtg = tirtg.instantiate(mcModel);
        SetAlgebra ref = (SetAlgebra) irtg.getInterpretation("ref").getAlgebra();
        Interpretation<Set<List<String>>> refI = (Interpretation<Set<List<String>>>)irtg.getInterpretation("ref");
        // put inputs here
        Set<List<String>> refInput = ref.parseString("{loc28}");
        Interpretation<List<String>> strI = (Interpretation<List<String>>)irtg.getInterpretation("string");
        ref.setModel(mcModel);
        TreeAutomaton<Pair<String, Set<List<String>>>> ta = irtg.parseSimple(refI, refInput);
        TreeAutomaton<List<String>> outputChart = irtg.decodeToAutomaton(strI, ta);
        Iterator<Tree<String>> it = outputChart.languageIterator();

        assert(it.hasNext());
        // assert strI.getAlgebra().evaluate(it.next()

        if (it.hasNext()) {
            System.out.println(strI.getAlgebra().evaluate(it.next()));
        }
        //def chart = irtg.parseSimple(strI, ["put the block",  "on top of",  "the",  "orange block"])
        //def derivTree = chart.viterbi()
        //print(refI.interpret(derivTree))
    }

	/*  
	@Test
    public void testMCRealizer() {
        InterpretedTreeAutomaton irtg = new IrtgInputCodec().read(new StringBufferInputStream(MCTIRTG))
        FirstOrderModel mcModel = FirstOrderModel.read(new StringReader(TESTJSON))
        def mcr = new MinecraftRealizer(irtg, mcModel)
        def res = mcr.generateStatement("put", "loc28")
        assert res.length() > 10
    }
*/
}

