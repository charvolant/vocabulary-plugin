package au.org.ala.util

import au.org.ala.vocabulary.VocabularyTagLib
import grails.testing.web.taglib.TagLibUnitTest
import spock.lang.Specification

/**
 * Unit tests for {@link au.org.ala.util.CamelCaseTokenizer}
 *
 * @author Doug Palmer &lt;Doug.Palmer@csiro.au&gt;
 * @copyright Copyright &copy; 2018 Atlas of Living Australia
 */
class CamelCaseTokenizerSpec extends Specification  {
    def 'test tokenize 1'() {
        when:
        def tokenizer = new CamelCaseTokenizer("something")
        then:
        tokenizer.hasMoreElements()
        tokenizer.nextElement() == 'something'
        !tokenizer.hasMoreElements()
    }

    def 'test tokenize 2'() {
        when:
        def tokenizer = new CamelCaseTokenizer("somethingElse")
        then:
        tokenizer.hasMoreElements()
        tokenizer.nextElement() == 'something'
        tokenizer.hasMoreElements()
        tokenizer.nextElement() == 'Else'
        !tokenizer.hasMoreElements()
    }

    def 'test tokenize 3'() {
        when:
        def tokenizer = new CamelCaseTokenizer("SomethingElseAgain")
        then:
        tokenizer.hasMoreElements()
        tokenizer.nextElement() == 'Something'
        tokenizer.hasMoreElements()
        tokenizer.nextElement() == 'Else'
        tokenizer.hasMoreElements()
        tokenizer.nextElement() == 'Again'
        !tokenizer.hasMoreElements()
    }

    def 'test tokenize 4'() {
        when:
        def tokenizer = new CamelCaseTokenizer("occurrenceID")
        then:
        tokenizer.hasMoreElements()
        tokenizer.nextElement() == 'occurrence'
        tokenizer.hasMoreElements()
        tokenizer.nextElement() == 'ID'
        !tokenizer.hasMoreElements()
    }

    def 'test tokenize 5'() {
        when:
        def tokenizer = new CamelCaseTokenizer("alpha bravo")
        then:
        tokenizer.hasMoreElements()
        tokenizer.nextElement() == 'alpha'
        tokenizer.hasMoreElements()
        tokenizer.nextElement() == 'bravo'
        !tokenizer.hasMoreElements()
    }

    def 'test tokenize 6'() {
        when:
        def tokenizer = new CamelCaseTokenizer("alpha-bravo")
        then:
        tokenizer.hasMoreElements()
        tokenizer.nextElement() == 'alpha'
        tokenizer.hasMoreElements()
        tokenizer.nextElement() == 'bravo'
        !tokenizer.hasMoreElements()
    }

    def 'test tokenize 7'() {
        when:
        def tokenizer = new CamelCaseTokenizer("iso639")
        then:
        tokenizer.hasMoreElements()
        tokenizer.nextElement() == 'iso'
        tokenizer.hasMoreElements()
        tokenizer.nextElement() == '639'
        !tokenizer.hasMoreElements()
    }

    def 'test tokenize 8'() {
        when:
        def tokenizer = new CamelCaseTokenizer("iso639-1.3")
        then:
        tokenizer.hasMoreElements()
        tokenizer.nextElement() == 'iso'
        tokenizer.hasMoreElements()
        tokenizer.nextElement() == '639-1.3'
        !tokenizer.hasMoreElements()
    }

    def 'test tokenize 9'() {
        when:
        def tokenizer = new CamelCaseTokenizer("alpha/bravo")
        then:
        tokenizer.hasMoreElements()
        tokenizer.nextElement() == 'alpha'
        tokenizer.hasMoreElements()
        tokenizer.nextElement() == 'bravo'
        !tokenizer.hasMoreElements()
    }

}
