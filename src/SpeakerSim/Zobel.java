/*
 * Copyright (C) 2018 Gregor Pintar <grpintar@gmail.com>
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

public class Zobel extends Filter
{
    public double R;
    public double C;
    
    public Zobel()
    {
        super();
        C = 0;
        R = 0;
    }
    
    public Zobel(JsonValue json)
    {
        JsonObject jsonObj = json.asObject();
        
        C = jsonObj.getDouble("C", 0);
        R = jsonObj.getDouble("R", 0);
        
        super.fromJSON(json);
    }
    
    @Override
    protected JsonObject itemToJSON()
    {
        JsonObject json = super.itemToJSON();
        
        json.add("C", C);
        json.add("R", R);
        
        return json;
    }
    
    public static double calcC(double Z, double Le)
    {
        return Le / (Z * Z);
    }
    
    protected Complex Z(double f)
    {
        return Fnc.zC(C, f).add(R);
    }

    @Override
    public Complex thisFilter(double f)
    {
        Complex z = super.itemImpedance(f);
        Complex z2 = Z(f);

        z = z.multiply(z2).divide(z.add(z2));
        z = z.divide(z);
        
        return z;
    }

    @Override
    public Complex itemImpedance(double f)
    {
        Complex z = super.itemImpedance(f);
        Complex z2 = Z(f);

        z = z.multiply(z2).divide(z.add(z2));
        
        return z;
    }
    
    @Override
    public String toString()
    {
        return "Zobel (" + Fnc.twoDecimalFormat(C * 1000000) + "μF / " + Fnc.twoDecimalFormat(R) + "Ω)";
    }
}
