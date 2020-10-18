Json5Parser is based on https://github.com/json5/json5/blob/master/lib/parse.js

The importer differs from `org.json` in the following ways:

* `null` values are imported.
* Duplicate keys are permitted. The final instance of that key will take
  priority.
* Empty array entries (i.e. no content followed by a comma, such as `[,]`) are
  invalid.


Note

* `Infinity` and `NaN` cannot be represented as a type in `org.json` container.