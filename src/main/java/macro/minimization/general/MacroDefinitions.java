package macro.minimization.general;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;
import macro.abbr.*;
import macro.abbr.explicit.*;
import macro.ont.*;
import macro.parser.*;
import macro.structure.*;
import macro.structure.nodes.*;
import macro.term.*;
import macro.util.*;
import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.traverse.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.*;

public class MacroDefinitions {
  private OWLOntologyManager manager;
  private OWLDataFactory factory;
  private IRI IOR;
  private int nextMacroID;
  private Map<OWLClassExpression, OWLClassExpression> macro2evaluation;
  private Map<OWLClassExpression, OWLClassExpression> evaluation2macro;

  private Map<OWLClassExpression, OWLClassExpression> macro2expansion;
  private Map<OWLClassExpression, OWLClassExpression> expansion2macro;
  private SyntaxTreeUtil synTreeUtil;

  public MacroDefinitions() {
    this.manager = OWLManager.createOWLOntologyManager();
    this.factory = manager.getOWLDataFactory();
    this.IOR = IRI.create("http://owl.generated.macro");
    this.nextMacroID = 0;

    this.synTreeUtil = new SyntaxTreeUtil();

    this.macro2evaluation = new HashMap<>();
    this.evaluation2macro = new HashMap<>();
    this.macro2expansion = new HashMap<>();
    this.expansion2macro = new HashMap<>();
  }

  public void addMacroDefinition(OWLClassExpression exp) {
    this.nextMacroID++;
    OWLClass macro = factory.getOWLClass(IOR + "#m_" + this.nextMacroID);
    macro2evaluation.put(macro, exp);
    evaluation2macro.put(exp, macro);
  }

  public Map<OWLClassExpression, OWLClassExpression> getEvaluation2macro() {
    return this.evaluation2macro;
  }

  public Map<OWLClassExpression, OWLClassExpression> getMacro2Expansion() {
    return this.macro2expansion;
  }

  public Map<OWLClassExpression, OWLClassExpression> getMacro2Evaluation() {
    return this.macro2evaluation;
  }

  public void computeMinimalMacroDefinitions() {
    // get macro 2 evaluation <- get subterms <- replace things
    SyntaxTreeBuilder builder = new SyntaxTreeBuilder();

    for (Map.Entry<OWLClassExpression, OWLClassExpression> set : macro2evaluation.entrySet()) {
      OWLClassExpression macro = set.getKey();
      OWLClassExpression evaluation = set.getValue();

      SyntaxTree minimized = builder.build(evaluation);

      TreeMap<Integer, Set<OWLClassExpression>> size2subterms =
          this.synTreeUtil.size2subExpressions(evaluation);

      for (Map.Entry<Integer, Set<OWLClassExpression>> entry : size2subterms.entrySet()) {
        int size = entry.getKey();
        if (size > 2) { // skip atomic symbols and binary things
          Set<OWLClassExpression> independent = entry.getValue();
          // macrofy things
          for (OWLClassExpression i : independent) {
            if (evaluation2macro.containsKey(i)) {
              OWLRewriter rewriter = new OWLRewriter(minimized, i, evaluation2macro.get(i));
            }
          }
        }
      }
      OWLClassExpression minExpression = this.synTreeUtil.getExpressionFromTree(minimized);
      this.macro2expansion.put(macro, minExpression);
      this.expansion2macro.put(minExpression, macro);
    }
  }
}
