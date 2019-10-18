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

public abstract class NotchFilter extends Filter
{
    public double L;
    public double C;
    public double R;
    
    public NotchFilter()
    {
        super();
        L = 0;
        C = 0;
        R = 0;
    }
    
    public NotchFilter(JsonValue json)
    {
        JsonObject jsonObj = json.asObject();
        
        L = jsonObj.getDouble("L", 0);
        C = jsonObj.getDouble("C", 0);
        R = jsonObj.getDouble("R", 0);
        
        super.fromJSON(json);
    }
    
    @Override
    public JsonValue toJSON()
    {
        JsonObject json = Json.object();
        
        json.add("L", L);
        json.add("C", C);
        json.add("R", R);
        json.add("Children", Item.childrenToJSON(children));
        
        return json;
    }
    
    protected Complex Z(double f)
    {
        return new Complex();
    }

    @Override
    public Complex thisFilter(double f)
    {
        Complex z = super.impedance(f);
        return z.divide(z.add(Z(f)));
    }

    @Override
    public Complex impedance(double f)
    {
        return super.impedance(f).add(Z(f));
    }
    
    public String name()
    {
        return "Notch filter";
    }
    
    @Override
    public String toString()
    {
        String s = name() + " (";
        
        if (L != 0)
        {
            s += Fnc.twoDecimalFormat(L * 1000) + "mH / ";
        }

        if (C != 0)
        {
            s += Fnc.twoDecimalFormat(C * 1000000) + "μF / ";
        }

        if (R != 0)
        {
            s += Fnc.twoDecimalFormat(R) + "Ω / ";
        }

        if (L == 0 && C == 0 && R == 0)
        {
            return s + ")";
        }
        
        return s.substring(0, s.length() - 3) + ")";
    }
}
