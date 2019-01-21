package au.org.ala.vocabulary

import com.stehno.ersatz.ContentType
import com.stehno.ersatz.Encoders
import com.stehno.ersatz.ErsatzServer
import grails.testing.services.ServiceUnitTest
import groovy.json.JsonSlurper
import spock.lang.AutoCleanup
import spock.lang.Specification

/**
 * Unit tests for {@link VocabularyTagLib}
 *
 * @author Doug Palmer &lt;Doug.Palmer@csiro.au&gt;
 * @copyright Copyright &copy; 2018 Atlas of Living Australia
 */
class VocabularyServiceSpec extends Specification implements ServiceUnitTest<VocabularyService> {
    @AutoCleanup
    ErsatzServer server
    Locale locale

    def setup() {
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

    protected Map getResponse(String resource) {
        JsonSlurper slurper = new JsonSlurper()
        return slurper.parse(this.class.getResource(resource), 'UTF-8')
    }
}
