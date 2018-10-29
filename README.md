# Prerequisites
- This package was tested on Ubuntu Mate 16.04.
- Have [maven](https://maven.apache.org/install.html) installed in your system
- Have [Gurobi6.5.2](http://www.gurobi.com/downloads/gurobi-optimizer) installed in your system and have the environment variables `GUROBI_HOME` and `GRB_LICENSE_FILE` setup in your path, as required by Gurobi. Gurobi would typically require adding the following lines to your .bashrc or .bash_profile, but please refer to Gurobi's installment instructions for details.
  ```
  export GUROBI_HOME=/opt/gurobi652/linux64
  export PATH=$PATH:$GUROBI_HOME/bin
  export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$GUROBI_HOME/lib
  export GRB_LICENSE_FILE=$GUROBI_HOME/gurobi.lic
  ```
  Please contact the author (qning2@illinois.edu) if you cannot find the official package for version 6.5.2. **Note that if you only need the system output files, you can move forward without gurobi.**

# Reproduce NAACL'18 Results
All the following commands should be run from the root dir of the project, i.e., `TemProb-NAACL18/`.

```
git clone git@github.com:qiangning/TemProb-NAACL18.git
cd TemProb-NAACL18
sh scripts/mvn_install.sh
mvn compile
```

If no error messages pop up, you're can move forward by
```
mkdir logs
mkdir logs/Awareness
mkdir logs/Awareness/CompareStateoftheArt_AllEventTimex
mkdir logs/Awareness/CompareStateoftheArt_PartialTBDense
mkdir logs/eval_corpus_prior
sh scripts/RunThis_All.sh > RunThis_All_log.txt
```
Note: Since Github is limiting the bandwidth for large files (our `data/TemProb.txt` is a large file), it's very likely that you will see an error saying that `TemProb.txt` fails to be downloaded. In that case, please go to [here](http://cogcomp.org/page/publication_view/830) and find the backup link to download it.

Again, if no errors are encountered, you should now have all the tables reported in the paper. Take a look at `scripts/RunThis_All.sh` and it should be rather easy to understand. For example,
- Table 3: `sh scripts/eval_corpus_prior_batch_table3.sh`
- Table 4: `sh scripts/eval_prior_causality_table4.sh`
- Table 5, line 1: mvn exec:java -Dexec.mainClass=test.global_ee_test -Dexec.args="table5_line1" > logs/Table5_line1.txt
- ...

**One exception is Table 7**, which was not generated automatically, but we have included the numbers in `logs_refs/DONOTDELETE_Table7.txt`.
**Another exception** is the description right above Table 5 (these numbers couldn't fit into Table 5, so we had to put them in the text). These numbers can be found in `logs_refs/DONOTDELETE_Table5_improvement`.

If you met with errors while evaluating the temporal awareness scores of each system, probably it's due to python 2 vs 3 issues. Please change corresponding `python` commands to be `python2`.

# Where do we find the logs?
Standard metrics (prec, rec, and F1):
- Table 3: `logs/eval_corpus_prior/th0.x_sentsplit.txt` where `x=5~9`.
- Table 4: `logs/eval_corpus_prior/CausalDirection.txt`.
- Table 5: `logs/Table5_line*.txt` where `*=1~3`.
- Table 6: `logs/Table6_line*.txt` where `*=1~3`.
- Table 7: `logs_refs/DONOTDELETE_Table7.txt`.
- Table 8: `logs/Table8_line*.txt` where `*=1~6`. In addition, `logs/Table8_line3_proposed_on_partialTBDense_detail.txt` contains more detailed information of System 3 in Table 6: there are performances of each of the 9 documents in the test split of TBDense, and both *local* and *global* performances. Please refer to the Sec. 2 of the paper for descriptions of *local* and *global*. However, note that the reported values in Table 6 are from *global*.

Temporal awareness scores:
- Table 6: `logs/Awareness/table6_line*.txt` where `*=1~3`.
- Table 8 (on partial TBDense, i.e., the top part of the table): `logs/Awareness/CompareStateoftheArt_PartialTBDense`.
- Table 8 (on full TBDense, i.e., the bottom part of the table): `logs/Awareness/CompareStateoftheArt_AllEventTimex`.

Note that sometimes the awareness evaluations are not finished by itself (due to an unknown instability in the awareness evaluation tools provided by TempEval3). You can go to the log of the standard metrics and locate a line starting with `sh scripts/evaluate_general_dir.sh` (usually at the bottom of each file). For example, if you see `logs/CompareStateoftheArt_PartialTBDense/naacl.txt` is incomplete. Since that corresponds to the 3rd line of Table 8, go to `logs/Table8_line3_proposed_on_partialTBDense.txt` and you will see a line I intentionally created for this situation:
```
sh scripts/evaluate_general_dir.sh output/Awareness/gold output/Awareness/CompareStateoftheArt_PartialTBDense/naacl naacl Awareness/CompareStateoftheArt_PartialTBDense
```
Run this from `TemProb-NAACL18/` and you will see that `logs/CompareStateoftheArt_PartialTBDense/naacl.txt` is updated and complete now (may take a few seconds to complete).

**I have also put the original logs I generated into `logs_refs/` for your reference.**

# Where do we find the system outputs?
- Table 6: `output/Awareness/global/table6_line*/` where `*=1~3`.
- Table 8 (on partial TBDense):
    - Line 1: `output/Awareness/CompareStateoftheArt_PartialTBDense/caevo/`
    - Line 2: `output/Awareness/CompareStateoftheArt_PartialTBDense/emnlp/`
    - Line 3: `output/Awareness/CompareStateoftheArt_PartialTBDense/naacl/`, or `output/Awareness/global/table8_line3/`.
- Table 8 (on full TBDense):
    - Line 4: `output/CAEVO_on_TBDense`
    - Line 5: `output/EMNLP_on_TBDense`
    - Line 6: `output/EMNLPAugmentedByNAACL_on_TBDense`
    - Note these three outputs are not generated by this package. Specifically, Line 4 comes from [here](https://github.com/qiangning/StructTempRel-EMNLP17/tree/master/output/Chambers/caveo). Line 5 comes from [here](https://github.com/qiangning/StructTempRel-EMNLP17/tree/master/output/Chambers/codl/TD-Test/0.3_1.4_1). And Line 6 comes from [here](https://github.com/qiangning/StructTempRel-EMNLP17/tree/kbcom/output/Chambers/codl/TD-Test/0.3_1.4_1_kbcom_newfeat).

# Citation
Please kindly cite the following paper: *Qiang Ning, Hao Wu, Haoruo Peng, Dan Roth, "Improving Temporal Relation Extraction with a Globally Acquired Statistical Resource", NAACL 2017* ([pdf](http://cogcomp.org/papers/NingWuPeRo18.pdf))

```
@inproceedings{NingWuPeRo18,
    author = {Qiang Ning and Hao Wu and Haoruo Peng and Dan Roth},
    title = {Improving Temporal Relation Extraction with a Globally Acquired Statistical Resource},
    booktitle = {NAACL},
    month = {6},
    year = {2018},
    publisher = {Association for Computational Linguistics},
    url = "http://cogcomp.org/papers/NingWuPeRo18.pdf",
}
```
