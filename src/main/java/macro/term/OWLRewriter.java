package macro.term;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;
import macro.ont.*;
import macro.parser.*;
import macro.structure.*;
import macro.structure.nodes.*;
import macro.util.*;
import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.traverse.*;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.*;

/** Replaces a term within a term */
public class OWLRewriter {

  private SyntaxTree synTree;
  private SimpleDirectedGraph<SyntaxNode, DefaultEdge> tree;

  private OWLClassExpression target;
  private OWLClassExpression replacement;

  private SyntaxTree replacementSynTree;
  private SimpleDirectedGraph<SyntaxNode, DefaultEdge> replacementTree;

  private Set<SyntaxNode> matches;
  private SyntaxTreeBuilder builder;

  public OWLRewriter() {
    this.matches = new HashSet<>();
    this.builder = new SyntaxTreeBuilder();
  }

  public OWLRewriter(SyntaxTree tree, OWLClassExpression t, OWLClassExpression r) {
    this.builder = new SyntaxTreeBuilder();

    this.synTree = tree;
    this.target = t;
    this.replacement = r;

    this.tree = this.synTree.getTree();
    this.matches = new HashSet<>();
    this.matches = findMatches();
    this.replace();
    // each node contains its associate term in OWL.
    // This needs to be rebuilt if the term tree changed
    this.compileOWL();
  }

  // NB: this mutates the given input tree!
  public void rewrite(SyntaxTree tree, OWLClassExpression t, OWLClassExpression r) {
    this.synTree = tree;
    this.target = t;
    this.replacement = r;

    this.tree = this.synTree.getTree();
    this.matches = findMatches(); // find t in tree
    this.replace();
    this.compileOWL();
  }

  // find all syntax nodes in the input tree
  // that are the root of 'isomorphic' trees to the one we want to replace
  // note that check for 'isomorphisms' using terms in OWL, that is,
  // every node can be associated with an OWL expression which can be tested for equality
  public Set<SyntaxNode> findMatches() {
    this.matches.clear();
    for (SyntaxNode node : this.tree.vertexSet()) {
      // CardinalityNode are special in the sense that they contain symbols for integers,
      // which are not OWLObjects. Note that integer symbols are constant in the context of our
      // work and never need to be replaced with a macro. So, they can simply be excluded here
      if (!(node instanceof CardinalityNode) && node.getObject().equals(this.target)) {
        matches.add(node);
      }
    }
    return matches;
  }

  public void compileOWL() {
    OWLCompiler compiler = new OWLCompiler(this.synTree);
    compiler.compile();
  }

  private void replace() {
    // construct replacement tree

    // NB: matches are initialisd in the 'rewrite' function before 'replace' is called
    for (SyntaxNode m : this.matches) {
      this.replacementSynTree = builder.build(this.replacement);
      this.replacementTree = this.replacementSynTree.getTree();

      // ensure that the replacement tree maintains labels of the original tree that indicate order
      // (this corresponds to edge labels described in our paper)
      this.setReplacementTreeRoot(m);

      // populate entry points
      // an entry point is the 'parent node' of a term that needs to be replaced
      // - there can be MULTIPLE entry points
      // if a term occurs multiple times as a subterm in another term
      // - there can be NO entry points, if the term has no parent
      Set<SyntaxNode> entryPoints = getEntryPoints(m);

      // if the entry points are empty, i.e.,
      // there are no parents to which the replacement needs to be connected,
      // then replace the entire term tree with the replacement
      if (entryPoints.isEmpty()) {
        this.replaceEntireTree();
      } else {

        // if there are entry points, then we need to 'rewire' the edges of the term tree
        // accordingly

        // remove nodes and edges that are being replaced
        Set<DefaultEdge> edges2delete = getBranches(m);
        Set<SyntaxNode> nodes2delete = getNodes(edges2delete);
        if (nodes2delete.isEmpty()) {
          nodes2delete.add(
              m); // we are trying to delete a leaf node .. this doesn;t have branches- so the above
          // code doesn't catch this
        }
        edges2delete.addAll(this.tree.incomingEdgesOf(m));
        this.tree.removeAllEdges(edges2delete);
        this.tree.removeAllVertices(nodes2delete);

        // add nodes and edges corresponding to replacement
        SyntaxNode replacementRoot = this.replacementSynTree.getRoot();
        Set<SyntaxNode> newNodes = this.replacementSynTree.getTree().vertexSet();
        Set<DefaultEdge> newEdges = this.replacementSynTree.getTree().edgeSet();

        for (SyntaxNode n : newNodes) {
          this.tree.addVertex(n);
        }
        for (DefaultEdge e : newEdges) {
          SyntaxNode source = this.replacementSynTree.getTree().getEdgeSource(e);
          SyntaxNode target = this.replacementSynTree.getTree().getEdgeTarget(e);
          this.tree.addEdge(source, target);
        }
        for (SyntaxNode n : entryPoints) {
          this.tree.addEdge(n, replacementRoot);
        }
      }
    }
  }

