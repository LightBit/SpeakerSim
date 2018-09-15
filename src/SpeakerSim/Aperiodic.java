/*
 * Copyright (C) 2018 Gregor Pintar <grpintar@gmail.com>
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

public final class Aperiodic implements JSONable
{
    public static final double[] K = {0.732, 0.850, 0.614};
    public static final String[] ENDS =
    {
        "one flanged, one free",
        "both ends flanged",
        "both ends free"
    };
    
    public static final String[] SHAPES =
    {
        "round",
        "rectangular"
    };
    
    public double Vb;
    public double Ql;
    public double Qa;
    public int VentShape;
    public double Dv;
    public double Wv;
    public double Hv;
    public int Np;
    public int Ends;
    public double Thickness;
    public Position VentPosition;
    
    public Aperiodic(JsonValue json)
    {
        if (json != null)
        {
            JsonObject jsonObj = json.asObject();

            Vb = jsonObj.get("Vb").asDouble();
            Ql = jsonObj.get("Ql").asDouble();
            Qa = jsonObj.get("Qa").asDouble();
            setVentShape(jsonObj.get("VentShape").asString());
            Dv = jsonObj.get("Dv").asDouble();
            Wv = jsonObj.get("Wv").asDouble();
            Hv = jsonObj.get("Hv").asDouble();
            Np = jsonObj.get("Np").asInt();
            Thickness = JSON.getDouble(jsonObj, "Thickness");
            setEnds(jsonObj.get("Ends").asString());
            VentPosition = new Position(jsonObj.get("VentPosition"));
        }
        else
        {
            Vb = 0.05;
            Ql = 10;
            Qa = 10;
            Np = 1;
            Dv = 0.1;
            Thickness = 0.002;
            VentPosition = new Position();
            VentPosition.Z = 0.5;
        }
    }
    
    public Aperiodic()
    {
        this(null);
    }
    
    public void setVentShape(String shape)
    {
        for (int i = 0; i < SHAPES.length; i++)
        {
            if (SHAPES[i].equals(shape))
            {
                VentShape = i;
                break;
            }
        }
    }
    
    public void setEnds(String ends)
    {
        for (int i = 0; i < ENDS.length; i++)
        {
            if (ENDS[i].equals(ends))
            {
                Ends = i;
                break;
            }
        }
    }
    
    @Override
    public JsonValue toJSON()
    {
        JsonObject json = Json.object();
        
        json.add("Vb", Json.value(Vb));
        json.add("Ql", Json.value(Ql));
        json.add("Qa", Json.value(Qa));
        json.add("VentShape", Json.value(SHAPES[VentShape]));
        json.add("Dv", Json.value(Dv));
        json.add("Wv", Json.value(Wv));
        json.add("Hv", Json.value(Hv));
        json.add("Np", Json.value(Np));
        json.add("Thickness", Json.value(Thickness));
        json.add("Ends", Json.value(ENDS[Ends]));
        json.add("VEntPosition", VentPosition.toJSON());
        
        return json;
    }
}
