package net.jimblackler.usejson;

import static net.jimblackler.usejson.StreamUtils.streamToString;

import java.io.IOException;
import java.io.InputStream;
import org.json.JSONArray;
import org.json.JSONObject;

public class DocumentUtils {
  public static Object loadJson(InputStream inputStream) throws IOException {
    return parseJson(streamToString(inputStream));
  }

  public static Object parseJson(String content) {
    content = content.replaceAll("[\uFEFF-\uFFFF]", ""); // Strip the dreaded FEFF.
    JSONArray objects = new JSONArray("[" + content + "]");
    if (objects.isEmpty()) {
      return null;
    }
    return objects.get(0);
  }

  public static String toString(Object object) {
    if (object == null) {
      return "null";
    }
    if (object instanceof JSONObject) {
      return ((JSONObject) object).toString(2);
    }
    if (object instanceof JSONArray) {
      return ((JSONArray) object).toString(2);
    }
    return object.toString();
  }
}
