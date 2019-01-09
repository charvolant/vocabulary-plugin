package au.org.ala.vocabulary

import au.org.ala.util.TitleCapitaliser
import grails.testing.web.taglib.TagLibUnitTest
import spock.lang.Specification

/**
 * Unit tests for {@link VocabularyTagLib}
 *
 * @author Doug Palmer &lt;Doug.Palmer@csiro.au&gt;
 * @copyright Copyright &copy; 2018 Atlas of Living Australia
 */
class VocabularyTagLibSpec extends Specification implements TagLibUnitTest<VocabularyTagLib> {
    def setup() {
        grailsApplication.config.vocabulary.service = 'https://voc.ala.org.au/ws'
        grailsApplication.config.vocabulary.server = 'https://voc.ala.org.au'
    }

    def 'test contract 1'() {
        given:
        def tag = 'http://www.ala.org.au/format/1.0/Tag'
        def context = [:]
        context['format:Tag'] = ['@id': tag]
        expect:
        tagLib.contract(tag, context) == 'format:Tag'
    }

    def 'test contract 2'() {
        given:
        def tag = 'http://www.ala.org.au/format/1.0/Tag'
        def concept = 'http://www.w3.org/2004/02/skos/core#Concept'
        def context = [:]
        context['format:Tag'] = [ '@id': tag]
        expect:
        tagLib.contract(concept, context) == concept
    }

    def 'test shorten 1'() {
        given:
        def text = "This is too short"
        expect:
        tagLib.shorten(text) == text
    }

    def 'test shorten 2'() {
        given:
        def text = "This is a lot longer. A lot, lot longer in fact"
        expect:
        tagLib.shorten(text, 12) == text.substring(0, 10) + "…"
    }

    def 'test shorten 3'() {
        given:
        def text = "blah " * 100
        expect:
        tagLib.shorten(text) == text.substring(0, 100) + "…"
    }

    def 'test localName 1'() {
        expect:
        tagLib.buildLocalName('http://www.ala.org.au/format/1.0/style/icon') == 'icon'
    }

    def 'test localName 2'() {
        expect:
        tagLib.buildLocalName('http://www.w3.org/2004/02/skos/core#Concept') == 'Concept'
    }

    def 'test localName 3'() {
        expect:
        tagLib.buildLocalName('urn:ietf:rfc:2648') == '2648'
    }

    def 'test localName 4'() {
        expect:
        tagLib.buildLocalName('') == null
    }

    def 'test localName 5'() {
        expect:
        tagLib.buildLocalName('sweet-william') == 'sweet-william'
    }

    def 'test expandCamelCase 1'() {
        expect:
        tagLib.expandCamelCase('hello') == 'Hello'
    }

    def 'test expandCamelCase 2'() {
        expect:
        tagLib.expandCamelCase('Hello') == 'Hello'
    }

    def 'test expandCamelCase 3'() {
        expect:
        tagLib.expandCamelCase('taxonomicStatus') == 'Taxonomic Status'
    }

    def 'test expandCamelCase 4'() {
        expect:
        tagLib.expandCamelCase('aSpaceShip') == 'A Space Ship'
    }

    def 'test expandCamelCase 5'() {
        expect:
        tagLib.expandCamelCase('enquiryIntoTheCausesOfBiology') == 'Enquiry Into the Causes of Biology'
    }

    def 'test expandCamelCase 6'() {
        given:
        response.locale = Locale.GERMAN // Tends to look up defaults
        expect:
        tagLib.expandCamelCase('depthInMeters') == 'Depth in Meters'
    }

    def 'test expandCamelCase 7'() {
        given:
        response.locale = Locale.FRENCH
        expect:
        tagLib.expandCamelCase('laLumièreDeLaChatEtLeChien') == 'La Lumière de la Chat et le Chien'
    }

    def 'test expandCamelCase 8'() {
        expect:
        tagLib.expandCamelCase('datasetID') == 'Dataset ID'
    }

    def 'test expandCamelCase 9'() {
        expect:
        tagLib.expandCamelCase('iso639') == 'Iso 639'
    }

    def 'test expandCamelCase 10'() {
        expect:
        tagLib.expandCamelCase('iso639-1') == 'Iso 639-1'
    }

