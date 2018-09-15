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

public class Baffle implements JSONable
{
    public double Width;
    public double Height;
    public double X;
    public double Y;
    public double EdgeRadius;
    
    public Baffle()
    {
        Width = 0.3;
        Height = 0.7;
        X = 0.15;
        Y = 0.45;
    }
    
    public Baffle(double width, double height, double x, double y, double edgeRadius)
    {
        Width = width;
        Height = height;
        X = x;
        Y = y;
        EdgeRadius = edgeRadius;
    }
    
    public Baffle(JsonValue json)
    {
        JsonObject jsonObj = json.asObject();

        Width = JSON.getDouble(jsonObj, "Width");
        Height = JSON.getDouble(jsonObj, "Height");
        X = JSON.getDouble(jsonObj, "X");
        Y = JSON.getDouble(jsonObj, "Y");
        EdgeRadius = JSON.getDouble(jsonObj, "EdgeRadius");
    }
    
    @Override
    public JsonValue toJSON()
    {
        JsonObject json = Json.object();
        
        json.add("Width", Width);
        json.add("Height", Height);
        json.add("X", X);
        json.add("Y", Y);
        json.add("EdgeRadius", EdgeRadius);
        
        return json;
    }
}
