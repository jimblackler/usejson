The importer differs from `org.json` in the following ways:

* `null` values are imported.
* Duplicate keys are permitted. The final instance of that key will take
  priority.
* It is about 8 times slower.