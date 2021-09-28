package gov.usgs.earthquake.nshmp.model.peer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import gov.usgs.earthquake.nshmp.calc.Site;
import gov.usgs.earthquake.nshmp.model.HazardModel;
import gov.usgs.earthquake.nshmp.model.peer.PeerTestArgumentProviders.Set1Case1;
import gov.usgs.earthquake.nshmp.model.peer.PeerTestArgumentProviders.Set1Case2;
import gov.usgs.earthquake.nshmp.model.peer.PeerTestArgumentProviders.Set1Case2_Fast;
import gov.usgs.earthquake.nshmp.model.peer.PeerTestArgumentProviders.Set1Case3;
import gov.usgs.earthquake.nshmp.model.peer.PeerTestArgumentProviders.Set1Case3_Fast;
import gov.usgs.earthquake.nshmp.model.peer.PeerTestArgumentProviders.Set1Case4;
import gov.usgs.earthquake.nshmp.model.peer.PeerTestArgumentProviders.Set1Case4_Fast;
import gov.usgs.earthquake.nshmp.model.peer.PeerTestArgumentProviders.Set1Case5;
import gov.usgs.earthquake.nshmp.model.peer.PeerTestArgumentProviders.Set1Case5_Fast;
import gov.usgs.earthquake.nshmp.model.peer.PeerTestArgumentProviders.Set1Case6;
import gov.usgs.earthquake.nshmp.model.peer.PeerTestArgumentProviders.Set1Case6_Fast;
import gov.usgs.earthquake.nshmp.model.peer.PeerTestArgumentProviders.Set1Case7;
import gov.usgs.earthquake.nshmp.model.peer.PeerTestArgumentProviders.Set1Case7_Fast;

class PeerTests {

  /*
   * This is the primary entry point for running all PEER test cases.
   *
   * All tolerances are percentages.
   *
   * Although HazardCurve calculations return results in annual rate, all PEER
   * test case comparisons are done as Poisson probability.
   */

  private static ExecutorService EXEC;
  private static final double TOL = 1e-6;

  @BeforeAll
  static void setUpBeforeClass() throws Exception {
    EXEC = Executors.newSingleThreadExecutor();
  }

  @AfterAll
  static void tearDownAfterClass() throws Exception {
    EXEC.shutdown();
  }

  @Test
  void emptyTestEclipseBug() {
    /*
     * Silly workaround for junt5 tests in eclipse where no tests are found
     * unless at least one @Test annotaion exists. Doesn't fix fact that one
     * test below cannot be run on its own.
     */
  }

  @ParameterizedTest(name = "{0}, Site={index}")
  @ArgumentsSource(Set1Case1.class)
  @DisplayName("Set1 Case1")
  final void set1Case1(String modelName, HazardModel model, Site site,
      double[] expected) {
    PeerTest.test(modelName, model, site, expected, TOL, EXEC);
  }

  @Disabled
  @ParameterizedTest(name = "{0}, Site={index}")
  @ArgumentsSource(Set1Case2.class)
  @DisplayName("Set1 Case2")
  final void set1Case2(String modelName, HazardModel model, Site site,
      double[] expected) {
    PeerTest.test(modelName, model, site, expected, TOL, EXEC);
  }

  @ParameterizedTest(name = "{0}, Site={index}")
  @ArgumentsSource(Set1Case2_Fast.class)
  @DisplayName("Set1 Case2 Fast")
  final void set1Case2F(String modelName, HazardModel model, Site site,
      double[] expected) {
    PeerTest.test(modelName, model, site, expected, TOL, EXEC);
  }

  @Disabled
  @ParameterizedTest(name = "{0}, Site={index}")
  @ArgumentsSource(Set1Case3.class)
  @DisplayName("Set1 Case3")
  final void set1Case3(String modelName, HazardModel model, Site site,
      double[] expected) {
    PeerTest.test(modelName, model, site, expected, TOL, EXEC);
  }

  @ParameterizedTest(name = "{0}, Site={index}")
  @ArgumentsSource(Set1Case3_Fast.class)
  @DisplayName("Set1 Case3 Fast")
  final void set1Case3F(String modelName, HazardModel model, Site site,
      double[] expected) {
    PeerTest.test(modelName, model, site, expected, TOL, EXEC);
  }

