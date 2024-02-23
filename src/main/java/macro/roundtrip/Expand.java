package macro.roundtrip;

import java.io.*;
import java.nio.file.*;
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

/** Macrofies an ontology */
public class Expand {

  public static void main(String[] args) throws Exception {

    String macrofication = args[0];
    String macroDefinitions = args[1];
    String output = args[2];

    run(macrofication, macroDefinitions, output);
  }

  public static void run(String ontFilePath, String definitionPath, String output)
      throws Exception {

    File ontFile = new File(ontFilePath);
    OntologyLoader ontLoader = new OntologyLoader(ontFile, true);
    OWLOntology macrofication = ontLoader.getOntology();

    File defFile = new File(definitionPath);
    OntologyLoader ontLoader2 = new OntologyLoader(defFile, true);
    OWLOntology definitions = ontLoader2.getOntology();

    String outputPath = output;
    // String ontologyName = Paths.get(ontFilePath).getFileName().toString();
    // String outputPath = output + "/" + ontologyName;
    // IOHelper.createFolder(outputPath);

    // get definitions
    ExplicitDefinitionFinder finder = new ExplicitDefinitionFinder(definitions);
    Map<OWLClass, Set<OWLClassExpression>> macro2expressions = finder.getAbbreviation2expressions();
    Map<OWLClass, OWLClassExpression> macro2expansion = new HashMap<>();

    for (Map.Entry<OWLClass, Set<OWLClassExpression>> set : macro2expressions.entrySet()) {
      OWLClass macro = set.getKey();
      for (OWLClassExpression expansion : set.getValue()) {
        // there should only be one because macros are functional
        macro2expansion.put(macro, expansion);
      }
    }
    // expand
    SyntaxTreeBuilder builder = new SyntaxTreeBuilder();
    OWLRewriter rewriter = new OWLRewriter();
    Set<OWLAxiom> originalOntology = new HashSet<>();
    for (OWLAxiom a : macrofication.getLogicalAxioms()) {

      SyntaxTree round = builder.build(a);

      Set<OWLClass> namedClasses = a.getClassesInSignature();
      for (OWLClass c : namedClasses) {
        if (macro2expansion.containsKey(c)) {
          rewriter.rewrite(round, c, macro2expansion.get(c));
        }
      }
      AxiomNode node = (AxiomNode) round.getRoot();
      OWLAxiom roundAxiom = node.getAxiom();
      originalOntology.add(roundAxiom);
    }
    // save
    OntologySaver.saveAxioms(originalOntology, outputPath + "/expandedOntology.owl");
  }
}
