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
    protected double frequency;
    protected double q;
    protected boolean linearPhase;
    
    public final static String[] TYPES =
    {
        "First order Butterworth",
        "Second order Butterworth",
        "Second order Bessel",
        "Second order Linkwitz-Riley",
        "Third order Butterworth",
        "Fourth order Butterworth",
        "Fourth order Bessel",
        "Fourth order Linkwitz-Riley",
        "Fifth order Butterworth",
        "Fifth order Bessel",
        "Sixth order Butterworth",
        "Sixth order Bessel",
        "Sixth order Linkwitz-Riley",
        "Eighth order Linkwitz-Riley",
        "Custom second order"
    };
    
    private final static double[][] CONST =
    {
        {1, 0, 0, 0, 0, 0, 0, 0},
        {1.4142, 1, 0, 0, 0, 0, 0, 0},
        {1.7321, 1, 0, 0, 0, 0, 0, 0},
        {2.0000, 1, 0, 0, 0, 0, 0, 0},
        {2.0000, 2.0000, 1, 0, 0, 0, 0, 0},
        {2.6132, 3.4142, 2.6132, 1, 0, 0, 0, 0},
        {3.2400, 4.5000, 3.2400, 1.0500, 0, 0, 0, 0},
        {2.8284, 4.0000, 2.8284, 1.0000, 0, 0, 0, 0},
        {3.2361, 5.2360, 5.2360, 3.2361, 1, 0, 0, 0},
        {3.9363, 6.8864, 6.7767, 3.8107, 1, 0, 0, 0.},
        {3.8637, 7.4641, 9.1416, 7.4641, 3.8637, 1, 0, 0},
        {4.6717, 9.9202, 12.3583, 9.6223, 4.4952, 1, 0, 0},
        {4.0000, 8.0000, 10.0000, 8.0000, 4.0000, 1, 0, 0},
        {5.2263, 13.6569, 23.0698, 27.3137, 23.0698, 13.6569, 5.2263, 1}
    };
    
    public static boolean isCustom(int type)
    {
        return type == TYPES.length - 1;
    }
    
    public final boolean isCustom()
    {
        return isCustom(type);
    }
    
    public boolean isLinearPhase()
    {
        return linearPhase;
    }
    
    public void setLinearPhase(boolean linearPhase)
    {
        this.linearPhase = linearPhase;
    }
    
    public ActivePassFilter()
    {
        type = 0;
        frequency = 1000;
        q = 0.707;
    }
    
    public ActivePassFilter(JsonValue json)
    {
        JsonObject jsonObj = json.asObject();
        
        setType(jsonObj.get("Type").asString());
        setFrequency(JSON.getDouble(jsonObj, "Frequency", 1000));
        setQ(JSON.getDouble(jsonObj, "Q", 0.707));
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
    
    public final void setQ(double q)
    {
        this.q = q;
    }
    
    public final double getQ()
    {
        return q;
    }
    
    protected Complex filter(double fn)
    {
        double fn2 = fn * fn;
        double fn3 = fn2 * fn;
        double fn4 = fn2 * fn2;
        double fn5 = fn4 * fn;
        double fn6 = fn3 * fn3;
        double fn7 = fn6 * fn;
        double fn8 = fn4 * fn4;
        
        double x = 1
                - CONST[type][1] * fn2
                + CONST[type][3] * fn4
                - CONST[type][5] * fn6
                + CONST[type][7] * fn8;
        
        double y = CONST[type][0] * fn
                - CONST[type][2] * fn3
                + CONST[type][4] * fn5
                - CONST[type][6] * fn7;
        
        Complex f = Complex.toComplex(1 / Math.hypot(x, y), -Math.atan2(y, x));
        
        if (isLinearPhase())
        {
            f = Complex.toComplex(f.abs(), 0);
        }
        
        return f;
    }
    
    public String name()
    {
        return "Pass filter";
    }
    
    @Override
    public String toString()
    {
        String s = name() + " (";
        
        s += isCustom() ? Fnc.twoDecimalFormat(getQ()) : getTypeString();
        s += " at " + Fnc.twoDecimalFormat(getFrequency()) + "Hz";
        
        return s + ")";
    }
    
    @Override
    public JsonValue toJSON()
    {
        JsonObject json = Json.object();
        
        json.add("Type", Json.value(getTypeString()));
        json.add("Frequency", Json.value(getFrequency()));
        json.add("Q", Json.value(getQ()));
        
        return json;
    }
}
