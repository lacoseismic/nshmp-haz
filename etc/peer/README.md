# PEER Test Cases

The PEER PSHA verification project developed a number of source models for the purpose of
examining the sensitivity of seismic hazard to different PSHA implementations (codes).
These test cases are used as end-to-end unit tests in `nshmp-haz`.

For more information, including source model specifications, result tables, and summary report,
please see the [PEER Probabilistic Seismic Hazard Analysis Code Verification Report -
2018/03](https://peer.berkeley.edu/sites/default/files/2018_03_hale_final_8.13.18.pdf).

## Implementation Notes

- Most test cases have a version that matches the target results, but that run slowly, as
  well as a'fast' implementations with coarser discretizations.
- Set1, Cases 8a, b, and c implementations are the same as the Set1, Case2 'fast' versions.
- Set2, Case1 (disagreggation) was skipped; implemented in OpenSHA.
- Set3 tests are not yet implemented.