  @Disabled
  @ParameterizedTest(name = "{0}, Site={index}")
  @ArgumentsSource(Set1Case4.class)
  @DisplayName("Set1 Case4")
  final void set1Case4(String modelName, HazardModel model, Site site,
      double[] expected) {
    PeerTest.test(modelName, model, site, expected, TOL, EXEC);
  }

  @ParameterizedTest(name = "{0}, Site={index}")
  @ArgumentsSource(Set1Case4_Fast.class)
  @DisplayName("Set1 Case4 Fast")
  final void set1Case4F(String modelName, HazardModel model, Site site,
      double[] expected) {
    PeerTest.test(modelName, model, site, expected, TOL, EXEC);
  }

  @Disabled
  @ParameterizedTest(name = "{0}, Site={index}")
  @ArgumentsSource(Set1Case5.class)
  @DisplayName("Set1 Case5")
  final void set1Case5(String modelName, HazardModel model, Site site,
      double[] expected) {
    PeerTest.test(modelName, model, site, expected, TOL, EXEC);
  }

  @ParameterizedTest(name = "{0}, Site={index}")
  @ArgumentsSource(Set1Case5_Fast.class)
  @DisplayName("Set1 Case5 Fast")
  final void set1Case5F(String modelName, HazardModel model, Site site,
      double[] expected) {
    PeerTest.test(modelName, model, site, expected, TOL, EXEC);
  }

  @Disabled
  @ParameterizedTest(name = "{0}, Site={index}")
  @ArgumentsSource(Set1Case6.class)
  @DisplayName("Set1 Case6")
  final void set1Case6(String modelName, HazardModel model, Site site,
      double[] expected) {
    PeerTest.test(modelName, model, site, expected, TOL, EXEC);
  }

  @ParameterizedTest(name = "{0}, Site={index}")
  @ArgumentsSource(Set1Case6_Fast.class)
  @DisplayName("Set1 Case6 Fast")
  final void set1Case6F(String modelName, HazardModel model, Site site,
      double[] expected) {
    PeerTest.test(modelName, model, site, expected, TOL, EXEC);
  }

  @Disabled
  @ParameterizedTest(name = "{0}, Site={index}")
  @ArgumentsSource(Set1Case7.class)
  @DisplayName("Set1 Case7")
  final void set1Case7(String modelName, HazardModel model, Site site,
      double[] expected) {
    PeerTest.test(modelName, model, site, expected, TOL, EXEC);
  }

  @ParameterizedTest(name = "{0}, Site={index}")
  @ArgumentsSource(Set1Case7_Fast.class)
  @DisplayName("Set1 Case7 Fast")
  final void set1Case7F(String modelName, HazardModel model, Site site,
      double[] expected) {
    PeerTest.test(modelName, model, site, expected, TOL, EXEC);
  }

