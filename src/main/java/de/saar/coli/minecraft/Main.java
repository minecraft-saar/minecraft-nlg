package de.saar.coli.minecraft;

import de.up.ling.irtg.algebra.ParserException;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;


@Command(name = "mcrealizer", mixinStandardHelpOptions = true, version = "Ḿinecraft realizer 0.1")
public class Main implements Runnable {

  @Option(names = {"-c",
      "--continuous"}, description = "Read goal from stdin, return to stdout, don't exit")
  private boolean continuous = false;

  @Option(names = {"-t", "--tirtg"}, required = true, description = "Path to the tirtg to use")
  private File tirtgFile;

  @Option(names = {"-m", "--model"}, required = true, description = "Path to the model to use")
  private File modelFile;

  @Parameters
  String[] inputLocation;

  MinecraftRealizer mcr;

  /**
   * Run the main loop of the minecraft realizer.
   */
  public void run() {
    try {

      mcr = MinecraftRealizer.createRealizer(tirtgFile, modelFile);
      if (continuous) {
        BufferedReader is = new BufferedReader(new InputStreamReader(System.in));
        String input = is.readLine();
        while (input != null) {
          System.out.println(processInput(input));
          input = is.readLine();
        }
      } else {
        if (inputLocation == null || inputLocation.length != 1) {
          System.err.println(
              "You need to either run in continuous mode or provide"
                  + " a single location via positional argument!");
          System.exit(1);
        }
        System.out.println(processInput(inputLocation[0]));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private String processInput(String input) throws ParserException {
    input = input.trim();
    String[] actionObjectAspects = input.split(":");
    String aspects = "";
    if (actionObjectAspects.length == 3) {
      aspects = actionObjectAspects[2];
    }
    return mcr.generateStatement(actionObjectAspects[0], actionObjectAspects[1], aspects);
  }

  public static void main(String[] args) {
    CommandLine.run(new Main(), args);
  }

  /*    */
}
