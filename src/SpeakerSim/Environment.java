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

public class Environment implements JSONable
{
    public double Temperature;
    public double Humidity;
    public double Altitude;
    public double AirPressure;
    public double AirDensity;
    public double SpeedOfSound;
    
    public double RoomX;
    public double RoomY;
    public double RoomZ;
    
    public static Environment getInstance()
    {
        return Project.getInstance().Environment;
    }
    
    public static double calcAirPressure(double Temperature, double Altitude)
    {
        return 101325 * Math.exp(-((0.284043733 * Altitude) / (8.31447 * (Temperature + 273.15))));
    }
    
    public static double calcAirDensity(double Temperature, double Humidity, double AirPressure)
    {
        double Pv = 610.78 * Math.pow(10, (7.5 * Temperature) / (Temperature + 237.3)) * Humidity;
        double Pd = AirPressure - Pv;
        
        return (Pd * 0.028964 + Pv * 0.018016) / (8.314 * (Temperature + 273.15));
    }
    
    public static double calcSpeedOfSound(double AirPressure, double AirDensity)
    {
        return Math.sqrt(1.4 * (AirPressure / AirDensity));
    }
    
    public Environment()
    {
        SpeedOfSound = 344;
        AirDensity = 1.2;
        AirPressure = 101325;
        Altitude = 0;
        Humidity = 0.50;
        Temperature = 20;
        RoomX = 7;
        RoomY = 5;
        RoomZ = 2.5;
    }
    
    public Environment(JsonValue json)
    {
        JsonObject jsonObj = json.asObject();

        SpeedOfSound = JSON.getDouble(jsonObj, "SpeedOfSound", 344);
        AirDensity = JSON.getDouble(jsonObj, "AirDensity", 1.2);
        AirPressure = JSON.getDouble(jsonObj, "AirPressure", 101325);
        Altitude = JSON.getDouble(jsonObj, "Altitude", 0);
        Humidity = JSON.getDouble(jsonObj, "Humidity", 0.5);
        Temperature = JSON.getDouble(jsonObj, "Temperature", 20);
        
        if (Project.getInstance().Version.compareTo(Project.parseVersion("2019-08-16")) < 0)
        {
            RoomX = JSON.getDouble(jsonObj, "RoomY", 5);
            RoomY = JSON.getDouble(jsonObj, "RoomX", 7);
        }
        else
        {
            RoomX = JSON.getDouble(jsonObj, "RoomX", 7);
            RoomY = JSON.getDouble(jsonObj, "RoomY", 5);
        }
        RoomZ = JSON.getDouble(jsonObj, "RoomZ", 2.5);
    }

    @Override
    public JsonValue toJSON()
    {
        JsonObject json = Json.object();
        
        json.add("SpeedOfSound", SpeedOfSound);
        json.add("AirDensity", AirDensity);
        json.add("AirPressure", AirPressure);
        json.add("Altitude", Altitude);
        json.add("Humidity", Humidity);
        json.add("Temperature", Temperature);
        json.add("RoomX", RoomX);
        json.add("RoomY", RoomY);
        json.add("RoomZ", RoomZ);
        
        return json;
    }
}
