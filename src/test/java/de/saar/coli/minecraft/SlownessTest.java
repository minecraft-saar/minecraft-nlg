package de.saar.coli.minecraft;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.saar.coli.minecraft.relationextractor.Block;
import de.saar.coli.minecraft.relationextractor.Floor;
import de.saar.coli.minecraft.relationextractor.MinecraftObject;
import de.saar.coli.minecraft.relationextractor.Railing;
import de.saar.coli.minecraft.relationextractor.Relation;
import de.saar.coli.minecraft.relationextractor.Relation.Orientation;
import de.saar.coli.minecraft.relationextractor.Row;
import de.saar.coli.minecraft.relationextractor.UniqueBlock;
import de.saar.coli.minecraft.relationextractor.Wall;
import de.up.ling.irtg.Interpretation;
import de.up.ling.irtg.InterpretedTreeAutomaton;
import de.up.ling.irtg.algebra.ParserException;
import de.up.ling.irtg.algebra.SubsetAlgebra;
import de.up.ling.irtg.semiring.LogDoubleArithmeticSemiring;
import de.up.ling.tree.ParseException;
import de.up.ling.tree.Tree;
import de.up.ling.tree.TreeParser;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SlownessTest {

  @Test
  public void testSecondWall() {
    var world = new HashSet<MinecraftObject>();
    world.addAll(createInitialWorld());
    world.addAll(createFirstWall());
    world.addAll(createSecondWall());
    var lastBlock = new Block(3,1,3);
    world.add(lastBlock);
    var realizer = MinecraftRealizer.createRealizer();
    realizer.generateReferringExpressionTree(world,
        new Wall("wall3",0,0,3,3,1,3),
        Set.of(lastBlock),
        Orientation.XMINUS);
  }

  public Set<MinecraftObject> createInitialWorld() {
    return Set.of(
        new UniqueBlock("blue_wool", 0,0,0),
        new UniqueBlock("black_wool", 3,0,0),
        new UniqueBlock("red_wool", 3,0,3),
        new UniqueBlock("yellow_wool", 0,0,3)
        );
  }
  
  public Set<MinecraftObject> createFirstWall() {
    var result = new HashSet<MinecraftObject>();
    result.add(new Wall("wall1",0,0,0,3,1,0));
    result.add(new Block(1,0,0));
    result.add(new Block(2,0,0));
    for (int x = 0; x < 4; x++){
      result.add(new Block(x,1,0));
    }
    return result;
  }

  public List<MinecraftObject> createSecondWall() {
    var result = new ArrayList<MinecraftObject>();
    result.add(new Wall("wall2",3,0,0,3,1,3));
    result.add(new Block(3,0,1));
    result.add(new Block(3,0,2));
    for (int z = 0; z < 3; z++){
      result.add(new Block(3,1, z));
    }
    return result;
  }

  @Test
  public void testSecondRow() {
 /*     var weights = """
          {"iblock":-10.544539976514358,"blackw":-9.195246648846501,"totopof":-6.067345819367511,"behindorientlrblock":-6.3556988766626805,"betweenrlBigBlock":-1.459893032992701,"prev":-1.3443566844784995,"tobottom":-7.090656622962407,"long3":-8.241409459403428,"ibridge":-2.7320511542670216,"railing":-7.536220381326913,"threefront":-10.697516124689269,"twofront":-2.6757863143562437,"leftorientlr":-9.40828509710086,"fourabove":-1.8447847396478525,"block":-7.992853072705284,"long4":-4.338952899579345,"rightorientlrblock":-3.3863697156051424,"threetop":-9.156129446033663,"frontorientaway":-2.507025436410946,"dnp":-10.71960551660115,"fromlefttoright":-3.306080056105362,"twoabove":-4.188394952538074,"height2":-9.623053836227752,"redw":-1.8903460512945849,"height4":-7.640964212490001,"left":-7.314875462253289,"height3":-1.4271404274885193,"behindorientlr":-2.451516518195458,"rightorientaway":-5.446812496691118,"high4":-5.300908064214337,"frontleftcorner":-1.041377318070619,"fiveright":-7.1757715611703965,"high2":-9.435798233963016,"twotop":-7.937624514636349,"high3":-1.5742285757748589,"bridge":-8.835341589259887,"threeright":-5.688886655941653,"loc":-9.591516225624932,"betweenbfBigBlock":-8.981166511781634,"yellow":-6.254357784466052,"iwall":-8.702103689907183,"fromaway":-7.637383017403341,"top":-1.606347643059525,"fivefront":-10.282414651154047,"nexttol":-5.416539563443123,"betweenbf":-3.227206022805267,"betweenrl":-8.91783478253883,"right":-2.9663474359489364,"orientaway":-1.7773255165303117,"blue":-9.313270133993614,"otherside":-2.0154163386356196,"fromtopof":-10.003189581439882,"frontorientlrblock":-2.6697232269344786,"to":-6.341964523099598,"fivebehind":-5.076766595935364,"upperrightcorner":-2.5473214973697615,"fourleft":-5.307560260847779,"leftorientaway":-7.813197503411094,"rightorientlr":-6.8835280697592935,"red":-8.638846965143781,"frontorientawayblock":-10.269010140916805,"backtleftcorner":-9.730639963613065,"frontorientlr":-6.950029624110012,"from":-6.14421526279836,"nexttor":-1.4079988441325688,"fourtop":-3.6525733285363042,"betweenlr":-9.04592804510775,"below":-3.40172070234818,"fourright":-6.040452647763095,"backrightcorner":-1.1227814908749736,"othersideof":-9.50247138229809,"threeabove":-5.150332301713281,"length3":-4.095523378453253,"betweenlrBigBlock":-7.41430409298006,"toright":-10.842207812698284,"leftorientawayblock":-8.11031723905981,"fiveabove":-8.228842369105397,"length4":-3.0845342544295042,"fourfront":-6.402443093233993,"fourbehind":-9.712475843959439,"threeleft":-9.641143554760644,"leftorientlrblock":-2.3800416850070096,"ifloor":-1.0924265688970771,"tworight":-5.835966160655978,"behind":-4.979180004150703,"np":-5.153979634531534,"bluew":-7.611696737435151,"threebehind":-8.942023868831356,"yelloww":-1.260520449801448,"tohere":-2.4966560345130766,"row":-4.9259954462576,"floor":-6.387639247670735,"behindorientawayblock":-4.706129587202813,"behindorientaway":-1.4558442550451036,"irow":-8.240900434901747,"twoleft":-6.908213359416959,"green":-3.2823883831636236,"irailing":-1.687308108138136,"upperleftcorner":-5.247747425113512,"orangew":-7.160464502564604,"betweenfb":-5.471916359878061,"orange":-10.734532878292306,"frontrightcorner":-5.925438064523918,"rightorientawayblock":-3.7324237681364627,"fivetop":-6.743191962986691,"obj":-5.808706510742994,"topsameshape":-9.488082024803884,"twobehind":-4.370834554413981,"fiveleft":-10.385048164529428,"front":-10.735831640208128,"wall":-8.231966402360735,"betweenfbBigBlock":-4.003749290639091}
          """;

      var weightMap = new Gson().<Map<String, Double>>fromJson(weights,
          new TypeToken<Map<String, Double>>() {
          }.getType());
*/
      var world = new HashSet<MinecraftObject>();

      world.add(new Block(6, 67, 6));
      world.add(new Block(7, 67, 6));
      world.add(new Block(6, 67, 7));
      world.add(new Block(8, 67, 6));
      world.add(new Block(6, 67, 8));
      world.add(new Block(9, 67, 6));
      world.add(new Block(6, 67, 9));
      world.add(new Block(9, 67, 7));
      world.add(new Block(7, 67, 9));
      world.add(new Block(9, 67, 8));
      world.add(new Block(8, 67, 9));
      world.add(new Block(9, 67, 9));
      world.add(new Wall("wall", 6, 66, 6, 6, 67, 9));
      world.add(new Block(6, 68, 6));
      world.add(new Block(6, 68, 7));
      world.add(new UniqueBlock("blue_wool", 6, 66, 6));
      world.add(new Block(6, 68, 8));
      world.add(new Block(7, 66, 6));
      world.add(new Block(6, 66, 7));
      world.add(new Block(6, 68, 9));
      world.add(new Block(8, 66, 6));
      world.add(new Block(6, 66, 8));
      world.add(new UniqueBlock("yellow_wool", 6, 66, 9));
      world.add(new UniqueBlock("black_wool", 9, 66, 6));
      world.add(new Block(9, 66, 7));
      world.add(new Block(7, 66, 9));
      world.add(new Block(9, 66, 8));
      world.add(new Block(8, 66, 9));
      world.add(new UniqueBlock("red_wool", 9, 66, 9));
      world.add(new Wall("wall", 6, 66, 6, 9, 67, 6));
      world.add(new Wall("wall", 6, 66, 9, 9, 67, 9));

      var prevrow = new Row("row", 6, 6, 6, 9, 68);

      world.add(prevrow);
      world.add(new Wall("wall", 9, 66, 6, 9, 67, 9));
      var realizer = MinecraftRealizer.createRealizer();
/*
      for (var kv : weightMap.entrySet()) {
        kv.setValue(-kv.getValue());
      }

      realizer.setExpectedDurations(weightMap, true);
*/
      realizer.generateReferringExpressionTree(world,
          new Row("row", 7, 6, 7, 9, 68),
          Set.of(prevrow),
          Orientation.XPLUS
      );
  }
}
