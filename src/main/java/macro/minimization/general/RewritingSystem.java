package macro.minimization.general;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;
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

public class RewritingSystem {

  private OWLOntology ontology;
  private Set<OWLAxiom> axioms;
  private MacroDefinitions macroDefinitions;
  private Set<AxiomMacrofication> macrofication;

  private Set<OWLClassExpression> subterms;

  private SyntaxTreeBuilder treeBuilder;
  private SyntaxTreeUtil syntaxTreeUtil;
  private OWLRewriter rewriter;

  // these are NOT direct relationships -- these list everything
  private Map<OWLClassExpression, Set<OWLClassExpression>> children;
  private Map<OWLClassExpression, Set<OWLClassExpression>> parents;

  private TreeMap<Integer, Set<OWLClassExpression>> level2expression_bottom2top;
  private TreeMap<Integer, Set<OWLClassExpression>> level2expression_top2bottom;

  private Map<OWLClassExpression, Integer> expression2occurrence;
  private Map<OWLClassExpression, Integer> expression2size;

  public RewritingSystem(OWLOntology o) {

    this.ontology = o;
    this.macroDefinitions = new MacroDefinitions();

    this.axioms = getClassExpressionAxioms(this.ontology);

    Set<OWLAxiom> axiomsWithNegation = this.getAxiomsWithNegation(axioms);
    axioms.removeAll(axiomsWithNegation);

    this.children = new HashMap<>();
    this.parents = new HashMap<>();
    this.expression2occurrence = new HashMap<>();
    this.expression2size = new HashMap<>();

    this.subterms = new HashSet<>();

    this.treeBuilder = new SyntaxTreeBuilder();
    this.syntaxTreeUtil = new SyntaxTreeUtil();
    this.rewriter = new OWLRewriter();

    for (OWLAxiom a : axioms) {
      buildSubTermRelationship(a);
      initialiseSizeAndOccurrence(a);
    }

    this.initialiseLevelMaps();

    this.initialiseMacroDefinitions();
    this.buildMacrofication();
  }

  public MacroDefinitions getMacroDefinitions() {
    return this.macroDefinitions;
  }

  public Set<AxiomMacrofication> getMacrofication() {
    return this.macrofication;
  }

  public Set<OWLAxiom> getAxioms() {
    return this.axioms;
  }

  private void initialiseMacroDefinitions() {

    for (Map.Entry<Integer, Set<OWLClassExpression>> set :
        this.level2expression_bottom2top.entrySet()) {
      int level = set.getKey();
      Set<OWLClassExpression> expressions = set.getValue();
      for (OWLClassExpression e : expressions) {

        int eOccurrence = this.expression2occurrence.get(e);
        Set<OWLClassExpression> parents = this.parents.get(e);

        boolean dominatingParent = false;
        if (parents != null) {
          for (OWLClassExpression p : parents) {
            int pOccurrence = this.expression2occurrence.get(p);
            if (eOccurrence == pOccurrence) {
              dominatingParent = true;
            }
          }
        }
        // criterion for introducing a macro definition for a term
        if (!dominatingParent
            && this.expression2size.get(e) > 2
            && this.expression2occurrence.get(e) > 1) {
          this.macroDefinitions.addMacroDefinition(e);
        }
      }
    }

    this.macroDefinitions.computeMinimalMacroDefinitions();
  }

  private void buildMacrofication() {

    Map<OWLClassExpression, OWLClassExpression> evaluation2macro =
        this.macroDefinitions.getEvaluation2macro();

    this.macrofication = new HashSet<>();
    for (OWLAxiom a : this.axioms) {

      SyntaxTree tree = this.treeBuilder.build(a);
      SyntaxTree minimized = this.treeBuilder.build(a);
      AxiomMacrofication axiomMacrofication = new AxiomMacrofication(tree);

      // returns size 2 expressions
      // (sorted in descending order -- meaning we start with the largest expressions)
      TreeMap<Integer, Set<OWLClassExpression>> size2subterms =
          this.syntaxTreeUtil.size2expressions(a);

      for (Map.Entry<Integer, Set<OWLClassExpression>> set : size2subterms.entrySet()) {
        int size = set.getKey();
        if (size > 1) { // skip atomic symbols and binary things
          Set<OWLClassExpression> independent = set.getValue();
          // macrofy things
          for (OWLClassExpression i : independent) {
            if (evaluation2macro.containsKey(i)) {
              this.rewriter.rewrite(minimized, i, evaluation2macro.get(i));
            }
          }
        }
      }
      axiomMacrofication.setMinimization(minimized);
      macrofication.add(axiomMacrofication);
    }
  }

