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

            if (Project.getInstance().Version.compareTo(Project.parseVersion("2019-08-16")) < 0)
            {
                X = JSON.getDouble(jsonObj, "Y");
                Y = JSON.getDouble(jsonObj, "X");
            }
            else
            {
                X = JSON.getDouble(jsonObj, "X");
                Y = JSON.getDouble(jsonObj, "Y");
            }
            Z = JSON.getDouble(jsonObj, "Z");
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
    
    public double distance(Position p)
    {
        double x = p.X - X;
        double y = p.Y - Y;
        double z = p.Z - Z;

        return Math.sqrt(x * x + y * y + z * z);
    }
    
    public double horizontalAngle(Position p)
    {
        double x = p.X - X;
        double y = p.Y - Y;
        double z = p.Z - Z;
        
        double alpha = Math.toRadians(VerticalAngle);
        double beta = Math.toRadians(-HorizontalAngle);
        
        double a = x * Math.cos(alpha) * Math.cos(beta)
                + y * -Math.sin(beta)
                + z * Math.cos(beta) * Math.sin(alpha);
        
        double b = x * Math.cos(alpha) * Math.sin(beta)
                + y * Math.cos(beta)
                + z * Math.sin(alpha) * Math.sin(beta);
        
        return Math.toDegrees(Math.atan2(b, a));
    }
    
    public double verticalAngle(Position p)
    {
        double x = p.X - X;
        double y = p.Y - Y;
        double z = p.Z - Z;
        
        double alpha = Math.toRadians(HorizontalAngle);
        double beta = Math.toRadians(-VerticalAngle);
        
        double a = x * Math.cos(alpha) * Math.cos(beta)
                + y * Math.cos(beta) * Math.sin(alpha)
                + z * -Math.sin(beta);
        
        double c = x * Math.cos(alpha) * Math.sin(beta)
                + y * Math.sin(alpha) * Math.sin(beta)
                + z * Math.cos(beta);
        
        return Math.toDegrees(Math.atan2(c, a));
    }
    
    public Position moveHorizontally(double distance, double angle)
    {
        angle = Math.toRadians(angle);
        
        return new Position
        (
            X + distance * Math.cos(angle),
            Y + distance * Math.sin(angle),
            Z,
            HorizontalAngle,
            VerticalAngle
        );
    }
    
    public Position moveVertically(double distance, double angle)
    {
        angle = Math.toRadians(angle);
        
        return new Position
        (
            X + distance * Math.cos(angle),
            Y,
            Z + distance * Math.sin(angle),
            HorizontalAngle,
            VerticalAngle
        );
    }
    
    public Position add(Position p)
    {
        return new Position
        (
            X + p.X,
            Y + p.Y,
            Z + p.Z,
            HorizontalAngle + p.HorizontalAngle,
            VerticalAngle + p.VerticalAngle
        );
    }
    
    public Position divide(double x)
    {
        return new Position(
                X / x,
                Y / x,
                Z / x,
                HorizontalAngle / x,
                VerticalAngle / x
        );
    }
}
