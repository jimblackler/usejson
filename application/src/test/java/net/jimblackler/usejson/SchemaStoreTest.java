package net.jimblackler.usejson;

import static net.jimblackler.usejson.ReaderUtils.getLines;
import static net.jimblackler.usejson.StreamUtils.streamToString;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.jimblacker.usejson.Parse;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

public class SchemaStoreTest {
  private static final FileSystem FILE_SYSTEM = FileSystems.getDefault();

  @TestFactory
  Collection<DynamicNode> all() {
    Collection<DynamicNode> testsOut = new ArrayList<>();

    Path path0 = FILE_SYSTEM.getPath("/SchemaStore").resolve("src");
    Path schemaPath = path0.resolve("schemas").resolve("json");
    Path testDir = path0.resolve("test");

    getLines(SchemaStoreTest.class.getResourceAsStream(testDir.toString()), resource -> {
      Path testSchema = schemaPath.resolve(resource + ".json");
      URL resource1 = SchemaStoreTest.class.getResource(testSchema.toString());
      if (resource1 == null) {
        return;
      }

      List<DynamicTest> tests = new ArrayList<>();
      Path directoryPath = testDir.resolve(resource);
      getLines(
          SchemaStoreTest.class.getResourceAsStream(directoryPath.toString()), testFileName -> {
            Path testFile = directoryPath.resolve(testFileName);
            URL testDataUrl = SchemaStoreTest.class.getResource(testFile.toString());
            if (testDataUrl == null) {
              return;
            }

            try {
              tests.add(DynamicTest.dynamicTest(testFileName, testDataUrl.toURI(), () -> {
                String content =
                    streamToString(SchemaStoreTest.class.getResourceAsStream(testFile.toString()));
                long startTimeOwn = System.nanoTime();
                Object own = Parse.parseJson(content);
                long timeOwn = System.nanoTime() - startTimeOwn;
                assert own != null;
                long startTimeOrg = System.nanoTime();
                Object org = DocumentUtils.parseJson(content);
                long timeOrg = System.nanoTime() - startTimeOrg;
                assert org != null;
                String orgString = DocumentUtils.toString(org);
                String ownString = DocumentUtils.toString(own);
                System.out.println(orgString);
                assertEquals(orgString, ownString);
                System.out.println("Own: " + timeOwn);
                System.out.println("Org: " + timeOrg);
              }));
            } catch (URISyntaxException e) {
              throw new IllegalStateException(e);
            }
          });
      testsOut.add(DynamicContainer.dynamicContainer(resource, testSchema.toUri(), tests.stream()));
    });

    return testsOut;
  }
}