  // @ParameterizedTest(name = "{0}, Site={index}")
  // @ArgumentsSource(Set1Case8a.class)
  // @DisplayName("Set1 Case8a")
  // final void set1Case8a(String modelName, HazardModel model, Site
  // site, double[] expected) {
  // PeerTest.test(modelName, model, site, expected, TOL, EXEC);
  // }
  //
  // @ParameterizedTest(name = "{0}, Site={index}")
  // @ArgumentsSource(Set1Case8b.class)
  // @DisplayName("Set1 Case8b")
  // final void set1Case8b(String modelName, HazardModel model, Site
  // site, double[] expected) {
  // PeerTest.test(modelName, model, site, expected, TOL, EXEC);
  // }
  //
  // @ParameterizedTest(name = "{0}, Site={index}")
  // @ArgumentsSource(Set1Case8c.class)
  // @DisplayName("Set1 Case8c")
  // final void set1Case8c(String modelName, HazardModel model, Site
  // site, double[] expected) {
  // PeerTest.test(modelName, model, site, expected, TOL, EXEC);
  // }
  //
  // @Disabled
  // @ParameterizedTest(name = "{0}, Site={index}")
  // @ArgumentsSource(Set1Case10.class)
  // @DisplayName("Set1 Case10")
  // final void set1Case10(String modelName, HazardModel model, Site
  // site, double[] expected) {
  // PeerTest.test(modelName, model, site, expected, TOL, EXEC);
  // }
  //
  // @ParameterizedTest(name = "{0}, Site={index}")
  // @ArgumentsSource(Set1Case10_Fast.class)
  // @DisplayName("Set1 Case10 Fast")
  // final void set1Case10F(String modelName, HazardModel model, Site
  // site, double[] expected) {
  // PeerTest.test(modelName, model, site, expected, TOL, EXEC);
  // }
  //
  // @Disabled
  // @ParameterizedTest(name = "{0}, Site={index}")
  // @ArgumentsSource(Set2Case2a.class)
  // @DisplayName("Set2 Case2a")
  // final void set2Case2a(String modelName, HazardModel model, Site
  // site, double[] expected) {
  // PeerTest.test(modelName, model, site, expected, TOL, EXEC);
  // }
  //
  // @ParameterizedTest(name = "{0}, Site={index}")
  // @ArgumentsSource(Set2Case2a_Fast.class)
  // @DisplayName("Set2 Case2a Fast")
  // final void set2Case2aF(String modelName, HazardModel model, Site
  // site, double[] expected) {
  // PeerTest.test(modelName, model, site, expected, TOL, EXEC);
  // }
  //
  // @Disabled
  // @ParameterizedTest(name = "{0}, Site={index}")
  // @ArgumentsSource(Set2Case2b.class)
  // @DisplayName("Set2 Case2b")
  // final void set2Case2b(String modelName, HazardModel model, Site
  // site, double[] expected) {
  // PeerTest.test(modelName, model, site, expected, TOL, EXEC);
  // }
  //
  // @ParameterizedTest(name = "{0}, Site={index}")
  // @ArgumentsSource(Set2Case2b_Fast.class)
  // @DisplayName("Set2 Caseb2 Fast")
  // final void set2Case2bF(String modelName, HazardModel model, Site
  // site, double[] expected) {
  // PeerTest.test(modelName, model, site, expected, TOL, EXEC);
  // }
  //
  // @Disabled
  // @ParameterizedTest(name = "{0}, Site={index}")
  // @ArgumentsSource(Set2Case2c.class)
  // @DisplayName("Set2 Case2c")
  // final void set2Case2c(String modelName, HazardModel model, Site
  // site, double[] expected) {
  // PeerTest.test(modelName, model, site, expected, TOL, EXEC);
  // }
  //
  // @ParameterizedTest(name = "{0}, Site={index}")
  // @ArgumentsSource(Set2Case2c_Fast.class)
  // @DisplayName("Set2 Case2c Fast")
  // final void set2Case2cF(String modelName, HazardModel model, Site
  // site, double[] expected) {
  // PeerTest.test(modelName, model, site, expected, TOL, EXEC);
  // }
  //
  // @Disabled
  // @ParameterizedTest(name = "{0}, Site={index}")
  // @ArgumentsSource(Set2Case2d.class)
  // @DisplayName("Se2 Case2d")
  // final void set2Case2d(String modelName, HazardModel model, Site
  // site, double[] expected) {
  // PeerTest.test(modelName, model, site, expected, TOL, EXEC);
  // }
  //
  // @ParameterizedTest(name = "{0}, Site={index}")
  // @ArgumentsSource(Set2Case2d_Fast.class)
  // @DisplayName("Set2 Case2d Fast")
  // final void set2Case2dF(String modelName, HazardModel model, Site
  // site, double[] expected) {
  // PeerTest.test(modelName, model, site, expected, TOL, EXEC);
  // }
  //
  // @Disabled
  // @ParameterizedTest(name = "{0}, Site={index}")
  // @ArgumentsSource(Set2Case3a.class)
  // @DisplayName("Set2 Case3a")
  // final void set2Case3a(String modelName, HazardModel model, Site
  // site, double[] expected) {
  // PeerTest.test(modelName, model, site, expected, TOL, EXEC);
  // }
  //
  // @ParameterizedTest(name = "{0}, Site={index}")
  // @ArgumentsSource(Set2Case3a_Fast.class)
  // @DisplayName("Set2 Case3a Fast")
  // final void set2Case3aF(String modelName, HazardModel model, Site
  // site, double[] expected) {
  // PeerTest.test(modelName, model, site, expected, TOL, EXEC);
  // }
  //
  // @Disabled
  // @ParameterizedTest(name = "{0}, Site={index}")
  // @ArgumentsSource(Set2Case3b.class)
  // @DisplayName("Set2 Case3b")
  // final void set2Case3b(String modelName, HazardModel model, Site
  // site, double[] expected) {
  // PeerTest.test(modelName, model, site, expected, TOL, EXEC);
  // }
  //
  // @ParameterizedTest(name = "{0}, Site={index}")
  // @ArgumentsSource(Set2Case3b_Fast.class)
  // @DisplayName("Set2 Case3b Fast")
  // final void set2Case3bF(String modelName, HazardModel model, Site
  // site, double[] expected) {
  // PeerTest.test(modelName, model, site, expected, TOL, EXEC);
  // }
  //
  // @Disabled
  // @ParameterizedTest(name = "{0}, Site={index}")
  // @ArgumentsSource(Set2Case3c.class)
  // @DisplayName("Set2 Case3c")
  // final void set2Case3c(String modelName, HazardModel model, Site
  // site, double[] expected) {
  // PeerTest.test(modelName, model, site, expected, TOL, EXEC);
  // }
  //
  // @ParameterizedTest(name = "{0}, Site={index}")
  // @ArgumentsSource(Set2Case3c_Fast.class)
  // @DisplayName("Set2 Case3c Fast")
  // final void set2Case3cF(String modelName, HazardModel model, Site
  // site, double[] expected) {
  // PeerTest.test(modelName, model, site, expected, TOL, EXEC);
  // }
  //
  // @Disabled
  // @ParameterizedTest(name = "{0}, Site={index}")
  // @ArgumentsSource(Set2Case3d.class)
  // @DisplayName("Set2 Case3d")
  // final void set2Case3d(String modelName, HazardModel model, Site
  // site, double[] expected) {
  // PeerTest.test(modelName, model, site, expected, TOL, EXEC);
  // }
  //
  // @ParameterizedTest(name = "{0}, Site={index}")
  // @ArgumentsSource(Set2Case3d_Fast.class)
  // @DisplayName("Set2 Case3d Fast")
  // final void set2Case3dF(String modelName, HazardModel model, Site
  // site, double[] expected) {
  // PeerTest.test(modelName, model, site, expected, TOL, EXEC);
  // }
  //
  // @Disabled
  // @ParameterizedTest(name = "{0}, Site={index}")
  // @ArgumentsSource(Set2Case4a.class)
  // @DisplayName("Set2 Case4a")
  // final void set2Case4a(String modelName, HazardModel model, Site
  // site, double[] expected) {
  // PeerTest.test(modelName, model, site, expected, TOL, EXEC);
  // }
  //
  // @ParameterizedTest(name = "{0}, Site={index}")
  // @ArgumentsSource(Set2Case4a_Fast.class)
  // @DisplayName("Set2 Case4a Fast")
  // final void set2Case4aF(String modelName, HazardModel model, Site
  // site, double[] expected) {
  // PeerTest.test(modelName, model, site, expected, TOL, EXEC);
  // }
  //
  // @Disabled
  // @ParameterizedTest(name = "{0}, Site={index}")
  // @ArgumentsSource(Set2Case4b.class)
  // @DisplayName("Set2 Case4b")
  // final void set2Case4b(String modelName, HazardModel model, Site
  // site, double[] expected) {
  // PeerTest.test(modelName, model, site, expected, TOL, EXEC);
  // }
  //
  // @ParameterizedTest(name = "{0}, Site={index}")
  // @ArgumentsSource(Set2Case4b_Fast.class)
  // @DisplayName("Set2 Case4b Fast")
  // final void set2Case4bF(String modelName, HazardModel model, Site
  // site, double[] expected) {
  // PeerTest.test(modelName, model, site, expected, TOL, EXEC);
  // }
  //
  // @ParameterizedTest(name = "{0}, Site={index}")
  // @ArgumentsSource(Set2Case5a.class)
  // @DisplayName("Set2 Case5a")
  // final void set2Case5a(String modelName, HazardModel model, Site
  // site, double[] expected) {
  // PeerTest.test(modelName, model, site, expected, TOL, EXEC);
  // }
  //
  // @ParameterizedTest(name = "{0}, Site={index}")
  // @ArgumentsSource(Set2Case5b.class)
  // @DisplayName("Set2 Case5b")
  // final void set2Case5b(String modelName, HazardModel model, Site
  // site, double[] expected) {
  // PeerTest.test(modelName, model, site, expected, TOL, EXEC);
  // }

}
