mvn compile
mvn exec:java -Dexec.mainClass=test.CompareStateoftheArt_AllEventTimex -Dexec.args="caevo" > logs/Table8_line4_caevo_on_TBDense.txt
mvn exec:java -Dexec.mainClass=test.CompareStateoftheArt_AllEventTimex -Dexec.args="emnlp" > logs/Table8_line5_emnlp_on_TBDense.txt
mvn exec:java -Dexec.mainClass=test.CompareStateoftheArt_AllEventTimex -Dexec.args="naacl" > logs/Table8_line6_proposed_on_TBDense.txt # this is the proposed