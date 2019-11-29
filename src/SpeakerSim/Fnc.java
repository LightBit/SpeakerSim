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

import java.text.NumberFormat;
import java.text.ParseException;

public final class Fnc
{
    private final static NumberFormat DECIMAL_FORMAT = NumberFormat.getNumberInstance();
    private final static NumberFormat DECIMAL_FORMAT2 = NumberFormat.getNumberInstance();
    
    private Fnc()
    {
        
    }
    
    static
    {
        DECIMAL_FORMAT.setMaximumFractionDigits(Integer.MAX_VALUE);
        DECIMAL_FORMAT.setGroupingUsed(false);
        
        DECIMAL_FORMAT2.setMaximumFractionDigits(2);
        DECIMAL_FORMAT2.setGroupingUsed(false);
    }

    public static String twoDecimalFormat(double x)
    {
        return DECIMAL_FORMAT2.format(x);
    }
    
    public static String decimalFormat(double x)
    {
        return DECIMAL_FORMAT.format(x);
    }
    
    public static Number parseNumber(String x) throws ParseException
    {
        return DECIMAL_FORMAT.parse(x);
    }
    
    public static double parseDouble(String value)
    {
        int dot = value.indexOf('.');
        int comma = value.indexOf(',');
        
        if (dot < comma) // 1.234.567,89
        {
            value = value.replace(".", "").replace(',', '.');
        }
        else if (comma != -1) // 1,234,567.89
        {
            value = value.replace(",", "");
        }
        
        return Double.parseDouble(value);
    }
    
    public static double factorial(double x)
    {
        if (x <= 0)
        {
            return 1;
        }

        double result = x;
        for (int i = 1; i < x; i++)
        {
            result *= i;
        }

        return result;
    }
    
    public static double log2(double x)
    {
        return Math.log(x) / Math.log(2);
    }
    
    public static double interpolate(double Xa, double Ya, double Xb, double Yb, double x)
    {
        return Ya + (x - Xa) * ((Yb - Ya) / (Xb - Xa));
    }
    
    public static double toDecibels(double x)
    {
        return Math.log10(x) * 20;
    }
    
    public static double powerToDecibels(double x)
    {
        return Math.log10(x) * 10;
    }
    
    public static double toAmplitude(double x)
    {
        return Math.pow(10, x / 20);
    }
    
    public static Complex zL(double L, double f)
    {
        return new Complex(0, 2 * Math.PI * f * L);
    }
    
    public static Complex zC(double C, double f)
    {
        if (C > 0)
        {
            return new Complex(0, -1 / (2 * Math.PI * f * C));
        }
        
        return new Complex();
    }
    
    public static Complex zR(double R)
    {
        return new Complex(R);
    }
    
    public static Complex zParallel(Complex ... x)
    {
        return Complex.multiplyAll(x).divide(Complex.addAll(x));
    }
    
    public static Complex zSerial(Complex ... x)
    {
        return Complex.addAll(x);
    }
    
    public static double to360Degress(double x)
    {
        return x < 0 ? x + 360 : x;
    }
    
    public static double to180Degress(double x)
    {
        return x > 180 ? x - 360 : x;
    }
    
    public static double[] averageSmooth(double[] values, int points)
    {
        double[] r = new double[values.length];
        int half = points / 2;
        
        for (int i = 0; i < values.length; i++)
        {
            int start = i - Math.min(half, i);
            if (values.length - start < points)
            {
                start = values.length - points;
            }
            
            double sum = 0;
            
            for (int p = 0; p < points; p++)
            {
                sum += values[start + p];
            }
            
            r[i] = sum / points;
        }
        
        return r;
    }
    
    public static double[] smooth(double[] values, int points)
    {
        double[] r = averageSmooth(values, points);
        r = averageSmooth(r, points);
        return averageSmooth(r, points);
    }
    
    public static double besselJn(int n, double x)
    {
        if (n == 0)
        {
            if (x > 10)
            {
                return Math.sqrt(2 / (Math.PI * x))
                        * (1 - 1 / (16 * x * x) + 53 / (512 * Math.pow(x, 4)))
                        * Math.cos(x - 0.78539816339744828 - 1 / (8 * x) + 25
                        / (384 * Math.pow(x, 3)));
            }
        }
        else if (n == 1)
        {
            if (x > 10)
            {
                return Math.sqrt(2 / (Math.PI * x))
                        * (1 + 3 / (16 * x * x) + 99 / (512 * Math.pow(x, 4)))
                        * Math.cos(x - 2.3561944901923448 + 3 / (8 * x) - 21
                        / (128 * Math.pow(x, 3)));
            }
        }
        else if (x > 40)
        {
            return 0;
        }
        
        double j = 0;
        
        x /= 2;
        
        for (int i = 0; i < 55; i++)
        {
            j += Math.pow(-1, i) * Math.pow(x, 2 * i + n) / (factorial(i) * factorial(i + n));
        }
        
        return j;
    }
    
    public static double besselH1(double x)
    {
        return 0.63661977236758138 - besselJn(0, x) + 0.092958178940651059 * (Math.sin(x) / x) + 0.54084409738353578 * ((1 - Math.cos(x)) / (x * x));
    }
    
