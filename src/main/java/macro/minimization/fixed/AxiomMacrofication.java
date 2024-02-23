package macro.minimization.fixed;

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

/**
 * Macrofication for Axioms in an encoding.
 *
 * <p>A Macrofication contains the original axiom, the axiom in which all macro symbols are
 * exhaustively expanded, and the axiom in the form of the size-minimal encoding.
 */
public class AxiomMacrofication {
  private OWLAxiom axiom;
  private OWLAxiom expandedAxiom;
  private OWLAxiom minimizedAxiom;

  private SyntaxTree axiomSyntaxTree;
  private SyntaxTree expandedAxiomSyntaxTree;
  private SyntaxTree minimizedAxiomSyntaxTree;

  public AxiomMacrofication(SyntaxTree t) {
    this.axiomSyntaxTree = t;
    this.axiom = (OWLAxiom) t.getRoot().getObject();
  }

  public OWLAxiom getAxiom() {
    return this.axiom;
  }

  public OWLAxiom getExpandedAxiom() {
    return this.expandedAxiom;
  }

  public OWLAxiom getMinimizedAxiom() {
    return this.minimizedAxiom;
  }

  public void setExpansion(SyntaxTree t) {
    this.expandedAxiomSyntaxTree = t;
    this.expandedAxiom = (OWLAxiom) t.getRoot().getObject();
  }

  public void setMinimization(SyntaxTree t) {
    this.minimizedAxiomSyntaxTree = t;
    this.minimizedAxiom = (OWLAxiom) t.getRoot().getObject();
  }

  public SyntaxTree getAxiomTree() {
    return this.axiomSyntaxTree;
  }

  public SyntaxTree getExpandedTree() {
    return this.expandedAxiomSyntaxTree;
  }

  public SyntaxTree getMinimizationTree() {
    return this.minimizedAxiomSyntaxTree;
  }

  public String toString() {
    String output = "Axiom: " + this.axiom + "," + this.axiomSyntaxTree.getSize() + "\n";
    output +=
        "Expansion: " + this.expandedAxiom + "," + this.expandedAxiomSyntaxTree.getSize() + "\n";
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

    // each AxiomMacrofication is associated with its original axiom
    return this.axiom.equals(m.getAxiom());
  }

  @Override
  public int hashCode() {
    // each AxiomMacrofication is associated with its original axiom
    return this.axiom.initHashCode();
  }
}
