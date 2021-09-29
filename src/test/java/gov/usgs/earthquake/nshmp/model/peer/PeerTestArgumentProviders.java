package gov.usgs.earthquake.nshmp.model.peer;

import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

/**
 * {@code ArgumentProviders} for parameterized tests in {@link PeerTests}
 */
class PeerTestArgumentProviders {

  static class Set1Case1 implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S1_C1);
    }
  }

  static class Set1Case2 implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S1_C2);
    }
  }

  static class Set1Case2_Fast implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S1_C2_F);
    }
  }

  static class Set1Case3 implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S1_C3);
    }
  }

  static class Set1Case3_Fast implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S1_C3_F);
    }
  }

  static class Set1Case4 implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S1_C4);
    }
  }

  static class Set1Case4_Fast implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S1_C4_F);
    }
  }

  static class Set1Case5 implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S1_C5);
    }
  }

  static class Set1Case5_Fast implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S1_C5_F);
    }
  }

  static class Set1Case6 implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S1_C6);
    }
  }

  static class Set1Case6_Fast implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S1_C6_F);
    }
  }

  static class Set1Case7 implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S1_C7);
    }
  }

  static class Set1Case7_Fast implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S1_C7_F);
    }
  }

  static class Set1Case8a implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S1_C8A);
    }
  }

  static class Set1Case8b implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S1_C8B);
    }
  }

  static class Set1Case8c implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S1_C8C);
    }
  }

  static class Set1Case10 implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S1_C10);
    }
  }

  static class Set1Case10_Fast implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S1_C10_F);
    }
  }

  static class Set1Case11 implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S1_C11);
    }
  }

  static class Set1Case11_Fast implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S1_C11_F);
    }
  }

  static class Set2Case2a implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S2_C2A);
    }
  }

  static class Set2Case2a_Fast implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S2_C2A_F);
    }
  }

  static class Set2Case2b implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S2_C2B);
    }
  }

  static class Set2Case2b_Fast implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S2_C2B_F);
    }
  }

  static class Set2Case2c implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S2_C2C);
    }
  }

  static class Set2Case2c_Fast implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S2_C2C_F);
    }
  }

  static class Set2Case2d implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S2_C2D);
    }
  }

  static class Set2Case2d_Fast implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S2_C2D_F);
    }
  }

  static class Set2Case3a implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S2_C3A);
    }
  }

  static class Set2Case3a_Fast implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S2_C3A_F);
    }
  }

  static class Set2Case3b implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S2_C3B);
    }
  }

  static class Set2Case3b_Fast implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S2_C3B_F);
    }
  }

  static class Set2Case3c implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S2_C3C);
    }
  }

  static class Set2Case3c_Fast implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S2_C3C_F);
    }
  }

  static class Set2Case3d implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S2_C3D);
    }
  }

  static class Set2Case3d_Fast implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S2_C3D_F);
    }
  }

  static class Set2Case4a implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S2_C4A);
    }
  }

  static class Set2Case4a_Fast implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S2_C4A_F);
    }
  }

  static class Set2Case4b implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S2_C4B);
    }
  }

  static class Set2Case4b_Fast implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S2_C4B_F);
    }
  }

  static class Set2Case5a implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S2_C5A);
    }
  }

  static class Set2Case5b implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return PeerTest.load(PeerTest.S2_C5B);
    }
  }

}
