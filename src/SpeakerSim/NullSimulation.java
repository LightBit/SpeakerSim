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

public class NullSimulation extends BaffleSimulation implements ISimulation
{
    private final Driver driver;
    private final DistanceSimulation distance;
    private final RoomSimulation room;
    private final PowerResponseSimulation powerResponse;
    private final ListeningWindowSimulation listeningWindow;
    
    public NullSimulation(Environment env, Driver driver, Baffle baffle, Position driverPos, Position centerPos, Position listeningPos)
    {
        super(baffle, driver, driverPos, listeningPos, env, false);
        
        this.driver = driver;
        this.distance = new DistanceSimulation(driverPos, listeningPos, env);
        this.room = new RoomSimulation(baffle, driver, driverPos, listeningPos, env, false);
        this.powerResponse = new PowerResponseSimulation(baffle, driver, driverPos, centerPos, env, false);
        this.listeningWindow = new ListeningWindowSimulation(baffle, driver, driverPos, centerPos, env, false);
    }
    
    @Override
    public Complex response(double f)
    {
        return driver.response(f).multiply(distance.response(f));
    }
    
    @Override
    public Complex response1W(double f)
    {
        return driver.response1W(f).multiply(distance.response(f));
    }
    
    @Override
    public Complex listeningWindowResponse(double f)
    {
        return driver.response(f).multiply(listeningWindow.response(f));
    }
    
    @Override
    public Complex powerResponse(double f)
    {
        return driver.response(f).multiply(powerResponse.response(f));
    }
    
    @Override
    public Complex impedance(double f)
    {
        return driver.impedance(f);
    }
    
    @Override
    public double maxPower(double f)
    {
        double Pe = driver.PeRMS();
        
        if (driver.Xmax == 0)
        {
            return Pe;
        }
        
        return Math.min(Math.pow(driver.Xmax * 1000 / excursion(f, Pe), 2) * Pe, Pe);
    }
    
    @Override
    public double excursion(double f, double Pe)
    {
        return driver.excursion(f, Pe);
    }
    
    @Override
    public Complex responseWithBaffle(double f)
    {
        return driver.response(f).multiply(super.response(f)).multiply(distance.response(f));
    }
    
    @Override
    public Complex responseWithRoom(double f)
    {
        return driver.response(f).multiply(room.response(f)).multiply(distance.response(f));
    }
}
