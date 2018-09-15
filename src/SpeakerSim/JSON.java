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
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public final class JSON
{
    private JSON()
    {
        
    }
    
    public static void add(JsonObject object, String name, JsonValue value)
    {
        if (value != null)
        {
            object.add(name, value);
        }
    }
    
    public static void add(JsonObject object, String name, double value, double defaultValue)
    {
        if (value != defaultValue)
        {
            object.add(name, value);
        }
    }
    
    public static void add(JsonObject object, String name, double value)
    {
        add(object, name, value, 0);
    }
    
    public static void add(JsonObject object, String name, int value, int defaultValue)
    {
        if (value != defaultValue)
        {
            object.add(name, value);
        }
    }
    
    public static void add(JsonObject object, String name, int value)
    {
        add(object, name, value, 0);
    }
    
    public static void add(JsonObject object, String name, String value, String defaultValue)
    {
        if (value != null && !defaultValue.equals(value))
        {
            object.add(name, value);
        }
    }
    
    public static void add(JsonObject object, String name, String value)
    {
        add(object, name, value, "");
    }
    
    public static void add(JsonObject object, String name, boolean value, boolean defaultValue)
    {
        if (value != defaultValue)
        {
            object.add(name, value);
        }
    }
   
    public static void add(JsonObject object, String name, boolean value)
    {
        add(object, name, value, false);
    }
    
    public static double getDouble(JsonObject object, String name, double defaultValue)
    {
        JsonValue value = object.get(name);
        if (value == null)
        {
            return defaultValue;
        }
        
        return value.asDouble();
    }
    
    public static double getDouble(JsonObject object, String name)
    {
        return getDouble(object, name, 0);
    }
    
    public static int getInt(JsonObject object, String name, int defaultValue)
    {
        JsonValue value = object.get(name);
        if (value == null)
        {
            return defaultValue;
        }
        
        return value.asInt();
    }
    
    public static int getInt(JsonObject object, String name)
    {
        return getInt(object, name, 0);
    }
    
    public static String getString(JsonObject object, String name, String defaultValue)
    {
        JsonValue value = object.get(name);
        if (value == null)
        {
            return defaultValue;
        }
        
        return value.asString();
    }
    
    public static String getString(JsonObject object, String name)
    {
        return getString(object, name, "");
    }
    
    public static boolean getBoolean(JsonObject object, String name, boolean defaultValue)
    {
        JsonValue value = object.get(name);
        if (value == null)
        {
            return defaultValue;
        }
       
        return value.asBoolean();
    }
   
    public static boolean getBoolean(JsonObject object, String name)
    {
        return getBoolean(object, name, false);
    }

    public static void save(JsonValue json, File file) throws IOException
    {
        try (FileWriter f = new FileWriter(file))
        {
            json.writeTo(f);
        }
    }

    public static JsonValue open(File file) throws IOException
    {
        JsonValue json;
        try (Reader f = new FileReader(file))
        {
            json = Json.parse(f);
        }
        return json;
    }
    
    public static JsonValue open(InputStream stream) throws IOException
    {
        JsonValue json;
        try (Reader f = new InputStreamReader(stream))
        {
            json = Json.parse(f);
        }
        return json;
    }
}
