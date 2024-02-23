package macro.minimization.fixed;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;
import macro.abbr.*;
import macro.abbr.explicit.*;
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
 * Rewriting System for size-minimal rewritings of OWL ontologies.
 *
 * <p>Computes a size-minimal reriting of class expression axioms in an ontology by interpreting
 * axioms of the from EquivalentClassesAxioms(N,C) where N is a named class and C is a complex class
 * as definitions. The rewriting can can be computed for the input ontology, excluding definition
 * axioms (Problem 1) or including definition axioms (Problem 2).
 */

// Terminology:
//
// we use a slightly different terminology for shorter identifiers:
// - "expansion" in the code corresponds to "1-step-expansion" in the paper
// - "evaluation" in the code corresponds to "fixed-point-expansion"
public class RewritingSystem {

  private OWLOntology ontology;
  // Each axiom in the ontology will be macrofied (including definition axioms).
  // So the number of class expression axioms of the input ontology will be equal to the number of
  // elements in "macrofication". We will use this set of AxiomMacrofications later to derive
  // statistics for the language encoding as defined in the paper.
  private Set<AxiomMacrofication> macrofication;

  private MacroDefinitions macroDefinitions;
  private Set<OWLAxiom> macroDefinitionAxioms;
  private Set<OWLClass> macroSymbols;
  private Set<OWLClassExpression> macroEvaluations;

  // macro definitions are not reduced
  // this means that evaluation2macro is not an inversion of macro2evaluation
  // since different macros can have the same fixed-point expansions
  private Map<OWLClass, OWLClassExpression> macro2evaluation;
  private Map<OWLClassExpression, OWLClass> evaluation2macro;

  private OWLRewriter rewriter;
  private SyntaxTreeBuilder treeBuilder;
  private SyntaxTreeUtil synTreeUtil;

  // macro symbols mapped to their minimized expansions (for Problem 2)
  private Map<OWLClass, OWLClassExpression> macro2minExpansion;

  /**
   * Constructor for the rewriting system.
   *
   * @param o the ontology to be minimized
   */
  public RewritingSystem(OWLOntology o) {
    this.ontology = o;
    this.rewriter = new OWLRewriter();
    this.treeBuilder = new SyntaxTreeBuilder();
    this.synTreeUtil = new SyntaxTreeUtil();

    this.initialiseMacroDefinitions();
    this.computeMinimalMacrofication();
  }

  private void initialiseMacroDefinitions() {
    this.macroDefinitions = new MacroDefinitions(this.ontology);
    this.macroDefinitionAxioms = this.macroDefinitions.getDefinitionAxioms();
    this.macro2evaluation = this.macroDefinitions.getClass2evaluation();
    this.evaluation2macro = this.macroDefinitions.getEvaluation2class();
    this.macroSymbols = this.macroDefinitions.getMacroSymbols();

    // collect all OWL class expressions for which we have macro definitions
    this.macroEvaluations = new HashSet<>();
    this.macroEvaluations.addAll(this.macro2evaluation.values());
  }

  public MacroDefinitions getMacroDefinitions() {
    return this.macroDefinitions;
  }

  public Set<AxiomMacrofication> getMacrofication() {
    return this.macrofication;
  }

  public Map<OWLClass, OWLClassExpression> getMinimialDefinitions() {
    return this.macro2minExpansion;
  }

