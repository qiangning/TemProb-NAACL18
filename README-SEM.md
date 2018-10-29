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

# Reproduce \*SEM'18 Results
All the following commands should be run from the root dir of the project, i.e., `TemProb-NAACL18/`.

```
git clone git@github.com:qiangning/TemProb-NAACL18.git
cd TemProb-NAACL18
tar xf data/TemProb.txt.tar.gz -C data
sh scripts/mvn_install.sh
mvn compile
```

If no error messages pop up, you're can move forward by
```
sh scripts/create_folders.sh
sh scripts/RunThis_All_PartialGraphc.sh > RunThis_All_PartialGraph_log.txt
```

If you met with errors while evaluating the temporal awareness scores of each system, probably it's due to python 2 vs 3 issues. Please change corresponding `python` commands to be `python2`.

# Where do we find the logs?
Standard metrics (prec, rec, and F1):
- Line 1: `logs/PartialPaper/PurelyOnTBDense.txt` (note that the numbers here will be different to those reported in the paper due to later changes)
- Line 5: `logs/PartialPaper/TBDense+TBAQ_Nonvague.txt`
- Line 6: `logs/PartialPaper/bootstrap/TBAQ_local_asU_iter2.txt`
- Line 7: `logs/PartialPaper/bootstrap/TBAQ_global_asU_iter2.txt`
- Line 8: `logs/PartialPaper/bootstrap/TBAQ_local_asP_iter2.txt`
- Line 9: `logs/PartialPaper/bootstrap/TBAQ_global_asP_iter2.txt`

Temporal awareness scores:
- Line 1: `logs/PartialPaper/Awareness/PurelyOnTBDense_aware.txt`
- Line 5: `logs/PartialPaper/Awareness/TBDense+TBAQ_Nonvague_aware.txt`
- Line 6: `logs/PartialPaper/Awareness/bootstrap/TBAQ_local_asU_iter1_aware.txt`
- Line 7: `logs/PartialPaper/Awareness/bootstrap/TBAQ_global_asU_iter1_aware.txt`
- Line 8: `logs/PartialPaper/Awareness/bootstrap/TBAQ_local_asP_iter1_aware.txt`
- Line 9: `logs/PartialPaper/Awareness/bootstrap/TBAQ_global_asP_iter1_aware.txt`

Note that sometimes the awareness evaluations are not finished by itself (due to an unknown instability in the awareness evaluation tools provided by TempEval3). You can go to the log of the standard metrics and locate a line starting with `sh scripts/evaluate_general_dir.sh` (usually at the bottom of each file). Note that this should be run from `TemProb-NAACL18/`.

**I have also put the original logs I generated into `logs_refs/PartialGraph/` for your reference.**

# Where do we find the system outputs?
- Line 1: `output/PartialPaper/PurelyOnTBDense/`
- Line 5: `output/PartialPaper/TBDense+TBAQ_Nonvague/`

# Citation
Please kindly cite the following paper: *Qiang Ning, Zhongzhi Yu, Chuchu Fan, and Dan Roth. "Exploiting Partially Annotated Data in Temporal Relation Extraction (short paper)." \*SEM, 2018.* [[pdf](http://cogcomp.org/papers/NingYuFaRo18.pdf)] [[poster](http://qning2.web.engr.illinois.edu/papers/NingYuFaRo18-poster.pdf)] [[slides](http://cogcomp.org/files/presentations/103_Qiang_Ning.pdf)]

```
@inproceedings{NingYuFaRo18,
    author = {Qiang Ning and Zhongzhi Yu and Chuchu Fan and Dan Roth},
    title = {Exploiting Partially Annotated Data for Temporal Relation Extraction},
    booktitle = {The Joint Conference on Lexical and Computational Semantics (*SEM)},
    month = {6},
    year = {2018},
    publisher = {Association for Computational Linguistics},
    acceptance = {42\%},
    url = "http://cogcomp.org/papers/NingYuFaRo18.pdf",
    funding = {AI2, IBM-ILLINOIS C3SR, DEFT, ARL},
}
```
