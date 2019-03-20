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
import java.util.Properties;

public class Project extends Item
{
    public String Version;
    public Settings Settings;
    public Environment Environment;
    public Position CenterPosition;
    public Position ListeningPosition;
    
    private boolean modified;
    
    private static Project instance;
    private static String currentVersion;
    
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
    
    public static String currentVersion()
    {
        if (currentVersion == null)
        {
            try
            {
                final Properties properties = new Properties();
                properties.load(Project.class.getClassLoader().getResourceAsStream("project.properties"));
                currentVersion = properties.getProperty("version");
            }
            catch (IOException ex)
            {
            }
        }
        
        return currentVersion;
    }
    
    public Project()
    {
        super();
        
        Version = currentVersion();
        Settings = new Settings();
        Environment = new Environment();
        ListeningPosition = new Position();
        ListeningPosition.X = 1.1;
        ListeningPosition.Y = 1.866;
        
        modified = false;
        instance = this;
    }
    
    public Project(JsonValue json)
    {
        JsonObject jsonObj = json.asObject();
        Version = jsonObj.getString("Version", "");
        
        try
        {
            Settings = new Settings(jsonObj.get("Settings"));
            Environment = new Environment(jsonObj.get("Environment"));
            ListeningPosition = new Position(jsonObj.get("ListeningPosition"));

            modified = false;
            super.fromJSON(json);
            instance = this;
        }
        catch (Exception e)
        {
            if (Version.compareTo(currentVersion()) > 0)
            {
                throw new HandledException("Can't open project file that was created with newer version!");
            }
            
            throw e;
        }
    }
    
    public Project(File file) throws IOException
    {
        this(JSON.open(file));
        modified = false;
    }
    
    public Project(InputStream stream) throws IOException
    {
        this(JSON.open(stream));
        modified = false;
    }
    
    @Override
    public JsonValue toJSON()
    {
        JsonObject json = Json.object();
        
        json.add("Version", currentVersion());
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
        return new Complex(0);
    }
}
