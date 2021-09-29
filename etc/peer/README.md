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
- Set1, Cases 5, 6, 7, 10, 11 have incremental MFds reflecting what should be built from
  parameters. These should be refactored once a Youngs and Coppersmith (!985) MFD is
  implemented.
- Set1 Cases 10 and 11 not enabled yet; requires dynamic creation of zone point sources. The
  _nshmp-haz-v1_ implementation distributed a predefined MFD over a dynamically created grid
  in lat-lon space that was close enough to the uniform km-based grid prescribed by the test.
- Set1, Cases 8a, b, and c implementations are the same as the Set1, Case2 'fast' versions.
- Set2, Case1 (disagreggation) was skipped; implemented in OpenSHA.
- Set3 tests are not yet implemented.
- Consider symlinks for features, mfd-map, and other redundant files