  /**
   * Computes the minimal macro definitions for the input ontology.
   *
   * <p>For each macro definition, we compute the minimal macro definition w.r.t. a given set of
   * macros.
   */
  public void computeMinimalMacroDefinitions() {
    this.macro2minExpansion = new HashMap<>();
    // get macro 2 evaluation <- get subterms <- replace things

    // iterate over all macro definitions
    for (Map.Entry<OWLClass, OWLClassExpression> set : macro2evaluation.entrySet()) {
      OWLClassExpression macro = set.getKey();
      OWLClassExpression evaluation = set.getValue();

      // build a term tree that will be rewritten into the minimal term
      SyntaxTree minimized = this.treeBuilder.build(evaluation);

      // order subterms in the macro evaluation, i.e., fixed-point evaluation by size ...
      TreeMap<Integer, Set<OWLClassExpression>> size2subterms =
          this.synTreeUtil.size2subExpressions(evaluation);

      // ... iterate over all subterms in descending order (by using a TreeMap)
      for (Map.Entry<Integer, Set<OWLClassExpression>> entry : size2subterms.entrySet()) {
        int size = entry.getKey();
        if (size > 1) { // skip constant symbols
          // terms of the same size are necessarily independent
          Set<OWLClassExpression> independent = entry.getValue();
          // macrofy
          for (OWLClassExpression i : independent) {
            if (evaluation2macro.containsKey(i)) {
              OWLRewriter rewriter = new OWLRewriter(minimized, i, evaluation2macro.get(i));
            }
          }
        }
      }

      // convert tree to term
      OWLClassExpression minExpression = this.synTreeUtil.getExpressionFromTree(minimized);
      this.macro2minExpansion.put((OWLClass) macro, minExpression);
    }
  }

  private void computeMinimalMacrofication() {
    this.macrofication = new HashSet<>();

    // get look up tables for
    // - macro symbols (named classes) to their evaluation (fixed-point expansion)
    // - the other way around
    // (note that macro definitions are not reduced.
    // This means multiple macros can have the same evaluation,
    // but we only use ONE of them to macrofy terms
    Map<OWLClass, OWLClassExpression> class2evaluation =
        this.macroDefinitions.getClass2evaluation();
    Map<OWLClassExpression, OWLClass> evaluation2class =
        this.macroDefinitions.getEvaluation2class();

    Set<OWLClass> macros = this.macroDefinitions.getMacroSymbols();
    Set<OWLClassExpression> evaluations = new HashSet<>();
    evaluations.addAll(class2evaluation.values());

    Set<OWLAxiom> definitions = this.macroDefinitions.getDefinitionAxioms();

    Set<OWLAxiom> axioms = getClassExpressionAxioms(this.ontology);
    for (OWLAxiom a : axioms) {

      OWLAxiom axiom = a.getAxiomWithoutAnnotations();
      SyntaxTree tree = this.treeBuilder.build(axiom);

      // initialise macrofication data structure
      AxiomMacrofication axiomMacrofication = new AxiomMacrofication(tree);
      this.macrofication.add(axiomMacrofication);

      // fully expand all macro symbols
      SyntaxTree expandedAxiom = expandMacros(a);
      axiomMacrofication.setExpansion(expandedAxiom);

      // get the fully expanded axiom
      OWLAxiom unfolded = axiomMacrofication.getExpandedAxiom();

      // macrofy (using the largest evaluations)
      SyntaxTree minimized = contractMacros(unfolded);
      axiomMacrofication.setMinimization(minimized);
    }
  }

  /**
   * Replace a macro's expansion with a macro symbol in an axiom and return it's associated term
   * tree.
   *
   * @param a the axiom to be minimized
   * @return the minimized axiom
   */
  public SyntaxTree contractMacros(OWLAxiom a) {

    OWLAxiom axiom = a.getAxiomWithoutAnnotations();

    // initialise term tree that will be rewritten
    SyntaxTree minimized = this.treeBuilder.build(axiom);

    // sort subterms in descending order by their size
    TreeMap<Integer, Set<OWLClassExpression>> size2subterms =
        this.synTreeUtil.size2expressions(axiom);

    // iterate over terms from largest to smallest
    for (Map.Entry<Integer, Set<OWLClassExpression>> set : size2subterms.entrySet()) {
      int size = set.getKey();
      if (size > 1) { // skip atomic symbols
        // terms with the same size are independent and can be traversed in any order
        for (OWLClassExpression macroEvaluation : set.getValue()) {
          if (this.evaluation2macro.containsKey(macroEvaluation)) {
            this.rewriter.rewrite(
                minimized, macroEvaluation, this.evaluation2macro.get(macroEvaluation));
          }
        }
      }
    }
    return minimized;
  }

