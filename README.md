Json5Parser is based on https://github.com/json5/json5/blob/master/lib/parse.js

The importer differs from `org.json` in the following ways:

* `null` values are imported.
* Duplicate keys are permitted. The final instance of that key will take
  priority.
* It is about 8 times slower.


Note

* `Infinity` and `NaN` cannot be represented as a type in `org.json` container.