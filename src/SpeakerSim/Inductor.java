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

public class Inductor extends Filter
{
    public double L;
    
    public Inductor()
    {
        super();
        L = 0;
    }
    
    public Inductor(JsonValue json)
    {
        JsonObject jsonObj = json.asObject();
        L = jsonObj.getDouble("L", 0);
        if (L == 0) L = jsonObj.getDouble("C", 0); // backwards compatibility (for bug)
        super.fromJSON(json);
    }
    
    @Override
    public JsonValue toJSON()
    {
        JsonObject json = Json.object();
        
        json.add("L", L);
        json.add("Children", Item.childrenToJSON(children));
        
        return json;
    }

    @Override
    public Complex thisFilter(double f)
    {
        if (L > 0)
        {
            Complex z = super.impedance(f);
            return z.divide(z.add(Fnc.zL(L, f)));
        }

        return super.filter(f);
    }

    @Override
    public Complex impedance(double f)
    {
        Complex z = super.impedance(f);

        if (L > 0)
        {
            z = z.add(Fnc.zL(L, f));
        }

        return z;
    }
    
    @Override
    public String toString()
    {
        return "Inductor (" + Fnc.decimalFormat(L * 1000) + "mH)";
    }
}
