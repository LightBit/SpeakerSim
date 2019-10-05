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

public class ClosedBoxSimulation implements ISimulation
{
    private final Driver driver;
    private final DistanceSimulation distance;
    private final BaffleSimulation baffle;
    private final RoomSimulation room;
    private final PowerResponseSimulation powerResponse;
    private final ListeningWindowSimulation listeningWindow;
    
    private final double Cas;
    private final double Mas;
    private final double Ras;
    private final double Rae;
    private final double Cab;
    private final double Rab;
    private final double Ral;
    
    public static double calcQtc(double Vb, double Vas, double Qts)
    {
        return Math.sqrt(Vas / Vb + 1) * Qts;
    }
    
    public static double calcFb(double Qtc, double Qts, double Fs)
    {
        return Qtc / Qts * Fs;
    }
    
    public ClosedBoxSimulation(Environment env, ClosedBox box, Driver driver, Baffle baffle, Position driverPos, Position centerPos, Position listeningPos)
    {
        this.driver = driver;
        this.distance = new DistanceSimulation(driverPos, listeningPos, env);
        this.baffle = new BaffleSimulation(baffle, driver, driverPos, listeningPos, env, false);
        this.room = new RoomSimulation(baffle, driver, driverPos, listeningPos, env, false);
        this.powerResponse = new PowerResponseSimulation(baffle, driver, driverPos, centerPos, env, false);
        this.listeningWindow = new ListeningWindowSimulation(baffle, driver, driverPos, centerPos, env, false);
        
        Cas = driver.calcCas();
        Mas = driver.calcMas();
        Ras = driver.calcRas();
        Rae = driver.calcRae();
        
        Cab = box.Vb / (env.AirDensity * env.SpeedOfSound * env.SpeedOfSound);
        double Cat = Cas * Cab / (Cas + Cab);
        double Fb = 1 / (2 * Math.PI * Math.sqrt(Cat * Mas));
        Rab = 1 / (2 * Math.PI * Fb * box.Qa * Cab);
        Ral = box.Ql / (2 * Math.PI * Fb * Cab);
    }
    
    private Complex Zat(double f)
    {
        f *= 2 * Math.PI;
        Complex Zas = new Complex(Ras, f * Mas - 1 / (f * Cas));
        Complex Zab = Fnc.zParallel(new Complex(Rab, -1 / (f * Cab)), new Complex(Ral));
        return Zas.add(Zab);
    }
    
    private Complex box(double f)
    {
        Complex x = new Complex(0, 2 * Math.PI * f * Mas).divide(Zat(f).add(Rae));
        
        /*if (driver.Inverted)
        {
            x = x.conjugate();
        }*/

        return x;
    }
    
    @Override
    public Complex response(double f)
    {
        Complex x = box(f).multiply(driver.normResponse(f));
        
        /*if (driver.Inverted)
        {
            x = x.conjugate();
        }*/
        
        return x.multiply(distance.response(f));
    }
    
    @Override
    public Complex response1W(double f)
    {
        Complex x = box(f).multiply(driver.normResponse1W(f));
        
        /*if (driver.Inverted)
        {
            x = x.conjugate();
        }*/
        
        return x.multiply(distance.response(f));
    }
    
    @Override
    public Complex listeningWindowResponse(double f)
    {
        Complex x = box(f).multiply(driver.normResponse(f)).multiply(listeningWindow.response(f));
        
        /*if (driver.Inverted)
        {
            x = x.conjugate();
        }*/
        
        return x;
    }
    
    @Override
    public Complex powerResponse(double f)
    {
        Complex x = box(f).multiply(driver.normResponse(f)).multiply(powerResponse.response(f));
        
        /*if (driver.Inverted)
        {
            x = x.conjugate();
        }*/
        
        return x;
    }
    
    @Override
    public Complex responseWithBaffle(double f)
    {
        Complex x = box(f).multiply(driver.normResponse(f)).multiply(baffle.response(f));
        
        /*if (driver.Inverted)
        {
            x = x.conjugate();
        }*/
        
        return x.multiply(distance.response(f));
    }
    
    @Override
    public Complex responseWithRoom(double f)
    {
        Complex x = box(f).multiply(driver.normResponse(f)).multiply(room.response(f));
        
        /*if (driver.Inverted)
        {
            x = x.conjugate();
        }*/
        
        return x.multiply(distance.response(f));
    }
    
    @Override
    public double excursion(double f, double Pe)
    {
        double maxVad = driver.voltage(Math.min(Pe, driver.PeRMS())) * driver.Bl / (driver.Re * driver.Sd);
        return new Complex(0, maxVad).divide(Zat(f).add(Rae)).abs() / (2 * Math.PI * f * driver.Sd) * 1000;
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
    public Complex impedance(double f)
    {
        Complex Zeb = new Complex(driver.Bl * driver.Bl / (driver.Sd * driver.Sd)).divide(Zat(f));
        return new Complex(0, driver.LeZ(f)).add(Zeb.add(driver.Re));
    }
    
    public double dBmag(double f)
    {
        return Fnc.toDecibels(box(f).abs());
    }
}
