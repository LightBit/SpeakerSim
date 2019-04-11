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
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

public class Settings implements JSONable
{
    public int StartFrequency;
    public int EndFrequency;
    public int Points;
    public int Smoothing;
    public int dBRange;
    public int MaxImpedance;
    public int MaxPower;
    public boolean RoomSimulation;
    public boolean BaffleSimulation;
    public PowerFilter PowerFilter;
    
    public double pointsPerOctave()
    {
        return (Points - 1) / (Math.log((double)EndFrequency / (double)StartFrequency) / Math.log(2));
    }
    
    public double multiplier()
    {
        return Math.pow(2, 1 / pointsPerOctave());
    }
    
    public Settings()
    {
        StartFrequency = 20;
        EndFrequency = 20000;
        Points = 1000;
        Smoothing = 12;
        dBRange = 30;
        MaxImpedance = 80;
        MaxPower = 1000;
        RoomSimulation = false;
        BaffleSimulation = true;
        PowerFilter = new PowerFilter();
    }
    
    public Settings(JsonValue json)
    {
        JsonObject jsonObj = json.asObject();

        StartFrequency = JSON.getInt(jsonObj, "StartFrequency", 20);
        EndFrequency = JSON.getInt(jsonObj, "EndFrequency", 20000);
        Points = JSON.getInt(jsonObj, "Points", 1000);
        Smoothing = JSON.getInt(jsonObj, "Smoothing");
        dBRange = JSON.getInt(jsonObj, "dBRange", 30);
        MaxImpedance = JSON.getInt(jsonObj, "MaxImpedance", 80);
        MaxPower = JSON.getInt(jsonObj, "MaxPower", 1000);
        RoomSimulation = JSON.getBoolean(jsonObj, "RoomSimulationEnabled", false);
        BaffleSimulation = JSON.getBoolean(jsonObj, "BaffleSimulationEnabled", true);
        PowerFilter = new PowerFilter(jsonObj.get("PowerFilter"));
    }

    @Override
    public JsonValue toJSON()
    {
        JsonObject json = Json.object();
        
        json.add("StartFrequency", StartFrequency);
        json.add("EndFrequency", EndFrequency);
        json.add("Points", Points);
        json.add("Smoothing", Smoothing);
        json.add("dBRange", dBRange);
        json.add("MaxImpedance", MaxImpedance);
        json.add("MaxPower", MaxPower);
        json.add("RoomSimulationEnabled", RoomSimulation);
        json.add("BaffleSimulationEnabled", BaffleSimulation);
        json.add("PowerFilter", PowerFilter.toJSON());
        
        return json;
    }
}