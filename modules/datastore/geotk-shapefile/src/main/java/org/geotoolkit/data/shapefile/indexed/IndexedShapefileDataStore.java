/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2003-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotoolkit.data.shapefile.indexed;

import static org.geotoolkit.data.shapefile.ShpFileType.DBF;
import static org.geotoolkit.data.shapefile.ShpFileType.FIX;
import static org.geotoolkit.data.shapefile.ShpFileType.QIX;
import static org.geotoolkit.data.shapefile.ShpFileType.SHP;
import static org.geotoolkit.data.shapefile.ShpFileType.SHX;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometryFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

import org.geotoolkit.storage.DataStoreException;
import org.geotoolkit.data.DefaultSimpleFeatureReader;
import org.geotoolkit.data.FeatureIDReader;
import org.geotoolkit.data.memory.GenericEmptyFeatureIterator;
import org.geotoolkit.data.FeatureReader;
import org.geotoolkit.data.FeatureWriter;
import org.geotoolkit.data.query.Query;
import org.geotoolkit.data.shapefile.ShapefileDataStore;
import org.geotoolkit.data.shapefile.ShapefileDataStoreFactory;
import org.geotoolkit.data.shapefile.ShpFileType;
import org.geotoolkit.data.dbf.DbaseFileReader;
import org.geotoolkit.data.dbf.IndexedDbaseFileReader;
import org.geotoolkit.data.shapefile.shp.IndexFile;
import org.geotoolkit.data.shapefile.shp.ShapefileReader;
import org.geotoolkit.data.shapefile.shp.ShapefileReader.Record;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.data.shapefile.ShpDBF;
import org.geotoolkit.data.shapefile.indexed.IndexDataReader.ShpData;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.index.quadtree.LazySearchCollection;
import org.geotoolkit.feature.SchemaException;
import org.geotoolkit.filter.visitor.FilterAttributeExtractor;
import org.geotoolkit.filter.visitor.ExtractBoundsFilterVisitor;
import org.geotoolkit.filter.visitor.IdCollectorFilterVisitor;
import org.geotoolkit.geometry.jts.JTSEnvelope2D;
import org.geotoolkit.index.CloseableCollection;
import org.geotoolkit.index.Data;
import org.geotoolkit.index.LockTimeoutException;
import org.geotoolkit.index.TreeException;
import org.geotoolkit.index.quadtree.QuadTree;
import org.geotoolkit.index.quadtree.StoreException;
import org.geotoolkit.index.quadtree.fs.FileSystemIndexStore;
import org.geotoolkit.index.rtree.RTree;
import org.geotoolkit.util.NullProgressListener;
import org.geotoolkit.data.query.QueryUtilities;
import org.geotoolkit.feature.FeatureTypeUtilities;
import org.geotoolkit.index.quadtree.LazyTyleSearchIterator;
import org.geotoolkit.resources.NIOUtilities;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.Id;
import org.opengis.filter.identity.Identifier;
import org.opengis.filter.sort.SortBy;
import org.opengis.filter.spatial.BBOX;


/**
 * A DataStore implementation which allows reading and writing from Shapefiles.
 * 
 * @author Ian Schneider
 * @author Tommaso Nolli
 * @author jesse eichar
 * 
 * @module pending
 */
public class IndexedShapefileDataStore extends ShapefileDataStore {

    private static final PreparedGeometryFactory PREPARED_FACTORY = new PreparedGeometryFactory();
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    private static final class IdentifierComparator implements Comparator<Identifier>{
        @Override
        public int compare(Identifier o1, Identifier o2){
            return o1.toString().compareTo(o2.toString());
        }
    }

    IndexType treeType;
    final boolean useIndex;
    private RTree rtree;
    int maxDepth;

    /**
     * Creates a new instance of ShapefileDataStore.
     * 
     * @param url The URL of the shp file to use for this DataSource.
     */
    public IndexedShapefileDataStore(URL url)
            throws MalformedURLException,DataStoreException {
        this(url, null, false, true, IndexType.QIX);
    }

