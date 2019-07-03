package au.org.ala.vocabulary

import com.stehno.ersatz.ContentType
import com.stehno.ersatz.Encoders
import com.stehno.ersatz.ErsatzServer
import grails.test.mixin.TestFor
import groovy.json.JsonSlurper
import org.yaml.snakeyaml.Yaml
import spock.lang.AutoCleanup
import spock.lang.Specification

/**
 * Unit tests for {@link VocabularyTagLib}
 *
 * @author Doug Palmer &lt;Doug.Palmer@csiro.au&gt;
 * @license See LICENSE
 */
@TestFor(VocabularyService)
class VocabularyServiceSpec extends Specification {
    @AutoCleanup
    ErsatzServer server
    Locale locale

    def setupSpec() {
        def config = new Yaml().load(this.class.getResourceAsStream('voc.yml'))
        grailsApplication.config.merge(config)
    }

    def setup() {
        service.setConfiguration(grailsApplication.config) // Grrr
        server = new ErsatzServer()
        server.reportToConsole()
        locale = Locale.UK
    }

    def 'test listTypes 1'() {
        given:
        server.expectations {
            get('/resource/types') {
                query('offset', '0')
                query('max', '5')
                header('Accept-Language', 'en-GB')
                called(1)
                responder {
                    encoder(ContentType.APPLICATION_JSON, Map, Encoders.json)
                    code(200)
                    body(getResponse('list-types-1.json'), ContentType.APPLICATION_JSON)
                }
            }
        }
        when:
        service.service = server.httpUrl
        def json = service.listTypes(0, 5, locale)
        then:
        json
        json['@language'] == 'en'
        json['@count'] == 21
        json['@graph']
        json['@graph'].size() == 5
        json['@context']
        and:
        server.verify()
    }

    def 'test listResources 1'() {
        given:
        server.expectations {
            get('/resource') {
                query('type', 'http://www.ala.org.au/format/1.0/Category')
                query('offset', '0')
                query('max', '3')
                header('Accept-Language', 'en-GB')
                called(1)
                responder {
                    encoder(ContentType.APPLICATION_JSON, Map, Encoders.json)
                    code(200)
                    body(getResponse('list-resources-1.json'), ContentType.APPLICATION_JSON)
                }
            }
        }
        when:
        service.service = server.httpUrl
        def json = service.listResources('http://www.ala.org.au/format/1.0/Category', 0, 3, locale)
        then:
        json
        json['@language'] == 'en'
        json['@count'] == 6
        json['@graph']
        json['@graph'].size() == 3
        json['@context']
        and:
        server.verify()
    }


    def 'test listReferences 1'() {
        given:
        server.expectations {
            get('/resource/references') {
                query('iri', 'http://www.ala.org.au/terms/1.0/taxonomicStatuses')
                query('offset', '0')
                query('max', '3')
                header('Accept-Language', 'en-GB')
                called(1)
                responder {
                    encoder(ContentType.APPLICATION_JSON, Map, Encoders.json)
                    code(200)
                    body(getResponse('list-references-1.json'), ContentType.APPLICATION_JSON)
                }
            }
        }
        when:
        service.service = server.httpUrl
        def json = service.listReferences('http://www.ala.org.au/terms/1.0/taxonomicStatuses', 0, 3, locale)
        then:
        json
        json['@language'] == 'en'
        json['@count'] == 9
        json['@graph']
        json['@graph'].size() == 3
        json['@context']
        and:
        server.verify()
    }

    def 'test search 1'() {
        given:
        server.expectations {
            get('/resource/search') {
                query('q', 'code')
                query('offset', '0')
                query('max', '3')
                header('Accept-Language', 'en-GB')
                called(1)
                responder {
                    encoder(ContentType.APPLICATION_JSON, Map, Encoders.json)
                    code(200)
                    body(getResponse('search-1.json'), ContentType.APPLICATION_JSON)
                }
            }
        }
        when:
        service.service = server.httpUrl
        def json = service.search('code', 0, 3, locale)
        then:
        json
        json['@language'] == 'en'
        json['@count'] == 6
        json['@graph']
        json['@graph'].size() == 3
        json['@context']
        and:
        server.verify()
    }

    def 'test getResource 1'() {
        given:
        server.expectations {
            get('/resource/show') {
                query('iri', 'http://www.ala.org.au/terms/1.0/taxonomicStatuses')
                header('Accept-Language', 'en-GB')
                called(1)
                responder {
                    encoder(ContentType.APPLICATION_JSON, Map, Encoders.json)
                    code(200)
                    body(getResponse('get-resource-1.json'), ContentType.APPLICATION_JSON)
                }
            }
        }
        when:
        service.service = server.httpUrl
        def json = service.getResource('http://www.ala.org.au/terms/1.0/taxonomicStatuses', locale)
        then:
        json
        json['@language'] == 'en'
        json['@id'] == 'http://www.ala.org.au/terms/1.0/taxonomicStatuses'
        json.get('rdfs:title')?.get('@value') == 'Taxonomic Status Vocabulary'
        json['@context']
        and:
        server.verify()
    }


