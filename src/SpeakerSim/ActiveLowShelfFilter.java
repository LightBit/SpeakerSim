/*
 * Copyright (C) 2026 Gregor Pintar <grpintar@gmail.com>
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

public final class ActiveLowShelfFilter extends ActiveShelfFilter
{
    public ActiveLowShelfFilter()
    {
        super();
    }

    public ActiveLowShelfFilter(JsonValue json)
    {
        super(json);
    }

    @Override
    protected Complex filter(double normalizedFrequency, double amplitude, double slope)
    {
        double x = normalizedFrequency;
        double x2 = x * x;

        Complex numerator = new Complex(1 - x2, slope * x);
        Complex denominator = new Complex(1 - amplitude * x2, slope * x);

        return numerator.divide(denominator).multiply(amplitude);
    }

    @Override
    protected String name()
    {
        return "Low shelf filter";
    }
}