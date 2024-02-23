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
public class PropertyNode extends SyntaxNode {

  private OWLPropertyExpression propertyExpression;

  public PropertyNode(OWLPropertyExpression pe) {
    super(pe);
    this.propertyExpression = pe;
  }

  public OWLPropertyExpression getPropertyExpression() {
    return this.propertyExpression;
  }
}
