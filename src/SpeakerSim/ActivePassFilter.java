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

public abstract class ActivePassFilter implements IActiveFilter
{
    private int type;
    private double frequency;
    
    public final static String[] TYPES =
    {
        "First order Butterworth",
        "Second order Butterworth",
        "Second order Bessel",
        "Second order Linkwitz-Riley",
        "Third order Butterworth",
        "Fourth order Butterworth",
        "Fourth order Bessel",
        "Fourth order Linkwitz-Riley"
    };
    
    private final static double[][] CONST =
    {
        {1, 0, 0, 0},
        {1.4142, 1, 0, 0},
        {1.7321, 1, 0, 0},
        {2, 1, 0, 0},
        {2, 2, 1, 0},
        {2.6132, 3.4142, 2.6132, 1},
        {3.24, 4.5, 3.24, 1.05},
        {2.8284, 4, 2.8284, 1}
    };
    
    public ActivePassFilter()
    {
        type = 0;
        frequency = 1000;
    }
    
    public ActivePassFilter(JsonValue json)
    {
        JsonObject jsonObj = json.asObject();
        
        setType(jsonObj.get("Type").asString());
        setFrequency(jsonObj.get("Frequency").asDouble());
    }
    
    public final void setType(int type)
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
    
    public final int getType()
    {
        return type;
    }
    
    public final String getTypeString()
    {
        return TYPES[type];
    }
    
    public final void setFrequency(double f)
    {
        frequency = f;
    }
    
    public final double getFrequency()
    {
        return frequency;
    }
    
    protected Complex filter(double fn)
    {
        double fn2 = fn * fn;
        double fn3 = fn2 * fn;
        double fn4 = fn2 * fn2;
        double x = 1 + CONST[type][3] * fn4 - CONST[type][1] * fn2;
        double y = -CONST[type][2] * fn3 + CONST[type][0] * fn;
        
        return Fnc.toComplex(1 / Math.hypot(x, y), -Math.atan2(y, x));
    }
    
    public String name()
    {
        return "Pass filter";
    }
    
    @Override
    public String toString()
    {
        String s = name() + " (";
        
        s += getTypeString() + " at " + Fnc.decimalFormat(getFrequency()) + "Hz";
        
        return s + ")";
    }
    
    @Override
    public JsonValue toJSON()
    {
        JsonObject json = Json.object();
        
        json.add("Type", Json.value(getTypeString()));
        json.add("Frequency", Json.value(getFrequency()));
        
        return json;
    }
}
