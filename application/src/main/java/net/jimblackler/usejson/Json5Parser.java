package net.jimblackler.usejson;

import static java.lang.Integer.parseInt;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import org.json.JSONArray;
import org.json.JSONObject;

public class Json5Parser {
  private String source;
  private State parseState;
  private LinkedList<Object> stack;
  private int pos;
  private int line;
  private int column;
  private Token token;
  private State lexState;
  private String key;
  private Object root;
  private StringBuilder buffer;
  private boolean doubleQuote;
  private int sign;
  private Character c;

  static String formatChar(char c) {
    String s = String.valueOf(c);
    return s.replace("\\", "\\\\")
        .replace("\t", "\\t")
        .replace("\b", "\\b")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\f", "\\f")
        .replace("\'", "\\'")
        .replace("\"", "\\\"");
  }

  public Object parse(String text) {
    source = text;
    parseState = State.START;
    stack = new LinkedList<>();
    pos = 0;
    line = 1;
    column = 0;
    token = null;
    // key = undefined;
    root = null;
    do {
      token = lex();

      parseStates();
    } while (token.getType() != TokenType.EOF);

    return root;
  }

  private void parseStates() {
    switch (parseState) {
      case START:
        if (token.getType() == TokenType.EOF) {
          throw invalidEOF();
        }

        push();
        break;

      case BEFORE_PROPERTY_NAME:
        switch (token.getType()) {
          case IDENTIFIER:
          case STRING:

            key = token.getValue().toString();
            parseState = State.AFTER_PROPERTY_NAME;
            return;

          case PUNCTUATOR:
            pop();
            return;

          case EOF:
            throw invalidEOF();
        }
        break;
      case AFTER_PROPERTY_NAME:
        if (token.getType() == TokenType.EOF) {
          throw invalidEOF();
        }
        parseState = State.BEFORE_PROPERTY_VALUE;
        break;

      case BEFORE_PROPERTY_VALUE:
        if (token.getType() == TokenType.EOF) {
          throw invalidEOF();
        }
        push();
        break;
      case BEFORE_ARRAY_VALUE:
        if (token.getType() == TokenType.EOF) {
          throw invalidEOF();
        }

        if (token.getType() == TokenType.PUNCTUATOR && ((Character) token.getValue()) == ']') {
          pop();
          return;
        }

        push();
        break;

      case AFTER_PROPERTY_VALUE:
        if (token.getType() == TokenType.EOF) {
          throw invalidEOF();
        }

        switch ((Character) token.getValue()) {
          case ',':
            parseState = State.BEFORE_PROPERTY_NAME;
            return;

          case '}':
            pop();
        }
        break;

      case AFTER_ARRAY_VALUE:
        if (token.getType() == TokenType.EOF) {
          throw invalidEOF();
        }

        switch ((Character) token.getValue()) {
          case ',':
            parseState = State.BEFORE_ARRAY_VALUE;
            return;

          case ']':
            pop();
        }
        break;
      case END:
        break;
    }
  }

  private void push() {
    Object value;

    switch (token.getType()) {
      case PUNCTUATOR:
        Object token = this.token.getValue();
        switch ((Character) token) {
          case '{':
            value = new JSONObject();
            break;

          case '[':
            value = new JSONArray();
            break;

          default:
            throw new InternalParserError();
        }
        break;
      case NULL:
      case BOOLEAN:
      case NUMERIC:
      case STRING:
        value = this.token.getValue();
        break;
      default:
        throw new InternalParserError();
    }

    if (root == null) {
      root = value;
    } else {
      Object parent = stack.getLast();
      if (parent instanceof JSONArray) {
        ((JSONArray) parent).put(value);
      } else {
        ((JSONObject) parent).put(key, value);
      }
    }

    if (value instanceof JSONArray || value instanceof JSONObject) {
      stack.add(value);
      if (value instanceof JSONArray) {
        parseState = State.BEFORE_ARRAY_VALUE;
      } else {
        parseState = State.BEFORE_PROPERTY_NAME;
      }
    } else {
      try {
        Object current = stack.getLast();
        if (current instanceof JSONArray) {
          parseState = State.AFTER_ARRAY_VALUE;
        } else {
          parseState = State.AFTER_PROPERTY_VALUE;
        }
      } catch (NoSuchElementException e) {
        parseState = State.END;
      }
    }
  }

