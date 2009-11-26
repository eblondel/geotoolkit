/*
 *    Geotoolkit.org - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2006-2009, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2009, Geomatys
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
package org.geotoolkit.util;

import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.measure.quantity.Length;

import org.junit.*;
import static org.junit.Assert.*;
import static org.geotoolkit.test.Commons.*;
import org.geotoolkit.test.Depend;


/**
 * Tests the {@link MeasurementRange}.
 *
 * @author Martin Desruisseaux (IRD)
 * @version 3.00
 *
 * @since 2.4
 */
@Depend(NumberRangeTest.class)
public final class MeasurementRangeTest {
    /**
     * Tests unit conversions.
     */
    @Test
    public void testConversion() {
        final MeasurementRange<Float> range = MeasurementRange.create(1000f, 2000f, SI.METRE);
        assertSame(range, range.convertTo(SI.METRE));
        final Unit<Length> KILOMETRE = SI.KILO(SI.METRE);
        assertEquals(MeasurementRange.create(1f, 2f, KILOMETRE), range.convertTo(KILOMETRE));
    }

    /**
     * Tests union and intersection involving a unit conversion.
     */
    @Test
    public void testIntersectWithConversion() {
        final Unit<Length> KILOMETRE = SI.KILO(SI.METRE);
        NumberRange<Float> r1 = MeasurementRange.create(1000f, 2000f, SI.METRE);
        NumberRange<Float> r2 = MeasurementRange.create(1.5f, 3f, KILOMETRE);
        assertEquals(Float.class, r1.getElementClass());
        assertEquals(Float.class, r2.getElementClass());
        assertEquals(MeasurementRange.create(1000f, 3000f, SI.METRE ), r1.union    (r2));
        assertEquals(MeasurementRange.create(1f,    3f,    KILOMETRE), r2.union    (r1));
        assertEquals(MeasurementRange.create(1500f, 2000f, SI.METRE ), r1.intersect(r2));
        assertEquals(MeasurementRange.create(1.5f,  2f,    KILOMETRE), r2.intersect(r1));
    }

    /**
     * Tests {@link MeasurementRange#toString()} method.
     */
    @Test
    public void testToString() {
        final MeasurementRange<Float> range = MeasurementRange.create(10f, 20f, SI.KILO(SI.METRE));
        assertEquals("[10.0 … 20.0] km", range.toString());
    }

    /**
     * Tests serialization.
     */
    @Test
    public void testSerialization() {
        final Unit<Length> KILOMETRE = SI.KILO(SI.METRE);
        NumberRange<Float> r1 = MeasurementRange.create(1000f, 2000f, SI.METRE);
        NumberRange<Float> r2 = MeasurementRange.create(1.5f, 3f, KILOMETRE);
        assertNotSame(r1, serialize(r1));
        assertNotSame(r2, serialize(r2));
    }
}
