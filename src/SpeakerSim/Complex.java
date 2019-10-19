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

import java.util.Objects;

public class Complex
{
    private final double real;
    private final double imag;

    public Complex(double real, double imag)
    {
        this.real = real;
        this.imag = imag;
    }
    
    public Complex(double real)
    {
        this(real, 0);
    }
    
    public Complex()
    {
        this(0, 0);
    }
    
    public static Complex toComplex(double amplitude, double phase)
    {
        return new Complex(amplitude * Math.cos(phase), amplitude * Math.sin(phase));
    }

    public double abs()
    {
        return Math.hypot(real, imag);
    }

    public double phase()
    {
        return Math.atan2(imag, real);
    }

    public Complex add(Complex x)
    {
        return new Complex(real + x.real, imag + x.imag);
    }
    
    public Complex add(double x)
    {
        return new Complex(real + x, imag);
    }

    public Complex subtract(Complex x)
    {
        return new Complex(real - x.real, imag - x.imag);
    }
    
    public Complex subtract(double x)
    {
        return new Complex(real - x, imag);
    }
    
    public static Complex subtract(double a, Complex b)
    {
        return new Complex(a - b.real, b.imag);
    }

    public Complex multiply(Complex x)
    {
        return new Complex(real * x.real - imag * x.imag, real * x.imag + imag * x.real);
    }
    
    public Complex multiply(double x)
    {
        return new Complex(real * x, imag * x);
    }
    
    public Complex divide(Complex x)
    {
        double scale = x.real * x.real + x.imag * x.imag;
        return new Complex((real * x.real + imag * x.imag) / scale, (imag * x.real - real * x.imag) / scale);
    }
    
    public Complex divide(double x)
    {
        return new Complex(real * x / (x * x), imag * x / (x * x));
    }
    
    public static Complex divide(double a, Complex b)
    {
        double scale = b.real * b.real + b.imag * b.imag;
        return new Complex(a * b.real / scale, -a * b.imag / scale);
    }

    public Complex conjugate()
    {
        return new Complex(real, -imag);
    }

    public Complex reciprocal()
    {
        double scale = real * real + imag * imag;
        return new Complex(real / scale, -imag / scale);
    }

    public Complex exp()
    {
        return new Complex(Math.exp(real) * Math.cos(imag), Math.exp(real) * Math.sin(imag));
    }
    
    public Complex sqrt()
    {
        double r = Math.sqrt(abs());
        double theta = phase() / 2;
        return new Complex(r * Math.cos(theta), r * Math.sin(theta));
    }

    public Complex sin()
    {
        return new Complex(Math.sin(real) * Math.cosh(imag), Math.cos(real) * Math.sinh(imag));
    }

    public Complex cos()
    {
        return new Complex(Math.cos(real) * Math.cosh(imag), -Math.sin(real) * Math.sinh(imag));
    }

    public Complex tan()
    {
        return sin().divide(cos());
    }
    
    public boolean isZero()
    {
        return real == 0 && imag == 0;
    }
    
    @Override
    public String toString()
    {
        if (imag == 0) return Double.toString(real);
        if (real == 0) return imag + "i";
        if (imag <  0) return real + " - " + (-imag) + "i";
        return real + " + " + imag + "i";
    }
    
    @Override
    public boolean equals(Object x)
    {
        if (x == null) return false;
        if (getClass() != x.getClass()) return false;
        
        Complex that = (Complex) x;
        return (real == that.real) && (imag == that.imag);
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(real, imag);
    }
}
