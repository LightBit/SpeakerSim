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

public class Resistor extends Filter
{
    public double R;
    
    public Resistor()
    {
        super();
        R = 0;
    }
    
    public Resistor(JsonValue json)
    {
        JsonObject jsonObj = json.asObject();
        R = jsonObj.getDouble("R", 0);
        super.fromJSON(json);
    }
    
    @Override
    public JsonValue toJSON()
    {
        JsonObject json = Json.object();
        
        json.add("R", R);
        json.add("Children", Item.childrenToJSON(children));
        
        return json;
    }

    @Override
    public Complex thisFilter(double f)
    {
        Complex z = super.impedance(f);
        return z.divide(z.add(R));
    }

    @Override
    public Complex impedance(double f)
    {
        return super.impedance(f).add(R);
    }
    
    @Override
    public String toString()
    {
        return "Resistor (" + Fnc.decimalFormat(R) + "Ω)";
    }
}
