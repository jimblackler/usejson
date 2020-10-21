package net.jimblackler.usejson;

import java.math.BigInteger;

public class NumberUtils {
  public static Object toBestObject(BigInteger bigInteger) {
    try {
      return bigInteger.byteValueExact();
    } catch (ArithmeticException ex) {
    }
    try {
      return bigInteger.shortValueExact();
    } catch (ArithmeticException ex) {
    }
    try {
      return bigInteger.intValueExact();
    } catch (ArithmeticException ex) {
    }
    try {
      return bigInteger.longValueExact();
    } catch (ArithmeticException ex) {
    }
    return bigInteger;
  }
}
