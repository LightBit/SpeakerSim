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

public class Speaker extends Item
{
    public enum SimulatorType
    {
        CLOSED_BOX,
        BASS_REFLEX,
        APERIODIC,
        OPEN_BAFFLE
    }
    
    public Driver Driver;
    public Position Position;
    public Baffle Baffle;
    public SimulatorType Simulator;
    public ClosedBox ClosedBox;
    public BassReflex BassReflex;
    public Aperiodic Aperiodic;
    
    private ISimulation simulation;
    
    @Override
    public final void refresh()
    {
        super.refresh();
        
        Project project = Project.getInstance();
        
        if (Driver.Closed)
        {
            simulation = new NullSimulation(project.Environment, Driver, Baffle, Position, project.CenterPosition, project.ListeningPosition);
        }
        else
        {
            switch (Simulator)
            {
                case CLOSED_BOX:
                    simulation = new ClosedBoxSimulation(project.Environment, ClosedBox, Driver, Baffle, Position, project.CenterPosition, project.ListeningPosition);
                    break;

                case BASS_REFLEX:
                    simulation = new BassReflexSimulation(project.Environment, BassReflex, Driver, Baffle, Position, project.CenterPosition, project.ListeningPosition);
                    break;
                
                case APERIODIC:
                    simulation = new AperiodicSimulation(project.Environment, Aperiodic, Driver, Baffle, Position, project.CenterPosition, project.ListeningPosition);
                    break;

                case OPEN_BAFFLE:
                    simulation = new OpenBaffleSimulation(project.Environment, Driver, Baffle, Position, project.CenterPosition, project.ListeningPosition);
                    break;

                default:
                    break;
            }
        }
    }
    
    public void setSimulators()
    {
        // select "best" enclosure type
        if (Driver.Qts > 1.2)
        {
            setSimulator(Speaker.SimulatorType.OPEN_BAFFLE);
        }
        else if (Driver.Qts > 0.7 || Driver.calcEBP() < 50)
        {
            setSimulator(Speaker.SimulatorType.CLOSED_BOX);
        }
        else
        {
            setSimulator(Speaker.SimulatorType.BASS_REFLEX);
        }

        // calculate enclosures
        BassReflexSimulation.calcBox(BassReflex, Driver);
        ClosedBoxSimulation.calcBox(ClosedBox, Driver);
        AperiodicSimulation.calcBox(Aperiodic, Driver);
    }
    
    public final void setSimulator(SimulatorType simulator)
    {
        Simulator = simulator;
    }
    
    public ISimulation getSimulation()
    {
        return simulation;
    }
    
    public Speaker()
    {
        super();
        Driver = new Driver();
        Position = new Position();
        Position.HorizontalAngle = 30;
        Baffle = new Baffle();
        ClosedBox = new ClosedBox();
        BassReflex = new BassReflex();
        Aperiodic = new Aperiodic();
        setSimulator(SimulatorType.BASS_REFLEX);
    }
    
    public Speaker(JsonValue json)
    {
        super();
        JsonObject jsonObj = json.asObject();
        
        Driver = new Driver(jsonObj.get("Driver"));
        Position = new Position(jsonObj.get("Position"));
        Baffle = new Baffle(jsonObj.get("Baffle"));
        ClosedBox = new ClosedBox(jsonObj.get("ClosedBox"));
        BassReflex = new BassReflex(jsonObj.get("BassReflex"));
        Aperiodic = new Aperiodic(jsonObj.get("Aperiodic"));
        setSimulator(SimulatorType.valueOf(jsonObj.get("Simulator").asString()));
        super.fromJSON(json);
    }
    
    public Speaker(File file) throws IOException
    {
        this(JSON.open(file));
    }
    
    @Override
    public JsonValue toJSON()
    {
        JsonObject json = Json.object();
        
        json.add("Driver", Driver.toJSON());
        json.add("Baffle", Baffle.toJSON());
        json.add("Position", Position.toJSON());
        json.add("Simulator", Simulator.toString());
        json.add("ClosedBox", ClosedBox.toJSON());
        json.add("BassReflex", BassReflex.toJSON());
        json.add("Aperiodic", Aperiodic.toJSON());
        json.add("Children", Item.childrenToJSON(children));
        
        return json;
    }
    
    @Override
    public Complex response(double f)
    {
        Complex superZ = super.impedance(f);
        Complex thisZ = simulation.impedance(f);
        return super.response(f).multiply(superZ.divide(superZ.add(thisZ))).add(simulation.response(f).multiply(thisZ.divide(thisZ.add(superZ))));
    }
    
    @Override
    public Complex response1W(double f)
    {
        Complex superZ = super.impedance(f);
        Complex thisZ = simulation.impedance(f);
        return super.response1W(f).multiply(superZ.divide(superZ.add(thisZ))).add(simulation.response1W(f).multiply(thisZ.divide(thisZ.add(superZ))));
    }
    
    @Override
    public Complex listeningWindowResponse(double f)
    {
        Complex superZ = super.impedance(f);
        Complex thisZ = simulation.impedance(f);
        return super.response(f).multiply(superZ.divide(superZ.add(thisZ))).add(simulation.listeningWindowResponse(f).multiply(thisZ.divide(thisZ.add(superZ))));
    }
    
    @Override
    public Complex powerResponse(double f)
    {
        Complex superZ = super.impedance(f);
        Complex thisZ = simulation.impedance(f);
        return super.response(f).multiply(superZ.divide(superZ.add(thisZ))).add(simulation.powerResponse(f).multiply(thisZ.divide(thisZ.add(superZ))));
    }
    
    @Override
    public Complex impedance(double f)
    {
        return super.impedance(f).add(simulation.impedance(f));
    }
    
    @Override
    public double maxPower(double f)
    {
        Complex superZ = super.impedance(f);
        Complex thisZ = simulation.impedance(f);
        return Math.min(super.maxPower(f) / superZ.divide(superZ.add(thisZ)).abs(), Project.getInstance().Settings.PowerFilter.toFiltered(simulation.maxPower(f), f) / thisZ.divide(thisZ.add(superZ)).abs());
    }
    
    @Override
    public double excursion(double f, double power)
    {
        return simulation.excursion(f, power) * filter(f).abs();
    }
    
    @Override
    public Complex filter(double f)
    {
        Complex superZ = super.impedance(f);
        Complex thisZ = simulation.impedance(f);
        return super.filter(f).multiply(superZ.divide(superZ.add(thisZ))).add(thisZ.divide(thisZ.add(superZ)));
    }
    
    @Override
    public Complex responseWithBaffle(double f)
    {
        Complex superZ = super.impedance(f);
        Complex thisZ = simulation.impedance(f);
        return super.response(f).multiply(superZ.divide(superZ.add(thisZ))).add(simulation.responseWithBaffle(f).multiply(thisZ.divide(thisZ.add(superZ))));
    }
    
    @Override
    public Complex responseWithRoom(double f)
    {
        Complex superZ = super.impedance(f);
        Complex thisZ = simulation.impedance(f);
        return super.response(f).multiply(superZ.divide(superZ.add(thisZ))).add(simulation.responseWithRoom(f).multiply(thisZ.divide(thisZ.add(superZ))));
    }
    
    @Override
    public String toString()
    {
        return "Speaker (" + Driver.Name + ")";
    }
}
