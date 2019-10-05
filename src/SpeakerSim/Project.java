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
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

public class Project extends Item
{
    public Date Version;
    public Settings Settings;
    public Environment Environment;
    public Position CenterPosition;
    public Position ListeningPosition;
    
    private boolean modified;
    
    private static Project instance;
    private static Date currentVersion;
    private static final SimpleDateFormat VERSION_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    
    public static Project getInstance()
    {
        return instance;
    }
    
    public static void setInstance(Project p)
    {
        instance = p;
    }
    
    public boolean isModified()
    {
        return modified;
    }
    
    public void setModified()
    {
        modified = true;
    }
    
    public static Date parseVersion(String version)
    {
        try
        {
            return VERSION_FORMAT.parse(version);
        }
        catch (ParseException ex)
        {
            return new Date(0);
        }
    }
    
    public static Date currentVersion()
    {
        if (currentVersion == null)
        {
            try
            {
                final Properties properties = new Properties();
                properties.load(Project.class.getClassLoader().getResourceAsStream("project.properties"));
                currentVersion = parseVersion(properties.getProperty("version"));
            }
            catch (IOException ex)
            {
                currentVersion = new Date(0);
            }
        }
        
        return currentVersion;
    }
    
    public static String currentVersionString()
    {
        return VERSION_FORMAT.format(currentVersion());
    }
    
    public Project()
    {
        super();
        
        if (instance == null)
        {
            instance = this;
        }
        
        Version = currentVersion();
        Settings = new Settings();
        Environment = new Environment();
        ListeningPosition = new Position();
        ListeningPosition.X = 1.1;
        ListeningPosition.Y = 1.866;
        
        modified = false;
    }
    
    public Project(JsonValue json)
    {
        JsonObject jsonObj = json.asObject();
        
        Version = parseVersion(jsonObj.getString("Version", ""));
        
        Project p = instance;
        try
        {
            instance = this;
            
            Settings = new Settings(jsonObj.get("Settings"));
            Environment = new Environment(jsonObj.get("Environment"));
            ListeningPosition = new Position(jsonObj.get("ListeningPosition"));

            modified = false;
            super.fromJSON(json);
        }
        catch (Exception e)
        {
            instance = p;
            
            if (Version.compareTo(currentVersion()) > 0)
            {
                throw new HandledException("Can't open project file that was created with newer version!");
            }
            
            throw e;
        }
        
        Version = currentVersion();
    }
    
    public Project(File file) throws IOException
    {
        this(JSON.open(file));
    }
    
    public Project(InputStream stream) throws IOException
    {
        this(JSON.open(stream));
    }
    
    @Override
    public JsonValue toJSON()
    {
        JsonObject json = Json.object();
        
        json.add("Version", currentVersionString());
        json.add("Settings", Settings.toJSON());
        json.add("Environment", Environment.toJSON());
        json.add("ListeningPosition", ListeningPosition.toJSON());
        json.add("Children", Item.childrenToJSON(children));
        
        return json;
    }
    
    @Override
    public String toString()
    {
        return "System";
    }
    
    public void save(File file) throws IOException
    {
        JSON.save(toJSON(), file);
        modified = false;
    }
    
    @Override
    public Complex impedance(double f)
    {
        return Complex.ZERO;
    }
}