  private void pop() {
    stack.removeLast();

    try {
      Object current = stack.getLast();
      if (current instanceof JSONArray) {
        parseState = State.AFTER_ARRAY_VALUE;
      } else {
        parseState = State.AFTER_PROPERTY_VALUE;
      }
    } catch (NoSuchElementException ex) {
      parseState = State.END;
    }
  }

  private Token lex() {
    lexState = State.DEFAULT;
    buffer = new StringBuilder();
    doubleQuote = false;
    sign = 1;

    while (true) {
      c = peek();

      token = lexStates(lexState);
      if (token != null) {
        return token;
      }
    }
  }

  private Character read() {
    Character c = peek();

    if (c == null) {
      column++;
    } else if (c == '\n') {
      line++;
      column = 0;
    } else {
      column += 1;
    }
    if (c != null) {
      pos += 1;
    }
    return c;
  }

  private Token lexStates(State state) {
    switch (state) {
      case DEFAULT:

        if (c == null) {
          read();
          return new Token(TokenType.EOF);
        }

        switch (c) {
          case '\t':
            // case '\v':
          case '\f':
          case ' ':
          case '\u00A0':
          case '\uFEFF':
          case '\n':
          case '\r':
          case '\u2028':
          case '\u2029':
            read();
            return null;

          case '/':
            read();
            lexState = State.COMMENT;
            return null;
        }
        if (Util.isSpaceSeparator(c)) {
          read();
          return null;
        }

        return lexStates(parseState);

      case COMMENT:
        switch (c) {
          case '*':
            read();
            lexState = State.MULTI_LINE_COMMENT;
            return null;

          case '/':
            read();
            lexState = State.SINGLE_LINE_COMMENT;
            return null;
        }

        throw invalidChar(read());

      case MULTI_LINE_COMMENT:
        if (c == null) {
          throw invalidChar(read());
        }
        switch (c) {
          case '*':
            read();
            lexState = State.MULTI_LINE_COMMENT_ASTERISK;
            return null;
        }
        read();
        return null;

      case MULTI_LINE_COMMENT_ASTERISK:
        if (c == null) {
          throw invalidChar(read());
        }
        switch (c) {
          case '*':
            read();
            return null;

          case '/':
            read();
            lexState = State.DEFAULT;
            return null;
        }

        read();
        lexState = State.MULTI_LINE_COMMENT;
        return null;

      case SINGLE_LINE_COMMENT:
        if (c == null) {
          read();
          return new Token(TokenType.EOF);
        }

        switch (c) {
          case '\n':
          case '\r':
          case '\u2028':
          case '\u2029':
            read();
            lexState = State.DEFAULT;
            return null;
        }

        read();
        break;

      case VALUE:
        switch (c) {
          case '{':
          case '[':
            return new Token(TokenType.PUNCTUATOR, read());

          case 'n':
            read();
            literal("ull");
            return new Token(TokenType.NULL, null);

          case 't':
            read();
            literal("rue");
            return new Token(TokenType.BOOLEAN, true);

          case 'f':
            read();
            literal("alse");
            return new Token(TokenType.BOOLEAN, false);

          case '-':
          case '+':
            if (read() == '-') {
              sign = -1;
            }

            lexState = State.SIGN;
            return null;

          case '.':
            buffer = new StringBuilder().append(read());
            lexState = State.DECIMAL_POINT_LEADING;
            return null;

          case '0':
            buffer = new StringBuilder().append(read());
            lexState = State.ZERO;
            return null;

          case '1':
          case '2':
          case '3':
          case '4':
          case '5':
          case '6':
          case '7':
          case '8':
          case '9':
            buffer = new StringBuilder().append(read());
            lexState = State.DECIMAL_INTEGER;
            return null;

          case 'I':
            read();
            literal("nfinity");
            return new Token(TokenType.NUMERIC, Double.POSITIVE_INFINITY);

          case 'N':
            read();
            literal("aN");
            return new Token(TokenType.NUMERIC, Double.NaN);

          case '"':
          case '\'':
            doubleQuote = (read() == '\"');
            buffer = new StringBuilder();
            lexState = State.STRING;
            return null;
        }
        throw invalidChar(read());

      case IDENTIFIER_NAME_START_ESCAPE:
      case IDENTIFIER_NAME:
        switch (c) {
          case '$':
          case '_':
          case '\u200C':
          case '\u200D':
            buffer.append(read().charValue());
            return null;

          case '\\':
            read();
            lexState = State.IDENTIFIER_NAME_ESCAPE;
            return null;
        }

        if (Util.isIdContinueChar(c)) {
          buffer.append(read().charValue());
          return null;
        }

        return new Token(TokenType.IDENTIFIER, buffer.toString());

      case IDENTIFIER_NAME_ESCAPE:
        throw new InternalParserError("Unhandled state: " + state.name());

      case SIGN:
        switch (c) {
          case '.':
            buffer = new StringBuilder().append(read());
            lexState = State.DECIMAL_POINT_LEADING;
            return null;

          case '0':
            buffer = new StringBuilder().append(read());
            lexState = State.ZERO;
            return null;

          case '1':
          case '2':
          case '3':
          case '4':
          case '5':
          case '6':
          case '7':
          case '8':
          case '9':
            buffer = new StringBuilder().append(read());
            lexState = State.DECIMAL_INTEGER;
            return null;

          case 'I':
            read();
            literal("nfinity");
            return new Token(TokenType.NUMERIC, sign * Double.POSITIVE_INFINITY);

          case 'N':
            read();
            literal("aN");
            return new Token(TokenType.NUMERIC, Double.NaN);
        }

        throw invalidChar(read());

      case ZERO:
        if (c != null) {
          switch (c) {
            case '.':
              buffer.append(read().charValue());
              lexState = State.DECIMAL_POINT;
              return null;

            case 'e':
            case 'E':
              buffer.append(read().charValue());
              lexState = State.DECIMAL_EXPONENT;
              return null;

            case 'x':
            case 'X':
              buffer.append(read().charValue());
              lexState = State.HEXADECIMAL;
              return null;
          }
        }

        return new Token(TokenType.NUMERIC, sign * 0);
      case DECIMAL_INTEGER:
        if (c != null) {
          switch (c) {
            case '.':
              buffer.append(read().charValue());
              lexState = State.DECIMAL_POINT;
              return null;
            case 'e':
            case 'E':
              buffer.append(read().charValue());
              lexState = State.DECIMAL_EXPONENT;
              return null;
          }

          if (Util.isDigit(c)) {
            buffer.append(read().charValue());
            return null;
          }
        }
        return new Token(TokenType.NUMERIC, sign * parseInt(buffer.toString()));

      case DECIMAL_POINT_LEADING:
        if (c != null) {
          if (Util.isDigit(c)) {
            buffer.append(read().charValue());
            lexState = State.DECIMAL_FRACTION;
            return null;
          }
        }
        throw invalidChar(read());

      case DECIMAL_POINT:
        switch (c) {
          case 'e':
          case 'E':
            buffer.append(read().charValue());
            lexState = State.DECIMAL_EXPONENT;
            return null;
        }

        if (Util.isDigit(c)) {
          buffer.append(read().charValue());
          lexState = State.DECIMAL_FRACTION;
          return null;
        }

        return new Token(TokenType.NUMERIC, sign * parseInt(buffer.toString()));

      case DECIMAL_FRACTION:
        if (c != null) {
          switch (c) {
            case 'e':
            case 'E':
              buffer.append(read().charValue());
              lexState = State.DECIMAL_EXPONENT;
              return null;
          }

          if (Util.isDigit(c)) {
            buffer.append(read().charValue());
            return null;
          }
        }
        return new Token(TokenType.NUMERIC, sign * Double.parseDouble(buffer.toString()));

      case DECIMAL_EXPONENT:
        if (c != null) {
          switch (c) {
            case '+':
            case '-':
              buffer.append(read().charValue());
              lexState = State.DECIMAL_EXPONENT_SIGN;
              return null;
          }

          if (Util.isDigit(c)) {
            buffer.append(read().charValue());
            lexState = State.DECIMAL_EXPONENT_INTEGER;
            return null;
          }
        }
        throw invalidChar(read());

      case DECIMAL_EXPONENT_SIGN:
        if (Util.isDigit(c)) {
          buffer.append(read().charValue());
          lexState = State.DECIMAL_EXPONENT_INTEGER;
          return null;
        }
        throw invalidChar(read());

      case DECIMAL_EXPONENT_INTEGER:
        if (c != null) {
          if (Util.isDigit(c)) {
            buffer.append(read().charValue());
            return null;
          }
        }
        return new Token(TokenType.NUMERIC,
            BigDecimal.valueOf(sign).multiply(new BigDecimal(buffer.toString())));

      case HEXADECIMAL:
        if (Util.isHexDigit(c)) {
          buffer.append(read().charValue());
          lexState = State.HEXADECIMAL_INTEGER;
          return null;
        }
        throw invalidChar(read());

      case HEXADECIMAL_INTEGER:
        if (c != null) {
          if (Util.isHexDigit(c)) {
            buffer.append(read().charValue());
            return null;
          }
        }
        return new Token(TokenType.NUMERIC, sign * parseInt(buffer.substring(2), 16));

      case STRING:
        if (c == null) {
          throw invalidChar(read());
        }
        switch (c) {
          case '\\':
            read();
            buffer.append(escape());
            return null;

          case '"':
            if (doubleQuote) {
              read();
              return new Token(TokenType.STRING, buffer);
            }

            buffer.append(read().charValue());
            return null;

          case '\'':
            if (!doubleQuote) {
              read();
              return new Token(TokenType.STRING, buffer);
            }

            buffer.append(read().charValue());
            return null;

          case '\n':
          case '\r':
            throw invalidChar(read());

          case '\u2028':
          case '\u2029':
            // Invalid ECMAScript.
            break;
        }

        buffer.append(read().charValue());
        break;

      case START:
        switch (c) {
          case '{':
          case '[':
            return new Token(TokenType.PUNCTUATOR, read());
        }

        lexState = State.VALUE;
        break;

      case BEFORE_PROPERTY_NAME:
        switch (c) {
          case '$':
          case '_':
            buffer = new StringBuilder().append(read());
            lexState = State.IDENTIFIER_NAME;
            return null;

          case '\\':
            read();
            lexState = State.IDENTIFIER_NAME_START_ESCAPE;
            return null;

          case '}':
            return new Token(TokenType.PUNCTUATOR, read());

          case '"':
          case '\'':
            doubleQuote = read() == '\"';
            lexState = State.STRING;
            return null;
        }

        if (Util.isIdStartChar(c)) {
          buffer.append(read().charValue());
          lexState = State.IDENTIFIER_NAME;
          return null;
        }

        throw invalidChar(read());

      case AFTER_PROPERTY_NAME:
        if (c == ':') {
          return new Token(TokenType.PUNCTUATOR, read());
        }
        throw invalidChar(read());

      case BEFORE_PROPERTY_VALUE:
        lexState = State.VALUE;
        break;

      case AFTER_PROPERTY_VALUE:
        switch (c) {
          case ',':
          case '}':
            return new Token(TokenType.PUNCTUATOR, read());
        }
        throw invalidChar(read());

      case BEFORE_ARRAY_VALUE:
        if (c == ']') {
          return new Token(TokenType.PUNCTUATOR, read());
        }
        lexState = State.VALUE;
        break;

      case AFTER_ARRAY_VALUE:
        switch (c) {
          case ',':
          case ']':
            return new Token(TokenType.PUNCTUATOR, read());
        }
        throw invalidChar(read());

      case END:
        throw invalidChar(read());
      default:
        throw new InternalParserError("Unknown state: " + state.name());
    }

    return null;
  }

