package net.jimblackler.usejson;

import static net.jimblackler.usejson.ReaderUtils.getLines;
import static net.jimblackler.usejson.StreamUtils.streamToString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

public class OwnTest {
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
    getLines(OwnTest.class.getResourceAsStream(testDir.toString()), testFile -> {
      testsOut.add(DynamicTest.dynamicTest(testFile, () -> {
        try {
          String content = streamToString(
              OwnTest.class.getResourceAsStream(testDir.resolve(testFile).toString()));
          System.out.println(content);
          String ownString = "<invalid>";
          String wrappedString = "<invalid>";
          String ownError = "";
          String wrappedError = "";

          try {
            wrappedString = DocumentUtils.toString(
                DocumentUtils.parseJson(json5JsWrapper.json5ToJson(content)));
            assertTrue(shouldPass);
          } catch (SyntaxError ex) {
            assertFalse(shouldPass);
            wrappedError = ex.getMessage();
          }

          try {
            Object own = JSONObject.wrap(new Json5Parser().parse(content));
            assertTrue(shouldPass);
            ownString = DocumentUtils.toString(own);
          } catch (SyntaxError ex) {
            assertFalse(shouldPass);
            ownError = ex.getMessage();
            System.out.print(ownError);
          }

          if (false)
            if (!testFile.endsWith(".json5")) {
              try {
                String orgString = DocumentUtils.toString(DocumentUtils.parseJson(content));
                assertEquals(orgString, ownString);
                assertTrue(shouldPass);
              } catch (JSONException ex) {
                if (shouldPass) {
                  throw ex;
                } else {
                  ex.printStackTrace();
                }
              }
            }

          assertEquals(wrappedString, ownString);
          assertEquals(wrappedError, ownError);

        } catch (IOException e) {
          throw new IllegalStateException(e);
        }
      }));
    });
    return testsOut;
  }
}
