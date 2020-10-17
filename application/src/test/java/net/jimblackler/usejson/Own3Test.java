package net.jimblackler.usejson;

import static net.jimblackler.usejson.ReaderUtils.getLines;
import static net.jimblackler.usejson.StreamUtils.streamToString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.brimworks.json5.JSON5ParseError;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import net.jimblacker.usejson.Json5JsWrapper;
import net.jimblacker.usejson.Json5Parser;
import net.jimblacker.usejson.JsonParseException;
import net.jimblacker.usejson.SyntaxError;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

public class Own3Test {
  private static final FileSystem FILE_SYSTEM = FileSystems.getDefault();

  @TestFactory
  Collection<DynamicNode> succeeds() throws IOException {
    return run(FILE_SYSTEM.getPath("/own").resolve("valid"), true);
  }

  @TestFactory
  Collection<DynamicNode> fails() throws IOException {
    return run(FILE_SYSTEM.getPath("/own").resolve("invalid"), false);
  }

  private Collection<DynamicNode> run(Path testDir, boolean shouldPass) throws IOException {
    Collection<DynamicNode> testsOut = new ArrayList<>();
    Json5JsWrapper json5JsWrapper = new Json5JsWrapper();
    getLines(Own3Test.class.getResourceAsStream(testDir.toString()), testFile -> {
      testsOut.add(DynamicTest.dynamicTest(testFile, () -> {
        try {
          String content = streamToString(
              Own3Test.class.getResourceAsStream(testDir.resolve(testFile).toString()));
          System.out.println(content);

          try {
            Json5Parser json5Parser = new Json5Parser();
            Object own = json5Parser.parse(content);
            assertTrue(shouldPass);

            String ownString = DocumentUtils.toString(own);
            if (!testFile.endsWith(".json5")) {
              String orgString = DocumentUtils.toString(DocumentUtils.parseJson(content));
              assertEquals(orgString, ownString);
            }

            String org2String = DocumentUtils.toString(
                DocumentUtils.parseJson(json5JsWrapper.json5ToJson(content)));
            assertEquals(org2String, ownString);

          } catch (JsonParseException | JSONException | JSON5ParseError | SyntaxError ex) {
            if (shouldPass) {
              throw ex;
            } else {
              ex.printStackTrace();
            }
          }

        } catch (IOException e) {
          throw new IllegalStateException(e);
        }
      }));
    });
    return testsOut;
  }
}
