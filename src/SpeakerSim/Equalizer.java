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

public class Equalizer implements IActiveFilter
{
    private double F;
    private double Q;
    private double a;
    private double c;
    protected boolean linearPhase;
    
    private void _set(double F, double Q, double dB)
    {
        this.F = F;
        this.Q = Q;
        
        a = Fnc.toAmplitude(dB);
        c = 1 / Math.sqrt(a) * Math.sinh(Math.log(2) / 2 * (1 / Q));
    }
    
    public Equalizer()
    {
        _set(1000, 1, 0);
    }

    public Equalizer(JsonValue json)
    {
        JsonObject jsonObj = json.asObject();
        
        _set(jsonObj.get("F").asDouble(), jsonObj.get("Q").asDouble(), jsonObj.get("dB").asDouble());
    }
    
    public void set(double F, double Q, double dB)
    {
        _set(F, Q, dB);
    }
    
    public double getF()
    {
        return F;
    }
    
    public double getQ()
    {
        return Q;
    }
    
    public boolean isLinearPhase()
    {
        return linearPhase;
    }
    
    public void setLinearPhase(boolean linearPhase)
    {
        this.linearPhase = linearPhase;
    }
    
    public double getDecibels()
    {
        return Fnc.toDecibels(a);
    }
    
    private double phase(double f)
    {
        if (isLinearPhase())
        {
            return 0;
        }
        else
        {
            double fn = f / this.F;
            return -Math.atan(((1 - fn * fn) * 2 * c * fn * (a - 1)) / (Math.pow(1 - fn * fn, 2) + 4 * c * c * fn * 2 * a));
        }
    }
    
    private double amplitude(double f)
    {
        double fn = f / this.F;
        fn *= fn;
        return Math.sqrt((fn * fn - 2 * fn + 4 * a * a * c * c * fn + 1) / (fn * fn - 2 * fn + 4 * c * c * fn + 1));
    }
    
    @Override
    public Complex response(double f)
    {
        return Complex.toComplex(amplitude(f), phase(f));
    }
    
    @Override
    public String toString()
    {
        return "Equalizer (" + Fnc.twoDecimalFormat(getDecibels()) + "dB at " + Fnc.twoDecimalFormat(getF()) + "Hz)";
    }
    
    @Override
    public JsonValue toJSON()
    {
        JsonObject json = Json.object();
        
        json.add("F", Json.value(getF()));
        json.add("Q", Json.value(getQ()));
        json.add("dB", Json.value(getDecibels()));
        
        return json;
    }
}
