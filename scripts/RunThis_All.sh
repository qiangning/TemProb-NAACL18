echo "Generating Table 3..."
echo "Results can be found in logs/eval_corpus_prior/th%f_sentsplit.txt"
sh scripts/eval_corpus_prior_batch_table3.sh

echo ""
echo "Generating Table 4..."
echo "Results can be found in logs/eval_corpus_prior/CausalDirection.txt"
sh scripts/eval_prior_causality_table4.sh

echo ""
echo "Generating Table 5..."
echo "Results can be found in logs/Table5_line%d.txt"
mvn exec:java -Dexec.mainClass=test.global_ee_test -Dexec.args="table5_line1" > logs/Table5_line1.txt
mvn exec:java -Dexec.mainClass=test.global_ee_test -Dexec.args="table5_line2" > logs/Table5_line2.txt
mvn exec:java -Dexec.mainClass=test.global_ee_test -Dexec.args="table5_line3" > logs/Table5_line3.txt

echo ""
echo "Generating Table 6..."
echo "Results can be found in logs/Table6_line%d.txt"
mvn exec:java -Dexec.mainClass=test.global_ee_test -Dexec.args="table6_line1" > logs/Table6_line1_baseline_noGoldProps.txt
mvn exec:java -Dexec.mainClass=test.global_ee_test -Dexec.args="table6_line2" > logs/Table6_line2_proposed_noGoldProps.txt
mvn exec:java -Dexec.mainClass=test.global_ee_test -Dexec.args="table6_line3" > logs/Table6_line3_proposed_noGoldProps.txt

echo ""
echo "Generating Table 8..."
echo "Results can be found in logs/Table8_line3_proposed_on_partialTBDense_detail.txt"
mvn exec:java -Dexec.mainClass=test.global_ee_test -Dexec.args="table8_line3" > logs/Table8_line3_proposed_on_partialTBDense_detail.txt


echo ""
echo "Evaluating Table 8..."
echo "Results can be found in logs/Table8_line%d.txt"
sh scripts/eval_partialTBDense_table8.sh
sh scripts/eval_FullTBDense_table8.sh