    /**
     * Creates a new instance of ShapefileDataStore.
     * 
     * @param url The URL of the shp file to use for this DataSource.
     * @param namespace DOCUMENT ME!
     */
    public IndexedShapefileDataStore(URL url, String namespace)
            throws MalformedURLException,DataStoreException {
        this(url, namespace, false, true, IndexType.QIX);
    }

    /**
     * Creates a new instance of ShapefileDataStore.
     * 
     * @param url The URL of the shp file to use for this DataSource.
     * @param useMemoryMappedBuffer enable/disable memory mapping of files
     * @param createIndex enable/disable automatic index creation if needed
     */
    public IndexedShapefileDataStore(URL url, boolean useMemoryMappedBuffer,
            boolean createIndex) throws MalformedURLException,DataStoreException {
        this(url, null, useMemoryMappedBuffer, createIndex, IndexType.QIX);
    }

    /**
     * Creates a new instance of ShapefileDataStore.
     * 
     * @param url The URL of the shp file to use for this DataSource.
     * @param namespace DOCUMENT ME!
     * @param useMemoryMappedBuffer enable/disable memory mapping of files
     * @param createIndex enable/disable automatic index creation if needed
     * @param treeType The type of index to use
     * 
     */
    public IndexedShapefileDataStore(URL url, String namespace, boolean useMemoryMappedBuffer,
            boolean createIndex, IndexType treeType)
            throws MalformedURLException,DataStoreException {
        this(url, namespace, useMemoryMappedBuffer, createIndex, treeType, DEFAULT_STRING_CHARSET);
    }

    /**
     * Creates a new instance of ShapefileDataStore.
     * 
     * @param url The URL of the shp file to use for this DataSource.
     * @param namespace DOCUMENT ME!
     * @param useMemoryMappedBuffer enable/disable memory mapping of files
     * @param createIndex enable/disable automatic index creation if needed
     * @param treeType The type of index used
     * @param dbfCharset {@link Charset} used to decode strings from the DBF
     * 
     * @throws MalformedURLException
     */
    public IndexedShapefileDataStore(URL url, String namespace, boolean useMemoryMappedBuffer,
            boolean createIndex, IndexType treeType, Charset dbfCharset)
            throws MalformedURLException,DataStoreException {
        super(url, namespace, useMemoryMappedBuffer, dbfCharset);

        this.treeType = treeType;
        this.useIndex = treeType != IndexType.NONE;
        maxDepth = -1;
        try {
            if (shpFiles.isLocal() && createIndex
                    && needsGeneration(treeType.shpFileType)) {
                createSpatialIndex();
            }
        } catch (IOException e) {
            this.treeType = IndexType.NONE;
            ShapefileDataStoreFactory.LOGGER.log(Level.WARNING, e
                    .getLocalizedMessage());
        }
        try {
            if (shpFiles.isLocal() && needsGeneration(FIX)) {
                generateFidIndex();
            }
        } catch (IOException e) {
            ShapefileDataStoreFactory.LOGGER.log(Level.WARNING, e
                    .getLocalizedMessage());
        }

    }

    /**
     * Forces the spatial index to be created
     */
    public void createSpatialIndex() throws IOException {
        buildQuadTree(maxDepth);
    }

    @Override
    protected void finalize() throws Throwable {
        if (rtree != null) {
            try {
                rtree.close();
            } catch (Exception e) {
                e.printStackTrace();
                getLogger()
                        .severe("org.geotoolkit.data.shapefile.indexed.IndexedShapeFileDataStore#finalize(): Error closing rtree. "
                                + e.getLocalizedMessage());
            }
        }
    }
    
