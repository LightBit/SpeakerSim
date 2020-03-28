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

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

public class Capacitor extends Filter
{
    public double C;
    
    public Capacitor()
    {
        super();
        C = 0;
    }
    
    public Capacitor(JsonValue json)
    {
        JsonObject jsonObj = json.asObject();
        C = jsonObj.getDouble("C", 0);
        super.fromJSON(json);
    }
    
    @Override
    protected JsonObject itemToJSON()
    {
        JsonObject json = super.itemToJSON();
        
        json.add("C", C);
        
        return json;
    }

    @Override
    public Complex thisFilter(double f)
    {
        if (C > 0)
        {
            Complex z = super.itemImpedance(f);
            return z.divide(z.add(Fnc.zC(C, f)));
        }

        return new Complex(1);
    }

    @Override
    protected Complex itemImpedance(double f)
    {
        Complex z = super.itemImpedance(f);

        if (C > 0)
        {
            z = z.add(Fnc.zC(C, f));
        }

        return z;
    }
    
    @Override
    public String toString()
    {
        return "Capacitor (" + Fnc.twoDecimalFormat(C * 1000000) + "μF)";
    }
}
