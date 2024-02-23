# AAAI 2024 - Minimal Macro-Based Rewritings of Formal Languages: Theory and Applications in Ontology Engineering (and beyond)

This repository contains the implementation used to produce the empirical results reported in our paper.
You may use the source code and executables for your own research.

Don't hesitate to reach out if you think I can help (preferably by email).

To avoid any confusion:

1. This implementation is an experimental prototype.
2. Its purpose is to provide a proof of concept.
3. Everything here is CRAPLed (see [CRAPL-LICENSE.txt](https://github.com/ckindermann/AAAI2024/blob/main/CRAPL-LICENSE.txt)).

## Basic Usage

Compute a size-minimal encoding of an ontology w.r.t. problem 1-3:

`java -jar program.jar solve problem ontology output`

The input of the command `solve` expects
- a number betwen `1` and `3` (specifying the `problem` to be solved),
- a path to an OWL `ontology`, and
- a path to an `output` destination

## Output Description

All output is written to a new folder `output/ontology` where `output` is the given output destination and `ontology` is the input ontology.
The folder contains the following two to three files:

1. for Problem 1,2,3: a file named `table1.csv` (containing a header and a data row following the same format of Table 1 in the paper) 
2. for Problem 3: a file named `table2.csv` (containing a header and a data row following the same format of Table 2 in the appendix)
3. for Problem 1,2,3: a file named `runningTime.csv` (containing the processing time of the minimization process -- please note that this time does not include the loading time of the ontology or any other operations that are performed as part of the experiment.)

## Round Trip Testing and Data Inspection

We provide support for round trip tests w.r.t. Problem 3.
A 'round trip' is a transformation that

1. takes an ontology as input,
2. macrofies it into a size-minimal encoding, and then
3. translates this size-minimal encoding back to an (output) ontology. 

The round trip is successful if the input ontology is equal to the output ontology.

We provide the following commands to perform round trip tests:

- `macrofy ontology output`: takes an OWL ontology and transforms it into a size-minimal encoding w.r.t. Problem 3 (without unary symbols) with the following outputs:
    - `macrofication.owl` 
    - `macroExpansion.owl` (minimized macro definitions)
    - `macroEvaluation.owl` (fixed-point macro definitions)

- `fixedpoint ontology output`: takes an OWL ontology consisting of (minimized) macro definitions and transforms these into fixed-poind expansions with the following output: 
    - `fixedpointExpansions.owl` (fixed-point macro definitions)

- `expand macrofication definitions output`: takes a `macrofication.owl` OWL ontology and `fixedpointExpansions.owl` definitions as input, and expands these in `macrofication.owl` with the following output:
    - `expandedOntology.owl` (the output ontology)

- `compare ontology1 ontology2`: takes two ontologies and checks whether they contain the same class expression axioms (disregarding OWL annotations and excluding axioms with `OWLComplementOf`). If so, return `true`, otherwise `false`

An example of such a round trip check is provided for the `pizza.owl` ontology in the `example.sh` script.

## Replicability

To replicate the results presented in the paper, proceed as follows:

1. Acquire ontologies indexed at [BioPortal](https://bioportal.bioontology.org/)
2. Run the provided executable using the command `solve`
3. Analyse the data for tables 1 and 2 (and check the running time measured in seconds)
