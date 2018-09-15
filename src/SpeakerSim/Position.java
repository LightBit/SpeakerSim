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

public class Position implements JSONable
{
    public double X; // meters
    public double Y; // meters
    public double Z; // meters
    public double HorizontalAngle; // -180 > 180
    public double VerticalAngle; // -180 > 180
    
    public Position(JsonValue json)
    {
        if (json != null)
        {
            JsonObject jsonObj = json.asObject();

            X = JSON.getDouble(jsonObj, "X", 0.6);
            Y = JSON.getDouble(jsonObj, "Y", 1);
            Z = JSON.getDouble(jsonObj, "Z", 1.1);
            HorizontalAngle = JSON.getDouble(jsonObj, "HorizontalAngle", 0);
            VerticalAngle = JSON.getDouble(jsonObj, "VerticalAngle", 0);
        }
        else
        {
            X = 0.6;
            Y = 1;
            Z = 1.1;
        }
    }
    
    public Position(double x, double y, double z)
    {
        X = x;
        Y = y;
        Z = z;
    }
    
    public Position(double x, double y, double z, double horizontalAngle, double verticalAngle)
    {
        X = x;
        Y = y;
        Z = z;
        HorizontalAngle = horizontalAngle;
        VerticalAngle = verticalAngle;
    }
    
    public Position()
    {
        this(null);
    }
    
    @Override
    public JsonValue toJSON()
    {
        JsonObject json = Json.object();
        
        json.add("X", Json.value(X));
        json.add("Y", Json.value(Y));
        json.add("Z", Json.value(Z));
        JSON.add(json, "HorizontalAngle", HorizontalAngle);
        JSON.add(json, "VerticalAngle", VerticalAngle);
        
        return json;
    }
    
    public static double distance(Position a, Position b)
    {
        double x = a.X - b.X;
        double y = a.Y - b.Y;
        double z = a.Z - b.Z;

        return Math.sqrt(x * x + y * y + z * z);
    }
    
    public double distance(Position x)
    {
        return distance(this, x);
    }
    
    private static double angle(double a1, double b1, double a2, double b2)
    {
        return Math.toDegrees(Math.atan2(b2 - b1, a2 - a1));
    }
    
    public static double horizontalAngle(Position a, Position b)
    {
        return angle(a.Y, a.X, b.Y, b.X) - (a.HorizontalAngle - b.HorizontalAngle);
    }
    
    public double horizontalAngle(Position x)
    {
        return horizontalAngle(this, x);
    }
    
    public static double verticalAngle(Position a, Position b)
    {
        return angle(a.Y, a.Z, b.Y, b.Z) - (a.VerticalAngle - b.VerticalAngle);
    }
    
    public double verticalAngle(Position x)
    {
        return verticalAngle(this, x);
    }
    
    public Position add(double distance, double horizontalAngle, double verticalAngle)
    {
        horizontalAngle = Math.toRadians(HorizontalAngle + horizontalAngle);
        verticalAngle = Math.toRadians(VerticalAngle + verticalAngle);
        
        return new Position(X + Math.sin(horizontalAngle) * distance, Y + Math.cos(horizontalAngle) * Math.cos(verticalAngle) * distance, Z + Math.sin(verticalAngle) * distance);
    }
    
    public Position add(Position p)
    {
        return new Position(X + p.X, Y + p.Y, Z + p.Z, HorizontalAngle + p.HorizontalAngle, VerticalAngle + p.VerticalAngle);
    }
    
    public Position divide(double x)
    {
        return new Position(X / x, Y / x, Z / x, HorizontalAngle / x, VerticalAngle / x);
    }
    
    public static Position average(Position... positions)
    {
        Position center = new Position(0, 0, 0);
        
        for (Position p : positions)
        {
            center.X += p.X;
            center.Y += p.Y;
            center.Z += p.Z;
            center.HorizontalAngle += p.HorizontalAngle;
            center.VerticalAngle += p.VerticalAngle;
        }
        
        center.X /= positions.length;
        center.Y /= positions.length;
        center.Z /= positions.length;
        center.HorizontalAngle /= positions.length;
        center.VerticalAngle /= positions.length;
        
        return center;
    }
}
