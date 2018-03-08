mvn compile
mvn exec:java -Dexec.mainClass=test.CompareStateoftheArt_SRLeventonly -Dexec.args="caevo" > logs/Table8_line1_caevo_on_partialTBDense.txt
mvn exec:java -Dexec.mainClass=test.CompareStateoftheArt_SRLeventonly -Dexec.args="emnlp" > logs/Table8_line2_emnlp_on_partialTBDense.txt
mvn exec:java -Dexec.mainClass=test.CompareStateoftheArt_SRLeventonly -Dexec.args="naacl" > logs/Table8_line3_proposed_on_partialTBDense.txt # this is the proposed