  private void literal(String s) {
    for (char c : s.toCharArray()) {
      Character p = peek();
      if (p != c) {
        throw invalidChar(read());
      }
      read();
    }
  }

  private char escape() {
    Character c = peek();
    switch (c) {
      case 'b':
        read();
        return '\b';

      case 'f':
        read();
        return '\f';

      case 'n':
        read();
        return '\n';

      case 'r':
        read();
        return '\r';

      case 't':
        read();
        return '\t';

      case 'v':
        read();
        return '\013';

      case '0':
        read();
        if (Util.isDigit(peek())) {
          throw invalidChar(read());
        }

        return '\0';

      case 'x':
        read();
        return hexEscape();

      case 'u':
        read();
        return unicodeEscape();

      case '\n':
      case '\u2028':
      case '\u2029':
        read();
        return 0;

      case '\r':
        read();
        if (peek() == '\n') {
          read();
        }

        return 0; // correct?

      case '1':
      case '2':
      case '3':
      case '4':
      case '5':
      case '6':
      case '7':
      case '8':
      case '9':
        throw invalidChar(read());
    }

    return read();
  }

  char hexEscape() {
    StringBuilder buffer = new StringBuilder();
    Character c = peek();

    if (!Util.isHexDigit(c)) {
      throw invalidChar(read());
    }

    buffer.append(read().charValue());

    c = peek();
    if (!Util.isHexDigit(c)) {
      throw invalidChar(read());
    }

    buffer.append(read().charValue());

    return (char) parseInt(buffer.toString(), 16);
  }