    def 'test voc:tagHeader 1'() {
        given:
        tagLib.metaClass.assetPath = { map ->
            "/asset/${map.src}"
        }
        expect:
        applyTemplate('<voc:tagHeader/>') == '<link rel="stylesheet" href="https://voc.ala.org.au/ws/tag/css"/><link rel="stylesheet" href="/asset/vocabulary.css"/><link rel="stylesheet" href="/asset/tags.css"/><script src="https://voc.ala.org.au/ws/tag/js"></script><script src="/asset/tags.js"></script><script type="text/javascript">$(document).ready(function() {  load_tags(\'https://voc.ala.org.au/vocabulary/show\');});</script>'
    }

    def 'test voc:tag 1'() {
        expect:
        applyTemplate('<voc:tag iri="urn:ietf:rfc:2648"/>') == '<span class="tag-holder tag-concept" iri="urn:ietf:rfc:2648">2648</span>'
    }

    def 'test voc:tag 2'() {
        expect:
        applyTemplate('<voc:tag iri="http://www.ala.org.au/format/1.0/style/icon"/>') == '<span class="tag-holder tag-concept" iri="http://www.ala.org.au/format/1.0/style/icon">Icon</span>'
    }

    def 'test voc:tag 3'() {
        expect:
        applyTemplate('<voc:tag concept="TK NV"/>') == '<span class="tag-holder tag-concept" concept="TK NV">TK NV</span>'
    }

    def 'test voc:tag 4'() {
        expect:
        applyTemplate('<voc:tag vocabulary="tkLabels" concept="TK NV"/>') == '<span class="tag-holder tag-concept" vocabulary="tkLabels" concept="TK NV">TK NV</span>'
    }

    def 'test voc:tag 5'() {
        expect:
        applyTemplate('<voc:tag vocabulary="taxonomicStatus"/>') == '<span class="tag-holder tag-concept" vocabulary="taxonomicStatus">Unknown</span>'
    }

    def 'test voc:lang 1'() {
        expect:
        applyTemplate('<voc:language iri="urn:iso639-1:de"/>') == '<span class="language-holder tag-language" iri="urn:iso639-1:de">de</span>'
    }

    def 'test voc:lang 2'() {
        expect:
        applyTemplate('<voc:language iri="http://www.ala.org.au/language/1.0/iso639-3/eng"/>') == '<span class="language-holder tag-language" iri="http://www.ala.org.au/language/1.0/iso639-3/eng">eng</span>'
    }

    def 'test voc:lang 3'() {
        expect:
        applyTemplate('<voc:language lang="akk"/>') == '<span class="language-holder tag-language" lang="akk">akk</span>'
    }

    def 'test voc:term 1'() {
        expect:
        applyTemplate('<voc:term iri="urn:iso639-1:de"/>') == '<span class="term-holder tag-term" iri="urn:iso639-1:de">De</span>'
    }

    def 'test voc:term 2'() {
        expect:
        applyTemplate('<voc:term iri="http://purl.org/dc/terms/accessRights"/>') == '<span class="term-holder tag-term" iri="http://purl.org/dc/terms/accessRights">Access Rights</span>'
    }

    def 'test voc:term 3'() {
        expect:
        applyTemplate('<voc:term iri="http://rs.tdwg.org/dwc/terms/occurrenceID"/>') == '<span class="term-holder tag-term" iri="http://rs.tdwg.org/dwc/terms/occurrenceID">Occurrence ID</span>'
    }

    def 'test voc:term 4'() {
        expect:
        applyTemplate('<voc:term vocabulary="dwc" term="occurrenceID"/>') == '<span class="term-holder tag-term" vocabulary="dwc" term="occurrenceID">Occurrence ID</span>'
    }

    def 'test voc:term 5'() {
        expect:
        applyTemplate('<voc:term term="occurrenceID"/>') == '<span class="term-holder tag-term" term="occurrenceID">Occurrence ID</span>'
    }

    def 'test voc:label 1'() {
        expect:
        applyTemplate('<voc:label value="A String"/>') == 'A String'
    }

