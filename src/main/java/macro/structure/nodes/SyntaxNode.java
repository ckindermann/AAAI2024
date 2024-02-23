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
// For example, a SubClassOf node represents a class expression that needs to appear as the subclass
// in a subclass axiom.
public class SyntaxNode {

  private OWLObject object; // associate term in OWL

  private int occurrence;
  private int children;

  public SyntaxNode(OWLObject o) {
    this.object = o;
    this.occurrence = 1;
    this.children = 0;
  }

  public int getOccurrence() {
    return this.occurrence;
  }

  public void setOccurrence(int occ) {
    this.occurrence = occ;
  }

  public void increaseChildren(int c) {
    this.children += c;
  }

  public String toString() {
    if (object == null) {
      return "null";
    } else {
      return this.object.toString();
    }
  }

  public OWLObject getObject() {
    return this.object;
  }

  public void setObject(OWLObject o) {
    this.object = o;
  }
}
