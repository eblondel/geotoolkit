/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2008-2009, Open Source Geospatial Foundation (OSGeo)
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

import javax.xml.bind.annotation.XmlElement;
import org.opengis.metadata.spatial.CellGeometry;


/**
 * JAXB adapter for {@link CellGeometry}, in order to integrate the value in an element
 * complying with ISO-19139 standard. See package documentation for more information
 * about the handling of {@code CodeList} in ISO-19139.
 *
 * @author Cédric Briançon (Geomatys)
 * @version 3.00
 *
 * @since 2.5
 * @module
 */
public final class CellGeometryAdapter extends CodeListAdapter<CellGeometryAdapter, CellGeometry> {
    /**
     * Ensures that the adapted code list class is loaded.
     */
    static {
        ensureClassLoaded(CellGeometry.class);
    }

    /**
     * Empty constructor for JAXB only.
     */
    public CellGeometryAdapter() {
    }

    /**
     * Creates a new adapter for the given proxy.
     */
    private CellGeometryAdapter(final CodeListProxy proxy) {
        super(proxy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CellGeometryAdapter wrap(CodeListProxy proxy) {
        return new CellGeometryAdapter(proxy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<CellGeometry> getCodeListClass() {
        return CellGeometry.class;
    }

    /**
     * Invoked by JAXB on marshalling.
     *
     * @return The value to be marshalled.
     */
    @XmlElement(name = "MD_CellGeometryCode")
    public CodeListProxy getCodeListProxy() {
        return this.proxy;
    }

    /**
     * Invoked by JAXB on unmarshalling.
     *
     * @param proxy The unmarshalled value.
     */
    public void setCodeListProxy(final CodeListProxy proxy) {
        this.proxy = proxy;
    }
}
