package macro.cli;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.*;
import java.util.logging.Logger;
import java.util.stream.*;
import macro.exp.*;
import macro.roundtrip.*;
import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.traverse.*;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.*;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.util.*;

/** A class to demonstrate the functionality of the library. */
public class CLI {

  private static final Logger log = Logger.getLogger(String.valueOf(CLI.class));

  public static void main(String[] args) throws IOException, Exception {

    if (args.length < 3 || args.length > 4) {
      System.out.println(
          "Incorrect arguments. Required input: \n"
              + "\t 'solve 1 ontology output' (to get statistics on solutions of Problem 1-3)\n"
              + "\t 'macrofy ontology output' (to macrofy an ontology)\n"
              + "\t 'expand macrofication definitions output' "
              + "(to expand a macrofication with fixed-point definitions)\n"
              + "\t 'fixedpoint definitions output' "
              + "(to expand a macro definitions to their fixed-point expansions)\n"
              + "\t 'compare ontology1 ontology2' "
              + "(to check whether two ontologies contain the same class expression axioms)");
      System.exit(0);
    }

    String command = args[0];

    if (command.equals("solve")) {
      if (args.length != 4) {
        System.out.println(
            "Incorrect arguments. Required input for 'solve': problem ontology output");
        System.exit(0);
      }

      String problem = args[1];
      String ontFilePath = args[2];
      String outputPath = args[3];

      if (!problem.equals("1") && !problem.equals("2") && !problem.equals("3")) {
        System.out.println(
            "Please provide a number between 1 and 3 to specify which problem to solve.");
        System.exit(0);
      }

      /*
      File ontFile = new File(ontFilePath);
      String ontologyName = Paths.get(ontFilePath).getFileName().toString();
      File outputFile = new File(outputPath + "/" + ontologyName);
      if (outputFile.exists()) {
        System.out.println(
            "Output destination at '"
                + outputPath
                + "' for '"
                + ontologyName
                + "' already exists.");
        System.exit(0);
      }*/

      solve(problem, ontFilePath, outputPath);
      System.exit(0);
    }

    if (command.equals("macrofy")) {

      if (args.length != 3) {
        System.out.println("Incorrect arguments. Required input for 'macrofy': ontology output");
        System.exit(0);
      }

      String ontFilePath = args[1];
      String outputPath = args[2];

      Macrofy.run(ontFilePath, outputPath);
      System.exit(0);
    }

    if (command.equals("expand")) {

      if (args.length != 4) {
        System.out.println(
            "Incorrect arguments. Required input for 'expand': macrofication definitions output");
        System.exit(0);
      }

      String ontFilePath = args[1];
      String definitionPath = args[2];
      String outputPath = args[3];

      Expand.run(ontFilePath, definitionPath, outputPath);
      System.exit(0);
    }

    if (command.equals("fixedpoint")) {

      if (args.length != 3) {
        System.out.println(
            "Incorrect arguments. Required input for 'fixedpoint': definitions output");
        System.exit(0);
      }

      String definitionPath = args[1];
      String outputPath = args[2];

      FixedPoint.run(definitionPath, outputPath);
      System.exit(0);
    }

    if (command.equals("compare")) {

      if (args.length != 3) {
        System.out.println(
            "Incorrect arguments. Required input for 'compare': ontology1 ontology2");
        System.exit(0);
      }

      String ontFilePath1 = args[1];
      String ontFilePath2 = args[2];

      Compare.run(ontFilePath1, ontFilePath2);
      System.exit(0);
    }

    System.out.println(
        "Incorrect command. Allowed commands are: 'solve, macrofy, expand, compare'");
  }

  private static void solve(String problem, String ontFilePath, String outputPath) {

    if (problem.equals("1")) {
      System.out.println("Computing Size-Minimal Encoding for Problem 1");
      Problem1.run(ontFilePath, outputPath);
    }

    if (problem.equals("2")) {
      System.out.println("Computing Size-Minimal Encoding for Problem 2");
      Problem2.run(ontFilePath, outputPath);
    }

    if (problem.equals("3")) {
      System.out.println("Computing Size-Minimal Encoding for Problem 3");
      Problem3.run(ontFilePath, outputPath);
    }
  }
}
