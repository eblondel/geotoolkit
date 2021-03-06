/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2011, Geomatys
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
package org.geotoolkit.feature.type;

import java.util.List;
import org.apache.sis.internal.util.UnmodifiableArrayList;
import org.apache.sis.util.ArraysExt;
import org.geotoolkit.feature.simple.DefaultSimpleFeatureType;
import org.geotoolkit.feature.type.AttributeDescriptor;
import org.geotoolkit.feature.type.AttributeType;
import org.geotoolkit.feature.type.GeometryDescriptor;
import org.geotoolkit.feature.type.Name;
import org.geotoolkit.feature.type.PropertyDescriptor;
import org.opengis.filter.Filter;
import org.opengis.util.InternationalString;

/**
 * SimpleFeatureType with modifiable properties.
 * This can be used when creating recursive types.
 *
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public class ModifiableSimpleFeaturetype extends DefaultSimpleFeatureType implements ModifiableType {

    private AttributeType parent;

    public ModifiableSimpleFeaturetype(final Name name, final List<AttributeDescriptor> schema,
            final GeometryDescriptor defaultGeometry, final boolean isAbstract,
            final List<Filter> restrictions, final AttributeType superType, final InternationalString description) {
        super(name, schema, defaultGeometry, isAbstract, restrictions, superType, description);
    }

    @Override
    public void changeProperty(final int index, PropertyDescriptor desc) {
        if(desc==null){
            descriptors = ArraysExt.remove(descriptors, index, 1);
        }else{
            descriptors[index] = desc;
        }
        this.descriptorsList = UnmodifiableArrayList.wrap(this.descriptors);
        
        //for faster access
        types = new AttributeType[descriptors.length];
        for(int i=0; i<descriptors.length;i++){
            types[i] = (AttributeType) descriptors[i].getType();
        }
        typesList = UnmodifiableArrayList.wrap(types);
        
        rebuildPropertyMap();
    }

    @Override
    public List<PropertyDescriptor> getDescriptors() {
        return descriptorsList;
    }

    @Override
    public void changeParent(AttributeType parent) {
        this.parent = parent;
    }

    @Override
    public AttributeType getSuper() {
        if(this.parent != null){
            return parent;
        }else{
            return super.getSuper();
        }
    }

}
