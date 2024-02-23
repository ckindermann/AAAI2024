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
public class PropertyChainNode extends SyntaxNode {

  private List<OWLObjectPropertyExpression> propertyExpressionChain;

  public PropertyChainNode(List<OWLObjectPropertyExpression> chain) {
    super(null); // List<OWLPropertyExpression> is not an OWLObject
    this.propertyExpressionChain = chain;
  }

  public List<OWLObjectPropertyExpression> getPropertyExpression() {
    return this.propertyExpressionChain;
  }

  // NB: Java's string representation for lists includes square brackets []
  // this representation is a bit confusing in the context of the string representation for graphs
  // of JGraphT
  @Override
  public String toString() {
    return propertyExpressionChain.toString();
  }
}
