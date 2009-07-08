/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2007 - 2008, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2008 - 2009, Johann Sorel
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
package org.geotoolkit.gui.swing.propertyedit;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.util.Iterator;
import java.util.Set;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;

import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.util.SimpleInternationalString;
import org.geotoolkit.data.AbstractDataStore;
import org.geotools.data.DataStore;
import org.geotools.data.ServiceInfo;
import org.geotoolkit.gui.swing.resource.MessageBundle;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.MapLayer;

/**
 * layer general information panel
 * 
 * @author Johann Sorel
 */
public class LayerGeneralPanel extends javax.swing.JPanel implements PropertyPane {

    private MapLayer layer = null;
    private final String title;

    /** Creates new form LayerGeneralPanel */
    public LayerGeneralPanel() {
        initComponents();
        title = MessageBundle.getString("property_general_title");        
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {



        jScrollPane1 = new JScrollPane();
        jPanel2 = new JPanel();
        jLabel15 = new JLabel();
        gui_jtf_name = new JTextField();
        jLabel16 = new JLabel();
        jtf_info_title = new JTextField();
        jLabel17 = new JLabel();
        jScrollPane12 = new JScrollPane();
        jtf_info_source = new JTextArea();
        jLabel18 = new JLabel();
        jScrollPane13 = new JScrollPane();
        jtf_info_schema = new JTextArea();
        jLabel19 = new JLabel();
        jScrollPane14 = new JScrollPane();
        jtf_info_description = new JTextArea();
        jLabel20 = new JLabel();
        jScrollPane15 = new JScrollPane();
        jtf_info_keyword = new JTextArea();
        jLabel21 = new JLabel();
        jScrollPane16 = new JScrollPane();
        jtf_info_publisher = new JTextArea();

        setLayout(new BorderLayout());

        jLabel15.setFont(jLabel15.getFont().deriveFont(jLabel15.getFont().getStyle() | Font.BOLD));
        jLabel15.setText(MessageBundle.getString("property_title")); // NOI18N
        jLabel16.setText(MessageBundle.getString("property_info_title")); // NOI18N
        jtf_info_title.setEditable(false);
        jtf_info_title.setOpaque(false);


        jLabel17.setText(MessageBundle.getString("property_info_source")); // NOI18N
        jScrollPane12.setOpaque(false);

        jtf_info_source.setColumns(20);
        jtf_info_source.setEditable(false);
        jtf_info_source.setRows(3);
        jScrollPane12.setViewportView(jtf_info_source);


        jLabel18.setText(MessageBundle.getString("property_info_schema")); // NOI18N
        jtf_info_schema.setColumns(20);
        jtf_info_schema.setEditable(false);
        jtf_info_schema.setRows(3);
        jScrollPane13.setViewportView(jtf_info_schema);


        jLabel19.setText(MessageBundle.getString("property_info_description")); // NOI18N
        jtf_info_description.setColumns(20);
        jtf_info_description.setEditable(false);
        jtf_info_description.setRows(3);
        jScrollPane14.setViewportView(jtf_info_description);


        jLabel20.setText(MessageBundle.getString("property_info_keyword")); // NOI18N
        jtf_info_keyword.setColumns(20);
        jtf_info_keyword.setEditable(false);
        jtf_info_keyword.setRows(3);
        jScrollPane15.setViewportView(jtf_info_keyword);


        jLabel21.setText(MessageBundle.getString("property_info_publisher")); // NOI18N
        jtf_info_publisher.setColumns(20);
        jtf_info_publisher.setEditable(false);
        jtf_info_publisher.setRows(3);
        jScrollPane16.setViewportView(jtf_info_publisher);

        GroupLayout jPanel2Layout = new GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel15)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(gui_jtf_name, GroupLayout.DEFAULT_SIZE, 339, Short.MAX_VALUE))
                    .addComponent(jScrollPane15, GroupLayout.DEFAULT_SIZE, 392, Short.MAX_VALUE)
                    .addComponent(jLabel16)
                    .addComponent(jLabel17)
                    .addComponent(jLabel18)
                    .addComponent(jLabel19)
                    .addComponent(jScrollPane14, GroupLayout.DEFAULT_SIZE, 392, Short.MAX_VALUE)
                    .addComponent(jLabel21)
                    .addComponent(jScrollPane16, GroupLayout.DEFAULT_SIZE, 392, Short.MAX_VALUE)
                    .addComponent(jLabel20)
                    .addComponent(jScrollPane13, GroupLayout.DEFAULT_SIZE, 392, Short.MAX_VALUE)
                    .addComponent(jScrollPane12, GroupLayout.DEFAULT_SIZE, 392, Short.MAX_VALUE)
                    .addComponent(jtf_info_title, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 392, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(gui_jtf_name, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(jLabel16)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(jtf_info_title, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(jLabel17)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(jScrollPane12, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(jLabel18)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(jScrollPane13, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(jLabel19)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(jScrollPane14, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(jLabel21)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(jScrollPane16, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(jLabel20)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(jScrollPane15, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(24, Short.MAX_VALUE))
        );

        jScrollPane1.setViewportView(jPanel2);

        add(jScrollPane1, BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    private void parse() {
        if (layer != null) {
            gui_jtf_name.setText(layer.getDescription().getTitle().toString());


            if(layer instanceof FeatureMapLayer){
                DataStore store = (DataStore) ((FeatureMapLayer)layer).getFeatureSource().getDataStore();

                if (store instanceof AbstractDataStore) {
                    ServiceInfo info = ((AbstractDataStore) store).getInfo();

                try {
                    jtf_info_title.setText(info.getTitle());
                } catch (Exception e) {
                    jtf_info_title.setText("Error : " + e.getMessage());
                }
                try {
                    jtf_info_source.setText(info.getSource().toString());
                } catch (Exception e) {
                    jtf_info_source.setText("Error : " + e.getMessage());
                }
                try {
                    jtf_info_schema.setText(info.getSchema().toString());
                } catch (Exception e) {
                    jtf_info_schema.setText("Error : " + e.getMessage());
                }
                try {
                    jtf_info_description.setText(info.getDescription());
                } catch (Exception e) {
                    jtf_info_description.setText("Error : " + e.getMessage());
                }
                try {
                    Set<String> set = info.getKeywords();
                    Iterator<String> ite = set.iterator();
                    while (ite.hasNext()) {
                        jtf_info_keyword.append(ite.next() + " ; ");
                    }
                } catch (Exception e) {
                    jtf_info_keyword.setText("Error : " + e.getMessage());
                }
                try {
                    jtf_info_publisher.setText(info.getPublisher().toString());
                } catch (Exception e) {
                    jtf_info_publisher.setText("Error : " + e.getMessage());
                }
                }


            }

        } else {
            gui_jtf_name.setText("");
            jtf_info_description.setText("");
            jtf_info_keyword.setText("");
            jtf_info_publisher.setText("");
            jtf_info_schema.setText("");
            jtf_info_source.setText("");
            jtf_info_title.setText("");
        }
    }

    @Override
    public void setTarget(Object target) {
        if (target instanceof MapLayer) {
            layer = (MapLayer) target;
        } else {
            layer = null;
        }
        parse();
    }

    @Override
    public void apply() {
        if (layer != null) {
            layer.setDescription(FactoryFinder.getStyleFactory(null).description(
                    new SimpleInternationalString(gui_jtf_name.getText()),
                    new SimpleInternationalString("")));
        }
    }

    @Override
    public void reset() {
        parse();
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public ImageIcon getIcon() {
        return null;
    }

    @Override
    public String getToolTip() {
        return title;
    }

    @Override
    public Component getComponent() {
        return this;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JTextField gui_jtf_name;
    private JLabel jLabel15;
    private JLabel jLabel16;
    private JLabel jLabel17;
    private JLabel jLabel18;
    private JLabel jLabel19;
    private JLabel jLabel20;
    private JLabel jLabel21;
    private JPanel jPanel2;
    private JScrollPane jScrollPane1;
    private JScrollPane jScrollPane12;
    private JScrollPane jScrollPane13;
    private JScrollPane jScrollPane14;
    private JScrollPane jScrollPane15;
    private JScrollPane jScrollPane16;
    private JTextArea jtf_info_description;
    private JTextArea jtf_info_keyword;
    private JTextArea jtf_info_publisher;
    private JTextArea jtf_info_schema;
    private JTextArea jtf_info_source;
    private JTextField jtf_info_title;
    // End of variables declaration//GEN-END:variables
}
