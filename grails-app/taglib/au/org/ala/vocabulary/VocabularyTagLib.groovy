package au.org.ala.vocabulary

import au.org.ala.util.CamelCaseTokenizer
import au.org.ala.util.ResourceUtils
import au.org.ala.util.TitleCapitaliser
import grails.config.Config
import grails.core.support.GrailsConfigurationAware

/**
 * Vocabulary tags, used to get data from jason-ld resources.
 * <p>
 * This taglib uses the following conventions
 * <ul>
 *     <li>If a context is not specifically supplied and the page model has <code>pageScope.context</code> then the model conext is used.</li>
 *     <li>If a locale is not specifically then the response locale is used</li>
 *     <li>A <code>@map</code> attribute is injected into any context to provide URL -> term mappings
 * </li>
 */
class VocabularyTagLib implements GrailsConfigurationAware {
    static namespace = "voc"

    def vocabularyService

    /** The format:asset IRI */
    String formatAsset
    /** The format:height IRI */
    String formatHeight
    /** The format:icon IRI */
    String formatIcon
    /** The format:style IRI */
    String formatStyle
    /** The format:width IRI */
    String formatWidth
    /** The skos:Concept type */
    String skosConcept
    /** The skos:ConceptScheme type */
    String skosConceptScheme
    /** The set of types that indicate an image resource */
    Map<String, String> imageTypes
    /** The set of types that indicate a taggable resource */
    Map<String, String> tagTypes

    /**
     * Update the configuration
     *
     * @param config
     */
    @Override
    void setConfiguration(Config config) {
        formatAsset = config.vocabulary.format.asset.iri
        formatHeight = config.vocabulary.format.height.iri
        formatIcon = config.vocabulary.format.icon.iri
        formatStyle = config.vocabulary.format.style.iri
        formatWidth = config.vocabulary.format.width.iri
        imageTypes = config.vocabulary.image.types
        tagTypes = config.vocabulary.tag.types
        skosConcept = config.vocabulary.skos.concept.iri
        skosConceptScheme = config.vocabulary.skos.conceptScheme.iri
    }

    /**
     * Get the short code (namespace:localName) if available associated with an IRI
     *
     * @attr iri The IRI
     * @attr context The context for de-referencing, defaults to pageScope.context
     */
    def shortCode = { attrs, body ->
        def context = attrs.context ?: pageScope.context
        def iri = attrs.iri
        out << vocabularyService.contract(iri, context)
    }

    /**
     * Get the label for a resource or value as text.
     * <p>
     * Called textLabel since the normal label includes a span
     *
     * @attr value The value to get a title for
     * @attr context Used to expand references (defaults to pageScope.context)
     * @attr locale The locale to use (default to response.locale)
     */
    def textLabel = { attrs, body ->
        def context = attrs.context ?: pageScope.context
        def value = attrs.value
        def locale = attrs.locale ?: response.locale
        def label = vocabularyService.getLabel(value, context, locale)
        if (label)
            out << label
    }

    /**
     * Get the title for a resource or value
     *
     * @attr value The value to get a title for
     * @attr context Used to expand references (defaults to pageScope.context)
     * @attr locale The locale to use (default to response.locale)
     */
    def title = { attrs, body ->
        def context = attrs.context ?: pageScope.context
        def value = attrs.value
        def locale = attrs.locale ?: response.locale
        def text = vocabularyService.getTitle(value, context, locale)
        if (text)
            out << text
    }

    /**
     * Get the description for a resource or value
     *
     * @attr value The value to get a title for
     * @attr context Used to expand references (defaults to pageScope.context)
     * @attr locale The locale to use (default to response.locale)
     */
    def description = { attrs, body ->
        def context = attrs.context ?: pageScope.context
        def value = attrs.value
        def locale = attrs.locale ?: response.locale
        def text = vocabularyService.getDescription(value, context, locale)
        if (text)
            out << text
    }

