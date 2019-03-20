/*
 * Copyright (C) 2018 Gregor Pintar
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package SpeakerSim;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ResponseData implements JSONable, Comparable<ResponseData>
{
    public boolean isMinimumPhase;
    public boolean inputIsVoltage;
    public double input;
    public double distance;
    public double horizontalAngle;
    public double verticalAngle;
    public ResponseEntry[] data;
    private final DistanceSimulation dist;
    
    public Complex response(double f, double SPLdiff)
    {
        // no lower value
        if (data[0].frequency > f)
        {
            return Fnc.toComplex(Double.MIN_NORMAL, 0);
        }

        for (int i = 1; i < data.length; i++)
        {
            if (data[i].frequency >= f)
            {
                double amplitude = Fnc.interpolate(data[i - 1].frequency, data[i - 1].amplitude, data[i].frequency, data[i].amplitude, f);
                double phase = Fnc.interpolate(data[i - 1].frequency, data[i - 1].phase, data[i].frequency, data[i].phase, f);

                if (inputIsVoltage)
                {
                    amplitude -= Fnc.toDecibels(input);
                }
                else
                {
                    amplitude += SPLdiff - Fnc.powerToDecibels(input);
                }
                
                Complex x = dist.response(f);
                if (isMinimumPhase)
                {
                    x = Fnc.toComplex(x.abs(), 0);
                }

                return Fnc.toComplex(Fnc.toAmplitude(amplitude), Math.toRadians(phase)).divide(x);
            }
        }

        // no higher value
        return Fnc.toComplex(Double.MIN_NORMAL, 0);
    }
    
    public static ResponseEntry[] ImportData(File file, boolean dB) throws IOException
    {
        List<ResponseEntry> frd = new ArrayList<ResponseEntry>();
        boolean hasPhase = false;
        
        try (BufferedReader r = new BufferedReader(new FileReader(file)))
        {
            String line;
            
            while ((line = r.readLine()) != null)
            {
                line = line.trim();
                
                if (!line.isEmpty() && Character.isDigit(line.charAt(0)))
                {
                    ResponseEntry entry = new ResponseEntry(line);
                    frd.add(entry);
                    
                    if (entry.phase != 0)
                    {
                        hasPhase = true;
                    }
                }
            }
            
            r.close();
        }
        
        if (frd.isEmpty())
        {
            return null;
        }
        
        // sort by frequency
        Collections.sort(frd);
        
        // average value of duplicate frequencies
        for (int i = 0; i < frd.size(); i++)
        {
            ResponseEntry entry = frd.get(i);
            int next = i + 1;
            
            for (int dups = 1; next < frd.size(); dups++)
            {
                ResponseEntry dup = frd.get(next);
                
                if (entry.compareTo(dup) == 0)
                {
                    entry.amplitude += dup.amplitude;
                    entry.phase += dup.phase;
                    frd.remove(next);
                }
                else
                {
                    if (dups > 1)
                    {
                        entry.amplitude /= dups;
                        entry.phase /= dups;
                        frd.set(i, entry);
                    }
                    break;
                }
            }
        }
        
        // no phase -> calculate phase from amplitude
        if (!hasPhase)
        {
            double[] frequency = new double[frd.size()];
            double[] amplitude = new double[frequency.length];
            
            int i = 0;
            for (ResponseEntry entry : frd)
            {
                frequency[i] = entry.frequency;
                amplitude[i] = entry.amplitude;
                i++;
            }
            
            double slopeLo = Fnc.calcSlopeLo(frequency, amplitude);
            double slopeHi = Fnc.calcSlopeHi(frequency, amplitude);
            
            if (dB)
            {
                for (i = 0; i < amplitude.length; i++)
                {
                    amplitude[i] = Fnc.toAmplitude(amplitude[i]);
                }
            }
            
            double[] phase = Fnc.calcPhase(frequency, amplitude, slopeLo, slopeHi);
            
            ResponseEntry[] array = new ResponseEntry[frequency.length];
            i = 0;
            for (ResponseEntry entry : frd)
            {
                entry.phase = Math.toDegrees(phase[i]);
                array[i] = entry;
                i++;
            }
            
            return array;
        }
        
        ResponseEntry[] array = new ResponseEntry[frd.size()];
        for (int i = 0; i < array.length; i++)
        {
            array[i] = frd.get(i);
        }
        
        return array;
    }
    
    public static void ExportData(ResponseEntry[] frd, File file) throws FileNotFoundException, UnsupportedEncodingException
    {
        try (PrintWriter writer = new PrintWriter(file, "UTF-8"))
        {
            for (ResponseEntry entry : frd)
            {
                writer.print(entry.toString());
            }

            writer.close();
        }
    }
    
    public void importData(File file, boolean dB) throws IOException
    {
        data = ImportData(file, dB);
    }
    
    public void exportData(File file) throws FileNotFoundException, UnsupportedEncodingException
    {
        ExportData(data, file);
    }
    
    public ResponseData(JsonValue json)
    {
        if (json != null)
        {
            JsonObject jsonObj = json.asObject();
            
            distance = JSON.getDouble(jsonObj, "Distance", 1);
            isMinimumPhase = JSON.getBoolean(jsonObj, "IsMinimumPhase", true);
            inputIsVoltage = JSON.getBoolean(jsonObj, "InputIsVoltage", false);
            input = JSON.getDouble(jsonObj, "Input", 1);
            horizontalAngle = JSON.getDouble(jsonObj, "HorizontalAngle");
            verticalAngle = JSON.getDouble(jsonObj, "VerticalAngle");
            
            JsonArray array = jsonObj.get("Data").asArray();
            data = new ResponseEntry[array.size()];
            
            int i = 0;
            for (JsonValue entry : array)
            {
                data[i++] = new ResponseEntry(entry.asArray().get(0).asDouble(), entry.asArray().get(1).asDouble(), entry.asArray().get(2).asDouble());
            }
        }
        else
        {
            isMinimumPhase = true;
            inputIsVoltage = false;
            input = 1;
            distance = 1;
        }
        
        dist = new DistanceSimulation(distance);
    }
    
    @Override
    public JsonValue toJSON()
    {
        JsonObject json = Json.object();
        JsonArray array = Json.array().asArray();
        
        for (ResponseEntry entry : data)
        {
            array.add(Json.array(entry.frequency, entry.amplitude, entry.phase));
        }
        
        json.add("Distance", distance);
        json.add("IsMinimumPhase", isMinimumPhase);
        json.add("InputIsVoltage", inputIsVoltage);
        json.add("InputValue", input);
        json.add("HorizontalAngle", horizontalAngle);
        json.add("VerticalAngle", verticalAngle);
        json.add("Data", array);
        
        return json;
    }
    
    @Override
    public String toString()
    {
        if (horizontalAngle != 0)
        {
            return "Horizontal " + Fnc.decimalFormat(horizontalAngle) + "°";
        }
        
        if (verticalAngle != 0)
        {
            return "Vertical " + Fnc.decimalFormat(verticalAngle) + "°";
        }
        
        return "On-axis";
    }
    
    @Override
    public int compareTo(ResponseData o)
    {
        /*if (horizontalAngle == 0 && verticalAngle == 0)
        {
            return -1;
        }
        
        if (o.horizontalAngle == 0 && o.verticalAngle == 0)
        {
            return 1;
        }*/
        
        if (horizontalAngle > o.horizontalAngle)
        {
            return 1;
        }
        
        if (horizontalAngle < o.horizontalAngle)
        {
            return -1;
        }
        
        if (verticalAngle > o.verticalAngle)
        {
            return 1;
        }
        
        if (verticalAngle < o.verticalAngle)
        {
            return -1;
        }
        
        return 0;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        
        ResponseData obj = (ResponseData) o;
        return horizontalAngle == obj.horizontalAngle && verticalAngle == obj.verticalAngle;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 31 * hash + (int) (Double.doubleToLongBits(horizontalAngle) ^ (Double.doubleToLongBits(horizontalAngle) >>> 32));
        hash = 31 * hash + (int) (Double.doubleToLongBits(verticalAngle) ^ (Double.doubleToLongBits(verticalAngle) >>> 32));
        return hash;
    }
}
