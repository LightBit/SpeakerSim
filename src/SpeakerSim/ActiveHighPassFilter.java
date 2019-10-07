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

public final class ActiveHighPassFilter extends ActivePassFilter
{
    public ActiveHighPassFilter()
    {
        super();
    }
    
    public ActiveHighPassFilter(JsonValue json)
    {
        super(json);
    }
    
    @Override
    public Complex response(double f)
    {
        if (isCustom())
        {
            double f2 = f * f;
            double f4 = f2 * f2;
            double frequency2 = frequency * frequency;
            double t = 1 / q;
            t = frequency2 * frequency2 - frequency2 * f2 * (2 - t * t) + f4;
            
            double x = (f4 - frequency2 * f2) / t;
            double y = frequency * f2 * f / q / t;
            return Complex.toComplex(Math.hypot(x, y), Math.atan2(y, x));
        }
        return super.filter(frequency / f);
    }
    
    @Override
    public String name()
    {
        return "High pass filter";
    }
}
