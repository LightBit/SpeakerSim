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

public class HighPassFilter extends PassFilter
{
    private final static double[][] C =
    {
        {0.1590, 0.0000}, // First order Butterworth
        {0.1125, 0.0000}, // Second order Butterworth
        {0.0912, 0.0000}, // Second order Bessel
        {0.0796, 0.0000}, // Second order Linkwitz-Riley
        {0.1592, 0.0000}, // Second order Chebychev
        {0.1061, 0.3183}, // Third order Butterworth
        {0.1040, 0.1470}, // Fourth order Butterworth
        {0.0702, 0.0719}, // Fourth order Bessel
        {0.0844, 0.1688}, // Fourth order Linkwitz-Riley
        {0.1104, 0.1246}, // Fourth order Legendre
        {0.0767, 0.1491}, // Fourth order Gaussian
        {0.0741, 0.1524}  // Fourth order Linear-Phase
    };
    
    private final static double[][] L =
    {
        {0.0000, 0.0000}, // First order Butterworth
        {0.2251, 0.0000}, // Second order Butterworth
        {0.2756, 0.0000}, // Second order Bessel
        {0.3183, 0.0000}, // Second order Linkwitz-Riley
        {0.1592, 0.0000}, // Second order Chebychev
        {0.1194, 0.0000}, // Third order Butterworth
        {0.1009, 0.4159}, // Fourth order Butterworth
        {0.0862, 0.4983}, // Fourth order Bessel
        {0.1000, 0.4501}, // Fourth order Linkwitz-Riley
        {0.1073, 0.2783}, // Fourth order Legendre
        {0.1116, 0.3251}, // Fourth order Gaussian
        {0.1079, 0.3853}  // Fourth order Linear-Phase
    };
    
    @Override
    public double calcC1(int type, double f)
    {
        return calcC(C[type][0], super.itemImpedance(f).abs(), f);
    }
    
    @Override
    public double calcC2(int type, double f)
    {
        return calcC(C[type][1], super.itemImpedance(f).abs(), f);
    }
    
    @Override
    public double calcL1(int type, double f)
    {
        return calcL(L[type][0], super.itemImpedance(f).abs(), f);
    }
    
    @Override
    public double calcL2(int type, double f)
    {
        return calcL(L[type][1], super.itemImpedance(f).abs(), f);
    }
    
    public HighPassFilter()
    {
        super();
    }
    
    public HighPassFilter(JsonValue json)
    {
        super(json);
    }
    
    @Override
    public Complex thisFilter(double f)
    {
        Complex r = new Complex(1);
        
        Complex z = super.itemImpedance(f);
        
        if (L2 > 0)
        {
            Complex x = zL2(f);
            z = z.multiply(x).divide(z.add(x));
        }
        
        if (C2 > 0)
        {
            Complex x = z.add(zC2(f));
            r = r.multiply(z.divide(x));
            z = x;
        }
        
        if (L1 > 0)
        {
            Complex x = zL1(f);
            z = z.multiply(x).divide(z.add(x));
        }
        
        z = z.divide(z.add(zC1(f)));
        
        return r.multiply(z);
    }
    
    @Override
    protected Complex itemImpedance(double f)
    {
        Complex z = super.itemImpedance(f);

        if (L2 > 0)
        {
            Complex x = zL2(f);
            z = z.multiply(x).divide(z.add(x));
        }

        if (C2 > 0)
        {
            z = z.add(zC2(f));
        }

        if (L1 > 0)
        {
            Complex x = zL1(f);
            z = z.multiply(x).divide(z.add(x));
        }

        z = z.add(zC1(f));
        
        return z;
    }
    
    @Override
    public String name()
    {
        return "High pass filter";
    }
}