  char unicodeEscape() {
    StringBuilder buffer = new StringBuilder();
    int count = 4;

    while (count-- > 0) {
      Character c = peek();
      if (!Util.isHexDigit(c)) {
        throw invalidChar(read());
      }

      buffer.append(read().charValue());
    }

    return (char) parseInt(buffer.toString(), 16);
  }

  private Character peek() {
    try {
      return source.charAt(pos);
    } catch (StringIndexOutOfBoundsException e) {
      return null;
    }
  }

  private SyntaxError invalidChar(Character c) {
    if (c == null) {
      return new SyntaxError("JSON5: invalid end of input at " + line + ":" + column);
    }

    return new SyntaxError(
        "JSON5: invalid character '" + formatChar(c) + "' at " + line + ":" + column);
  }

  private SyntaxError invalidEOF() {
    return new SyntaxError("JSON5: invalid end of input at " + line + ":" + column);
  }

  private enum TokenType { EOF, PUNCTUATOR, NULL, BOOLEAN, NUMERIC, STRING, IDENTIFIER }

  private enum State {
    DEFAULT,
    COMMENT,
    MULTI_LINE_COMMENT,
    MULTI_LINE_COMMENT_ASTERISK,
    SINGLE_LINE_COMMENT,
    VALUE,
    IDENTIFIER_NAME_START_ESCAPE,
    IDENTIFIER_NAME,
    IDENTIFIER_NAME_ESCAPE,
    SIGN,
    ZERO,
    DECIMAL_INTEGER,
    DECIMAL_POINT_LEADING,
    DECIMAL_POINT,
    DECIMAL_FRACTION,
    DECIMAL_EXPONENT,
    DECIMAL_EXPONENT_SIGN,
    DECIMAL_EXPONENT_INTEGER,
    HEXADECIMAL,
    HEXADECIMAL_INTEGER,
    STRING,
    START,
    BEFORE_PROPERTY_NAME,
    AFTER_PROPERTY_NAME,
    BEFORE_PROPERTY_VALUE,
    AFTER_PROPERTY_VALUE,
    BEFORE_ARRAY_VALUE,
    AFTER_ARRAY_VALUE,
    END
  }

  private static class Token {
    private final TokenType type;
    private final Object value;

    Token(TokenType type) {
      this.type = type;
      value = null;
    }

    Token(TokenType type, Object value) {
      this.type = type;
      this.value = value;
    }

    private TokenType getType() {
      return type;
    }

    public Object getValue() {
      return value;
    }
  }
}
