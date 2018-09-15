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
        return super.filter(f / getFrequency());
    }
    
    @Override
    public String name()
    {
        return "Low pass filter";
    }
}
