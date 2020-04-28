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
    private final double horizontalAngle;
    private final double verticalAngle;
    
    private final double Cas;
    private final double Mas;
    private final double Ras;
    private final double Rae;
    private final double Cab;
    private final double Rab;
    private final double Ral;
    private final double Map;
    private final double Rap;
    
    private class Port implements ISource
    {
        private final double Dv;
        
        public Port(double Dv)
        {
            this.Dv = Dv;
        }
        
        @Override
        public Complex relativeOffAxis(double f, double horizontalAngle, double verticalAngle, boolean dipole)
        {
            double x = Math.PI * f / Environment.getInstance().SpeedOfSound;
            double angle = Math.max(Math.abs(horizontalAngle), Math.abs(verticalAngle));
            
            x *= Dv;
            
            if (angle > 90)
            {
                x *= 1 + Math.sin(Math.toRadians(angle - 90));
            }
            else
            {
                x *= Math.sin(Math.toRadians(angle));
            }
            
            return new Complex(x > 0 ? Math.abs(2 * Fnc.besselJ1(x) / x) : 1);
        }
    }
    
    public BassReflexSimulation(Environment env, BassReflex box, Driver driver, Baffle baffle, Position driverPos, Position centerPos, Position listeningPos)
    {
        this.box = box;
        this.driver = driver;
        Port port = new Port(box.Dv);
        this.baffle = new BaffleSimulation(baffle, driver, driverPos, listeningPos, env, false);
        this.distanceCone = new DistanceSimulation(driverPos, listeningPos, env);
        this.distancePort = new DistanceSimulation(box.PortPosition.distance(listeningPos), env);
        this.roomCone = new RoomSimulation(baffle, driver, driverPos, listeningPos, env, false);
        this.roomPort = new RoomSimulation(null, port, box.PortPosition, listeningPos, env, false);
        this.powerResponseCone = new PowerResponseSimulation(baffle, driver, driverPos, centerPos, env, false);
        this.powerResponsePort = new PowerResponseSimulation(baffle, port, box.PortPosition, centerPos, env, false);
        this.listeningWindowCone = new ListeningWindowSimulation(baffle, driver, driverPos, centerPos, env, false);
        this.listeningWindowPort = new ListeningWindowSimulation(baffle, port, box.PortPosition, centerPos, env, false);
        horizontalAngle = driverPos.horizontalAngle(listeningPos);
        verticalAngle = driverPos.verticalAngle(listeningPos);
        
        double Qp = box.Qp + 0.00000000000001;
        
        Cas = driver.Cas();
        Mas = driver.Mas();
        Ras = driver.Ras();
        Rae = driver.Rae();
        
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
        
        return driver.normResponse(f, horizontalAngle, verticalAngle, false).multiply(c.subtract(p));
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
        
        return driver.normResponse(f, horizontalAngle, verticalAngle, false).multiply(c.subtract(p));
    }
    
    @Override
    public Complex responseWithRoom(double f)
    {
        Complex c = cone(f).multiply(roomCone.response(f).multiply(distanceCone.response(f)));
        Complex p = port(f).multiply(roomPort.response(f).multiply(distancePort.response(f)));
        
        return driver.normResponse(f, horizontalAngle, verticalAngle, false).multiply(c.subtract(p));
    }
    
    @Override
    public double excursion(double f, double Pe)
    {
        double Sd = driver.effectiveSd();
        double maxVad = driver.voltage(Math.min(Pe, driver.PeRMS())) * driver.effectiveBl() / (driver.effectiveRe() * Sd);
        return new Complex(0, maxVad).divide(Zat(f).add(Rae)).abs() / (2 * Math.PI * f * Sd) * 1000;
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
        double Bl = driver.effectiveBl();
        double Sd = driver.effectiveSd();
        Complex Zeb = new Complex(Bl * Bl / (Sd * Sd)).divide(Zat(f));
        return new Complex(0, driver.LeZ(f)).add(Zeb.add(driver.effectiveRe()));
    }
    
    private double Sdp(double Dv, double Np)
    {
        Dv /= 2;
        return Dv * Dv * Math.PI * Np;
    }
    
    public double portVelocity(double f, double Dv, double Np)
    {
        double fn = f / driver.Fs;
        return driver.effectiveSd() / Sdp(Dv, Np) * driver.voltage(maxPower(f)) * driver.effectiveBl() / (driver.effectiveRe() * driver.effectiveMms() * Math.pow(2 * Math.PI * driver.Fs, 2) * fn * fn) * port(f).abs() * 2 * Math.PI * f;
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
    
    private final static double[] BB4_A =
    {
        5.8980, 5.3339, 4.8457, 4.4204, 4.0478, 3.7114, 3.4286, 3.1699,
        2.9388, 2.7315, 2.5448, 2.3761, 2.2233, 2.0843, 1.9576, 1.8419,
        1.7357, 1.6392, 1.5484, 1.4656, 1.3890, 1.3181, 1.2523, 1.1911,
        1.1341, 1.0809, 1.0313, 0.9849, 0.9414, 0.9006, 0.8622, 0.8262,
        0.7923, 0.7603, 0.7302, 0.7017, 0.6747, 0.6493, 0.6251, 0.6022,
        0.5805, 0.5599, 0.5403, 0.5216, 0.5038, 0.4869, 0.4708, 0.4554,
        0.4407, 0.4267, 0.4133
    };
    
    public static void calcBB4(BassReflex box, Driver driver)
    {
        if (driver.Qts < 0.2 || driver.Qts > 0.7)
        {
            throw new HandledException("SBB4/BB4 alignment is not possible for this driver!");
        }
        
        box.Ql = 7;
        box.Qa = 100;
        box.Qp = 100;
        box.Vb = driver.effectiveVas() / BB4_A[(int)Math.round(driver.Qts * 100) - 20];
        box.Fb = driver.Fs;
    }
    
    private final static double[] QB3_A =
    {
        34.3925, 28.2341, 23.5499, 19.9046, 17.0150, 14.6784, 12.7685, 11.1855,
        9.8589, 8.7361, 7.7775, 6.9524, 6.2372, 5.6132, 5.0655, 4.5822,
        4.1535, 3.7714, 3.4295, 3.1223, 2.8421, 2.5944, 2.3667, 2.1594,
        1.9699, 1.7964, 1.6371, 1.4905, 1.3552, 1.2300, 1.1141, 1.0065,
        0.9064, 0.8131, 0.7260, 0.6445, 0.5682, 0.4966, 0.4294, 0.3661,
        0.3065, 0.2503, 0.1971, 0.1468, 0.0992, 0.0540, 0.0111
    };
    
    private final static double[] QB3_H =
    {
        3.8416, 3.4947, 3.2058, 2.9615, 2.7525, 2.5712, 2.4129, 2.2743,
        2.1495, 2.0388, 1.9393, 1.8494, 1.7678, 1.6935, 1.6254, 1.5629,
        1.5054, 1.4522, 1.4029, 1.3571, 1.3145, 1.2748, 1.2376, 1.2028,
        1.1702, 1.1395, 1.1106, 1.0834, 1.0578, 1.0335, 1.0106, 0.9889,
        0.9683, 0.9488, 0.9303, 0.9128, 0.8961, 0.8802, 0.8651, 0.8507,
        0.8370, 0.8240, 0.8116, 0.7998, 0.7886, 0.7779, 0.7677
    };
    
    public static void calcQB3(BassReflex box, Driver driver)
    {
        if (driver.Qts < 0.1 || driver.Qts > 0.56)
        {
            throw new HandledException("QB3/SQB3 alignment is not possible for this driver!");
        }
        
        box.Ql = 7;
        box.Qa = 100;
        box.Qp = 100;
        int i = (int)Math.round(driver.Qts * 100) - 10;
        box.Vb = driver.effectiveVas() / QB3_A[i];
        box.Fb = driver.Fs * QB3_H[i];
    }
    
    private final static double[] C4_A =
    {
        3.8961, 3.6755, 3.45512, 3.2360, 3.0193, 2.8062, 2.5977, 2.3952,
        2.1997, 2.0125, 1.8347, 1.6672, 1.5109, 1.3665, 1.2343, 1.1146,
        1.0070, 0.9113, 0.8266, 0.7521, 0.6868, 0.6297, 0.5798, 0.5361,
        0.4978, 0.4642, 0.4345, 0.4083, 0.3849, 0.3640, 0.3453, 0.3284,
        0.3131, 0.2992, 0.2865, 0.2749, 0.2641, 0.2542, 0.2449, 0.2363,
        0.2283, 0.2208, 0.2136, 0.2069, 0.2006, 0.1946
    };
    
    private final static double[] C4_H =
    {
        1.0338, 1.0534, 1.0703, 1.0842, 1.0951, 1.1028, 1.1073, 1.1086,
        1.1065, 1.1012, 1.0926, 1.0810, 1.0667, 1.0498, 1.0309, 1.0103,
        0.9886, 0.9662, 0.9436, 0.9212, 0.8992, 0.8780, 0.8578, 0.8385,
        0.8203, 0.8031, 0.7870, 0.7719, 0.7578, 0.7445, 0.7321, 0.7205,
        0.7096, 0.6993, 0.6896, 0.6805, 0.6719, 0.6638, 0.6561, 0.6488,
        0.6418, 0.6353, 0.6289, 0.6229, 0.6171, 0.6116
    };
    
    public static void calcC4(BassReflex box, Driver driver)
    {
        if (driver.Qts < 0.25 || driver.Qts > 0.7)
        {
            throw new HandledException("SC4/C4 alignment is not possible for this driver!");
        }
        
        box.Ql = 7;
        box.Qa = 100;
        box.Qp = 100;
        int i = (int)Math.round(driver.Qts * 100) - 25;
        box.Vb = driver.effectiveVas() / C4_A[i];
        box.Fb = driver.Fs * C4_H[i];
    }
    
    public static void calcBox(BassReflex box, Driver driver)
    {
        if (driver.Qts < 0.1 || driver.Qts > 0.7)
        {
            box.Ql = 7;
            box.Qa = 100;
            box.Qp = 100;
            box.Vb = 20 * Math.pow(driver.Qts, 3.3) * driver.effectiveVas();
            box.Fb = Math.pow(driver.effectiveVas() / box.Vb, 0.31) * driver.Fs;
        }
        else if (driver.Qts < 0.2)
        {
            calcQB3(box, driver);
        }
        else if (driver.Qts > 0.4)
        {
            calcC4(box, driver);
        }
        else
        {
            calcBB4(box, driver);
        }
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
