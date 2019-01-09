package au.org.ala.util
/**
 * Parse a camel-case word into a stream of tokens
 * <p>
 * Parsing follows the following rules:
 * </p>
 * <ul>
 *     <li>Words are split by capital letters, spaces or punctuation<li>
 *     <li>Unless there is a sequence of capital letters</li>
 *     <li>Or a sequence of digits and puntuation</li>
 * </li>
 *
 * @author Doug Palmer &lt;Doug.Palmer@csiro.au&gt;
 * @copyright Copyright &copy; 2019 Atlas of Living Australia
 */
class CamelCaseTokenizer implements Enumeration<String> {
    protected static DASH = '-' as char
    protected static PERIOD = '.' as char

    /** The input camel case */
    Reader input
    /** The current character */
    int ch
    /** The parser state */
    State state
    /** The next word */
    StringBuilder word

    /**
     * Construct a parser from a reader.
     *
     * @param input The reader
     */
    CamelCaseTokenizer(Reader input) {
        this.input = input
        this.ch = input.read()
        this.word = new StringBuilder(16)
        this.state = State.START
        parseElement()
    }

    /**
     * Construct a parser for a string
     *
     * @param input
     */
    CamelCaseTokenizer(String input) {
        this(new StringReader(input))
    }

    /**
     * See if there are any more elements to return
     *
     * @return True if all elements have been parsed
     */
    @Override
    boolean hasMoreElements() {
        return ch != -1 || word.length() > 0
    }

    /**
     * Get the next camel case token
     *
     * @return The next token
     */
    @Override
    String nextElement() {
        if (!hasMoreElements())
            throw new NoSuchElementException("At end of input")
        def element = word.toString()
        word.setLength(0)
        parseElement()
        return element
    }

    protected void skipConnective() {
        while (ch != -1 && !Character.isLetterOrDigit(ch))
            ch = input.read()
    }

    protected void parseElement() {
        skipConnective()
        while (ch != -1) {
            switch (state) {
                case State.START:
                    word.appendCodePoint(ch)
                    int old = ch
                    ch = input.read()
                    if (ch != -1) {
                        if (Character.isUpperCase(old) && Character.isUpperCase(ch))
                            state = State.START
                        else if (Character.isDigit(old))
                            state = State.NUMBER
                        else if (Character.isLetter(old))
                            state = State.WORD
                    }
                    break
                case State.WORD:
                    if (Character.isLowerCase(ch)) {
                        word.appendCodePoint(ch)
                        ch = input.read()
                    } else {
                        state = State.START
                        return
                    }
                    break
                case State.NUMBER:
                    if (Character.isDigit(ch) || ch == DASH || ch == PERIOD) {
                        word.appendCodePoint(ch)
                        ch = input.read()
                    } else {
                        state = State.START
                        return
                    }
                    break
            }
        }
    }

    enum State {
        START,
        WORD,
        NUMBER
    }
}
