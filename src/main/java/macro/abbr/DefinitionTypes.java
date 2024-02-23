package macro.abbr;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.*;
import java.util.stream.*;
import macro.ont.*;
import macro.parser.*;
import macro.structure.*;
import macro.structure.nodes.*;
import macro.util.*;
import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.traverse.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.*;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.util.*;

public class DefinitionTypes {

  public static boolean isAbbreviation(OWLAxiom axiom) {
    if (axiom instanceof OWLEquivalentClassesAxiom) {
      OWLEquivalentClassesAxiom a = (OWLEquivalentClassesAxiom) axiom;
      return isSimpleAbbreviation(a) || isAmbiguousAbbreviation(a) || isCompoundDefinition(a);
    }
    return false;
  }

  public static boolean isAbbreviation(OWLEquivalentClassesAxiom axiom) {
    return isSimpleAbbreviation(axiom)
        || isAmbiguousAbbreviation(axiom)
        || isCompoundDefinition(axiom);
  }

  public static boolean isSynonym(OWLEquivalentClassesAxiom axiom) {
    return isSimpleSynonym(axiom)
        || isEnumerativeSynonym(axiom)
        || isCompoundDefinition(axiom); // But it could also be a compound definition?
  }

  public static boolean isSimpleAbbreviation(OWLEquivalentClassesAxiom axiom) {
    // check if there are only 2 classes
    Set<OWLClassExpression> arguments = axiom.getClassExpressions();
    if (arguments.size() != 2) {
      return false;
    }
    // one is named
    Set<OWLClass> named = getNamedClasses(axiom);
    if (named.size() != 1) {
      return false;
    }
    return true;
  }

  public static boolean isSimpleSynonym(OWLEquivalentClassesAxiom axiom) {
    // check if there are only 2 classes
    Set<OWLClassExpression> arguments = axiom.getClassExpressions();
    if (arguments.size() != 2) {
      return false;
    }
    // both are named
    Set<OWLClass> named = getNamedClasses(axiom);
    if (named.size() != 2) {
      return false;
    }
    return true;
  }

  public static boolean isAmbiguousAbbreviation(OWLEquivalentClassesAxiom axiom) {
    // check if there are more than 2 classes
    Set<OWLClassExpression> arguments = axiom.getClassExpressions();
    if (arguments.size() <= 2) {
      return false;
    }

    // only one is named
    Set<OWLClass> named = getNamedClasses(axiom);
    if (named.size() != 1) {
      return false;
    }
    return true;
  }

  public static boolean isEnumerativeSynonym(OWLEquivalentClassesAxiom axiom) {
    // check if there are more than 2 classes
    Set<OWLClassExpression> arguments = axiom.getClassExpressions();
    if (arguments.size() <= 2) {
      return false;
    }

    // all are named
    Set<OWLClass> named = getNamedClasses(axiom);
    if (named.size() != arguments.size()) {
      return false;
    }
    return true;
  }

  public static boolean isCompoundDefinition(OWLEquivalentClassesAxiom axiom) {
    // check if there are at least 4 classes
    Set<OWLClassExpression> arguments = axiom.getClassExpressions();
    if (arguments.size() <= 3) {
      return false;
    }

    // at least 2 named classes
    Set<OWLClass> named = getNamedClasses(axiom);
    if (named.size() < 2) {
      return false;
    }

    if (arguments.size() - named.size() < 2) {
      return false;
    }

    return true;
  }

  public static Set<OWLClass> getNamedClasses(OWLEquivalentClassesAxiom axiom) {
    Set<OWLClass> named = axiom.getNamedClasses();

    OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    OWLDataFactory factory = manager.getOWLDataFactory();

    if (axiom.containsOWLThing()) {
      OWLClass thing = factory.getOWLThing();
      named.add(thing);
    }

    if (axiom.containsOWLNothing()) {
      OWLClass nothing = factory.getOWLNothing();
      named.add(nothing);
    }

    return named;
  }
}
