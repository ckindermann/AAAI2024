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
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.*;

/** Compare two ontology (w.r.t.) their Class Expression Axioms */
public class Compare {

  public static void main(String[] args) throws Exception {

    String ont1 = args[0];
    String ont2 = args[1];
    // String output = args[2];

    run(ont1, ont2);
  }

  public static void run(String ont1, String ont2) throws Exception {

    File ontFile = new File(ont1);
    OntologyLoader ontLoader = new OntologyLoader(ontFile, true);
    OWLOntology ontology1 = ontLoader.getOntology();

    File ontFile2 = new File(ont2);
    OntologyLoader ontLoader2 = new OntologyLoader(ontFile2, true);
    OWLOntology ontology2 = ontLoader2.getOntology();

    Set<OWLAxiom> axioms1 = getClassExpressionAxioms(ontology1);
    Set<OWLAxiom> axioms2 = getClassExpressionAxioms(ontology2);

    System.out.println("Ontologies are equal : " + axioms1.equals(axioms2));
    if (!axioms1.equals(axioms2)) {
      System.out.println("Number of axioms 1 " + axioms1.size());
      System.out.println("Number of axioms 2 " + axioms2.size());
      for (OWLAxiom a1 : axioms1) {
        if (!axioms2.contains(a1)) {
          System.out.println("Missing Axiom " + a1);
        }
      }
    }
  }

  private static Set<OWLAxiom> getClassExpressionAxioms(OWLOntology o) {

    Set<OWLAxiom> axioms = new HashSet<>();
    axioms.addAll(o.getAxioms(AxiomType.EQUIVALENT_CLASSES, true));
    axioms.addAll(o.getAxioms(AxiomType.DISJOINT_CLASSES, true));
    axioms.addAll(o.getAxioms(AxiomType.SUBCLASS_OF, true));
    axioms.addAll(o.getAxioms(AxiomType.DISJOINT_UNION, true));

    // remove annotations and axioms with negations
    Set<OWLAxiom> axiomsWithoutAnnotations = new HashSet<>();
    Set<OWLAxiom> axiomsWithNegation = new HashSet<>();
    for (OWLAxiom a : axioms) {
      OWLAxiom noannotations = a.getAxiomWithoutAnnotations();
      axiomsWithoutAnnotations.add(noannotations);
      for (OWLClassExpression exp : a.getNestedClassExpressions()) {
        if (exp instanceof OWLObjectComplementOf) {
          axiomsWithNegation.add(noannotations);
        }
      }
    }

    axiomsWithoutAnnotations.removeAll(axiomsWithNegation);

    return axiomsWithoutAnnotations;
  }
}
