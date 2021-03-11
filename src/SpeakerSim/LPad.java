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

public class LPad extends Filter
{
    public double Rs;
    public double Rp;
    
    public LPad()
    {
        super();
        Rs = 0;
        Rp = 0;
    }
    
    public LPad(JsonValue json)
    {
        JsonObject jsonObj = json.asObject();
        
        Rs = jsonObj.getDouble("Rs", 0);
        Rp = jsonObj.getDouble("Rp", 0);
        
        super.fromJSON(json);
    }
    
    @Override
    protected JsonObject itemToJSON()
    {
        JsonObject json = super.itemToJSON();
        
        json.add("Rs", Rs);
        json.add("Rp", Rp);
        
        return json;
    }

    @Override
    public Complex thisFilter(double f)
    {
        Complex z = super.itemImpedance(f);

        if (Rp > 0)
        {
            z = z.multiply(Rp).divide(z.add(Rp));
        }
        z = z.divide(z.add(Rs));
        
        return z;
    }

    @Override
    protected Complex itemImpedance(double f)
    {
        Complex z = super.itemImpedance(f);

        if (Rp > 0)
        {
            z = z.multiply(Rp).divide(z.add(Rp));
        }
        z = z.add(Rs);

        return z;
    }
    
    @Override
    public String toString()
    {
        return "L pad (" + Fnc.twoDecimalFormat(Rs) + "Ω / " + Fnc.twoDecimalFormat(Rp) + "Ω)";
    }
    
    public double Zmin()
    {
        if (getChildren().isEmpty())
        {
            return 0;
        }
        
        double f = Settings.getInstance().StartFrequency;
        double m = Settings.getInstance().multiplier();
        double[] z = new double[Settings.getInstance().Points - 1];
        
        for (int i = 0; i < z.length; i++)
        {
            z[i] = super.itemImpedance(f).abs();
            f *= m;
        }
        
        z = Fnc.smooth(z, (int)Math.round(Settings.getInstance().pointsPerOctave()));
        
        return Fnc.min(z);
    }
}