    def 'test voc:label 2'() {
        given:
        def model = makeLd1()
        expect:
        applyTemplate('<voc:label value="${resource}"/>', model) == '<span title="Accepted\r' +
                '&lt;http://www.ala.org.au/terms/1.0/taxonomicStatus/accepted&gt;\r' +
                'A description">accepted</span>'
    }

    def 'test voc:label 3'() {
        given:
        def model = makeLd1()
        expect:
        applyTemplate('<voc:label value="${resource}" style="label"/>', model) == '<span title="Accepted\r' +
                '&lt;http://www.ala.org.au/terms/1.0/taxonomicStatus/accepted&gt;\r' +
                'A description">accepted</span>'
    }

    def 'test voc:label 4'() {
        given:
        def model = makeLd1()
        expect:
        applyTemplate('<voc:label value="${resource}" style="id"/>', model) == '<span title="Accepted\r' +
                '&lt;http://www.ala.org.au/terms/1.0/taxonomicStatus/accepted&gt;\r' +
                'A description">ts:accepted</span>'
    }

    def 'test voc:label 5'() {
        given:
        def model = makeLd1()
        model.resource['@shortId'] = null
        expect:
        applyTemplate('<voc:label value="${resource}" style="id"/>', model) == '<span title="Accepted\r' +
                'A description">http://www.ala.org.au/terms/1.0/taxonomicStatus/accepted</span>'
    }

    def 'test voc:label 6'() {
        given:
        def model = makeLd1()
        expect:
        applyTemplate('<voc:label value="${resource}" style="title"/>', model) == '<span title="' +
                '&lt;http://www.ala.org.au/terms/1.0/taxonomicStatus/accepted&gt;\r' +
                'A description">Accepted</span>'
    }

    def 'test voc:label 7'() {
        given:
        def model = makeLd1()
        model.resource['@title'] = null
        expect:
        applyTemplate('<voc:label value="${resource}" style="title"/>', model) == '<span title="' +
                '&lt;http://www.ala.org.au/terms/1.0/taxonomicStatus/accepted&gt;\r' +
                'A description">accepted</span>'
    }

    def 'test voc:label 8'() {
        given:
        def model = makeLd1()
        expect:
        applyTemplate('<voc:label value="${resource}" style="long"/>', model) == '<span title="Accepted\r' +
                'A description">http://www.ala.org.au/terms/1.0/taxonomicStatus/accepted</span>'
    }

    def 'test voc:label 9'() {
        given:
        def model = makeLd2()
        expect:
        applyTemplate('<voc:label value="${context[\'http://www.ala.org.au/icon/accepted.png\']}" style="image"/>', model) == '<img width="48" width="24" title="Accepted\r' +
                '&lt;http://www.ala.org.au/icon/accepted.png&gt;" src="http://localhost/accepted.png">'
    }

    def 'test voc:label 10'() {
        given:
        def model = makeLd2()
        model.context['http://www.ala.org.au/icon/accepted.png']['format:asset'] = null
        expect:
        applyTemplate('<voc:label value="${context[\'http://www.ala.org.au/icon/accepted.png\']}" style="image"/>', model) == '<img width="48" width="24" title="Accepted\r' +
                '&lt;http://www.ala.org.au/icon/accepted.png&gt;" src="http://www.ala.org.au/icon/accepted.png">'
    }

    def 'test voc:label 11'() {
        given:
        def model = makeLd2()
        model.context['http://www.ala.org.au/icon/accepted.png']['@title'] = null
        expect:
        applyTemplate('<voc:label value="${context[\'http://www.ala.org.au/icon/accepted.png\']}" style="image"/>', model) == '<img width="48" width="24" title="&lt;http://www.ala.org.au/icon/accepted.png&gt;" src="http://localhost/accepted.png">'
    }

    def 'test voc:label 12'() {
        given:
        def model = makeLd2()
        expect:
        applyTemplate('<voc:label value="${context[\'http://www.ala.org.au/icon/accepted.png\']}" style="thumbnail"/>', model) == '<img width="48" width="24" title="Accepted\r' +
                '&lt;http://www.ala.org.au/icon/accepted.png&gt;" src="http://localhost/accepted.png">'
    }

