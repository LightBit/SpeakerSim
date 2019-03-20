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
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import java.lang.reflect.Constructor;

public class Amplifier extends Filter
{
    public double Pe;
    public double Zo;
    public IActiveFilter[] Filters;
    
    private static IActiveFilter constructActiveFilter(JsonValue json)
    {
        JsonObject jsonObj = json.asObject();
        String type = jsonObj.getString("Type", null);
        json = jsonObj.get("Value");
        
        try
        {
            Class<?> filterClass = Class.forName("SpeakerSim." + type);
            Constructor constructor = filterClass.getConstructor(JsonValue.class);
            return (IActiveFilter) constructor.newInstance(json);
        }
        catch (Exception e)
        {
            throw new HandledException("Cannot construct active filter", e);
        }
    }
    
    private static JsonValue activeFilterToJSON(IActiveFilter filter)
    {
        JsonObject json = Json.object();
        json.add("Type", filter.getClass().getSimpleName());
        json.add("Value", filter.toJSON());
        return json;
    }
    
    public Amplifier()
    {
        super();
        Pe = 1000;
        Zo = 0.1;
        Filters = new IActiveFilter[0];
    }
    
    public Amplifier(JsonValue json)
    {
        JsonObject jsonObj = json.asObject();

        Pe = jsonObj.getDouble("Pe", 1000);
        Zo = jsonObj.getDouble("Zo", 0.1);
        
        JsonValue af = jsonObj.get("Filters");
        if (af != null)
        {
            JsonArray array = af.asArray();
            Filters = new IActiveFilter[array.size()];
            for (int i = 0; i < Filters.length; i++)
            {
                Filters[i] = constructActiveFilter(array.get(i));
            }
        }
        else
        {
            Filters = new IActiveFilter[0];
        }
        
        super.fromJSON(json);
    }
    
    @Override
    public JsonValue toJSON()
    {
        JsonObject json = Json.object();
        
        json.add("Pe", Pe);
        json.add("Zo", Zo);
        
        JsonArray array = Json.array().asArray();
        
        for (IActiveFilter af: Filters)
        {
            array.add(activeFilterToJSON(af));
        }
        json.add("Filters", array);
        
        json.add("Children", Item.childrenToJSON(children));
        
        return json;
    }
    
    private Complex filterZo(double f)
    {
        Complex z = super.impedance(f);
        return z.divide(z.add(Zo));
    }
    
    private Complex activeFilters(double f)
    {
        Complex filters = new Complex(1);
        
        for (IActiveFilter af: Filters)
        {
            filters = filters.multiply(af.response(f));
        }
        
        return filters;
    }

    @Override
    public Complex thisFilter(double f)
    {
        return filterZo(f).multiply(activeFilters(f));
    }

    @Override
    public Complex impedance(double f)
    {
        return super.impedance(f).add(Zo);
    }
    
    @Override
    public double maxPower(double f)
    {
        return Math.min(super.maxPower(f) * Math.pow(activeFilters(f).abs(), 2), Pe);
    }
    
    @Override
    public double excursion(double f, double Pe)
    {
        return super.excursion(f, this.Pe) * filter(f).abs();
    }
    
    @Override
    public String toString()
    {
        return "Amplifier (" + Fnc.decimalFormat(Pe) + "W)";
    }
}