  private void setReplacementTreeRoot(SyntaxNode match) {
    if (match instanceof SuperClassOfNode) {
      SuperClassOfNode nr = new SuperClassOfNode(this.replacement);
      this.setRootTo(nr);
    }

    if (match instanceof SubClassOfNode) {
      SubClassOfNode nr = new SubClassOfNode(this.replacement);
      this.setRootTo(nr);
    }

    if (match instanceof UnionNode) {
      UnionNode nr = new UnionNode(this.replacement);
      this.setRootTo(nr);
    }
  }

  private void setRootTo(SyntaxNode nr) {

    SyntaxNode r = this.replacementSynTree.getRoot();
    Set<DefaultEdge> rootBranches = this.replacementTree.outgoingEdgesOf(r);

    Set<SyntaxNode> children = new HashSet<>();
    for (DefaultEdge e : rootBranches) {
      children.add(this.replacementTree.getEdgeTarget(e));
    }

    // for some reason the order is important here..
    this.replacementTree.removeVertex(r);
    this.replacementTree.removeAllEdges(rootBranches);

    this.replacementTree.addVertex(nr);
    for (SyntaxNode c : children) {
      this.replacementTree.addEdge(nr, c);
    }
    this.replacementSynTree.setRoot(nr);
  }

  private Set<SyntaxNode> getEntryPoints(SyntaxNode match) {
    Set<SyntaxNode> entryPoints = new HashSet<>();
    for (DefaultEdge e : this.tree.incomingEdgesOf(match)) {
      entryPoints.add(this.tree.getEdgeSource(e));
    }
    return entryPoints;
  }

  private void replaceEntireTree() {
    // remove all old nodes & edges
    Set<DefaultEdge> edges = this.tree.edgeSet();
    this.tree.removeAllEdges(edges);
    Set<SyntaxNode> nodes = this.tree.vertexSet();
    this.tree.removeAllVertices(nodes);

    // add all new nodes & edges
    Set<SyntaxNode> replacementNodes = this.replacementSynTree.getTree().vertexSet();
    for (SyntaxNode r : replacementNodes) {
      this.tree.addVertex(r);
    }
    Set<DefaultEdge> replacementEdges = this.replacementSynTree.getTree().edgeSet();
    for (DefaultEdge e : replacementEdges) {
      SyntaxNode source = this.replacementTree.getEdgeSource(e);
      SyntaxNode target = this.replacementTree.getEdgeTarget(e);

      this.tree.addEdge(source, target);
    }

    this.synTree.setRoot(this.replacementSynTree.getRoot());
  }

  public Set<SyntaxNode> getNodes(Set<DefaultEdge> edges) {
    Set<SyntaxNode> nodes = new HashSet<>();
    for (DefaultEdge e : edges) {
      nodes.add(this.tree.getEdgeTarget(e));
      nodes.add(this.tree.getEdgeSource(e));
    }
    return nodes;
  }

  public Set<DefaultEdge> getBranches(SyntaxNode node) {
    Set<DefaultEdge> branches = new HashSet<>();
    Set<SyntaxNode> current = new HashSet<>();
    Set<SyntaxNode> next = new HashSet<>();
    current.add(node);
    while (!current.isEmpty()) {
      for (SyntaxNode n : current) {
        Set<DefaultEdge> out = this.tree.outgoingEdgesOf(n);
        for (DefaultEdge e : out) {
          next.add(this.tree.getEdgeTarget(e));
        }
        branches.addAll(out);
      }
      current.clear();
      current.addAll(next);
      next.clear();
    }
    return branches;
  }
}
