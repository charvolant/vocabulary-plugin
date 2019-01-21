package au.org.ala.vocabulary

import groovy.json.StringEscapeUtils

class FormatUtilitiesTagLib {
    static defaultEncodeAs = [taglib:'none']
    static namespace = "fu"

    /**
     * Encode a value as an escaped java string, or null if the string is empty/null
     *
     * @attr value The value to encode
     */
    def jsString = { attrs, body ->
        String value = attrs.value
        if (!value)
            out << 'null'
        else {
            out << '"'
            StringEscapeUtils.escapeJava(out, value)
            out << '"'
        }
    }

    /**
     * Encode an IRI as a turtle IRI.
     * <p>
     * This encodes the IRI between angle brackets, which can cause some
     * grief when trying to encode an IRI in a GSP.
     * </p>
     *
     * @attr iri The IRI to encode
     */
    def turtleIri = { attrs, body ->
        String iri = attrs.iri
        if (!iri)
            throw new IllegalArgumentException("Expecting valid IRI")
        out << '<'
        out << iri
        out << '>'
    }

}
