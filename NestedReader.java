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
    private final int LINE_FEED = 10;
    private final char NEW_LINE = '\n';
    private final int NULL = 0;
    StringBuilder buf;
    BufferedReader input;
    Stack<Character> stack;
    int c;

    public NestedReader(BufferedReader input){
        this.input = input;
    }

    public String getNestedString () throws IOException {
        this.buf = new StringBuilder();
        this.stack = new Stack<Character>();
        //c = input.read();
        //while(Character.isValidCodePoint(c))
        for (int c = input.read(); Character.isValidCodePoint(c); c = input.read()) {
            System.out.print((char)c);
            if (c == FORWARD_SLASH) {
                buf.append((char)c);
                c = input.read();
                System.out.print((char)c);
                if (c == FORWARD_SLASH) {
                    buf.append((char)c);
                    continue;
                }
                else {
                    // todo check if it will work
                    return buf.toString();
                }
                // todo add the asterisk (*) feature for block comments
            }
            else if (c == LEFT_BRACKET) {
                stack.push(LEFT_BRACKET);
            }
            else if (c == RIGHT_BRACKET) {
                // todo fix the check
                if (Character.compare(stack.peek().charValue(), LEFT_BRACKET) == 0) {
                    stack.pop();
                }
            }
            else if (c == LEFT_PAREN) {
                stack.push(LEFT_PAREN);
            }
            else if (c == RIGHT_PAREN) {
                if (Character.compare(stack.peek().charValue(), LEFT_PAREN) == 0) {
                    stack.pop();
                }
            }
            else if (c == NEW_LINE) {
                if (stack.isEmpty()) { return buf.toString(); } // todo FIXME
            }
            else if (c == NULL) {

            } else {
                buf.append((char)c);
            }
        }
        System.out.println();
        //System.out.println(buf.toString());

        // todo remove
        return this.buf.toString();
    }
    /*public String getNestedString() throws IOException {
        this.buf = new StringBuilder();
        this.stack = new Stack<>();
        String buffer = null;
        boolean consuming = true;
        boolean printing = false;
        do {
            consume();
            if (!Character.isValidCodePoint(c)) {
                buf = null;
                break;
            }
            switch(c) {
                case '/':
                    consume();
                    if (c == '/') {
                        buf.append((char) c);
                        int t;
                        do {
                            t = input.read();
                        }while( t != 10);
                        c = t;
                    }
                    break;
                case '{':
                    stack.push('}');
                    break;
                case '}':
                    if (!stack.isEmpty()) {
                        if (Character.compare(stack.peek().charValue(), '}') == 0) {
                            stack.pop();
                        }
                    }
                    break;
                case '(':
                    stack.push(new Character(')'));
                    break;
                case ')':
                    if (!stack.isEmpty()) {
                        if (Character.compare(stack.peek().charValue(), ')') == 0) {
                            stack.pop();
                        }
                    }
                    break;
                case '\n':
                    if (stack.isEmpty())
                        consuming = false;
                    buffer = buf.toString();
                    break;
                case 0:
                    consuming = false;
                    break;
                default:
                    break;
            }
        }while (consuming);
        c = 0;


        if (buf != null) {
            buffer = buf.toString();
        }
        return buffer;
    }

    public void consume() throws IOException{
        if (c > 0) {
            buf.append((char) c);
        }
        c = input.read();
    }*/
}

