#!/bin/bash

ontology="./pizza.owl" #use another ontology (and inspect the files)
output="./example"

if [ ! -d "$output" ]; then

  echo "Creating $output directory"
  mkdir $output
  
  echo "Running round trip check for $ontology"
  # compute statistics for problems 1-3 (as reported in the paper)
  java -jar program.jar solve 1 $ontology $output
  java -jar program.jar solve 2 $ontology $output
  java -jar program.jar solve 3 $ontology $output
  
  # round trip test
  java -jar program.jar macrofy $ontology $output
  java -jar program.jar fixedpoint ./example/macroExpansions.owl $output
  java -jar program.jar compare ./example/macroEvaluations.owl ./example/fixedpointExpansions.owl
  java -jar program.jar expand ./example/macrofication.owl ./example/fixedpointExpansions.owl $output
  java -jar program.jar compare $ontology ./example/expandedOntology.owl

else
  echo "Directory $output already exists"
fi
