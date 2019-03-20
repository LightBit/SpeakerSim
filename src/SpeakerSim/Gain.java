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
import com.eclipsesource.json.JsonValue;

public class Gain implements IActiveFilter
{
    public double dB;
    
    public Gain()
    {
        dB = 0;
    }

    public Gain(JsonValue json)
    {
        dB = json.asDouble();
    }
    
    @Override
    public Complex response(double f)
    {
        return new Complex(Fnc.toAmplitude(dB));
    }
    
    @Override
    public String toString()
    {
        return "Gain (" + Fnc.decimalFormat(dB) + "dB)";
    }
    
    @Override
    public JsonValue toJSON()
    {
        return Json.value(dB);
    }
}
