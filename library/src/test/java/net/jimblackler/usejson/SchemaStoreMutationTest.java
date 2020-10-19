package net.jimblackler.usejson;

import static net.jimblackler.usejson.ReaderUtils.getLines;
import static net.jimblackler.usejson.StreamUtils.streamToString;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

public class SchemaStoreMutationTest {
  private static final FileSystem FILE_SYSTEM = FileSystems.getDefault();

  @TestFactory
  Collection<DynamicNode> all() throws IOException {
    Collection<DynamicNode> testsOut = new ArrayList<>();

    Path path0 = FILE_SYSTEM.getPath("/SchemaStore").resolve("src");
    Path schemaPath = path0.resolve("schemas").resolve("json");
    Path testDir = path0.resolve("test");
    Json5JsWrapper json5JsWrapper = new Json5JsWrapper();
    getLines(SchemaStoreMutationTest.class.getResourceAsStream(testDir.toString()), resource -> {
      Path testSchema = schemaPath.resolve(resource + ".json");
      URL resource1 = SchemaStoreMutationTest.class.getResource(testSchema.toString());
      if (resource1 == null) {
        return;
      }

      List<DynamicTest> tests = new ArrayList<>();
      Path directoryPath = testDir.resolve(resource);

      getLines(SchemaStoreMutationTest.class.getResourceAsStream(directoryPath.toString()),
          testFileName -> {
            Path testFile = directoryPath.resolve(testFileName);
            URL testDataUrl = SchemaStoreMutationTest.class.getResource(testFile.toString());
            if (testDataUrl == null) {
              return;
            }

            try {
              tests.add(DynamicTest.dynamicTest(testFileName, testDataUrl.toURI(), () -> {
                Random random = new Random(1);
                String content = streamToString(
                    SchemaStoreMutationTest.class.getResourceAsStream(testFile.toString()));
                content = StringMutator.mutate(content, random);
                System.out.println(content);

                String ownString = "<invalid>";
                String wrappedString = "<invalid>";
                String ownError = "";
                String wrappedError = "";

                try {
                  wrappedString = DocumentUtils.toString(
                      DocumentUtils.parseJson(json5JsWrapper.json5ToJson(content)));
                } catch (SyntaxError ex) {
                  wrappedError = ex.getMessage();
                }

                try {
                  Json5Parser json5Parser = new Json5Parser(); // MOVE THIS OUT!!
                  Object own = OrgJsonAdapter.adapt(json5Parser.parse(content));
                  ownString = DocumentUtils.toString(own);
                } catch (SyntaxError ex) {
                  ownError = ex.getMessage();
                  System.out.print(ownError);
                }

                assertEquals(wrappedString, ownString);
                assertEquals(wrappedError, ownError);

                System.out.println(ownString);
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
