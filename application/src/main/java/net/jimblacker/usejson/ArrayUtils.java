package net.jimblacker.usejson;

import java.lang.reflect.Array;

public class ArrayUtils {
  public static char[] concatenate(char[] a, char[] b) {
    char[] out = (char[]) Array.newInstance(a.getClass().getComponentType(), a.length + b.length);
    System.arraycopy(a, 0, out, 0, a.length);
    System.arraycopy(b, 0, out, a.length, b.length);
    return out;
  }
}
