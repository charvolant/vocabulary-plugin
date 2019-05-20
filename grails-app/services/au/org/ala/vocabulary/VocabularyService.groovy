package au.org.ala.vocabulary

import au.org.ala.util.ResourceUtils
import grails.config.Config
import grails.core.support.GrailsConfigurationAware
import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse
import groovy.json.JsonSlurper
import org.springframework.boot.json.JsonParserFactory
import org.springframework.http.HttpStatus

/**
 * Service to connect to vocabulary service back-end API.
 */
class VocabularyService implements GrailsConfigurationAware {
    /** The vocabulary server */
    String server
    /** The vocabulary service */
    String service
    String skosConceptScheme
    /** The sources for labels */
    List<List<String>> labelSources
    /** The sources for titles */
    List<List<String>> titleSources
    /** The sources for labels */
    List<List<String>> descriptionSources

    /**
     * Update the configuration
     *
     * @param config
     */
    @Override
    void setConfiguration(Config config) {
        server = config.vocabulary.server
        service = config.vocabulary.service
        labelSources = config.vocabulary.label.sources
        titleSources = config.vocabulary.title.sources
        descriptionSources = config.vocabulary.description.sources
    }

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
     * Search for text in resources
     *
     * @param q The query text
     * @param offset The starting point for the list
     * @param max The maximum number of type to return
     * @param locale The request locale for correct language-specific labelling
     *
     * @return A list of matching resources in JSON-LD form (using the @graph entry for a list, with search:score and search:snippet added)
     */
    def search(String q, int offset, int max, Locale locale) {
        return service("${service}/resource/search?q={q}&offset={offset}&max={max}", [q: q, offset: offset, max: max], locale)
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
     * Get the parents of a SKOS resource.
     *
     * @param iri The resource IRI
     * @param locale The request locale for correct language-specific labelling
     *
     * @return The parent list in the form of a list of lists of parents at each level, highest first
     */
    def getSkosParents(String iri, Locale locale) {
        return service("${service}/skos/parents?iri={iri}", [iri: iri], locale)
    }

    /**
     * Get the children of a SKOS resource.
     *
     * @param iri The resource IRI
     * @param locale The request locale for correct language-specific labelling
     *
     * @return The list of immediate child concepts
     */
    def getSkosChildren(String iri, Locale locale) {
        return service("${service}/skos/children?iri={iri}", [iri: iri], locale)
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

    /**
     * Get a suitable label (short descriptive text) for a resource
     *
     * @param resource The resource
     * @param context The resource context
     * @param locale The preferred locale
     *
     * @return A label, or none for not present
     */
    def getLabel(Map resource, Map context, Locale locale) {
        def label = getText(resource, context, labelSources, locale)
        if (label)
            return label
        def iri = resource['@id']
        def shortId = contract(iri, context)
        if (shortId == iri)
            shortId = namespaced(iri, context)
        if (shortId == iri)
            shortId = null
        return shortId ?: ResourceUtils.localName(iri)
    }

    /**
     * Get a suitable title (short headline text) for a resource
     *
     * @param resource The resource
     * @param context The resource context
     * @param locale The preferred locale
     *
     * @return A title, or none for not present
     */
    def getTitle(Map resource, Map context, Locale locale) {
        return getText(resource, context, titleSources, locale)
    }
    /**
     * Get a suitable description (long descriptive text) for a resource
     *
     * @param resource The resource
     * @param context The resource context
     * @param locale The preferred locale
     *
     * @return A description, or none for not present
     */
    def getDescription(Map resource, Map context, Locale locale) {
        return getText(resource, context, descriptionSources, locale)
    }

    /**
     * Search for a suitable property.
     * <p>
     * Groups of sources are searched in order first for a language tag match, then for a language match and then for a value.
     * If one is not found, then the next group of sources is tried.
     * </p>
     * <p>
     * For example, if the sources are <code>[[dcterms:title, >dc:title]]</code>, the locale is 'fr-CA' and the possible values are
     * <code>dcterms:title = ['chercheuse'@fr, 'researcher' ] dc:title = ['chercheure'@fr-CA ]</code> will result in 'chercheure'.
     * If the sources are <code>[[skos:prefLabel], [rdfs:label]]</code>, the locale is 'fr-CA' and the possible values
     * are <code>skos:prefLabel = ['tofu'@fr, 'tahu'], rdfs:label = ['toffu'@fr-CA ]</code> with result in 'tofu'
     * </p>
     *
     * @param resource The resource
     * @param context The resource context
     * @param sources The sources, a list of lists of
     * @param locale The locale to use
     *
     * @return A suitable value or null for not found
     */
    protected def getText(Map resource, Map context, List<List<String>> sources, Locale locale) {
        if (!resource)
            return ''
        def lt = locale.toLanguageTag()
        def ln = locale.language
        def finders = [ { it in Map && it['@language'] == lt }, { it in Map && it['@language'] == ln }, { it in String || !it['@language'] } ]
        for (List<String> group: sources) {
            for (Closure<Boolean> finder: finders) {
                for (String source : group) {
                    source = contract(source, context)
                    def labels = resource[source]
                    if (!labels)
                        continue
                    def lab = (labels in Iterable) ? labels.find(finder) : (finder(labels) ? labels : null)
                    if (lab)
                        return (lab in Map) ? lab['@value'] : lab
                }
            }
        }
        return null
    }

    /**
     * See if we can go from a full URL to a contracted version with a prefix.
     * <p>
     * Useful if someone has supplied a complete URL for something which, in the context
     * is simplified.
     * <p>
     * The first time this is used, a '@map' value is injected into the context to speed up contractions
     *
     * @param identifier The identifier
     * @param context The context map
     *
     * @return A possible contracted identifier
     */
    def contract(String identifier, Map context) {
        if (!context)
            return identifier
        def map = context['@map']
        if(map == null) {
            synchronized (context) {
                map = context['@map']
                if (map == null) {
                    map = context.inject([:]) { m, k, v ->
                        if (v in String) {
                            m[v] = k
                        } else if (v in Map && v['@id'])
                            m[v['@id']] = k
                        m
                    }
                    context['@map'] = map
                }
            }
        }
        return map[identifier] ?: identifier
    }

    /**
     * See if we can namespace an iri
     *
     * @param identifier The IRI
     * @param context The context, with a namespace map
     *
     * @return Either the namespaced IRI or the unchanged IRI
     */
    def namespaced(String identifier, Map context) {
        def namespace = context.find { entry -> entry.value in String && identifier.startsWith(entry.value) }
        if (namespace) {
            def local = identifier.substring(namespace.value.length())
            if (local && local.matches(/[^\/#:]+/))
                return namespace.key + ':' + local
        }
        return identifier
    }
    /**
     * See if a resource has a particular type
     *
     * @param resource The resource
     * @param context The context
     * @param types The list of matching types
     *
     * @return True if the resource has a type matching any of the supplied types
     */
    boolean hasType(Map resource, Map context, Map types) {
        def valid = types.collect { k, v -> contract(v, context) }
        def rtypes = resource['@type']
        if (!rtypes)
            return false
        return (rtypes in Collection) ? rtypes.any { valid.contains(it) } : valid.contains(rtypes)
    }

    /**
     * See if a resource has a particular type
     *
     * @param resource The resource
     * @param context The context
     * @param type The matching type
     *
     * @return True if the resource has a type matching the supplied type
     */
    boolean hasType(Map resource, Map context, String type) {
        def valid = contract(type, context)
        def rtypes = resource['@type']
        if (!rtypes)
            return false
        return (rtypes in Collection) ? rtypes.any { valid == it } : valid == rtypes
    }

}
