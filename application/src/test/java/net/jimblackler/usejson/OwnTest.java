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
import net.jimblacker.usejson.JsonParseException;
import net.jimblacker.usejson.Parse;
import org.json.JSONException;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

public class OwnTest {
  private static final FileSystem FILE_SYSTEM = FileSystems.getDefault();

  @TestFactory
  Collection<DynamicNode> succeeds() {
    return run(FILE_SYSTEM.getPath("/own").resolve("valid"), true);
  }

  @TestFactory
  Collection<DynamicNode> fails() {
    return run(FILE_SYSTEM.getPath("/own").resolve("invalid"), false);
  }

  private Collection<DynamicNode> run(Path testDir, boolean shouldPass) {
    Collection<DynamicNode> testsOut = new ArrayList<>();
    getLines(OwnTest.class.getResourceAsStream(testDir.toString()), testFile -> {
      testsOut.add(DynamicTest.dynamicTest(testFile, () -> {
        try {
          String content = streamToString(
              OwnTest.class.getResourceAsStream(testDir.resolve(testFile).toString()));

          try {
            try {
              Object own = Parse.parseJson(content);
              assert own != null;
              Object org = DocumentUtils.parseJson(content);
              assert org != null;
              String orgString = DocumentUtils.toString(org);
              String ownString = DocumentUtils.toString(own);
              System.out.println(orgString);
              assertEquals(orgString, ownString);
            } catch (JSONException | JSON5ParseError ex) {
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
