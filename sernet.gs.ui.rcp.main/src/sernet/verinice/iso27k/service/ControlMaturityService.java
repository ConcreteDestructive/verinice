/**
 * Copyright 2010 Alexander Koderman.
 *
 * This file is part of Verinice.
 *
 * Verinice is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Verinice is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Verinice. If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *  - Alexander Koderman - Initial API and implementation
 */

package sernet.verinice.iso27k.service;

import sernet.hui.common.VeriniceContext;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.IControl;
import sernet.verinice.model.iso27k.IISRControl;

/**
 * Calculate maturity values and weights for controls and control groups.
 * 
 * @author Alexander Koderman
 */
public class ControlMaturityService {
    
    private int type = 0;
    
    public static final int TYPE_MATURITY = 0;
    public static final int TYPE_ISR = 1;
    
    public enum DecoratorColor {
        NULL, GREEN, YELLOW, RED
    }
    
    private enum Maturity {
        NOT_EDITED(-2), NA(-1);
        
        private int specialMaturityValue;
        
        Maturity(int specialMaturityValue) {
            this.specialMaturityValue = specialMaturityValue;
        }

        public int getValue() {
            return specialMaturityValue;
        }
    }
    
    public ControlMaturityService() {
        this(TYPE_MATURITY);
    }
    
    public ControlMaturityService(int type) {
        this.type = type;
    }
    
    
    /**
     * Calculate accumulated maturity times weight of each control contained in this group.
     * @return the calculated maturity times the weights for each control
     */
    public Integer getWeightedMaturity(ControlGroup cg) {
        int maturity = 0;
        for (CnATreeElement child : cg.getChildren()) {
            if (child instanceof IControl) {
                IControl control = (IControl) child;
                maturity += getWeightedMaturity(control);
            }
            if (child instanceof ControlGroup) {
                ControlGroup control = (ControlGroup) child;
                maturity += getWeightedMaturity(control);
            }
        }
        return maturity;
    }
    
    /**
     * Calculate accumulated maturity of each control contained in this group.
     * 
     * @return the calculated maturity for each control
     */
    public Integer getMaturity(ControlGroup cg) {
        int maturity = 0;
        for (CnATreeElement child : cg.getChildren()) {
            if (child instanceof IControl) {
                int m = getMaturity((IControl) child);
                if (isNotSpecialMaturityValue(m)) {
                    maturity += m;
                }
            }
            if (child instanceof ControlGroup) {
                maturity += getMaturity((ControlGroup) child);
            }
        }
        return maturity;
    }
    
    /**
     * Calculates the average maturity of the <code>IControl</code> children of <code>controlGroup</code> and
     * all sub groups of <code>controlGroup</code>.
     * 
     * @return averageMaturity
     */
    public Double getAvgMaturity(ControlGroup controlGroup) {
        Double averageMaturity;
        int accumulativeMaturity = 0;
        int count = 0;
        for (CnATreeElement child : controlGroup.getChildren()) {
            if (child instanceof IControl) {
                int maturity = getMaturity((IControl) child);
                if (isNotSpecialMaturityValue(maturity)) {
                    accumulativeMaturity += maturity;
                    count += 1;
                }
            }
            if (child instanceof ControlGroup) {
                accumulativeMaturity += getMaturity((ControlGroup) child);
                count += getControlCount((ControlGroup) child);
            }
        }
        averageMaturity = Double.valueOf(accumulativeMaturity / (double) count);
        return averageMaturity;
    }
    
    public double getAverageTargetMaturity(ControlGroup controlGroup) {
        double averageTargetMaturity;
        int accumulativeTargetMaturity = 0;
        int count = 0;
        for (CnATreeElement child : controlGroup.getChildren()) {
            if (child instanceof IControl) {
                int targetMaturity = getTargetMaturity((IControl) child);
                accumulativeTargetMaturity += targetMaturity;
                count += 1;
            }
            if (child instanceof ControlGroup) {
                accumulativeTargetMaturity += getAverageTargetMaturity((ControlGroup) child);
                count += 1;
            }
        }
        averageTargetMaturity = (double) accumulativeTargetMaturity / (double) count;
        return averageTargetMaturity;
    }

    private boolean isNotSpecialMaturityValue(int targetMaturity) {
        return targetMaturity != Maturity.NA.getValue() && targetMaturity != Maturity.NOT_EDITED.getValue();
    }
    
    public int getControlCount(ControlGroup cg){
        int controlCount = 0;
        for(CnATreeElement child : cg.getChildren()){
            if(child instanceof IControl){
                controlCount += 1;
            } else if(child instanceof ControlGroup){
                controlCount += getControlCount((ControlGroup)child);
            }
        }
        return controlCount;
    }
    
    public int getMaturity(IControl control) {
        if (this.type == TYPE_ISR) {
            IISRControl isrControl = (IISRControl) control;
            return isrControl.getISRMaturity();
        } 
        else {
            return control.getMaturity();
        }
    }
    
    public Double getMaturityByWeight(ControlGroup cg) {
        double result =0;
        if (getWeights(cg) != 0){
            result = ((double)getWeightedMaturity(cg)) / ((double)getWeights(cg));
        }
        return result;
    }

    /**
     * Return sum of all weights of all controls contained in this group and subgroups.
     * @return combined weight
     */
    public Integer getWeights(ControlGroup cg) {
        int weight = 0;
        for (CnATreeElement child : cg.getChildren()) {
            if (child instanceof IControl) {
                IControl control = (IControl) child;
                weight += control.getWeight2();
            }
            if (child instanceof ControlGroup) {
                ControlGroup control = (ControlGroup) child;
                weight += getWeights(control);
            }
        }
        return weight;
    }
    
