package org.opensha2.internal;

import org.opensha2.geo.Location;
import org.opensha2.util.NamedLocation;

/**
 * Cybershake UGMS sites.
 *
 * @author Peter Powers
 */
@SuppressWarnings("javadoc")
public enum CybershakeSite implements NamedLocation {

  /*
   * Notes:
   * 
   * CVM4 derived vs30 values have been rounded to the nearest whole value. Note
   * that there are scattered inconsistencies between the Wills and CVM vs30
   * values (e.g. LAPD) Sites beginning with lowercase s have been capitalized.
   * 
   * CVM vs30 values for [S603, S684], [S474, S476], [S644, S646], and [S688,
   * S689] are identical; waiting on reponse from Callahan or Milner.
   * 
   * Site S603 is repeated (and commented out) in Inland Empire group.
   */

  /* UGMS Original 14 */
  CCP(-118.41302, 34.054886, 3876, 387.0, 362.0, 0.39, 2.96),
  COO(-118.21639, 33.89604, 3877, 280.0, 267.0, 0.73, 4.28),
  LADT(-118.25713, 34.05204, 3870, 390.0, 359.0, 0.31, 2.08),
  LAPD(-118.125, 34.557, 3875, 515.0, 2571.0, 0.0, 0.0),
  P22(-118.56609, 34.18277, 3879, 280.0, 303.0, 0.22, 2.27),
  PAS(-118.17119, 34.148426, 4266, 748.0, 821.0, 0.01, 0.31),
  S429(-118.23333, 33.80858, 3882, 280.0, 332.0, 0.71, 2.83),
  S603(-117.53735, 34.10275, 3883, 354.0, 364.0, 0.19, 0.43),
  S684(-117.40266, 33.93515, 3884, 387.0, 364.0, 0.15, 0.31),
  S758(-117.53532, 33.37562, 3885, 390.0, 2414.0, 0.0, 1.19),
  SBSM(-117.29201, 34.064986, 3880, 280.0, 355.0, 0.33, 1.77),
  SMCA(-118.48939, 34.00909, 3869, 387.0, 387.0, 0.59, 2.47),
  STNI(-118.17881, 33.93088, 3873, 280.0, 269.0, 0.88, 5.57),
  WNGC(-118.0653, 34.041824, 3861, 280.0, 296.0, 0.51, 2.44),

  /* LA Basin West Array */
  S344(-118.39576, 33.93587, 4067, 387.0, 366.0, 0.69, 2.49),
  S345(-118.36942, 33.97525, 4068, 387.0, 358.0, 0.58, 3.01),
  S346(-118.34305, 34.01462, 4069, 280.0, 285.0, 0.65, 4.16),
  S347(-118.31666, 34.05399, 4217, 280.0, 366.0, 0.31, 2.05),
  S348(-118.29024, 34.09336, 4218, 387.0, 506.0, 0.31, 1.93),
  S349(-118.2638, 34.13272, 4219, 280.0, 412.0, 0.15, 0.49),
  S351(-118.21085, 34.21142, 4220, 349.0, 379.0, 0.09, 0.31),
  S353(-118.1578, 34.2901, 4221, 748.0, 703.0, 0.02, 0.31),

  /* LA Basin Central Array */
  S383(-118.38029, 33.77382, 4083, 390.0, 2195.0, 0.0, 1.46),
  S385(-118.32766, 33.85257, 4084, 387.0, 332.0, 0.76, 3.14),
  S387(-118.27493, 33.9313, 4085, 387.0, 341.0, 0.66, 3.97),
  S388(-118.24853, 33.97065, 4086, 280.0, 287.0, 0.83, 5.61),
  S389(-118.22211, 34.01, 4087, 280.0, 295.0, 0.6, 3.98),
  S391(-118.16919, 34.08869, 4088, 390.0, 383.0, 0.31, 2.11),
  S393(-118.11618, 34.16735, 4237, 387.0, 374.0, 0.15, 1.27),
  S395(-118.06307, 34.24599, 4090, 748.0, 2537.92, 0.0, 0.0),

