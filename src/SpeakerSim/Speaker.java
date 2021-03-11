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
        if (!Driver.Closed)
        {
            // select "best" enclosure type
            if (Driver.Qts > 1.2)
            {
                setSimulator(Speaker.SimulatorType.OPEN_BAFFLE);
            }
            else if (Driver.Qts > 0.7 || Driver.EBP() < 50)
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
        Baffle = new Baffle();
        ClosedBox = new ClosedBox();
        BassReflex = new BassReflex();
        Aperiodic = new Aperiodic();
        setSimulator(SimulatorType.BASS_REFLEX);
    }
    
    public Speaker(JsonValue json)
    {
        super.fromJSON(json);
        JsonObject jsonObj = json.asObject();
        
        Driver = new Driver(jsonObj.get("Driver"));
        Position = new Position(jsonObj.get("Position"));
        Baffle = new Baffle(jsonObj.get("Baffle"));
        ClosedBox = new ClosedBox(jsonObj.get("ClosedBox"));
        BassReflex = new BassReflex(jsonObj.get("BassReflex"));
        Aperiodic = new Aperiodic(jsonObj.get("Aperiodic"));
        setSimulator(SimulatorType.valueOf(jsonObj.get("Simulator").asString()));
    }
    
    public Speaker(File file) throws IOException
    {
        this(JSON.open(file));
    }
    
    @Override
    protected JsonObject itemToJSON()
    {
        JsonObject json = super.itemToJSON();
        
        json.add("Driver", Driver.toJSON());
        json.add("Baffle", Baffle.toJSON());
        json.add("Position", Position.toJSON());
        json.add("Simulator", Simulator.toString());
        json.add("ClosedBox", ClosedBox.toJSON());
        json.add("BassReflex", BassReflex.toJSON());
        json.add("Aperiodic", Aperiodic.toJSON());
        
        return json;
    }
    
    @Override
    protected Complex itemResponse(double f)
    {
        Complex superZ = super.itemImpedance(f);
        Complex thisZ = simulation.impedance(f);
        return super.itemResponse(f).multiply(superZ.divide(superZ.add(thisZ))).add(simulation.response(f).multiply(thisZ.divide(thisZ.add(superZ))));
    }
    
    @Override
    protected Complex itemResponse1W(double f)
    {
        Complex superZ = super.itemImpedance(f);
        Complex thisZ = simulation.impedance(f);
        return super.itemResponse1W(f).multiply(superZ.divide(superZ.add(thisZ))).add(simulation.response1W(f).multiply(thisZ.divide(thisZ.add(superZ))));
    }
    
    @Override
    protected Complex itemListeningWindowResponse(double f)
    {
        Complex superZ = super.itemImpedance(f);
        Complex thisZ = simulation.impedance(f);
        return super.itemListeningWindowResponse(f).multiply(superZ.divide(superZ.add(thisZ))).add(simulation.listeningWindowResponse(f).multiply(thisZ.divide(thisZ.add(superZ))));
    }
    
    @Override
    protected Complex itemPowerResponse(double f)
    {
        Complex superZ = super.itemImpedance(f);
        Complex thisZ = simulation.impedance(f);
        return super.itemPowerResponse(f).multiply(superZ.divide(superZ.add(thisZ))).add(simulation.powerResponse(f).multiply(thisZ.divide(thisZ.add(superZ))));
    }
    
    @Override
    protected Complex itemResponseWithBaffle(double f)
    {
        Complex superZ = super.itemImpedance(f);
        Complex thisZ = simulation.impedance(f);
        return super.itemResponseWithBaffle(f).multiply(superZ.divide(superZ.add(thisZ))).add(simulation.responseWithBaffle(f).multiply(thisZ.divide(thisZ.add(superZ))));
    }
    
    @Override
    protected Complex itemResponseWithRoom(double f)
    {
        Complex superZ = super.itemImpedance(f);
        Complex thisZ = simulation.impedance(f);
        return super.itemResponseWithRoom(f).multiply(superZ.divide(superZ.add(thisZ))).add(simulation.responseWithRoom(f).multiply(thisZ.divide(thisZ.add(superZ))));
    }
    
    @Override
    protected Complex itemFilter(double f)
    {
        Complex superZ = super.itemImpedance(f);
        Complex thisZ = simulation.impedance(f);
        return super.itemFilter(f).multiply(superZ.divide(superZ.add(thisZ))).add(thisZ.divide(thisZ.add(superZ)));
    }
    
    @Override
    protected Complex itemImpedance(double f)
    {
        return super.itemImpedance(f).add(simulation.impedance(f));
    }
    
    @Override
    protected double itemMaxPower(double f)
    {
        Complex superZ = super.itemImpedance(f);
        Complex thisZ = simulation.impedance(f);
        return Math.min(super.itemMaxPower(f) / superZ.divide(superZ.add(thisZ)).abs(), Settings.getInstance().PowerFilter.toFiltered(simulation.maxPower(f), f) / thisZ.divide(thisZ.add(superZ)).abs());
    }
    
    @Override
    protected double itemExcursion(double f, double power)
    {
        return simulation.excursion(f, power) * itemFilter(f).abs();
    }
    
    @Override
    public String toString()
    {
        return "Speaker (" + Driver.Name + ")";
    }
}