    /**
     * Display a resource or value.
     * <p>
     * The possible styles are given by the http://www.ala.org.au/format/1.0/styles vocabulary but can be
     * <ul>
     *     <li><code>label</code> Choose the supplied label, which is usually the skos:prefLabel or rdfs:label, if not that then the shortId or the @id</li>
     *     <li><code>id</code> Choose the shortId, which is a namespaced identifier or the @id</li>
     *     <li><code>title</code> Choose the supplied title, followed by the label, shortId and @id</li>
     *     <li><code>long</code> Always show the long @id</li>
     *     <li><code>image</code> Display an image as an full-size image
     *     <li><code>thumbnail</code> Display an image as a thumbnail with a link to the full size image
     *     <li><code>icon</code> Display an image as a small icon
     * </ul>
     * <p>
     *
     * @attr value The value to format
     * @attr class The class to render the result as (defaults to rdf-resource)
     * @attr style The style to display resources as, defaults to either a style from the property label
     * @attr property The property associated with this value, used to inherit style parameters
     * @attr language If true and a language-tagged resource other than the locale language, show a language tag (defaults to false)
     * @attr context / pageScope.context Used to expand references
     * @attr locale The locale to display (defaults to response.locale)
     */
    def format = { attrs, body ->
        def context = attrs.context ?: pageScope.context
        def value = attrs.value
        def class_ = attrs.class ?: 'rdf-resource'
        def style = null
        def property = attrs.property
        def language = attrs.language?.toBoolean() ?: false
        def locale = attrs.locale ?: response.locale

        if (!value)
            return
        if (property && context) {
            def sf = context.get(property)
            if (!sf) {
                property = vocabularyService.contract(property, context)
                sf = context.get(property)
            }
            if (sf) {
                def key = vocabularyService.contract(formatStyle, context)
                style = sf[key] ?: style
            }
        }
        style = attrs.style ?: style ?: 'label'
        value = context?.get(value) ?: value // Dereference
        if ((value in Map) && value['@id']) {
            if (value['@type'] == '@id' && value.size() == 2) { // Simple id/type link without additional context
                out << link(url: value['@id'], class: class_) {
                    voc.label(value: value, style: style, link: 'external', locale: locale)
                }
            } else { // Contextualised IRI
                out << link(base: vocabularyService.server, controller: 'vocabulary', action: 'show', params: [iri: value['@id']], class: class_) {
                    voc.label(value: value, style: style, link: 'internal', locale: locale)
                }
            }
        } else if (value instanceof String) { // Uncontextualised IRI
            out << link(url: value, class: class_) {
                voc.label(value: value, style: style, link: 'external', locale: locale)
            }
        } else if (value in Collection) {
            if (value.size() < 2) {
                value.each { val -> out << voc.format(value: val, class: class_, style: style, language: language, locale: locale) }
            } else {
                out << "<ol class=\"rdf-list\">"
                value.each { val ->
                    out << "<li>"
                    out << voc.format(value: val, class: class_, style: style, language: language, locale: locale)
                    out << "</li>"
                }
                out << "</ol>"
            }
        } else {
            def lang = value['@language']
            def val = value['@value'] ?: value.toString()
            if (lang)
                out << "<span lang=\"${lang}\">"
            out << val.encodeAsHTML()
            if (language && lang && !lang.startsWith(locale.language)) {
                out << '&nbsp;'
                out << voc.tag(concept: lang, style: 'language')
            }
            if (lang)
                out << "</span>"
        }
    }