  /**
   * Replace a macro's expansion with a macro symbol in an axiom and return it's associated term
   * tree.
   *
   * @param expr the expression to be minimized
   * @return the minimized expression
   */
  public SyntaxTree contractMacros(OWLClassExpression expr) {

    // initialise term tree that will be rewritten
    SyntaxTree minimized = this.treeBuilder.build(expr);

    // sort subterms in descending order by their size
    TreeMap<Integer, Set<OWLClassExpression>> size2subterms =
        // TODO: change this so that it includes the expression itself
        this.synTreeUtil.size2expressions(expr);

    // iterate over terms from largest to smallest
    for (Map.Entry<Integer, Set<OWLClassExpression>> set : size2subterms.entrySet()) {
      int size = set.getKey();
      if (size > 1) { // skip atomic symbols
        // terms with the same size are independent and can be traversed in any order
        for (OWLClassExpression macroEvaluation : set.getValue()) {
          if (this.evaluation2macro.containsKey(macroEvaluation)) {
            this.rewriter.rewrite(
                minimized, macroEvaluation, this.evaluation2macro.get(macroEvaluation));
          }
        }
      }
    }
    return minimized;
  }

  /**
   * Replace all macro symbols (named class) with their evaluations (fixed-point expansion).
   *
   * @param a the axiom to be expanded
   * @return the expanded axiom
   */
  public SyntaxTree expandMacros(OWLAxiom a) {

    OWLAxiom axiom = a.getAxiomWithoutAnnotations();

    // initialise term tree that will be rewritten
    SyntaxTree expandedAxiom = this.treeBuilder.build(axiom);

    if (!this.macroDefinitionAxioms.contains(axiom)) {
      Set<OWLClass> namedClasses = axiom.getClassesInSignature();
      namedClasses.retainAll(this.macroSymbols);
      for (OWLClass c : namedClasses) {
        OWLClassExpression evaluation = this.macro2evaluation.get(c);
        this.rewriter.rewrite(expandedAxiom, c, evaluation);
      }
    }
    return expandedAxiom;
  }

  /**
   * Replace all macro symbols (named class) with their evaluations (fixed-point expansion).
   *
   * @param expr the expression to be expanded
   * @return the expanded expression
   */
  public SyntaxTree expandMacros(OWLClassExpression expr) {

    // initialise term tree that will be rewritten
    SyntaxTree expandedAxiom = this.treeBuilder.build(expr);

    Set<OWLClass> namedClasses = expr.getClassesInSignature();
    namedClasses.retainAll(this.macroSymbols);
    for (OWLClass c : namedClasses) {
      if (this.macro2evaluation.containsKey(c)) {
        OWLClassExpression evaluation = this.macro2evaluation.get(c);
        this.rewriter.rewrite(expandedAxiom, c, evaluation);
      }
    }
    return expandedAxiom;
  }

  private Set<OWLAxiom> getClassExpressionAxioms(OWLOntology o) {

    Set<OWLAxiom> axioms = new HashSet<>();
    axioms.addAll(o.getAxioms(AxiomType.EQUIVALENT_CLASSES, true));
    axioms.addAll(o.getAxioms(AxiomType.DISJOINT_CLASSES, true));
    axioms.addAll(o.getAxioms(AxiomType.SUBCLASS_OF, true));
    axioms.addAll(o.getAxioms(AxiomType.DISJOINT_UNION, true));

    // remove annotations
    Set<OWLAxiom> axiomsWithoutAnnotations = new HashSet<>();
    for (OWLAxiom a : axioms) {
      axiomsWithoutAnnotations.add(a.getAxiomWithoutAnnotations());
    }

    return axiomsWithoutAnnotations;
  }
}