  /* LA Basin East Array */
  S470(-118.1391, 33.76452, 4111, 387.0, 353.22, 0.64, 2.93),
  S472(-118.08625, 33.84317, 4112, 280.0, 268.0, 0.82, 4.79),
  S474(-118.03331, 33.9218, 4113, 387.0, 351.0, 0.63, 3.36),
  S476(-117.98027, 34.00041, 4114, 390.0, 351.0, 0.31, 1.96),
  S478(-117.92713, 34.07899, 4115, 280.0, 325.0, 0.26, 2.65),
  S480(-117.87389, 34.15755, 4116, 748.0, 728.0, 0.02, 1.25),
  S518(-117.83275, 34.0347, 4129, 390.0, 366.0, 0.15, 1.14),
  S520(-117.77945, 34.11322, 4130, 349.0, 364.0, 0.15, 0.31),

  /* San Fernando Valley */
  S311(-118.35851, 34.17667, 4058, 280.0, 323.0, 0.33, 1.16),
  S275(-118.45331, 34.22055, 4047, 280.0, 330.0, 0.35, 1.85),
  P3(-118.42409, 34.24939, 3938, 280.0, 343.0, 0.35, 2.33),
  S238(-118.54821, 34.26435, 4037, 387.0, 473.0, 0.15, 2.62),
  P2(-118.46393, 34.285625, 3932, 455.0, 537.0, 0.15, 3.5),
  P21(-118.58717, 34.209946, 3934, 280.0, 292.0, 0.22, 2.36),

  /* Orange County */
  S510(-118.04497, 33.72038, 4125, 387.0, 254.0, 0.58, 3.16),
  S512(-117.99206, 33.79899, 4126, 280.0, 283.0, 0.72, 4.72),
  S514(-117.93905, 33.87758, 4127, 387.0, 359.0, 0.62, 4.24),
  S516(-117.88595, 33.95615, 4128, 390.0, 328.0, 0.31, 2.21),
  S550(-117.95093, 33.67617, 4139, 280.0, 243.0, 0.44, 4.04),
  S552(-117.89795, 33.75474, 4140, 280.0, 278.0, 0.47, 4.84),
  S554(-117.84489, 33.83329, 4141, 280.0, 328.0, 0.33, 2.26),
  S556(-117.79173, 33.91182, 4142, 390.0, 364.0, 0.18, 2.38),
  S591(-117.85698, 33.63189, 4151, 390.0, 1975.0, 0.0, 2.27),
  S593(-117.80395, 33.71042, 4152, 280.0, 279.0, 0.31, 1.8),
  S595(-117.75082, 33.78893, 4153, 515.0, 2040.0, 0.0, 1.54),

  /* Inland Empire */
  S599(-117.64428, 33.94589, 4155, 387.0, 364.0, 0.19, 0.95),
  S601(-117.59086, 34.02433, 4156, 354.0, 272.0, 0.2, 0.31),
  // S603(-117.53735,34.10275,3883,354.0,364.0,0.19,0.43),
  S605(-117.48374, 34.18115, 4157, 748.0, 704.0, 0.02, 1.07),
  S644(-117.44314, 34.05816, 4167, 280.0, 364.0, 0.17, 0.31),
  S646(-117.38947, 34.13652, 4168, 280.0, 364.0, 0.16, 0.31),
  S647(-117.36259, 34.17569, 4169, 280.0, 392.0, 0.31, 1.22),
  S688(-117.2953, 34.09181, 4181, 280.0, 272.0, 0.29, 1.45),
  S689(-117.2684, 34.13096, 4182, 280.0, 272.0, 0.2, 0.31);

  private final Location loc;
  private final int run; // cybershake run #
  private final double willsVs30;
  private final double cvmVs30;
  private final double z1p0;
  private final double z2p5;

  private CybershakeSite(
      double lon,
      double lat,
      int run,
      double willsVs30,
      double cvmVs30,
      double z1p0,
      double z2p5) {

    this.loc = Location.create(lat, lon);
    this.run = run;
    this.willsVs30 = willsVs30;
    this.cvmVs30 = cvmVs30;
    this.z1p0 = z1p0;
    this.z2p5 = z2p5;
  }

  @Override
  public Location location() {
    return loc;
  }

  @Override
  public String id() {
    return this.name();
  }

  public int runId() {
    return run;
  }

  public double willsVs30() {
    return willsVs30;
  }

  public double cvmVs30() {
    return cvmVs30;
  }

  public double z1p0() {
    return z1p0;
  }

  public double z2p5() {
    return z2p5;
  }

}
