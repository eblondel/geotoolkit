/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 1999-2009, Open Source Geospatial Foundation (OSGeo)
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
package org.geotoolkit.measure;

import java.io.Serializable;
import java.text.Format;
import java.text.ParseException;
import java.util.Locale;

import org.geotoolkit.util.converter.SimpleConverter;
import org.geotoolkit.util.converter.ConverterRegistry;


/**
 * An angle in degrees. An angle is the amount of rotation needed to bring one line or plane
 * into coincidence with another, generally measured in degrees, sexagesimal degrees or grads.
 *
 * @author Martin Desruisseaux (MPO, IRD)
 * @version 3.00
 *
 * @see Latitude
 * @see Longitude
 * @see AngleFormat
 *
 * @since 1.0
 * @module
 */
public class Angle implements Comparable<Angle>, Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 1158747349433104534L;

    /**
     * A shared instance of {@link AngleFormat}.
     *
     * @see #getAngleFormat
     */
    private static Format format;

    /**
     * Defines how angle can be converted to {@link Number} objects.
     */
    static {
        final ConverterRegistry system = ConverterRegistry.system();
        system.register(new SimpleConverter<Angle,Double>() {
            @Override public Class<Angle>  getSourceClass()      {return Angle .class;}
            @Override public Class<Double> getTargetClass()      {return Double.class;}
            @Override public Double        convert(Angle o)      {return o.theta;}
        });
        system.register(new SimpleConverter<Double,Angle>() {
            @Override public Class<Double> getSourceClass()      {return Double.class;}
            @Override public Class<Angle>  getTargetClass()      {return Angle .class;}
            @Override public Angle         convert(Double value) {return new Angle(value);}
        });
    }

    /**
     * Angle value in degres.
     */
    private final double theta;

    /**
     * Contructs a new angle with the specified value.
     *
     * @param theta Angle in degrees.
     */
    public Angle(final double theta) {
        this.theta = theta;
    }

    /**
     * Constructs a newly allocated {@code Angle} object that represents the angle value
     * represented by the string. The string should represents an angle in either fractional
     * degrees (e.g. 45.5°) or degrees with minutes and seconds (e.g. 45°30').
     *
     * @param  string A string to be converted to an {@code Angle}.
     * @throws NumberFormatException if the string does not contain a parsable angle.
     */
    public Angle(final String string) throws NumberFormatException {
        final Format format = getAngleFormat();
        final Angle theta;
        try {
            synchronized (Angle.class) {
                theta = (Angle) format.parseObject(string);
            }
        } catch (ParseException exception) {
            NumberFormatException e = new NumberFormatException(exception.getLocalizedMessage());
            e.initCause(exception);
            throw e;
        }
        if (getClass().isAssignableFrom(theta.getClass())) {
            this.theta = theta.theta;
        } else {
            throw new NumberFormatException(string);
        }
    }

    /**
     * Returns the angle value in decimal degrees.
     *
     * @return The angle value in degrees.
     */
    public double degrees() {
        return theta;
    }

    /**
     * Returns the angle value in radians.
     *
     * @return The angle value in radians.
     */
    public double radians() {
        return Math.toRadians(theta);
    }

    /**
     * Returns a hash code for this {@code Angle} object.
     */
    @Override
    public int hashCode() {
        final long code = Double.doubleToLongBits(theta);
        return (int) code ^ (int) (code >>> 32);
    }

    /**
     * Compares the specified object with this angle for equality.
     *
     * @param object The object to compare with this angle for equality.
     * @return {@code true} if the given object is equal to this angle.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && getClass().equals(object.getClass())) {
            final Angle that = (Angle) object;
            return Double.doubleToLongBits(this.theta) ==
                   Double.doubleToLongBits(that.theta);
        }  else {
            return false;
        }
    }

    /**
     * Compares two {@code Angle} objects numerically. The comparison
     * is done as if by the {@link Double#compare(double,double)} method.
     *
     * @param that The angle to compare with this object for order.
     * @return -1 if this angle is smaller than the given one, +1 if greater or 0 if equals.
     */
    @Override
    public int compareTo(final Angle that) {
        return Double.compare(this.theta, that.theta);
    }

    /**
     * Returns a string representation of this {@code Angle} object.
     */
    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        synchronized (Angle.class) {
            final Format format = getAngleFormat();
            buffer = format.format(this, buffer, null);
        }
        return buffer.toString();
    }

    /**
     * Returns a shared instance of {@link AngleFormat}. The return type is
     * {@link Format} in order to avoid class loading before necessary.
     */
    private static Format getAngleFormat() {
        assert Thread.holdsLock(Angle.class);
        if (format == null) {
            format = new AngleFormat("D°MM.m'", Locale.US);
        }
        return format;
    }
}
