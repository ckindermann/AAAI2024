package macro.roundtrip;

import java.io.*;
import java.nio.file.*;
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
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.*;

/** Macrofies an ontology */
public class FixedPoint {

  public static void main(String[] args) throws Exception {

    String macroExpansions = args[0];
    String output = args[2];

    run(macroExpansions, output);
  }

  public static void run(String macroExpansions, String output) throws Exception {

    File ontFile = new File(macroExpansions);
    OntologyLoader ontLoader = new OntologyLoader(ontFile, true);
    OWLOntology expansions = ontLoader.getOntology();

    String outputPath = output;
    // String ontologyName = Paths.get(ontFilePath).getFileName().toString();
    // String outputPath = output + "/" + ontologyName;
    // IOHelper.createFolder(outputPath);

    MacroDefinitions definitions = new MacroDefinitions(expansions);
    Map<OWLClass, OWLClassExpression> macro2evaluation = definitions.getClass2evaluation();

    OWLDataFactory factory = OWLManager.createOWLOntologyManager().getOWLDataFactory();

    Set<OWLAxiom> evaluations = new HashSet<>();
    for (Map.Entry<OWLClass, OWLClassExpression> set : macro2evaluation.entrySet()) {

      OWLClassExpression macro = set.getKey();
      OWLClassExpression evaluation = set.getValue();
      OWLEquivalentClassesAxiom a = factory.getOWLEquivalentClassesAxiom(macro, evaluation);
      evaluations.add(a);
    }
    OntologySaver.saveAxioms(evaluations, outputPath + "/fixedpointExpansions.owl");
  }
}
