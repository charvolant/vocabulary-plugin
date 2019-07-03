package au.org.ala.util

/**
 * A title capitaliser.
 * <p>
 * Titles are capitalised according to the following rules:
 * </p>
 * <ol>
 *     <li>Certain initial letters with an apostrophe (d', O') are replaced with the correct case and the following letter capitalised</li>
 *     <li>The first and last words are always capitalised</li>
 *     <li>Conjunctions (and, or, nor, but, for) are lower case</li>
 *     <li>Articles (a, an, the) are lower case</li>
 *     <li>Prepositions (to, from, by, on, at) are lower case</li>
 * </ul>
 * <p>
 * Capitalisation capitalises anything that occurs after a punctuation symbol.
 * So 'a.j.p.' becomes 'A.J.P.' and 'indo-pacific' becomes 'Indo-Pacific'
 * <p>
 * The terms used for articles, etc. are language-specific and can be specified in the <code>messages</code>
 * resource bundle.
 * </p>
 * <p>
 * Constructing a capitaliser can be a little expensive, so the
 * {@link #create} factory method can be used to get a capitaliser for a language.
 * </p>
 *
 * @author Doug Palmer &lt;Doug.Palmer@csiro.au&gt;
 * @license See LICENSE
 */
class TitleCapitaliser {
    /** The resource bundle to use for word lookups */
    static RESOURCE_BUNDLE = "capitaliser"
    /** The resource bundle entry for conjunctions */
    static CONJUNCTION_RESOURCE = "title.conjunctions"
    /** The resource bundle entry for articless */
    static ARTICLE_RESOURCE = "title.articles"
    /** The resource bundle entry for initial */
    static INITIALS_RESOURCE = "title.initials"
    /** The resource bundle entry for prepositions */
    static PREPOSITION_RESOURCE = "title.prepositions"
    /** The action to take */
    private static ACTION_CAPITALISE = 1
    private static ACTION_LOWERCASE = 2
    private static ACTION_ASIS = 3
    /** Patterns */
    private static LETTER_APOSTROPHE = /\p{L}'\p{L}.*/
    private static ALL_CAPITALS = /\p{Lu}{2,}/

    /** The capitaliser list */
    private static capitializers = [:]

    Locale locale
    Set<String> lowercase
    Set<String> initials

    /**
     * Construct a capitaliser for a locale.
     *
     * @param locale The locale
     */
    TitleCapitaliser(Locale locale) {
        this.locale = locale
        ResourceBundle rb = ResourceBundle.getBundle(RESOURCE_BUNDLE, locale)
        this.lowercase = (rb.getString(CONJUNCTION_RESOURCE).split(',').collect { it.trim().toLowerCase() })
        this.lowercase.addAll(rb.getString(ARTICLE_RESOURCE).split(',').collect { it.trim().toLowerCase() }) as Set
        this.lowercase.addAll(rb.getString(PREPOSITION_RESOURCE).split(',').collect { it.trim().toLowerCase() }) as Set
        this.initials = (rb.getString(INITIALS_RESOURCE).split(',').collect { it.trim() }) as Set
    }

    /**
     * Construct a capitaliser for a language
     *
     * @param language The language (must map onto a locale)
     */
    TitleCapitaliser(String language) {
        this(Locale.forLanguageTag(language))
    }

    /**
     * Capitalize a single word
     *
     * @param word The word to capitalize
     * @param capitalised The buffer to append to
     * @param force Force capitalization
     */
    void capitalise(String word, StringBuffer capitalised, boolean force) {
        String lc = word.toLowerCase()
        int action = ACTION_CAPITALISE
        if (word ==~ LETTER_APOSTROPHE) {
            String pre = word.substring(0, 2)
            String puc = pre.toUpperCase()
            String plc = pre.toLowerCase()
            boolean cap = true
            if (this.initials.contains(puc))
                pre = puc
            else if (this.initials.contains(plc))
                pre = plc
            else
                cap = false
            if (cap) {
                word = pre + word.substring(2, 3).toUpperCase() + word.substring(3)
                action = ACTION_ASIS
            }
        } else if (word ==~ ALL_CAPITALS)
            action = ACTION_ASIS
        else if (force)
            action = ACTION_CAPITALISE
        else if (this.lowercase.contains(lc))
            action = ACTION_LOWERCASE
        if (capitalised.length() > 0)
            capitalised.append(' ')
        switch (action) {
            case ACTION_CAPITALISE:
                boolean cap = true
                for (int j = 0; j < word.length(); j++) {
                    int ch = word.codePointAt(j)
                    capitalised.appendCodePoint(cap ? Character.toUpperCase(ch) : Character.toLowerCase(ch))
                    cap = !Character.isLetterOrDigit(ch) && ch != 0x27 // 0x27 == '
                }
                break
            case ACTION_LOWERCASE:
                capitalised.append(word.toLowerCase())
                break
            default:
                capitalised.append(word)
        }
    }

    /**
     * Capitalise a a likes of words according to the capitaliser rules.
     *
     * @param words The list of words to capitalize
     *
     * @return The capitalised words
     */
    String capitalise(String[] words) {
        StringBuffer capitalised = new StringBuffer(32)
        for (int i = 0; i < words.length; i++) {
            capitalise(words[i], capitalised, i == 0 || i == words.length - 1)
        }
        return capitalised.toString()
    }

    /**
     * Capitalise a a likes of words according to the capitaliser rules.
     *
     * @param words The list of words to capitalize
     *
     * @return The capitalised words
     */
    String capitalise(List<String> words) {
        StringBuffer capitalised = new StringBuffer(32)
        for (int i = 0; i < words.size(); i++) {
            capitalise(words.get(i), capitalised, i == 0 || i == words.size() - 1)
        }
        return capitalised.toString()
    }

    /**
     * Capitalise a a likes of words according to the capitaliser rules.
     *
     * @param words The list of words to capitalize
     *
     * @return The capitalised words
     */
    String capitalise(Enumeration<String> words) {
        StringBuffer capitalised = new StringBuffer(32)
        boolean force = true
        String element = words.hasMoreElements() ? words.nextElement() : null
        while (element != null) {
            capitalise(element, capitalised, force)
            element = words.hasMoreElements() ? words.nextElement() : null
            force = !words.hasMoreElements()
        }
        return capitalised.toString()
    }


    /**
     * Capitalise a title according to the capitaliser rules.
     *
     * @param title The title
     *
     * @return The capitalised title
     */
    String capitalise(String title) {
        return capitalise(title.split('\\s+') as List)
    }

    /**
     * Create a capitaliser.
     *
     * @param lang The language code
     *
     * @return The capitaliser
     */
    synchronized static TitleCapitaliser create(String lang) {
        TitleCapitaliser capitaliser = capitializers.get(lang)
        if (!capitaliser) {
            capitaliser = new TitleCapitaliser(lang)
            capitializers.put(lang, capitaliser)
        }
        return capitaliser
    }
}
