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
import com.eclipsesource.json.JsonValue;

public class Delay implements IActiveFilter
{
    public double time;
    
    public Delay()
    {
        time = 0;
    }

    public Delay(JsonValue json)
    {
        time = json.asDouble();
    }
    
    public static double calcDistance(double speedOfSound, double time)
    {
        return time * speedOfSound;
    }
    
    public static double calcTime(double speedOfSound, double distance)
    {
        return distance / speedOfSound;
    }
    
    @Override
    public Complex response(double f)
    {
        return Complex.toComplex(1, 2 * Math.PI * f * -time);
    }
    
    @Override
    public String toString()
    {
        return "Delay (" + Fnc.twoDecimalFormat(time * 1000) + "ms)";
    }
    
    @Override
    public JsonValue toJSON()
    {
        return Json.value(time);
    }
}
