/* Scripts to handle tags */

function replace_tag(element, base) {
    var vocabulary = element.attr('vocabulary');
    var concept = element.attr('concept');
    var iri = element.attr('iri');
    var data = null;
    if (iri)
        data = VOCABULARY_LOOKUP_ID[iri];
    else if (vocabulary) {
        var vd = VOCABULARY_LOOKUP_VOC[vocabulary];
        if (vd)
            data = vd[concept];
    }
    if (!data)
        data = VOCABULARY_LOOKUP_TERM[concept];
    if (!data)
        return;
    var href = base + '?iri=' + encodeURIComponent(data.id);
    var title = data.label + '\n<' + data.id + '>';
    if (data.title && data.title != data.label)
        title = title + '\n' + data.title;
    if (data.description)
        title = title + '\n' + data.description;
    var html = '<a href="' + href + '" class="' + data.css + '" title="' + title + '">';
    html = html + '<span about="' + encodeURI(data.id) + '">';
    html = html + data.label;
    html = html + '</span></a>';
    element.replaceWith(html)
}

function replace_language(element, base) {
    var lang = element.attr('lang');
    var iri = element.attr('iri');
    var data = null;
    if (iri)
        data = VOCABULARY_LOOKUP_ID[iri];
    if (!data)
        data = VOCABULARY_LOOKUP_TERM[term];
    if (!data)
        return;
    var href = base + '?iri=' + encodeURIComponent(data.id);
    var title = data.label + '\n<' + data.id + '>';
    if (data.title && data.title != data.label)
        title = title + '\n' + data.title;
    if (data.description)
        title = title + '\n' + data.description;
    var html = '<a href="' + href + '" class="' + data.css + '" title="' + title + '">';
    html = html + '<span about="' + encodeURI(data.id) + '">';
    html = html + data.label;
    html = html + '</span></a>';
    element.replaceWith(html)
}

function replace_term(element, base) {
    var vocabulary = element.attr('vocabulary');
    var term = element.attr('term');
    var iri = element.attr('iri');
    var data = null;
    if (iri)
        data = VOCABULARY_LOOKUP_ID[iri];
    else if (vocabulary) {
        var vd = VOCABULARY_LOOKUP_VOC[vocabulary];
        if (vd)
            data = vd[term];
    }
    if (!data)
        data = VOCABULARY_LOOKUP_TERM[term];
    if (!data)
        return;
    var href = base + '?iri=' + encodeURIComponent(data.id);
    var title = data.label + '\n<' + data.id + '>';
    if (data.title && data.title != data.label)
        title = title + '\n' + data.title;
    if (data.description)
        title = title + '\n' + data.description;
    var html = '<a href="' + href + '" class="' + data.css + '" title="' + title + '">';
    html = html + '<span about="' + encodeURI(data.id) + '">';
    html = html + data.label;
    html = html + '</span></a>';
    element.replaceWith(html)
}


function load_tags(base) {
    $('.tag-holder').each(function () {
        replace_tag($(this), base);
    });
    $('.language-holder').each(function () {
        replace_language($(this), base);
    });
    $('.term-holder').each(function () {
        replace_term($(this), base);
    });
}