    /**
     * Display a label for a resource or IRI.
     *
     * @attr value The value to display
     * @attr style The style to display resources as using the values of {@link #format}
     * @attr context / pageScope.context Used to expand references
     * @attr link If null dont include a link marker, if 'internal' include an  interbal link marker if 'external' include an external link marker
     * @attr locale The locale (defaults to response.locale)
     */
    def label = { attrs, body ->
        def context = attrs.context ?: pageScope.context
        def value = attrs.value
        def style = attrs.style
        def link = attrs.link
        def locale = attrs.locale ?: response.locale
        def addtitle = { title, add ->
            title ? title + '\r' + add : add
        }
        if (value instanceof String) {
            out << value.encodeAsHTML()
        } else {
            def iri = value['@id']
            def shortId = vocabularyService.contract(iri, context)
            if (shortId == iri)
                shortId = vocabularyService.namespaced(iri, context)
            if (shortId == iri)
                shortId = null
            def label = vocabularyService.getLabel(value, context, locale)
            def title = vocabularyService.getTitle(value, context, locale)
            def description = vocabularyService.getDescription(value, context, locale)
            switch (style) {
                case 'long':
                    if (label && !title)
                        title = label
                    label = iri
                    break
                case 'id':
                    if (label && !title)
                        title = label
                    if (shortId)
                        title = addtitle(title, '<' + iri + '>')
                    label = shortId ?: iri
                    break
                case 'title':
                    label = title ?: label ?: shortId ?: iri
                    title = '<' + iri +'>'
                    break
                default:
                    label = label ?: shortId ?: iri
                    if (label || shortId)
                        title = addtitle(title, '<' + iri + '>')
            }
            if (description)
                title = addtitle(title, ResourceUtils.shorten(description))
            def icon = null
            if (isImageStyle(style)) {
                if (vocabularyService.hasType(value, context, imageTypes)) {
                    icon = value
                } else {
                    def iref = value[vocabularyService.contract(formatIcon, context)]
                    if (iref) {
                        iref = context?.get(iref) ?: iref
                        if (iref in Map) {
                            icon = iref
                        }
                    }
                }
            }
            if (icon) {
                def width = icon[vocabularyService.contract(formatWidth, context)]?.get('@value') as Integer
                def height = icon[vocabularyService.contract(formatHeight, context)]?.get('@value') as Integer
                def asset = icon[vocabularyService.contract(formatAsset, context)]
                def src = asset ?: icon['@id']
                out << "<img "
                if (width)
                    out << "width=\"${width}\" "
                if (height)
                    out << "width=\"${height}\" "
                if (title)
                    out << "title=\"${title.encodeAsHTML()}\" "
                out << "src=\"${src}\">"
            } else {
                if (title) {
                    out << "<span title=\"${title.encodeAsHTML()}\">"
                }
                out << label.encodeAsHTML()
                if (link == 'internal') {
                    out << '<span class="glyphicon glyphicon-link"></span>'
                }
                if (link == 'external') {
                    out << '<span class="glyphicon glyphicon-new-window"></span>'
                }
                if (title) {
                    out << '</span>'
                }
            }
        }
    }

    /**
     * Generate a tag that describes vocabulary concept.
     * <p>
     * Concepts are assumed to come from a SKOS skos:Concept contained within a
     * skos:ConceptScheme vocabulary, related by skos:inScheme.
     * The term is generally indicated by skos:notation (or skos:prefLabel, rdfs:label).
     * The vocabulary can either be indicated by skos:notation/skos:prefLabel/rdfs:label
     * but a vocabulary related to a DwC term by ala:forTerm can just use the term name.
     * eg. <code>taxonomicStatus/synonym</code.
     * </p>
     * <p>
     * Tags are expanded by the data supplied by the vocabulary service and javascript
     * supplied by tags.js. These are all included in a page by using the {@link #tagHeader}
     * tag.
     * </p>
     * There are three ways of generating a tag:
     *
     * <ul>
     *     <li>iri only: generates a tag directly from the concept IRI</li>
     *     <li>vocabulary/concept: Uses the DwC term or vocabulary name and the concept label</li>
     *     <li>concept only: Looks up a unique term directly (not reliable if there is a concept collision)</li>
     * </ul>
     *
     * @attr iri The full tag iri
     * @attr vocabulary The name of the vocabulary/dwc term
     * @attr concept The concept
     * @attr style The display style, either an IRI or concept, language or term. If absent, then a concept is assumed until replaced by the proper style
     * @attr locale The locale to use (defaults to the response.locale)
     */
    def tag = { attrs, body ->
        def iri = attrs.iri
        def vocabulary = attrs.vocabulary
        def concept = attrs.concept
        def style = attrs.style
        def locale = attrs.locale ?: response.locale
        def expand = (attrs.expand ?: true).toBoolean()
        style = tagTypes[style] ?: style
        def text = concept ?: ResourceUtils.localName(iri) ?: 'unknown'
        def clazz = ''
        if (style == tagTypes['concept'])
            clazz = 'tag-concept'
        if (style == tagTypes['language'])
            clazz = 'tag-language'
        else
            text = expandCamelCase(text, locale)
        if (style == tagTypes['term'])
            clazz = 'tag-term'
        if (!iri && !vocabulary && !concept)
            concept = 'unknown'
        out << "<span class=\"tag-holder ${clazz}\""
        if (iri)
            out << " iri=\"${iri.encodeAsHTML()}\""
        if (vocabulary)
            out << " vocabulary=\"${vocabulary.encodeAsHTML()}\""
        if (concept)
            out << " concept=\"${concept.encodeAsHTML()}\""
        if (!expand)
            out << " expand=\"${expand}\""
        out << ">${text}</span>"
    }

