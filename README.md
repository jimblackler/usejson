## usejson

A Java library that allows JSON5 format files to be read into an ordinary tree
of Java objects (using types `Map<>` for dictionaries, and type `List<>` for
arrays).
 
The parser is ported from the reference JavaScript JSON5 parser;
[json5](https://github.com/json5/json5). 
 
The library also provides a simple adapter to load these Java objects into
`org.json` JSON objects. This allows JSON5 files to be used in applications
written for `org.json` types; allowing comments, unquoted keys, and more.

The library is written by [Jim Blackler](mailto:jimblackler@gmail.com) and
offered under the Apache 2.0 license.

## Adapter

`org.json` objects are able to contain data parsed from JSON5 files with the
following exception:

* `Infinity` and `NaN` cannot be represented as a type in `org.json` container.

As JSON5 is a superset of JSON, classic JSON files can also be read. However
this importer differs from `org.json` in the following ways:

* Values of type `null` may be handled differently (a side effect of `org.json`
  inconsistencies with `null` handling through the constructor vs text parser).
* Duplicate keys are permitted. The final instance of that key will take
  priority.
* Empty array entries (i.e. no content followed by a comma, such as `[,]`) are
  invalid.