    /**
     * Use the spatial index if available and adds a small optimization: if no
     * attributes are going to be read, don't uselessly open and read the dbf
     * file.
     */
    @Override
    public FeatureReader<SimpleFeatureType, SimpleFeature> getFeatureReader(Query query)
            throws DataStoreException {
        final SimpleFeatureType originalSchema = getFeatureType();
        final Name              queryTypeName = query.getTypeName();
        final SortBy[]          querySortBy = query.getSortBy();
        final Hints             queryHints = query.getHints();
        Filter            queryFilter = query.getFilter();

        if (queryFilter == Filter.EXCLUDE){
            return GenericEmptyFeatureIterator.createReader(originalSchema);
        }

        if (querySortBy != null && querySortBy.length > 0) {
            throw new DataStoreException("The ShapeFileDatastore does not support sortby query");
        }

        final Name defaultGeomName = originalSchema.getGeometryDescriptor().getName();

        final FilterAttributeExtractor fae = new FilterAttributeExtractor();
        queryFilter.accept(fae, null);

        //find all attributs needed
        final Set<Name> attributes = new HashSet<Name>(fae.getAttributeNameSet());
        final Name[] queryPropertyNames = query.getPropertyNames();
        if(queryPropertyNames != null && queryPropertyNames.length > 0){
            attributes.addAll(Arrays.asList(queryPropertyNames));
        }
        final Name[] propertyNames = (Name[]) attributes.toArray(new Name[attributes.size()]);
        

        try {
            final SimpleFeatureType newSchema;
            final boolean readDbf;
            final boolean readGeometry;

            if(queryPropertyNames != null){
                if (propertyNames.length==1 && DefaultName.match(propertyNames[0],defaultGeomName)){
                    readDbf = false;
                    readGeometry = true;
                    newSchema = (SimpleFeatureType) FeatureTypeUtilities.createSubType(originalSchema, propertyNames);
                } else if (propertyNames.length == 0) {
                    readDbf = false;
                    readGeometry = false;
                    newSchema = (SimpleFeatureType) FeatureTypeUtilities.createSubType(originalSchema, propertyNames);
                } else {
                    readDbf = true;
                    readGeometry = true;
                    newSchema = (SimpleFeatureType) FeatureTypeUtilities.createSubType(originalSchema, propertyNames, 
                            originalSchema.getCoordinateReferenceSystem());
                }
            }else{
                readDbf = true;
                readGeometry = true;
                newSchema = originalSchema;
            }

            final FeatureReader<SimpleFeatureType,SimpleFeature> reader;
            
            if(queryFilter instanceof BBOX){
                //in case we have a BBOX filter onyle, which is very commun, we can speed
                //the process by relying on the quadtree estimations
                final Envelope bbox = (Envelope) queryFilter.accept(
                        ExtractBoundsFilterVisitor.BOUNDS_VISITOR, new JTSEnvelope2D());
                queryFilter = Filter.INCLUDE;
                reader = createFeatureReader(queryTypeName.getLocalPart(),
                        getBBoxAttributesReader(readDbf,readGeometry, bbox),
                        newSchema, queryHints);

            }else{
                reader = createFeatureReader(queryTypeName.getLocalPart(),
                        getAttributesReader(readDbf,readGeometry, queryFilter),
                        newSchema, queryHints);
            }

            //handle remaining parameters
            final QueryBuilder qb = new QueryBuilder(queryTypeName);
            qb.setProperties(queryPropertyNames);
            qb.setFilter(queryFilter);
            qb.setHints(queryHints);
            qb.setCRS(query.getCoordinateSystemReproject());
            qb.setResolution(query.getResolution());
            return handleRemaining(reader, qb.buildQuery());

        } catch (IOException se) {
            throw new DataStoreException("Error creating reader", se);
        }
    }

    protected FeatureReader<SimpleFeatureType, SimpleFeature> createFeatureReader(String typeName,
            IndexedShapefileAttributeReader r, SimpleFeatureType readerSchema, Hints hints)
            throws SchemaException, IOException,DataStoreException {

        FeatureIDReader fidReader;
        if (!indexUseable(FIX)) {
            fidReader = new ShapeFIDReader(getName().getLocalPart(), r);
        } else {
            fidReader = new IndexedFidReader(shpFiles, r);
        }

        return DefaultSimpleFeatureReader.create(r, fidReader, readerSchema, hints);
    }

    /**
     * Forces the FID index to be regenerated
     * 
     * @throws IOException
     */
    public void generateFidIndex() throws IOException {
        FidIndexer.generate(shpFiles);
    }

