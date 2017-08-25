/**
 * @author Carlos Aguilera
 * @apiNote  1/28/2016
 * @version 2.0.0
 */
import java.io.*;
import java.util.*;

public class NestedReader {
    private final char LEFT_BRACKET = '{';
    private final char RIGHT_BRACKET = '}';
    private final char LEFT_PAREN = '(';
    private final char RIGHT_PAREN = ')';
    private final char FORWARD_SLASH = '/';
    private final char ASTERISK = '*';
    private final int LINE_FEED = 10;
    private final char NEW_LINE = '\n';
    private final int NULL = 0;
    StringBuilder buf;
    BufferedReader input;
    Stack<Character> stack;
    int c;

    public NestedReader(BufferedReader input) {
        this.input = input;
    }

    public String getNestedString() throws IOException {
        this.buf = new StringBuilder();
        this.stack = new Stack<Character>();
        for (c = input.read(); Character.isValidCodePoint(c); c = input.read()) {
            if (c == FORWARD_SLASH) {
                c = input.read();
                if (c == FORWARD_SLASH) {
                    c = input.read();
                    while (c != LINE_FEED) {
                        c = input.read();
                    }
                } else if (c == ASTERISK) {
                    if (!isBlockComment()) {
                        break;
                    }
                } else {
                    break;
                }
            } else if (c == LEFT_BRACKET) {
                stack.push(LEFT_BRACKET);
                buf.append(LEFT_BRACKET);
            } else if (c == RIGHT_BRACKET) {
                if (Character.compare(stack.peek(), LEFT_BRACKET) == 0) {
                    stack.pop();
                    buf.append(RIGHT_BRACKET);
                }
            } else if (c == LEFT_PAREN) {
                stack.push(LEFT_PAREN);
                buf.append(LEFT_PAREN);
            } else if (c == RIGHT_PAREN) {
                if (Character.compare(stack.peek(), LEFT_PAREN) == 0) {
                    stack.pop();
                    buf.append(RIGHT_PAREN);
                }
            } else if (c == NEW_LINE) {
                if (stack.isEmpty()) {
                    break;
                }
                buf.append(NEW_LINE);
            } else if (c == NULL) {
                break;
            } else {
                buf.append((char) c);
            }
        }
        return this.buf.toString();
    }

    private boolean isBlockComment() throws IOException {
        c = input.read();
        while (c != ASTERISK) {
            c = input.read();
        }
        c = input.read();
        if (c == FORWARD_SLASH) {
            return true;
        } else if (c != FORWARD_SLASH && c != ASTERISK) {
            return false;
        }
        return isBlockComment();
    }
}