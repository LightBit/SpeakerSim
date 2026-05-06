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

public abstract class ActiveShelfFilter implements IActiveFilter
{
    private double frequency;
    private double q;
    private double decibels;
    private boolean linearPhase;

    public ActiveShelfFilter()
    {
        frequency = 1000;
        q = 0.707;
        decibels = 0;
    }

    public ActiveShelfFilter(JsonValue json)
    {
        JsonObject jsonObj = json.asObject();

        frequency = JSON.getDouble(jsonObj, "Frequency", 1000);
        q = JSON.getDouble(jsonObj, "Q", 0.707);
        decibels = JSON.getDouble(jsonObj, "dB", 0);
        linearPhase = JSON.getBoolean(jsonObj, "LinearPhase", false);
    }

    public final void setFrequency(double frequency)
    {
        this.frequency = frequency;
    }

    public final double getFrequency()
    {
        return frequency;
    }

    public final void setQ(double q)
    {
        this.q = q;
    }

    public final double getQ()
    {
        return q;
    }

    public final void setDecibels(double decibels)
    {
        this.decibels = decibels;
    }

    public final double getDecibels()
    {
        return decibels;
    }

    public final boolean isLinearPhase()
    {
        return linearPhase;
    }

    public final void setLinearPhase(boolean linearPhase)
    {
        this.linearPhase = linearPhase;
    }

    protected abstract Complex filter(double normalizedFrequency, double amplitude, double slope);

    protected abstract String name();

    @Override
    public final Complex response(double f)
    {
        double normalizedFrequency = f / frequency;
        double amplitude = Fnc.toAmplitude(decibels);
        double slope = Math.sqrt(amplitude) / q;
        Complex h = filter(normalizedFrequency, amplitude, slope);

        if (isLinearPhase())
        {
            return new Complex(h.abs());
        }

        return h;
    }

    @Override
    public String toString()
    {
        return name() + " (" + Fnc.twoDecimalFormat(getDecibels())
                + "dB at " + Fnc.twoDecimalFormat(getFrequency())
                + "Hz, Q " + Fnc.twoDecimalFormat(getQ()) + ")";
    }

    @Override
    public JsonValue toJSON()
    {
        JsonObject json = Json.object();

        json.add("Frequency", Json.value(getFrequency()));
        json.add("Q", Json.value(getQ()));
        json.add("dB", Json.value(getDecibels()));
        JSON.add(json, "LinearPhase", isLinearPhase(), false);

        return json;
    }
}