    /**
     * Utility method to transform an envelope in geometry.
     * @param env
     * @return Geometry
     */
    private static PreparedGeometry toGeometry(Envelope env){
        final Coordinate[] coords = new Coordinate[5];
        coords[0] = new Coordinate(env.getMinX(), env.getMinY());
        coords[1] = new Coordinate(env.getMinX(), env.getMaxY());
        coords[2] = new Coordinate(env.getMaxX(), env.getMaxY());
        coords[3] = new Coordinate(env.getMaxX(), env.getMinY());
        coords[4] = new Coordinate(env.getMinX(), env.getMinY());
        final LinearRing ring = GEOMETRY_FACTORY.createLinearRing(coords);
        Geometry geom = GEOMETRY_FACTORY.createPolygon(ring, new LinearRing[0]);
        return PREPARED_FACTORY.create(geom);
    }

    protected IndexedShapefileAttributeReader getBBoxAttributesReader(
            boolean readDbf, boolean readGeometry, final Envelope bbox)
            throws DataStoreException {

        final PreparedGeometry boundingGeometry = toGeometry(bbox);

        CloseableCollection<Data> goodCollec = null;
        try {
            final QuadTree quadTree = openQuadTree();
            if ((quadTree != null)) {
                goodCollec = quadTree.search(bbox);
            }

        } catch (StoreException e) {
            throw new DataStoreException("Error querying index: " + e.getMessage());
        }
        final LazySearchCollection col = (LazySearchCollection) goodCollec;

        final SimpleFeatureType schema = getFeatureType();
        final List<AttributeDescriptor> atts;

        IndexedDbaseFileReader dbfR = null;

        if (!readDbf) {
            getLogger().fine("The DBF file won't be opened since no attributes "
                    + "will be read from it");
            if(readGeometry){
                atts = Collections.singletonList((AttributeDescriptor)schema.getGeometryDescriptor());
            }else{
                atts = Collections.EMPTY_LIST;
            }
        } else {
            atts = (schema == null) ? readAttributes(namespace)
                : schema.getAttributeDescriptors();
            dbfR = (IndexedDbaseFileReader) openDbfReader();
        }

        return new IndexedShapefileAttributeReader(atts, openShapeReader(), dbfR, goodCollec, col.bboxIterator()){

            private boolean hasNext = false;
            private final Object[] buffer = new Object[metaData.length];

            @Override
            public boolean hasNext() throws IOException {
                findNext();
                return hasNext;
            }

            @Override
            public void next() throws IOException {
                hasNext = false;
            }

            @Override
            public void read(Object[] buffer) throws IOException {
                System.arraycopy(this.buffer, 0, buffer, 0, this.buffer.length);
            }

            private void findNext() throws IOException{
                while(!hasNext && super.hasNext()){
                    super.next();
                    if(((LazyTyleSearchIterator.Buffered)goodRecs).isSafe()){
                        super.read(buffer);
                        hasNext = true;
                        continue;
                    }

                    if(!(bbox.getMinX() > record.maxX ||
                         bbox.getMaxX() < record.minX ||
                         bbox.getMinY() > record.maxY ||
                         bbox.getMaxY() < record.minY)){
                        super.read(buffer);
                        final Geometry candidate = (Geometry)buffer[0];
                        hasNext = boundingGeometry.intersects(candidate);
                    }
                }
            }

        };
    }

