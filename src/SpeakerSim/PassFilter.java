/*
 * Copyright (C) 2017 Gregor Pintar <grpintar@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package SpeakerSim;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

public abstract class PassFilter extends Filter
{
    private int type;
    private double frequency;
    
    public double C1;
    public double C2;
    public double RC1;
    public double RC2;
    public double L1;
    public double L2;
    public double RL1;
    public double RL2;
    
    public final static String[] TYPES =
    {
        "First order Butterworth",
        "Second order Butterworth",
        "Second order Bessel",
        "Second order Linkwitz-Riley",
        "Second order Chebychev",
        "Third order Butterworth",
        "Fourth order Butterworth",
        "Fourth order Bessel",
        "Fourth order Linkwitz-Riley",
        "Fourth order Legendre",
        "Fourth order Gaussian",
        "Fourth order Linear-Phase",
        "Custom"
    };
    
    public static boolean isCustom(int type)
    {
        return type == TYPES.length - 1;
    }
    
    public final boolean isCustom()
    {
        return isCustom(type);
    }
    
    public static double calcL(double L, double z, double f)
    {
        L = L * z / f;
        return Double.isFinite(L) ? L : 0;
    }
    
    public static double calcC(double C, double z, double f)
    {
        C = C / (z * f);
        return Double.isFinite(C) ? C : 0;
    }
    
    public double calcC1(int type, double f)
    {
        throw new UnsupportedOperationException();
    }
    
    public double calcC2(int type, double f)
    {
        throw new UnsupportedOperationException();
    }
    
    public double calcL1(int type, double f)
    {
        throw new UnsupportedOperationException();
    }
    
    public double calcL2(int type, double f)
    {
        throw new UnsupportedOperationException();
    }
    
    protected Complex zC1(double f)
    {
        return Fnc.zC(C1, f).add(RC1);
    }
    
    protected Complex zC2(double f)
    {
        return Fnc.zC(C2, f).add(RC2);
    }
    
    protected Complex zL1(double f)
    {
        return Fnc.zL(L1, f).add(RL1);
    }
    
    protected Complex zL2(double f)
    {
        return Fnc.zL(L2, f).add(RL2);
    }
    
    public PassFilter()
    {
        super();
        type = 0;
        frequency = 1000;
    }
    
    public PassFilter(JsonValue json)
    {
        JsonObject jsonObj = json.asObject();
        
        setType(jsonObj.get("Type").asString());
        setFrequency(JSON.getDouble(jsonObj, "Frequency", 1000));
        
        if (isCustom())
        {
            C1 = JSON.getDouble(jsonObj, "C1");
            C2 = JSON.getDouble(jsonObj, "C2");
            L1 = JSON.getDouble(jsonObj, "L1");
            L2 = JSON.getDouble(jsonObj, "L2");
        }
        
        RC1 = JSON.getDouble(jsonObj, "RC1");
        RC2 = JSON.getDouble(jsonObj, "RC2");
        RL1 = JSON.getDouble(jsonObj, "RL1", JSON.getDouble(jsonObj, "R1"));
        RL2 = JSON.getDouble(jsonObj, "RL2", JSON.getDouble(jsonObj, "R2"));
        
        super.fromJSON(json);
    }
    
    public void setType(int type)
    {
        this.type = type;
    }
    
    public final void setType(String type)
    {
        for (int i = 0; i < TYPES.length; i++)
        {
            if (TYPES[i].equals(type))
            {
                setType(i);
                break;
            }
        }
    }
    
    public int getType()
    {
        return type;
    }
    
    public String getTypeString()
    {
        return TYPES[type];
    }
    
    public final void setFrequency(double f)
    {
        frequency = f;
    }
    
    public double getFrequency()
    {
        return frequency;
    }
    
    public String name()
    {
        return "Pass filter";
    }
    
    @Override
    public void refresh()
    {
        super.refresh();
       
        if (!isCustom())
        {
            C1 = calcC1(type, frequency);
            C2 = calcC2(type, frequency);
            L1 = calcL1(type, frequency);
            L2 = calcL2(type, frequency);
        }
    }
    
    @Override
    public String toString()
    {
        String s = name() + " (";
        
        if (isCustom())
        {
            if (C1 != 0)
            {
                s += Fnc.twoDecimalFormat(C1 * 1000000) + "μF / ";
            }
            
            if (L1 != 0)
            {
                s += Fnc.twoDecimalFormat(L1 * 1000) + "mH / ";
            }
            
            if (C2 != 0)
            {
                s += Fnc.twoDecimalFormat(C2 * 1000000) + "μF / ";
            }
            
            if (L2 != 0)
            {
                s += Fnc.twoDecimalFormat(L2 * 1000) + "mH / ";
            }
            
            s = s.substring(0, s.length() - 3);
        }
        else
        {
            s += getTypeString() + " at " + Fnc.twoDecimalFormat(getFrequency()) + "Hz";
        }
        
        return s + ")";
    }
    
    @Override
    protected JsonObject itemToJSON()
    {
        JsonObject json = super.itemToJSON();
        
        json.add("Type", Json.value(getTypeString()));
        json.add("Frequency", Json.value(getFrequency()));
        
        if (isCustom())
        {
            JSON.add(json, "C1", C1);
            JSON.add(json, "C2", C2);
            JSON.add(json, "L1", L1);
            JSON.add(json, "L2", L2);
        }
        
        JSON.add(json, "RC1", RC1);
        JSON.add(json, "RC2", RC2);
        JSON.add(json, "RL1", RL1);
        JSON.add(json, "RL2", RL2);
        
        return json;
    }
}
