package gov.usgs.earthquake.nshmp.www.meta;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import gov.usgs.earthquake.nshmp.Maths;
import gov.usgs.earthquake.nshmp.calc.Site;

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

  public static final class SiteSerializer implements JsonSerializer<Site> {

    @Override
    public JsonElement serialize(Site site, Type typeOfSrc, JsonSerializationContext context) {
      JsonObject loc = new JsonObject();

      loc.addProperty("latitude", Maths.round(site.location().latitude, 3));
      loc.addProperty("longitude", Maths.round(site.location().longitude, 3));

      JsonObject json = new JsonObject();
      json.add("location", loc);
      json.addProperty("vs30", site.vs30());
      json.addProperty("vsInferred", site.vsInferred());
      json.addProperty("z1p0", Double.isNaN(site.z1p0()) ? null : site.z1p0());
      json.addProperty("z2p5", Double.isNaN(site.z2p5()) ? null : site.z2p5());

      return json;
    }

  }

}
