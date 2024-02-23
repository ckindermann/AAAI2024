package macro.mat;

import java.io.*;
import java.nio.file.*;
import java.text.DecimalFormat;
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
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.*;

public class Negations {
  private static final DecimalFormat df = new DecimalFormat("0.00");

  public static void main(String[] args) {

    String ontFilePath = args[0];
    String output = args[1];

    File ontFile = new File(ontFilePath);
    OntologyLoader ontLoader = new OntologyLoader(ontFile, true);
    OWLOntology ont = ontLoader.getOntology();

    String ontologyName = Paths.get(ontFilePath).getFileName().toString();

    // get axioms to minimize
    // NB: we are currently only handling class expressions
    Set<OWLAxiom> axioms = new HashSet<>();
    axioms.addAll(ont.getAxioms(AxiomType.EQUIVALENT_CLASSES, true));
    axioms.addAll(ont.getAxioms(AxiomType.DISJOINT_CLASSES, true));
    axioms.addAll(ont.getAxioms(AxiomType.SUBCLASS_OF, true));
    axioms.addAll(ont.getAxioms(AxiomType.DISJOINT_UNION, true));

    Set<OWLAxiom> axiomsWithNegation = new HashSet<>();

    // remove axioms with negation
    for (OWLAxiom a : axioms) {
      for (OWLClassExpression exp : a.getNestedClassExpressions()) {
        if (exp instanceof OWLObjectComplementOf) {
          axiomsWithNegation.add(a);
        }
      }
    }
    // axioms.removeAll(axiomsWithNegation);
    System.out.println("Ontology " + ontologyName + " has " + axiomsWithNegation.size());
  }
}
