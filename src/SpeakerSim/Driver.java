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

import com.eclipsesource.json.*;
import org.ini4j.Ini;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Driver implements JSONable, ISource
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
    public double N0; // Reference efficiency
    public double SPL_1W; // Reference sound presure level at 1W/1m (dB)
    public double SPL_2_83V; // Reference sound presure level at 2.83V/1m (dB)
    public double Pe; // Maximum input power for driver (W)
    public double PeF; // High pass filter frequency (12dB/octave)
    public PowerFilter PowerFilter;
    public boolean Closed; // closed back (tweeter)
    public boolean Inverted; // if polarity is inverted
    public int Series;
    public int Parallel;
    public double CrossStart;
    public double CrossEnd;
    public ResponseData FRD;
    public ResponseData[] hFRD;
    public ResponseData[] vFRD;
    public ResponseEntry[] ZMA;
    
    public Driver()
    {
        Name = "";
        shape = Shape.Circular;
        PowerFilter = new PowerFilter();
        Series = 1;
        Parallel = 1;
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
        dst.N0 = src.N0;
        dst.SPL_1W = src.SPL_1W;
        dst.SPL_2_83V = src.SPL_2_83V;
        dst.Pe = src.Pe;
        dst.PeF = src.PeF;
        dst.PowerFilter.setType(src.PowerFilter.getType());
        dst.Closed = src.Closed;
        dst.Inverted = src.Inverted;
        dst.Series = src.Series;
        dst.Parallel = src.Parallel;
        dst.CrossStart = src.CrossStart;
        dst.CrossEnd = src.CrossEnd;
        dst.FRD = src.FRD;
        dst.hFRD = src.hFRD;
        dst.vFRD = src.vFRD;
        dst.ZMA = src.ZMA;
    }
    
    public double effectiveVas()
    {
        return Vas * Series * Parallel;
    }
    
    public double effectiveRe()
    {
        return Re * Series / Parallel;
    }
    
    public double effectiveBl()
    {
        return Bl * Series;
    }
    
    public double effectiveLe()
    {
        return Le * Series / Parallel;
    }
    
    public double effectiveDia()
    {
        return Dia * Series * Parallel;
    }
    
    public double effectiveWidth()
    {
        return Width * Series * Parallel;
    }
    
    public double effectiveHeight()
    {
        return Height * Series * Parallel;
    }
    
    public double effectiveSd()
    {
        return Sd * Series * Parallel;
    }
    
    public double effectiveVd()
    {
        return Vd * Series * Parallel;
    }
    
    public double effectiveCms()
    {
        return Cms / Series / Parallel;
    }
    
    public double effectiveMms()
    {
        return Mms * Series * Parallel;
    }
    
    public double effectiveRms()
    {
        return Rms * Series * Parallel;
    }
    
    public double effectiveN0()
    {
        return N0 * Series * Parallel;
    }
    
    public double effectiveSPL_1W()
    {
        return SPL_1W + 3 * (Series * Parallel - 1);
    }
    
    public double effectiveSPL_2_83V()
    {
        return SPL_2_83V + 6 * (Parallel - 1);
    }
    
    public double effectivePe()
    {
        return Pe * Series * Parallel;
    }
    
    public Driver copy()
    {
        Driver drv = new Driver();
        copy(this, drv);
        return drv;
    }
    
    public boolean hasFRD()
    {
        return FRD != null;
    }
    
    public boolean hasZMA()
    {
        return ZMA != null;
    }
    
    public void importZMA(File file) throws IOException
    {
        ZMA = ResponseData.ImportData(file, false);
    }
    
    public void exportZMA(File file) throws FileNotFoundException, UnsupportedEncodingException
    {
        ResponseData.ExportData(ZMA, file);
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
        return PowerFilter.toRMS(effectivePe(), PeF);
    }
    
    public double voltage(double Pe)
    {
        return Math.sqrt(Pe * effectiveRe()) * Math.sqrt(2);
    }
    
    public double SPLdiff()
    {
        return effectiveSPL_2_83V() - SPL_1W;
    }
    
    public Complex responseSimRelative(double f)
    {
        double Cas = Cas();
        double Mas = Mas();
        double Ras = Ras();
        double Rae = Rae();
        
        f *= 2 * Math.PI;
        Complex Zas = new Complex(Ras, f * Mas - 1 / (f * Cas));
        Complex x = new Complex(0, f * Mas).divide(Zas.add(Rae));
        
        if (Inverted)
        {
            double p = x.phase();
            
            if (p >= 0)
            {
                p -= Math.PI;
            }
            else
            {
                p += Math.PI;
            }

            x = Complex.toComplex(x.abs(), p);
        }
        
        return x;
    }
    
    public Complex responseSim(double f)
    {
        return responseSimRelative(f).multiply(Fnc.toAmplitude(effectiveSPL_2_83V()));
    }
    
    public Complex response(double f)
    {
        if (hasFRD())
        {
            return FRD.response(f, SPLdiff(), Inverted);
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
            Complex x = Complex.toComplex(Fnc.toAmplitude(effectiveSPL_2_83V()), response(CrossEnd).phase());
            return normalize(f, response(f), x);
        }
        
        return Complex.toComplex(Fnc.toAmplitude(effectiveSPL_2_83V()), Inverted ? -Math.PI : 0);
    }
    
    public Complex response1W(double f)
    {
        return response(f).divide(Fnc.toAmplitude(SPLdiff()));
    }
    
    public Complex normResponse1W(double f)
    {
        if (FRD != null)
        {
            Complex x = Complex.toComplex(Fnc.toAmplitude(effectiveSPL_1W()), response1W(CrossEnd).phase());
            return normalize(f, response1W(f), x);
        }
        
        return Complex.toComplex(Fnc.toAmplitude(effectiveSPL_1W()), Inverted ? -Math.PI : 0);
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
    
    private Complex relativeOffAxisSim(double f, double horizontalAngle, double verticalAngle, boolean dipole)
    {
        double x = Math.PI * f / Environment.getInstance().SpeedOfSound;
        horizontalAngle = Math.abs(horizontalAngle);
        verticalAngle = Math.abs(verticalAngle);
        
        if (shape == Shape.Circular)
        {
            x *= effectiveDia() * directivity(Math.max(horizontalAngle, verticalAngle), dipole);
            return new Complex(x > 0 ? Math.abs(2 * Fnc.besselJ1(x) / x) : 1);
        }
        else
        {
            double w = x * effectiveWidth() * directivity(horizontalAngle, dipole);
            double h = x * effectiveHeight() * directivity(verticalAngle, dipole);
            return new Complex(Math.abs(Fnc.sinc(w) * Fnc.sinc(h)));
        }
    }
    
    private Complex offAxisSim(double f, double horizontalAngle, double verticalAngle, boolean dipole)
    {
        return response(f).multiply(relativeOffAxisSim(f, horizontalAngle, verticalAngle, dipole));
    }
    
    private Complex horizontalAxis(double f, double horizontalAngle, boolean dipole)
    {
        double SPLdiff = SPLdiff();
        
        if (hFRD != null)
        {
            if (hFRD[0].horizontalAngle >= 0)
            {
                horizontalAngle = Math.abs(horizontalAngle);
            }
        }

        // no lower value
        if (hFRD[0].horizontalAngle > horizontalAngle)
        {
            return offAxisSim(f, horizontalAngle, 0, dipole);
        }

        for (int i = 1; i < hFRD.length; i++)
        {
            if (hFRD[i].horizontalAngle >= horizontalAngle)
            {
                return Complex.toComplex
                (
                    Fnc.interpolate
                    (
                            hFRD[i - 1].horizontalAngle,
                            hFRD[i - 1].response(f, SPLdiff, Inverted).abs(),
                            hFRD[i].horizontalAngle,
                            hFRD[i].response(f, SPLdiff, Inverted).abs(),
                            horizontalAngle
                    ),
                    Fnc.interpolate
                    (
                            hFRD[i - 1].horizontalAngle,
                            hFRD[i - 1].response(f, SPLdiff, Inverted).phase(),
                            hFRD[i].horizontalAngle,
                            hFRD[i].response(f, SPLdiff, Inverted).phase(),
                            horizontalAngle
                    )
                );
            }
        }

        // no higher value
        return offAxisSim(f, horizontalAngle, 0, dipole);
    }
    
    private Complex verticalAxis(double f, double verticalAngle, boolean dipole)
    {
        double SPLdiff = SPLdiff();
        
        if (vFRD != null)
        {
            if (vFRD[0].verticalAngle >= 0)
            {
                verticalAngle = Math.abs(verticalAngle);
            }
        }

        // no lower value
        if (vFRD[0].verticalAngle > verticalAngle)
        {
            return offAxisSim(f, verticalAngle, 0, dipole);
        }

        for (int i = 1; i < vFRD.length; i++)
        {
            if (vFRD[i].verticalAngle >= verticalAngle)
            {
                return Complex.toComplex
                (
                    Fnc.interpolate
                    (
                            vFRD[i - 1].verticalAngle,
                            vFRD[i - 1].response(f, SPLdiff, Inverted).abs(),
                            vFRD[i].verticalAngle,
                            vFRD[i].response(f, SPLdiff, Inverted).abs(),
                            verticalAngle
                    ),
                    Fnc.interpolate
                    (
                            vFRD[i - 1].verticalAngle,
                            vFRD[i - 1].response(f, SPLdiff, Inverted).phase(),
                            vFRD[i].verticalAngle,
                            vFRD[i].response(f, SPLdiff, Inverted).phase(),
                            verticalAngle
                    )
                );
            }
        }

        // no higher value
        return offAxisSim(f, verticalAngle, 0, dipole);
    }
    
    public Complex response(double f, double horizontalAngle, double verticalAngle, boolean dipole)
    {
        Complex x;
        
        if (hasFRD() && (hFRD != null || vFRD != null))
        {
            Complex h = horizontalAxis(f, horizontalAngle, dipole);
            Complex v = verticalAxis(f, verticalAngle, dipole);
            
            x = v.abs() < h.abs() ? v : h;
        }
        else
        {
            x = offAxisSim(f, horizontalAngle, verticalAngle, dipole);
        }
        
        // invert phase, if behind dipole
        if (dipole && Math.max(Math.abs(horizontalAngle), Math.abs(verticalAngle)) > 90)
        {
            double p = x.phase();
            
            if (p >= 0)
            {
                p -= Math.PI;
            }
            else
            {
                p += Math.PI;
            }

            x = Complex.toComplex(x.abs(), p);
        }
        
        return x;
    }
    
    public Complex normResponse(double f, double horizontalAngle, double verticalAngle, boolean dipole)
    {
        if (FRD != null)
        {
            Complex x = Complex.toComplex(Fnc.toAmplitude(effectiveSPL_2_83V()), response(CrossEnd).phase());
            return normalize(f, response(f, horizontalAngle, verticalAngle, dipole), x);
        }
        
        return Complex.toComplex(Fnc.toAmplitude(effectiveSPL_2_83V()), Inverted ? -Math.PI : 0);
    }
    
    @Override
    public Complex relativeOffAxis(double f, double horizontalAngle, double verticalAngle, boolean dipole)
    {
        return response(f, horizontalAngle, verticalAngle, dipole).divide(response(f));
    }
    
    public double excursion(double f, double Pe)
    {
        double maxVad = voltage(Math.min(Pe, PeRMS())) * effectiveBl() / (effectiveRe() * effectiveSd());
        double Cas = Cas();
        double Mas = Mas();
        double Ras = Ras();
        double Rae = Rae();
        
        f *= 2 * Math.PI;
        Complex Zas = new Complex(Ras, f * Mas - 1 / (f * Cas));
        return new Complex(0, maxVad).divide(Zas.add(Rae)).abs() / (f * effectiveSd()) * 1000;
    }
    
    private static double LeZ(double Le, double f)
    {
        return Math.pow(12 * Math.PI * Le * f, 0.56);
    }
    
    private Complex impedanceSim(double f)
    {
        double Cas = Cas();
        double Mas = Mas();
        double Ras = Ras();
        double bl = effectiveBl();
        double sd = effectiveSd();
        
        double x = 2 * Math.PI * f;
        Complex Zas = new Complex(Ras, x * Mas - 1 / (x * Cas));
        Complex Zes = new Complex(bl * bl / (sd * sd)).divide(Zas);
        return new Complex(0, LeZ(f)).add(Zes.add(effectiveRe()));
    }
    
    public Complex impedance(double f)
    {
        if (hasZMA())
        {
            ResponseEntry prev = ZMA[0];
            
            // no lower value
            if (prev.frequency > f)
            {
                return Complex.toComplex(prev.amplitude * Series / Parallel, Math.toRadians(prev.phase)); // TODO: use predicted slope
            }

            for (ResponseEntry entry : ZMA)
            {
                if (entry.frequency >= f)
                {
                    double amplitude = Fnc.interpolate(prev.frequency, prev.amplitude, entry.frequency, entry.amplitude, f);
                    double phase = Fnc.interpolate(prev.frequency, prev.phase, entry.frequency, entry.phase, f);
                    return Complex.toComplex(amplitude * Series / Parallel, Math.toRadians(phase));
                }
                
                prev = entry;
            }
            
            // no higher value
            return Complex.toComplex(prev.amplitude * Series / Parallel, Math.toRadians(prev.phase)); // TODO: use predicted slope
        }
        else
        {
            return impedanceSim(f);
        }
    }
    
    public double LeZ(double f)
    {
        double z = LeZ(effectiveLe(), f);
        
        if (ZMA != null)
        {
            z = normalize(f, impedance(f).abs() - effectiveRe(), z);
        }
        
        return z;
    }
    
    public Complex normImpedance(double f)
    {
        Complex z = Complex.toComplex(effectiveRe(), LeZ(effectiveLe(), f));
        
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
        json.add("n0", Json.value(N0));
        json.add("SPL", Json.value(SPL_1W));
        json.add("SPL_2_83V", Json.value(SPL_2_83V));
        json.add("Pe", Json.value(Pe));
        json.add("PeF", Json.value(PeF));
        json.add("PowerFilter", PowerFilter.toJSON());
        json.add("NormStartF", Json.value(CrossStart));
        json.add("NormEndF", Json.value(CrossEnd));
        json.add("Closed", Json.value(Closed));
        json.add("Inverted", Json.value(Inverted));
        json.add("Series", Json.value(Series));
        json.add("Parallel", Json.value(Parallel));
        
        if (FRD != null || hFRD != null || vFRD != null)
        {
            JsonArray array = Json.array().asArray();
            
            if (FRD != null)
            {
                array.add(FRD.toJSON());
            }
            
            if (hFRD != null)
            {
                for (ResponseData d : hFRD)
                {
                    if (d != FRD)
                    {
                        array.add(d.toJSON());
                    }
                }
            }
            
            if (vFRD != null)
            {
                for (ResponseData d : vFRD)
                {
                    if (d != FRD)
                    {
                        array.add(d.toJSON());
                    }
                }
            }
            
            json.add("FRDS", array);
        }
        
        if (ZMA != null)
        {
            JsonArray array = Json.array().asArray();
            
            for (ResponseEntry entry : ZMA)
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
        N0 = jsonObj.get("n0").asDouble();
        SPL_1W = jsonObj.get("SPL").asDouble();
        SPL_2_83V = JSON.getDouble(jsonObj, "SPL_2_83V", calcSPL_2_83V(SPL_1W, Re));
        Pe = jsonObj.get("Pe").asDouble();
        PeF = JSON.getDouble(jsonObj, "PeF");
        PowerFilter = new PowerFilter(jsonObj.get("PowerFilter"));
        CrossStart = jsonObj.get("NormStartF").asDouble();
        CrossEnd = jsonObj.get("NormEndF").asDouble();
        Closed = jsonObj.get("Closed").asBoolean();
        Inverted = jsonObj.get("Inverted").asBoolean();
        Series = JSON.getInt(jsonObj, "Series", 1);
        Parallel = JSON.getInt(jsonObj, "Parallel", 1);
        
        FRD = null;
        hFRD = null;
        vFRD = null;
        JsonValue frds = jsonObj.get("FRDS");
        if (frds != null)
        {
            JsonArray array = frds.asArray();
            List<ResponseData> hfrd = new ArrayList<ResponseData>();
            List<ResponseData> vfrd = new ArrayList<ResponseData>();

            for (JsonValue entry : array)
            {
                ResponseData rd = new ResponseData(entry);
                if (rd.horizontalAngle == 0 && rd.verticalAngle == 0)
                {
                    FRD = rd;
                    hfrd.add(rd);
                    vfrd.add(rd);
                }
                else if (rd.verticalAngle == 0)
                {
                    hfrd.add(rd);
                }
                else if (rd.horizontalAngle == 0)
                {
                    vfrd.add(rd);
                }
                else
                {
                    //TODO: throw exception
                }
            }

            if (hfrd.size() > 0)
            {
                Collections.sort(hfrd);
                hFRD = new ResponseData[hfrd.size()];
                hFRD = hfrd.toArray(hFRD);
            }

            if (vfrd.size() > 0)
            {
                Collections.sort(vfrd);
                vFRD = new ResponseData[vfrd.size()];
                vFRD = vfrd.toArray(vFRD);
            }
        }
        else
        {
            JsonValue frd = jsonObj.get("FRD");
            if (frd != null)
            {
                JsonArray array = frd.asArray();
                
                FRD = new ResponseData(null);
                FRD.data = new ResponseEntry[array.size()];

                int i = 0;
                for (JsonValue entry : array)
                {
                    FRD.data[i++] = new ResponseEntry(entry.asArray().get(0).asDouble(), entry.asArray().get(1).asDouble(), entry.asArray().get(2).asDouble());
                }
            }
        }
        
        ZMA = null;
        JsonValue zma = jsonObj.get("ZMA");
        if (zma != null)
        {
            JsonArray array = zma.asArray();
            ZMA = new ResponseEntry[array.size()];
            
            int i = 0;
            for (JsonValue entry : array)
            {
                ZMA[i++] = new ResponseEntry(entry.asArray().get(0).asDouble(), entry.asArray().get(1).asDouble(), entry.asArray().get(2).asDouble());
            }
        }
    }
    
    public void save(File file) throws IOException
    {
        JSON.save(toJSON(), file);
    }
    
    public static Driver importFromFile(File file) throws IOException
    {
        String name = file.getName();
        
        if (name.endsWith(".wdr")) // WinISD
        {
            Ini ini = new Ini(file);
            Ini.Section driver = ini.get("Driver");
            Driver drv = new Driver();
            
            drv.Name = driver.get("Brand") + " " + driver.get("Model");
            drv.Fs = Fnc.parseDouble(driver.get("Fs"));
            drv.Vas = Fnc.parseDouble(driver.get("Vas"));
            drv.Qes = Fnc.parseDouble(driver.get("Qes"));
            drv.Qms = Fnc.parseDouble(driver.get("Qms"));
            drv.Qts = Fnc.parseDouble(driver.get("Qts"));
            drv.Re = Fnc.parseDouble(driver.get("Re"));
            drv.Bl = Fnc.parseDouble(driver.get("BL"));
            drv.Le = Fnc.parseDouble(driver.get("Le"));
            drv.Xmax = Fnc.parseDouble(driver.get("Xmax"));
            drv.Dia = Fnc.parseDouble(driver.get("Dd"));
            drv.Sd = Fnc.parseDouble(driver.get("Sd"));
            drv.Vd = drv.calcVd();
            drv.Cms = Fnc.parseDouble(driver.get("Cms"));
            drv.Mms = Fnc.parseDouble(driver.get("Mms"));
            drv.Rms = Fnc.parseDouble(driver.get("Rms"));
            drv.N0 = Fnc.parseDouble(driver.get("no"));
            drv.SPL_1W = Fnc.parseDouble(driver.get("SPL"));
            drv.SPL_2_83V = Fnc.parseDouble(driver.get("USPL"));
            drv.Pe = Fnc.parseDouble(driver.get("Pe"));
            
            return drv;
        }
        else // native
        {
            return new Driver(file);
        }
    }
    
    public void open(File file) throws IOException
    {
        Driver drv = importFromFile(file);
        copy(drv, this);
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
        return 112.1 + Fnc.powerToDecibels(n0);
    }
    
    public static double calcSPL_2_83V(double SPL_1W, double Re)
    {
        return SPL_1W + Fnc.powerToDecibels(8 / Re);
    }
    
    public double calcFs()
    {
        return calcFs(Cms, Mms);
    }
    
    public double calcVas(double AirDensity, double SpeedOfSound)
    {
        return calcVas(Sd, Cms, AirDensity, SpeedOfSound);
    }
    
    public double calcVas()
    {
        Environment env = Environment.getInstance();
        return calcVas(env.AirDensity, env.SpeedOfSound);
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
    
    public double calcCms()
    {
        Environment env = Environment.getInstance();
        return calcCms(env.AirDensity, env.SpeedOfSound);
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
    
    public double calcN0()
    {
        Environment env = Environment.getInstance();
        return calcN0(env.AirDensity, env.SpeedOfSound);
    }
    
    public double calcSPL_1W()
    {
        return calcSPL_1W(N0);
    }
    
    public double calcSPL_2_83V()
    {
        return calcSPL_2_83V(SPL_1W, Re);
    }
    
    public double EBP()
    {
        return Fs / Qes;
    }
    
    public double Mmd(double AirDensity)
    {
        return effectiveMms() - 2 * (8.0 / 3.0) * Math.pow(Math.sqrt(effectiveSd() / Math.PI), 3) * AirDensity;
    }
    
    public double Mmd()
    {
        return Mmd(Environment.getInstance().AirDensity);
    }
    
    public double Mes()
    {
        double bl = effectiveBl();
        return effectiveMms() / (bl * bl);
    }
    
    public double Ces()
    {
        double bl = effectiveBl();
	return effectiveCms() * (bl * bl);
    }
    
    public double Res()
    {
        double bl = effectiveBl();
        return bl * bl / effectiveRms();
    }
    
    public double Cas()
    {
        double sd = effectiveSd();
        return effectiveCms() * (sd * sd);
    }
    
    public double Mas()
    {
        double sd = effectiveSd();
        return effectiveMms() / (sd * sd);
    }
    
    public double Ras()
    {
        double sd = effectiveSd();
        return effectiveRms() / (sd * sd);
    }
    
    public double Rae()
    {
        double bl = effectiveBl();
        double sd = effectiveSd();
        return bl * bl / (sd * sd * effectiveRe());
    }
}