    public Integer getWeightedMaturity(IControl contr) {
        return getMaturity(contr) * contr.getWeight2();
    }
    
    public Double getMaturityByWeight(IControl contr) {
        return ((double)getWeightedMaturity(contr)) / ((double)contr.getWeight2());
    }

    public Double getMaxMaturityValue(ControlGroup group) {
        Double result = Double.valueOf(0);
        for (CnATreeElement child : group.getChildren()) {
            if (child instanceof IControl) {
                Double maturity = getMaxMaturityValue((IControl) child);
                if(maturity > result) {
                    result = maturity;
                }
            }
            if (child instanceof ControlGroup) {
                Double maturity = getMaxMaturityValue((ControlGroup) child);
                if(maturity > result) {
                    result = maturity;
                }
            }
        }
        return result;
    }
    
    private Double getMaxMaturityValue(IControl control) {
        HUITypeFactory hui = (HUITypeFactory) VeriniceContext.get(VeriniceContext.HUI_TYPE_FACTORY);
        PropertyType propertyType;
        if (type == TYPE_MATURITY) {
            propertyType = hui.getPropertyType(control.getTypeId(), control.getMaturityPropertyId());
        }
        else {
            propertyType = hui.getPropertyType(control.getTypeId(), ((IISRControl)control).getISRPropertyId());
        }
        return Double.valueOf(propertyType.getMaxValue());
    }
    
    public int getThreshold1(ControlGroup group) {
        int result = 0;
        for (CnATreeElement child : group.getChildren()) {
            if (child instanceof IControl) {
                IControl control = (IControl)child;
                // don't add threshold if maturity is NA
                if(getMaturity(control)!=IControl.IMPLEMENTED_NA_NUMERIC) {
                    result += control.getThreshold1();
                }
            }
            if (child instanceof ControlGroup) {
                result += getThreshold1((ControlGroup)child);
            }
        }
        return result;
    }
    
    public int getThreshold2(ControlGroup group) {
        int result = 0;
        for (CnATreeElement child : group.getChildren()) {
            if (child instanceof IControl) {
                IControl control = (IControl)child;
                // don't add threshold if maturity is NA
                if(getMaturity(control)!=IControl.IMPLEMENTED_NA_NUMERIC) {
                    result += control.getThreshold2();
                }
            }
            if (child instanceof ControlGroup) {
                result+= getThreshold2((ControlGroup)child);
            }
        }
        return result;
    }
    
    /**
     * Returns the implementaiton state based on the maturity level of the <code>IControl.</code>
     * 
     * @param control
     * @return the implementation state as definied in the <code>IControl.IMPLEMENTED</code> constants.
     */
    public String getImplementationState(ControlGroup group) {

        String state = IControl.IMPLEMENTED_NO;
        int threshold1 = getThreshold1(group);
        int maturity = getMaturity(group);
        if (maturity >= threshold1) {
            state = IControl.IMPLEMENTED_PARTLY;
            int threshold2 = getThreshold2(group);
            if (maturity >= threshold2) {
                state = IControl.IMPLEMENTED_YES;
            }
        }
        return state;
    }
    
    /**
     * Returns the implementaiton state based on the maturity level of the <code>IControl.</code>
     * 
     * @param control
     * @return the implementation state as definied in the <code>IControl.IMPLEMENTED</code> constants.
     */
    public String getImplementationState(IControl control) {

        String state = IControl.IMPLEMENTED_NO;
        if (getMaturity(control) >= control.getThreshold1()) {
            state = IControl.IMPLEMENTED_PARTLY;
        }
        if (getMaturity(control) >= control.getThreshold2()) {
            state = IControl.IMPLEMENTED_YES;
        }
        if (getMaturity(control) == IControl.IMPLEMENTED_NA_NUMERIC) {
            state = IControl.IMPLEMENTED_YES;
        }
        return state;
    }
    
    /**
     * Returns the isa implementaiton state based on the maturity level of the <code>IControl.</code>
     * Changed to represent just the states "control has been edited or not".
     * 
     * @param control
     * @return the implementation state as definied in the <code>IControl.IMPLEMENTED</code> constants.
     */
    public String getIsaState(IControl control) {
        
        if (getMaturity(control) == IControl.IMPLEMENTED_NOTEDITED_NUMERIC) {
            return IControl.IMPLEMENTED_NOTEDITED;
        }
        return IControl.IMPLEMENTED_YES;
    }
    
    public int getTargetMaturity(IControl control) {
        return control.getThreshold2();
    }
    
    public DecoratorColor getDecoratorColor(IControl control) {
        int maturity = getMaturity(control);
        int getTargetMaturity = getTargetMaturity(control);
        return getDecoratorColor(maturity, getTargetMaturity);
    }
    
    public DecoratorColor getDecoratorColor(ControlGroup controlGroup) {
        Double averageMaturity = getAvgMaturity(controlGroup);
        Double averageTargetMaturity = getAverageTargetMaturity(controlGroup);
        
        return getDecoratorColor(averageMaturity, averageTargetMaturity);
    }
    
    private DecoratorColor getDecoratorColor(double maturity, double targetMaturity) {
        if (maturity == Double.NaN) {
            maturity = Maturity.NOT_EDITED.getValue();
        } else {
            maturity = Math.round(maturity);
        }

        if (maturity == Maturity.NOT_EDITED.getValue()) {
            return DecoratorColor.RED;
        } else if (maturity == Maturity.NA.getValue()) {
            return DecoratorColor.NULL;
        } else if (maturity <= targetMaturity - 2) {
            return DecoratorColor.RED;
        } else if (maturity == targetMaturity - 1) {
            return DecoratorColor.YELLOW;
        } else if (maturity >= targetMaturity) {
            return DecoratorColor.GREEN;
        }
        return DecoratorColor.NULL;
    }
}