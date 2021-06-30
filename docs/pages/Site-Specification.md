# Site Specification

(TODO update etc and/or javadoc links)

The sites at which to perform hazard and related calculations may be defined in a variety of
ways. Examples of the file formats described below are available in the resource directory:
[`etc/nshm`](../../etc/nshm/README.md).

__Note on Coordinates:__ *nshmp-haz-v2* supports longitude and latitude values in the closed
ranges `[-360° ‥ 360°]` and `[-90° ‥ 90°]`. Note, however, that mixing site and/or source
coordinates across the antimeridian (the -180° to 180° transition) will yield unexpected results.
For Pacific models and calculations, always use positive or negative longitudes exclusively.

## Site String

For the case of running a single site of interest, most *nshmp-haz-v2* programs accept a
comma-delimited string of the form: `name,lon,lat[,vs30,vsInf[,z1p0,z2p5]]`, where `vs30`, `vsInf`,
`z1p0`, and `z2p5` are optional. Note that if `vs30` is supplied, so too must `vsInf`. Likewise if
`z1p0` is supplied, so too must `z2p5`. If the string contains any spaces, escape them or wrap the
entire string in double quotes.

For any site parameter values that are not supplied on the command line or in the file formats
below, the following defaults are used (see the `site` member of the
[configuration](./Calculation-Configuration.md) file):

```text
    name: Unnamed
    vs30: 760.0
   vsInf: true
    z1p0: null (GMM will use default basin depth model)
    z2p5: null (GMM will use default basin depth model)
```

For basin depth parameters `z1p0` and `z2p5`, a `null` value indicates that a GMM should use
it's 'default' basin depth scale factor.

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
versatile format for encoding geographic data. *nshmp-haz-v2* uses GeoJSON for both lists of sites
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
Additional properties, if present, are ignored by *nshmp-haz-v2* but permitted as they may be
relevant for other applications. For example, [styling
properties](https://help.github.com/articles/mapping-geojson-files-on-github/#styling-features)
may be used to improve rendering in mapping applications. For a fully fledged example, see the
[NSHM test sites](../../etc/nshm/sites-nshmp.geojson) file.

### Map Regions

GeoJSON is also used to define *nshmp-haz-v2* map regions. For example, see the file that defines a
region commonly used when creating hazard and other maps for the
[Los Angeles basin](../../etc/nshm/map-la-basin.geojson).

A map region is expected as a `Polygon` `FeatureCollection`. Currently, *nshmp-haz-v2* only supports
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

TODO This needs updating; link to conus-2018 active crust map-region.geojson.

---

* [**Documentation Index**](../README.md)
* [Building & Running](./Building-&-Running.md)
  * [Developer Basics](./Developer-Basics.md)
  * [Calculation Configuration](./Calculation-Configuration.md)
  * [Site Specification](./Site-Specification.md)
  * [Examples](../../etc/examples/README.md) (or
    [on GitLab](https://code.usgs.gov/ghsc/nshmp/nshmp-haz-v2/-/tree/master/etc/examples))

---
![USGS logo](./images/usgs-icon.png) &nbsp;[U.S. Geological Survey](https://www.usgs.gov)
National Seismic Hazard Mapping Project ([NSHMP](https://earthquake.usgs.gov/hazards/))
