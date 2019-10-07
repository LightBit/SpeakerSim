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

import com.eclipsesource.json.JsonValue;

public class LowPassFilter extends PassFilter
{
    private final static double[][] C =
    {
        {0.0000, 0.0000}, // First order Butterworth
        {0.1125, 0.0000}, // Second order Butterworth
        {0.0912, 0.0000}, // Second order Bessel
        {0.0796, 0.0000}, // Second order Linkwitz-Riley
        {0.1592, 0.0000}, // Second order Chebychev
        {0.2122, 0.0000}, // Third order Butterworth
        {0.2509, 0.0609}, // Fourth order Butterworth
        {0.2336, 0.0504}, // Fourth order Bessel
        {0.2533, 0.0563}, // Fourth order Linkwitz-Riley
        {0.2365, 0.0910}, // Fourth order Legendre
        {0.2235, 0.0768}, // Fourth order Gaussian
        {0.2255, 0.0632}  // Fourth order Linear-Phase
    };
    
    private final static double[][] L =
    {
        {0.1590, 0.0000}, // First order Butterworth
        {0.2251, 0.0000}, // Second order Butterworth
        {0.2756, 0.0000}, // Second order Bessel
        {0.3183, 0.0000}, // Second order Linkwitz-Riley
        {0.1592, 0.0000}, // Second order Chebychev
        {0.2387, 0.0796}, // Third order Butterworth
        {0.2437, 0.1723}, // Fourth order Butterworth
        {0.3583, 0.1463}, // Fourth order Bessel
        {0.3000, 0.1500}, // Fourth order Linkwitz-Riley
        {0.2294, 0.2034}, // Fourth order Legendre
        {0.3253, 0.1674}, // Fourth order Gaussian
        {0.3285, 0.1578}  // Fourth order Linear-Phase
    };
    
    @Override
    public double calcC1(int type, double f)
    {
        return calcC(C[type][0], super.impedance(f).abs(), f);
    }
    
    @Override
    public double calcC2(int type, double f)
    {
        return calcC(C[type][1], super.impedance(f).abs(), f);
    }
    
    @Override
    public double calcL1(int type, double f)
    {
        return calcL(L[type][0], super.impedance(f).abs(), f);
    }
    
    @Override
    public double calcL2(int type, double f)
    {
        return calcL(L[type][1], super.impedance(f).abs(), f);
    }
    
    public LowPassFilter()
    {
        super();
    }
    
    public LowPassFilter(JsonValue json)
    {
        super(json);
    }
    
    @Override
    public Complex thisFilter(double f)
    {
        Complex r = new Complex(1);
        
        Complex z = super.impedance(f);
        
        if (C2 > 0)
        {
            Complex x = zC2(f);
            z = z.multiply(x).divide(z.add(x));
        }
        
        if (L2 > 0)
        {
            Complex x = z.add(zL2(f));
            r = r.multiply(z.divide(x));
            z = x;
        }
        
        if (C1 > 0)
        {
            Complex x = zC1(f);
            z = z.multiply(x).divide(z.add(x));
        }
        
        z = z.divide(z.add(zL1(f)));
        
        return r.multiply(z);
    }
    
    @Override
    public Complex impedance(double f)
    {
        Complex z = super.impedance(f);

        if (C2 > 0)
        {
            Complex x = zC2(f);
            z = z.multiply(x).divide(z.add(x));
        }

        if (L2 > 0)
        {
            z = z.add(zL2(f));
        }

        if (C1 > 0)
        {
            Complex x = zC1(f);
            z = z.multiply(x).divide(z.add(x));
        }

        z = z.add(zL1(f));
        
        return z;
    }
    
    @Override
    public String name()
    {
        return "Low pass filter";
    }
}
