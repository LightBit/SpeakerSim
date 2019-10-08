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

public class ClosedBox implements JSONable
{
    public double Vb;
    public double Ql;
    public double Qa;
    
    public ClosedBox(JsonValue json)
    {
        if (json != null)
        {
            JsonObject jsonObj = json.asObject();

            Vb = JSON.getDouble(jsonObj, "Vb", 0.05);
            Ql = JSON.getDouble(jsonObj, "Ql", 7);
            Qa = JSON.getDouble(jsonObj, "Qa", 8);
        }
        else
        {
            Vb = 0.05;
            Ql = 7;
            Qa = 8;
        }
    }
    
    public ClosedBox()
    {
        this(null);
    }
    
    @Override
    public JsonValue toJSON()
    {
        JsonObject json = Json.object();
        
        json.add("Vb", Vb);
        json.add("Ql", Ql);
        json.add("Qa", Qa);
        
        return json;
    }
}
