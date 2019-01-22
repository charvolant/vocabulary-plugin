package au.org.ala.vocabulary

import au.org.ala.util.CamelCaseTokenizer
import au.org.ala.util.ResourceUtils
import au.org.ala.util.TitleCapitaliser

class VocabularyTagLib {
    static namespace = "voc"

    /** The IRI for the rdf:type attribute */
    static TYPE = 'http://www.w3.org/1999/02/22-rdf-syntax-ns#type'
    /** The IRI for the format:Tag class */
    static CONCEPT = 'http://www.ala.org.au/format/1.0/Concept'
    /** The IRI for the format:Language class */
    static LANGUAGE= 'http://www.ala.org.au/format/1.0/Language'
    /** The IRI for the formatImage class */
    static IMAGE = 'http://www.ala.org.au/format/1.0/Image'
    /** The IRI for the format:Language class */
    static TERM= 'http://www.ala.org.au/format/1.0/Term'
    /** The IRI for the format:style attribute */
    static STYLE = 'http://www.ala.org.au/format/1.0/style'
    /** The IRI for the format:cssClass attribute */
    static CSS_CLASS = 'http://www.ala.org.au/format/1.0/cssClass'
    /** The IRI for the format:height attribute */
    static HEIGHT = 'http://www.ala.org.au/format/1.0/height'
    /** The IRI for the format:width attribute */
    static WIDTH = 'http://www.ala.org.au/format/1.0/width'
    /** The IRI for the format:asset attribute */
    static ASSET = 'http://www.ala.org.au/format/1.0/asset'
    /** The IRI for the format:icon attribute */
    static ICON = 'http://www.ala.org.au/format/1.0/icon'
    /** The IRI for the skos:prefLabel attribute */
    static PREF_LABEL = 'http://www.w3.org/2004/02/skos/core#prefLabel'
    /** The IRI for the rdfs:label attribute */
    static LABEL = 'http://www.w3.org/2000/01/rdf-schema#label'
    /** The IRI for the skos:altLLabel attribute */
    static ALT_LABEL = 'http://www.w3.org/2004/02/skos/core#altLabel'
    /** The IRI for the dcterms:title attribute */
    static TITLE = 'http://purl.org/dc/terms/title'
    /** The IRI for the dc:title attribute */
    static DC_TITLE = 'http://purl.org/dc/elements/1.1/title'
    /** The IRI for the dcterms:description attribute */
    static DESCRIPTION= 'http://purl.org/dc/terms/description'
    /** The IRI for the dc:title attribute */
    static DC_DESCRIPTION = 'http://purl.org/dc/elements/1.1/description'
    /** The IRI for the rdfs:comment attribute */
    static COMMENT = 'http://www.w3.org/2000/01/rdf-schema#comment'
    /** The IRI for the skos:notation attribute */
    static NOTATION = 'http://www.w3.org/2004/02/skos/core#notation'
    /** The IRI for the skos:notation attribute */
    static IN_SCHEME = 'http://www.w3.org/2004/02/skos/core#inScheme'
    
    static TAG_TYPE = [
            concept: CONCEPT,
            language: LANGUAGE,
            term: TERM
    ]

    static LABEL_SOURCES = [
            PREF_LABEL,
            LABEL,
            ALT_LABEL,
            TITLE,
            DC_TITLE,
            NOTATION
    ]

    static TITLE_SOURCES = [
            TITLE,
            DC_TITLE
    ]

    static DESCRIPTION_SOURCES = [
            DESCRIPTION,
            DC_DESCRIPTION,
            COMMENT
    ]

