/*
 *    Geotoolkit.org - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2008-2011, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2009-2011, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotoolkit.internal.jaxb.code;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.MissingResourceException;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

import org.opengis.annotation.UML;
import org.opengis.util.CodeList;

import org.geotoolkit.util.logging.Logging;
import org.geotoolkit.internal.StringUtilities;
import org.geotoolkit.internal.jaxb.MarshalContext;
import org.geotoolkit.resources.Locales;


/**
 * Stores information about {@link CodeList}, in order to handle format defined in ISO-19139
 * about the {@code CodeList} tags. This object is wrapped by {@link CodeListAdapter} or, in
 * the spacial case of {@link Locale} type, by {@link CodeListLocaleAdapter}. It provides the
 * {@link #codeList} and {@link #codeListValue} attribute to be marshalled.
 *
 * @author Cédric Briançon (Geomatys)
 * @author Martin Desruisseaux (Geomatys)
 * @version 3.17
 *
 * @see CodeListAdapter
 * @see CodeListLocaleAdapter
 *
 * @since 2.5
 * @module
 *
 * @todo JAXB BUG: Properties must be declared in reverse order. This is fixed in a JAXB version
 *       more recent than the one provided in the JDK. A Geotk test case should fail when the bug
 *       will be fixed, which will remind us to restore the correct order.
 */
@XmlType(name = "CodeList", propOrder = { "codeSpace", "codeListValue", "codeList" })
public final class CodeListProxy {
    /**
     * Returns the URL to given code list in the given XML file.
     *
     * @param  file The XML file, either {@code "gmxCodelists.xml"} or {@code "ML_gmxCodelists.xml"}.
     * @param  identifier The UML identifier of the code list.
     * @return The URL to the given code list in the given XSD schema.
     *
     * @since 3.17
     */
    private static String schemaURL(final String file, final String identifier) {
        final StringBuilder buffer = new StringBuilder(128);
        final String base = MarshalContext.schema("gmd");
        if (base == null) {
            buffer.append("http://schemas.opengis.net/iso/19139/20070417/resources/Codelist/");
        } else {
            buffer.append(base);
            final int length = buffer.length();
            if (length != 0 && buffer.charAt(length - 1) != '/') {
                buffer.append('/');
            }
            buffer.append("resources/Codelist/");
        }
        return buffer.append(file).append('#').append(identifier).toString();
    }

    /**
     * The {@code codeList} attribute in the XML element.
     */
    @XmlAttribute(required = true)
    public String codeList;

    /**
     * The {@code codeListValue} attribute in the XML element.
     */
    @XmlAttribute(required = true)
    public String codeListValue;

    /**
     * The optional {@code codeSpace} attribute in the XML element. The default value is
     * {@code null}. If a value is provided in this field, then {@link #value} should be
     * set as well.
     * <p>
     * This attribute is set to the 3 letters language code of the {@link #value} attribute,
     * as returned by {@link Locale#getISO3Language()}.
     *
     * @since 3.17
     */
    @XmlAttribute
    public String codeSpace;

    /**
     * The optional value to write in the XML element. The default value is {@code null}.
     * If a value is provided in this field, then {@link #codeSpace} is the language code
     * of this field or {@code null} for English.
     *
     * @since 3.17
     */
    @XmlValue
    public String value;

    /**
     * Default empty constructor for JAXB.
     */
    public CodeListProxy() {
    }

    /**
     * Builds a {@link CodeList} as defined in ISO-19139 standard.
     *
     * @param codeList The {@code codeList} attribute, <strong>including</strong> the URL path.
     * @param codeListValue The {@code codeListValue} attribute.
     * @param value The value to provide, in English language (because this
     *        constructor does not set the {@link #codeSpace} attribute).
     */
    CodeListProxy(final String file, final String codeList, final String codeListValue, final String value) {
        this.codeList      = schemaURL(file, codeList);
        this.codeListValue = codeListValue;
        this.value         = value;
    }

    /**
     * Builds a proxy instance of {@link CodeList}. It stores the values that will be
     * used for marshalling.
     *
     * @param code The code list to wrap.
     */
    CodeListProxy(final CodeList<?> code) {
        // Get the class identifier.
        final Class<?> type = code.getClass();
        final UML uml = type.getAnnotation(UML.class);
        String identifier = null;
        if (uml != null) {
            identifier = uml.identifier();
        }
        if (identifier == null || (identifier = identifier.trim()).length() == 0) {
            identifier = type.getSimpleName();
        }
        codeList = schemaURL("gmxCodelists.xml", identifier);

        // Get the field identifier.
        String field = code.identifier();
        codeListValue = (field != null && (field = field.trim()).length() != 0) ? field : code.name();

        // Get the localized name of the field identifier, if possible.
        Locale locale = MarshalContext.getLocale();
        if (locale != null) {
            final String key = identifier + '.' + field;
            try {
                value = ResourceBundle.getBundle("org.opengis.metadata.CodeLists", locale).getString(key);
            } catch (MissingResourceException e) {
                Logging.recoverableException(CodeListAdapter.class, "marshal", e);
            }
        }
        if (value != null) {
            codeSpace = Locales.getLanguage(locale);
        } else {
            value = StringUtilities.makeSentence(field);
        }
    }
}
