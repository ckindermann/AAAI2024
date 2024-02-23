package macro.structure.nodes;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.util.stream.*;
import macro.ont.*;
import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.traverse.*;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.*;
import org.semanticweb.owlapi.util.*;

/** Syntax Node A syntax node */
// Instead of edge labels we attach 'labels' indicating the order of subterms to nodes themselves.
// Note that this is not a significant deviation from the description in the paper because every
// edge has *exactly* one target node -- in other words, it doesn't matter whether the label is
// on the edge or the node. Here, all information is kept in nodes.
public class UnionNode extends SyntaxNode {

  private OWLClassExpression classExpression;

  public UnionNode(OWLClassExpression ce) {
    super(ce);
    this.classExpression = ce;
  }

  public OWLClassExpression getExpression() {
    return this.classExpression;
  }

  public void setExpression(OWLClassExpression ce) {
    this.setObject(ce);
    this.classExpression = ce;
  }
}
