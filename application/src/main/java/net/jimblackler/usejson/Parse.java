package net.jimblackler.usejson;

import com.brimworks.json5.JSON5Parser;
import com.brimworks.json5.JSON5Visitor;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.json.JSONArray;
import org.json.JSONObject;

public class Parse {
  public static Object parseJson(String content) {
    AtomicReference<Object> outObject = new AtomicReference<>();
    JSON5Parser parser = new JSON5Parser();
    parser.setVisitor(new Visitor(outObject::set));
    parser.parse(content, "");
    return outObject.get();
  }

  static class Visitor implements JSON5Visitor {
    private final List<Consumer<Object>> stack = new ArrayList<>();
    private JSONObject activeObject;
    private JSONArray activeArray;

    Visitor(Consumer<Object> consumer) {
      stack.add(consumer);
    }

    @Override
    public void visitNull(int line, long offset) {
      stack.remove(stack.size() - 1).accept(null);
    }

    @Override
    public void visit(boolean val, int line, long offset) {
      stack.remove(stack.size() - 1).accept(val);
    }

    @Override
    public void visit(String val, int line, long offset) {
      stack.remove(stack.size() - 1).accept(val);
    }

    @Override
    public void visit(Number val, int line, long offset) {
      stack.remove(stack.size() - 1).accept(val);
    }

    @Override
    public void visitNumber(BigInteger val, int line, long offset) {
      stack.remove(stack.size() - 1).accept(val);
    }

    @Override
    public void visitNumber(BigDecimal val, int line, long offset) {
      stack.remove(stack.size() - 1).accept(val);
    }

    @Override
    public void visitNumber(long val, int line, long offset) {
      stack.remove(stack.size() - 1).accept(val);
    }

    @Override
    public void visitNumber(double val, int line, long offset) {
      stack.remove(stack.size() - 1).accept(val);
    }

    @Override
    public void startObject(int line, long offset) {
      activeObject = new JSONObject();
    }

    @Override
    public void visitKey(String key, int line, long offset) {
      JSONObject currentActiveObject = activeObject;
      stack.add(o -> {
        activeObject = currentActiveObject;
        activeObject.put(key, o);
      });
    }

    @Override
    public void endObjectPair(String key, int line, long offset) {}

    @Override
    public void endObject(int line, long offset) {
      stack.remove(stack.size() - 1).accept(activeObject);
    }

    @Override
    public void startArray(int line, long offset) {
      activeArray = new JSONArray();
    }

    @Override
    public void visitIndex(int index, int line, long offset) {
      JSONArray currentActiveArray = activeArray;
      stack.add(o -> {
        activeArray = currentActiveArray;
        activeArray.put(index, o);
      });
    }

    @Override
    public void endArrayValue(int line, long offset) {}

    @Override
    public void endArray(int line, long offset) {
      stack.remove(stack.size() - 1).accept(activeArray);
    }

    @Override
    public void visitComment(String comment, int line, long offset) {}

    @Override
    public void visitSpace(String space, int line, long offset) {}

    @Override
    public void visitColon(int line, long offset) {}

    @Override
    public void visitComma(int line, long offset) {}

    @Override
    public void endOfStream(int line, long offset) {}
  }
}
