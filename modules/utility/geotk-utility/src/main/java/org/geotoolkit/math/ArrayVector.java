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
package org.geotoolkit.math;

import java.io.Serializable;
import java.lang.reflect.Array;
import org.geotoolkit.resources.Errors;
import org.geotoolkit.util.converter.Classes;


/**
 * A vector backed by an array. This class does not copy the array, so changes in the underlying
 * array is reflected in this vector and vis-versa. The backing array is typically an array of a
 * primitive type, but array of wrappers are also accepted.
 *
 * @author Martin Desruisseaux (MPO, Geomatys)
 * @version 3.00
 *
 * @since 1.0
 * @module
 */
final class ArrayVector extends Vector implements Serializable {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = 3496467575389288163L;

    /**
     * The backing array. This is typically an array of a primitive type,
     * but can also be an array of wrappers.
     */
    private final Object array;

    /**
     * Creates a new vector for the given array.
     */
    public ArrayVector(final Object array) {
        this.array = array;
    }

    /**
     * Returns the type of elements in the backing array. This method returns
     * alway the wrapper type, as documented in the parent class.
     */
    @Override
    public Class<? extends Number> getElementType() {
        return Classes.primitiveToWrapper(array.getClass().getComponentType()).asSubclass(Number.class);
    }

    /**
     * Returns the length of the backing array.
     */
    @Override
    public int size() {
        return Array.getLength(array);
    }

    /**
     * Returns {@code true} if the value at the given index is {@code NaN}. The default
     * implementation returns {@code true} if {@code get(index)} returned {@code null}
     * or {@link Double#NaN}. However subclasses will typically provide more efficient
     * implementations. For example vectors of integer type return {@code false}
     * inconditionnaly.
     *
     * @param  index The index in the [0&hellip;{@linkplain #size size}-1] range.
     * @return {@code true} if the value at the given index is {@code NaN}.
     * @throws IndexOutOfBoundsException if the given index is out of bounds.
     */
    @Override
    public boolean isNaN(final int index) throws IndexOutOfBoundsException {
        final Number n = get(index);
        return (n == null) || Double.isNaN(n.doubleValue());
    }

    /**
     * Returns the value at the given index.
     *
     * @throws ArrayIndexOutOfBoundsException if the given index is out of bounds.
     * @throws ClassCastException if the component type can not be converted to a
     *         {@code double} by an identity or widening conversion.
     */
    @Override
    public double doubleValue(final int index) throws ArrayIndexOutOfBoundsException, ClassCastException {
        try {
            return Array.getDouble(array, index);
        } catch (IllegalArgumentException cause) {
            throw cantConvert(cause);
        }
    }

    /**
     * Returns the value at the given index.
     *
     * @throws ArrayIndexOutOfBoundsException if the given index is out of bounds.
     * @throws ClassCastException if the component type can not be converted to a
     *         {@code float} by an identity or widening conversion.
     */
    @Override
    public float floatValue(final int index) throws ArrayIndexOutOfBoundsException, ClassCastException {
        try {
            return Array.getFloat(array, index);
        } catch (IllegalArgumentException cause) {
            throw cantConvert(cause);
        }
    }

    /**
     * Returns the value at the given index.
     *
     * @throws ArrayIndexOutOfBoundsException if the given index is out of bounds.
     * @throws ClassCastException if the component type can not be converted to a
     *         {@code long} by an identity or widening conversion.
     */
    @Override
    public long longValue(final int index) throws ArrayIndexOutOfBoundsException, ClassCastException {
        try {
            return Array.getLong(array, index);
        } catch (IllegalArgumentException cause) {
            throw cantConvert(cause);
        }
    }

    /**
     * Returns the value at the given index.
     *
     * @throws ArrayIndexOutOfBoundsException if the given index is out of bounds.
     * @throws ClassCastException if the component type can not be converted to an
     *         {@code int} by an identity or widening conversion.
     */
    @Override
    public int intValue(final int index) throws ArrayIndexOutOfBoundsException, ClassCastException {
        try {
            return Array.getInt(array, index);
        } catch (IllegalArgumentException cause) {
            throw cantConvert(cause);
        }
    }

    /**
     * Returns the value at the given index.
     *
     * @throws ArrayIndexOutOfBoundsException if the given index is out of bounds.
     * @throws ClassCastException if the component type can not be converted to a
     *         {@code short} by an identity or widening conversion.
     */
    @Override
    public short shortValue(final int index) throws ArrayIndexOutOfBoundsException, ClassCastException {
        try {
            return Array.getShort(array, index);
        } catch (IllegalArgumentException cause) {
            throw cantConvert(cause);
        }
    }

    /**
     * Returns the value at the given index.
     *
     * @throws ArrayIndexOutOfBoundsException if the given index is out of bounds.
     * @throws ClassCastException if the component type can not be converted to a
     *         {@code byte} by an identity or widening conversion.
     */
    @Override
    public byte byteValue(final int index) throws ArrayIndexOutOfBoundsException, ClassCastException {
        try {
            return Array.getByte(array, index);
        } catch (IllegalArgumentException cause) {
            throw cantConvert(cause);
        }
    }

    /**
     * Returns the exception to be thrown when the component type in the backing array can
     * not be converted to the requested type through an identity or widening conversion.
     */
    private ClassCastException cantConvert(final IllegalArgumentException cause) {
        final ClassCastException exception = new ClassCastException(Errors.format(
                Errors.Keys.CANT_CONVERT_FROM_TYPE_$1, array.getClass().getComponentType()));
        exception.initCause(cause);
        return exception;
    }

    /**
     * Returns the value at the given index.
     *
     * @throws ArrayIndexOutOfBoundsException if the given index is out of bounds.
     */
    @Override
    public Number get(final int index) throws ArrayIndexOutOfBoundsException {
        return (Number) Array.get(array, index);
    }

    /**
     * Sets the value at the given index.
     *
     * @throws ArrayIndexOutOfBoundsException if the given index is out of bounds.
     * @throws ArrayStoreException if the backing array can not store the given object.
     */
    @Override
    public Number set(final int index, final Number value)
            throws ArrayIndexOutOfBoundsException, ArrayStoreException
    {
        final Number old = (Number) Array.get(array, index);
        try {
            Array.set(array, index, value);
        } catch (IllegalArgumentException cause) {
            final ArrayStoreException exception = new ArrayStoreException(Errors.format(
                    Errors.Keys.CANT_CONVERT_FROM_TYPE_$1, Classes.getClass(value)));
            exception.initCause(cause);
            throw exception;
        }
        return old;
    }
}
