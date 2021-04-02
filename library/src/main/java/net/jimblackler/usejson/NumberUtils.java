package net.jimblackler.usejson;

import java.math.BigInteger;

public class NumberUtils {
  public static Object toBestObject(BigInteger bigInteger) {
    if (bigInteger.compareTo(BigInteger.valueOf(Byte.MIN_VALUE)) >= 0
        && bigInteger.compareTo(BigInteger.valueOf(Byte.MAX_VALUE)) <= 0) {
      return bigInteger.byteValue();
    }

    if (bigInteger.compareTo(BigInteger.valueOf(Short.MIN_VALUE)) >= 0
        && bigInteger.compareTo(BigInteger.valueOf(Short.MAX_VALUE)) <= 0) {
      return bigInteger.shortValue();
    }

    if (bigInteger.compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) >= 0
        && bigInteger.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) <= 0) {
      return bigInteger.intValue();
    }

    if (bigInteger.compareTo(BigInteger.valueOf(Long.MIN_VALUE)) >= 0
        && bigInteger.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) <= 0) {
      return bigInteger.longValue();
    }
    return bigInteger;
  }
}