    /**
     * Returns the attribute reader, allowing for a pure shape reader, or a
     * combined dbf/shp reader.
     * 
     * @param readDbf - if true, the dbf fill will be opened and read
     * @param readGeometry DOCUMENT ME!
     * @param filter - a Filter to use
     * @throws IOException
     */
    protected IndexedShapefileAttributeReader getAttributesReader(
            boolean readDbf, boolean readGeometry, Filter filter)
            throws DataStoreException {
        Envelope bbox = new JTSEnvelope2D(); // will be bbox.isNull() to
        // start

        CloseableCollection<Data> goodRecs = null;
        if (filter instanceof Id && shpFiles.isLocal() && shpFiles.exists(FIX)) {
            Id fidFilter = (Id) filter;

            TreeSet idsSet = new TreeSet(new IdentifierComparator());
            idsSet.addAll(fidFilter.getIdentifiers());
            try {
                goodRecs = queryFidIndex(idsSet);
            } catch (IOException ex) {
                throw new DataStoreException(ex);
            }
        } else {
            if (filter != null) {
                // Add additional bounds from the filter
                // will be null for Filter.EXCLUDES
                bbox = (Envelope) filter.accept(
                        ExtractBoundsFilterVisitor.BOUNDS_VISITOR, bbox);
                if (bbox == null) {
                    bbox = new JTSEnvelope2D();
                    // we hit Filter.EXCLUDES consider returning an empty
                    // reader?
                    // (however should simplify the filter to detect ff.not(
                    // fitler.EXCLUDE )
                }
            }

            if (!bbox.isNull() && this.useIndex) {
                try {
                    goodRecs = this.queryQuadTree(bbox);
                } catch (TreeException e) {
                    throw new DataStoreException("Error querying index: " + e.getMessage());
                } catch (IOException e) {
                    throw new DataStoreException("Error querying index: " + e.getMessage());
                }
            }
        }

        final SimpleFeatureType schema = getFeatureType();
        final List<AttributeDescriptor> atts;

        IndexedDbaseFileReader dbfR = null;

        if (!readDbf) {
            getLogger().fine("The DBF file won't be opened since no attributes "
                    + "will be read from it");            
            if(readGeometry){
                atts = Collections.singletonList((AttributeDescriptor)schema.getGeometryDescriptor());
            }else{
                atts = Collections.EMPTY_LIST;
            }
        } else {
            atts = (schema == null) ? readAttributes(namespace)
                : schema.getAttributeDescriptors();
            dbfR = (IndexedDbaseFileReader) openDbfReader();
        }

        return new IndexedShapefileAttributeReader(atts, openShapeReader(), dbfR, 
                goodRecs, ((goodRecs!=null)?goodRecs.iterator():null));
    }

    /**
     * Uses the Fid index to quickly lookup the shp offset and the record number
     * for the list of fids
     * 
     * @param fids
     *                the fids of the features to find.  If the set is sorted by alphabet the performance is likely to be better.
     * @return a list of Data objects
     * @throws IOException
     * @throws TreeException
     */
    private CloseableCollection<Data> queryFidIndex(Set<Identifier> idsSet) throws IOException {

        if (!indexUseable(FIX)) {
            return null;
        }

        final IndexedFidReader reader = new IndexedFidReader(shpFiles);
        final CloseableCollection<Data> records = new CloseableArrayList(idsSet.size());

        try {
            final IndexFile shx = openIndexFile();
            try {

                for (Identifier identifier : idsSet) {
                    String fid = identifier.toString();
                    long recno = reader.findFid(fid);
                    if (recno == -1){
                        if(getLogger().isLoggable(Level.FINEST)){
                            getLogger().finest("fid " + fid+ " not found in index, continuing with next queried fid...");
                        }
                        continue;
                    }
                    try {
                        Data data = new ShpData(
                                (int)(recno+1),
                                (long)shx.getOffsetInBytes((int) recno));
                        if(getLogger().isLoggable(Level.FINEST)){
                            getLogger().finest("fid " + fid+ " found for record #"
                                    + data.getValue(0) + " at index file offset "
                                    + data.getValue(1));
                        }
                        records.add(data);
                    } catch (Exception e) {
                        IOException exception = new IOException();
                        exception.initCause(e);
                        throw exception;
                    }
                }
            } finally {
                shx.close();
            }
        } finally {
            reader.close();
        }

        return records;
    }

    /**
     * Returns true if the index for the given type exists and is useable.
     * 
     * @param indexType the type of index to check
     * @return true if the index for the given type exists and is useable.
     */
    public boolean indexUseable(ShpFileType indexType) {
        if (shpFiles.isLocal()) {
            if (needsGeneration(indexType) || !shpFiles.exists(indexType)) {
                return false;
            }
        } else {

            ReadableByteChannel read = null;
            try {
                read = shpFiles.getReadChannel(indexType, this);
            } catch (IOException e) {
                return false;
            } finally {
                //todo replace by ARM in JDK 1.7
                if (read != null) {
                    try {
                        read.close();
                    } catch (IOException e) {
                        ShapefileDataStoreFactory.LOGGER.log(Level.WARNING,
                                "could not close stream", e);
                    }
                }
            }
        }

        return true;
    }

