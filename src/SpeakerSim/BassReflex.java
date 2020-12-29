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

public final class BassReflex implements JSONable
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
    public double Fb;
    public double Ql;
    public double Qa;
    public double Qp;
    public int PortShape;
    public double Dv;
    public double Wv;
    public double Hv;
    public int Np;
    public int Ends;
    public double Thickness;
    public Position PortPosition;
    
    public BassReflex(JsonValue json)
    {
        if (json != null)
        {
            JsonObject jsonObj = json.asObject();

            Vb = jsonObj.get("Vb").asDouble();
            Fb = jsonObj.get("Fb").asDouble();
            Ql = jsonObj.get("Ql").asDouble();
            Qa = jsonObj.get("Qa").asDouble();
            Qp = jsonObj.get("Qp").asDouble();
            setPortShape(jsonObj.get("PortShape").asString());
            Dv = jsonObj.get("Dv").asDouble();
            Wv = jsonObj.get("Wv").asDouble();
            Hv = jsonObj.get("Hv").asDouble();
            Np = jsonObj.get("Np").asInt();
            Thickness = JSON.getDouble(jsonObj, "Thickness");
            setEnds(jsonObj.get("Ends").asString());
            PortPosition = new Position(jsonObj.get("PortPosition"));
        }
        else
        {
            Vb = 0.05;
            Fb = 30;
            Ql = 7;
            Qa = 50;
            Qp = 100;
            Np = 1;
            Dv = 0.08;
            Thickness = 0.002;
            PortPosition = new Position();
        }
    }
    
    public BassReflex()
    {
        this(null);
    }
    
    public void setPortShape(String shape)
    {
        for (int i = 0; i < SHAPES.length; i++)
        {
            if (SHAPES[i].equals(shape))
            {
                PortShape = i;
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
        json.add("Fb", Json.value(Fb));
        json.add("Ql", Json.value(Ql));
        json.add("Qa", Json.value(Qa));
        json.add("Qp", Json.value(Qp));
        json.add("PortShape", Json.value(SHAPES[PortShape]));
        json.add("Dv", Json.value(Dv));
        json.add("Wv", Json.value(Wv));
        json.add("Hv", Json.value(Hv));
        json.add("Np", Json.value(Np));
        json.add("Thickness", Json.value(Thickness));
        json.add("Ends", Json.value(ENDS[Ends]));
        json.add("PortPosition", PortPosition.toJSON());
        
        return json;
    }
}
