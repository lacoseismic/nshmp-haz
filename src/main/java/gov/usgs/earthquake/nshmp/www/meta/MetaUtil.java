package gov.usgs.earthquake.nshmp.www.meta;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.Range;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import gov.usgs.earthquake.nshmp.Maths;
import gov.usgs.earthquake.nshmp.calc.Site;
import gov.usgs.earthquake.nshmp.gmm.GmmInput;
import gov.usgs.earthquake.nshmp.gmm.GmmInput.Field;
import gov.usgs.earthquake.nshmp.internal.www.meta.ParamType;

@SuppressWarnings("javadoc")
public final class MetaUtil {

  public static <E extends Enum<E>> List<String> enumsToNameList(
      Collection<E> values) {
    return enumsToStringList(values, Enum::name);
  }

  public static <E extends Enum<E>> List<String> enumsToStringList(
      Collection<E> values,
      Function<E, String> function) {
    return values.stream().map(function).collect(Collectors.toList());
  }

  public static final class EnumSerializer<E extends Enum<E>> implements JsonSerializer<E> {

    @Override
    public JsonElement serialize(E src, Type type, JsonSerializationContext context) {

      JsonObject jObj = new JsonObject();
      jObj.addProperty("id", src.ordinal());
      jObj.addProperty("value", src.name());
      jObj.addProperty("display", src.toString());
      jObj.addProperty("displayorder", src.ordinal());

      if (src instanceof Constrained) {
        Constrained cSrc = (Constrained) src;
        jObj.add("supports", context.serialize(cSrc.constraints()));
      }

      return jObj;
    }
  }

  public static final class SiteSerializer implements JsonSerializer<Site> {

    @Override
    public JsonElement serialize(Site site, Type typeOfSrc, JsonSerializationContext context) {
      JsonObject loc = new JsonObject();

      loc.addProperty("latitude", Maths.round(site.location.latitude, 3));
      loc.addProperty("longitude", Maths.round(site.location.longitude, 3));

      JsonObject json = new JsonObject();
      json.add("location", loc);
      json.addProperty("vs30", site.vs30);
      json.addProperty("vsInfered", site.vsInferred);
      json.addProperty("z1p0", Double.isNaN(site.z1p0) ? null : site.z1p0);
      json.addProperty("z2p5", Double.isNaN(site.z2p5) ? null : site.z2p5);

      return json;
    }

  }

  /* Constrain all doubles to 8 decimal places */
  public static final class DoubleSerializer implements JsonSerializer<Double> {
    @Override
    public JsonElement serialize(Double d, Type type, JsonSerializationContext context) {
      if (Double.isNaN(d)) {
        return null;
      }

      double dOut = Double.valueOf(String.format("%.8g", d));
      return new JsonPrimitive(dOut);
    }
  }

  /* Serialize param type enum as lowercase */
  public static class ParamTypeSerializer implements JsonSerializer<ParamType> {
    @Override
    public JsonElement serialize(ParamType paramType, Type type, JsonSerializationContext context) {
      return new JsonPrimitive(paramType.name().toLowerCase());
    }
  }

  /* Convert NaN to null */
  public static final class NaNSerializer implements JsonSerializer<Double> {
    @Override
    public JsonElement serialize(Double d, Type type, JsonSerializationContext context) {
      return Double.isNaN(d) ? null : new JsonPrimitive(d);
    }
  }

  public static final class ConstraintsSerializer implements JsonSerializer<GmmInput.Constraints> {
    @Override
    public JsonElement serialize(
        GmmInput.Constraints constraints,
        Type type,
        JsonSerializationContext context) {
      JsonArray json = new JsonArray();

      for (Field field : Field.values()) {
        Optional<?> opt = constraints.get(field);
        if (opt.isPresent()) {
          Range<?> value = (Range<?>) opt.get();
          Constraint constraint = new Constraint(
              field.id,
              value.lowerEndpoint(),
              value.upperEndpoint());
          json.add(context.serialize(constraint));
        }
      }

      return json;
    }
  }

  private static class Constraint {
    final String id;
    final Object min;
    final Object max;

    Constraint(String id, Object min, Object max) {
      this.id = id;
      this.min = min;
      this.max = max;
    }
  }

}
