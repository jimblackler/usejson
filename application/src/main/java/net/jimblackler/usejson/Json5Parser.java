package net.jimblackler.usejson;

import static java.lang.Integer.parseInt;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class Json5Parser {
  private String source;
  private State parseState;
  private List<Object> stack;
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
    return "unwritten";
  }

  public Object parse(String text) {
    source = text;
    parseState = State.START;
    stack = new ArrayList<>();
    pos = 0;
    line = 1;
    column = 0;
    token = null;
    // key = undefined;
    root = null;
    do {
      token = lex();

      parseStates(parseState);
    } while (token.getType() != TokenType.EOF);

    return root;
  }

  private void parseStates(State state) {
    switch (state) {
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

        if (token.getType() == TokenType.PUNCTUATOR && "]".equals(token.getValue().toString())) {
          pop();
          return;
        }

        push();
        break;

      case AFTER_PROPERTY_VALUE:
        if (token.getType() == TokenType.EOF) {
          throw invalidEOF();
        }

        switch (token.getValue().toString().charAt(0)) {
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

        switch (token.getValue().toString().charAt(0)) {
          case ',':
            parseState = State.BEFORE_ARRAY_VALUE;
            return;

          case ']':
            pop();
        }
        break;
      case END:
        break; // what about the comment about this being unreachable?
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
      Object parent = stack.get(stack.size() - 1);
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
      if (stack.isEmpty()) {
        parseState = State.END;
        return;
      }
      Object current = stack.get(stack.size() - 1);
      if (current instanceof JSONArray) {
        parseState = State.AFTER_ARRAY_VALUE;
      } else {
        parseState = State.AFTER_PROPERTY_VALUE;
      }
    }
  }

  private void pop() {
    stack.remove(stack.size() - 1);

    if (stack.isEmpty()) {
      parseState = State.END;
      return;
    }

    Object current = stack.get(stack.size() - 1);
    if (current instanceof JSONArray) {
      parseState = State.AFTER_ARRAY_VALUE;
    } else {
      parseState = State.AFTER_PROPERTY_VALUE;
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
    Character character = c;
    switch (state) {
      case DEFAULT:

        if (character == null) {
          read();
          return new Token(TokenType.EOF);
        }

        switch (character) {
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
        switch (character) {
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
        if (character == null) {
          throw invalidChar(read());
        }
        switch (character) {
          case '*':
            read();
            lexState = State.MULTI_LINE_COMMENT_ASTERISK;
            return null;
        }
        read();
        return null;

      case MULTI_LINE_COMMENT_ASTERISK:
        if (character == null) {
          throw invalidChar(read());
        }
        switch (character) {
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
        if (character == null) {
          read();
          return new Token(TokenType.EOF);
        }

        switch (character) {
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
        switch (character) {
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
            buffer.append(read());
            return null;

          case '\\':
            read();
            lexState = State.IDENTIFIER_NAME_ESCAPE;
            return null;
        }

        if (Util.isIdContinueChar(c)) {
          buffer.append(read());
          return null;
        }

        return new Token(TokenType.IDENTIFIER, buffer.toString());

      case IDENTIFIER_NAME_ESCAPE:
        throw new InternalParserError("Unhandled state: " + state.name());

      case SIGN:
        switch (character) {
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
        if (character != null) {
          switch (character) {
            case '.':
              buffer.append(read());
              lexState = State.DECIMAL_POINT;
              return null;

            case 'e':
            case 'E':
              buffer.append(read());
              lexState = State.DECIMAL_EXPONENT;
              return null;

            case 'x':
            case 'X':
              buffer.append(read());
              lexState = State.HEXADECIMAL;
              return null;
          }
        }

        return new Token(TokenType.NUMERIC, sign * 0);
      case DECIMAL_INTEGER:
        if (character != null) {
          switch (character) {
            case '.':
              buffer.append(read());
              lexState = State.DECIMAL_POINT;
              return null;
            case 'e':
            case 'E':
              buffer.append(read());
              lexState = State.DECIMAL_EXPONENT;
              return null;
          }

          if (Util.isDigit(character)) {
            buffer.append(read());
            return null;
          }
        }
        return new Token(TokenType.NUMERIC, sign * parseInt(buffer.toString()));

      case DECIMAL_POINT_LEADING:
        if (character != null) {
          if (Util.isDigit(character)) {
            buffer.append(read());
            lexState = State.DECIMAL_FRACTION;
            return null;
          }
        }
        throw invalidChar(read());

      case DECIMAL_POINT:
        switch (character) {
          case 'e':
          case 'E':
            buffer.append(read());
            lexState = State.DECIMAL_EXPONENT;
            return null;
        }

        if (Util.isDigit(character)) {
          buffer.append(read());
          lexState = State.DECIMAL_FRACTION;
          return null;
        }

        return new Token(TokenType.NUMERIC, sign * parseInt(buffer.toString()));

      case DECIMAL_FRACTION:
        if (character != null) {
          switch (character) {
            case 'e':
            case 'E':
              buffer.append(read());
              lexState = State.DECIMAL_EXPONENT;
              return null;
          }

          if (Util.isDigit(character)) {
            buffer.append(read());
            return null;
          }
        }
        return new Token(TokenType.NUMERIC, sign * Double.parseDouble(buffer.toString()));

      case DECIMAL_EXPONENT:
        if (character != null) {
          switch (character) {
            case '+':
            case '-':
              buffer.append(read());
              lexState = State.DECIMAL_EXPONENT_SIGN;
              return null;
          }

          if (Util.isDigit(character)) {
            buffer.append(read());
            lexState = State.DECIMAL_EXPONENT_INTEGER;
            return null;
          }
        }
        throw invalidChar(read());

      case DECIMAL_EXPONENT_SIGN:
        if (Util.isDigit(character)) {
          buffer.append(read());
          lexState = State.DECIMAL_EXPONENT_INTEGER;
          return null;
        }
        throw invalidChar(read());

      case DECIMAL_EXPONENT_INTEGER:
        if (character != null) {
          if (Util.isDigit(character)) {
            buffer.append(read());
            return null;
          }
        }
        return new Token(TokenType.NUMERIC,
            BigDecimal.valueOf(sign).multiply(new BigDecimal(buffer.toString())));

      case HEXADECIMAL:
        if (Util.isHexDigit(character)) {
          buffer.append(read());
          lexState = State.HEXADECIMAL_INTEGER;
          return null;
        }
        throw invalidChar(read());

      case HEXADECIMAL_INTEGER:
        if (character != null) {
          if (Util.isHexDigit(character)) {
            buffer.append(read());
            return null;
          }
        }
        return new Token(TokenType.NUMERIC, sign * parseInt(buffer.substring(2), 16));

      case STRING:
        if (character == null) {
          throw invalidChar(read());
        }
        switch (character) {
          case '\\':
            read();
            buffer.append(escape());
            return null;

          case '"':
            if (doubleQuote) {
              read();
              return new Token(TokenType.STRING, buffer);
            }

            buffer.append(read());
            return null;

          case '\'':
            if (!doubleQuote) {
              read();
              return new Token(TokenType.STRING, buffer);
            }

            buffer.append(read());
            return null;

          case '\n':
          case '\r':
            throw invalidChar(read());

          case '\u2028':
          case '\u2029':
            // Invalid ECMAScript.
            break;
        }

        buffer.append(read());
        break;

      case START:
        switch (character) {
          case '{':
          case '[':
            return new Token(TokenType.PUNCTUATOR, read());
        }

        lexState = State.VALUE;
        break;

      case BEFORE_PROPERTY_NAME:
        switch (character) {
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

        if (Util.isIdStartChar(character)) {
          buffer.append(read());
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
        switch (character) {
          case ',':
          case '}':
            return new Token(TokenType.PUNCTUATOR, read());
        }
        throw invalidChar(read());

      case BEFORE_ARRAY_VALUE:
        if (character == ']') {
          return new Token(TokenType.PUNCTUATOR, read());
        }
        lexState = State.VALUE;
        break;

      case AFTER_ARRAY_VALUE:
        switch (character) {
          case ',':
          case ']':
            return new Token(TokenType.PUNCTUATOR, read());
        }
        throw invalidChar(read());

      case END:
        throw new SyntaxError("Unhandled state: " + state.name());
      default:
        throw new SyntaxError("Unknown state: " + state.name());
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
    Character character = c;
    StringBuilder buffer = new StringBuilder();
    Character c = peek();

    if (!Util.isHexDigit(character)) {
      throw invalidChar(read());
    }

    buffer.append(read());

    c = peek();
    if (!Util.isHexDigit(character)) {
      throw invalidChar(read());
    }

    buffer.append(read());

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

      buffer.append(read());
    }

    return (char) parseInt(buffer.toString(), 16);
  }

  private Character peek() {
    if (pos >= source.length()) {
      return null;
    }
    return source.charAt(pos);
  }

  private SyntaxError invalidChar(Character c) {
    if (c == null) {
      return new SyntaxError("JSON5: invalid end of input at " + line + ":" + column);
    }

    return new SyntaxError(
        "JSON5: invalid character " + formatChar(c) + " of input at " + line + ":" + column);
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