    /**
     * Generate the header that includes all the tag machinery
     */
    def tagHeader = { attrs, body ->
        out << "<link rel=\"stylesheet\" href=\"${vocabularyService.service}/tag/css\"/>"
        out << "<link rel=\"stylesheet\" href=\"${assetPath(src: 'vocabulary.css')}\"/>"
        out << "<link rel=\"stylesheet\" href=\"${assetPath(src: 'tags.css')}\"/>"
        out << "<script src=\"${vocabularyService.service}/tag/js\"></script>"
        out << "<script src=\"${assetPath(src: 'tags.js')}\"></script>"
        out << '<script type="text/javascript">'
        out << '$(document).ready(function() {'
        out << "  load_tags('${createLink(base: vocabularyService.server, controller: 'vocabulary', action: 'show', absolute: true)}');"
        out << "});"
        out << '</script>'
    }

    /**
     * Test to see if a resource is a tag. If so, include the body.
     * <p>
     * This looks for format:Tag|format:Language|format:Term depending on the tag attribute in the types.
     *
     * @attr value The resource to test (a JSON LD resource)
     * @attr style Either an IRI or one of 'concept', 'language' or 'term' (defaults to empty, meaning everything)
     * @attr context The associated context defaults to pageScope.context
     */
    def isTag = { attrs, body ->
        def value = attrs.value
        def style = attrs.style
        def context = attrs.context ?: pageScope.context
        style = tagTypes[style] ?: style
        style = style ? [style: style] : tagTypes
        if ( vocabularyService.hasType(value, context, style) ) {
            out << body()
        }
    }

    /**
     * Test to see if a resource is an image. If so, include the body.
     * <p>
     * This looks for format:Tag|format:Language|format:Term depending on the tag attribute in the types.
     *
     * @attr value The resource to test (a JSON LD resource)
     * @attr style Either an IRI or one of 'image'  (defaults to empty, meaning everything)
     * @attr context The associated context defaults to pageScope.context
     */
    def isImage = { attrs, body ->
        def value = attrs.value
        def style = attrs.style
        def context = attrs.context ?: pageScope.context
        style = imageTypes[style] ?: style
        style = style ? [style: style] : imageTypes
        if ( vocabularyService.hasType(value, context, style) ) {
            out << body()
        }
    }

    /**
     * Test to see if a resource is a skos concept. If so, include the body.
     * <p>
     * This looks for skos:Concept in the types.
     *
     * @attr value The resource to test (a JSON LD resource)
     * @attr concept Check for a skos concept (default true)
     * @attr scheme Check for a skos scheme (default true)
     * @attr context The associated context defaults to pageScope.context
     */
    def isSkos = { attrs, body ->
        def value = attrs.value
        boolean isConcept = attrs.containsKey("concept") ? attrs.concept.toBoolean() : true
        boolean isScheme = attrs.containsKey("scheme") ? attrs.scheme.toBoolean() : true
        def context = attrs.context ?: pageScope.context
        if ( (isConcept && vocabularyService.hasType(value, context, skosConcept)) || (isScheme && vocabularyService.hasType(value, context, skosConceptScheme)) ) {
            out << body()
        }
    }

    /**
     * Get a local name for an IRI.
     * <p>
     * The local name is the name without namespace information.
     * For example <code>&lt;voc:localName iri="http://rs.tdwg.org/dwc/terms/scientificName"/&gt;</code> is <code>scienfificName</code>
     * and <code>&lt;voc:localName iri="urn:iso639-3:xul"/&gt;</code> is <code>xule</code>.
     * </p>
     *
     * @attr iri The IRI to convert to a local name
     *
     * @return
     */
    def localName = { attrs, body ->
        out << ResourceUtils.localName(attrs.iri)
    }

    /**
     * Is this an image style
     *
     * @param style The style
     *
     * @return True if the style is an icon, thumbnail or image
     */
    def isImageStyle(style) {
        return style == 'icon' || style == 'thumbnail' || style == 'image'
    }

    /**
     * Un-camel-case a term.
     *
     * @param term The term to un-camel case
     *
     * @return An un camel-scased term
     *
     * @see CamelCaseTokenizer
     */
    String expandCamelCase(String term, Locale locale) {
        TitleCapitaliser capitaliser = TitleCapitaliser.create(locale.language)
        CamelCaseTokenizer tokenizer = new CamelCaseTokenizer(term)
        return capitaliser.capitalise(tokenizer)
    }
}
