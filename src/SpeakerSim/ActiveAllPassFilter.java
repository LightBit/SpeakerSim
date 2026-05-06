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

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

public final class ActiveAllPassFilter implements IActiveFilter
{
    private double frequency;
    private double q;
    private boolean linearPhase;

    public ActiveAllPassFilter()
    {
        frequency = 1000;
        q = 0.707;
    }

    public ActiveAllPassFilter(JsonValue json)
    {
        JsonObject jsonObj = json.asObject();

        frequency = JSON.getDouble(jsonObj, "Frequency", 1000);
        q = JSON.getDouble(jsonObj, "Q", 0.707);
        linearPhase = JSON.getBoolean(jsonObj, "LinearPhase", false);
    }

    public void setFrequency(double frequency)
    {
        this.frequency = frequency;
    }

    public double getFrequency()
    {
        return frequency;
    }

    public void setQ(double q)
    {
        this.q = q;
    }

    public double getQ()
    {
        return q;
    }

    public boolean isLinearPhase()
    {
        return linearPhase;
    }

    public void setLinearPhase(boolean linearPhase)
    {
        this.linearPhase = linearPhase;
    }

    @Override
    public Complex response(double f)
    {
        double fn = f / frequency;
        double r = 1 - fn * fn;
        Complex num = new Complex(r, -fn / q);
        Complex den = new Complex(r, fn / q);
        Complex h = num.divide(den);

        if (isLinearPhase())
        {
            return new Complex(h.abs());
        }

        return h;
    }

    @Override
    public String toString()
    {
        return "All-pass filter (" + Fnc.twoDecimalFormat(getFrequency()) + "Hz, Q " + Fnc.twoDecimalFormat(getQ()) + ")";
    }

    @Override
    public JsonValue toJSON()
    {
        JsonObject json = Json.object();

        json.add("Frequency", Json.value(getFrequency()));
        json.add("Q", Json.value(getQ()));
        JSON.add(json, "LinearPhase", isLinearPhase(), false);

        return json;
    }
}