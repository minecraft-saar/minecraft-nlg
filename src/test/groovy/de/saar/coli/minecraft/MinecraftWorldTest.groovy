package de.saar.coli.minecraft

import de.saar.basic.Pair
import de.up.ling.irtg.Interpretation
import de.up.ling.irtg.InterpretedTreeAutomaton
import de.up.ling.irtg.TemplateInterpretedTreeAutomaton
import de.up.ling.irtg.algebra.Algebra
import de.up.ling.irtg.algebra.SetAlgebra
import de.up.ling.irtg.algebra.SubsetAlgebra
import de.up.ling.irtg.codec.IrtgInputCodec
import de.up.ling.irtg.codec.TemplateIrtgInputCodec
import de.up.ling.irtg.util.FirstOrderModel
import de.up.ling.irtg.util.Util
import de.up.ling.tree.Tree
import org.junit.Test

/**
 *
 * @author Arne Köhn
 */
class MinecraftWorldTest {

    @Test
    public void testRunByHand() {
        TemplateInterpretedTreeAutomaton tirtg = new TemplateIrtgInputCodec().read(new StringBufferInputStream(ExampleWorlds.MCTIRTG))
        FirstOrderModel mcModel = FirstOrderModel.read(new StringReader(ExampleWorlds.TESTJSON))
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
        print("foobar")

        assert it.hasNext()
        // assert strI.getAlgebra().evaluate(it.next()

        if (it.hasNext()) {
            print(strI.getAlgebra().evaluate(it.next()))
        }
        //def chart = irtg.parseSimple(strI, ["put the block",  "on top of",  "the",  "orange block"])
        //def derivTree = chart.viterbi()
        //print(refI.interpret(derivTree))
    }

    @Test
    public void testMCRealizer() {
        InterpretedTreeAutomaton irtg = new IrtgInputCodec().read(new StringBufferInputStream(MCTIRTG))
        FirstOrderModel mcModel = FirstOrderModel.read(new StringReader(TESTJSON))
        def mcr = new MinecraftRealizer(irtg, mcModel)
        def res = mcr.generateStatement("put", "loc28")
        assert res.length() > 10
    }


    private final static String MCTIRTG = '''
interpretation string: de.up.ling.irtg.algebra.StringAlgebra
interpretation ref: de.up.ling.irtg.algebra.SetAlgebra
interpretation sem: de.up.ling.irtg.algebra.SubsetAlgebra

// a referent is always the location the current subtree refers to
// Terminalsymbole dürfen nur in einer einzigen Regel auftreten

S! -> s(RefLoc)
  [string] *("put the block", ?1)
  [ref] ?1
  [sem] ?1 

RefBlock -> blockloc(RefLoc)
  [string] *("the block", ?1)
  [ref] ?1
  [sem] ?1

// RefLoc - refers to a position which may or may not contain a block

RefLoc -> top(RefBlock)
  [string] *("on top of", ?1)
  [ref] project_1(intersect_2(top-of,?1))
  [sem] ?1
  
RefLoc -> below(RefBlock)
  [string] *("below of", ?1)
  [ref] project_2(intersect_1(top-of,?1))
  [sem] ?1
  
RefLoc -> left(RefBlock)
  [string] *("to the left of", ?1)
  [ref] project_1(intersect_2(left-of,?1))
  [sem] ?1
  
RefLoc -> right(RefBlock)
  [string] *("to the right of", ?1)
  [ref] project_2(intersect_1(left-of,?1))
  [sem] ?1
  
RefLoc -> front(RefBlock)
  [string] *("infront of", ?1)
  [ref] project_1(intersect_2(in-front-of,?1))
  [sem] ?1
  
RefLoc -> behind(RefBlock)
  [string] *("behind of", ?1)
  [ref] project_2(intersect_1(in-front-of,?1))
  [sem] ?1
  

RefBlock -> uniqblockdesc(AdjBlock)
 [string] *("the", ?1)
 [ref] project_2(intersect_1(at, size_1(?1)))
 [sem] ?1

AdjBlock -> orangeblock
  [string] "orange block"
  [ref] orange
  [sem] EMPTYSET
  
AdjBlock -> yellowblock
  [string] "yellow block"
  [ref] yellow
  [sem] EMPTYSET

AdjBlock -> blueblock
  [string] "blue block"
  [ref] blue
  [sem] EMPTYSET

AdjBlock -> prevblock
  [string] "previous block"
  [ref] it
  [sem] EMPTYSET
'''
    
    private final static String TESTJSON = '''
 {
    "it": [["b8"]],
    "orange": [
      [
        "b7"
      ]
    ],
    "at": [
      [
        "b7",
        "loc25"
      ]
    ],
    "block": [
      [
        "b7"
      ]
    ],
    "top-of": [
      [
        "loc28",
        "loc25"
      ]
    ],
    "location": [
      [
        "loc25"
      ],
      [
        "loc28"
      ]
    ]
  }
'''

}

