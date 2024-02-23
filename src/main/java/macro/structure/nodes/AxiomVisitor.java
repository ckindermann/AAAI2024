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

/** Logical Axiom Visitor */
public class AxiomVisitor implements OWLAxiomVisitor {

  private SyntaxNode syntaxNode;

  public SyntaxNode getSyntaxNode() {
    return this.syntaxNode;
  }

  public void visit(OWLAsymmetricObjectPropertyAxiom axiom) {
    this.syntaxNode = new AxiomNode(axiom);
  }

  public void visit(OWLClassAssertionAxiom axiom) {
    this.syntaxNode = new AxiomNode(axiom);
  }

  public void visit(OWLDataPropertyAssertionAxiom axiom) {
    this.syntaxNode = new AxiomNode(axiom);
  }

  public void visit(OWLDataPropertyDomainAxiom axiom) {
    this.syntaxNode = new AxiomNode(axiom);
  }

  public void visit(OWLDataPropertyRangeAxiom axiom) {
    this.syntaxNode = new AxiomNode(axiom);
  }

  public void visit(OWLDifferentIndividualsAxiom axiom) {
    this.syntaxNode = new AxiomNode(axiom);
  }

  public void visit(OWLDisjointClassesAxiom axiom) {
    this.syntaxNode = new AxiomNode(axiom);
  }

  public void visit(OWLDisjointDataPropertiesAxiom axiom) {
    this.syntaxNode = new AxiomNode(axiom);
  }

  public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
    this.syntaxNode = new AxiomNode(axiom);
  }

  public void visit(OWLDisjointUnionAxiom axiom) {
    this.syntaxNode = new AxiomNode(axiom);
  }

  public void visit(OWLEquivalentClassesAxiom axiom) {
    this.syntaxNode = new AxiomNode(axiom);
  }

  public void visit(OWLEquivalentDataPropertiesAxiom axiom) {
    this.syntaxNode = new AxiomNode(axiom);
  }

  public void visit(OWLEquivalentObjectPropertiesAxiom axiom) {
    this.syntaxNode = new AxiomNode(axiom);
  }

  public void visit(OWLFunctionalDataPropertyAxiom axiom) {
    this.syntaxNode = new AxiomNode(axiom);
  }

  public void visit(OWLFunctionalObjectPropertyAxiom axiom) {
    this.syntaxNode = new AxiomNode(axiom);
  }

  public void visit(OWLHasKeyAxiom axiom) {
    this.syntaxNode = new AxiomNode(axiom);
  }

  public void visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
    this.syntaxNode = new AxiomNode(axiom);
  }

  public void visit(OWLInverseObjectPropertiesAxiom axiom) {
    this.syntaxNode = new AxiomNode(axiom);
  }

  public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
    this.syntaxNode = new AxiomNode(axiom);
  }

  public void visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
    this.syntaxNode = new AxiomNode(axiom);
  }

  public void visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
    this.syntaxNode = new AxiomNode(axiom);
  }

  public void visit(OWLObjectPropertyAssertionAxiom axiom) {
    this.syntaxNode = new AxiomNode(axiom);
  }

  public void visit(OWLObjectPropertyDomainAxiom axiom) {
    this.syntaxNode = new AxiomNode(axiom);
  }

  public void visit(OWLObjectPropertyRangeAxiom axiom) {
    this.syntaxNode = new AxiomNode(axiom);
  }

  public void visit(OWLReflexiveObjectPropertyAxiom axiom) {
    this.syntaxNode = new AxiomNode(axiom);
  }

  public void visit(OWLSameIndividualAxiom axiom) {
    this.syntaxNode = new AxiomNode(axiom);
  }

  public void visit(OWLSubClassOfAxiom axiom) {
    this.syntaxNode = new AxiomNode(axiom);
  }

  public void visit(OWLSubDataPropertyOfAxiom axiom) {
    this.syntaxNode = new AxiomNode(axiom);
  }

  public void visit(OWLSubObjectPropertyOfAxiom axiom) {
    this.syntaxNode = new AxiomNode(axiom);
  }

  public void visit(OWLSubPropertyChainOfAxiom axiom) {
    this.syntaxNode = new AxiomNode(axiom);
  }

  public void visit(OWLSymmetricObjectPropertyAxiom axiom) {
    this.syntaxNode = new AxiomNode(axiom);
  }

  public void visit(OWLTransitiveObjectPropertyAxiom axiom) {
    this.syntaxNode = new AxiomNode(axiom);
  }

  // base things

  public void visit(SWRLRule node) {
    // TODO
    ; // ?
  }

  public void doDefault(Object object) {
    ; // ?
  }

  public void getDefaultReturnValue(Object object) {
    ; // ?
  }

  public void handleDefault(Object c) {
    ; // ?
  }

  // annotation axioms

  public void visit(OWLAnnotationAssertionAxiom axiom) {
    this.syntaxNode = new AxiomNode(axiom);
  }

  public void visit(OWLAnnotationPropertyDomainAxiom axiom) {
    this.syntaxNode = new AxiomNode(axiom);
  }

  public void visit(OWLAnnotationPropertyRangeAxiom axiom) {
    this.syntaxNode = new AxiomNode(axiom);
  }

  public void visit(OWLSubAnnotationPropertyOfAxiom axiom) {
    this.syntaxNode = new AxiomNode(axiom);
  }
}
