package net.jimblackler.usejson;

import java.util.Random;

public class StringMutator {
  static final String IMPORTANT_CHARACTERS = "[]{}\"':.,\\/*\n-e";
  public static String mutate(String content, Random random) {
    StringBuilder builder = new StringBuilder(content);
    do {
      char ch;
      if (random.nextFloat() < 0.5f) {
        ch = (char) random.nextInt();
      } else {
        ch = IMPORTANT_CHARACTERS.charAt(random.nextInt(IMPORTANT_CHARACTERS.length()));
      }
      builder.setCharAt(random.nextInt(builder.length()), ch);
    } while (random.nextFloat() < 0.99f);

    return builder.toString();
  }
}