    boolean needsGeneration(ShpFileType indexType) {
        if (!shpFiles.isLocal())
            throw new IllegalStateException(
                    "This method only applies if the files are local and the file can be created");

        final URL indexURL = shpFiles.acquireRead(indexType, this);
        final URL shpURL = shpFiles.acquireRead(SHP, this);
        try {

            if (indexURL == null) {
                return true;
            }
            // indexes require both the SHP and SHX so if either or missing then
            // you don't need to index
            if (!shpFiles.exists(SHX) || !shpFiles.exists(SHP)) {
                return false;
            }

            final File indexFile = NIOUtilities.urlToFile(indexURL);
            final File shpFile = NIOUtilities.urlToFile(shpURL);
            final long indexLastModified = indexFile.lastModified();
            final long shpLastModified = shpFile.lastModified();
            final boolean shpChangedMoreRecently = indexLastModified < shpLastModified;
            return !indexFile.exists() || shpChangedMoreRecently;
        } finally {
            if (shpURL != null) {
                shpFiles.unlockRead(shpURL, this);
            }
            if (indexURL != null) {
                shpFiles.unlockRead(indexURL, this);
            }
        }
    }

    /**
     * Returns true if the indices already exist and do not need to be
     * regenerated or cannot be generated (IE isn't local).
     * 
     * @return true if the indices already exist and do not need to be regenerated.
     */
    public boolean isIndexed() {
        if (shpFiles.isLocal()) {
            return true;
        }
        return !needsGeneration(FIX) && !needsGeneration(treeType.shpFileType);
    }


    /**
     * QuadTree Query
     * 
     * @param bbox
     * 
     * @throws DataSourceException
     * @throws IOException
     * @throws TreeException DOCUMENT ME!
     */
    private CloseableCollection<Data> queryQuadTree(Envelope bbox)
            throws DataStoreException, IOException, TreeException {
        CloseableCollection<Data> tmp = null;

        try {
            final QuadTree quadTree = openQuadTree();
            if ((quadTree != null) && !bbox.contains(quadTree.getRoot().getBounds(new Envelope()))) {
                tmp = quadTree.search(bbox);

                if (tmp == null || !tmp.isEmpty())
                    return tmp;
            }
            if (quadTree != null) {
                quadTree.close();
            }
        } catch (Exception e) {
            throw new DataStoreException("Error querying QuadTree", e);
        }

        return null;
    }

    /**
     * Convenience method for opening a DbaseFileReader.
     * 
     * @return A new DbaseFileReader
     * @throws DataStoreException If an error occurs during creation.
     */
    @Override
    protected DbaseFileReader openDbfReader() throws DataStoreException {
        if (shpFiles.get(DBF) == null) {
            return null;
        }

        if (shpFiles.isLocal() && !shpFiles.exists(DBF)) {
            return null;
        }
        try {
            return ShpDBF.indexed(shpFiles, false, dbfCharset);
        } catch (IOException ex) {
            throw new DataStoreException(ex);
        }
    }

    /**
     * Convenience method for opening a QuadTree index.
     * 
     * @return A new QuadTree
     * @throws StoreException
     */
    protected QuadTree openQuadTree() throws StoreException {
        if (!shpFiles.isLocal()) {
            return null;
        }
        final URL treeURL = shpFiles.acquireRead(QIX, this);
        final File treeFile = NIOUtilities.urlToFile(treeURL);

        try {
            if (!treeFile.exists() || (treeFile.length() == 0)) {
                treeType = IndexType.NONE;
                return null;
            }

            try {
                final FileSystemIndexStore store = new FileSystemIndexStore(treeFile);
                return store.load(new IndexDataReader(openIndexFile()));
            } catch (IOException e) {
                throw new StoreException(e);
            }
        } finally {
            shpFiles.unlockRead(treeURL, this);
        }

    }

