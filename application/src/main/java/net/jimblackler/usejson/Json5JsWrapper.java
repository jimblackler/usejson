package net.jimblackler.usejson;

import static net.jimblackler.usejson.CacheLoader.load;

import java.io.IOException;
import java.net.URI;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

public class Json5JsWrapper {
  private final Context context = Context.create("js");
  private final Value _json5ToJson;

  public Json5JsWrapper() throws IOException {
    context.eval("js", load(URI.create("https://unpkg.com/json5@2.1.3/dist/index.min.js")));
    _json5ToJson = context.eval("js", "str => JSON.stringify(JSON5.parse(str))");
  }

  public String json5ToJson(String str) {
    return _json5ToJson.execute(str).toString();
  }
}
