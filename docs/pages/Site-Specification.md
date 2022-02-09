# Site Specification

The sites at which to perform hazard and related calculations may be defined in a variety of
ways. Examples of the file formats described below are available in the resource directory:
[`etc/nshm`](../../etc/nshm/README.md).

__Note on Coordinates:__ *nshmp-haz* supports longitude and latitude values in the closed
ranges `[-360° ‥ 360°]` and `[-90° ‥ 90°]`. However, mixing site and/or source
coordinates across the antimeridian (the -180° to 180° transition) will yield unexpected results.
For Pacific models and calculations, always use positive or negative longitudes exclusively.

For any site parameter values that are not supplied in the file formats below, the following
defaults are used:

```text
    name: Unnamed
    vs30: 760.0
   vsInf: true
    z1p0: null (GMM will use default basin depth model)
    z2p5: null (GMM will use default basin depth model)
```

For basin depth parameters `z1p0` and `z2p5`, a `null` value indicates that a GMM should use
it's 'default' basin depth scale factor, which is usually included in it's Vs30 site term.

## Comma-Delimited Format (\*.csv)

* Header row must identify columns.
* Valid and [optional] column names are:
  `[name,] lon, lat [, vs30] [, vsInf] [, z1p0] [, z2p5]`  
* At a minimum, `lon` and `lat` must be defined.
* Columns can be in any order and any missing fields will be populated with the default values
  listed above.
* If a site `name` is supplied, it is included in the first column of any output curve files.

## GeoJSON Format (\*.geojson)

Although more verbose than the comma-delimited format, [GeoJSON](http://geojson.org) is a more
versatile format for encoding geographic data. *nshmp-haz* uses GeoJSON for both lists of sites
and to define map regions. If you encounter problems when formatting JSON files, use
[JSONLint](http://jsonlint.com) or [GeoJSONLint](http://geojsonlint.com) for validation.

### Site Lists

A site list is expected as a `FeatureCollection` of `Point` features. For example:

```json
{
  "type": "FeatureCollection",
  "features": [
    {
      "type": "Feature",
      "geometry": {
        "type": "Point",
        "coordinates": [-122.25, 37.80]
      },
      "properties": {
        "title": "Oakland CA",
        "vs30": 760.0,
        "vsInf": true,
        "z1p0": 0.048,
        "z2p5": 0.607
      }
    }
  ]
}
```

As with the CSV format, the minimum required data is a `geometry` `coordinates` array. All
`properties` are optional. When using GeoJSON, the `title` property maps to the name of the site.
Additional properties, if present, are ignored by *nshmp-haz* but permitted as they may be
relevant for other applications. For example, [styling
properties](https://github.com/mapbox/simplestyle-spec/tree/master/1.1.0)
may be used to improve rendering in mapping applications. For a fully fledged example, see the
[NSHM test sites](../../etc/nshm/sites-nshmp.geojson) file.

### Map Regions

GeoJSON is also used to define *nshmp-haz* map regions. For example, see the file that defines a
region commonly used when creating hazard and other maps for the
[Los Angeles basin](../../etc/nshm/map-la-basin.geojson).

A map region is expected as a `Polygon` `FeatureCollection`. Currently, *nshmp-haz* only supports
a `FeatureCollection` with 1 or 2 polygons. When a single polygon is defined, it must consist of a
single, simple closed loop. Additional arrays that define holes in the polygon (per the GeoJSON
spec) are not processed and results are undefined for self-intersecting coordinate arrays. This
polygon feature supports the same (optional and/or extra) `properties` as the `Point` features
described above. The site properties are applied to all sites in the defined region. In addition,
this feature *must* define a `spacing` property with a value in decimal degrees that governs the
site density within the region. This polygon defines the area over which hazard calculations are
to be performed.

A second polygon may be used to define a map region. This polygon *must* be defined first, *must*
have a feature `id` of `Extents`, and *must* be rectangular (in a mercator projection) with edges
parallel to lines of latitude and longitude. Any points in the 'calculation' polygon outside the
'extents' polygon are ignored; hazard values at any points within the 'extents' polygon but
outside the 'calculation' polygon are set to zero. For an example, see the
[NSHMP Western US](../../etc/nshm/map-wus.geojson) map site file.

---

## Related Pages

* [Building & Running](./Building-&-Running.md#building-&-running)
  * [Developer Basics](./Developer-Basics.md#developer-basics)
  * [Calculation Configuration](./Calculation-Configuration.md#calculation-configuration)
  * [Site Specification](./Site-Specification.md#site-specification)
  * [Examples](../../etc/examples) (or
    [on GitLab](https://code.usgs.gov/ghsc/nshmp/nshmp-haz/-/tree/main/etc/examples))
* [**Documentation Index**](../README.md)

---
![USGS logo](./images/usgs-icon.png) &nbsp;[U.S. Geological Survey](https://www.usgs.gov)
National Seismic Hazard Mapping Project ([NSHMP](https://earthquake.usgs.gov/hazards/))
