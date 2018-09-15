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

public class ParallelNotchFilter extends NotchFilter
{
    public ParallelNotchFilter()
    {
        super();
    }
    
    public ParallelNotchFilter(JsonValue json)
    {
        super(json);
    }
    
    @Override
    protected Complex Z(double f)
    {
        Complex z = L != 0 ? Complex.divide(1, Fnc.zL(L, f)) : new Complex();
        
        if (C != 0)
        {
            z = z.add(Complex.divide(1, Fnc.zC(C, f)));
        }
        
        if (R != 0)
        {
            z = z.add(1 / R);
        }
        
        return z.isZero() ? z : Complex.divide(1, z);
    }
    
    @Override
    public String name()
    {
        return "Parallel notch filter";
    }
}