    def 'test voc:label 13'() {
        given:
        def model = makeLd2()
        expect:
        applyTemplate('<voc:label value="${context[\'http://www.ala.org.au/icon/accepted.png\']}" style="icon"/>', model) == '<img width="48" width="24" title="Accepted\r' +
                '&lt;http://www.ala.org.au/icon/accepted.png&gt;" src="http://localhost/accepted.png">'
    }

    def 'test voc:format 1'() {
        expect:
        applyTemplate('<voc:format value="${[ \'@value\': \'A String\']}"/>') == 'A String'
    }

    def 'test voc:format 2'() {
        given:
        def model = makeLd1()
        expect:
        applyTemplate('<voc:format value="${resource}"/>', model) == '<a href="https://voc.ala.org.au/vocabulary/show?iri=http%3A%2F%2Fwww.ala.org.au%2Fterms%2F1.0%2FtaxonomicStatus%2Faccepted" class="rdf-resource"><span title="Accepted\r' +
                '&lt;http://www.ala.org.au/terms/1.0/taxonomicStatus/accepted&gt;\r' +
                'A description">accepted<span class="glyphicon glyphicon-link"></span></span></a>'
    }

    def 'test voc:format 3'() {
        given:
        def model = makeLd1()
        expect:
        applyTemplate('<voc:format value="${resource}" class="test"/>', model) == '<a href="https://voc.ala.org.au/vocabulary/show?iri=http%3A%2F%2Fwww.ala.org.au%2Fterms%2F1.0%2FtaxonomicStatus%2Faccepted" class="test"><span title="Accepted\r' +
                '&lt;http://www.ala.org.au/terms/1.0/taxonomicStatus/accepted&gt;\r' +
                'A description">accepted<span class="glyphicon glyphicon-link"></span></span></a>'
    }

    def 'test voc:format 4'() {
        given:
        def model = makeLd1()
        expect:
        applyTemplate('<voc:format value="${resource}" style="id"/>', model) == '<a href="https://voc.ala.org.au/vocabulary/show?iri=http%3A%2F%2Fwww.ala.org.au%2Fterms%2F1.0%2FtaxonomicStatus%2Faccepted" class="rdf-resource"><span title="Accepted\r' +
                '&lt;http://www.ala.org.au/terms/1.0/taxonomicStatus/accepted&gt;\r' +
                'A description">ts:accepted<span class="glyphicon glyphicon-link"></span></span></a>'
    }

    def 'test voc:format 5'() {
        given:
        def model = makeLd2()
        expect:
        applyTemplate('<voc:format value="${context[\'http://www.ala.org.au/icon/accepted.png\']}" style="icon"/>', model) == '<a href="https://voc.ala.org.au/vocabulary/show?iri=http%3A%2F%2Fwww.ala.org.au%2Ficon%2Faccepted.png" class="rdf-resource"><img width="48" width="24" title="Accepted\r' +
                '&lt;http://www.ala.org.au/icon/accepted.png&gt;" src="http://localhost/accepted.png"></a>'
    }

    def 'test voc:format 6'() {
        given:
        def model = makeLd2()
        expect:
        applyTemplate('<voc:format value="${resource}" style="icon"/>', model) == '<a href="https://voc.ala.org.au/vocabulary/show?iri=http%3A%2F%2Fwww.ala.org.au%2Fterms%2F1.0%2FtaxonomicStatus%2Faccepted" class="rdf-resource"><img width="48" width="24" title="Accepted\r' +
                '&lt;http://www.ala.org.au/terms/1.0/taxonomicStatus/accepted&gt;\r' +
                'A description" src="http://localhost/accepted.png"></a>'
    }

    def 'test voc:format 7'() {
        given:
        def model = makeLd2()
        model.context = null
        expect:
        applyTemplate('<voc:format value="${resource}"/>', model) == '<a href="https://voc.ala.org.au/vocabulary/show?iri=http%3A%2F%2Fwww.ala.org.au%2Fterms%2F1.0%2FtaxonomicStatus%2Faccepted" class="rdf-resource"><span title="Accepted\r' +
                '&lt;http://www.ala.org.au/terms/1.0/taxonomicStatus/accepted&gt;\r' +
                'A description">accepted<span class="glyphicon glyphicon-link"></span></span></a>'
    }

