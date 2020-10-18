package net.jimblackler.usejson;

import static net.jimblackler.usejson.ReaderUtils.getLines;
import static net.jimblackler.usejson.StreamUtils.streamToString;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.brimworks.json5.JSON5ParseError;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

import org.graalvm.polyglot.PolyglotException;
import org.json.JSONException;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

public class Own2Test {
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
    Json5JsWrapper json5JsWrapper = new Json5JsWrapper();
    Collection<DynamicNode> testsOut = new ArrayList<>();
    getLines(Own2Test.class.getResourceAsStream(testDir.toString()), testFile -> {
      testsOut.add(DynamicTest.dynamicTest(testFile, () -> {
        try {
          String content = streamToString(
              Own2Test.class.getResourceAsStream(testDir.resolve(testFile).toString()));

          try {
            try {
              Object org = DocumentUtils.parseJson(content);
              String contentConverted = json5JsWrapper.json5ToJson(content);
              Object org2 = DocumentUtils.parseJson(contentConverted);
              assert org != null;
              String orgString = DocumentUtils.toString(org);
              String org2String = DocumentUtils.toString(org2);
              System.out.println(orgString);
              assertEquals(orgString, org2String);
            } catch (JSONException | JSON5ParseError | PolyglotException ex) {
              if (shouldPass) {
                throw ex;
              } else {
                ex.printStackTrace();
              }
            }

          } catch (JsonParseException ex) {
            if (shouldPass) {
              throw ex;
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
