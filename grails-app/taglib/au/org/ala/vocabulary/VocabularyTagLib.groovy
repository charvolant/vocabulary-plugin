package au.org.ala.vocabulary

import au.org.ala.util.CamelCaseTokenizer
import au.org.ala.util.TitleCapitaliser

class VocabularyTagLib {
    static namespace = "voc"

    static SHORTEN_LENGTH = 100

    /** The IRI for the rdf:type attribute */
    static TYPE = 'http://www.w3.org/1999/02/22-rdf-syntax-ns#type'
    /** The IRI for the format:Tag class */
    static TAG = 'http://www.ala.org.au/format/1.0/Tag'
    /** The IRI for the format:Language class */
    static LANGUAGE= 'http://www.ala.org.au/format/1.0/Language'
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
    /** The IRI for the skos:notation attribute */
    static NOTATION = 'http://www.w3.org/2004/02/skos/core#notation'
    /** The IRI for the skos:notation attribute */
    static IN_SCHEME = 'http://www.w3.org/2004/02/skos/core#inScheme'

    /**
     * Display a resource or value.
     * <p>
     * The possible styles are given by the http://www.ala.org.au/format/1.0/styles vocabulary but can be
     * <ul>
     *     <li><code>label</code> Choose the supplied @label, which is usually the skos:prefLabel or rdfs:label, if not that then the @shortId or the @id</li>
     *     <li><code>id</code> Choose the supplied @shortId, which is a namespaced identifier or the @id</li>
     *     <li><code>title</code> Choose the supplied @title, followed by the @label, @shortId and @id</li>
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
     * @attr context / pageScope.context Used to expand references
     */
    def format = { attrs, body ->
        def context = attrs.context ?: pageScope.context
        def value = attrs.value
        def class_ = attrs.class ?: 'rdf-resource'
        def style = null
        def property = attrs.property

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
                    style = context[pstyle]?.get('@label')
                }
            }
        }
        style = attrs.style ?: style ?: 'label'
        value = context?.get(value) ?: value // Dereference
        if ((value in Map) && value['@id']) { // Contextualised IRI
            out << link(base: grailsApplication.config.vocabulary.server, controller: 'vocabulary', action: 'show', params: [iri: value.'@id'], class: class_) {
                def label = value
                if (isImageStyle(style)) {
                    def icon = contract(ICON, context)
                    if (value[icon]) {
                        def iref = value[icon]
                        iref = context?.get(iref) ?: iref
                        if (iref in Map) {
                            label = iref
                            ['@label', '@title', '@description', '@id'].each { iref[it] = value[it] ?: iref[it] }
                        }
                    }
                }
                voc.label(value: label, style: style, link: true)
            }
        } else if (value instanceof String) { // Uncontextualised IRI
            out << link(controller: 'vocabulary', action: 'show', params: [iri: value], class: class_) {
                voc.label(value: value, style: style, link: true)
            }
        } else if (value in Collection) {
            if (value.size() < 2) {
                value.each { val -> out << voc.format(value: val, class: class_, style: style) }
            } else {
                out << "<ol class=\"rdf-list\">"
                value.each { val ->
                    out << "<li>"
                    out << voc.format(value: val, class: class_, style)
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
            def label = value['@label']
            def shortId = value['@shortId']
            def title = value['@title']
            def description = value['@description']
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
                title = addtitle(title, shorten(description))
            if (isImageStyle(style)) {
                def width = value[contract(WIDTH, context)]?.get('@value') as Integer
                def height = value[contract(HEIGHT, context)]?.get('@value') as Integer
                def asset = value[contract(ASSET, context)]
                def src = asset ?: iri
                out << "<img "
                if (width)
                    out << "width=\"${width}\" "
                if (height)
                    out << "width=\"${height}\" "
                if (title)
                    out << "title=\"${ title.encodeAsHTML() }\" "
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
     */
    def tag = { attrs, body ->
        def iri = attrs.iri
        def vocabulary = attrs.vocabulary
        def concept = attrs.concept
        if (!iri && !vocabulary && !concept)
            concept = 'unknown'
        def text = expandCamelCase(concept ?: buildLocalName(iri) ?: 'unknown')
        out << "<span class=\"tag-holder tag-concept\""
        if (iri)
            out << " iri=\"${iri.encodeAsHTML()}\""
        if (vocabulary)
            out << " vocabulary=\"${vocabulary.encodeAsHTML()}\""
        if (concept)
            out << " concept=\"${concept.encodeAsHTML()}\""
        out << ">${text}</span>"
    }

    /**
     * Generate a tag that describes a language.
     * <p>
     * Languages are assumed to come from a SKOS skos:Concept contained within a
     * skos:ConceptScheme vocabulary, related by skos:inScheme.
     * The term is generally indicated by skos:notation (or skos:prefLabel, rdfs:label).
     * </p>
     * <p>
     * Tags are expanded by the data supplied by the vocabulary service and javascript
     * supplied by tags.js. These are all included in a page by using the {@link #tagHeader}
     * tag.
     * </p>
     * There are three ways of generating a language tag:
     *
     * <ul>
     *     <li>iri only: generates a tag directly from the term IRI</li>
     *     <li>lang only: Looks up a unique term directly (not reliable if there is a term collision)</li>
     * </ul>
     *
     * @attr iri The full language iri
     * @attr lang The language code
     */
    def language = { attrs, body ->
        def iri = attrs.iri
        def lang = attrs.lang
        if (!iri && !lang)
            lang = 'unknown'
        def text = lang ?: buildLocalName(iri) ?: 'unknown'
        out << "<span class=\"language-holder tag-language\""
        if (iri)
            out << " iri=\"${iri.encodeAsHTML()}\""
        if (lang)
            out << " lang=\"${lang.encodeAsHTML()}\""
        out << ">${text}</span>"
    }

    /**
     * Generate a term that describes a data property.
     * <p>
     * Terms are usually derived from an rdfs:Property
     * </p>
     * <p>
     * Terms are expanded by the data supplied by the vocabulary service and javascript
     * supplied by tags.js. These are all included in a page by using the {@link #tagHeader}
     * tag.
     * </p>
     * There are three ways of generating a language tag:
     *
     * <ul>
     *     <li>iri only: generates a tag directly from the term IRI</li>
     *     <li>vocabulary/term: Uses the vocabulary name and the term label</li>
     *     <li>term only: Looks up a unique term directly (not reliable if there is a term collision)</li>
     * </ul>
     *
     * @attr iri The full language iri
     * @attr term The term name
     * @attr locale The locale, defaults to the response locale or request locale
     */
    def term = { attrs, body ->
        def iri = attrs.iri
        def vocabulary = attrs.vocabulary
        def term = attrs.term
        if (!iri && !vocabulary && !term)
            term = 'unknown'
        def text = expandCamelCase(term ?: buildLocalName(iri) ?: 'unknown')
        out << "<span class=\"term-holder tag-term\""
        if (iri)
            out << " iri=\"${iri.encodeAsHTML()}\""
        if (vocabulary)
            out << " vocabulary=\"${vocabulary.encodeAsHTML()}\""
        if (term)
            out << " term=\"${term.encodeAsHTML()}\""
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
     * This looks for ala:Tag in the types.
     *
     * @attr term The term to test (a JSON LD resource)
     * @attr pageScope.context The associated context
     */
    def isTag = { attrs, body ->
        def term = attrs.term
        def context = pageScope.context
        def type = contract(TYPE, context)
        def tag = contract(TAG, context)
        def types = term[type]
        if (types && (types == tag || types.contains(tag))) {
            out << body()
        }
    }

    /**
     * Test to see if a resource is a language. If so, include the body.
     * <p>
     * This looks for ala:Language in the types.
     *
     * @attr term The term to test (a JSON LD resource)
     * @attr pageScope.context The associated context
     */
    def isLanguage = { attrs, body ->
        def term = attrs.term
        def context = pageScope.context
        def type = contract(TYPE, context)
        def tag = contract(LANGUAGE, context)
        def types = term[type]
        if (types && (types == tag || types.contains(tag))) {
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
        out << buildLocalName(attrs.iri)
    }

    /**
     * Shortened text
     *
     * @param text The text to shorten
     * @param length The length to shorten to, not including the ellipsis
     *
     * @return A shortened piece of text or null for empty
     */
    def shorten(String text, int length = SHORTEN_LENGTH) {
        if (!text)
            return null
        if (text.length() < length + 3)
            return text
        int p = length
        while (p > 0 && Character.isLetterOrDigit(text.charAt(p)))
            p--
        if (p <= 0)
            p = SHORTEN_LENGTH
        return text.substring(0, p) + " \u2026"
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
     * Get the local name of a IRI.
     *
     * @param iri
     * @return The identifier piece of an iri, either a fragment, the last part of the path or the final part of a urn
     */
    def buildLocalName(String iri) {
        if (!iri)
            return null
        def p = -1
        if (iri.startsWith('http:') || iri.startsWith('https:')) {
            p = iri.lastIndexOf('#')
            if (p < 0)
                p = iri.lastIndexOf('/')
        }
        if (iri.startsWith('urn:'))
            p = iri.lastIndexOf(':')
        return p < 0 ? iri : iri.substring(p + 1)
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
