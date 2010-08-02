/*
 *    Geotoolkit.org - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2001-2010, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2009-2010, Geomatys
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
package org.geotoolkit.referencing.operation.transform;

import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;
import java.awt.geom.Point2D;
import java.awt.geom.AffineTransform;

import org.opengis.geometry.DirectPosition;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.referencing.operation.Matrix;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransform1D;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.TransformException;
import org.opengis.referencing.operation.NoninvertibleTransformException;

import org.geotoolkit.lang.Immutable;
import org.geotoolkit.geometry.GeneralDirectPosition;
import org.geotoolkit.referencing.operation.matrix.XMatrix;
import org.geotoolkit.referencing.operation.matrix.Matrix3;
import org.geotoolkit.internal.referencing.Semaphores;
import org.geotoolkit.util.converter.Classes;
import org.geotoolkit.io.wkt.Formattable;
import org.geotoolkit.io.wkt.Formatter;
import org.geotoolkit.resources.Errors;
import org.geotoolkit.util.Utilities;

import static org.geotoolkit.internal.referencing.MatrixUtilities.*;


/**
 * Base class for concatenated transform. Concatenated transforms are
 * serializable if all their step transforms are serializables.
 *
 * @author Martin Desruisseaux (IRD)
 * @version 3.00
 *
 * @since 1.2
 * @module
 */
