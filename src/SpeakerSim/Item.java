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

import com.eclipsesource.json.*;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;

public abstract class Item implements IItem
{
    public enum Status
    {
        CONNECTED,
        DISCONNECTED,
        BYPASSED
    }
    
    private List<IItem> children;
    private Status status;
    
    public Item()
    {
        children = new ArrayList<IItem>();
        status = Status.CONNECTED;
    }
    
    @Override
    final public void setStatus(Status status)
    {
        this.status = status;
    }
    
    @Override
    final public Status getStatus()
    {
        return status;
    }
    
    private void childrenRefresh()
    {
        for (IItem item: children)
        {
            item.refresh();
        }
    }
    
    private Complex childrenResponse(double f)
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
    
    private Complex childrenResponse1W(double f)
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
    
    private Complex childrenListeningWindowResponse(double f)
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
    
    private Complex childrenPowerResponse(double f)
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
    
    private Complex childrenImpedance(double f)
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
    
    private double childrenMaxPower(double f)
    {
        double sum = Double.MAX_VALUE;
        
        for (IItem item: children)
        {
            sum = Math.min(sum, item.maxPower(f));
        }
        
        return sum;
    }
    
    private double childrenExcursion(double f, double power)
    {
        double sum = 0;
        
        for (IItem item: children)
        {
            sum = Math.max(sum, item.excursion(f, power));
        }
        
        return sum;
    }
    
    private Complex childrenFilter(double f)
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
    
    private Complex childrenResponseWithBaffle(double f)
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
    
    private Complex childrenResponseWithRoom(double f)
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
    
    private JsonValue childrenToJSON()
    {
        JsonArray array = Json.array().asArray();
        
        for (IItem item: children)
        {
            array.add(item.toJSON());
        }
        
        return array;
    }
    
    public static IItem createItem(JsonValue json)
    {
        JsonObject jsonObj = json.asObject();
        String type = jsonObj.getString("Type", null);
        json = jsonObj.get("Value");
        
        try
        {
            Class<?> itemClass = Class.forName("SpeakerSim." + type);
            if (!IItem.class.isAssignableFrom(itemClass))
            {
                throw new HandledException("Unsupported item: " + type);
            }
            
            Constructor<?> constructor = itemClass.getConstructor(JsonValue.class);
            IItem item = (IItem) constructor.newInstance(json);
            item.create(jsonObj);
            return item;
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
                c.add(createItem(entry));
            }
        }

        return c;
    }
    
    @Override
    public void create(JsonValue json)
    {
        JsonObject jsonObj = json.asObject();
        status = Status.valueOf(jsonObj.getString("Status", Status.CONNECTED.toString()));
    }
    
    protected void fromJSON(JsonValue json)
    {
        JsonObject jsonObj = json.asObject();
        children = childrenFromJSON(jsonObj.get("Children"));
    }
    
    protected JsonObject itemToJSON()
    {
        JsonObject json = Json.object();
        if (!children.isEmpty())
        {
            json.add("Children", childrenToJSON());
        }
        return json;
    }
    
    @Override
    public JsonValue toJSON()
    {
        JsonObject json = Json.object();
        json.add("Type", getClass().getSimpleName());
        JSON.add(json, "Status", status.toString(), Status.CONNECTED.toString());
        json.add("Value", itemToJSON());
        return json;
    }
    
    @Override
    final public List<IItem> getChildren()
    {
        return children;
    }
    
    @Override
    public void refresh()
    {
        childrenRefresh();
    }
    
    protected Complex itemResponse(double f)
    {
        return childrenResponse(f);
    }
    
    protected Complex itemResponse1W(double f)
    {
        return childrenResponse1W(f);
    }
    
    protected Complex itemListeningWindowResponse(double f)
    {
        return childrenListeningWindowResponse(f);
    }
    
    protected Complex itemPowerResponse(double f)
    {
        return childrenPowerResponse(f);
    }
    
    protected Complex itemResponseWithBaffle(double f)
    {
        return childrenResponseWithBaffle(f);
    }
    
    protected Complex itemResponseWithRoom(double f)
    {
        return childrenResponseWithRoom(f);
    }
    
    protected Complex itemImpedance(double f)
    {
        return childrenImpedance(f);
    }
    
    protected Complex itemFilter(double f)
    {
        return childrenFilter(f);
    }
    
    protected double itemMaxPower(double f)
    {
        return childrenMaxPower(f);
    }
    
    protected double itemExcursion(double f, double power)
    {
        return childrenExcursion(f, power);
    }
    
    @Override
    final public Complex response(double f)
    {
        switch (status)
        {
            case DISCONNECTED:
                return new Complex(1);
            case BYPASSED:
                return childrenResponse(f);
            default:
                return itemResponse(f);
        }
    }
    
    @Override
    final public Complex response1W(double f)
    {
        switch (status)
        {
            case DISCONNECTED:
                return new Complex(1);
            case BYPASSED:
                return childrenResponse1W(f);
            default:
                return itemResponse1W(f);
        }
    }
    
    @Override
    final public Complex listeningWindowResponse(double f)
    {
        switch (status)
        {
            case DISCONNECTED:
                return new Complex(1);
            case BYPASSED:
                return childrenListeningWindowResponse(f);
            default:
                return itemListeningWindowResponse(f);
        }
    }
    
    @Override
    final public Complex powerResponse(double f)
    {
        switch (status)
        {
            case DISCONNECTED:
                return new Complex(1);
            case BYPASSED:
                return childrenPowerResponse(f);
            default:
                return itemPowerResponse(f);
        }
    }
    
    @Override
    final public Complex responseWithBaffle(double f)
    {
        switch (status)
        {
            case DISCONNECTED:
                return new Complex(1);
            case BYPASSED:
                return childrenResponseWithBaffle(f);
            default:
                return itemResponseWithBaffle(f);
        }
    }
    
    @Override
    final public Complex responseWithRoom(double f)
    {
        switch (status)
        {
            case DISCONNECTED:
                return new Complex(1);
            case BYPASSED:
                return childrenResponseWithRoom(f);
            default:
                return itemResponseWithRoom(f);
        }
    }
    
    @Override
    final public Complex impedance(double f)
    {
        switch (status)
        {
            case DISCONNECTED:
                return new Complex(Double.MAX_VALUE);
            case BYPASSED:
                return childrenImpedance(f);
            default:
                return itemImpedance(f);
        }
    }
    
    @Override
    final public Complex filter(double f)
    {
        switch (status)
        {
            case DISCONNECTED:
                return new Complex(0);
            case BYPASSED:
                return childrenFilter(f);
            default:
                return itemFilter(f);
        }
    }
    
    @Override
    final public double maxPower(double f)
    {
        switch (status)
        {
            case DISCONNECTED:
                return Double.MAX_VALUE;
            case BYPASSED:
                return childrenMaxPower(f);
            default:
                return itemMaxPower(f);
        }
    }
    
    @Override
    final public double excursion(double f, double power)
    {
        switch (status)
        {
            case DISCONNECTED:
                return 0;
            case BYPASSED:
                return childrenExcursion(f, power);
            default:
                return itemExcursion(f, power);
        }
    }
}
