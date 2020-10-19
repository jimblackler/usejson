package net.jimblackler.usejson;

public class OrgJsonParser {

  /**
   * Converts a JSON5 or a JSON document into a org.json.JSONObject, an org.json.JSONArray, or in
   * the case of a sole value, null, a String, a Number or a Boolean.
   * @param content The document to convert.
   * @return The converted Object.
   */
  public static Object parse(String content) {
    return OrgJsonAdapter.adapt(new Json5Parser().parse(content));
  }
}