    /**
     * Get the short code (namespace:localName) if available associated with an IRI
     *
     * @attr iri The IRI
     * @attr context The context for de-referencing, defaults to pageScope.context
     */
    def shortCode = { attrs, body ->
        def context = attrs.context ?: pageScope.context
        def iri = attrs.iri
        out << contract(iri, context)
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
     * @attr language If true and a language-tagged resource, show a language tag (defaults to false)
     * @attr context / pageScope.context Used to expand references
     */
    def format = { attrs, body ->
        def context = attrs.context ?: pageScope.context
        def value = attrs.value
        def class_ = attrs.class ?: 'rdf-resource'
        def style = null
        def property = attrs.property
        def language = attrs.language?.toBoolean() ?: false

        if (!value)
            return
        if (property && context) {
            def sf = context.get(property)
            if (!sf) {
                property = contract(property, context)
                sf = context.get(property)
            }
            if (sf) {
                def key = contract(STYLE, context)
                def pstyle = sf[key]
                if (pstyle) {
                    style = getLabel(context[pstyle], context, response.locale)
                }
            }
        }
        style = attrs.style ?: style ?: 'label'
        value = context?.get(value) ?: value // Dereference
        if ((value in Map) && value['@id']) { // Contextualised IRI
            out << link(base: grailsApplication.config.vocabulary.server, controller: 'vocabulary', action: 'show', params: [iri: value['@id']], class: class_) {
                 voc.label(value: value, style: style, link: true)
            }
        } else if (value instanceof String) { // Uncontextualised IRI
            out << link(controller: 'vocabulary', action: 'show', params: [iri: value], class: class_) {
                voc.label(value: value, style: style, link: true)
            }
        } else if (value in Collection) {
            if (value.size() < 2) {
                value.each { val -> out << voc.format(value: val, class: class_, style: style, language: language) }
            } else {
                out << "<ol class=\"rdf-list\">"
                value.each { val ->
                    out << "<li>"
                    out << voc.format(value: val, class: class_, style: style, language: language)
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
            if (language && lang) {
                out << '&nbsp;'
                out << voc.concept(concept: lang, style: 'language')
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
     * @attr link If true include a glyphicon link marker (false by default)
     */
    def label = { attrs, body ->
        def context = attrs.context ?: pageScope.context
        def value = attrs.value
        def style = attrs.style
        def link = (attrs.link ?: false) as Boolean
        def addtitle = { title, add ->
            title ? title + '\r' + add : add
        }
        if (value instanceof String) {
            out << value.encodeAsHTML()
        } else {
            def iri = value['@id']
            def shortId = contract(iri, context)
            if (shortId == iri)
                shortId = namespaced(iri, context)
            if (shortId == iri)
                shortId = null
            def label = getLabel(value, context, response.locale) ?: shortId ?: ResourceUtils.localName(iri)
            def title = getTitle(value, context, response.locale)
            def description = getDescription(value, context, response.locale)
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
                def types = value[contract(TYPE, context)]
                def imagetype = contract(IMAGE, context)
                if (types == imagetype || (types in Collection && types.contains(imagetype))) {
                    icon = value
                } else {
                    def iref = value[contract(ICON, context)]
                    if (iref) {
                        iref = context?.get(iref) ?: iref
                        if (iref in Map) {
                            icon = iref
                        }
                    }
                }
            }
            if (icon) {
                def width = icon[contract(WIDTH, context)]?.get('@value') as Integer
                def height = icon[contract(HEIGHT, context)]?.get('@value') as Integer
                def asset = icon[contract(ASSET, context)]
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
                if (link) {
                    out << '<span class="glyphicon glyphicon-link"></span>'
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
     */
    def concept = { attrs, body ->
        def iri = attrs.iri
        def vocabulary = attrs.vocabulary
        def concept = attrs.concept
        def style = attrs.style
        style = TAG_TYPE[style] ?: style
        def text = concept ?: ResourceUtils.localName(iri) ?: 'unknown'
        def clazz = 'tag-concept'
        if (style == LANGUAGE)
            clazz = 'tag-language'
        if (style == TERM)
            clazz = 'tag-term'
        if (style != LANGUAGE)
            text = expandCamelCase(text)
        if (!iri && !vocabulary && !concept)
            concept = 'unknown'
        out << "<span class=\"tag-holder ${clazz}\""
        if (iri)
            out << " iri=\"${iri.encodeAsHTML()}\""
        if (vocabulary)
            out << " vocabulary=\"${vocabulary.encodeAsHTML()}\""
        if (concept)
            out << " concept=\"${concept.encodeAsHTML()}\""
        out << ">${text}</span>"
    }

    /**
     * Generate the header that includes all the tag machinery
     */
    def tagHeader = { attrs, body ->
        def service = grailsApplication.config.vocabulary.service
        def server = grailsApplication.config.vocabulary.server
        out << "<link rel=\"stylesheet\" href=\"${service}/tag/css\"/>"
        out << "<link rel=\"stylesheet\" href=\"${assetPath(src: 'vocabulary.css')}\"/>"
        out << "<link rel=\"stylesheet\" href=\"${assetPath(src: 'tags.css')}\"/>"
        out << "<script src=\"${service}/tag/js\"></script>"
        out << "<script src=\"${assetPath(src: 'tags.js')}\"></script>"
        out << '<script type="text/javascript">'
        out << '$(document).ready(function() {'
        out << "  load_tags('${createLink(base: server, controller: 'vocabulary', action: 'show', absolute: true)}');"
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
        def type = contract(TYPE, context)
        style = TAG_TYPE[style] ?: style
        style = style ? [contract(style, context)] : TAG_TYPE.values().collect({ contract(it, context) })
        def types = value[type]
        if (types.find { style.contains(it)} ) {
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

    def getLabel(Map resource, Map context, Locale locale) {
        return getText(resource, context, LABEL_SOURCES, locale)
    }

    def getTitle(Map resource, Map context, Locale locale) {
        return getText(resource, context, TITLE_SOURCES, locale)
    }

    def getDescription(Map resource, Map context, Locale locale) {
        return getText(resource, context, DESCRIPTION_SOURCES, locale)
    }

    def getText(Map resource, Map context, List sources, Locale locale) {
        if (!resource)
            return ''
        def lt = locale.toLanguageTag()
        def ln = locale.language
         for (String source: sources) {
            source = contract(source, context)
            def labels = resource[source]
            def label = null
            if (labels in List) {
                label  = labels.find({ it['@language'] == lt }) ?: labels.find({ it['@language'] == ln}) ?: labels.find({ !it['@language'] }) ?: labels[0]
            } else
                label = labels
            if (label)
                return label
        }
        return null
    }

    /**
     * See if we can go from a full URL to a contracted version with a prefix.
     * <p>
     * Useful if someone has supplied a complete URL for something which, in the context
     * is simplified.
     *
     * @param identifier The identifier
     * @param context The context map
     *
     * @return A possible contracted identifier
     */
    def contract(String identifier, Map context) {
        def definition = context.find { entry -> entry.value in Map && entry.value['@id'] == identifier }
        return definition?.key ?: identifier
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
    String expandCamelCase(String term) {
        def locale = response.locale ?: request.locale ?: Locale.default
        TitleCapitaliser capitaliser = TitleCapitaliser.create(locale.language)
        CamelCaseTokenizer tokenizer = new CamelCaseTokenizer(term)
        return capitaliser.capitalise(tokenizer)
    }
}
