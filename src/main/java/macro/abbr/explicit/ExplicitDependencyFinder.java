package macro.abbr.explicit;

import java.nio.file.*;
import java.util.*;
import macro.abbr.*;
import macro.ont.*;
import macro.parser.*;
import macro.structure.*;
import macro.structure.nodes.*;
import macro.util.*;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.*;
import org.semanticweb.owlapi.util.*;

public class ExplicitDependencyFinder {

  // TODO: this currently only handles abbreviations
  private OWLOntology ontology;
  private Map<OWLClass, Set<OWLClass>> directDependencies;
  private Map<OWLClass, Set<OWLClass>> dependencies;
  private ExplicitDefinitionFinder definitionFinder;

  public ExplicitDependencyFinder(OWLOntology o) {
    this.ontology = o;
    this.init();
  }

  private void init() {
    this.directDependencies = new HashMap<>();
    this.dependencies = new HashMap<>();
    this.definitionFinder = new ExplicitDefinitionFinder(this.ontology);
    this.findDirectDependencies();
    this.findDependencies();
  }

  public ExplicitDefinitionFinder getDefinitionFinder() {
    return this.definitionFinder;
  }

  public Map<OWLClass, Set<OWLClass>> getDirectDependencies() {
    return this.directDependencies;
  }

  private void findDirectDependencies() {
    Map<OWLClass, Set<OWLClassExpression>> abbreviation2expressions =
        this.definitionFinder.getAbbreviation2expressions();
    for (Map.Entry<OWLClass, Set<OWLClassExpression>> set : abbreviation2expressions.entrySet()) {
      Set<OWLClass> dependencies = new HashSet<>();
      for (OWLClassExpression exp : set.getValue()) {
        for (OWLClass c : exp.getClassesInSignature()) {
          if (abbreviation2expressions.containsKey(c)) {
            dependencies.add(c);
          }
        }
      }
      this.directDependencies.put(set.getKey(), dependencies);
    }
  }

  private void findDependencies() {
    // Set<OWLClass> abbreviations = this.definitionFinder.getAbbreviations();
    Map<OWLClass, Set<OWLClassExpression>> abbreviation2expressions =
        this.definitionFinder.getAbbreviation2expressions();

    for (Map.Entry<OWLClass, Set<OWLClassExpression>> set : abbreviation2expressions.entrySet()) {
      Set<OWLClass> dependencies = new HashSet<>();
      Set<OWLClassExpression> current = new HashSet<>();
      Set<OWLClassExpression> next = new HashSet<>();
      current.addAll(set.getValue());
      while (!current.isEmpty()) {
        for (OWLClassExpression exp : current) {
          for (OWLClassExpression nested : exp.getNestedClassExpressions()) {
            if (nested.isNamed()) {
              OWLClass named = (OWLClass) nested;
              if (abbreviation2expressions.containsKey(named) && !dependencies.contains(named)) {
                next.addAll(abbreviation2expressions.get(named));
                dependencies.add(named);
              }
            }
          }
        }
        current.clear();
        current.addAll(next);
        next.clear();
      }
      this.dependencies.put(set.getKey(), dependencies);
    }
  }

  public Map<OWLClass, Set<OWLClass>> getDependencies() {
    return this.dependencies;
  }

  // this returns classes that depend on themselves
  // NB: this does not include classes that depend on cyclic classes
  public Set<OWLClass> cyclicClasses() {
    Set<OWLClass> cyclic = new HashSet<>();

    for (Map.Entry<OWLClass, Set<OWLClass>> set : this.dependencies.entrySet()) {
      OWLClass abbreviation = set.getKey();
      Set<OWLClass> dependencies = set.getValue();
      if (dependencies.contains(abbreviation)) {
        cyclic.add(abbreviation);
      }
    }
    return cyclic;
  }
}
