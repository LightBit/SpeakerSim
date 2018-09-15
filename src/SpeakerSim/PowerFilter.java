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
import com.eclipsesource.json.JsonValue;

public final class PowerFilter implements JSONable
{
    public enum FilterType
    {
        None,
        IEC_268_5,
        AES2_1984
    }
    
    public static final String[] FILTERS =
    {
        "None",
        "IEC 268-5",
        "AES2-1984"
    };
    
    private FilterType type;
    
    @Override
    public String toString()
    {
        switch (type)
        {
            case IEC_268_5: return "IEC 268-5";
            case AES2_1984: return "AES2-1984";
            default: return "None";
        }
    }
    
    public static FilterType valueOf(String name)
    {
        switch (name)
        {
            case "IEC 268-5": return FilterType.IEC_268_5;
            case "AES2-1984": return FilterType.AES2_1984;
            default: return FilterType.None;
        }
    }
    
    public PowerFilter(JsonValue json)
    {
        type = json != null ? valueOf(json.asString()) : FilterType.None;
    }
    
    public PowerFilter()
    {
        this.type = FilterType.None;
    }
    
    public PowerFilter(FilterType type)
    {
        this.type = type;
    }
    
    public void setType(FilterType type)
    {
        this.type = type;
    }
    
    public FilterType getType()
    {
        return type;
    }
    
    private static double secondOrderButterworth(double fn)
    {
        return 1 / Math.hypot(1 - fn * fn, 1.4142 * fn);
    }
    
    public double filter(double f)
    {
        switch (type)
        {
            case IEC_268_5: return 1 / Math.sqrt(f) * secondOrderButterworth(f / 5000) * secondOrderButterworth(40 / f);
            case AES2_1984: return 1 / Math.sqrt(f);
            default: return 1;
        }
    }
    
    public double toRMS(double power, double f)
    {
        switch (type)
        {
            case IEC_268_5: return power * filter(Math.max(f, 80));
            default: return power * filter(Math.max(f, 1));
        }
    }
    
    public double toFiltered(double power, double f)
    {
        return power / filter(f);
    }
    
    @Override
    public JsonValue toJSON()
    {
        return Json.value(toString());
    }
}
