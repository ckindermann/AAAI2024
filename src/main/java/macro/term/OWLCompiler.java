package macro.term;

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
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.*;

/** Transforms an Term Tree (Abstract Syntax Tree) back into a term, i.e., an OWL expression */
public class OWLCompiler {

  private SyntaxTree synTree;
  private SyntaxNode root;
  private SimpleDirectedGraph<SyntaxNode, DefaultEdge> tree;
  private OWLClassExpression expression;
  private OWLDataFactory factory;

  public OWLCompiler(SyntaxTree t) {
    this.synTree = t;
    this.tree = this.synTree.getTree();
    this.root = this.synTree.getRoot();

    this.factory = OWLManager.createOWLOntologyManager().getOWLDataFactory();
  }

  public SyntaxTree getTree() {
    return this.synTree;
  }

  public void compile() {
    if (this.root instanceof AxiomNode) {
      compileAxiomNode((AxiomNode) this.root);
    }
    if (this.root instanceof ClassNode) {
      compileClassExpressionNode(this.root);
    }
    if (this.root instanceof SubClassOfNode) {
      compileClassExpressionNode(this.root);
    }
    if (this.root instanceof SuperClassOfNode) {
      compileClassExpressionNode(this.root);
    }
    if (this.root instanceof UnionNode) {
      compileClassExpressionNode(this.root);
    }
    // TODO: other kinds of nodes
  }

  private Set<SyntaxNode> getSubExpressionNodes(SyntaxNode n) {
    Set<DefaultEdge> edgeToSubexpression = this.tree.outgoingEdgesOf(n);
    Set<SyntaxNode> res = new HashSet<>();
    for (DefaultEdge e : edgeToSubexpression) {
      res.add(this.tree.getEdgeTarget(e));
    }
    return res;
  }

  public OWLAxiom compileAxiomNode(AxiomNode n) {
    OWLAxiom axiom = n.getAxiom();

    Set<SyntaxNode> children = getSubExpressionNodes(n);

    // SubClassOf
    if (axiom instanceof OWLSubClassOfAxiom) {

      OWLClassExpression sub = null;
      OWLClassExpression sup = null;

      for (SyntaxNode c : children) {
        if (c instanceof SubClassOfNode) {
          sub = compileClassExpressionNode(c);
        }
        if (c instanceof SuperClassOfNode) {
          sup = compileClassExpressionNode(c);
        }
      }

      OWLAxiom a = this.factory.getOWLSubClassOfAxiom(sub, sup);
      n.setAxiom(a);
      return a;
    }

    // EquivalentClasses
    if (axiom instanceof OWLEquivalentClassesAxiom) {

      Set<OWLClassExpression> arguments = new HashSet<>();
      for (SyntaxNode c : children) {
        arguments.add(compileClassExpressionNode(c));
      }
      OWLAxiom a = this.factory.getOWLEquivalentClassesAxiom(arguments);
      n.setAxiom(a);
      return a;
    }

    // DisjointClasses
    if (axiom instanceof OWLDisjointClassesAxiom) {

      Set<OWLClassExpression> arguments = new HashSet<>();
      for (SyntaxNode c : children) {
        arguments.add(compileClassExpressionNode(c));
      }
      OWLAxiom a = this.factory.getOWLDisjointClassesAxiom(arguments);
      n.setAxiom(a);
      return a;
    }

    // DisjointUnion
    if (axiom instanceof OWLDisjointUnionAxiom) {

      Set<OWLClassExpression> arguments = new HashSet<>();
      OWLClass unionNode = null;
      for (SyntaxNode c : children) {
        if (c instanceof UnionNode) {
          unionNode = (OWLClass) compileClassExpressionNode(c);
        } else {
          arguments.add(compileClassExpressionNode(c));
        }
      }
      OWLAxiom a = this.factory.getOWLDisjointUnionAxiom(unionNode, arguments);
      n.setAxiom(a);
      return a;
    }

    return null;
  }

