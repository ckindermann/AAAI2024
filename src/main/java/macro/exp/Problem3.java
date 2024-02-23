package macro.exp;

import java.io.*;
import java.nio.file.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.*;
import macro.abbr.explicit.*;
import macro.minimization.general.*;
import macro.ont.*;
import macro.parser.*;
import macro.structure.*;
import macro.structure.nodes.*;
import macro.term.*;
import macro.util.*;
import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.traverse.*;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.*;

/** Implementation for solving Problem 1 as described in the paper. */
public class Problem3 {
  private static final DecimalFormat df = new DecimalFormat("0.00");

  public static void main(String[] args) {

    String ontFilePath = args[0];
    String output = args[1];

    run(ontFilePath, output);
  }

  public static void run(String ontFilePath, String output) {

    File ontFile = new File(ontFilePath);
    OntologyLoader ontLoader = new OntologyLoader(ontFile, true);
    OWLOntology ont = ontLoader.getOntology();

    String ontologyName = Paths.get(ontFilePath).getFileName().toString();

    long startTime = System.nanoTime();

    // Rewriting system for Problem 3
    RewritingSystem system = new RewritingSystem(ont);

    long endTime = System.nanoTime();
    long duration = (endTime - startTime) / 1000000000; // divide by 1000000 to get milliseconds.
    System.out.println("Minimization Time: " + duration + " seconds");

    // The set of 'AxiomMacrofications' corresponds to the set of (all) axioms of the input
    // ontology.
    // An AxiomMacrofication contains information about each axiom (term) w.r.t.
    // - its original structure
    // - its fully expanded structure
    // - its exhaustive macrofication
    // Note that this set includes axioms that are interpreted as macro definitions.
    // We will iterate over this set of AxiomMacrofications to measure both
    // - the 'macrofication' as defined in the paper as the part of an ontology that are not macro
    // definitions
    // - the macro definitions
    Set<AxiomMacrofication> macrofication = system.getMacrofication();
    MacroDefinitions macroDefinitions = system.getMacroDefinitions();

    // we map each class expression to its original size
    // as well as its size in the macrofication
    Map<OWLClassExpression, Integer> origExpression2size = new HashMap<>();
    Map<OWLClassExpression, Integer> minExpression2size = new HashMap<>();

    SyntaxTreeBuilder builder = new SyntaxTreeBuilder();
    SyntaxTreeUtil synUtil = new SyntaxTreeUtil();

    int ontologySize = 0;
    int minimizedOntologySize = 0;
    int macroficationSize = 0;
    int sizeOfDefinitions = 0;
    int changedAxioms = 0;
    int numberOfDefinitions = 0;

    int numberOfExpressions = 0;
    int numberOfChangedExpressions = 0;

    for (AxiomMacrofication mac : macrofication) {

      int origAxiomSize = mac.getAxiomTree().getSize();
      int minAxiomSize = mac.getMinimizationTree().getSize();

      ontologySize += origAxiomSize;
      minimizedOntologySize += minAxiomSize;
      if (origAxiomSize - minAxiomSize > 0) {
        changedAxioms++;
      }

      OWLAxiom origAxiom = mac.getAxiom();
      OWLAxiom minAxiom = mac.getMinimizedAxiom();

      // measure the size of all top level class expression
      // that occur in the ontology and the size-minimal macrofication

      // (1) Subclass Axioms
      if (origAxiom instanceof OWLSubClassOfAxiom) {
        OWLSubClassOfAxiom subclassof = (OWLSubClassOfAxiom) origAxiom;
        OWLClassExpression sub = subclassof.getSubClass();
        OWLClassExpression sup = subclassof.getSuperClass();

        SyntaxTree subTree = builder.build(sub);
        SyntaxTree supTree = builder.build(sup);

        int subTreeSize = subTree.getSize();
        int supTreeSize = supTree.getSize();

        origExpression2size.putIfAbsent(sub, subTreeSize);
        origExpression2size.putIfAbsent(sup, supTreeSize);
      }

      if (minAxiom instanceof OWLSubClassOfAxiom) {
        OWLSubClassOfAxiom subclassof = (OWLSubClassOfAxiom) minAxiom;
        OWLClassExpression sub = subclassof.getSubClass();
        OWLClassExpression sup = subclassof.getSuperClass();

        SyntaxTree subTree = builder.build(sub);
        SyntaxTree supTree = builder.build(sup);

        int subTreeSize = subTree.getSize();
        int supTreeSize = supTree.getSize();

        minExpression2size.putIfAbsent(sub, subTreeSize);
        minExpression2size.putIfAbsent(sup, supTreeSize);
      }

      // (2) EquivalentClasses Axioms
      if (origAxiom instanceof OWLEquivalentClassesAxiom) {
        OWLEquivalentClassesAxiom equivalent = (OWLEquivalentClassesAxiom) origAxiom;
        Set<OWLClassExpression> exprs = equivalent.getClassExpressions();

        for (OWLClassExpression e : exprs) {
          SyntaxTree tree = builder.build(e);
          int eSize = tree.getSize();
          origExpression2size.putIfAbsent(e, eSize);
        }
      }

      if (minAxiom instanceof OWLEquivalentClassesAxiom) {
        OWLEquivalentClassesAxiom equivalent = (OWLEquivalentClassesAxiom) minAxiom;
        Set<OWLClassExpression> exprs = equivalent.getClassExpressions();

        for (OWLClassExpression e : exprs) {
          SyntaxTree tree = builder.build(e);
          int eSize = tree.getSize();
          minExpression2size.putIfAbsent(e, eSize);
        }
      }

      // (3) DisjointClasses Axioms
      if (origAxiom instanceof OWLDisjointClassesAxiom) {
        OWLDisjointClassesAxiom disjoint = (OWLDisjointClassesAxiom) origAxiom;
        Set<OWLClassExpression> exprs = disjoint.getClassExpressions();

        for (OWLClassExpression e : exprs) {
          SyntaxTree tree = builder.build(e);
          int eSize = tree.getSize();
          origExpression2size.putIfAbsent(e, eSize);
        }
      }

      if (minAxiom instanceof OWLDisjointClassesAxiom) {
        OWLDisjointClassesAxiom disjoint = (OWLDisjointClassesAxiom) minAxiom;
        Set<OWLClassExpression> exprs = disjoint.getClassExpressions();

        for (OWLClassExpression e : exprs) {
          SyntaxTree tree = builder.build(e);
          int eSize = tree.getSize();
          minExpression2size.putIfAbsent(e, eSize);
        }
      }

      // (4) DisjointUnion Axioms
      if (origAxiom instanceof OWLDisjointUnionAxiom) {
        OWLDisjointUnionAxiom disjointUnion = (OWLDisjointUnionAxiom) origAxiom;
        OWLClassExpression union = disjointUnion.getOWLClass(); // don't need this - this is a class
        Set<OWLClassExpression> exprs = disjointUnion.getClassExpressions();

        for (OWLClassExpression e : exprs) {
          SyntaxTree tree = builder.build(e);
          int eSize = tree.getSize();
          origExpression2size.putIfAbsent(e, eSize);
        }
      }

      if (minAxiom instanceof OWLDisjointUnionAxiom) {
        OWLDisjointUnionAxiom disjointUnion = (OWLDisjointUnionAxiom) minAxiom;
        OWLClassExpression union = disjointUnion.getOWLClass(); // don't need this - this is a class
        Set<OWLClassExpression> exprs = disjointUnion.getClassExpressions();

        for (OWLClassExpression e : exprs) {
          SyntaxTree tree = builder.build(e);
          int eSize = tree.getSize();
          minExpression2size.putIfAbsent(e, eSize);
        }
      }
    }

    numberOfExpressions = origExpression2size.size();
    int cumulativeExpressionSize = 0;
    for (Integer i : origExpression2size.values()) {
      cumulativeExpressionSize += i;
    }

    int cumulativeMinExpressionSize = 0;
    for (Integer i : minExpression2size.values()) {
      cumulativeMinExpressionSize += i;
    }

    Map<OWLClassExpression, OWLClassExpression> macro2expansion =
        macroDefinitions.getMacro2Expansion();

    numberOfDefinitions = macro2expansion.size();
    for (OWLClassExpression exp : macro2expansion.values()) {
      SyntaxTree tree = builder.build(exp);
      sizeOfDefinitions += tree.getSize(); // size of the expansion
      sizeOfDefinitions += 1; // newly introduced macro symbol
    }
    double practicalReduction =
        (double) (minimizedOntologySize + sizeOfDefinitions) / (double) ontologySize;
    practicalReduction = 1 - practicalReduction;

    String header = "P,O,size(O),size(L_M),size(M),Prac. Red.,# Axioms, #Ch. Axioms, # M";
    String results =
        "3,"
            + ontologyName
            + ","
            + ontologySize
            + ","
            + minimizedOntologySize
            + ","
            + sizeOfDefinitions
            + ","
            + df.format(practicalReduction)
            + ","
            // every axiom in the ontology is represented by an AxiomMacrofication
            + macrofication.size()
            + ","
            + changedAxioms
            + ","
            + changedAxioms
            + ","
            + numberOfDefinitions;

    String outputPath = output + "/" + ontologyName;
    IOHelper.createFolder(outputPath);

    IOHelper.writeAppend(header, outputPath + "/table1.csv");
    IOHelper.writeAppend(results, outputPath + "/table1.csv");
    IOHelper.writeAppend(duration + " (s)", outputPath + "/runningTime.csv");

    // construction of table 2
    Map<OWLClassExpression, Integer> originalExpression2size = new HashMap<>();
    Map<OWLClassExpression, Integer> minimizedExpression2size = new HashMap<>();
    Map<OWLClassExpression, OWLClassExpression> macro2evaluation =
        macroDefinitions.getMacro2Evaluation();
    for (AxiomMacrofication m : macrofication) {
      OWLAxiom axiom = m.getAxiom();
      OWLAxiom minimized = m.getMinimizedAxiom();

      getExpression2size(axiom, originalExpression2size, builder);
      getExpression2size(minimized, minimizedExpression2size, builder);
    }

    Set<OWLClassExpression> size2orLarger = new HashSet<>();
    Set<OWLClassExpression> size5orLarger = new HashSet<>();
    Set<OWLClassExpression> size10orLarger = new HashSet<>();

    Set<OWLClassExpression> size2orLargerReduced = new HashSet<>();
    Set<OWLClassExpression> size5orLargerReduced = new HashSet<>();
    Set<OWLClassExpression> size10orLargerReduced = new HashSet<>();

    double avgReduction2 = 0;
    double avgReduction5 = 0;
    double avgReduction10 = 0;

    int expressionSize = 0;
    int minimizedExpressionSize = 0;

    OWLRewriter rewriter = new OWLRewriter();

    for (OWLClassExpression minimized : minimizedExpression2size.keySet()) {

      int minSize = minimizedExpression2size.get(minimized);
      minimizedExpressionSize += minSize;

      // get term tree for minimized term ...
      SyntaxTree miniTree = builder.build(minimized);
      Set<OWLClass> namedClasses = minimized.getClassesInSignature();
      for (OWLClass c : namedClasses) {
        if (macro2evaluation.containsKey(c)) {
          // ... exhaustively expand macros ...
          rewriter.rewrite(miniTree, c, macro2evaluation.get(c));
        }
      }
      // ... get term from (rewritten) tree
      OWLClassExpression exp = synUtil.getExpressionFromTree(miniTree);

      int origSize = originalExpression2size.get(exp);
      expressionSize += origSize;

      if (origSize >= 2) {
        size2orLarger.add(exp);
        if (origSize > minSize) {
          size2orLargerReduced.add(minimized);
          avgReduction2 += ((double) minSize / (double) origSize);
        }
      }
      if (origSize >= 5) {
        size5orLarger.add(exp);
        if (origSize > minSize) {
          size5orLargerReduced.add(minimized);
          avgReduction5 += ((double) minSize / (double) origSize);
        }
      }
      if (origSize >= 10) {
        size10orLarger.add(exp);
        if (origSize > minSize) {
          size10orLargerReduced.add(minimized);
          avgReduction10 += ((double) minSize / (double) origSize);
        }
      }
    }

    avgReduction2 = avgReduction2 / (double) size2orLargerReduced.size();
    avgReduction5 = avgReduction5 / (double) size5orLargerReduced.size();
    avgReduction10 = avgReduction10 / (double) size10orLargerReduced.size();

    double reductionProportion2 =
        (double) size2orLargerReduced.size() / (double) size2orLarger.size();
    double reductionProportion5 =
        (double) size5orLargerReduced.size() / (double) size5orLarger.size();
    double reductionProportion10 =
        (double) size10orLargerReduced.size() / (double) size10orLarger.size();

    double avgExpressionSize = (double) expressionSize / (double) originalExpression2size.size();
    double redBy2 = (double) size2orLargerReduced.size() / (double) size2orLarger.size();
    double redBy5 = (double) size5orLargerReduced.size() / (double) size5orLarger.size();
    double redBy10 = (double) size10orLargerReduced.size() / (double) size10orLarger.size();

    header = "P,O,#Expr.,S_O,Red. E_2,Red.By E_2,Red. E_5,Red.By E_5,Red.E_10,Red.By E_10";
    results =
        "3,"
            + ontologyName
            + ","
            + originalExpression2size.size()
            + ","
            + df.format(avgExpressionSize)
            + ","
            + df.format(redBy2)
            + ","
            + df.format(avgReduction2)
            + ","
            + df.format(redBy5)
            + ","
            + df.format(avgReduction5)
            + ","
            + df.format(redBy10)
            + ","
            + df.format(avgReduction10);

    IOHelper.writeAppend(header, outputPath + "/table2.csv");
    IOHelper.writeAppend(results, outputPath + "/table2.csv");
  }

