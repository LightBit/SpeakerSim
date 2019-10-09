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

public class AperiodicSimulation extends BassReflexSimulation
{
    public static BassReflex convert(Aperiodic box, Driver driver)
    {
        BassReflex x = new BassReflex();
        
        x.Vb = box.Vb;
        x.Ql = box.Ql;
        x.Qa = box.Qa;
        x.PortShape = box.VentShape;
        x.Dv = box.Dv;
        x.Wv = box.Wv;
        x.Hv = box.Hv;
        x.Np = box.Np;
        x.Thickness = box.Thickness;
        x.Ends = box.Ends;
        x.PortPosition = box.VentPosition;
        
        x.Fb = ClosedBoxSimulation.calcFb(ClosedBoxSimulation.calcQtc(x.Vb, driver.Vas, driver.Qts, x.Qa), driver.Qts, driver.Fs);
        x.Qp = 0.5;
        
        return x;
    }
    
    public static void calcBox(Aperiodic box, Driver driver)
    {
        box.Qa = 10;
        box.Ql = 7;
        box.Vb = ClosedBoxSimulation.calcVb(0.707 + 1 / box.Qa, driver.Qts, driver.Vas);
    }
    
    public AperiodicSimulation(Environment env, Aperiodic box, Driver driver, Baffle baffle, Position driverPos, Position centerPos, Position listeningPos)
    {
        super(env, convert(box, driver), driver, baffle, driverPos, centerPos, listeningPos);
    }
    
    public double ventVelocity(double f, double Dv, double Np)
    {
        return portVelocity(f, Dv, Np);
    }
}
