package net.jimblackler.usejson;

import java.math.BigInteger;

public class NumberUtils {
  public static Object toBestObject(BigInteger bigInteger) {
    try {
      return bigInteger.byteValueExact();
    } catch (ArithmeticException ignored) {
    }
    try {
      return bigInteger.shortValueExact();
    } catch (ArithmeticException ignored) {
    }
    try {
      return bigInteger.intValueExact();
    } catch (ArithmeticException ignored) {
    }
    try {
      return bigInteger.longValueExact();
    } catch (ArithmeticException ignored) {
    }
    return bigInteger;
  }
}