    def 'test voc:format 8'() {
        given:
        def model = makeLd3()
        expect:
        applyTemplate('<voc:format value="${resource}"/>', model) == 'A String'
    }

    def 'test voc:format 9'() {
        given:
        def model = makeLd4()
        expect:
        applyTemplate('<voc:format value="${resource}"/>', model) == '<span lang="en">A String</span>'
    }

    def 'test voc:format 10'() {
        given:
        def model = makeLd5()
        expect:
        applyTemplate('<voc:format value="${resource}"/>', model) == '100'
    }

    def 'test voc:format 11'() {
        given:
        def model = makeLd6()
        expect:
        applyTemplate('<voc:format value="${resource}"/>', model) == '2019-01-02T10:00:00+11:00'
    }

    def 'test voc:format 12'() {
        given:
        def model = makeLd7()
        expect:
        applyTemplate('<voc:format value="${resource}"/>', model) == '<ol class="rdf-list"><li>Item 1</li><li>Item 2</li></ol>'
    }

    def 'test voc:format 13'() {
        given:
        def model = makeLd7()
        model.resource.remove(1)
        expect:
        applyTemplate('<voc:format value="${resource}"/>', model) == 'Item 1'
    }

    def makeLd1() {
        def resource = [ '@id': 'http://www.ala.org.au/terms/1.0/taxonomicStatus/accepted', '@shortId': 'ts:accepted', '@label': 'accepted', '@title': 'Accepted', '@description': 'A description']
        def context = [ts: 'http://www.ala.org.au/terms/1.0/taxonomicStatus']
        def model = [resource: resource, context: context]
        return model
    }


    def makeLd2() {
        def resource = [ '@id': 'http://www.ala.org.au/terms/1.0/taxonomicStatus/accepted', '@shortId': 'ts:accepted', '@label': 'accepted', '@title': 'Accepted', '@description': 'A description', 'format:icon': 'http://www.ala.org.au/icon/accepted.png']
        def rdfType = [ '@id': 'http://www.w3.org/1999/02/22-rdf-syntax-ns#type', '@shortId': 'rdf:type']
        def formatIcon = [ '@id': 'http://www.ala.org.au/format/1.0/icon', '@shortId': 'format:icon']
        def formatWidth = [ '@id': 'http://www.ala.org.au/format/1.0/width', '@shortId': 'format:width']
        def formatHeight = [ '@id': 'http://www.ala.org.au/format/1.0/height', '@shortId': 'format:height']
        def formatAsset = [ '@id': 'http://www.ala.org.au/format/1.0/asset', '@shortId': 'format:asset']
        def icon = ['@id': 'http://www.ala.org.au/icon/accepted.png', '@title': 'Accepted', 'rdf:type': 'format:Image', 'format:width': [ '@value': '48'], 'format:height': [ '@value': '24'], 'format:asset': 'http://localhost/accepted.png']
        def context = [ts: 'http://www.ala.org.au/terms/1.0/taxonomicStatus', 'rdf:type': rdfType, 'format:icon': formatIcon, 'format:height': formatHeight, 'format:width': formatWidth,'format:asset': formatAsset, 'http://www.ala.org.au/icon/accepted.png': icon]
        def model = [resource: resource, context: context]
        return model
    }

    def makeLd3() {
        def resource = [ '@value': 'A String' ]
        def context = [:]
        def model = [resource: resource, context: context]
        return model
    }

    def makeLd4() {
        def resource = [ '@value': 'A String', '@language': 'en' ]
        def context = [:]
        def model = [resource: resource, context: context]
        return model
    }

    def makeLd5() {
        def resource = [ '@value': 100 ]
        def context = [:]
        def model = [resource: resource, context: context]
        return model
    }

    def makeLd6() {
        def resource = [ '@value': '2019-01-02T10:00:00+11:00', '@type': ' http://www.w3.org/2001/XMLSchema#dateTime' ]
        def context = [:]
        def model = [resource: resource, context: context]
        return model
    }

    def makeLd7() {
        def resource = [
                [ '@value': 'Item 1'],
                [ '@value': 'Item 2'],
        ]
        def context = [:]
        def model = [resource: resource, context: context]
        return model
    }

}