@Immutable
public class ConcatenatedTransform extends AbstractMathTransform implements Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 5772066656987558634L;

    /**
     * {@code true} if math transform comparisons for {@linkplain #equivalent equivalence}
     * should be strict. A value of {@code false} cause this class to tolerate small departure
     * due to rounding errors.
     */
    private static final boolean STRICT = false;

    /**
     * Relative tolerance threshold for considering two matrix as
     * {@linkplain #equivalent equivalent}.
     */
    private static final double EQUIVALENT_THRESHOLD = STRICT ? 0 : 1E-14;

    /**
     * Tolerance level for deciding if an affine transform is an identity transform.
     * The 1E-9 value has been determined empirically using some "real world" values
     * of map projection parameters (scale factor, false easting/northing) and looking
     * for an identity transform when an affine transform matrix is multiplied by its
     * inverse.
     * <p>
     * If this value is changed, running the tests again should tell us if it cause
     * identity transforms to not be recognized anymore.
     */
    static final double IDENTITY_TOLERANCE = STRICT ? 0 : 1E-9;

    /**
     * The first math transform.
     */
    public final MathTransform transform1;

    /**
     * The second math transform.
     */
    public final MathTransform transform2;

    /**
     * The inverse transform. This field will be computed only when needed.
     * But it is serialized in order to avoid rounding error if the inverse
     * transform is serialized instead of the original one.
     */
    private ConcatenatedTransform inverse;

    /**
     * Constructs a concatenated transform. This constructor is for subclasses only. To
     * create a concatenated transform, use the factory method {@link #create} instead.
     *
     * @param transform1 The first math transform.
     * @param transform2 The second math transform.
     */
    protected ConcatenatedTransform(final MathTransform transform1,
                                    final MathTransform transform2)
    {
        this.transform1 = transform1;
        this.transform2 = transform2;
        if (!isValid()) {
            throw new IllegalArgumentException(Errors.format(
                    Errors.Keys.CANT_CONCATENATE_TRANSFORMS_$2,
                    getName(transform1), getName(transform2)));
        }
    }

    /**
     * Returns the underlying matrix for the specified transform,
     * or {@code null} if the matrix is unavailable.
     */
    private static Matrix getMatrix(final MathTransform transform) {
        if (transform instanceof LinearTransform) {
            return ((LinearTransform) transform).getMatrix();
        }
        if (transform instanceof AffineTransform) {
            return new Matrix3((AffineTransform) transform);
        }
        return null;
    }

    /**
     * Tests if one math transform is the inverse of the other.
     */
    private static boolean areInverse(final MathTransform tr1, MathTransform tr2) {
        try {
            tr2 = tr2.inverse();
        } catch (NoninvertibleTransformException e) {
            return false;
        }
        if (tr1 == tr2) {
            return true;
        }
        if (tr1 instanceof AbstractMathTransform) {
            return ((AbstractMathTransform) tr1).equivalent(tr2, STRICT);
        } else {
            return tr1.equals(tr2);
        }
    }

    /**
     * Concatenates the two given transforms. This factory method checks for step transforms
     * dimension. The returned transform will implement {@link MathTransform2D} if source and
     * target dimensions are equal to 2. Likewise, it will implement {@link MathTransform1D}
     * if source and target dimensions are equal to 1. {@link MathTransform} implementations
     * are available in two versions: direct and non-direct. The "non-direct" versions use an
     * intermediate buffer when performing transformations; they are slower and consume more
     * memory. They are used only as a fallback when a "direct" version can't be created.
     *
     * @param tr1 The first math transform.
     * @param tr2 The second math transform.
     * @return    The concatenated transform.
     *
     * @todo We could add one more optimisation: if one transform is a matrix and the
     *       other transform is a PassThroughTransform, and if the matrix as 0 elements
     *       for all rows matching the PassThrough sub-transform, then we can get ride
     *       of the whole PassThroughTransform object.
     */
    public static MathTransform create(MathTransform tr1, MathTransform tr2) {
        final int dim1 = tr1.getTargetDimensions();
        final int dim2 = tr2.getSourceDimensions();
        if (dim1 != dim2) {
            throw new IllegalArgumentException(Errors.format(
                    Errors.Keys.CANT_CONCATENATE_TRANSFORMS_$2, getName(tr1), getName(tr2)) +
                    ' ' + Errors.format(Errors.Keys.MISMATCHED_DIMENSION_$2, dim1, dim2));
        }
        MathTransform mt = createOptimized(tr1, tr2);
        if (mt != null) {
            return mt;
        }
        /*
         * If at least one math transform is an instance of ConcatenatedTransform and assuming
         * that MathTransforms are associatives, tries the following arrangements and select
         * one one with the fewest amount of steps:
         *
         *   Assuming :  tr1 = (A * B)
         *               tr2 = (C * D)
         *
         *   Current  :  (A * B) * (C * D)     Will be the selected one if nothing better.
         *   Try k=0  :  A * (B * (C * D))     Implies A * ((B * C) * D) through recursivity.
         *   Try k=1  :  ((A * B) * C) * D     Implies (A * (B * C)) * D through recursivity.
         *   Try k=2  :                        Tried only if try k=1 changed something.
         *
         * TODO: The same combination may be computed more than once (e.g. (B * C) above).
         *       Should not be a big deal if there is not two many steps. In the even where
         *       it would appears a performance issue, we could maintain a Map of combinations
         *       already computed. The map would be local to a "create" method execution.
         */
        int stepCount = getStepCount(tr1) + getStepCount(tr2);
        boolean tryAgain = true; // Really 'true' because we want at least 2 iterations.
        for (int k=0; ; k++) {
            MathTransform c1 = tr1;
            MathTransform c2 = tr2;
            final boolean first = (k & 1) == 0;
            MathTransform candidate = first ? c1 : c2;
            while (candidate instanceof ConcatenatedTransform) {
                final ConcatenatedTransform ctr = (ConcatenatedTransform) candidate;
                if (first) {
                    c1 = candidate = ctr.transform1;
                    c2 = create(ctr.transform2, c2);
                } else {
                    c1 = create(c1, ctr.transform1);
                    c2 = candidate = ctr.transform2;
                }
                final int c = getStepCount(c1) + getStepCount(c2);
                if (c < stepCount) {
                    tr1 = c1;
                    tr2 = c2;
                    stepCount = c;
                    tryAgain = true;
                }
            }
            if (!tryAgain) break;
            tryAgain = false;
        }
        /*
         * Tries again the check for optimized cases (identity, etc.), because a
         * transform may have been simplified to identity as a result of the above.
         */
        mt = createOptimized(tr1, tr2);
        if (mt != null) {
            return mt;
        }
        /*
         * Can't avoid the creation of a ConcatenatedTransform object.
         * Check for the type to create (1D, 2D, general case...)
         */
        return createConcatenatedTransform(tr1, tr2);
    }

    /**
     * Concatenates the three given transforms. This is a convenience methods doing its job
     * as two consecutive concatenations.
     *
     * @param tr1 The first math transform.
     * @param tr2 The second math transform.
     * @param tr3 The third math transform.
     * @return    The concatenated transform.
     *
     * @since 3.00
     */
    public static MathTransform create(MathTransform tr1, MathTransform tr2, MathTransform tr3) {
        return create(create(tr1, tr2), tr3);
    }

    /**
     * Tries to returns an optimized concatenation, for example by merging to affine transforms
     * into a single one. If no optimized cases has been found, returns {@code null}. In the later
     * case, the caller will need to create a more heavy {@link ConcatenatedTransform} instance.
     */
    private static MathTransform createOptimized(final MathTransform tr1, final MathTransform tr2) {
        /*
         * Trivial - but actually essential!! - check for the identity cases.
         */
        if (tr1.isIdentity()) return tr2;
        if (tr2.isIdentity()) return tr1;
        /*
         * If both transforms use matrix, then we can create
         * a single transform using the concatenated matrix.
         */
        final Matrix matrix1 = getMatrix(tr1);
        if (matrix1 != null) {
            final Matrix matrix2 = getMatrix(tr2);
            if (matrix2 != null) {
                final XMatrix matrix = toXMatrix(multiply(matrix2, matrix1));
                if (matrix.isIdentity(IDENTITY_TOLERANCE)) {
                    matrix.setIdentity();
                }
                // May not be really affine, but work anyway...
                // This call will detect and optimize the special
                // case where an 'AffineTransform' can be used.
                return ProjectiveTransform.create(matrix);
            }
        }
        /*
         * If one transform is the inverse of the
         * other, returns the identity transform.
         */
        if (areInverse(tr1, tr2) || areInverse(tr2, tr1)) {
            assert tr1.getSourceDimensions() == tr2.getTargetDimensions();
            assert tr1.getTargetDimensions() == tr2.getSourceDimensions();
            return IdentityTransform.create(tr1.getSourceDimensions());
        }
        /*
         * Gives a chance to AbstractMathTransform to returns an optimized object.
         * The main use case is Logarithmic vs Exponential transforms.
         */
        if (tr1 instanceof AbstractMathTransform) {
            final MathTransform optimized = ((AbstractMathTransform) tr1).concatenate(tr2, false);
            if (optimized != null) {
                return optimized;
            }
        }
        if (tr2 instanceof AbstractMathTransform) {
            final MathTransform optimized = ((AbstractMathTransform) tr2).concatenate(tr1, true);
            if (optimized != null) {
                return optimized;
            }
        }
        // No optimized case found.
        return null;
    }

    /**
     * Continue the construction started by {@link #create}. The construction step is available
     * separatly for testing purpose (in a JUnit test), and for {@link #inverse()} implementation.
     */
    static ConcatenatedTransform createConcatenatedTransform(
            final MathTransform tr1, final MathTransform tr2)
    {
        final int dimSource = tr1.getSourceDimensions();
        final int dimTarget = tr2.getTargetDimensions();
        /*
         * Checks if the result need to be a MathTransform1D.
         */
        if (dimSource == 1 && dimTarget == 1) {
            if (tr1 instanceof MathTransform1D && tr2 instanceof MathTransform1D) {
                return new ConcatenatedTransformDirect1D((MathTransform1D) tr1,
                                                         (MathTransform1D) tr2);
            } else {
                return new ConcatenatedTransform1D(tr1, tr2);
            }
        } else
        /*
         * Checks if the result need to be a MathTransform2D.
         */
        if (dimSource == 2 && dimTarget == 2) {
            if (tr1 instanceof MathTransform2D && tr2 instanceof MathTransform2D) {
                return new ConcatenatedTransformDirect2D((MathTransform2D) tr1,
                                                         (MathTransform2D) tr2);
            } else {
                return new ConcatenatedTransform2D(tr1, tr2);
            }
        } else if (dimSource == tr1.getTargetDimensions() && tr2.getSourceDimensions() == dimTarget) {
            return new ConcatenatedTransformDirect(tr1, tr2);
        } else {
            return new ConcatenatedTransform(tr1, tr2);
        }
    }

    /**
     * Returns a name for the specified math transform.
     */
    private static String getName(final MathTransform transform) {
        if (transform instanceof AbstractMathTransform) {
            ParameterValueGroup params = ((AbstractMathTransform) transform).getParameterValues();
            if (params != null) {
                String name = params.getDescriptor().getName().getCode();
                if (name != null && (name = name.trim()).length() != 0) {
                    return name;
                }
            }
        }
        return Classes.getShortClassName(transform);
    }

    /**
     * Checks if transforms are compatibles. The default
     * implementation check if transfert dimension match.
     */
    boolean isValid() {
        return transform1.getTargetDimensions() == transform2.getSourceDimensions();
    }

    /**
     * Gets the dimension of input points.
     */
    @Override
    public final int getSourceDimensions() {
        return transform1.getSourceDimensions();
    }

    /**
     * Gets the dimension of output points.
     */
    @Override
    public final int getTargetDimensions() {
        return transform2.getTargetDimensions();
    }

    /**
     * Returns the number of {@linkplain MathTransform math transform} steps performed by this
     * concatenated transform.
     *
     * @return The number of transform steps.
     *
     * @since 2.5
     */
    public final int getStepCount() {
        return getStepCount(transform1) + getStepCount(transform2);
    }

    /**
     * Returns the number of {@linkplain MathTransform math transform} steps performed by the
     * given transform. As a special case, we returns 0 for the identity transform since it
     * should be omitted from the final chain.
     */
    private static int getStepCount(final MathTransform transform) {
        if (transform.isIdentity()) {
            return 0;
        }
        if (!(transform instanceof ConcatenatedTransform)) {
            return 1;
        }
        return ((ConcatenatedTransform) transform).getStepCount();
    }

    /**
     * Returns all concatenated transforms. The {@linkplain List#size size} of the
     * returned list is equal to the value returned by {@link #getStepCount}.
     *
     * @return All math transforms performed by this concatenated transform.
     * @since 3.00
     */
    public final List<MathTransform> getSteps() {
        final List<MathTransform> transforms = new ArrayList<MathTransform>(5);
        getSteps(transforms);
        return transforms;
    }

    /**
     * Returns all concatenated transforms, modified with a pre-processing
     * applies before WKT formating.
     */
    private List<Object> getPseudoSteps() {
        final List<Object> transforms = new ArrayList<Object>();
        getSteps(transforms);
        /*
         * Pre-process the transforms before to format. Some steps may be
         * merged, or new steps may be created. Do not move size() out of
         * the loop, because it may change.
         */
        for (int i=0; i<transforms.size(); i++) {
            final Object step = transforms.get(i);
            if (step instanceof AbstractMathTransform) {
                i = ((AbstractMathTransform) step).beforeFormat(transforms, i);
            }
        }
        return transforms;
    }

    /**
     * Adds all concatenated transforms in the given list.
     *
     * @param transforms The list where to add concatenated transforms.
     */
    private void getSteps(final List<? super MathTransform> transforms) {
        if (transform1 instanceof ConcatenatedTransform) {
            ((ConcatenatedTransform) transform1).getSteps(transforms);
        } else {
            transforms.add(transform1);
        }
        if (transform2 instanceof ConcatenatedTransform) {
            ((ConcatenatedTransform) transform2).getSteps(transforms);
        } else {
            transforms.add(transform2);
        }
    }

    /**
     * If there is exactly one transform step which is {@linkplain Parameterized parameterized},
     * returns that transform step. Otherwise returns {@code null}.
     * <p>
     * This method normally requires that there is exactly one transform step remaining after we
     * processed map projections in the special way described in {@link #getParameterValues()},
     * because if they were more than one remaining steps, the returned parameters would not be
     * suffisient for rebuilding the full concatenated transform. Returning parameters when there
     * is more than one remaining step, even if all other transform steps are not parameterisable,
     * would be a contract violation.
     * <p>
     * However in the special case where we are formatting {@code PROJCS} element, this rule is
     * slightlty relaxed. More specifically we ignore affine transforms in order to accept axis
     * swapping or unit conversions. This special case is internal to Geotk implementation
     * and should be unknown to users.
     *
     * @return The parameterisable transform step, or {@code null} if none.
     */
    private Parameterized getParameterised() {
        Parameterized param = null;
        final List<Object> transforms = getPseudoSteps();
        if (transforms.size() == 1 || Semaphores.query(Semaphores.PROJCS)) {
            for (final Object candidate : transforms) {
                if (!(candidate instanceof Parameterized)) {
                    /*
                     * If a step does not implement the Parameterized interface, we conservatively
                     * handle it as if it was a non-linear step: we should return its parameters
                     * (which are null), or return null if there is more non-linear steps.
                     */
                    return null;
                }
                if (param != null) {
                    /*
                     * We found more than one Parameterized step. If both steps are non-linear,
                     * we fail (return null) because we don't know which one to choose. If both
                     * steps are linear, we fail for the same reason   (actually the later case
                     * should never occur, since consecutive linear transforms should have been
                     * concatenated in a single affine transform. But we check as a safety). If
                     * the previous step was linear and the current candidate is non-linear, we
                     * retain the current candidate. Otherwise we discart it.
                     */
                    final boolean isLinear = (candidate instanceof LinearTransform);
                    if ((param instanceof LinearTransform) == isLinear) {
                        return null;
                    }
                    if (isLinear) {
                        continue;
                    }
                }
                param = (Parameterized) candidate;
            }
        }
        return param;
    }

    /**
     * Returns the parameter descriptor, or {@code null} if none. This method performs the
     * same special check than {@link #getParameterValues}.
     */
    @Override
    public ParameterDescriptorGroup getParameterDescriptors() {
        final Parameterized param = getParameterised();
        return (param != null) ? param.getParameterDescriptors() : null;
    }

    /**
     * Returns the parameter values, or {@code null} if none. Concatenated transforms usually have
     * no parameters; instead the parameters of the individual components ({@link #transform1} and
     * {@link #transform2}) need to be inspected. However map projections in Geotk are implemented as
     * (<cite>normalize</cite> &ndash; <cite>non-linear kernel</cite> &ndash; <cite>denormalize</cite>)
     * tuples. This method detects such concatenation chains in order to return the parameter values
     * that describe the projection as a whole.
     */
    @Override
    public ParameterValueGroup getParameterValues() {
        final Parameterized param = getParameterised();
        return (param != null) ? param.getParameterValues() : null;
    }

    /**
     * Transforms the specified {@code ptSrc} and stores the result in {@code ptDst}.
     *
     * @throws TransformException If {@link #transform1} or {@link #transform2} failed.
     */
    @Override
    public DirectPosition transform(final DirectPosition ptSrc, final DirectPosition ptDst)
            throws TransformException
    {
        assert isValid();
        //  Note: If we know that the transfert dimension is the same than source
        //        and target dimension, then we don't need to use an intermediate
        //        point. This optimization is done in ConcatenatedTransformDirect.
        return transform2.transform(transform1.transform(ptSrc, null), ptDst);
    }

    /**
     * Transforms a single coordinates in a list of ordinal values.
     *
     * @throws TransformException If {@link #transform1} or {@link #transform2} failed.
     */
    @Override
    protected void transform(final double[] srcPts, int srcOff,
                             final double[] dstPts, int dstOff)
            throws TransformException
    {
        transform(srcPts, srcOff, dstPts, dstOff, 1);
    }

    /**
     * Transforms many coordinates in a list of ordinal values. The source points are first
     * transformed by {@link #transform1}, then the intermediate points are transformed by
     * {@link #transform2}. The transformations are performed without intermediate buffer
     * if it can be avoided.
     *
     * @throws TransformException If {@link #transform1} or {@link #transform2} failed.
     */
    @Override
    public void transform(final double[] srcPts, int srcOff,
                          final double[] dstPts, int dstOff, int numPts)
            throws TransformException
    {
        assert isValid();
        int bufferDim = transform2.getSourceDimensions();
        int targetDim = transform2.getTargetDimensions();
        /*
         * If the transfert dimension is not greater than the target dimension, then we
         * don't need to use an intermediate buffer. Note that this optimization is done
         * inconditionnaly in ConcatenatedTransformDirect.
         */
        if (bufferDim <= targetDim) {
            transform1.transform(srcPts, srcOff, dstPts, dstOff, numPts);
            transform2.transform(dstPts, dstOff, dstPts, dstOff, numPts);
            return;
        }
        if (numPts <= 0) {
            return;
        }
        /*
         * Creates a temporary array for the intermediate result. The array may be smaller than
         * the length necessary for containing every coordinates. In such case the concatenated
         * transform will need to be applied piecewise with special care in case of overlapping
         * arrays.
         */
        boolean descending = false;
        int sourceDim = transform1.getSourceDimensions();
        int numBuf = numPts;
        int length = numBuf * bufferDim;
        if (length > MAXIMUM_BUFFER_SIZE) {
            numBuf = Math.max(1, MAXIMUM_BUFFER_SIZE / bufferDim);
            if (srcPts == dstPts) {
                // Since we are using a buffer, the whole buffer is like a single coordinate point.
                switch (IterationStrategy.suggest(srcOff, numBuf*sourceDim, dstOff, numBuf*targetDim, numPts)) {
                    default: {
                        // Needs to copy the whole data.
                        numBuf = numPts;
                        break;
                    }
                    case ASCENDING: {
                        // No special care needed.
                        break;
                    }
                    case DESCENDING: {
                        // Traversing in reverse order is suffisient.
                        final int shift = numPts - numBuf;
                        srcOff += shift*sourceDim; sourceDim = -sourceDim;
                        dstOff += shift*targetDim; targetDim = -targetDim;
                        descending = true;
                        break;
                    }
                }
            }
            length = numBuf * bufferDim;
        }
        final double[] buf = new double[length];
        do {
            if (!descending && numBuf > numPts) {
                // Must be done before transforms if we are iterating in ascending order.
                numBuf = numPts;
            }
            transform1.transform(srcPts, srcOff, buf, 0, numBuf);
            transform2.transform(buf, 0, dstPts, dstOff, numBuf);
            numPts -= numBuf;
            if (descending && numBuf > numPts) {
                // Must be done after transforms if we are iterating in descending order.
                numBuf = numPts;
            }
            srcOff += numBuf * sourceDim;
            dstOff += numBuf * targetDim;
        } while (numPts != 0);
    }

    /**
     * Transforms many coordinates in a list of ordinal values. The source points are first
     * transformed by {@link #transform1}, then the intermediate points are transformed by
     * {@link #transform2}. An intermediate buffer of type {@code double[]} for intermediate
     * results is used for reducing rounding errors.
     *
     * @throws TransformException If {@link #transform1} or {@link #transform2} failed.
     */
    @Override
    public void transform(final float[] srcPts, int srcOff,
                          final float[] dstPts, int dstOff, int numPts)
            throws TransformException
    {
        assert isValid();
        if (numPts <= 0) {
            return;
        }
        boolean descending = false;
        int sourceDim = transform1.getSourceDimensions();
        int bufferDim = transform1.getTargetDimensions();
        int targetDim = transform2.getTargetDimensions();
        int dimension = Math.max(targetDim, bufferDim);
        int numBuf = numPts;
        int length = numBuf * dimension;
        if (length > MAXIMUM_BUFFER_SIZE) {
            numBuf = Math.max(1, MAXIMUM_BUFFER_SIZE / dimension);
            if (srcPts == dstPts) {
                switch (IterationStrategy.suggest(srcOff, numBuf*sourceDim, dstOff, numBuf*targetDim, numPts)) {
                    default: {
                        numBuf = numPts;
                        break;
                    }
                    case ASCENDING: {
                        break;
                    }
                    case DESCENDING: {
                        final int shift = numPts - numBuf;
                        srcOff += shift*sourceDim; sourceDim = -sourceDim;
                        dstOff += shift*targetDim; targetDim = -targetDim;
                        descending = true;
                        break;
                    }
                }
            }
            length = numBuf * dimension;
        }
        final double[] buf = new double[length];
        do {
            if (!descending && numBuf > numPts) {
                numBuf = numPts;
            }
            transform1.transform(srcPts, srcOff, buf, 0, numBuf);
            transform2.transform(buf, 0, dstPts, dstOff, numBuf);
            numPts -= numBuf;
            if (descending && numBuf > numPts) {
                numBuf = numPts;
            }
            srcOff += numBuf * sourceDim;
            dstOff += numBuf * targetDim;
        } while (numPts != 0);
    }

    /**
     * Transforms many coordinates in a list of ordinal values. The source points are first
     * transformed by {@link #transform1}, then the intermediate points are transformed by
     * {@link #transform2}. An intermediate buffer of type {@code double[]} for intermediate
     * results is used for reducing rounding errors.
     *
     * @throws TransformException If {@link #transform1} or {@link #transform2} failed.
     */
    @Override
    public void transform(final double[] srcPts, int srcOff,
                          final float [] dstPts, int dstOff, int numPts)
            throws TransformException
    {
        // Same code than transform(float[], ..., float[], ...) but the method calls
        // are actually different because of overloading of the "transform" methods.
        assert isValid();
        if (numPts <= 0) {
            return;
        }
        final int sourceDim = transform1.getSourceDimensions();
        final int bufferDim = transform1.getTargetDimensions();
        final int targetDim = transform2.getTargetDimensions();
        final int dimension = Math.max(targetDim, bufferDim);
        int numBuf = numPts;
        int length = numBuf * dimension;
        if (length > MAXIMUM_BUFFER_SIZE) {
            numBuf = Math.max(1, MAXIMUM_BUFFER_SIZE / dimension);
            length = numBuf * dimension;
        }
        final double[] buf = new double[length];
        do {
            if (numBuf > numPts) {
                numBuf = numPts;
            }
            transform1.transform(srcPts, srcOff, buf, 0, numBuf);
            transform2.transform(buf, 0, dstPts, dstOff, numBuf);
            srcOff += numBuf * sourceDim;
            dstOff += numBuf * targetDim;
            numPts -= numBuf;
        } while (numPts != 0);
    }

    /**
     * Transforms many coordinates in a list of ordinal values. The source points are first
     * transformed by {@link #transform1}, then the intermediate points are transformed by
     * {@link #transform2}. The transformations are performed without intermediate buffer
     * if it can be avoided.
     *
     * @throws TransformException If {@link #transform1} or {@link #transform2} failed.
     */
    @Override
    public void transform(final float [] srcPts, int srcOff,
                          final double[] dstPts, int dstOff, int numPts)
            throws TransformException
    {
        // Same code than transform(double[], ..., double[], ...) but the method calls
        // are actually different because of overloading of the "transform" methods.
        assert isValid();
        final int bufferDim = transform2.getSourceDimensions();
        final int targetDim = transform2.getTargetDimensions();
        if (bufferDim <= targetDim) {
            transform1.transform(srcPts, srcOff, dstPts, dstOff, numPts);
            transform2.transform(dstPts, dstOff, dstPts, dstOff, numPts);
            return;
        }
        if (numPts <= 0) {
            return;
        }
        int numBuf = numPts;
        int length = numBuf * bufferDim;
        if (length > MAXIMUM_BUFFER_SIZE) {
            numBuf = Math.max(1, MAXIMUM_BUFFER_SIZE / bufferDim);
            length = numBuf * bufferDim;
        }
        final double[] buf = new double[length];
        final int sourceDim = getSourceDimensions();
        do {
            if (numBuf > numPts) {
                numBuf = numPts;
            }
            transform1.transform(srcPts, srcOff, buf, 0, numBuf);
            transform2.transform(buf, 0, dstPts, dstOff, numBuf);
            srcOff += numBuf * sourceDim;
            dstOff += numBuf * targetDim;
            numPts -= numBuf;
        } while (numPts != 0);
    }

    /**
     * Creates the inverse transform of this object.
     */
    @Override
    public synchronized MathTransform inverse() throws NoninvertibleTransformException {
        assert isValid();
        if (inverse == null) {
            inverse = createConcatenatedTransform(transform2.inverse(), transform1.inverse());
            inverse.inverse = this;
        }
        return inverse;
    }

    /**
     * Gets the derivative of this transform at a point. This method delegates to the
     * {@link #derivative(DirectPosition)} method because the transformation steps
     * {@link #transform1} and {@link #transform2} may not be instances of {@link MathTransform2D}.
     *
     * @param  point The coordinate point where to evaluate the derivative.
     * @return The derivative at the specified point as a 2&times;2 matrix.
     * @throws TransformException if the derivative can't be evaluated at the specified point.
     */
    @Override
    public Matrix derivative(final Point2D point) throws TransformException {
        return derivative(new GeneralDirectPosition(point));
    }

    /**
     * Gets the derivative of this transform at a point.
     *
     * @param  point The coordinate point where to evaluate the derivative.
     * @return The derivative at the specified point (never {@code null}).
     * @throws TransformException if the derivative can't be evaluated at the specified point.
     */
    @Override
    public Matrix derivative(final DirectPosition point) throws TransformException {
        final Matrix matrix1 = transform1.derivative(point);
        final Matrix matrix2 = transform2.derivative(transform1.transform(point, null));
        return multiply(matrix2, matrix1);
    }

    /**
     * Tests whether this transform does not move any points.
     * Default implementation check if the two transforms are identity.
     */
    @Override
    public final boolean isIdentity() {
        return transform1.isIdentity() && transform2.isIdentity();
    }

    /**
     * Returns a hash value for this transform.
     */
    @Override
    public final int hashCode() {
        return getSteps().hashCode() ^ (int) serialVersionUID;
    }

    /**
     * Compares the specified object with this math transform for equality.
     */
    @Override
    public final boolean equals(final Object object) {
        if (object == this) {
            // Slight optimization
            return true;
        }
        /*
         * Do not invoke super.equals(...) because we don't want to compare descriptors.
         * Their computation may be expensive and this information is derived from the
         * transform steps anyway.
         */
        if (object instanceof ConcatenatedTransform) {
            final ConcatenatedTransform that = (ConcatenatedTransform) object;
            return getSteps().equals(that.getSteps());
        }
        return false;
    }

    /**
     * Compares the specified object with this math transform for equivalence.
     */
    @Override
    public final boolean equivalent(final MathTransform object, final boolean strict) {
        if (object == this) {
            // Slight optimization
            return true;
        }
        if (object instanceof ConcatenatedTransform) {
            final ConcatenatedTransform that = (ConcatenatedTransform) object;
            final List<MathTransform> s1 = this.getSteps();
            final List<MathTransform> s2 = that.getSteps();
            final int size = s1.size();
            if (size == s2.size()) {
                for (int i=0; i<size; i++) {
                    final MathTransform mt1 = s1.get(i);
                    final MathTransform mt2 = s2.get(i);
                    final boolean cmp;
                    if (mt1 instanceof AbstractMathTransform) {
                        cmp = ((AbstractMathTransform) mt1).equivalent(mt2, strict);
                    } else if (mt2 instanceof AbstractMathTransform) {
                        cmp = ((AbstractMathTransform) mt2).equivalent(mt1, strict);
                    } else {
                        cmp = Utilities.equals(mt1, mt2);
                    }
                    if (cmp) continue;
                    /*
                     * Before to gives up, maybe the math transform are affine transforms.
                     * Because the later do not have an "equivalent" method, if we want a
                     * tolerant comparison we need to check ourself.
                     */
                    if (!strict) {
                        final Matrix m1 = getMatrix(mt1);
                        if (m1 != null) {
                            final Matrix m2 = getMatrix(mt2);
                            if (m2 != null) {
                                if (epsilonEqual(m1, m2, EQUIVALENT_THRESHOLD, true)) {
                                    continue;
                                }
                            }
                        }
                    }
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Formats the inner part of a
     * <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html#CONCAT_MT"><cite>Well
     * Known Text</cite> (WKT)</A> element.
     *
     * @param  formatter The formatter to use.
     * @return The WKT element name, which is {@code "CONCAT_MT"}.
     */
    @Override
    public String formatWKT(final Formatter formatter) {
        final List<? super MathTransform> transforms;
        if (formatter.isInternalWKT()) {
            transforms = getSteps();
        } else {
            transforms = getPseudoSteps();
        }
        /*
         * Now formats the list that we got. Note that as a result of the above
         * pre-processing the list may have been reduced to a singleton, in which
         * case this is no longer a CONCAT_MT.
         */
        if (transforms.size() == 1) {
            return ((Formattable) transforms.get(0)).formatWKT(formatter);
        }
        for (final Object step : transforms) {
            if (step instanceof MathTransform) {
                formatter.append((MathTransform) step);
            } else {
                formatter.append((Formattable) step);
            }
        }
        return "CONCAT_MT";
    }
}
