package de.saar.coli.minecraft;

import de.saar.coli.minecraft.relationextractor.MinecraftObject;
import de.up.ling.tree.Tree;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Realizes the inputs from the given logfile as a derivation tree and a string.
 * This can be useful for debugging and grammar development purposes.<p>
 * 
 * The logfiles will be produced by {@link MinecraftRealizer#estimateCostForPlanningSystem(Set, MinecraftObject, Set)},
 * which is called by the planner, if you set the variable LOG_REALIZER_IN_FILE to true in that class.
 *
 */
@Command(name = "logrealizer", mixinStandardHelpOptions = true)
public class RealizeFromLogfile implements Runnable {
  @Option(names = {"-i", "--irtg"}, required = true, description = "Path to the IRTG to use")
  private File irtgFile;

  @Option(names = {"-l", "--logfile"}, required = true, description = "Logfile that was dumped by MinecraftRealizer#estimateCostForPlanningSystem")
  private File logfile;

  @Override
  public void run() {
    try {
      InputStream irtgStream = new FileInputStream(irtgFile);
      InputsInLogfile inputs = InputsInLogfile.read(new FileReader(logfile));

      MinecraftObject o = MinecraftObject.fromString(inputs.getTargetObject(), "stone"); //TODO: add types to file, realize
      MinecraftRealizer mcr = MinecraftRealizer.createRealizer(irtgStream, new StringReader(inputs.getJson()));
      Tree<String> bestTree = mcr.generateStatementTree(inputs.getTargetObject(), o.getFeaturesStrings());

      String s = "**NONE**";
      if (bestTree != null) {
        s = mcr.treeToReferringExpression(bestTree);
      }

      System.out.println(bestTree);
      System.out.println(s);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
//    mcr = MinecraftRealizer.createRealizer(irtgFile, modelFile);

  }

  private static class InputsInLogfile {
    private String targetObject, it, json;

    public static InputsInLogfile read(Reader r) throws IOException {
      BufferedReader br = new BufferedReader(r);
      List<String> lines = new ArrayList<>();
      String s;

      while ( (s = br.readLine()) != null ) {
        lines.add(s);
      }

      InputsInLogfile ret = new InputsInLogfile();
      ret.targetObject = lines.get(0).trim().split("\\s+")[1];
      ret.it = lines.get(1).trim().split("\\s+")[1];
      ret.json = lines.get(6).trim();
      return ret;
    }

    public String getTargetObject() {
      return targetObject;
    }

    public String getIt() {
      return it;
    }

    public String getJson() {
      return json;
    }

    @Override
    public String toString() {
      return "InputsInLogfile{" +
          "targetObject='" + targetObject + '\'' +
          ", it='" + it + '\'' +
          ", json='" + json + '\'' +
          '}';
    }
  }


  public static void main(String[] args) {
    CommandLine.run(new RealizeFromLogfile(), args);
  }

}
