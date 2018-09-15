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
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;

public abstract class Item implements IItem
{
    protected List<IItem> children;
    
    public Item()
    {
        children = new ArrayList<IItem>();
    }
    
    public Item(JsonValue json)
    {
        JsonObject jsonObj = json.asObject();
        children = childrenFromJSON(jsonObj.get("Children"));
    }
    
    public static void childrenRefresh(List<IItem> children)
    {
        for (IItem item: children)
        {
            item.refresh();
        }
    }
    
    public static Complex childrenResponse(List<IItem> children, double f)
    {
        if (children.isEmpty())
        {
            return new Complex(1);
        }
        
        Complex sum = new Complex();
        
        for (IItem item: children)
        {
            sum = sum.add(item.response(f));
        }
        
        return sum;
    }
    
    public static Complex childrenResponse1W(List<IItem> children, double f)
    {
        if (children.isEmpty())
        {
            return new Complex(1);
        }
        
        Complex sum = new Complex();
        
        for (IItem item: children)
        {
            sum = sum.add(item.response1W(f));
        }
        
        return sum;
    }
    
    public static Complex childrenListeningWindowResponse(List<IItem> children, double f)
    {
        if (children.isEmpty())
        {
            return new Complex(1);
        }
        
        Complex sum = new Complex();
        
        for (IItem item: children)
        {
            sum = sum.add(item.listeningWindowResponse(f));
        }
        
        return sum;
    }
    
    public static Complex childrenPowerResponse(List<IItem> children, double f)
    {
        if (children.isEmpty())
        {
            return new Complex(1);
        }
        
        Complex sum = new Complex();
        
        for (IItem item: children)
        {
            sum = sum.add(item.powerResponse(f));
        }
        
        return sum;
    }
    
    public static Complex childrenImpedance(List<IItem> children, double f)
    {
        Complex sum = new Complex();
        
        if (children.isEmpty())
        {
            return sum;
        }
        
        for (IItem item: children)
        {
            sum = sum.add(Complex.divide(1, item.impedance(f)));
        }
        
        return Complex.divide(1, sum);
    }
    
    public static double childrenMaxPower(List<IItem> children, double f)
    {
        double sum = Double.POSITIVE_INFINITY;
        
        for (IItem item: children)
        {
            sum = Math.min(sum, item.maxPower(f));
        }
        
        return sum;
    }
    
    public static double childrenExcursion(List<IItem> children, double f, double power)
    {
        double sum = 0;
        
        for (IItem item: children)
        {
            sum = Math.max(sum, item.excursion(f, power));
        }
        
        return sum;
    }
    
    public static Complex childrenFilter(List<IItem> children, double f)
    {
        if (children.isEmpty())
        {
            return new Complex(1);
        }
        
        Complex sum = new Complex();
        
        for (IItem item: children)
        {
            sum = sum.add(item.filter(f));
        }
        
        return sum;
    }
    
    public static Complex childrenBaffle(List<IItem> children, double f)
    {
        if (children.isEmpty())
        {
            return new Complex(1);
        }
        
        Complex sum = new Complex();
        
        for (IItem item: children)
        {
            sum = sum.add(item.responseWithBaffle(f));
        }
        
        return sum;
    }
    
    public static Complex childrenRoom(List<IItem> children, double f)
    {
        if (children.isEmpty())
        {
            return new Complex(1);
        }
        
        Complex sum = new Complex();
        
        for (IItem item: children)
        {
            sum = sum.add(item.responseWithRoom(f));
        }
        
        return sum;
    }
    
    public static JsonValue itemToJSON(IItem item)
    {
        JsonObject json = Json.object();
        json.add("Type", item.getClass().getSimpleName());
        json.add("Value", item.toJSON());
        return json;
    }
    
    public static JsonValue childrenToJSON(List<IItem> children)
    {
        JsonArray array = Json.array().asArray();
        
        for (IItem item: children)
        {
            array.add(itemToJSON(item));
        }
        
        return array;
    }
    
    public static IItem constructItem(JsonValue json)
    {
        JsonObject jsonObj = json.asObject();
        String type = jsonObj.getString("Type", "");
        json = jsonObj.get("Value");
        
        try
        {
            Class<?> itemClass = Class.forName("SpeakerSim." + type);
            if (!IItem.class.isAssignableFrom(itemClass))
            {
                throw new HandledException("Unsupported item: " + type);
            }
            
            Constructor constructor = itemClass.getConstructor(JsonValue.class);
            return (IItem) constructor.newInstance(json);
        }
        catch (ClassNotFoundException e)
        {
            throw new HandledException("Unsupported item: " + type);
        }
        catch (ReflectiveOperationException e)
        {
            throw new HandledException("Cannot construct item: " + type, e);
        }
    }
    
    private List<IItem> childrenFromJSON(JsonValue json)
    {
        List<IItem> c = new ArrayList<IItem>();
        
        if (json != null)
        {
            JsonArray array = json.asArray();
            for (JsonValue entry: array)
            {
                c.add(constructItem(entry));
            }
        }

        return c;
    }
    
    public void fromJSON(JsonValue json)
    {
        JsonObject jsonObj = json.asObject();
        children = childrenFromJSON(jsonObj.get("Children"));
    }
    
    @Override
    public List<IItem> getChildren()
    {
        return children;
    }
    
    @Override
    public void refresh()
    {
        childrenRefresh(children);
    }
    
    @Override
    public Complex response(double f)
    {
        return childrenResponse(children, f);
    }
    
    @Override
    public Complex response1W(double f)
    {
        return childrenResponse1W(children, f);
    }
    
    @Override
    public Complex listeningWindowResponse(double f)
    {
        return childrenListeningWindowResponse(children, f);
    }
    
    @Override
    public Complex powerResponse(double f)
    {
        return childrenPowerResponse(children, f);
    }
    
    @Override
    public Complex impedance(double f)
    {
        return childrenImpedance(children, f);
    }
    
    @Override
    public double maxPower(double f)
    {
        return childrenMaxPower(children, f);
    }
    
    @Override
    public double excursion(double f, double power)
    {
        return childrenExcursion(children, f, power);
    }
    
    @Override
    public Complex filter(double f)
    {
        return childrenFilter(children, f);
    }
    
    @Override
    public Complex responseWithBaffle(double f)
    {
        return childrenBaffle(children, f);
    }
    
    @Override
    public Complex responseWithRoom(double f)
    {
        return childrenRoom(children, f);
    }

    @Override
    public JsonValue toJSON()
    {
        return childrenToJSON(children);
    }
}
