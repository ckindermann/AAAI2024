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
public class AxiomNode extends SyntaxNode {

  private OWLAxiom axiom;

  public AxiomNode(OWLAxiom a) {
    super(a);
    this.axiom = a;
  }

  public OWLAxiom getAxiom() {
    return this.axiom;
  }

  public void setAxiom(OWLAxiom a) {
    this.setObject(a);
    this.axiom = a;
  }
}