    def 'test contract 1'() {
        given:
        def tag = 'http://www.ala.org.au/format/1.0/Tag'
        def context = [:]
        context['format:Tag'] = ['@id': tag]
        expect:
        service.contract(tag, context) == 'format:Tag'
    }

    def 'test contract 2'() {
        given:
        def tag = 'http://www.ala.org.au/format/1.0/Tag'
        def concept = 'http://www.w3.org/2004/02/skos/core#Concept'
        def context = [:]
        context['format:Tag'] = [ '@id': tag]
        expect:
        service.contract(concept, context) == concept
    }

    def 'test contract 3'() {
        given:
        def model = makeLd9()
        def context = model.context
        expect:
        service.contract('http://www.ala.org.au/terms/1.0/DwCVocabulary', context) == 'ala:DwCVocabulary'
        service.contract('http://rs.tdwg.org/dwc/terms/taxonomicStatus', context) == 'dwc:taxonomicStatus'
        service.contract('http://www.ala.org.au/format/1.0/backgroundColor', context) == 'format:backgroundColor'
        service.contract('http://www.ala.org.au/format/1.0/categories/vocabulary', context) == 'http://www.ala.org.au/format/1.0/categories/vocabulary'
        service.contract('urn:some:random:place', context) == 'urn:some:random:place'
    }

    def 'test getLabel 1'() {
        given:
        def model = makeLd1()
        expect:
        service.getLabel(model.resource, model.context, Locale.ENGLISH) == 'accepted'
    }

    def 'test getLabel 2'() {
        given:
        def model = makeLd1()
        model.resource['rdfs:label'] = [['@value': 'accepted'], ['@value': 'Accepto', '@language': 'en']]
        expect:
        service.getLabel(model.resource, model.context, Locale.ENGLISH) == 'Accepto'
    }

    def 'test getLabel 3'() {
        given:
        def model = makeLd1()
        model.resource['rdfs:label'] = [['@value': 'accepted'], ['@value': 'Accepto', '@language': 'en']]
        expect:
        service.getLabel(model.resource, model.context, Locale.FRENCH) == 'accepted'
    }

    def 'test getLabel 4'() {
        given:
        def model = makeLd1()
        model.resource['rdfs:label'] = [['@value': 'accepted'], ['@value': 'Accepte', '@language': 'fr']]
        expect:
        service.getLabel(model.resource, model.context, Locale.FRENCH) == 'Accepte'
    }

    def 'test getLabel 5'() {
        given:
        def model = makeLd9()
        def context = model.context
        expect:
        service.getLabel(model.resource, context, Locale.ENGLISH) == 'Taxonomic Status Vocabulary'
        service.getLabel(context['dwc:taxonomicStatus'], context, Locale.ENGLISH) == 'taxonomicStatus'
        service.getLabel(context['dwc:taxonomicStatus'], context, Locale.FRENCH) == 'taxonomicStatus'
        service.getLabel(context['ala:forTerm'], context, Locale.ENGLISH) == 'for term'
        service.getLabel(context['ala:forTerm'], context, Locale.FRENCH) == 'for term'
        service.getLabel(context['xsd:string'], context, Locale.ENGLISH) == 'xsd:string'
        service.getLabel(context['xsd:string'], context, Locale.FRENCH) == 'xsd:string'
        service.getLabel(context['http://www.ala.org.au/terms/1.0/taxonomicStatus/accepted'], context, Locale.ENGLISH) == 'Accepted'
        service.getLabel(context['http://www.ala.org.au/terms/1.0/taxonomicStatus/accepted'], context, Locale.FRENCH) == 'accepted'
    }

    def 'test getTitle 1'() {
        given:
        def model = makeLd1()
        expect:
        service.getTitle(model.resource, model.context, Locale.ENGLISH) == 'Accepted Title'
    }

    def 'test getTitle 2'() {
        given:
        def model = makeLd1()
        model.resource['dcterms:title'] = [['@value': 'accepted'], ['@value': 'Accepto', '@language': 'en']]
        expect:
        service.getTitle(model.resource, model.context, Locale.ENGLISH) == 'Accepto'
    }

