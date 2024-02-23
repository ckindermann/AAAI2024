package macro.ont;

import java.io.File;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.util.AutoIRIMapper;

public class OntologyLoader {

  private OWLOntology ontology;
  private OWLOntologyManager manager;

  private double loadingTime;

  public OntologyLoader(File file, boolean includeImports) {
    this.loadingTime = 0.0;
    if (includeImports) {
      loadOntologyWithImports(file);
    } else {
      loadOntology(file);
    }
  }

  private void loadOntology(File ontFile) {
    manager = OWLManager.createOWLOntologyManager();
    ontology = null;
    try {
      long starTime = System.nanoTime();
      ontology = manager.loadOntologyFromOntologyDocument(ontFile);
      long endTime = System.nanoTime();
      double duration = (endTime - starTime) / 1000000000.0;
      this.loadingTime = duration;
    } catch (OWLOntologyCreationException e) {
      e.printStackTrace();
    }
  }

  private void loadOntologyWithImports(File ontFile) {
    manager = OWLManager.createOWLOntologyManager();
    AutoIRIMapper mapper = new AutoIRIMapper(ontFile.getParentFile(), true);
    OWLOntologyManager tempManager = OWLManager.createOWLOntologyManager();
    tempManager.addIRIMapper(mapper);
    try {

      long starTime = System.nanoTime();
      OWLOntology o = tempManager.loadOntologyFromOntologyDocument(ontFile);
      long endTime = System.nanoTime();
      double duration = (endTime - starTime) / 1000000000.0;
      this.loadingTime = duration;
      // include all imports
      ontology = manager.createOntology(o.getAxioms(Imports.INCLUDED));
    } catch (OWLOntologyCreationException e) {
      e.printStackTrace();
    }
  }

  public double getLoadingTime() {
    return this.loadingTime;
  }

  public OWLOntology getOntology() {
    return ontology;
  }

  public OWLOntologyManager getManager() {
    return manager;
  }
}
