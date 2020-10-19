package net.jimblackler.usejson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

public class OrgJsonAdapter {
  /**
   * Converts a Map or an Iterable (such as a List) into a org.json.JSONObject or an
   * org.json.JSONArray. Otherwise return the object.
   * @param value The Object to convert.
   * @return The converted Object.
   */
  public static Object adapt(Object value) {
    if (value instanceof Iterable<?>) {
      return new JSONArray((Iterable<?>) value);
    }

    if (value instanceof Map<?, ?>) {
      return new JSONObject((Map<?, ?>) value);
    }

    return value;
  }
}
