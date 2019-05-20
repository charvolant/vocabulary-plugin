package au.org.ala.vocabulary

import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * Unit tests for {@link FormatUtilitiesTagLib}
 *
 * @author Doug Palmer &lt;Doug.Palmer@csiro.au&gt;
 * @copyright Copyright &copy; 2018 Atlas of Living Australia
 */
@TestFor(FormatUtilitiesTagLib)
class FormatUtilitiesTagLibSpec extends Specification {

    def 'test fu:jsString 1'() {
        expect:
        applyTemplate('<fu:jsString value="Hello World"/>') == '"Hello World"'
    }

    def 'test fu:jsString 2'() {
        expect:
        applyTemplate('<fu:jsString value="${\'Hello" World\'}"/>') == '"Hello\\" World"'
    }

    def 'test fu:jsString 3'() {
        expect:
        applyTemplate('<fu:jsString value=""/>') == 'null'
    }

    def 'test fu:turtleIri 1'() {
        expect:
        applyTemplate('<fu:turtleIri iri="http://www.ala.org.au/terms/1,0/subgenus"/>') == '<http://www.ala.org.au/terms/1,0/subgenus>'
    }

    def 'test fu:turtleIri 2'() {
        expect:
        applyTemplate('<fu:turtleIri iri="urn:lsid:afd:name:5467"/>') == '<urn:lsid:afd:name:5467>'
    }

}
