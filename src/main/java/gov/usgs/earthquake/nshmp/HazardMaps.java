package gov.usgs.earthquake.nshmp;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import gov.usgs.earthquake.nshmp.data.Interpolator;

/**
 * Utility class to create hazard map datasets from a hazard curve results.
 * Methods in class assume *.csv curve files have no comments and have a header
 * row that starts with {@code "name,lon,lat,..."} or {@code "lon,lat,..."}.
 *
 * @author U.S. Geological Survey
 */
public class HazardMaps {

  private static final String COMMA = ",";
  private static final String CURVES_FILE = "curves.csv";
  private static final String MAP_FILE = "map.csv";
  private static final Interpolator INTERPOLATOR = Interpolator.builder()
      .logx()
      .logy()
      .decreasingX()
      .build();

  private HazardMaps() {}

  /**
   * Command line application to create a file of return period slices through a
   * hazard curve dataset. Result of slicing job is saved to a {@code map.csv}
   * file in the same directory as the source.
   *
   * @param args a path to a hazard curve result file or directory. If the
   *        supplied path is a directory, application will recurse through file
   *        tree slicing each {@code curves.csv} file encountered.
   */
  public static void main(String[] args) {
    if (args.length < 2) {
      System.out.println("Usage: Supply a path to a file of hazard curve results and");
      System.out.println("       a space separated list of return periods (in yr)");
    }

    Path curvesPath = Path.of(args[0]);
    List<Integer> returnPeriods = Arrays.stream(args)
        .skip(1)
        .mapToInt(Integer::valueOf)
        .boxed()
        .collect(Collectors.toList());

    try {
      createDataSets(curvesPath, returnPeriods);
    } catch (Exception e) {
      System.out.println("Processing Error");
      System.out.println("Arguments: " + Arrays.toString(args));
      e.printStackTrace();
    }
  }

  static void createDataSets(Path curvesPath, List<Integer> returnPeriods) throws IOException {
    if (Files.isDirectory(curvesPath)) {
      CurvesVisitor curvesFinder = new CurvesVisitor(returnPeriods);
      Files.walkFileTree(curvesPath, curvesFinder);
    } else {
      processCurveFile(curvesPath, returnPeriods);
    }
  }

  private static List<String> create(List<String> lines, List<Integer> returnPeriods) {
    int headerCount = lines.get(0).startsWith("name") ? 3 : 2;
    List<String> header = Arrays.asList(lines.get(0).split(COMMA));

    String siteStr = header.subList(0, headerCount)
        .stream()
        .collect(Collectors.joining(COMMA));

    double[] imls = header.subList(headerCount, header.size())
        .stream()
        .mapToDouble(Double::valueOf)
        .toArray();

    StringBuilder mapHeader = new StringBuilder(siteStr);
    returnPeriods.forEach(rp -> mapHeader.append(COMMA).append(rp));

    List<String> linesOut = new ArrayList<>(lines.size());
    linesOut.add(mapHeader.toString());

    Slicer slicer = new Slicer(returnPeriods, imls, headerCount);

    lines.stream()
        .skip(1)
        .map(slicer::slice)
        .forEach(linesOut::add);

    return linesOut;
  }

  private static void processCurveFile(Path curves, List<Integer> returnPeriods) {
    try (Stream<String> stream = Files.lines(curves)) {
      List<String> linesIn = stream.collect(Collectors.toList());
      List<String> linesOut = create(linesIn, returnPeriods);
      Path maps = curves.resolveSibling(MAP_FILE);
      Files.write(maps, linesOut);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  private static class CurvesVisitor extends SimpleFileVisitor<Path> {
    List<Integer> returnPeriods;

    public CurvesVisitor(List<Integer> returnPeriods) {
      this.returnPeriods = returnPeriods;
    }

    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
      Path fileName = path.getFileName();
      if (fileName != null && fileName.endsWith(CURVES_FILE)) {
        processCurveFile(path, returnPeriods);
      }
      return FileVisitResult.CONTINUE;
    }
  }

  private static class Slicer {
    private final List<Integer> returnPeriods;
    private final double[] imls;
    private final int headerCount;

    private Slicer(List<Integer> returnPeriods, double imls[], int headerCount) {
      this.returnPeriods = returnPeriods;
      this.imls = imls;
      this.headerCount = headerCount;
    }

    private String slice(String line) {
      List<String> elements = Arrays.asList(line.split(COMMA));
      String siteStr = elements.subList(0, headerCount)
        .stream()
        .collect(Collectors.joining(COMMA));

      StringBuilder lineOut = new StringBuilder(siteStr);

      double[] rates = elements
          .stream()
          .skip(headerCount)
          .mapToDouble(Double::valueOf)
          .toArray();

      for (double returnPeriod : returnPeriods) {
        lineOut.append(COMMA);
        lineOut.append(INTERPOLATOR.findX(imls, rates, 1 / returnPeriod));
      }

      return lineOut.toString();
    }
  }

}
