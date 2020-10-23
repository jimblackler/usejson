## usejson

A Java library that parses JSON5 or JSON format into an ordinary tree of Java
objects (using types `Map<>` for dictionaries, and type `List<>` for arrays).
 
An online demonstration [is here](https://tryjsonschematypes.appspot.com/#json5);
it shows the library being used to convert JSON5 objects to JSON5.

The parser is ported from the reference JavaScript JSON5 parser;
[json5](https://github.com/json5/json5). 
 
Parser output is compatible with `org.json.JSONObject.wrap`, allowing output to
be easily converted into `org.json` objects. This allows JSON5 files to be used
in applications written for `org.json` types; allowing comments, unquoted keys,
single quoted strings, line continuation in strings, hex numbers and more.

The library is written by [Jim Blackler](mailto:jimblackler@gmail.com) and
offered under the Apache 2.0 license.

## Conversion of output to `org.json` types

`org.json` objects are able to contain data parsed from JSON5 files with the
following exception:

As JSON5 is a superset of JSON, classic JSON files can also be read. However,
this importer differs from `org.json` in the following ways:

* Duplicate keys are permitted. The final instance of that key will take
  priority.
* Empty array entries (i.e. no content followed by a comma, such as `[,]`) are
  invalid.

When adapted to `org.json` tyoes using `JSONObject.wrap`:

* `Infinity` and `NaN` values will cause an error to be thrown.
* Values of type `null` are lost.
