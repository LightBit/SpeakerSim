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
    public double L1;
    public double L2;
    public double R1;
    public double R2;
    
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
        return z != 0 ? L * z / f : 0;
    }
    
    public static double calcC(double C, double z, double f)
    {
        return z != 0 ? C / (z * f) : 0;
    }
    
    public double calcC1(int type, double f)
    {
        return 0;
    }
    
    public double calcC2(int type, double f)
    {
        return 0;
    }
    
    public double calcL1(int type, double f)
    {
        return 0;
    }
    
    public double calcL2(int type, double f)
    {
        return 0;
    }
    
    protected Complex zC1(double f)
    {
        return Fnc.zC(C1, f);
    }
    
    protected Complex zC2(double f)
    {
        return Fnc.zC(C2, f);
    }
    
    protected Complex zL1(double f)
    {
        return Fnc.zL(L1, f).add(R1);
    }
    
    protected Complex zL2(double f)
    {
        return Fnc.zL(L2, f).add(R2);
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
        
        R1 = JSON.getDouble(jsonObj, "R1");
        R2 = JSON.getDouble(jsonObj, "R2");
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
            if (C2 != 0)
            {
                s += Fnc.decimalFormat(C2 * 1000000) + "μF / ";
            }
            
            if (L2 != 0)
            {
                s += Fnc.decimalFormat(L2 * 1000) + "mH / ";
            }
            
            if (C1 != 0)
            {
                s += Fnc.decimalFormat(C1 * 1000000) + "μF / ";
            }
            
            if (L1 != 0)
            {
                s += Fnc.decimalFormat(L1 * 1000) + "mH";
            }
        }
        else
        {
            s += getTypeString() + " at " + Fnc.decimalFormat(getFrequency()) + "Hz";
        }
        
        return s + ")";
    }
    
    @Override
    public JsonValue toJSON()
    {
        JsonObject json = Json.object();
        
        json.add("Type", Json.value(getTypeString()));
        json.add("Frequency", Json.value(getFrequency()));
        
        if (isCustom())
        {
            JSON.add(json, "C1", C1);
            JSON.add(json, "C2", C2);
            JSON.add(json, "L1", L1);
            JSON.add(json, "L2", L2);
        }
        
        JSON.add(json, "R1", R1);
        JSON.add(json, "R2", R2);
        json.add("Children", Item.childrenToJSON(children));
        
        return json;
    }
}
