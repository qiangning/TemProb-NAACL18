#!/usr/bin/env bash
echo "Generating Line 1 in Table 3..."
echo "Results can be found in logs/PartialPaper/PurelyOnTBDense.txt"
mvn exec:java -Dexec.mainClass=PartialGraph.RunThis -Dexec.args="PurelyOnTBDense false" > logs/PartialPaper/PurelyOnTBDense.txt

echo "Generating Line 5 in Table 3..."
echo "Results can be found in logs/PartialPaper/TBDense+TBAQ_Nonvague.txt"
mvn exec:java -Dexec.mainClass=PartialGraph.RunThis -Dexec.args="TBDense+TBAQ_Nonvague false" > logs/PartialPaper/TBDense+TBAQ_Nonvague.txt

echo "Generating Line 6 in Table 3..."
echo "Results can be found in logs/PartialPaper/bootstrap/TBAQ_local_asU_iter2.txt"
mvn exec:java -Dexec.mainClass=PartialGraph.RunThis_bootstrap -Dexec.args="TBAQ local 0.15 2 false false" > logs/PartialPaper/bootstrap/TBAQ_local_asU_iter2.txt

echo "Generating Line 7 in Table 3..."
echo "Results can be found in logs/PartialPaper/bootstrap/TBAQ_global_asU_iter2.txt"
mvn exec:java -Dexec.mainClass=PartialGraph.RunThis_bootstrap -Dexec.args="TBAQ global 0.2 2 false false" > logs/PartialPaper/bootstrap/TBAQ_global_asU_iter2.txt

echo "Generating Line 8 in Table 3..."
echo "Results can be found in logs/PartialPaper/bootstrap/TBAQ_local_asP_iter2.txt"
mvn exec:java -Dexec.mainClass=PartialGraph.RunThis_bootstrap -Dexec.args="TBAQ local 0.15 2 true false" > logs/PartialPaper/bootstrap/TBAQ_local_asP_iter2.txt

echo "Generating Line 9 in Table 3..."
echo "Results can be found in logs/PartialPaper/bootstrap/TBAQ_global_asP_iter2.txt"
mvn exec:java -Dexec.mainClass=PartialGraph.RunThis_bootstrap -Dexec.args="TBAQ global 0.2 2 true false" > logs/PartialPaper/bootstrap/TBAQ_global_asP_iter2.txt
