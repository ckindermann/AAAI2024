package macro.minimization.general;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;
import macro.ont.*;
import macro.parser.*;
import macro.structure.*;
import macro.structure.nodes.*;
import macro.util.*;
import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.traverse.*;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.*;

public class AxiomMacrofication {
  private OWLAxiom axiom;
  private OWLAxiom minimizedAxiom;

  private SyntaxTree axiomSyntaxTree;
  private SyntaxTree minimizedAxiomSyntaxTree;

  public AxiomMacrofication(SyntaxTree t) {
    this.axiomSyntaxTree = t;
    this.axiom = (OWLAxiom) t.getRoot().getObject();
  }

  public OWLAxiom getAxiom() {
    return this.axiom;
  }

  public OWLAxiom getMinimizedAxiom() {
    return this.minimizedAxiom;
  }

  public void setMinimization(SyntaxTree t) {
    this.minimizedAxiomSyntaxTree = t;
    this.minimizedAxiom = (OWLAxiom) t.getRoot().getObject();
  }

  public SyntaxTree getAxiomTree() {
    return this.axiomSyntaxTree;
  }

  public SyntaxTree getMinimizationTree() {
    return this.minimizedAxiomSyntaxTree;
  }

  public String toString() {
    String output = "Axiom: " + this.axiom + "," + this.axiomSyntaxTree.getSize() + "\n";
    output +=
        "Miminization: " + this.minimizedAxiom + "," + this.minimizedAxiomSyntaxTree.getSize();
    return output;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof AxiomMacrofication)) {
      return false;
    }

    AxiomMacrofication m = (AxiomMacrofication) o;

    // TODO: this is dirty..
    return this.axiom.equals(m.getAxiom());
  }

  @Override
  public int hashCode() {
    // TODO: this is dirty..
    return this.axiom.initHashCode();
  }
}