  public OWLClassExpression compileClassExpressionNode(SyntaxNode n) {

    Set<SyntaxNode> children = getSubExpressionNodes(n);

    // Class Node
    if (n instanceof ClassNode) {
      ClassNode node = (ClassNode) n;
      OWLClassExpression exp = node.getExpression();
      // nothing to be done -- base case (or no nested class expressions)
      if (exp instanceof OWLClass
          || exp instanceof OWLDataSomeValuesFrom
          || exp instanceof OWLDataAllValuesFrom
          || exp instanceof OWLDataHasValue
          || exp instanceof OWLDataMinCardinality
          || exp instanceof OWLDataMaxCardinality
          || exp instanceof OWLObjectOneOf
          || exp instanceof OWLObjectHasSelf
          || exp instanceof OWLObjectHasValue
          || exp instanceof OWLDataExactCardinality) {
        return exp;
      }

      if (exp instanceof OWLObjectSomeValuesFrom) {
        OWLObjectPropertyExpression property = null;
        OWLClassExpression filler = null;

        for (SyntaxNode c : children) {
          if (c instanceof ClassNode) {
            filler = compileClassExpressionNode(c);
          }
          if (c instanceof PropertyNode) {
            PropertyNode pnode = (PropertyNode) c;
            property = (OWLObjectPropertyExpression) pnode.getPropertyExpression();
          }
        }

        OWLClassExpression change = this.factory.getOWLObjectSomeValuesFrom(property, filler);
        node.setExpression(change);
        return change;
      }

      if (exp instanceof OWLObjectAllValuesFrom) {
        OWLObjectPropertyExpression property = null;
        OWLClassExpression filler = null;

        for (SyntaxNode c : children) {
          if (c instanceof ClassNode) {
            filler = compileClassExpressionNode(c);
          }
          if (c instanceof PropertyNode) {
            PropertyNode pnode = (PropertyNode) c;
            property = (OWLObjectPropertyExpression) pnode.getPropertyExpression();
          }
        }

        OWLClassExpression change = this.factory.getOWLObjectAllValuesFrom(property, filler);
        node.setExpression(change);
        return change;
      }

      if (exp instanceof OWLObjectComplementOf) {
        OWLClassExpression argument = null;

        for (SyntaxNode c : children) {
          if (c instanceof ClassNode) {
            argument = compileClassExpressionNode(c);
          }
        }

        OWLClassExpression change = this.factory.getOWLObjectComplementOf(argument);
        node.setExpression(change);
        return change;
      }

      if (exp instanceof OWLObjectExactCardinality) {

        OWLObjectPropertyExpression property = null;
        int cardinality = -1;
        OWLClassExpression filler = null;

        for (SyntaxNode c : children) {
          if (c instanceof ClassNode) {
            filler = compileClassExpressionNode(c);
          }
          if (c instanceof PropertyNode) {
            PropertyNode pnode = (PropertyNode) c;
            property = (OWLObjectPropertyExpression) pnode.getPropertyExpression();
          }
          if (c instanceof CardinalityNode) {
            CardinalityNode cnode = (CardinalityNode) c;
            cardinality = cnode.getCardinality();
          }
        }

        if (filler != null) {
          OWLClassExpression change =
              this.factory.getOWLObjectExactCardinality(cardinality, property, filler);
          node.setExpression(change);
          return change;
        } else {
          OWLClassExpression change =
              this.factory.getOWLObjectExactCardinality(cardinality, property);
          node.setExpression(change);
          return change;
        }
      }

      if (exp instanceof OWLObjectMinCardinality) {

        OWLObjectPropertyExpression property = null;
        int cardinality = -1;
        OWLClassExpression filler = null;

        for (SyntaxNode c : children) {
          if (c instanceof ClassNode) {
            filler = compileClassExpressionNode(c);
          }
          if (c instanceof PropertyNode) {
            PropertyNode pnode = (PropertyNode) c;
            property = (OWLObjectPropertyExpression) pnode.getPropertyExpression();
          }
          if (c instanceof CardinalityNode) {
            CardinalityNode cnode = (CardinalityNode) c;
            cardinality = cnode.getCardinality();
          }
        }

        if (filler != null) {
          OWLClassExpression change =
              this.factory.getOWLObjectMinCardinality(cardinality, property, filler);
          node.setExpression(change);
          return change;
        } else {
          OWLClassExpression change =
              this.factory.getOWLObjectMinCardinality(cardinality, property);
          node.setExpression(change);
          return change;
        }
      }

      if (exp instanceof OWLObjectMaxCardinality) {

        OWLObjectPropertyExpression property = null;
        int cardinality = -1;
        OWLClassExpression filler = null;

        for (SyntaxNode c : children) {
          if (c instanceof ClassNode) {
            filler = compileClassExpressionNode(c);
          }
          if (c instanceof PropertyNode) {
            PropertyNode pnode = (PropertyNode) c;
            property = (OWLObjectPropertyExpression) pnode.getPropertyExpression();
          }
          if (c instanceof CardinalityNode) {
            CardinalityNode cnode = (CardinalityNode) c;
            cardinality = cnode.getCardinality();
          }
        }

        if (filler != null) {
          OWLClassExpression change =
              this.factory.getOWLObjectMaxCardinality(cardinality, property, filler);
          node.setExpression(change);
          return change;
        } else {
          OWLClassExpression change =
              this.factory.getOWLObjectMaxCardinality(cardinality, property);
          node.setExpression(change);
          return change;
        }
      }

      if (exp instanceof OWLObjectIntersectionOf) {

        Set<OWLClassExpression> arguments = new HashSet<>();
        for (SyntaxNode c : children) {
          arguments.add(compileClassExpressionNode(c));
        }

        OWLClassExpression change = this.factory.getOWLObjectIntersectionOf(arguments);
        node.setExpression(change);
        return change;
      }

      if (exp instanceof OWLObjectUnionOf) {

        Set<OWLClassExpression> arguments = new HashSet<>();
        for (SyntaxNode c : children) {
          arguments.add(compileClassExpressionNode(c));
        }

        OWLClassExpression change = this.factory.getOWLObjectUnionOf(arguments);
        node.setExpression(change);
        return change;
      }
    }

    if (n instanceof SubClassOfNode) {
      SubClassOfNode node = (SubClassOfNode) n;
      OWLClassExpression exp = node.getExpression();
      // nothing to be done -- base case (or no nested class expressions)
      if (exp instanceof OWLClass
          || exp instanceof OWLDataSomeValuesFrom
          || exp instanceof OWLDataAllValuesFrom
          || exp instanceof OWLDataHasValue
          || exp instanceof OWLDataMinCardinality
          || exp instanceof OWLDataMaxCardinality
          || exp instanceof OWLObjectOneOf
          || exp instanceof OWLObjectHasSelf
          || exp instanceof OWLObjectHasValue
          || exp instanceof OWLDataExactCardinality) {
        return exp;
      }

      if (exp instanceof OWLObjectSomeValuesFrom) {
        OWLObjectPropertyExpression property = null;
        OWLClassExpression filler = null;

        for (SyntaxNode c : children) {
          if (c instanceof ClassNode) {
            filler = compileClassExpressionNode(c);
          }
          if (c instanceof PropertyNode) {
            PropertyNode pnode = (PropertyNode) c;
            property = (OWLObjectPropertyExpression) pnode.getPropertyExpression();
          }
        }

        OWLClassExpression change = this.factory.getOWLObjectSomeValuesFrom(property, filler);
        node.setExpression(change);
        return change;
      }

      if (exp instanceof OWLObjectAllValuesFrom) {
        OWLObjectPropertyExpression property = null;
        OWLClassExpression filler = null;

        for (SyntaxNode c : children) {
          if (c instanceof ClassNode) {
            filler = compileClassExpressionNode(c);
          }
          if (c instanceof PropertyNode) {
            PropertyNode pnode = (PropertyNode) c;
            property = (OWLObjectPropertyExpression) pnode.getPropertyExpression();
          }
        }

        OWLClassExpression change = this.factory.getOWLObjectAllValuesFrom(property, filler);
        node.setExpression(change);
        return change;
      }

      if (exp instanceof OWLObjectComplementOf) {
        OWLClassExpression argument = null;

        for (SyntaxNode c : children) {
          if (c instanceof ClassNode) {
            argument = compileClassExpressionNode(c);
          }
        }

        OWLClassExpression change = this.factory.getOWLObjectComplementOf(argument);
        node.setExpression(change);
        return change;
      }

      if (exp instanceof OWLObjectExactCardinality) {

        OWLObjectPropertyExpression property = null;
        int cardinality = -1;
        OWLClassExpression filler = null;

        for (SyntaxNode c : children) {
          if (c instanceof ClassNode) {
            filler = compileClassExpressionNode(c);
          }
          if (c instanceof PropertyNode) {
            PropertyNode pnode = (PropertyNode) c;
            property = (OWLObjectPropertyExpression) pnode.getPropertyExpression();
          }
          if (c instanceof CardinalityNode) {
            CardinalityNode cnode = (CardinalityNode) c;
            cardinality = cnode.getCardinality();
          }
        }

        if (filler != null) {
          OWLClassExpression change =
              this.factory.getOWLObjectExactCardinality(cardinality, property, filler);
          node.setExpression(change);
          return change;
        } else {
          OWLClassExpression change =
              this.factory.getOWLObjectExactCardinality(cardinality, property);
          node.setExpression(change);
          return change;
        }
      }

      if (exp instanceof OWLObjectMinCardinality) {

        OWLObjectPropertyExpression property = null;
        int cardinality = -1;
        OWLClassExpression filler = null;

        for (SyntaxNode c : children) {
          if (c instanceof ClassNode) {
            filler = compileClassExpressionNode(c);
          }
          if (c instanceof PropertyNode) {
            PropertyNode pnode = (PropertyNode) c;
            property = (OWLObjectPropertyExpression) pnode.getPropertyExpression();
          }
          if (c instanceof CardinalityNode) {
            CardinalityNode cnode = (CardinalityNode) c;
            cardinality = cnode.getCardinality();
          }
        }

        if (filler != null) {
          OWLClassExpression change =
              this.factory.getOWLObjectMinCardinality(cardinality, property, filler);
          node.setExpression(change);
          return change;
        } else {
          OWLClassExpression change =
              this.factory.getOWLObjectMinCardinality(cardinality, property);
          node.setExpression(change);
          return change;
        }
      }

      if (exp instanceof OWLObjectMaxCardinality) {

        OWLObjectPropertyExpression property = null;
        int cardinality = -1;
        OWLClassExpression filler = null;

        for (SyntaxNode c : children) {
          if (c instanceof ClassNode) {
            filler = compileClassExpressionNode(c);
          }
          if (c instanceof PropertyNode) {
            PropertyNode pnode = (PropertyNode) c;
            property = (OWLObjectPropertyExpression) pnode.getPropertyExpression();
          }
          if (c instanceof CardinalityNode) {
            CardinalityNode cnode = (CardinalityNode) c;
            cardinality = cnode.getCardinality();
          }
        }

        if (filler != null) {
          OWLClassExpression change =
              this.factory.getOWLObjectMaxCardinality(cardinality, property, filler);
          node.setExpression(change);
          return change;
        } else {
          OWLClassExpression change =
              this.factory.getOWLObjectMaxCardinality(cardinality, property);
          node.setExpression(change);
          return change;
        }
      }

      if (exp instanceof OWLObjectIntersectionOf) {

        Set<OWLClassExpression> arguments = new HashSet<>();
        for (SyntaxNode c : children) {
          arguments.add(compileClassExpressionNode(c));
        }

        OWLClassExpression change = this.factory.getOWLObjectIntersectionOf(arguments);
        node.setExpression(change);
        return change;
      }

      if (exp instanceof OWLObjectUnionOf) {

        Set<OWLClassExpression> arguments = new HashSet<>();
        for (SyntaxNode c : children) {
          arguments.add(compileClassExpressionNode(c));
        }

        OWLClassExpression change = this.factory.getOWLObjectUnionOf(arguments);
        node.setExpression(change);
        return change;
      }
    }

    if (n instanceof SuperClassOfNode) {
      SuperClassOfNode node = (SuperClassOfNode) n;
      OWLClassExpression exp = node.getExpression();
      // nothing to be done -- base case (or no nested class expressions)
      if (exp instanceof OWLClass
          || exp instanceof OWLDataSomeValuesFrom
          || exp instanceof OWLDataAllValuesFrom
          || exp instanceof OWLDataHasValue
          || exp instanceof OWLDataMinCardinality
          || exp instanceof OWLDataMaxCardinality
          || exp instanceof OWLObjectOneOf
          || exp instanceof OWLObjectHasSelf
          || exp instanceof OWLObjectHasValue
          || exp instanceof OWLDataExactCardinality) {
        return exp;
      }

      if (exp instanceof OWLObjectSomeValuesFrom) {
        OWLObjectPropertyExpression property = null;
        OWLClassExpression filler = null;

        for (SyntaxNode c : children) {
          if (c instanceof ClassNode) {
            filler = compileClassExpressionNode(c);
          }
          if (c instanceof PropertyNode) {
            PropertyNode pnode = (PropertyNode) c;
            property = (OWLObjectPropertyExpression) pnode.getPropertyExpression();
          }
        }

        OWLClassExpression change = this.factory.getOWLObjectSomeValuesFrom(property, filler);
        node.setExpression(change);
        return change;
      }

      if (exp instanceof OWLObjectAllValuesFrom) {
        OWLObjectPropertyExpression property = null;
        OWLClassExpression filler = null;

        for (SyntaxNode c : children) {
          if (c instanceof ClassNode) {
            filler = compileClassExpressionNode(c);
          }
          if (c instanceof PropertyNode) {
            PropertyNode pnode = (PropertyNode) c;
            property = (OWLObjectPropertyExpression) pnode.getPropertyExpression();
          }
        }

        OWLClassExpression change = this.factory.getOWLObjectAllValuesFrom(property, filler);
        node.setExpression(change);
        return change;
      }

      if (exp instanceof OWLObjectComplementOf) {
        OWLClassExpression argument = null;

        for (SyntaxNode c : children) {
          if (c instanceof ClassNode) {
            argument = compileClassExpressionNode(c);
          }
        }

        OWLClassExpression change = this.factory.getOWLObjectComplementOf(argument);
        node.setExpression(change);
        return change;
      }

      if (exp instanceof OWLObjectExactCardinality) {

        OWLObjectPropertyExpression property = null;
        int cardinality = -1;
        OWLClassExpression filler = null;

        for (SyntaxNode c : children) {
          if (c instanceof ClassNode) {
            filler = compileClassExpressionNode(c);
          }
          if (c instanceof PropertyNode) {
            PropertyNode pnode = (PropertyNode) c;
            property = (OWLObjectPropertyExpression) pnode.getPropertyExpression();
          }
          if (c instanceof CardinalityNode) {
            CardinalityNode cnode = (CardinalityNode) c;
            cardinality = cnode.getCardinality();
          }
        }

        if (filler != null) {
          OWLClassExpression change =
              this.factory.getOWLObjectExactCardinality(cardinality, property, filler);
          node.setExpression(change);
          return change;
        } else {
          OWLClassExpression change =
              this.factory.getOWLObjectExactCardinality(cardinality, property);
          node.setExpression(change);
          return change;
        }
      }

      if (exp instanceof OWLObjectMinCardinality) {

        OWLObjectPropertyExpression property = null;
        int cardinality = -1;
        OWLClassExpression filler = null;

        for (SyntaxNode c : children) {
          if (c instanceof ClassNode) {
            filler = compileClassExpressionNode(c);
          }
          if (c instanceof PropertyNode) {
            PropertyNode pnode = (PropertyNode) c;
            property = (OWLObjectPropertyExpression) pnode.getPropertyExpression();
          }
          if (c instanceof CardinalityNode) {
            CardinalityNode cnode = (CardinalityNode) c;
            cardinality = cnode.getCardinality();
          }
        }

        if (filler != null) {
          OWLClassExpression change =
              this.factory.getOWLObjectMinCardinality(cardinality, property, filler);
          node.setExpression(change);
          return change;
        } else {
          OWLClassExpression change =
              this.factory.getOWLObjectMinCardinality(cardinality, property);
          node.setExpression(change);
          return change;
        }
      }

      if (exp instanceof OWLObjectMaxCardinality) {

        OWLObjectPropertyExpression property = null;
        int cardinality = -1;
        OWLClassExpression filler = null;

        for (SyntaxNode c : children) {
          if (c instanceof ClassNode) {
            filler = compileClassExpressionNode(c);
          }
          if (c instanceof PropertyNode) {
            PropertyNode pnode = (PropertyNode) c;
            property = (OWLObjectPropertyExpression) pnode.getPropertyExpression();
          }
          if (c instanceof CardinalityNode) {
            CardinalityNode cnode = (CardinalityNode) c;
            cardinality = cnode.getCardinality();
          }
        }

        if (filler != null) {
          OWLClassExpression change =
              this.factory.getOWLObjectMaxCardinality(cardinality, property, filler);
          node.setExpression(change);
          return change;
        } else {
          OWLClassExpression change =
              this.factory.getOWLObjectMaxCardinality(cardinality, property);
          node.setExpression(change);
          return change;
        }
      }

      if (exp instanceof OWLObjectIntersectionOf) {

        Set<OWLClassExpression> arguments = new HashSet<>();
        for (SyntaxNode c : children) {
          arguments.add(compileClassExpressionNode(c));
        }

        OWLClassExpression change = this.factory.getOWLObjectIntersectionOf(arguments);
        node.setExpression(change);
        return change;
      }

      if (exp instanceof OWLObjectUnionOf) {

        Set<OWLClassExpression> arguments = new HashSet<>();
        for (SyntaxNode c : children) {
          arguments.add(compileClassExpressionNode(c));
        }

        OWLClassExpression change = this.factory.getOWLObjectUnionOf(arguments);
        node.setExpression(change);
        return change;
      }
    }

    if (n instanceof UnionNode) {
      UnionNode node = (UnionNode) n;
      OWLClassExpression exp = node.getExpression();
      // nothing to be done -- base case (or no nested class expressions)
      if (exp instanceof OWLClass
          || exp instanceof OWLDataSomeValuesFrom
          || exp instanceof OWLDataAllValuesFrom
          || exp instanceof OWLDataHasValue
          || exp instanceof OWLDataMinCardinality
          || exp instanceof OWLDataMaxCardinality
          || exp instanceof OWLObjectOneOf
          || exp instanceof OWLObjectHasSelf
          || exp instanceof OWLObjectHasValue
          || exp instanceof OWLDataExactCardinality) {
        return exp;
      }

      if (exp instanceof OWLObjectSomeValuesFrom) {
        OWLObjectPropertyExpression property = null;
        OWLClassExpression filler = null;

        for (SyntaxNode c : children) {
          if (c instanceof ClassNode) {
            filler = compileClassExpressionNode(c);
          }
          if (c instanceof PropertyNode) {
            PropertyNode pnode = (PropertyNode) c;
            property = (OWLObjectPropertyExpression) pnode.getPropertyExpression();
          }
        }

        OWLClassExpression change = this.factory.getOWLObjectSomeValuesFrom(property, filler);
        node.setExpression(change);
        return change;
      }

      if (exp instanceof OWLObjectAllValuesFrom) {
        OWLObjectPropertyExpression property = null;
        OWLClassExpression filler = null;

        for (SyntaxNode c : children) {
          if (c instanceof ClassNode) {
            filler = compileClassExpressionNode(c);
          }
          if (c instanceof PropertyNode) {
            PropertyNode pnode = (PropertyNode) c;
            property = (OWLObjectPropertyExpression) pnode.getPropertyExpression();
          }
        }

        OWLClassExpression change = this.factory.getOWLObjectAllValuesFrom(property, filler);
        node.setExpression(change);
        return change;
      }

      if (exp instanceof OWLObjectComplementOf) {
        OWLClassExpression argument = null;

        for (SyntaxNode c : children) {
          if (c instanceof ClassNode) {
            argument = compileClassExpressionNode(c);
          }
        }

        OWLClassExpression change = this.factory.getOWLObjectComplementOf(argument);
        node.setExpression(change);
        return change;
      }

      if (exp instanceof OWLObjectExactCardinality) {

        OWLObjectPropertyExpression property = null;
        int cardinality = -1;
        OWLClassExpression filler = null;

        for (SyntaxNode c : children) {
          if (c instanceof ClassNode) {
            filler = compileClassExpressionNode(c);
          }
          if (c instanceof PropertyNode) {
            PropertyNode pnode = (PropertyNode) c;
            property = (OWLObjectPropertyExpression) pnode.getPropertyExpression();
          }
          if (c instanceof CardinalityNode) {
            CardinalityNode cnode = (CardinalityNode) c;
            cardinality = cnode.getCardinality();
          }
        }

        if (filler != null) {
          OWLClassExpression change =
              this.factory.getOWLObjectExactCardinality(cardinality, property, filler);
          node.setExpression(change);
          return change;
        } else {
          OWLClassExpression change =
              this.factory.getOWLObjectExactCardinality(cardinality, property);
          node.setExpression(change);
          return change;
        }
      }

      if (exp instanceof OWLObjectMinCardinality) {

        OWLObjectPropertyExpression property = null;
        int cardinality = -1;
        OWLClassExpression filler = null;

        for (SyntaxNode c : children) {
          if (c instanceof ClassNode) {
            filler = compileClassExpressionNode(c);
          }
          if (c instanceof PropertyNode) {
            PropertyNode pnode = (PropertyNode) c;
            property = (OWLObjectPropertyExpression) pnode.getPropertyExpression();
          }
          if (c instanceof CardinalityNode) {
            CardinalityNode cnode = (CardinalityNode) c;
            cardinality = cnode.getCardinality();
          }
        }

        if (filler != null) {
          OWLClassExpression change =
              this.factory.getOWLObjectMinCardinality(cardinality, property, filler);
          node.setExpression(change);
          return change;
        } else {
          OWLClassExpression change =
              this.factory.getOWLObjectMinCardinality(cardinality, property);
          node.setExpression(change);
          return change;
        }
      }

      if (exp instanceof OWLObjectMaxCardinality) {

        OWLObjectPropertyExpression property = null;
        int cardinality = -1;
        OWLClassExpression filler = null;

        for (SyntaxNode c : children) {
          if (c instanceof ClassNode) {
            filler = compileClassExpressionNode(c);
          }
          if (c instanceof PropertyNode) {
            PropertyNode pnode = (PropertyNode) c;
            property = (OWLObjectPropertyExpression) pnode.getPropertyExpression();
          }
          if (c instanceof CardinalityNode) {
            CardinalityNode cnode = (CardinalityNode) c;
            cardinality = cnode.getCardinality();
          }
        }

        if (filler != null) {
          OWLClassExpression change =
              this.factory.getOWLObjectMaxCardinality(cardinality, property, filler);
          node.setExpression(change);
          return change;
        } else {
          OWLClassExpression change =
              this.factory.getOWLObjectMaxCardinality(cardinality, property);
          node.setExpression(change);
          return change;
        }
      }

      if (exp instanceof OWLObjectIntersectionOf) {

        Set<OWLClassExpression> arguments = new HashSet<>();
        for (SyntaxNode c : children) {
          arguments.add(compileClassExpressionNode(c));
        }

        OWLClassExpression change = this.factory.getOWLObjectIntersectionOf(arguments);
        node.setExpression(change);
        return change;
      }

      if (exp instanceof OWLObjectUnionOf) {

        Set<OWLClassExpression> arguments = new HashSet<>();
        for (SyntaxNode c : children) {
          arguments.add(compileClassExpressionNode(c));
        }

        OWLClassExpression change = this.factory.getOWLObjectUnionOf(arguments);
        node.setExpression(change);
        return change;
      }
    }
    return null;
  }
}
