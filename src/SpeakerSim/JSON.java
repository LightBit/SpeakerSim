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

import java.io.*;
import java.util.Arrays;
import java.util.zip.Adler32;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import com.eclipsesource.json.*;

public final class JSON
{
    private static final byte[] MAGIC = {0x53, 0x70, 0x65, 0x61, 0x6B, 0x65, 0x72, 0x53, 0x69, 0x6D, 0x01};
    
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
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (Writer w = new OutputStreamWriter(baos, "UTF-8"))
        {
            json.writeTo(w);
        }
        byte[] data = baos.toByteArray();
        
        Adler32 checksum = new Adler32();
        checksum.update(data);
        long crcValue = checksum.getValue();
        
        try (OutputStream os = new FileOutputStream(file))
        {
            os.write(MAGIC);
            os.write((int) (crcValue >>> 24) & 0xFF);
            os.write((int) (crcValue >>> 16) & 0xFF);
            os.write((int) (crcValue >>> 8) & 0xFF);
            os.write((int) crcValue & 0xFF);
            
            try (DeflaterOutputStream dos = new DeflaterOutputStream(os))
            {
                dos.write(data);
            }
        }
    }

    public static JsonValue open(File file) throws IOException
    {
        try (InputStream f = new BufferedInputStream(new FileInputStream(file)))
        {
            return open(f);
        }
    }
    
    public static JsonValue open(InputStream stream) throws IOException
    {
        try
        {
            BufferedInputStream bis;
            if (stream instanceof BufferedInputStream)
            {
                bis = (BufferedInputStream) stream;
            }
            else
            {
                bis = new BufferedInputStream(stream);
            }
            
            byte[] b = new byte[11];
            bis.mark(12);
            int len = bis.read(b, 0, 11);
            if (len == 11 && Arrays.equals(MAGIC, b))
            {
                len = bis.read(b, 0, 4);
                if (len != 4)
                {
                    throw new HandledException("File is damaged (missing checksum)!");
                }
                long expectedChecksum =
                    ((long)(b[0] & 0xFF) << 24) |
                    ((long)(b[1] & 0xFF) << 16) |
                    ((long)(b[2] & 0xFF) << 8)  |
                    ((long)(b[3] & 0xFF));
                
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try (InflaterInputStream iis = new InflaterInputStream(bis))
                {
                    byte[] buf = new byte[4096];
                    int n;
                    while ((n = iis.read(buf)) != -1)
                    {
                        baos.write(buf, 0, n);
                    }
                }
                byte[] data = baos.toByteArray();
                
                Adler32 checksum = new Adler32();
                checksum.update(data);
                if (checksum.getValue() != expectedChecksum)
                {
                    throw new HandledException("File is damaged (checksum mismatch)!");
                }
                
                try (Reader f = new InputStreamReader(new ByteArrayInputStream(data), "UTF-8"))
                {
                    return Json.parse(f);
                }
            }
            else
            {
                bis.reset();
                
                try (Reader f = new InputStreamReader(bis, "UTF-8"))
                {
                    return Json.parse(f);
                }
            }
        }
        catch (ParseException e)
        {
            throw new HandledException("File is not in correct format or is damaged!");
        }
    }
}
