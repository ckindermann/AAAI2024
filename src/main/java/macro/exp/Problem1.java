package macro.exp;

import java.io.*;
import java.nio.file.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.*;
import macro.abbr.explicit.*;
import macro.minimization.fixed.*;
import macro.ont.*;
import macro.parser.*;
import macro.structure.*;
import macro.structure.nodes.*;
import macro.term.*;
import macro.util.*;
import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.traverse.*;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.*;

/** Implementation for solving Problem 1 as described in the paper. */
public class Problem1 {

  private static final DecimalFormat df = new DecimalFormat("0.00");

  /**
   * Main method for running the implementation for Problem 1.
   *
   * @param args the command line arguments: ontologyFilePath outputPath
   */
  public static void main(String[] args) {

    String ontFilePath = args[0];
    String output = args[1];

    run(ontFilePath, output);
  }

  /**
   * Runs the implementation for Problem 1.
   *
   * @param ontFilePath the path to the ontology file
   * @param output the path to the output folder
   */
  public static void run(String ontFilePath, String output) {

    // load ontology
    File ontFile = new File(ontFilePath);
    OntologyLoader ontLoader = new OntologyLoader(ontFile, true);
    OWLOntology ontology = ontLoader.getOntology();

    // minimize ontology (and measure processing time)
    long startTime = System.nanoTime();

    RewritingSystem system = new RewritingSystem(ontology);

    long endTime = System.nanoTime();
    long duration = (endTime - startTime) / 1000000000; // divide by 1000000 to get milliseconds.
    System.out.println("Minimization Time: " + duration + " seconds");

    // get macrofication and macro definitions
    Set<AxiomMacrofication> macrofication = system.getMacrofication();
    MacroDefinitions macroDefinitions = system.getMacroDefinitions();

    // There are some differences to the presentation in the paper:
    // - In the paper, we split an ontology into two *disjoint* sets of axioms:
    //    * the set of axioms that are macro definitions, and
    //    * the set of axioms that are *not* macro definitions, i.e, the input language.
    // - Here, in the implementation the set returned by system.getMacrofication()
    //   is the set of *all* axioms of the input ontology.
    //   Note that this includes axioms that are interpreted as macro definitions.
    //   So, we iterate over this set and partition it into the two sets described in the paper.

    // partition set of AxiomMacrofications into 'language' and 'macro definitions'
    Set<AxiomMacrofication> ontologyLanguage = new HashSet<>();
    Set<AxiomMacrofication> ontologyMacroDefinitions = new HashSet<>();

    Set<OWLAxiom> macroDefinitionAxioms = macroDefinitions.getNonCyclicNonAmbiguousDefinitions();
    Map<OWLClass, Set<OWLClassExpression>> ambiguous = macroDefinitions.getAmbiguousClasses();

    // statistics for table 1
    int ontologySize = 0;
    int ontologyMinimizedLanguageSize = 0;
    int ontologyMacroDefinitionSize = 0;
    double practicalReduction = 0.0;
    int numberOfontologyLanguageAxioms = 0;
    int numberOfChangedAxioms = 0;
    int numberOfOntologyMacroDefinitions = 0;

    for (AxiomMacrofication m : macrofication) {

      // a term of the input language (an axiom of the ontology)
      OWLAxiom axiom = m.getAxiom();

      // An AxiomMacrofication contains information about an axiom's (term's)
      SyntaxTree axiomTree = m.getAxiomTree(); // original structure
      SyntaxTree expansionTree = m.getExpandedTree(); // (exhaustively) expanded structure
      SyntaxTree minimizedTree = m.getMinimizationTree(); // (minimal) macrofication

      // partition set of AxiomMacrofications
      if (macroDefinitionAxioms.contains(axiom)) {
        ontologyMacroDefinitions.add(m);
        ontologyMacroDefinitionSize += axiomTree.getSize(); // NB: definitions are not modified
      } else {
        ontologyLanguage.add(m);
        ontologyMinimizedLanguageSize += minimizedTree.getSize();
        if (axiomTree.getSize() > minimizedTree.getSize()) {
          numberOfChangedAxioms++;
        }
      }

      // every axiom counts to the size of the ontology
      ontologySize += axiomTree.getSize();
    }
    numberOfontologyLanguageAxioms = ontologyLanguage.size();
    numberOfOntologyMacroDefinitions = ontologyMacroDefinitions.size();

    practicalReduction =
        (double) (ontologyMinimizedLanguageSize + ontologyMacroDefinitionSize)
            / (double) ontologySize;
    practicalReduction = (1 - practicalReduction);

    String ontologyName = Paths.get(ontFilePath).getFileName().toString();

    String header = "P,O,size(O),size(L_M),size(M),Prop.Red.,#Axioms,#Ch.Axioms,#M";
    String results =
        "1,"
            + ontologyName
            + ","
            + ontologySize
            + ","
            + ontologyMinimizedLanguageSize
            + ","
            + ontologyMacroDefinitionSize
            + ","
            + df.format(practicalReduction)
            + ","
            + numberOfontologyLanguageAxioms
            + ","
            + numberOfChangedAxioms
            + ","
            + numberOfOntologyMacroDefinitions;

    String outputPath = output + "/" + ontologyName;
    IOHelper.createFolder(outputPath);

    IOHelper.writeAppend(header, outputPath + "/table1.csv");
    IOHelper.writeAppend(results, outputPath + "/table1.csv");
    IOHelper.writeAppend(duration + " (s)", outputPath + "/runningTime.csv");
  }
}
