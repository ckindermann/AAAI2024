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

/**
 * Implementation for solving Problem 1 as described in the paper.
 *
 * <p>The code is almost identical to Problem1 -- the only difference being the size-minimal
 * rewriting of macro definitions.
 */
public class Problem2 {

  private static final DecimalFormat df = new DecimalFormat("0.00");

  public static void main(String[] args) {

    String ontFilePath = args[0];
    String output = args[1];

    run(ontFilePath, output);
  }

  public static void run(String ontFilePath, String output) {

    // load ontology
    File ontFile = new File(ontFilePath);
    OntologyLoader ontLoader = new OntologyLoader(ontFile, true);
    OWLOntology ontology = ontLoader.getOntology();
    String ontologyName = Paths.get(ontFilePath).getFileName().toString();

    long startTime = System.nanoTime();

    // Rewriting system for Problem 2
    RewritingSystem system = new RewritingSystem(ontology);
    system.computeMinimalMacroDefinitions(); // construction of size-minimal macro definitions

    long endTime = System.nanoTime();
    long duration = (endTime - startTime) / 1000000000; // divide by 1000000 to get milliseconds.
    System.out.println("Minimization Time: " + duration + " seconds");

    // The set of 'AxiomMacrofications' corresponds to the set of (all) axioms of the input
    // ontology.
    // An AxiomMacrofication contains information about each axiom (term) w.r.t.
    // - its original structure
    // - its fully expanded structure
    // - its exhaustive macrofication
    // Note that this set includes axioms that are interpreted as macro definitions.
    // We will iterate over this set of AxiomMacrofications to measure both
    // - the 'macrofication' as defined in the paper as the part of an ontology that are not macro
    // definitions
    // - the macro definitions
    Set<AxiomMacrofication> macrofication = system.getMacrofication();
    MacroDefinitions macroDefinitions = system.getMacroDefinitions(); // minimise macro definitions

    // ... to make this explicit we use these two sets
    Set<AxiomMacrofication> ontologyMacroDefinitions = new HashSet<>();
    Set<AxiomMacrofication> ontologyLanguage = new HashSet<>();

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

      // the three components of an AxiomMacrofication
      SyntaxTree axiomTree = m.getAxiomTree();
      SyntaxTree expansionTree = m.getExpandedTree();
      SyntaxTree minimizedTree = m.getMinimizationTree();

      // partition set of AxiomMacrofications
      if (macroDefinitionAxioms.contains(axiom)) {
        ontologyMacroDefinitions.add(m);
        // the minimal size gets measured later (*)
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

    // (*) calculate size of macro definitions
    Map<OWLClass, OWLClassExpression> macro2minExpansion = system.getMinimialDefinitions();
    Map<OWLClass, OWLClassExpression> macro2oneStep = macroDefinitions.getClass2expansion();
    SyntaxTreeBuilder builder = new SyntaxTreeBuilder();
    for (Map.Entry<OWLClass, OWLClassExpression> entry : macro2minExpansion.entrySet()) {
      SyntaxTree minExpansion = builder.build(entry.getValue());
      ontologyMacroDefinitionSize += minExpansion.getSize() + 2;

      OWLClassExpression oneStep = macro2oneStep.get(entry.getKey());
      SyntaxTree origExpansion = builder.build(oneStep);

      if (origExpansion.getSize() > minExpansion.getSize()) {
        numberOfChangedAxioms++;
      }
    }

    numberOfontologyLanguageAxioms = ontologyLanguage.size();
    numberOfOntologyMacroDefinitions = ontologyMacroDefinitions.size();

    practicalReduction =
        (double) (ontologyMinimizedLanguageSize + ontologyMacroDefinitionSize)
            / (double) ontologySize;
    practicalReduction = (1 - practicalReduction);

    String header = "P,O,size(O),size(L_M),size(M),Prop.Red.,#Axioms,#Ch.Axioms,#M";
    String results =
        "2,"
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
