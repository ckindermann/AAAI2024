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
 * MacroDefinitions.
 *
 * <p>Axioms of the form EquivalentClasses(N,C), where N is a named class and C is a complex class
 * expression, can be interpreted as macro definitions. However, sets of macro definitions are
 * required to be functions. So, we exclude such axioms if there exist EquivalentClasses(N,C') with
 * C != C'. Furthermore, we exclude such axioms if the fixed-point expansion does not exist due to
 * cycles.
 */

// Terminology:
//
// we use a slightly different terminology for shorter identifiers:
// - "expansion" in the code corresponds to "1-step-expansion" in the paper
// - "evaluation" in the code corresponds to "fixed-point-expansion"
public class MacroDefinitions {

  private OWLOntology ontology;

  private Set<OWLAxiom> definitionAxioms; // includes ambiguous and cyclic classes
  private Set<OWLAxiom> nonAmbiguousNonCyclicdefinitionAxioms;
  private Map<OWLClass, Set<OWLClassExpression>> ambiguousClasses;
  private Map<OWLClass, Set<OWLClassExpression>> cyclicClasses;
  private Map<OWLClass, Set<OWLClass>> class2directDependency;
  private Map<OWLClass, Set<OWLClass>> class2dependency;

  private Map<OWLClass, OWLClassExpression>
      class2expansion; // does not include ambiguous and cyclic classes
  private Map<OWLClass, OWLClassExpression> class2evaluation;
  private Map<OWLClassExpression, OWLClass> evaluation2class;

  private Map<OWLClass, OWLClassExpression> class2minimizedExpansion;

  private ExplicitDependencyFinder dependencyFinder;
  private ExplicitDefinitionFinder finder;

  private OWLRewriter rewriter;
  private SyntaxTreeUtil synTreeUtil;
  private SyntaxTreeBuilder treeBuilder;

  public MacroDefinitions(OWLOntology o) {
    this.synTreeUtil = new SyntaxTreeUtil();
    this.rewriter = new OWLRewriter();
    this.treeBuilder = new SyntaxTreeBuilder();

    this.dependencyFinder = new ExplicitDependencyFinder(o);
    this.finder = this.dependencyFinder.getDefinitionFinder();

    this.class2expansion = new HashMap<>();
    this.class2minimizedExpansion = new HashMap<>();
    this.class2evaluation = new HashMap<>();
    this.evaluation2class = new HashMap<>();
    this.cyclicClasses = new HashMap<>();
    this.ambiguousClasses = new HashMap<>();

    initSyntacticDefinitions();
    initEvaluations();
    initialiseNonAmbiguousNonCyclicDefinitions();
  }

  public Set<OWLAxiom> getNonCyclicNonAmbiguousDefinitions() {
    return this.nonAmbiguousNonCyclicdefinitionAxioms;
  }

  public Set<OWLAxiom> getDefinitionAxioms() {
    return this.definitionAxioms;
  }

  public Map<OWLClass, OWLClassExpression> getClass2expansion() {
    return this.class2expansion;
  }

  public Map<OWLClass, OWLClassExpression> getClass2evaluation() {
    return this.class2evaluation;
  }

  public Map<OWLClass, Set<OWLClass>> getDependencies() {
    return this.class2dependency;
  }

  public Set<OWLClass> getMacroSymbols() {
    return this.class2evaluation.keySet();
  }

  public Map<OWLClass, Set<OWLClassExpression>> getAmbiguousClasses() {
    return this.ambiguousClasses;
  }

  public Map<OWLClass, Set<OWLClassExpression>> getCyclicClasses() {
    return this.cyclicClasses;
  }

  public Map<OWLClassExpression, OWLClass> getEvaluation2class() {
    return this.evaluation2class;
  }

  private void initSyntacticDefinitions() {

    // Consider an axiom of the form EquivalentClasses(N,C) where N is named class and
    // C is a complex class expression. Then N, a name, can be used instead of C, a complex class
    // expression. So, N can be used  as an 'abbreviation' for C.
    // The axiom EquivalentClasses(N,C) is called an abbreviation definition.
    this.definitionAxioms = finder.getAbbreviationDefinitions();

    // a named class N 'depends' on another named class N'
    // if there exists an axiom EquivalentClasses(N,C), where C is a complex class  ,
    // and N' occurs in C
    this.class2directDependency = this.dependencyFinder.getDirectDependencies();
    this.class2dependency = this.dependencyFinder.getDependencies();

    // get classes that are involved in a cycle
    Set<OWLClass> classesWithCyclicDefinition = this.dependencyFinder.cyclicClasses();

    // get classes with an expansion that includes cylces
    Set<OWLClass> classesWithCyclicDependencies = new HashSet<>();
    for (Map.Entry<OWLClass, Set<OWLClass>> set : class2dependency.entrySet()) {
      OWLClass namedClass = set.getKey();
      Set<OWLClass> dependencies = set.getValue();
      for (OWLClass d : dependencies) {
        if (classesWithCyclicDefinition.contains(d)) {
          classesWithCyclicDependencies.add(namedClass);
        }
      }
    }

    // initialse (a) cyclic classes, (b) ambiguous classes,
    // and (c) macro definitions in terms of a map from named classes to their equivalent class
    // expressions
    for (Map.Entry<OWLClass, Set<OWLClassExpression>> set :
        finder.getAbbreviation2expressions().entrySet()) {

      OWLClass namedClass = set.getKey();
      Set<OWLClassExpression> expressions = set.getValue();
      // (a)
      if (expressions.size() > 1) { // check for ambiguity, i.e., more than one expansion
        this.ambiguousClasses.put(namedClass, expressions);
      }
      // (b)
      if (classesWithCyclicDependencies.contains(namedClass)) { // check for cyclic dependencies
        this.cyclicClasses.put(namedClass, expressions);
      }
      // (c)
      // check for macro condition, i.e., there is only one expansion and no cycles
      if (expressions.size() == 1 && !classesWithCyclicDependencies.contains(namedClass)) {
        for (OWLClassExpression e : expressions) { // NB: iteration over singleton set
          this.class2expansion.put(namedClass, e);
        }
      }
    }
  }

  private void initEvaluations() {

    this.class2evaluation = new HashMap<>();
    for (OWLClass c : this.class2expansion.keySet()) {
      OWLClassExpression evaluation = getEvaluation(c);
      this.class2evaluation.put(c, evaluation);
    }

    for (Map.Entry<OWLClass, OWLClassExpression> set : this.class2evaluation.entrySet()) {
      OWLClass macro = set.getKey();
      OWLClassExpression evaluation = set.getValue();
      // NB: if two different macros have different evaluations,
      // then we make an arbitrary choice for which macro symbol to use here
      this.evaluation2class.put(evaluation, macro);
    }
  }

  // given a macro symbol (a named class), compute its evaluation (its fixed-point expansion)
  private OWLClassExpression getEvaluation(OWLClass c) {
    OWLClassExpression expansion = this.class2expansion.get(c);
    Set<OWLClass> currentDependencies = new HashSet<>();
    Set<OWLClass> nextDependencies = new HashSet<>();
    if (this.class2directDependency.containsKey(c)) {
      currentDependencies.addAll(this.class2directDependency.get(c));
    }
    SyntaxTree tree = this.treeBuilder.build(expansion);

    // traversal through the dependency tree
    while (!currentDependencies.isEmpty()) {
      for (OWLClass dependency : currentDependencies) {
        if (this.class2directDependency.containsKey(dependency)) {
          nextDependencies.addAll(this.class2directDependency.get(dependency));
        }
        if (dependency != null && this.class2expansion.get(dependency) != null) {
          this.rewriter.rewrite(tree, dependency, this.class2expansion.get(dependency));
        }
      }
      currentDependencies.clear();
      currentDependencies.addAll(nextDependencies);
      nextDependencies.clear();
    }

    return (OWLClassExpression) tree.getRoot().getObject();
  }

  public void initialiseNonAmbiguousNonCyclicDefinitions() {
    this.nonAmbiguousNonCyclicdefinitionAxioms = new HashSet<>();
    this.nonAmbiguousNonCyclicdefinitionAxioms.addAll(this.definitionAxioms);
    Map<OWLClass, Set<OWLAxiom>> abbreviation2definitions =
        this.finder.getAbbreviation2Definitions();
    for (OWLClass c : this.ambiguousClasses.keySet()) {
      Set<OWLAxiom> ambiguousDefintion = abbreviation2definitions.get(c);
      this.nonAmbiguousNonCyclicdefinitionAxioms.removeAll(ambiguousDefintion);
    }
    for (OWLClass c : this.cyclicClasses.keySet()) {
      Set<OWLAxiom> ambiguousDefintion = abbreviation2definitions.get(c);
      this.nonAmbiguousNonCyclicdefinitionAxioms.removeAll(ambiguousDefintion);
    }
  }

  // replace complex class expressions with macro symbols in macro definitions (Problem 2)
  public void computeMinimalMacroDefinitions() {

    for (Map.Entry<OWLClass, OWLClassExpression> set : class2evaluation.entrySet()) {
      OWLClass macro = set.getKey();
      OWLClassExpression evaluation = set.getValue();

      // initialise term tree that will be rewritten
      SyntaxTree minimized = this.treeBuilder.build(evaluation);

      // sort subterms in descending order by their size
      TreeMap<Integer, Set<OWLClassExpression>> size2subterms =
          this.synTreeUtil.size2subExpressions(evaluation);

      // iterate over terms from largest to smallest
      for (Map.Entry<Integer, Set<OWLClassExpression>> entry : size2subterms.entrySet()) {
        int size = entry.getKey();
        if (size > 1) { // skip atomic symbols and binary things
          Set<OWLClassExpression> independent = entry.getValue();
          // terms with the same size are independent and can be traversed in any order
          for (OWLClassExpression i : independent) {
            if (evaluation2class.containsKey(i)) {
              this.rewriter.rewrite(minimized, i, evaluation2class.get(i));
            }
          }
        }
      }

      OWLClassExpression minExpression = this.synTreeUtil.getExpressionFromTree(minimized);

      this.class2minimizedExpansion.put(macro, minExpression);
    }
  }
}
