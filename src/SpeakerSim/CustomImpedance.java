/*
 * Copyright (C) 2020 Gregor Pintar <grpintar@gmail.com>
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

import com.eclipsesource.json.*;
import java.io.*;

public class CustomImpedance extends Filter
{
    public ResponseEntry[] ZMA;
    
    private Complex z(double f)
    {
        ResponseEntry prev = ZMA[0];

        // no lower value
        if (prev.frequency > f)
        {
            return Complex.toComplex(prev.amplitude, Math.toRadians(prev.phase));
        }

        for (ResponseEntry entry : ZMA)
        {
            if (entry.frequency >= f)
            {
                double amplitude = Fnc.interpolate(prev.frequency, prev.amplitude, entry.frequency, entry.amplitude, f);
                double phase = Fnc.interpolate(prev.frequency, prev.phase, entry.frequency, entry.phase, f);
                return Complex.toComplex(amplitude, Math.toRadians(phase));
            }

            prev = entry;
        }

        // no higher value
        return Complex.toComplex(prev.amplitude, Math.toRadians(prev.phase));
    }
    
    public void importZMA(File file) throws IOException
    {
        ZMA = ResponseData.ImportData(file, false);
    }
    
    public void exportZMA(File file) throws FileNotFoundException, UnsupportedEncodingException
    {
        ResponseData.ExportData(ZMA, file);
    }
    
    public CustomImpedance()
    {
        super();
    }
    
    public CustomImpedance(JsonValue json)
    {
        JsonValue zma = json.asObject().get("ZMA");
        JsonArray array = zma.asArray();
        ZMA = new ResponseEntry[array.size()];

        int i = 0;
        for (JsonValue entry : array)
        {
            ZMA[i++] = new ResponseEntry(entry.asArray().get(0).asDouble(), entry.asArray().get(1).asDouble(), entry.asArray().get(2).asDouble());
        }
        
        super.fromJSON(json);
    }
    
    @Override
    protected JsonObject itemToJSON()
    {
        JsonObject json = super.itemToJSON();
        JsonArray array = Json.array().asArray();
        
        for (ResponseEntry entry : ZMA)
        {
            array.add(Json.array(entry.frequency, entry.amplitude, entry.phase));
        }

        json.add("ZMA", array);
        
        return json;
    }

    @Override
    public Complex thisFilter(double f)
    {
        Complex z = super.itemImpedance(f);
        return z.divide(z.add(z(f)));
    }

    @Override
    protected Complex itemImpedance(double f)
    {
        return super.itemImpedance(f).add(z(f));
    }
    
    @Override
    public String toString()
    {
        return "Custom impedance";
    }
}
