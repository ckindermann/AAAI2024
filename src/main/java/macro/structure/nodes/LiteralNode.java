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
public class LiteralNode extends SyntaxNode {

  private OWLLiteral literal;

  public LiteralNode(OWLLiteral l) {
    super(l);
    this.literal = l;
  }

  public OWLLiteral getLiteral() {
    return this.literal;
  }
}
