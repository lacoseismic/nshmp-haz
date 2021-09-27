package gov.usgs.earthquake.nshmp.model.peer;

import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

/**
 * {@code ArgumentProviders} for parameterized tests in {@link PeerTests}
 */
public class PeerTestArgumentProviders {

  public static class Set1Case1 implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S1_C1);
    }
  }

  public static class Set1Case2 implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S1_C2);
    }
  }

  public static class Set1Case2_Fast implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S1_C2_F);
    }
  }

  public static class Set1Case3 implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S1_C3);
    }
  }

  public static class Set1Case3_Fast implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S1_C3_F);
    }
  }

  public static class Set1Case4 implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S1_C4);
    }
  }

  public static class Set1Case4_Fast implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S1_C4_F);
    }
  }

  public static class Set1Case5 implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S1_C5);
    }
  }

  public static class Set1Case5_Fast implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S1_C5_F);
    }
  }

  public static class Set1Case6 implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S1_C6);
    }
  }

  public static class Set1Case6_Fast implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S1_C6_F);
    }
  }

  public static class Set1Case7 implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S1_C7);
    }
  }

  public static class Set1Case7_Fast implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S1_C7_F);
    }
  }

  public static class Set1Case8a implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S1_C8A);
    }
  }

  public static class Set1Case8b implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S1_C8B);
    }
  }

  public static class Set1Case8c implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S1_C8C);
    }
  }

  public static class Set1Case10 implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S1_C10);
    }
  }

  public static class Set1Case10_Fast implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S1_C10_F);
    }
  }

  public static class Set2Case2a implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S2_C2A);
    }
  }

  public static class Set2Case2a_Fast implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S2_C2A_F);
    }
  }

  public static class Set2Case2b implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S2_C2B);
    }
  }

  public static class Set2Case2b_Fast implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S2_C2B_F);
    }
  }

  public static class Set2Case2c implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S2_C2C);
    }
  }

  public static class Set2Case2c_Fast implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S2_C2C_F);
    }
  }

  public static class Set2Case2d implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S2_C2D);
    }
  }

  public static class Set2Case2d_Fast implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S2_C2D_F);
    }
  }

  public static class Set2Case3a implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S2_C3A);
    }
  }

  public static class Set2Case3a_Fast implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S2_C3A_F);
    }
  }

  public static class Set2Case3b implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S2_C3B);
    }
  }

  public static class Set2Case3b_Fast implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S2_C3B_F);
    }
  }

  public static class Set2Case3c implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S2_C3C);
    }
  }

  public static class Set2Case3c_Fast implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S2_C3C_F);
    }
  }

  public static class Set2Case3d implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S2_C3D);
    }
  }

  public static class Set2Case3d_Fast implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S2_C3D_F);
    }
  }

  public static class Set2Case4a implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S2_C4A);
    }
  }

  public static class Set2Case4a_Fast implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S2_C4A_F);
    }
  }

  public static class Set2Case4b implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S2_C4B);
    }
  }

  public static class Set2Case4b_Fast implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S2_C4B_F);
    }
  }

  public static class Set2Case5a implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S2_C5A);
    }
  }

  public static class Set2Case5b implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S2_C5B);
    }
  }

}
