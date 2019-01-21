package au.org.ala.util

import spock.lang.Specification

/**
 * Test cases for {@link ResourceUtils}
 *
 * @author Doug Palmer &lt;Doug.Palmer@csiro.au&gt;
 * @copyright Copyright &copy; 2017 Atlas of Living Australia
 */
class ResourceUtilsSpec extends Specification {

    def 'test localName 1'() {
        expect:
        ResourceUtils.localName('http://www.ala.org.au/format/1.0/style/icon') == 'icon'
    }

    def 'test localName 2'() {
        expect:
        ResourceUtils.localName('http://www.w3.org/2004/02/skos/core#Concept') == 'Concept'
    }

    def 'test localName 3'() {
        expect:
        ResourceUtils.localName('urn:ietf:rfc:2648') == '2648'
    }

    def 'test localName 4'() {
        expect:
        ResourceUtils.localName('') == null
    }

    def 'test localName 5'() {
        expect:
        ResourceUtils.localName('sweet-william') == 'sweet-william'
    }

    def 'test shorten 1'() {
        given:
        def text = "This is too short"
        expect:
        ResourceUtils.shorten(text) == text
    }

    def 'test shorten 2'() {
        given:
        def text = "This is a lot longer. A lot, lot longer in fact"
        expect:
        ResourceUtils.shorten(text, 12) == text.substring(0, 10) + "…"
    }

    def 'test shorten 3'() {
        given:
        def text = "blah " * 100
        expect:
        ResourceUtils.shorten(text) == text.substring(0, 100) + "…"
    }


}
