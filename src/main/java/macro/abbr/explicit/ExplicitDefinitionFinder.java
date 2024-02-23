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

/** A class to find macro definitions in an ontology. */
public class ExplicitDefinitionFinder {

  private OWLOntology ontology;

  private Set<OWLClass> abbreviations;
  private HashMap<OWLClass, Set<OWLClassExpression>> abbreviation2expressions;
  private Set<OWLAxiom> abbreviationDefinitions;
  private HashMap<OWLClass, Set<OWLAxiom>> abbreviation2definitions;

  private Set<OWLClass> synonyms;
  private HashMap<OWLClass, Set<OWLClass>> synonym2synonyms;
  private Set<OWLAxiom> synonymDefinitions;
  private HashMap<OWLClass, Set<OWLAxiom>> synonym2definitions;

  public ExplicitDefinitionFinder(OWLOntology o) {
    this.ontology = o;
    this.init();
  }

  public Map<OWLClass, Set<OWLAxiom>> getAbbreviation2Definitions() {
    return this.abbreviation2definitions;
  }

  public Set<OWLAxiom> getAbbreviationDefinitions() {
    return this.abbreviationDefinitions;
  }

  private void init() {
    this.abbreviations = new HashSet<>();
    this.abbreviation2expressions = new HashMap<>();
    this.abbreviation2definitions = new HashMap<>();
    this.abbreviationDefinitions = new HashSet<>();

    this.synonyms = new HashSet<>();
    this.synonym2synonyms = new HashMap<>();
    this.synonym2definitions = new HashMap<>();

    this.findDefinitions();
  }

  private void findDefinitions() {
    Set<OWLEquivalentClassesAxiom> axioms =
        this.ontology.getAxioms(AxiomType.EQUIVALENT_CLASSES, true);
    for (OWLEquivalentClassesAxiom a : axioms) {
      checkAbbreviation(a.getAxiomWithoutAnnotations());
      checkSynonym(a.getAxiomWithoutAnnotations());
    }
  }

  private void checkAbbreviation(OWLEquivalentClassesAxiom a) {
    if (DefinitionTypes.isAbbreviation(a)) {
      this.abbreviationDefinitions.add(a);

      Set<OWLClass> namedClasses = new HashSet<>();
      Set<OWLClassExpression> expressions = new HashSet<>();

      for (OWLClassExpression c : a.getClassExpressions()) {
        if (c.isNamed()) {
          namedClasses.add(c.asOWLClass());
        } else {
          expressions.add(c);
        }
      }

      for (OWLClass c : namedClasses) {
        this.abbreviations.add(c);
        this.abbreviation2expressions.putIfAbsent(c, new HashSet<>());
        this.abbreviation2expressions.get(c).addAll(expressions);
        this.abbreviation2definitions.putIfAbsent(c, new HashSet<>());
        this.abbreviation2definitions.get(c).add(a);
      }
    }
  }

  private void checkSynonym(OWLEquivalentClassesAxiom a) {
    if (DefinitionTypes.isSynonym(a)) {

      Set<OWLClass> namedClasses = new HashSet<>();
      Set<OWLClassExpression> expressions = new HashSet<>();

      for (OWLClassExpression c : a.getClassExpressions()) {
        if (c.isNamed()) {
          namedClasses.add(c.asOWLClass());
        } else {
          expressions.add(c);
        }
      }

      for (OWLClass c : namedClasses) {
        this.synonyms.add(c);
        this.synonym2synonyms.putIfAbsent(c, new HashSet<>());
        this.synonym2synonyms.get(c).addAll(namedClasses);
        this.synonym2definitions.putIfAbsent(c, new HashSet<>());
        this.synonym2definitions.get(c).add(a);
      }
    }
  }

  public Set<OWLClass> getAbbreviations() {
    return this.abbreviations;
  }

  public Set<OWLClass> getSynonyms() {
    return this.synonyms;
  }

  public HashMap<OWLClass, Set<OWLClassExpression>> getAbbreviation2expressions() {
    return this.abbreviation2expressions;
  }

  public HashMap<OWLClass, Set<OWLClass>> getSynonyms2synonyms() {
    return this.synonym2synonyms;
  }
}
