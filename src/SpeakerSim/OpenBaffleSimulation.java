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

public class OpenBaffleSimulation implements ISimulation
{
    private final Driver driver;
    private final DistanceSimulation distance;
    private final BaffleSimulation baffle;
    private final RoomSimulation room;
    private final PowerResponseSimulation powerResponse;
    private final ListeningWindowSimulation listeningWindow;
    private final double horizontalAngle;
    private final double verticalAngle;
    //private final double wavelength;
    private final double listeningQuarterWave;
    
    public OpenBaffleSimulation(Environment env, Driver driver, Baffle baffle, Position driverPos, Position centerPos, Position listeningPos)
    {
        this.driver = driver;
        this.distance = new DistanceSimulation(driverPos, listeningPos, env);
        this.baffle = new BaffleSimulation(baffle, driver, driverPos, listeningPos, env, true);
        this.room = new RoomSimulation(driver, driverPos, env, baffle, true);
        this.powerResponse = new PowerResponseSimulation(baffle, driver, driverPos, centerPos, env, true);
        this.listeningWindow = new ListeningWindowSimulation(baffle, driver, driverPos, centerPos, env, true);
        horizontalAngle = driverPos.horizontalAngle(listeningPos);
        verticalAngle = driverPos.verticalAngle(listeningPos);
        
        //wavelength = env.SpeedOfSound / Math.min(baffle.Width, baffle.Height);
        listeningQuarterWave = env.SpeedOfSound / driverPos.distance(listeningPos) / 4;
    }
    
    @Override
    public Complex response(double f)
    {
        Complex x = driver.normResponse(f, horizontalAngle, verticalAngle, true).multiply(driver.responseSimRelative(f));
        //x = x.divide(Math.sqrt(1 + Math.pow(listeningQuarterWave / f, 2)));
        
        return x.multiply(distance.response(f));
    }
    
    @Override
    public Complex response1W(double f)
    {
        Complex x = driver.normResponse1W(f).multiply(driver.responseSimRelative(f));
        //x = x.divide(Math.sqrt(1 + Math.pow(listeningQuarterWave / f, 2)));
        
        return x.multiply(distance.response(f));
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
    public Complex listeningWindowResponse(double f)
    {
        //Complex x = driver.normResponse(f).multiply(driver.responseSimRelative(f)).multiply(listeningWindow.response(f).divide(Math.sqrt(1 + Math.pow(wavelength / f, 2))));
        Complex x = driver.normResponse(f).multiply(driver.responseSimRelative(f)).multiply(listeningWindow.response(f));
        x = x.divide(Math.sqrt(1 + Math.pow(listeningQuarterWave / f, 2)));
        
        return x;
    }

    @Override
    public Complex powerResponse(double f)
    {
        //Complex x = driver.normResponse(f).multiply(driver.responseSimRelative(f)).multiply(powerResponse.response(f).divide(Math.sqrt(1 + Math.pow(wavelength / f, 2))));
        Complex x = driver.normResponse(f).multiply(driver.responseSimRelative(f)).multiply(powerResponse.response(f));
        x = x.divide(Math.sqrt(1 + Math.pow(listeningQuarterWave / f, 2)));
        
        return x;
    }

    @Override
    public double excursion(double f, double Pe)
    {
        return driver.excursion(f, Pe);
    }

    @Override
    public Complex responseWithBaffle(double f)
    {
        Complex x = driver.normResponse(f, horizontalAngle, verticalAngle, true);
        x = x.multiply(driver.responseSimRelative(f));
        x = x.divide(Math.sqrt(1 + Math.pow(listeningQuarterWave / f, 2)));
        //x = x.multiply(baffle.response(f).divide(Math.sqrt(1 + Math.pow(wavelength / f, 2))));
        x = x.multiply(baffle.response(f));
        
        return x.multiply(distance.response(f));
    }

    @Override
    public Complex responseWithRoom(double f)
    {
        Complex x = driver.normResponse(f, horizontalAngle, verticalAngle, true);
        x = x.multiply(driver.responseSimRelative(f));
        x = x.multiply(room.response(f));
        
        return x.multiply(distance.response(f));
    }
}
