# geocoordinates

A Clojure library for carrying out common calculations on geographical coordinates.

All calculations details can be found in the [Ordnance Survey guide] (https://www.ordnancesurvey.co.uk/docs/support/guide-coordinate-systems-great-britain.pdf).
The guide also explains the limitations of this type of datum transformation, in particular related to accuracy.

Available calculations:
* convert latitude and longitude to grid eastings and northings for the Ordnance Survey National Grid Transverse Mercator map projection - and vice versa

## Installation

Via Clojars: http://clojars.org/geocoordinates

[![Clojars Project](http://clojars.org/geocoordinates/latest-version.svg)](http://clojars.org/geocoordinates)

## Usage

The main conversion functions are provided by the ```geocoordinates.core``` namespace.

First, require it in the REPL:

```clojure
(require '[geocoordinates.core :as geo])
```

Or in your application:

```clojure
(ns my-app.core
  (:require [geocoordinates.core :as geo]))
```

To convert from easting and northing to latitude and longitude for the Ordnance Survey National Grid Transverse Mercator map projection:

```clojure
(geo/easting-northing->latitude-longitude {:easting 651409.903 :northing 313177.27} :national-grid)
```

To convert from latitude and longitude to easting and northing for the Ordnance Survey National Grid Transverse Mercator map projection:

```clojure
(geo/latitude-longitude->easting-northing {:latitude 52.65757 :longitude 1.7179215} :national-grid)
```

## Development

To run unit tests:
```shell
clj -M:test
```

To create a jar file:

```shell
clj -T:build jar
```

To install into local maven repository:
```shell
clj -T:build install
```

To upload to clojars:
```shell
clj -T:build deploy
```
To deploy, environment variables CLOJARS_USERNAME and CLOJARS_PASSWORD must be set.

## License

Copyright © 2015 dilico

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
