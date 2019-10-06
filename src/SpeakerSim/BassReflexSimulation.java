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

public class BassReflexSimulation implements ISimulation
{
    private final BassReflex box;
    private final Driver driver;
    private final BaffleSimulation baffle;
    private final DistanceSimulation distanceCone;
    private final DistanceSimulation distancePort;
    private final RoomSimulation roomCone;
    private final RoomSimulation roomPort;
    private final PowerResponseSimulation powerResponseCone;
    private final PowerResponseSimulation powerResponsePort;
    private final ListeningWindowSimulation listeningWindowCone;
    private final ListeningWindowSimulation listeningWindowPort;
    
    private final double Cas;
    private final double Mas;
    private final double Ras;
    private final double Rae;
    private final double Cab;
    private final double Rab;
    private final double Ral;
    private final double Map;
    private final double Rap;
    
    public BassReflexSimulation(Environment env, BassReflex box, Driver driver, Baffle baffle, Position driverPos, Position centerPos, Position listeningPos)
    {
        this.box = box;
        this.driver = driver;
        this.baffle = new BaffleSimulation(baffle, driver, driverPos, listeningPos, env, false);
        this.distanceCone = new DistanceSimulation(driverPos, listeningPos, env);
        this.distancePort = new DistanceSimulation(/*driverPos.distance(box.PortPosition) + */box.PortPosition.distance(listeningPos), env);
        this.roomCone = new RoomSimulation(baffle, driver, driverPos, listeningPos, env, false);
        this.roomPort = new RoomSimulation(null, null, box.PortPosition, listeningPos, env, false);
        this.powerResponseCone = new PowerResponseSimulation(baffle, driver, driverPos, centerPos, env, false);
        this.powerResponsePort = new PowerResponseSimulation(baffle, null, box.PortPosition, centerPos, env, false);
        this.listeningWindowCone = new ListeningWindowSimulation(baffle, driver, driverPos, centerPos, env, false);
        this.listeningWindowPort = new ListeningWindowSimulation(baffle, null, box.PortPosition, centerPos, env, false);
        
        double Qp = box.Qp + 0.00000000000001;
        
        Cas = driver.calcCas();
        Mas = driver.calcMas();
        Ras = driver.calcRas();
        Rae = driver.calcRae();
        
        Cab = box.Vb / (env.AirDensity * env.SpeedOfSound * env.SpeedOfSound);
        Rab = 1 / (2 * Math.PI * box.Fb * box.Qa * Cab);
        Ral = box.Ql / (2 * Math.PI * box.Fb * Cab);
        Map = 1 / (Math.pow(2 * Math.PI * box.Fb, 2) * Cab);
        Rap = 1 / (2 * Math.PI * box.Fb * Qp * Cab);
    }
    
    private Complex Zat(double f)
    {
        f *= 2 * Math.PI;
        Complex Zas = new Complex(Ras, f * Mas - 1 / (f * Cas));
        Complex Zab = Fnc.zParallel(new Complex(Rab, -1 / (f * Cab)), new Complex(Ral));
        Complex Zap = new Complex(Rap, f * Map);
        return Zas.add(Fnc.zParallel(Zab, Zap));
    }
    
    private Complex cone(double f)
    {
        return new Complex(0, 2 * Math.PI * f * Mas).divide(Zat(f).add(Rae));
    }
    
    private Complex port(double f)
    {
        f *= 2 * Math.PI;
        Complex Zas = new Complex(Ras, f * Mas - 1 / (f * Cas));
        Complex Zab = Fnc.zParallel(new Complex(Rab, -1 / (f * Cab)), new Complex(Ral));
        Complex Zap = new Complex(Rap, f * Map);
        Complex Zat = Zas.add(Fnc.zParallel(Zab, Zap));
        return new Complex(0, f * Mas).divide(Zat.add(Rae)).multiply(Fnc.zParallel(Zab, Zap)).divide(Zap);
    }
    
    private Complex box(double f)
    {
        return cone(f).subtract(port(f));
    }
    
    @Override
    public Complex response(double f)
    {
        Complex c = cone(f).multiply(distanceCone.response(f));
        Complex p = port(f).multiply(distancePort.response(f));
        
        return driver.normResponse(f).multiply(c.subtract(p));
    }
    
    @Override
    public Complex response1W(double f)
    {
        Complex c = cone(f).multiply(distanceCone.response(f));
        Complex p = port(f).multiply(distancePort.response(f));
        
        return driver.normResponse1W(f).multiply(c.subtract(p));
    }
    
    @Override
    public Complex listeningWindowResponse(double f)
    {
        Complex c = cone(f).multiply(listeningWindowCone.response(f));
        Complex p = port(f).multiply(listeningWindowPort.response(f));
        
        return driver.normResponse(f).multiply(c.subtract(p));
    }
    
    @Override
    public Complex powerResponse(double f)
    {
        Complex c = cone(f).multiply(powerResponseCone.response(f));
        Complex p = port(f).multiply(powerResponsePort.response(f));
        
        return driver.normResponse(f).multiply(c.subtract(p));
    }
    
    @Override
    public Complex responseWithBaffle(double f)
    {
        Complex c = cone(f).multiply(baffle.response(f).multiply(distanceCone.response(f)));
        Complex p = port(f).divide(2).multiply(distancePort.response(f));
        
        return driver.normResponse(f).multiply(c.subtract(p));
    }
    
    @Override
    public Complex responseWithRoom(double f)
    {
        Complex c = cone(f).multiply(roomCone.response(f).multiply(distanceCone.response(f)));
        Complex p = port(f).multiply(roomPort.response(f).multiply(distancePort.response(f)));
        
        return driver.normResponse(f).multiply(c.subtract(p));
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
    
    private double Sdp(double Dv, double Np)
    {
        Dv /= 2;
        return Dv * Dv * Math.PI * Np;
    }
    
    public double portVelocity(double f, double Dv, double Np)
    {
        double fn = f / driver.Fs;
        return driver.Sd / Sdp(Dv, Np) * driver.voltage(maxPower(f)) * driver.Bl / (driver.Re * driver.Mms * Math.pow(2 * Math.PI * driver.Fs, 2) * fn * fn) * port(f).abs() * 2 * Math.PI * f;
    }
    
    /*
    Flanged End: 0.425
    Free End: 0.307
    
    if both ends are flanged, k = 0.425 + 0.425 = 0.850
    if one flanged, one free, k = 0.425 + 0.307 = 0.732
    if both ends are free, k = 0.307 + 0.307 = 0.614
    */
    public double Lv(double Dv, double Np, double k)
    {
        return Math.max(2356.25 * Dv * Dv * Np / (box.Fb * box.Fb * box.Vb) - k * Dv, 0);
    }
    
    public static double calcSlotDv(double width, double height)
    {
        return 2 * Math.sqrt(width * height / Math.PI);
    }
    
    public static double calcPortVolume(double Lv, double Dv, double Np)
    {
        return Math.PI * Math.pow(Dv / 2, 2) * Lv * Np;
    }
    
    public double dBmag(double f)
    {
        return Fnc.toDecibels(box(f).abs());
    }
    
    public double dBmagCone(double f)
    {
        return Fnc.toDecibels(cone(f).abs());
    }
    
    public double dBmagPort(double f)
    {
        return Fnc.toDecibels(port(f).abs());
    }
}
