package net.jimblackler.usejson;

import static net.jimblackler.usejson.ReaderUtils.getLines;
import static net.jimblackler.usejson.StreamUtils.streamToString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
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
          String ownString = "<invalid>";
          String ownError = "";
          String wrappedError = "";

          try {
            Json5Parser json5Parser = new Json5Parser();
            Object own = json5Parser.parse(content);
            assertTrue(shouldPass);
            ownString = DocumentUtils.toString(own);
          } catch (SyntaxError ex) {
            if (shouldPass) {
              throw ex;
            } else {
              ownError = ex.getMessage();
              System.out.print(ownError);
            }
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

          try {
            String wrappedString = DocumentUtils.toString(
                DocumentUtils.parseJson(json5JsWrapper.json5ToJson(content)));
            assertEquals(wrappedString, ownString);
            assertTrue(shouldPass);
          } catch (SyntaxError ex) {
            if (shouldPass) {
              throw ex;
            } else {
              wrappedError = ex.getMessage();
            }
          }

          assertEquals(wrappedError, ownError);

        } catch (IOException e) {
          throw new IllegalStateException(e);
        }
      }));
    });
    return testsOut;
  }
}
