package net.jimblackler.usejson;

import java.util.Random;

public class StringMutator {
  public static String mutate(String content, Random random) {
    StringBuilder builder = new StringBuilder(content);
    do {
      builder.setCharAt(random.nextInt(builder.length()), (char) random.nextInt());
    } while (random.nextFloat() < 0.75f);

    return builder.toString();
  }
}
