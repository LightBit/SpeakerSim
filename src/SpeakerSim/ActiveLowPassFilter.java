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

import com.eclipsesource.json.JsonValue;

public final class ActiveLowPassFilter extends ActivePassFilter
{
    public ActiveLowPassFilter()
    {
        super();
    }
    
    public ActiveLowPassFilter(JsonValue json)
    {
        super(json);
    }
    
    @Override
    public Complex response(double f)
    {
        if (isCustom())
        {
            double f2 = f * f;
            double frequency2 = frequency * frequency;
            double frequency4 = frequency2 * frequency2;
            double t = 1 / q;
            t = frequency4 - frequency2 * f2 * (2 - t * t) + f2 * f2;
            
            double x = (frequency4 - frequency2 * f2) / t;
            double y = -f * frequency2 * frequency / q / t;
            return Complex.toComplex(Math.hypot(x, y), Math.atan2(y, x));
        }
        return super.filter(f / frequency);
    }
    
    @Override
    public String name()
    {
        return "Low pass filter";
    }
}
