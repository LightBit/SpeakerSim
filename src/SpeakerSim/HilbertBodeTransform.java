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

public class HilbertBodeTransform
{
    private final double[] t1;
    private final double[] t2;
    private final static int SIZE = 100;

    private HilbertBodeTransform()
    {
        t1 = new double[SIZE * 2 + 3];
        t2 = new double[t1.length];
        double prev = 0;
        
        t1[0] = 8 * Math.pow((-SIZE - 1) / (double) SIZE, 3);
        t2[0] = 0;
        
        for (int i = 1; i < t1.length - 1; i++)
        {
            double t = t1[i] = 8 * Math.pow((i - SIZE - 1) / (double) SIZE, 3);
            
            if (t == 0)
            {
                t = Double.MIN_NORMAL;
            }
            
            t = Math.abs(t) / 2;
            t = 2 * Math.log(Math.cosh(t) / Math.sinh(t)) / (Math.PI * Math.PI);
            
            t2[i] = t2[i - 1];
            
            if (i != 1 && i != t1.length - 2)
            {
                t2[i] += (t + prev) / 2 * (t1[i] - t1[i - 1]);
            }
            
            prev = t;
        }
        
        t1[t1.length - 1] = 8 * Math.pow((t1.length - SIZE - 2) / (double) SIZE, 3);
        t2[t2.length - 1] = 1;
    }

    private double interpolate(double x)
    {
        for (int i = x > 0 ? SIZE : 1; i < t1.length; i++)
        {
            if (x >= t1[i - 1] && x <= t1[i])
            {
                return Fnc.interpolate(t1[i - 1], t2[i - 1], t1[i], t2[i], x);
            }
        }
        
        return 0;
    }
    
    private static HilbertBodeTransform inst;
    
    public static double transform(double x)
    {
        if (inst == null)
        {
            inst = new HilbertBodeTransform();
        }
        
        return inst.interpolate(x);
    }
}