  private void initialiseLevelMaps() {

    this.level2expression_top2bottom = new TreeMap<>(Collections.reverseOrder());
    this.level2expression_bottom2top = new TreeMap<>();

    for (OWLClassExpression e : this.subterms) {
      int level = getLevel(e);
      this.level2expression_bottom2top.putIfAbsent(level, new HashSet<>());
      this.level2expression_bottom2top.get(level).add(e);

      this.level2expression_top2bottom.putIfAbsent(level, new HashSet<>());
      this.level2expression_top2bottom.get(level).add(e);
    }
  }

  public TreeMap<Integer, Set<OWLClassExpression>> getLevels_bottom2top() {
    return this.level2expression_bottom2top;
  }

  public TreeMap<Integer, Set<OWLClassExpression>> getLevels_top2bottom() {
    return this.level2expression_top2bottom;
  }

  public Map<OWLClassExpression, Integer> getExpression2occurrence() {
    return this.expression2occurrence;
  }

  public Map<OWLClassExpression, Integer> getExpression2size() {
    return this.expression2size;
  }

  public Map<OWLClassExpression, Set<OWLClassExpression>> getParents() {
    return this.parents;
  }

  private void buildSubTermRelationship(OWLAxiom a) {
    for (OWLClassExpression exp : a.getNestedClassExpressions()) {
      Set<OWLClassExpression> allSubterms = exp.getNestedClassExpressions();
      this.subterms.addAll(allSubterms);
      allSubterms.remove(exp);
      this.children.put(exp, allSubterms);
      for (OWLClassExpression e : allSubterms) {
        this.parents.putIfAbsent(e, new HashSet<>());
        this.parents.get(e).add(exp);
      }
    }
  }

  private void initialiseSizeAndOccurrence(OWLAxiom a) {

    OWLAxiom noannotations = a.getAxiomWithoutAnnotations();
    SyntaxTree tree = this.treeBuilder.build(noannotations);

    // initialise expression2occurrence + expression2size
    for (SyntaxNode node : tree.getNodes()) {
      if (node instanceof ClassNode) {
        ClassNode n = (ClassNode) node;
        OWLClassExpression e = n.getExpression();
        SyntaxTree eTree = treeBuilder.build(e);
        this.expression2size.putIfAbsent(e, eTree.getSize());
        this.expression2occurrence.putIfAbsent(e, 0);
        this.expression2occurrence.put(e, this.expression2occurrence.get(e) + 1);
      }
      if (node instanceof SubClassOfNode) {
        SubClassOfNode n = (SubClassOfNode) node;
        OWLClassExpression e = n.getExpression();
        SyntaxTree eTree = treeBuilder.build(e);
        this.expression2size.putIfAbsent(e, eTree.getSize());
        this.expression2occurrence.putIfAbsent(e, 0);
        this.expression2occurrence.put(e, this.expression2occurrence.get(e) + 1);
      }
      if (node instanceof SuperClassOfNode) {
        SuperClassOfNode n = (SuperClassOfNode) node;
        OWLClassExpression e = n.getExpression();
        SyntaxTree eTree = treeBuilder.build(e);
        this.expression2size.putIfAbsent(e, eTree.getSize());
        this.expression2occurrence.putIfAbsent(e, 0);
        this.expression2occurrence.put(e, this.expression2occurrence.get(e) + 1);
      }
      if (node instanceof UnionNode) {
        UnionNode n = (UnionNode) node;
        OWLClassExpression e = n.getExpression();
        SyntaxTree eTree = treeBuilder.build(e);
        this.expression2size.putIfAbsent(e, eTree.getSize());
        this.expression2occurrence.putIfAbsent(e, 0);
        this.expression2occurrence.put(e, this.expression2occurrence.get(e) + 1);
      }
    }
  }

  private int getLevel(OWLClassExpression exp) {
    int level = 0;
    if (!this.children.get(exp).isEmpty()) {
      level += 1;
      int maxChildrenLevel = -1;
      for (OWLClassExpression c : this.children.get(exp)) {
        int cLevel = getLevel(c);
        if (cLevel > maxChildrenLevel) {
          maxChildrenLevel = cLevel;
        }
      }
      level += maxChildrenLevel;
    }
    return level;
  }

  private Set<OWLAxiom> getAxiomsWithNegation(Set<OWLAxiom> axioms) {
    // remove axioms with negation
    Set<OWLAxiom> axiomsWithNegation = new HashSet<>();
    for (OWLAxiom a : axioms) {
      for (OWLClassExpression exp : a.getNestedClassExpressions()) {
        if (exp instanceof OWLObjectComplementOf) {
          axiomsWithNegation.add(a);
        }
      }
    }
    return axiomsWithNegation;
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
