package au.org.ala.vocabulary

import grails.config.Config
import grails.core.support.GrailsConfigurationAware
import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse
import groovy.json.JsonSlurper
import org.springframework.http.HttpStatus

/**
 * Service to connect to vocabulary service back-end API.
 */
class VocabularyService implements GrailsConfigurationAware {
    String service

    /**
     * List the types of resource available.
     * Types are RDF classes, given by rdf:type
     *
     * @param offset The starting point for the list
     * @param max The maximum number of type to return
     * @param locale The request locale for correct language-specific labelling
     *
     * @return A list of type resources in JSON-LD form (using the @graph entry for a list)
     */
    def listTypes(int offset, int max, Locale locale) {
        return service("${service}/resource/types?offset={offset}&max={max}", [offset: offset, max: max], locale)
    }

    /**
     * List the resources of a specific type.
     *
     * @param type The type of resource as an IRI
     * @param offset The starting point for the list
     * @param max The maximum number of type to return
     * @param locale The request locale for correct language-specific labelling
     *
     * @return A list of matching resources in JSON-LD form (using the @graph entry for a list)
     */
    def listResources(String type, int offset, int max, Locale locale) {
        return service("${service}/resource?type={type}&offset={offset}&max={max}", [type: type, offset: offset, max: max], locale)
    }

    /**
     * List the references to a specific resource.
     * <p>
     * References are either statmements of the form <code>reference _property iri</code> or
     * <code>reference iri _value</code>
     * </p>
     *
     * @param iri The resource IRI
     * @param offset The starting point for the list
     * @param max The maximum number of type to return
     * @param locale The request locale for correct language-specific labelling
     *
     * @return A list of referencing resources in JSON-LD form (using the @graph entry for a list)
     */
    def listReferences(String iri, int offset, int max, Locale locale) {
        return service("${service}/resource/references?iri={iri}&offset={offset}&max={max}", [iri: iri, offset: offset, max: max], locale)
    }

    /**
     * Get a description of a resource.
     *
     * @param iri The IRI of the resource
     * @param locale The request locale for correct language-specific labelling
     *
     * @return The resource in JSON-LD form
     */
    def getResource(String iri, Locale locale) {
        return service("${service}/resource/show?iri={iri}", [iri: iri], locale)
    }

    /**
     * Call the service
     *
     * @param spec The URI
     * @param locale The request locale for correct language-specific labelling
     *
     * @return The resulting, parsed JSON-LD as a map
     */
    protected service(String spec, Map params, Locale locale) {
        RestBuilder rest = new RestBuilder()
        RestResponse response = rest.get(spec) {
            urlVariables params
            header('Accept-Language', locale.toLanguageTag())
        }
        if (response.statusCode == HttpStatus.OK && response.text) {
            def slurper = new JsonSlurper() // .json method returns deeply annoying JSONObject
            return slurper.parse(new StringReader(response.text))
        }
        throw new IOException("Unable to get ${spec} response ${response.statusCode}")
    }

    @Override
    void setConfiguration(Config co) {
        service = co.getProperty('vocabulary.service', String, 'http://localhost:8080/ws')
    }
}
