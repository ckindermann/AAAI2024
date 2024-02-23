package macro.roundtrip;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;
import macro.abbr.explicit.*;
import macro.minimization.general.*;
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
public class Macrofy {

  public static void main(String[] args) throws Exception {

    String ontFilePath = args[0];
    String output = args[1];

    run(ontFilePath, output);
  }

  public static void run(String ontFilePath, String output) throws Exception {
    File ontFile = new File(ontFilePath);
    OntologyLoader ontLoader = new OntologyLoader(ontFile, true);
    OWLOntology ont = ontLoader.getOntology();

    String ontologyName = Paths.get(ontFilePath).getFileName().toString();

    String outputPath = output;

    RewritingSystem system = new RewritingSystem(ont);

    OWLDataFactory factory = OWLManager.createOWLOntologyManager().getOWLDataFactory();

    Set<AxiomMacrofication> macrofication = system.getMacrofication();
    MacroDefinitions macroDefinitions = system.getMacroDefinitions();

    // materialise definitions
    // (1) macro -> evaluation
    Map<OWLClassExpression, OWLClassExpression> macro2evaluation =
        macroDefinitions.getMacro2Evaluation();
    Set<OWLAxiom> evaluations = new HashSet<>();
    for (Map.Entry<OWLClassExpression, OWLClassExpression> set : macro2evaluation.entrySet()) {

      OWLClassExpression macro = set.getKey();
      OWLClassExpression evaluation = set.getValue();
      OWLEquivalentClassesAxiom a = factory.getOWLEquivalentClassesAxiom(macro, evaluation);
      evaluations.add(a);
    }
    OntologySaver.saveAxioms(evaluations, outputPath + "/macroEvaluations.owl");

    // (2) macro -> expansion
    Map<OWLClassExpression, OWLClassExpression> macro2expansion =
        macroDefinitions.getMacro2Expansion();
    Set<OWLAxiom> expansions = new HashSet<>();
    for (Map.Entry<OWLClassExpression, OWLClassExpression> set : macro2expansion.entrySet()) {

      OWLClassExpression macro = set.getKey();
      OWLClassExpression expansion = set.getValue();
      OWLEquivalentClassesAxiom a = factory.getOWLEquivalentClassesAxiom(macro, expansion);
      expansions.add(a);
    }
    OntologySaver.saveAxioms(expansions, outputPath + "/macroExpansions.owl");

    // (3) macrofication
    Set<OWLAxiom> macroficationOntology = new HashSet<>();
    for (AxiomMacrofication m : macrofication) {
      macroficationOntology.add(m.getMinimizedAxiom());
    }
    OntologySaver.saveAxioms(macroficationOntology, outputPath + "/macrofication.owl");
  }
}
