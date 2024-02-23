package macro.structure;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.util.stream.*;
import macro.ont.*;
import macro.parser.*;
import macro.structure.nodes.*;
import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.traverse.*;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.*;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.util.*;

/** Syntax Tree */
public class SyntaxTreeUtil {

  private SyntaxTreeBuilder treeBuilder;

  public SyntaxTreeUtil() {
    this.treeBuilder = new SyntaxTreeBuilder();
  }

  public TreeMap<Integer, Set<OWLClassExpression>> size2expressions(OWLAxiom axiom) {
    TreeMap<Integer, Set<OWLClassExpression>> size2expressions =
        new TreeMap<>(Collections.reverseOrder());
    Set<OWLClassExpression> expressions = axiom.getNestedClassExpressions();

    for (OWLClassExpression exp : expressions) {
      SyntaxTree tree = this.treeBuilder.build(exp);
      int size = tree.getSize();
      size2expressions.putIfAbsent(size, new HashSet<>());
      size2expressions.get(size).add(exp);
    }
    return size2expressions;
  }

  // NB: this excludes top level expression (meaning the expression itself
  // this just gets the things for subterms
  public TreeMap<Integer, Set<OWLClassExpression>> size2expressions(OWLClassExpression expression) {
    TreeMap<Integer, Set<OWLClassExpression>> size2expressions =
        new TreeMap<>(Collections.reverseOrder());
    Set<OWLClassExpression> expressions = expression.getNestedClassExpressions();
    // expressions.remove(expression); // this excludes the expression itself
    for (OWLClassExpression exp : expressions) {
      SyntaxTree tree = this.treeBuilder.build(exp);
      int size = tree.getSize();
      size2expressions.putIfAbsent(size, new HashSet<>());
      size2expressions.get(size).add(exp);
    }
    return size2expressions;
  }

  // NB: this excludes top level expression (meaning the expression itself
  // this just gets the things for subterms
  public TreeMap<Integer, Set<OWLClassExpression>> size2subExpressions(
      OWLClassExpression expression) {
    TreeMap<Integer, Set<OWLClassExpression>> size2expressions =
        new TreeMap<>(Collections.reverseOrder());
    Set<OWLClassExpression> expressions = expression.getNestedClassExpressions();
    expressions.remove(expression); // this excludes the expression itself
    for (OWLClassExpression exp : expressions) {
      SyntaxTree tree = this.treeBuilder.build(exp);
      int size = tree.getSize();
      size2expressions.putIfAbsent(size, new HashSet<>());
      size2expressions.get(size).add(exp);
    }
    return size2expressions;
  }

  public OWLClassExpression getExpressionFromTree(SyntaxTree tree) {
    SyntaxNode n = tree.getRoot();
    if (n instanceof ClassNode) {
      ClassNode r = (ClassNode) n;
      return r.getExpression();
    }
    if (n instanceof SubClassOfNode) {
      SubClassOfNode r = (SubClassOfNode) n;
      return r.getExpression();
    }
    if (n instanceof SuperClassOfNode) {
      SuperClassOfNode r = (SuperClassOfNode) n;
      return r.getExpression();
    }
    if (n instanceof UnionNode) {
      UnionNode r = (UnionNode) n;
      return r.getExpression();
    }
    return null;
  }
}
