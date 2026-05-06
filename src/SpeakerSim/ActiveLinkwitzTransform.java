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

public final class ActiveLinkwitzTransform implements IActiveFilter
{
    private double sourceFrequency;
    private double sourceQ;
    private double targetFrequency;
    private double targetQ;
    private boolean linearPhase;

    public ActiveLinkwitzTransform()
    {
        sourceFrequency = 50;
        sourceQ = 0.9;
        targetFrequency = 30;
        targetQ = 0.707;
    }

    public ActiveLinkwitzTransform(JsonValue json)
    {
        JsonObject jsonObj = json.asObject();

        sourceFrequency = JSON.getDouble(jsonObj, "SourceFrequency", 50);
        sourceQ = JSON.getDouble(jsonObj, "SourceQ", 0.9);
        targetFrequency = JSON.getDouble(jsonObj, "TargetFrequency", 30);
        targetQ = JSON.getDouble(jsonObj, "TargetQ", 0.707);
        linearPhase = JSON.getBoolean(jsonObj, "LinearPhase", false);
    }

    public void setSourceFrequency(double sourceFrequency)
    {
        this.sourceFrequency = sourceFrequency;
    }

    public double getSourceFrequency()
    {
        return sourceFrequency;
    }

    public void setSourceQ(double sourceQ)
    {
        this.sourceQ = sourceQ;
    }

    public double getSourceQ()
    {
        return sourceQ;
    }

    public void setTargetFrequency(double targetFrequency)
    {
        this.targetFrequency = targetFrequency;
    }

    public double getTargetFrequency()
    {
        return targetFrequency;
    }

    public void setTargetQ(double targetQ)
    {
        this.targetQ = targetQ;
    }

    public double getTargetQ()
    {
        return targetQ;
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
        double sourceFn = f / sourceFrequency;
        double sourceR = 1 - sourceFn * sourceFn;
        Complex source = new Complex(sourceR, sourceFn / sourceQ);

        double targetFn = f / targetFrequency;
        double targetR = 1 - targetFn * targetFn;
        Complex target = new Complex(targetR, targetFn / targetQ);

        Complex h = source.divide(target);

        if (isLinearPhase())
        {
            return new Complex(h.abs());
        }

        return h;
    }

    @Override
    public String toString()
    {
        return "Linkwitz Transform ("
                + Fnc.twoDecimalFormat(sourceFrequency) + "Hz/Q " + Fnc.twoDecimalFormat(sourceQ)
                + " -> " + Fnc.twoDecimalFormat(targetFrequency) + "Hz/Q " + Fnc.twoDecimalFormat(targetQ)
                + ")";
    }

    @Override
    public JsonValue toJSON()
    {
        JsonObject json = Json.object();

        json.add("SourceFrequency", Json.value(sourceFrequency));
        json.add("SourceQ", Json.value(sourceQ));
        json.add("TargetFrequency", Json.value(targetFrequency));
        json.add("TargetQ", Json.value(targetQ));
        JSON.add(json, "LinearPhase", isLinearPhase(), false);

        return json;
    }
}