    def 'test getTitle 3'() {
        given:
        def model = makeLd1()
        model.resource['dcterms:title'] = null
        model.resource['dc:title'] = [['@value': 'accepted'], ['@value': 'Accepto', '@language': 'en']]
        expect:
        service.getTitle(model.resource, model.context, Locale.FRENCH) == 'accepted'
    }

    def 'test getTitle 4'() {
        given:
        def model = makeLd1()
        model.resource['dc:title'] = [['@value': 'accepted'], ['@value': 'Accepte', '@language': 'fr']]
        expect:
        service.getTitle(model.resource, model.context, Locale.FRENCH) == 'Accepte'
    }

    def 'test getTitle 5'() {
        given:
        def model = makeLd9()
        def context = model.context
        expect:
        service.getTitle(model.resource, context, Locale.ENGLISH) == 'Taxonomic Status Vocabulary'
        service.getTitle(context['dwc:taxonomicStatus'], context, Locale.ENGLISH) == null
        service.getTitle(context['dwc:taxonomicStatus'], context, Locale.FRENCH) == null
        service.getTitle(context['ala:forTerm'], context, Locale.ENGLISH) == null
        service.getTitle(context['ala:forTerm'], context, Locale.FRENCH) == null
        service.getTitle(context['xsd:string'], context, Locale.ENGLISH) == null
        service.getTitle(context['xsd:string'], context, Locale.FRENCH) == null
        service.getTitle(context['http://www.ala.org.au/terms/1.0/taxonomicStatus/accepted'], context, Locale.ENGLISH) == 'Accepted'
        service.getTitle(context['http://www.ala.org.au/terms/1.0/taxonomicStatus/accepted'], context, Locale.FRENCH) == null
    }


    protected Map getResponse(String resource) {
        JsonSlurper slurper = new JsonSlurper()
        return slurper.parse(this.class.getResource(resource), 'UTF-8')
    }

    def makeContext1() {
        return [
                'ts': 'http://www.ala.org.au/terms/1.0/taxonomicStatus/',
                'rdf:type': ['@id': 'http://www.w3.org/1999/02/22-rdf-syntax-ns#type' ],
                'rdfs:label': ['@id': 'http://www.w3.org/2000/01/rdf-schema#label' ],
                'skos:preLabel':  ['@id': 'http://www.w3.org/2004/02/skos/core#prefLabel' ],
                'dcterms:title': [ '@id': 'http://purl.org/dc/terms/title'],
                'dcterms:description': [ '@id': 'http://purl.org/dc/terms/description'],
                'dc:title': [ '@id': 'http://purl.org/dc/elements/1.1/title' ],
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
        context['http://www.ala.org.au/icon/accepted.png'] = ['@id': 'http://www.ala.org.au/icon/accepted.png', 'dcterms:title': [ '@value': 'Accepted Icon' ], '@type': 'format:Image', 'format:width': [ '@value': '48'], 'format:height': [ '@value': '24'], 'format:asset': 'http://localhost/accepted.png']
        return context
    }


    def makeLd1() {
        def resource = [ '@id': 'http://www.ala.org.au/terms/1.0/taxonomicStatus/accepted', 'rdfs:label': [ '@value': 'accepted' ], 'dcterms:title': [ '@value': 'Accepted Title' ], 'dc:description': [ '@value': 'A description' ]]
        def model = [resource: resource, context: makeContext1()]
        return model
    }


    def makeLd2() {
        def resource = [ '@id': 'http://www.ala.org.au/terms/1.0/taxonomicStatus/accepted', 'skos:prefLabel': ['@value': 'accepted'], 'dcterms:title': [ '@value': 'Accepted Thing' ], 'dcterms:description': [ '@value': 'A descriptive bit' ], 'format:icon': 'http://www.ala.org.au/icon/accepted.png']
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
        def resource = [ '@id': 'http://www.ala.org.au/terms/1.0/taxonomicStatus/accepted', 'rdfs:label': [ '@value': 'accepted' ], 'dcterms:title': [ '@value': 'Accepted' ], 'dcterms:description': [ '@value': 'A description' ], '@type': [ 'skos:Concept']]
        def context = makeContext1()
        context['skos:Concept'] = [ '@id': 'http://www.w3.org/2004/02/skos/core#Concept']
        context['format:Concept'] = [ '@id': 'http://www.ala.org.au/format/1.0/Concept']
        context['format:Language'] = [ '@id': 'http://www.ala.org.au/format/1.0/Language']
        context['format:Term'] = [ '@id': 'http://www.ala.org.au/format/1.0/Term']
        def model = [resource: resource, context: context]
        return model
    }

    def makeLd9() {
        def resource = new JsonSlurper().parse(this.class.getResource('taxonomic-statuses.json'))
        def model = [resource: resource, context: resource['@context']]
    }

}
