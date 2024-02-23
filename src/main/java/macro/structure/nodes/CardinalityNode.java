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
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.util.*;

/** Syntax Node A syntax node */
public class CardinalityNode extends SyntaxNode {

  private int cardinality;

  public CardinalityNode(int c) {
    super(null); // integers are not OWLObjects? okay...
    this.cardinality = c;
  }

  @Override
  public String toString() {
    return String.valueOf(this.cardinality);
  }

  public int getCardinality() {
    return this.cardinality;
  }
}
