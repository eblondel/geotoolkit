/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2008 - 2009, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotoolkit.sml.xml.v101;

import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import org.geotoolkit.sml.xml.AbstractComponent;
import org.geotoolkit.sml.xml.AbstractInputs;
import org.geotoolkit.sml.xml.AbstractOutputs;
import org.geotoolkit.sml.xml.AbstractParameters;
import org.apache.sis.util.ComparisonMode;


/**
 * Complex Type for all generic components (soft typed inputs/outputs/parameters)
 * 
 * <p>Java class for AbstractComponentType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AbstractComponentType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/sensorML/1.0.1}AbstractDerivableComponentType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/sensorML/1.0.1}inputs" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/sensorML/1.0.1}outputs" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/sensorML/1.0.1}parameters" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 * @module pending
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AbstractComponentType")
@XmlSeeAlso({
    ComponentType.class,
    SystemType.class
})
public abstract class AbstractComponentType extends AbstractDerivableComponentType implements AbstractComponent {

    private Inputs inputs;
    private Outputs outputs;
    private Parameters parameters;

    public AbstractComponentType() {

    }

    public AbstractComponentType(final AbstractComponent ac) {
        super(ac);
        if (ac != null) {
            if (ac.getInputs() != null) {
                this.inputs = new Inputs(ac.getInputs());
            }
            if (ac.getOutputs() != null) {
                this.outputs = new Outputs(ac.getOutputs());
            }
            if (ac.getParameters() != null) {
                this.parameters = new Parameters(ac.getParameters());
            }
        }
    }

    /**
     * @return the inputs
     */
    @Override
    public Inputs getInputs() {
        return inputs;
    }

    /**
     * @param inputs the inputs to set
     */
    @Override
    public void setInputs(final AbstractInputs inputs) {
        this.inputs = new Inputs(inputs);
    }

    /**
     * @return the outputs
     */
    @Override
    public Outputs getOutputs() {
        return outputs;
    }

    /**
     * @param outputs the outputs to set
     */
    @Override
    public void setOutputs(final AbstractOutputs outputs) {
        this.outputs = new Outputs(outputs);
    }

    /**
     * @return the parameters
     */
    @Override
    public Parameters getParameters() {
        return parameters;
    }

    /**
     * @param parameters the parameters to set
     */
    @Override
    public void setParameters(final AbstractParameters parameters) {
        this.parameters = new Parameters(parameters);
    }

    /**
     * Verify if this entry is identical to specified object.
     */
    @Override
    public boolean equals(final Object object, final ComparisonMode mode) {
        if (object == this) {
            return true;
        }

        if (object instanceof AbstractComponentType && super.equals(object, mode)) {
            final AbstractComponentType that = (AbstractComponentType) object;
            return Objects.equals(this.inputs,     that.inputs)     &&
                   Objects.equals(this.outputs,    that.outputs)    &&
                   Objects.equals(this.parameters, that.parameters);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (this.inputs != null ? this.inputs.hashCode() : 0);
        hash = 97 * hash + (this.outputs != null ? this.outputs.hashCode() : 0);
        hash = 97 * hash + (this.parameters != null ? this.parameters.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder(super.toString());
        if (outputs != null)
            s.append("outputs:").append(outputs).append('\n');
        if (inputs != null)
            s.append("inputs:").append(inputs).append('\n');
        if (parameters != null)
            s.append("parameters:").append(parameters).append('\n');

        return s.toString();
    }

}