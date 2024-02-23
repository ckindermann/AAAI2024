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
public class ClassNode extends SyntaxNode {

  private OWLClassExpression classExpression;

  public ClassNode(OWLClassExpression ce) {
    super(ce);
    this.classExpression = ce;
  }

  public OWLClassExpression getExpression() {
    return this.classExpression;
  }

  public void setExpression(OWLClassExpression ce) {
    this.classExpression = ce;
    this.setObject(ce);
  }
}