  public static void getExpression2size(
      OWLAxiom axiom, Map<OWLClassExpression, Integer> expression2size, SyntaxTreeBuilder builder) {

    if (axiom instanceof OWLSubClassOfAxiom) {
      OWLSubClassOfAxiom subclassof = (OWLSubClassOfAxiom) axiom;
      OWLClassExpression sub = subclassof.getSubClass();
      OWLClassExpression sup = subclassof.getSuperClass();

      SyntaxTree subTree = builder.build(sub);
      SyntaxTree supTree = builder.build(sup);

      int subTreeSize = subTree.getSize();
      int supTreeSize = supTree.getSize();

      expression2size.putIfAbsent(sub, subTreeSize);
      expression2size.putIfAbsent(sup, supTreeSize);
    }

    if (axiom instanceof OWLEquivalentClassesAxiom) {
      OWLEquivalentClassesAxiom equivalent = (OWLEquivalentClassesAxiom) axiom;
      Set<OWLClassExpression> exprs = equivalent.getClassExpressions();

      for (OWLClassExpression e : exprs) {
        SyntaxTree tree = builder.build(e);
        int eSize = tree.getSize();
        expression2size.putIfAbsent(e, eSize);
      }
    }

    if (axiom instanceof OWLDisjointClassesAxiom) {
      OWLDisjointClassesAxiom disjoint = (OWLDisjointClassesAxiom) axiom;
      Set<OWLClassExpression> exprs = disjoint.getClassExpressions();

      for (OWLClassExpression e : exprs) {
        SyntaxTree tree = builder.build(e);
        int eSize = tree.getSize();
        expression2size.putIfAbsent(e, eSize);
      }
    }

    if (axiom instanceof OWLDisjointUnionAxiom) {
      OWLDisjointUnionAxiom disjointUnion = (OWLDisjointUnionAxiom) axiom;
      OWLClassExpression union = disjointUnion.getOWLClass(); // don't need this - this is a class
      Set<OWLClassExpression> exprs = disjointUnion.getClassExpressions();

      for (OWLClassExpression e : exprs) {
        SyntaxTree tree = builder.build(e);
        int eSize = tree.getSize();
        expression2size.putIfAbsent(e, eSize);
      }
    }
  }
}
