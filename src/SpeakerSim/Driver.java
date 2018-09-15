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

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class Driver implements JSONable
{
    public enum Shape
    {
        Circular,
        Rectangular
    }
    
    public String Name;
    public double Fs; // Resonance frequency (Hz)
    public double Vas; // Vas (cubic metres)
    public double Qes; // Electrical Q at resonance
    public double Qms; // Mechanical Q at resonance
    public double Qts; // Q at resonance
    public double Re; // DC resistance of the voice coil, measured in ohms.
    public double Bl; // The product of magnet field strength in the voice coil gap and the length of wire in the magnetic field, in tesla-metres (Tm).
    public double Le; // Voice coil inductance measured in henries (H) (measured at 1 kHz).
    public double Xmax; // Peak linear displacement of cone (mm)
    public Shape shape; // Membrane shape
    public double Dia; // Effective diameter (membrane + 1/3 surround) (m)
    public double Width; // Effective width (membrane + 1/3 surround) (m)
    public double Height; // Effective width (membrane + 1/3 surround) (m)
    public double Sd; // Projected area of the driver diaphragm, in square metres.
    public double Vd; // Peak displacement volume
    public double Cms; // Compliance of the driver's suspension, in metres per newton (the reciprocal of its 'stiffness'). (m/N)
    public double Mms; // Mass of the diaphragm/coil, including acoustic load, in kilograms. (kg) Mass of the diaphragm and coil alone is known as Mmd
    public double Rms; // The mechanical resistance of a driver's suspension (i.e., 'lossiness') in Ns/m (= kg/s)
    public double n0; // Reference efficiency
    public double SPL_1W; // Reference sound presure level at 1W/1m (dB)
    public double SPL_2_83V; // Reference sound presure level at 2.83V/1m (dB)
    public double Pe; // Maximum input power for driver (W)
    public double PeF; // High pass filter frequency (12dB/octave)
    public PowerFilter PowerFilter;
    public boolean Closed; // closed back (tweeter)
    public boolean Inverted; // if polarity is inverted
    public double CrossStart;
    public double CrossEnd;
    
    private FrdEntry[] FRD;
    private FrdEntry[] ZMA;
    
    public Driver()
    {
        Name = "";
        shape = Shape.Circular;
        PowerFilter = new PowerFilter();
        CrossStart = 100;
        CrossEnd = 500;
    }
    
    public Driver(JsonValue json)
    {
        fromJSON(json);
    }
    
    public Driver(File file) throws IOException
    {
        fromJSON(JSON.open(file));
    }
    
    public Driver(InputStream stream) throws IOException
    {
        fromJSON(JSON.open(stream));
    }
    
    public static void copy(Driver src, Driver dst)
    {
        dst.Name = src.Name;
        dst.Vas = src.Vas;
        dst.Fs = src.Fs;
        dst.Qes = src.Qes;
        dst.Qms = src.Qms;
        dst.Qts = src.Qts;
        dst.Re = src.Re;
        dst.Bl = src.Bl;
        dst.Le = src.Le;
        dst.Xmax = src.Xmax;
        dst.shape = src.shape;
        dst.Dia = src.Dia;
        dst.Width = src.Width;
        dst.Height = src.Height;
        dst.Sd = src.Sd;
        dst.Vd = src.Vd;
        dst.Cms = src.Cms;
        dst.Mms = src.Mms;
        dst.Rms = src.Rms;
        dst.n0 = src.n0;
        dst.SPL_1W = src.SPL_1W;
        dst.SPL_2_83V = src.SPL_2_83V;
        dst.Pe = src.Pe;
        dst.PeF = src.PeF;
        dst.PowerFilter.setType(src.PowerFilter.getType());
        dst.CrossStart = src.CrossStart;
        dst.CrossEnd = src.CrossEnd;
        dst.Closed = src.Closed;
        dst.Inverted = src.Inverted;
        dst.FRD = src.FRD;
        dst.ZMA = src.ZMA;
    }
    
    public Driver copy()
    {
        Driver drv = new Driver();
        copy(this, drv);
        return drv;
    }
    
    private static FrdEntry[] importTable(File file, boolean dB) throws IOException
    {
        List<FrdEntry> frd = new ArrayList<FrdEntry>();
        boolean hasPhase = false;
        
        try (BufferedReader r = new BufferedReader(new FileReader(file)))
        {
            String line;
            
            while ((line = r.readLine()) != null)
            {
                line = line.trim();
                
                if (!line.isEmpty() && Character.isDigit(line.charAt(0)))
                {
                    FrdEntry entry = new FrdEntry(line);
                    frd.add(entry);
                    
                    if (entry.phase != 0)
                    {
                        hasPhase = true;
                    }
                }
            }
            
            r.close();
        }
        
        if (frd.isEmpty())
        {
            return null;
        }
        
        // sort by frequency
        Collections.sort(frd);
        
        // average value of duplicate frequencies
        for (int i = 0; i < frd.size(); i++)
        {
            FrdEntry entry = frd.get(i);
            int next = i + 1;
            
            for (int dups = 1; next < frd.size(); dups++)
            {
                FrdEntry dup = frd.get(next);
                
                if (entry.compareTo(dup) == 0)
                {
                    entry.amplitude += dup.amplitude;
                    entry.phase += dup.phase;
                    frd.remove(next);
                }
                else
                {
                    if (dups > 1)
                    {
                        entry.amplitude /= dups;
                        entry.phase /= dups;
                        frd.set(i, entry);
                    }
                    break;
                }
            }
        }
        
        // no phase -> calculate phase from amplitude
        if (!hasPhase)
        {
            double[] frequency = new double[frd.size()];
            double[] amplitude = new double[frequency.length];
            
            int i = 0;
            for (FrdEntry entry : frd)
            {
                frequency[i] = entry.frequency;
                amplitude[i] = entry.amplitude;
                i++;
            }
            
            double slopeLo = Fnc.calcSlopeLo(frequency, amplitude);
            double slopeHi = Fnc.calcSlopeHi(frequency, amplitude);
            
            if (dB)
            {
                for (i = 0; i < amplitude.length; i++)
                {
                    amplitude[i] = Fnc.toAmplitude(amplitude[i]);
                }
            }
            
            double[] phase = Fnc.calcPhase(frequency, amplitude, slopeLo, slopeHi);
            
            FrdEntry[] array = new FrdEntry[frequency.length];
            i = 0;
            for (FrdEntry entry : frd)
            {
                entry.phase = Math.toDegrees(phase[i]);
                array[i] = entry;
                i++;
            }
            
            return array;
        }
        
        FrdEntry[] array = new FrdEntry[frd.size()];
        for (int i = 0; i < array.length; i++)
        {
            array[i] = frd.get(i);
        }
        
        return array;
    }
    
    private static void exportTable(FrdEntry[] frd, File file) throws FileNotFoundException, UnsupportedEncodingException
    {
        try (PrintWriter writer = new PrintWriter(file, "UTF-8"))
        {
            for (FrdEntry entry : frd)
            {
                writer.print(entry.toString() + "\r\n");
            }

            writer.close();
        }
    }
    
    public boolean hasFRD()
    {
        return FRD != null;
    }
    
    public boolean hasZMA()
    {
        return ZMA != null;
    }
    
    public void importFRD(File file) throws IOException
    {
        FRD = importTable(file, true);
    }
    
    public void importZMA(File file) throws IOException
    {
        ZMA = importTable(file, false);
    }
    
    public void exportFRD(File file) throws FileNotFoundException, UnsupportedEncodingException
    {
        exportTable(FRD, file);
    }
    
    public void exportZMA(File file) throws FileNotFoundException, UnsupportedEncodingException
    {
        exportTable(ZMA, file);
    }
    
    public void removeFRD()
    {
        FRD = null;
    }
    
    public void removeZMA()
    {
        ZMA = null;
    }
    
    public static Complex normalize(double startF, double endF, double f, Complex a, Complex b)
    {
        if (f < startF)
        {
            return b;
        }
        else if (f < endF)
        {
            double x = (f - startF) / endF;
            return a.multiply(x).add(b.multiply(1 - x));
        }
        
        return a;
    }
    
    private Complex normalize(double f, Complex a, Complex b)
    {
        if (!Closed)
        {
            return normalize(CrossStart, CrossEnd, f, a, b);
        }
        
        return a;
    }
    
    public static double normalize(double startF, double endF, double f, double a, double b)
    {
        if (f < startF)
        {
            return b;
        }
        else if (f < endF)
        {
            double x = (f - startF) / endF;
            return a * x + b * (1 - x);
        }
        
        return a;
    }
    
    private double normalize(double f, double a, double b)
    {
        if (!Closed)
        {
            return normalize(CrossStart, CrossEnd, f, a, b);
        }
        
        return a;
    }
    
    public double PeRMS()
    {
        return PowerFilter.toRMS(Pe, PeF);
    }
    
    public double voltage(double Pe)
    {
        return Math.sqrt(Pe * Re) * Math.sqrt(2);
    }
    
    public Complex responseSimRelative(double f)
    {
        double Cas = calcCas();
        double Mas = calcMas();
        double Ras = calcRas();
        double Rae = calcRae();
        
        f *= 2 * Math.PI;
        Complex Zas = new Complex(Ras, f * Mas - 1 / (f * Cas));
        return new Complex(0, f * Mas).divide(Zas.add(Rae));
    }
    
    public Complex responseSim(double f)
    {
        return responseSimRelative(f).multiply(Fnc.toAmplitude(SPL_2_83V));
    }
    
    public Complex response(double f)
    {
        if (FRD != null)
        {
            FrdEntry prev = FRD[0];
            
            // no lower value
            if (prev.frequency > f)
            {
                return Fnc.toComplex(Double.MIN_NORMAL, 0); // TODO: use predicted slope
            }

            for (FrdEntry entry : FRD)
            {
                if (entry.frequency >= f)
                {
                    double amplitude = Fnc.interpolate(prev.frequency, prev.amplitude, entry.frequency, entry.amplitude, f) + (SPL_2_83V - SPL_1W); // TODO: support 2.83V
                    double phase = Fnc.interpolate(prev.frequency, prev.phase, entry.frequency, entry.phase, f);
                    return Fnc.toComplex(Fnc.toAmplitude(amplitude), Math.toRadians(phase));
                }
                
                prev = entry;
            }
            
            // no higher value
            return Fnc.toComplex(Double.MIN_NORMAL, 0); // TODO: use predicted slope
        }
        else
        {
            return responseSim(f);
        }
    }
    
    public Complex normResponse(double f)
    {
        if (FRD != null)
        {
            Complex x = Fnc.toComplex(Fnc.toAmplitude(SPL_2_83V), response(CrossEnd).phase());
            return normalize(f, response(f), x);
        }
        
        return new Complex(Fnc.toAmplitude(SPL_2_83V));
    }
    
    public Complex response1W(double f)
    {
        return response(f).divide(Fnc.toAmplitude(SPL_2_83V - SPL_1W));
    }
    
    public Complex normResponse1W(double f)
    {
        if (FRD != null)
        {
            Complex x = Fnc.toComplex(Fnc.toAmplitude(SPL_1W), response1W(CrossEnd).phase());
            return normalize(f, response1W(f), x);
        }
        
        return new Complex(Fnc.toAmplitude(SPL_1W));
    }
    
    private double directivity(double angle, boolean dipole)
    {
        if (angle > 90 && !dipole)
        {
            return 1 + Math.sin(Math.toRadians(angle - 90));
        }
        else
        {
            return Math.sin(Math.toRadians(angle));
        }
    }
    
    private double offAxisSim(double f, double horizontalAngle, double verticalAngle, boolean dipole)
    {
        double x = Math.PI * f / Project.getInstance().Environment.SpeedOfSound;
        horizontalAngle = Math.abs(horizontalAngle);
        verticalAngle = Math.abs(verticalAngle);
        
        if (shape == Shape.Circular)
        {
            x *= Dia * directivity(Math.max(horizontalAngle, verticalAngle), dipole);
            return x > 0 ? Math.abs(2 * Fnc.besselJ1(x) / x) : 1;
        }
        else
        {
            double w = x * Width * directivity(horizontalAngle, dipole);
            double h = x * Height * directivity(verticalAngle, dipole);
            return Math.abs(Fnc.sinc(w) * Fnc.sinc(h));
        }
    }
    
    public double relativeOffAxis(double f, double horizontalAngle, double verticalAngle, boolean dipole)
    {
        return offAxisSim(f, horizontalAngle, verticalAngle, dipole); // TODO: from FRD
    }
    
    public double excursion(double f, double Pe)
    {
        double maxVad = voltage(Math.min(Pe, PeRMS())) * Bl / (Re * Sd);
        double Cas = calcCas();
        double Mas = calcMas();
        double Ras = calcRas();
        double Rae = calcRae();
        
        f *= 2 * Math.PI;
        Complex Zas = new Complex(Ras, f * Mas - 1 / (f * Cas));
        return new Complex(0, maxVad).divide(Zas.add(Rae)).abs() / (f * Sd) * 1000;
    }
    
    private static double LeZ(double Le, double f)
    {
        return Math.pow(12 * Math.PI * Le * f, 0.56);
    }
    
    private Complex impedanceSim(double f)
    {
        double Cas = calcCas();
        double Mas = calcMas();
        double Ras = calcRas();
        
        double x = 2 * Math.PI * f;
        Complex Zas = new Complex(Ras, x * Mas - 1 / (x * Cas));
        Complex Zes = new Complex(Bl * Bl / (Sd * Sd)).divide(Zas);
        return new Complex(0, LeZ(f)).add(Zes.add(Re));
    }
    
    public Complex impedance(double f)
    {
        if (ZMA != null)
        {
            FrdEntry prev = ZMA[0];
            
            // no lower value
            if (prev.frequency > f)
            {
                return Fnc.toComplex(prev.amplitude, Math.toRadians(prev.phase)); // TODO: use predicted slope
            }

            for (FrdEntry entry : ZMA)
            {
                if (entry.frequency >= f)
                {
                    double amplitude = Fnc.interpolate(prev.frequency, prev.amplitude, entry.frequency, entry.amplitude, f);
                    double phase = Fnc.interpolate(prev.frequency, prev.phase, entry.frequency, entry.phase, f);
                    return Fnc.toComplex(amplitude, Math.toRadians(phase));
                }
                
                prev = entry;
            }
            
            // no higher value
            return Fnc.toComplex(prev.amplitude, Math.toRadians(prev.phase)); // TODO: use predicted slope
        }
        else
        {
            return impedanceSim(f);
        }
    }
    
    public double LeZ(double f)
    {
        double z = LeZ(Le, f);
        
        if (ZMA != null)
        {
            z = normalize(f, impedance(f).abs() - Re, z);
        }
        
        return z;
    }
    
    public Complex normImpedance(double f)
    {
        Complex z = Fnc.toComplex(Re, LeZ(Le, f));
        
        if (ZMA != null)
        {
            z = normalize(f, impedance(f), z);
        }
        
        return z;
    }
    
    @Override
    public JsonValue toJSON()
    {
        JsonObject json = Json.object();
        
        json.add("Name", Json.value(Name));
        json.add("Fs", Json.value(Fs));
        json.add("Vas", Json.value(Vas));
        json.add("Qes", Json.value(Qes));
        json.add("Qms", Json.value(Qms));
        json.add("Qts", Json.value(Qts));
        json.add("Re", Json.value(Re));
        json.add("Bl", Json.value(Bl));
        json.add("Le", Json.value(Le));
        json.add("Xmax", Json.value(Xmax));
        json.add("Shape", Json.value(shape.toString()));
        json.add("Dia", Json.value(Dia));
        json.add("Width", Json.value(Width));
        json.add("Height", Json.value(Height));
        json.add("Sd", Json.value(Sd));
        json.add("Vd", Json.value(Vd));
        json.add("Cms", Json.value(Cms));
        json.add("Mms", Json.value(Mms));
        json.add("Rms", Json.value(Rms));
        json.add("n0", Json.value(n0));
        json.add("SPL", Json.value(SPL_1W));
        json.add("SPL_2_83V", Json.value(SPL_2_83V));
        json.add("Pe", Json.value(Pe));
        json.add("PeF", Json.value(PeF));
        json.add("PowerFilter", PowerFilter.toJSON());
        json.add("NormStartF", Json.value(CrossStart));
        json.add("NormEndF", Json.value(CrossEnd));
        json.add("Closed", Json.value(Closed));
        json.add("Inverted", Json.value(Inverted));
        
        if (FRD != null)
        {
            JsonArray array = Json.array().asArray();
            
            for (FrdEntry entry : FRD)
            {
                array.add(Json.array(entry.frequency, entry.amplitude, entry.phase));
            }
            
            json.add("FRD", array);
        }
        
        if (ZMA != null)
        {
            JsonArray array = Json.array().asArray();
            
            for (FrdEntry entry : ZMA)
            {
                array.add(Json.array(entry.frequency, entry.amplitude, entry.phase));
            }
            
            json.add("ZMA", array);
        }
        
        return json;
    }
    
    private void fromJSON(JsonValue json)
    {
        JsonObject jsonObj = json.asObject();
        
        Name = jsonObj.get("Name").asString();
        Fs = jsonObj.get("Fs").asDouble();
        Vas = jsonObj.get("Vas").asDouble();
        Qes = jsonObj.get("Qes").asDouble();
        Qms = jsonObj.get("Qms").asDouble();
        Qts = jsonObj.get("Qts").asDouble();
        Re = jsonObj.get("Re").asDouble();
        Bl = jsonObj.get("Bl").asDouble();
        Le = jsonObj.get("Le").asDouble();
        Xmax = jsonObj.get("Xmax").asDouble();
        
        JsonValue x = jsonObj.get("Shape");
        shape = x != null ? Shape.valueOf(x.asString()) : Shape.Circular;
        
        Dia = JSON.getDouble(jsonObj, "Dia");
        Width = JSON.getDouble(jsonObj, "Width");
        Height = JSON.getDouble(jsonObj, "Height");
        Sd = jsonObj.get("Sd").asDouble();
        Vd = jsonObj.get("Vd").asDouble();
        Cms = jsonObj.get("Cms").asDouble();
        Mms = jsonObj.get("Mms").asDouble();
        Rms = jsonObj.get("Rms").asDouble();
        n0 = jsonObj.get("n0").asDouble();
        SPL_1W = jsonObj.get("SPL").asDouble();
        SPL_2_83V = JSON.getDouble(jsonObj, "SPL_2_83V", calcSPL_2_83V(SPL_1W, Re));
        Pe = jsonObj.get("Pe").asDouble();
        PeF = JSON.getDouble(jsonObj, "PeF");
        PowerFilter = new PowerFilter(jsonObj.get("PowerFilter"));
        CrossStart = jsonObj.get("NormStartF").asDouble();
        CrossEnd = jsonObj.get("NormEndF").asDouble();
        Closed = jsonObj.get("Closed").asBoolean();
        Inverted = jsonObj.get("Inverted").asBoolean();
        
        FRD = null;
        JsonValue frd = jsonObj.get("FRD");
        if (frd != null)
        {
            JsonArray array = frd.asArray();
            FRD = new FrdEntry[array.size()];
            
            int i = 0;
            for (JsonValue entry : array)
            {
                FRD[i++] = new FrdEntry(entry.asArray().get(0).asDouble(), entry.asArray().get(1).asDouble(), entry.asArray().get(2).asDouble());
            }
        }
        
        ZMA = null;
        JsonValue zma = jsonObj.get("ZMA");
        if (zma != null)
        {
            JsonArray array = zma.asArray();
            ZMA = new FrdEntry[array.size()];
            
            int i = 0;
            for (JsonValue entry : array)
            {
                ZMA[i++] = new FrdEntry(entry.asArray().get(0).asDouble(), entry.asArray().get(1).asDouble(), entry.asArray().get(2).asDouble());
            }
        }
    }
    
    public void save(File file) throws IOException
    {
        JSON.save(toJSON(), file);
    }
    
    public void open(File file) throws IOException
    {
        fromJSON(JSON.open(file));
    }
    
    public static double calcFs(double Cms, double Mms)
    {
        return 1 / (2 * Math.PI * Math.sqrt(Cms * Mms));
    }
    
    public static double calcVas(double Sd, double Cms, double AirDensity, double SpeedOfSound)
    {
        return AirDensity * SpeedOfSound * SpeedOfSound * Sd * Sd * Cms;
    }
    
    public static double calcQes(double Fs, double Mms, double Re, double Bl)
    {
        return (2 * Math.PI * Fs * Mms * Re) / (Bl * Bl);
    }
    
    public static double calcQms(double Fs, double Mms, double Rms)
    {
        return (2 * Math.PI * Fs * Mms) / Rms;
    }
    
    public static double calcQts(double Qes, double Qms)
    {
        return Qms * Qes / (Qms + Qes);
    }
    
    public static double calcBl(double Fs, double Qes, double Re, double Mms)
    {
        return Math.sqrt(Mms * Re * 2 * Math.PI * Fs / Qes);
    }
    
    public static double calcDia(double Sd)
    {
        return Math.sqrt(Sd / Math.PI) * 2;
    }
    
    public static double calcSd(double Dia)
    {
        double radius = Dia / 2;
        return Math.PI * radius * radius;
    }
    
    public static double calcSd(double Width, double Height)
    {
        return Width * Height;
    }
    
    public static double calcVd(double Sd, double Xmax)
    {
        return Sd * Xmax;
    }
    
    public static double calcCms(double Vas, double Sd, double AirDensity, double SpeedOfSound)
    {
        return Vas / (AirDensity * Math.pow(SpeedOfSound * Sd, 2));
    }
    
    public static double calcMms(double Fs, double Cms)
    {
        return Math.pow(1 / (2 * Math.PI * Fs), 2) / Cms;
    }
    
    public static double calcRms(double Mms, double Fs, double Qms)
    {
        return 2 * Math.PI * Fs * Mms / Qms;
    }
    
    public static double calcN0(double Bl, double Sd, double Mms, double Re, double AirDensity, double SpeedOfSound)
    {
        return (AirDensity * Bl * Bl * Sd * Sd) / (2 * Math.PI * SpeedOfSound * Mms * Mms * Re);
    }
        
    public static double calcSPL_1W(double n0)
    {
        return 112.1 + Math.log10(n0) * 10;
    }
    
    public static double calcSPL_2_83V(double SPL_1W, double Re)
    {
        return SPL_1W + Math.log10(8 / Re) * 10;
    }
    
    public double calcFs()
    {
        return calcFs(Cms, Mms);
    }
    
    public double calcVas(double AirDensity, double SpeedOfSound)
    {
        return calcVas(Sd, Cms, AirDensity, SpeedOfSound);
    }
    
    public double calcQes()
    {
        return calcQes(Fs, Mms, Re, Bl);
    }
    
    public double calcQms()
    {
        return calcQms(Fs, Mms, Rms);
    }
    
    public double calcQts()
    {
        return calcQts(Qes, Qms);
    }
    
    public double calcBl()
    {
        return calcBl(Fs, Qes, Re, Mms);
    }
    
    public double calcDia()
    {
        if (shape == Shape.Circular)
        {
            return calcDia(Sd);
        }
        else
        {
            return 0;
        }
    }
    
    public double calcSd()
    {
        if (shape == Shape.Circular)
        {
            return calcSd(Dia);
        }
        else
        {
            return calcSd(Width, Height);
        }
    }
    
    public double calcVd()
    {
        return calcVd(Sd, Xmax);
    }
    
    public double calcCms(double AirDensity, double SpeedOfSound)
    {
        return calcCms(Vas, Sd, AirDensity, SpeedOfSound);
    }
    
    public double calcMms()
    {
        return calcMms(Fs, Cms);
    }
    
    public double calcRms()
    {
        return calcRms(Mms, Fs, Qms);
    }
    
    public double calcN0(double AirDensity, double SpeedOfSound)
    {
        return calcN0(Bl, Sd, Mms, Re, AirDensity, SpeedOfSound);
    }
    
    public double calcSPL_1W()
    {
        return calcSPL_1W(n0);
    }
    
    public double calcSPL_2_83V()
    {
        return calcSPL_2_83V(SPL_1W, Re);
    }
    
    public double calcMmd(double AirDensity)
    {
        return Mms - 2 * (8.0 / 3.0) * Math.pow(Math.sqrt(Sd / Math.PI), 3) * AirDensity;
    }
    
    public double calcMes()
    {
        return Mms / (Bl * Bl);
    }
    
    public double calcCes()
    {
	return Cms * (Bl * Bl);
    }
    
    public double calcRes()
    {
        return Bl * Bl / Rms;
    }
    
    public double calcCas()
    {
        return Cms * (Sd * Sd);
    }
    
    public double calcMas()
    {
        return Mms / (Sd * Sd);
    }
    
    public double calcRas()
    {
        return Rms / (Sd * Sd);
    }
    
    public double calcRae()
    {
        return Bl * Bl / (Sd * Sd * Re);
    }
}
