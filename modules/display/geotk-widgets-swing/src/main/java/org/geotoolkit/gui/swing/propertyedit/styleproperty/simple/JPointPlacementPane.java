/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2012 Geomatys
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
package org.geotoolkit.gui.swing.propertyedit.styleproperty.simple;

import java.awt.Color;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import org.geotoolkit.gui.swing.resource.MessageBundle;
import org.geotoolkit.gui.swing.style.JNumberExpressionPane;
import org.geotoolkit.gui.swing.style.JNumberSliderExpressionPane;
import org.geotoolkit.gui.swing.style.StyleElementEditor;
import org.geotoolkit.map.MapLayer;
import org.opengis.style.PointPlacement;

/**
 * Point placement panel
 * 
 * @author Fabien Rétif (Geomatys)
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public class JPointPlacementPane extends StyleElementEditor<PointPlacement>{
    
    private MapLayer layer = null;
    
    /** Creates new form JPointPlacementPanel */
    public JPointPlacementPane() {
        super(PointPlacement.class);
        initComponents();
        guiRotation.setModel(0, 0, 360, 1);
        guiDisplacementX.setExpressionVisible(false);
        guiDisplacementY.setExpressionVisible(false);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new JLabel();
        guiRotation = new JNumberSliderExpressionPane();
        jLabel5 = new JLabel();
        guiDisplacementY = new JNumberExpressionPane();
        jLabel6 = new JLabel();
        guiDisplacementX = new JNumberExpressionPane();
        guiAnchor = new JAnchorPointPane();

        setBackground(new Color(204, 204, 204));
        setOpaque(false);

        jLabel1.setText(MessageBundle.getString("rotation")); // NOI18N

        guiRotation.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                JPointPlacementPane.this.propertyChange(evt);
            }
        });

        jLabel5.setText(MessageBundle.getString("displacementX")); // NOI18N

        guiDisplacementY.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                JPointPlacementPane.this.propertyChange(evt);
            }
        });

        jLabel6.setText(MessageBundle.getString("displacementY")); // NOI18N

        guiDisplacementX.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                JPointPlacementPane.this.propertyChange(evt);
            }
        });

        guiAnchor.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                JPointPlacementPane.this.propertyChange(evt);
            }
        });

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(guiAnchor, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(guiRotation, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(guiDisplacementY, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(guiDisplacementX, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {jLabel5, jLabel6});

        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(guiRotation, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(guiAnchor, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(Alignment.LEADING, false)
                    .addComponent(guiDisplacementY, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel5, GroupLayout.PREFERRED_SIZE, 26, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.LEADING, false)
                    .addComponent(guiDisplacementX, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel6, GroupLayout.PREFERRED_SIZE, 26, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(SwingConstants.VERTICAL, new Component[] {guiRotation, jLabel1});

    }// </editor-fold>//GEN-END:initComponents

    private void propertyChange(PropertyChangeEvent evt) {//GEN-FIRST:event_propertyChange
        if (PROPERTY_TARGET.equalsIgnoreCase(evt.getPropertyName())) {
            firePropertyChange(PROPERTY_TARGET, null, create());
        }
    }//GEN-LAST:event_propertyChange

    @Override
    public void setLayer(final MapLayer layer) {
        this.layer = layer;
        guiAnchor.setLayer(layer);        
        guiRotation.setLayer(layer);
        guiDisplacementX.setLayer(layer);
        guiDisplacementY.setLayer(layer);
    }

    @Override
    public MapLayer getLayer() {
        return layer;
    }

    @Override
    public void parse(final PointPlacement placement) {
        if(placement != null){
            guiAnchor.parse(placement.getAnchorPoint());
            guiDisplacementX.parse(placement.getDisplacement().getDisplacementX());
            guiDisplacementY.parse(placement.getDisplacement().getDisplacementY());
            guiRotation.parse(placement.getRotation());
        }
    }

    @Override
    public PointPlacement create() {
        return getStyleFactory().pointPlacement(
                guiAnchor.create(),
                getStyleFactory().displacement(guiDisplacementX.create(), guiDisplacementY.create()),
                guiRotation.create());
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JAnchorPointPane guiAnchor;
    private JNumberExpressionPane guiDisplacementX;
    private JNumberExpressionPane guiDisplacementY;
    private JNumberSliderExpressionPane guiRotation;
    private JLabel jLabel1;
    private JLabel jLabel5;
    private JLabel jLabel6;
    // End of variables declaration//GEN-END:variables
    
}