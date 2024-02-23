package macro.structure;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.util.stream.*;
import macro.ont.*;
import macro.structure.nodes.*;
import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.traverse.*;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.*;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.util.*;

/** Syntax Tree */
public class SyntaxTree {

  private SimpleDirectedGraph<SyntaxNode, DefaultEdge> syntaxTree;
  private SyntaxNode root;

  public SyntaxTree(SimpleDirectedGraph<SyntaxNode, DefaultEdge> t, SyntaxNode r) {
    this.syntaxTree = t;
    this.root = r;
  }

  public SimpleDirectedGraph<SyntaxNode, DefaultEdge> getTree() {
    return this.syntaxTree;
  }

  public void setTree(SimpleDirectedGraph<SyntaxNode, DefaultEdge> t) {
    this.syntaxTree = t;
  }

  public int getSize() {
    return this.syntaxTree.vertexSet().size();
  }

  public SyntaxNode getRoot() {
    return this.root;
  }

  public void setRoot(SyntaxNode r) {
    this.root = r;
  }

  public Set<SyntaxNode> getNodes() {
    return this.syntaxTree.vertexSet();
  }
}