    public static double besselJ1(double x)
    {
        double ax = Math.abs(x);
 
        if (ax < 8)
        {
            double y = x * x;
             
            x = x * (72362614232.0 + y * (-7895059235.0 + y * (242396853.1 + y * (-2972611.439 + y * (15704.4826 + y * -30.16036606)))));
            y = 144725228442.0 + y * (2300535178.0 + y * (18583304.74 + y * (99447.43394 + y * (376.9991397 + y))));
             
            return x / y;
        }
        else
        {
            double z = 8 / ax;
            double y = z * z;
            double xx = ax - 2.356194491;
 
            x = 1 + y * (0.183105e-2 + y * (-0.3516396496e-4 + y * (0.2457520174e-5 + y * -0.240337019e-6)));
            y = 0.04687499995 + y * (-0.2002690873e-3 + y * (0.8449199096e-5 + y * (-0.88228987e-6 + y * 0.105787412e-6)));
             
            return Math.abs(Math.sqrt(0.636619772 / ax) * (Math.cos(xx) * x - z * Math.sin(xx) * y));
        }
    }

    public static double sinc(double x)
    {
        return x == 0 ? 1 : Math.sin(x) / x;
    }
    
    public static double min(double[] x)
    {
        double min = Double.MAX_VALUE;
        
        for (int i = 0; i < x.length; i++)
        {
            if (Double.isFinite(x[i]) && x[i] < min)
            {
                min = x[i];
            }
        }
        
        return min;
    }
    
    /*public static double calcSlopeLo(double[] frequency, double[] amplitude)
    {
        for (int i = 1; i < amplitude.length; i++)
        {
            double octave = log2(frequency[i] / frequency[0]);
            if (octave >= 0.5)
            {
                return (amplitude[i] - amplitude[0]) / octave;
            }
        }

        return 0;
    }

    public static double calcSlopeHi(double[] frequency, double[] amplitude)
    {
        for (int i = amplitude.length - 1; i > 1; i--)
        {
            double octave = log2(frequency[amplitude.length - 1] / frequency[i]);
            if (octave >= 0.5)
            {
                return (amplitude[amplitude.length - 1] - amplitude[i]) / octave;
            }
        }

        return 0;
    }*/
    
    public static double calcSlopeLo(double[] frequency, double[] amplitude)
    {
        double octave = 0;
        double slope = 0;
       
        for (int i = 1; i < amplitude.length; i++)
        {
            double f = log2(frequency[i] / frequency[i - 1]);
            slope += (amplitude[i] - amplitude[i - 1]) / f;
            
            octave += f;
            if (octave >= 0.5)
            {
                return slope / i;
            }
        }

        return slope / amplitude.length;
    }

    public static double calcSlopeHi(double[] frequency, double[] amplitude)
    {
        double octave = 0;
        double slope = 0;
       
        for (int i = amplitude.length - 1; i > 1; i--)
        {
            double f = log2(frequency[i] / frequency[i - 1]);
            slope += (amplitude[i] - amplitude[i - 1]) / f;
            
            octave += f;
            if (octave >= 0.5)
            {
                return slope / (amplitude.length - i);
            }
        }

        return slope / amplitude.length;
    }

    public static double[] calcPhase(double[] frequency, double[] amplitude, double slopeLo, double slopeHi)
    {
        double lo = Math.log(2 * Math.PI * frequency[0]);
        double hi = Math.log(2 * Math.PI * frequency[frequency.length - 1]);
        
        slopeLo /= 6;
        slopeHi /= 6;
        
        double[] phase = new double[frequency.length];
        
        for (int i = 0; i < frequency.length; i++)
        {
            double prev_a = Math.log10(amplitude[0]) * Math.log(10);
            double prev_f = Math.log(frequency[0] / frequency[i]);
            double sum = 0;
            
            for (int j = 1; j < frequency.length; j++)
            {
                double a = Math.log10(amplitude[j]) * Math.log(10);
                double f = Math.log(frequency[j] / frequency[i]);
                
                double t = Math.exp(Math.abs((prev_f + f) / 2));
                t = 2 * Math.log((t + 1) / (t - 1)) / (Math.PI * Math.PI);
                sum += (a - prev_a) / Math.log(frequency[j] / frequency[j - 1]) * t * (f - prev_f);
                
                prev_a = a;
                prev_f = f;
            }
            
            double x = Math.log(2 * Math.PI * frequency[i]);
            x = Math.PI * (sum + slopeLo * HilbertBodeTransform.transform(lo - x) + slopeHi * (1 - HilbertBodeTransform.transform(hi - x))) / 2;
            phase[i] = Math.atan2(Math.sin(x), Math.cos(x));
        }
        
        return phase;
    }
    
    /*public static double[] unwrapPhase(double[] f, double[] phase)
    {
        double[] unwrapped = new double[phase.length];
        double prev = 0;
        double x = 0;
        
        for (int i = 0; i < f.length; i++)
        {
            double p = phase[i];
            
            if (i > 0)
            {
                if (prev < -Math.PI / 2 && p > Math.PI / 2)
                {
                    x -= 2 * Math.PI;
                }
                else if (prev > Math.PI / 2 && p < -Math.PI / 2)
                {
                    x += 2 * Math.PI;
                }
                
                unwrapped[i] = p + x;
            }
            
            prev = p;
        }
        
        return unwrapped;
    }*/
}
