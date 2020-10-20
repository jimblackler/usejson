package net.jimblackler.usejson;

import java.lang.Iterable;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

public class OrgJsonAdapter {
  /**
   * Converts a Map or an Iterable (such as a List) into a org.json.JSONObject or an
   * org.json.JSONArray. Otherwise return the object.
   * @param value The Object to convert.
   * @return The converted Object.
   */
  public static Object adapt(Object value) {
    if (value instanceof Iterable<?>) {
      // The below can be simplified when this patch has landed to a published version of
      // `org.json`.
      // https://github.com/stleary/JSON-java/commit/f37c2d67c57f1217b0c42b29d636da0b2e750bfa
      JSONArray array = new JSONArray();
      for (Object o : (Iterable<?>) value) {
        array.put(JSONObject.wrap(o));
      }
      return array;
    }

    if (value instanceof Map<?, ?>) {
      return new JSONObject((Map<?, ?>) value);
    }

    return value;
  }
}
