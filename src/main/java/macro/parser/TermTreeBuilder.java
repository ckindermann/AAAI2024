package macro.parser;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.util.stream.*;
import macro.ont.*;
import macro.structure.*;
import macro.structure.nodes.*;
import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.traverse.*;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.*;
import org.semanticweb.owlapi.util.*;

/** Class Expression Visitor */

// given a syntax tree for an axiom: build syntax trees for all subexpressions
// 1. traverse syntax tree
// 2. depending on the node - extract OWL object and run SyntaxTreeBuilder (or build something
// custom in the case of integegrs/cardinality nodes and strings
public class TermTreeBuilder {

  private SyntaxTreeBuilder syntaxTreeBuilder;

  public TermTreeBuilder() {
    this.syntaxTreeBuilder = new SyntaxTreeBuilder();
  }

  public SyntaxTree build(SyntaxNode node) {

    if (node instanceof AxiomNode) {
      AxiomNode an = (AxiomNode) node;
      return syntaxTreeBuilder.build(an.getAxiom());
    }

    if (node instanceof CardinalityNode) {
      SimpleDirectedGraph<SyntaxNode, DefaultEdge> syntaxTree =
          new SimpleDirectedGraph<>(DefaultEdge.class);
      CardinalityNode cn = (CardinalityNode) node;
      CardinalityNode copy = new CardinalityNode(cn.getCardinality());
      syntaxTree.addVertex(copy);
      return new SyntaxTree(syntaxTree, copy);
    }

    if (node instanceof ClassNode) {
      ClassNode cn = (ClassNode) node;
      return syntaxTreeBuilder.buildTerm(cn.getExpression());
    }

    if (node instanceof DataRangeNode) {
      DataRangeNode dn = (DataRangeNode) node;
      return syntaxTreeBuilder.buildTerm(dn.getDataRange());
    }

    if (node instanceof PropertyChainNode) {
      SimpleDirectedGraph<SyntaxNode, DefaultEdge> syntaxTree =
          new SimpleDirectedGraph<>(DefaultEdge.class);
      PropertyChainNode cn = (PropertyChainNode) node;
      PropertyChainNode copy = new PropertyChainNode(cn.getPropertyExpression());
      syntaxTree.addVertex(copy);

      for (OWLObjectPropertyExpression pe : cn.getPropertyExpression()) {
        SyntaxTree tree = syntaxTreeBuilder.buildTerm(pe);

        SimpleDirectedGraph<SyntaxNode, DefaultEdge> graph = tree.getTree();

        for (SyntaxNode n : graph.vertexSet()) {
          syntaxTree.addVertex(n);
        }
        for (DefaultEdge e : graph.edgeSet()) {
          syntaxTree.addEdge(graph.getEdgeSource(e), graph.getEdgeTarget(e));
        }

        SyntaxNode root = tree.getRoot();
        syntaxTree.addEdge(copy, root);
      }

      return new SyntaxTree(syntaxTree, copy);
    }

    if (node instanceof FacetRestrictionNode) {
      FacetRestrictionNode fn = (FacetRestrictionNode) node;
      return syntaxTreeBuilder.buildTerm(fn.getFacetRestriction());
    }

    if (node instanceof IndividualNode) {
      IndividualNode in = (IndividualNode) node;
      return syntaxTreeBuilder.buildTerm(in.getIndividual());
    }

    if (node instanceof LiteralNode) {
      LiteralNode ln = (LiteralNode) node;
      return syntaxTreeBuilder.buildTerm(ln.getLiteral());
    }

    if (node instanceof PropertyNode) {
      PropertyNode pn = (PropertyNode) node;
      return syntaxTreeBuilder.buildTerm(pn.getPropertyExpression());
    }

    if (node instanceof SubClassOfNode) {
      SubClassOfNode sn = (SubClassOfNode) node;
      return syntaxTreeBuilder.buildTerm(sn.getExpression());
    }

    if (node instanceof SuperClassOfNode) {
      SuperClassOfNode sn = (SuperClassOfNode) node;
      return syntaxTreeBuilder.buildTerm(sn.getExpression());
    }

    if (node instanceof UnionNode) {
      UnionNode un = (UnionNode) node;
      return syntaxTreeBuilder.buildTerm(un.getExpression());
    }

    return null;
  }
}
