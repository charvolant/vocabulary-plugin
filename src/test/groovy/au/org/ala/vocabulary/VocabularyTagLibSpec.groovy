package au.org.ala.vocabulary

import au.org.ala.util.ResourceUtils
import grails.config.Config
import grails.testing.web.taglib.TagLibUnitTest
import org.springframework.boot.bind.YamlConfigurationFactory
import org.yaml.snakeyaml.Yaml
import spock.lang.Specification

/**
 * Unit tests for {@link VocabularyTagLib}
 *
 * @author Doug Palmer &lt;Doug.Palmer@csiro.au&gt;
 * @copyright Copyright &copy; 2018 Atlas of Living Australia
 */
class VocabularyTagLibSpec extends Specification implements TagLibUnitTest<VocabularyTagLib> {
    def setup() {
        def config = new Yaml().load(this.class.getResourceAsStream('voc.yml'))
        grailsApplication.config.merge(config)
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

    def 'test shortCode 1'() {
        given:
        def model = makeLd8()
        expect:
        applyTemplate('<voc:shortCode iri="http://www.w3.org/2004/02/skos/core#Concept"/>', model) == 'skos:Concept'
    }

    def 'test shortCode 2'() {
        given:
        def model = makeLd8()
        expect:
        applyTemplate('<voc:shortCode iri="http://www.ala.org.au"/>', model) == 'http://www.ala.org.au'
    }

    def 'test voc:tagHeader 1'() {
        given:
        tagLib.metaClass.assetPath = { map ->
            "/asset/${map.src}"
        }
        expect:
        applyTemplate('<voc:tagHeader/>') == '<link rel="stylesheet" href="https://voc.ala.org.au/ws/tag/css"/><link rel="stylesheet" href="/asset/vocabulary.css"/><link rel="stylesheet" href="/asset/tags.css"/><script src="https://voc.ala.org.au/ws/tag/js"></script><script src="/asset/tags.js"></script><script type="text/javascript">$(document).ready(function() {  load_tags(\'https://voc.ala.org.au/vocabulary/show\');});</script>'
    }

    def 'test voc:concept 1'() {
        expect:
        applyTemplate('<voc:concept iri="urn:ietf:rfc:2648"/>') == '<span class="tag-holder tag-concept" iri="urn:ietf:rfc:2648">2648</span>'
    }

    def 'test voc:concept 2'() {
        expect:
        applyTemplate('<voc:concept iri="http://www.ala.org.au/format/1.0/style/icon"/>') == '<span class="tag-holder tag-concept" iri="http://www.ala.org.au/format/1.0/style/icon">Icon</span>'
    }

    def 'test voc:concept 3'() {
        expect:
        applyTemplate('<voc:concept concept="TK NV"/>') == '<span class="tag-holder tag-concept" concept="TK NV">TK NV</span>'
    }

    def 'test voc:concept 4'() {
        expect:
        applyTemplate('<voc:concept vocabulary="tkLabels" concept="TK NV"/>') == '<span class="tag-holder tag-concept" vocabulary="tkLabels" concept="TK NV">TK NV</span>'
    }

    def 'test voc:concept 5'() {
        expect:
        applyTemplate('<voc:concept vocabulary="taxonomicStatus"/>') == '<span class="tag-holder tag-concept" vocabulary="taxonomicStatus">Unknown</span>'
    }

    def 'test voc:concept 6'() {
        expect:
        applyTemplate('<voc:concept iri="urn:iso639-1:de" style="language"/>') == '<span class="tag-holder tag-language" iri="urn:iso639-1:de">de</span>'
    }

    def 'test voc:concept 7'() {
        expect:
        applyTemplate('<voc:concept concept="akk" style="language"/>') == '<span class="tag-holder tag-language" concept="akk">akk</span>'
    }

    def 'test voc:concept 8'() {
        expect:
        applyTemplate('<voc:concept iri="http://purl.org/dc/terms/accessRights" style="term"/>') == '<span class="tag-holder tag-term" iri="http://purl.org/dc/terms/accessRights">Access Rights</span>'
    }

    def 'test voc:concept 9'() {
        expect:
        applyTemplate('<voc:concept iri="http://rs.tdwg.org/dwc/terms/occurrenceID" style="term"/>') == '<span class="tag-holder tag-term" iri="http://rs.tdwg.org/dwc/terms/occurrenceID">Occurrence ID</span>'
    }

    def 'test voc:concept 10'() {
        expect:
        applyTemplate('<voc:concept vocabulary="dwc" concept="occurrenceID" style="term"/>') == '<span class="tag-holder tag-term" vocabulary="dwc" concept="occurrenceID">Occurrence ID</span>'
    }

    def 'test voc:label 1'() {
        expect:
        applyTemplate('<voc:label value="A String"/>') == 'A String'
    }

    def 'test voc:label 2'() {
        given:
        def model = makeLd1()
        expect:
        applyTemplate('<voc:label value="${resource}"/>', model) == '<span title="Accepted Title\r' +
                '&lt;http://www.ala.org.au/terms/1.0/taxonomicStatus/accepted&gt;\r' +
                'A description">accepted</span>'
    }

    def 'test voc:label 3'() {
        given:
        def model = makeLd1()
        expect:
        applyTemplate('<voc:label value="${resource}" style="label"/>', model) == '<span title="Accepted Title\r' +
                '&lt;http://www.ala.org.au/terms/1.0/taxonomicStatus/accepted&gt;\r' +
                'A description">accepted</span>'
    }

    def 'test voc:label 4'() {
        given:
        def model = makeLd1()
        expect:
        applyTemplate('<voc:label value="${resource}" style="id"/>', model) == '<span title="Accepted Title\r' +
                '&lt;http://www.ala.org.au/terms/1.0/taxonomicStatus/accepted&gt;\r' +
                'A description">ts:accepted</span>'
    }

    def 'test voc:label 5'() {
        given:
        def model = makeLd1()
        model.context['ts'] = null
        expect:
        applyTemplate('<voc:label value="${resource}" style="id"/>', model) == '<span title="Accepted Title\r' +
                'A description">http://www.ala.org.au/terms/1.0/taxonomicStatus/accepted</span>'
    }

    def 'test voc:label 6'() {
        given:
        def model = makeLd1()
        expect:
        applyTemplate('<voc:label value="${resource}" style="title"/>', model) == '<span title="' +
                '&lt;http://www.ala.org.au/terms/1.0/taxonomicStatus/accepted&gt;\r' +
                'A description">Accepted Title</span>'
    }

    def 'test voc:label 7'() {
        given:
        def model = makeLd1()
        model.resource.remove('dcterms:title')
        expect:
        applyTemplate('<voc:label value="${resource}" style="title"/>', model) == '<span title="' +
                '&lt;http://www.ala.org.au/terms/1.0/taxonomicStatus/accepted&gt;\r' +
                'A description">accepted</span>'
    }

    def 'test voc:label 8'() {
        given:
        def model = makeLd1()
        expect:
        applyTemplate('<voc:label value="${resource}" style="long"/>', model) == '<span title="Accepted Title\r' +
                'A description">http://www.ala.org.au/terms/1.0/taxonomicStatus/accepted</span>'
    }

    def 'test voc:label 9'() {
        given:
        def model = makeLd2()
        expect:
        applyTemplate('<voc:label value="${context[\'http://www.ala.org.au/icon/accepted.png\']}" style="image"/>', model) == '<img width="48" width="24" title="Accepted Icon\r' +
                '&lt;http://www.ala.org.au/icon/accepted.png&gt;" src="http://localhost/accepted.png">'
    }

    def 'test voc:label 10'() {
        given:
        def model = makeLd2()
        model.context['http://www.ala.org.au/icon/accepted.png']['format:asset'] = null
        expect:
        applyTemplate('<voc:label value="${context[\'http://www.ala.org.au/icon/accepted.png\']}" style="image"/>', model) == '<img width="48" width="24" title="Accepted Icon\r' +
                '&lt;http://www.ala.org.au/icon/accepted.png&gt;" src="http://www.ala.org.au/icon/accepted.png">'
    }

    def 'test voc:label 11'() {
        given:
        def model = makeLd2()
        model.context['http://www.ala.org.au/icon/accepted.png']['dcterms:title'] = null
        expect:
        applyTemplate('<voc:label value="${context[\'http://www.ala.org.au/icon/accepted.png\']}" style="image"/>', model) == '<img width="48" width="24" title="&lt;http://www.ala.org.au/icon/accepted.png&gt;" src="http://localhost/accepted.png">'
    }

    def 'test voc:label 12'() {
        given:
        def model = makeLd2()
        expect:
        applyTemplate('<voc:label value="${context[\'http://www.ala.org.au/icon/accepted.png\']}" style="thumbnail"/>', model) == '<img width="48" width="24" title="Accepted Icon\r' +
                '&lt;http://www.ala.org.au/icon/accepted.png&gt;" src="http://localhost/accepted.png">'
    }

    def 'test voc:label 13'() {
        given:
        def model = makeLd2()
        expect:
        applyTemplate('<voc:label value="${context[\'http://www.ala.org.au/icon/accepted.png\']}" style="icon"/>', model) == '<img width="48" width="24" title="Accepted Icon\r' +
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
        applyTemplate('<voc:format value="${resource}"/>', model) == '<a href="https://voc.ala.org.au/vocabulary/show?iri=http%3A%2F%2Fwww.ala.org.au%2Fterms%2F1.0%2FtaxonomicStatus%2Faccepted" class="rdf-resource"><span title="Accepted Title\r' +
                '&lt;http://www.ala.org.au/terms/1.0/taxonomicStatus/accepted&gt;\r' +
                'A description">accepted<span class="glyphicon glyphicon-link"></span></span></a>'
    }

    def 'test voc:format 3'() {
        given:
        def model = makeLd1()
        expect:
        applyTemplate('<voc:format value="${resource}" class="test"/>', model) == '<a href="https://voc.ala.org.au/vocabulary/show?iri=http%3A%2F%2Fwww.ala.org.au%2Fterms%2F1.0%2FtaxonomicStatus%2Faccepted" class="test"><span title="Accepted Title\r' +
                '&lt;http://www.ala.org.au/terms/1.0/taxonomicStatus/accepted&gt;\r' +
                'A description">accepted<span class="glyphicon glyphicon-link"></span></span></a>'
    }

    def 'test voc:format 4'() {
        given:
        def model = makeLd1()
        expect:
        applyTemplate('<voc:format value="${resource}" style="id"/>', model) == '<a href="https://voc.ala.org.au/vocabulary/show?iri=http%3A%2F%2Fwww.ala.org.au%2Fterms%2F1.0%2FtaxonomicStatus%2Faccepted" class="rdf-resource"><span title="Accepted Title\r' +
                '&lt;http://www.ala.org.au/terms/1.0/taxonomicStatus/accepted&gt;\r' +
                'A description">ts:accepted<span class="glyphicon glyphicon-link"></span></span></a>'
    }

    def 'test voc:format 5'() {
        given:
        def model = makeLd2()
        expect:
        applyTemplate('<voc:format value="${context[\'http://www.ala.org.au/icon/accepted.png\']}" style="icon"/>', model) == '<a href="https://voc.ala.org.au/vocabulary/show?iri=http%3A%2F%2Fwww.ala.org.au%2Ficon%2Faccepted.png" class="rdf-resource"><img width="48" width="24" title="Accepted Icon\r' +
                '&lt;http://www.ala.org.au/icon/accepted.png&gt;" src="http://localhost/accepted.png"></a>'
    }

    def 'test voc:format 6'() {
        given:
        def model = makeLd2()
        expect:
        applyTemplate('<voc:format value="${resource}" style="icon"/>', model) == '<a href="https://voc.ala.org.au/vocabulary/show?iri=http%3A%2F%2Fwww.ala.org.au%2Fterms%2F1.0%2FtaxonomicStatus%2Faccepted" class="rdf-resource"><img width="48" width="24" title="Accepted Thing\r' +
                '&lt;http://www.ala.org.au/terms/1.0/taxonomicStatus/accepted&gt;\r' +
                'A descriptive bit" src="http://localhost/accepted.png"></a>'
    }

    def 'test voc:format 7'() {
        given:
        def model = makeLd2()
        model.context = null
        expect:
        applyTemplate('<voc:format value="${resource}"/>', model) == '<a href="https://voc.ala.org.au/vocabulary/show?iri=http%3A%2F%2Fwww.ala.org.au%2Fterms%2F1.0%2FtaxonomicStatus%2Faccepted" class="rdf-resource"><span title="' +
                '&lt;http://www.ala.org.au/terms/1.0/taxonomicStatus/accepted&gt;' +
                '">accepted<span class="glyphicon glyphicon-link"></span></span></a>'
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
        def model = makeLd4()
        expect:
        applyTemplate('<voc:format value="${resource}" language="true"/>', model) == '<span lang="en">A String&nbsp;<span class="tag-holder tag-language" concept="en">en</span></span>'
    }

    def 'test voc:format 11'() {
        given:
        def model = makeLd5()
        expect:
        applyTemplate('<voc:format value="${resource}"/>', model) == '100'
    }

    def 'test voc:format 12'() {
        given:
        def model = makeLd6()
        expect:
        applyTemplate('<voc:format value="${resource}"/>', model) == '2019-01-02T10:00:00+11:00'
    }

    def 'test voc:format 13'() {
        given:
        def model = makeLd7()
        expect:
        applyTemplate('<voc:format value="${resource}"/>', model) == '<ol class="rdf-list"><li>Item 1</li><li>Item 2</li></ol>'
    }

    def 'test voc:format 14'() {
        given:
        def model = makeLd7()
        model.resource.remove(1)
        expect:
        applyTemplate('<voc:format value="${resource}"/>', model) == 'Item 1'
    }

    def 'test voc:isTag 1'() {
        given:
        def model = makeLd8()
        expect:
        applyTemplate('<voc:isTag value="${resource}">I See You</voc:isTag>', model) == ''
    }

    def 'test voc:isTag 2'() {
        given:
        def model = makeLd8()
        model.resource.'rdf:type' << 'format:Concept'
        expect:
        applyTemplate('<voc:isTag value="${resource}">I See You</voc:isTag>', model) == 'I See You'
    }

    def 'test voc:isTag 3'() {
        given:
        def model = makeLd8()
        expect:
        applyTemplate('<voc:isTag value="${resource}" tag="language">I See You</voc:isTag>', model) == ''
    }

    def 'test voc:isTag 4'() {
        given:
        def model = makeLd8()
        model.resource.'rdf:type' << 'format:Concept'
        expect:
        applyTemplate('<voc:isTag value="${resource}" style="language">I See You</voc:isTag>', model) == ''
    }

    def 'test voc:isTag 5'() {
        given:
        def model = makeLd8()
        model.resource.'rdf:type' << 'format:Language'
        expect:
        applyTemplate('<voc:isTag value="${resource}" style="language">I See You</voc:isTag>', model) == 'I See You'
    }

    def 'test voc:isTag 6'() {
        given:
        def model = makeLd8()
        model.resource.'rdf:type' << 'format:Language'
        expect:
        applyTemplate('<voc:isTag value="${resource}" style="http://www.ala.org.au/format/1.0/Language">I See You</voc:isTag>', model) == 'I See You'
    }

    def 'test voc:isTag 7'() {
        given:
        def model = makeLd8()
         expect:
        applyTemplate('<voc:isTag value="${resource}" style="term">I See You</voc:isTag>', model) == ''
    }

    def 'test voc:isTag 8'() {
        given:
        def model = makeLd8()
        model.resource.'rdf:type' << 'format:Term'
        expect:
        applyTemplate('<voc:isTag value="${resource}" style="term">I See You</voc:isTag>', model) == 'I See You'
    }

    def makeContext1() {
        return [
                'ts': 'http://www.ala.org.au/terms/1.0/taxonomicStatus/',
                'rdf:type': ['@id': 'http://www.w3.org/1999/02/22-rdf-syntax-ns#type' ],
                'rdfs:label': ['@id': 'http://www.w3.org/2000/01/rdf-schema#label' ],
                'skos:preLabel':  ['@id': 'http://www.w3.org/2004/02/skos/core#prefLabel' ],
                'dcterms:title': [ '@id': 'http://purl.org/dc/terms/title'],
                'dcterms:description': [ '@id': 'http://purl.org/dc/terms/description'],
                'dc:description': [ '@id': 'http://purl.org/dc/elements/1.1/description' ]
        ]
    }

    def makeContext2() {
        def context = makeContext1()
        context['format:Image'] = [ '@id': 'http://www.ala.org.au/format/1.0/Image']
        context['format:Concept'] = [ '@id': 'http://www.ala.org.au/format/1.0/Concept']
        context['format:icon'] = [ '@id': 'http://www.ala.org.au/format/1.0/icon']
        context['format:width'] = [ '@id': 'http://www.ala.org.au/format/1.0/width']
        context['format:height'] = [ '@id': 'http://www.ala.org.au/format/1.0/height']
        context['format:asset'] = [ '@id': 'http://www.ala.org.au/format/1.0/asset']
        context['http://www.ala.org.au/icon/accepted.png'] = ['@id': 'http://www.ala.org.au/icon/accepted.png', 'dcterms:title': 'Accepted Icon', 'rdf:type': 'format:Image', 'format:width': [ '@value': '48'], 'format:height': [ '@value': '24'], 'format:asset': 'http://localhost/accepted.png']
        return context
    }

    def makeLd1() {
        def resource = [ '@id': 'http://www.ala.org.au/terms/1.0/taxonomicStatus/accepted', 'rdfs:label': 'accepted', 'dcterms:title': 'Accepted Title', 'dc:description': 'A description']
        def model = [resource: resource, context: makeContext1()]
        return model
    }


    def makeLd2() {
        def resource = [ '@id': 'http://www.ala.org.au/terms/1.0/taxonomicStatus/accepted', 'skos:prefLabel': 'accepted', 'dcterms:title': 'Accepted Thing', 'dcterms:description': 'A descriptive bit', 'format:icon': 'http://www.ala.org.au/icon/accepted.png']
        def model = [resource: resource, context: makeContext2()]
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

    def makeLd8() {
        def resource = [ '@id': 'http://www.ala.org.au/terms/1.0/taxonomicStatus/accepted', 'rdfs:label': 'accepted', 'dcterms:title': 'Accepted', 'dcterms:description': 'A description', 'rdf:type': [ 'skos:Concept']]
        def context = makeContext1()
        context['skos:Concept'] = [ '@id': 'http://www.w3.org/2004/02/skos/core#Concept']
        context['format:Concept'] = [ '@id': 'http://www.ala.org.au/format/1.0/Concept']
        context['format:Language'] = [ '@id': 'http://www.ala.org.au/format/1.0/Language']
        context['format:Term'] = [ '@id': 'http://www.ala.org.au/format/1.0/Term']
        def model = [resource: resource, context: context]
        return model
    }

}
