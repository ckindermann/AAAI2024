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
public class DataRangeNode extends SyntaxNode {

  private OWLDataRange dataRange;

  public DataRangeNode(OWLDataRange r) {
    super(r); // integers are not OWLObjects? okay...
    this.dataRange = r;
  }

  public OWLDataRange getDataRange() {
    return this.dataRange;
  }
}