    /**
     * Create a FeatureWriter for the given type name.
     * 
     * @param typeName The typeName of the FeatureType to write
     * @return A new FeatureWriter.
     * @throws IOException If the typeName is not available or some other error occurs.
     */
    @Override
    public FeatureWriter<SimpleFeatureType, SimpleFeature> getFeatureWriter(Name typeName, Filter filter)
            throws DataStoreException {
        typeCheck(typeName);

        FeatureReader<SimpleFeatureType, SimpleFeature> featureReader;
        IndexedShapefileAttributeReader attReader = getAttributesReader(true,true, null);
        try {
            SimpleFeatureType schema = (SimpleFeatureType) getFeatureType(typeName);
            if (schema == null) {
                throw new IOException(
                        "To create a shapefile, you must first call createSchema()");
            }
            featureReader = createFeatureReader(typeName.getLocalPart(), attReader, schema, null);

        } catch (Exception e) {
            featureReader = GenericEmptyFeatureIterator.createReader(getFeatureType());
        }
        try {
            FeatureWriter<SimpleFeatureType, SimpleFeature> writer = new IndexedShapefileFeatureWriter(
                    typeName.getLocalPart(), shpFiles, attReader, featureReader, this, dbfCharset);
            writer = handleRemaining(writer, filter);
            return writer;
        } catch (IOException ex) {
            throw new DataStoreException(ex);
        }
    }

    
    @Override
    public org.opengis.geometry.Envelope getEnvelope(Query query) throws DataStoreException {

        
        final Filter filter = query.getFilter();
        if (filter == Filter.INCLUDE || QueryUtilities.queryAll(query) ) {
            //use the generic envelope calculation
            return super.getEnvelope(query);
        }

        final Comparator<Identifier> identifierComparator = new IdentifierComparator();
        final Set<Identifier> fids = (Set<Identifier>) filter.accept(
                IdCollectorFilterVisitor.IDENTIFIER_COLLECTOR, new TreeSet<Identifier>(identifierComparator));

        final Set records = new HashSet();
        if (!fids.isEmpty()) {
            Collection<Data> recordsFound = null;
            try {
                recordsFound = queryFidIndex(fids);
            } catch (IOException ex) {
                throw new DataStoreException(ex);
            }
            if (recordsFound != null) {
                records.addAll(recordsFound);
            }
        }

        if (records.isEmpty()) return null;

        ShapefileReader reader = null;
        try {
            reader = new ShapefileReader(shpFiles, false, false);

            final JTSEnvelope2D ret = new JTSEnvelope2D(getFeatureType(getNames().iterator().next()).getCoordinateReferenceSystem());
            for(final Iterator iter = records.iterator(); iter.hasNext();) {
                final Data data = (Data) iter.next();
                reader.goTo(((Long) data.getValue(1)).intValue());
                final Record record = reader.nextRecord();
                ret.expandToInclude(record.minX,record.minY);
                ret.expandToInclude(record.maxX,record.maxY);
            }
            return ret;
        } catch(IOException ex){
            throw new DataStoreException(ex);
        } finally {
            //todo replace by ARM in JDK 1.7
            if(reader != null){
                try {
                    reader.close();
                } catch (IOException ex) {
                    throw new DataStoreException(ex);
                }
            }
        }
    }

    /**
     * Builds the QuadTree index. Usually not necessary since reading features
     * will index when required
     * 
     * @param maxDepth depth of the tree. if < 0 then a best guess is made.
     * @throws TreeException
     */
    public void buildQuadTree(int maxDepth) throws TreeException {
        if (shpFiles.isLocal()) {
            getLogger().fine("Creating spatial index for " + shpFiles.get(SHP));

            final ShapeFileIndexer indexer = new ShapeFileIndexer();
            indexer.setIdxType(IndexType.QIX);
            indexer.setShapeFileName(shpFiles);
            indexer.setMax(maxDepth);

            try {
                indexer.index(false, new NullProgressListener());
            } catch (MalformedURLException e) {
                throw new TreeException(e);
            } catch (LockTimeoutException e) {
                throw new TreeException(e);
            } catch (Exception e) {
                if (e instanceof TreeException) {
                    throw (TreeException) e;
                } else {
                    throw new TreeException(e);
                }
            }
        }
    }
    
}
