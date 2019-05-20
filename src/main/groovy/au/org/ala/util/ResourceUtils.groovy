package au.org.ala.util
/**
 * Useful utilities for handling resources and IRIs
 *
 * @author Doug Palmer &lt;Doug.Palmer@csiro.au&gt;
 * @copyright Copyright &copy; 2019 Atlas of Living Australia
 */
class ResourceUtils {
    static SHORTEN_LENGTH = 100

    /**
     * Get the local name of a IRI.
     *
     * @param iri
     * @return The identifier piece of an iri, either a fragment, the last part of the path or the final part of a urn
     */
    static def localName(String iri) {
        if (!iri)
            return null
        def p = -1
        if (iri.startsWith('http:') || iri.startsWith('https:')) {
            p = iri.lastIndexOf('#')
            if (p < 0)
                p = iri.lastIndexOf('/')
        }
        if (iri.startsWith('urn:'))
            p = iri.lastIndexOf(':')
        return p < 0 ? iri : iri.substring(p + 1)
    }


    /**
     * Shortened text
     *
     * @param text The text to shorten
     * @param length The length to shorten to, not including the ellipsis
     *
     * @return A shortened piece of text or null for empty
     */
    static def shorten(String text, int length = SHORTEN_LENGTH) {
        if (!text)
            return null
        if (text.length() < length + 3)
            return text
        int p = length
        while (p > 0 && Character.isLetterOrDigit(text.charAt(p)))
            p--
        if (p <= 0)
            p = SHORTEN_LENGTH
        return text.substring(0, p) + " \u2026"
    }


}
