package gov.usgs.earthquake.nshmp.json;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import gov.usgs.earthquake.nshmp.geo.Location;
import gov.usgs.earthquake.nshmp.geo.LocationList;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Create a GeoJson {@code FeatureCollection}. See
 *    {@link Builder} for an example. 
 *  <br><br>
 * 
 * A GeoJson {@code FeatureCollection} is a GeoJson object with {@link #type}
 *    "FeatureCollection" and a single member {@link #features}. The
 *    {@link #features} member is a {@code List} of {@link Feature}s containing
 *    a {@link Geometry} of {@link Point}(s) and/or {@link Polygon}(s). 
 * <br><br>
 * 
 * The {@code List} of {@link Feature}s can be a mix of
 *    {@link Point}(s) and {@link Polygon}(s). 
 * <br><br>
 *
 * Convenience methods are added to easily read and write GeoJson files:
 *    <ul>
 *      <li> Write to a file: {@link FeatureCollection#write(Path)} </li>
 *      <li> Read a file: {@link FeatureCollection#read(InputStreamReader)} </li>
 *    </ul>
 * <br><br> 
 * 
 * A {@link Builder} is supplied for ease of adding {@link Feature}s and
 *    creating a {@link Geometry}: 
 *    <ul> 
 *      <li> {@link Builder#add(Feature)} </li> 
 *      <li> {@link Builder#createPoint(Properties, Location)} </li> 
 *      <li> {@link Builder#createPoint(Properties, double, double)} </li> 
 *      <li> {@link Builder#createPolygon(Properties, LocationList, LocationList...)} </li> 
 *    </ul> 
 *    See {@link Builder} for example.
 * 
 * @author Brandon Clayton
 */
public class FeatureCollection implements GeoJson {
  /** The {@link GeoJsonType} of GeoJson object: FeatureCollection */
  private String type;
  /** The {@code List} of {@link Feature}s. */
  private final List<Feature> features;

  /**
   * Return a new instance of a GeoJson {@code FeatureCollection}
   *    using the {@link #builder()}. 
   */
  private FeatureCollection(Builder builder) {
    this.type = GeoJsonType.FEATURE_COLLECTION.toUpperCamelCase();
    this.features = builder.features;
  }

  /**
   * Return a {@code ImmutableList<Feature>} representing the 
   *    {@link Feature}s
   *    
   * @return The {@code Feature}s
   */
  public ImmutableList<Feature> getFeatures() {
    return new ImmutableList.Builder<Feature>().addAll(this.features).build();
  }
  
  /**
   * Return the {@link GeoJsonType} representing the {@code FeatureCollection}.
   * @return The {@code GeoJsonType}.
   */ 
  public GeoJsonType getType() {
    return GeoJsonType.getEnum(this.type);
  }
 
  /**
   * Read in a GeoJson {@code FeatureCollection} from a
   *    {@code InputStreamReader}. 
   * <br><br>
   * 
   * Example:
   * 
   * <pre>
   * {@code
   *   String urlStr = "url of GeoJson FeatureCollection file";
   *   URL url = new URL(urlStr);
   *   InputStreamReader reader = new InputStreamReader(url.openStream());
   *   FeatureCollection fc = FeatureCollection.read(reader);
   * 
   *   Feature singleFeature = fc.getFeatures().get(0);
   *   Point point = (Point) singleFeature.getGeometry();
   *   double[] coords = point.getCoordinates();
   *   Location loc = point.getLocation();
   *   Properties properties = singleFeature.getProperties();
   * }
   * </pre>
   * 
   * @param reader The {@code InputStreamReader}
   * @return A new instance of a {@code FeatureCollection}.
   */
  public static FeatureCollection read(InputStreamReader reader) {
    checkNotNull(reader, "Input stream cannot be null");
    return Util.GSON.fromJson(reader, FeatureCollection.class);
  }

  /**
   * Write a {@code FeatureCollection} to a file.
   * <br><br>
   * 
   * Example:
   * <pre>
   * {@code 
   *  Properties properties = Properties.builder()
   *      .title("Title")
   *      .id("id")
   *      .build();
   *  FeatureCollection fc = FeatureCollection.builder()
   *      .createPoint(properties, 40, -120)
   *      .build();
   *  Path out = Paths.get("etc").resolve("test.geojson");
   *  fc.write(out);
   * }
   * </pre>
   *
   * @param out The {@code Path} to write the file.
   * @throws IOException The {@code IOException}. 
   */
  public void write(Path out) throws IOException {
    checkNotNull(out, "Path cannot be null");
    String json = this.toJsonString();
    Files.write(out, json.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Return a {@code String} in JSON format.
   */
  public String toJsonString() {
    return Util.cleanPoly(Util.GSON.toJson(this));
  }

  /**
   * Return a new instance of {@link Builder}.
   * @return New {@link Builder}.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Convenience builder to build a new instance of a {@link FeatureCollection}.
   * <br><br>
   * 
   * Easily add {@link Feature}s to a {@code List} by: 
   *  <ul> 
   *    <li> {@link Builder#add(Feature)} </li> 
   *    <li> {@link Builder#createPoint(Properties, Location)} </li> 
   *    <li> {@link Builder#createPoint(Properties, double, double)} </li> 
   *    <li> {@link Builder#createPolygon(Properties, LocationList, LocationList...)} </li> 
   *  </ul>
   * <br><br>
   * 
   * Example:
   * <pre>
   * {@code
   *   Properties properties = Properties.builder()
   *       .title("Golden")
   *       .id("golden")
   *       .build();
   *   FeatureCollection fc = FeatureCollection.builder()
   *       .createPoint(properties, 39.75, -105)
   *       .build();
   * }
   * </pre>
   * 
   * @author Brandon Clayton
   */
  public static class Builder {
    private List<Feature> features = new ArrayList<>(); 

    private Builder() {}

    /**
     * Return a new instance of a {@link FeatureCollection}.
     * @return New {@link FeatureCollection}.
     */
    public FeatureCollection build() {
      checkState(!features.isEmpty(), "List of features cannot be empty");
      return new FeatureCollection(this);
    }

    /**
     * Add a {@link Feature} to the {@link FeatureCollection#features}
     *    {@code List}.
     *    
     * @param feature The {@code Feature} to add.
     * @return Return the {@code Builder} to make chainable.
     */
    public Builder add(Feature feature) {
      checkNotNull(feature, "A feature cannot be null");
      this.features.add(feature);
      return this;
    }

    /**
     * Add a {@link Feature} with {@link Geometry} of {@link Point} to
     *    the {@link FeatureCollection#features} {@code List}.
     *    
     * @param properties The {@link Properties} of the point.
     * @param loc The {@link Location} of the point.
     * @return Return the {@code Builder} to make chainable.
     */
    public Builder createPoint(Properties properties, Location loc) {
      this.features.add(Feature.createPoint(properties, loc));
      return this;
    }

    /**
     * Add a {@link Feature} with {@link Geometry} of {@link Point} to
     *    the {@link FeatureCollection#features} {@code List}.
     *    
     * @param properties The {@link Properties} of the point.
     * @param latitude The latitude of the point.
     * @param longitude The longitude of the point.
     * @return Return the {@code Builder} to make chainable.
     */
    public Builder createPoint(Properties properties, double latitude, double longitude) {
      this.features.add(Feature.createPoint(properties, latitude, longitude));
      return this;
    }

    /**
     * Add a {@link Feature} with {@link Geometry} of {@link Polygon}
     *    to the {@link FeatureCollection#features} {@code List}.
     *    
     * @param properties The {@link Properties} of the polygon.
     * @param border The border of the {@code Polygon} 
     * @param interiors The interiors of the {@code Polygon}
     * @return Return the {@code Builder} to make chainable.
     */
    public Builder createPolygon(
        Properties properties,
        LocationList border,
        LocationList... interiors) {
      this.features.add(Feature.createPolygon(properties, border, interiors));
      return this;
    }
    
  }

}