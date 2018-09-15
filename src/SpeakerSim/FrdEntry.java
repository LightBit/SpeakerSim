/*
 * Copyright (C) 2018 Gregor Pintar <grpintar@gmail.com>
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package SpeakerSim;

import java.util.Locale;

public class FrdEntry implements Comparable
{
    public double frequency;
    public double amplitude;
    public double phase;

    public FrdEntry(double frequency, double amplitude, double phase)
    {
        this.frequency = frequency;
        this.amplitude = amplitude;
        this.phase = phase;
    }
    
    public FrdEntry(String line)
    {
        String[] entry = line.split("\\s+");
        
        if (entry.length < 2)
        {
            throw new HandledException("FRD or ZMA file is invalid!");
        }

        frequency = Fnc.parseDouble(entry[0]);
        amplitude = Fnc.parseDouble(entry[1]);
        phase = entry.length >= 3 ? Fnc.parseDouble(entry[2]) : 0;
    }
    
    @Override
    public int compareTo(Object o)
    {
        if (frequency < ((FrdEntry)o).frequency)
        {
            return -1;
        }
        else if (frequency > ((FrdEntry)o).frequency)
        {
            return 1;
        }
        return 0;
    }

    @Override
    public String toString()
    {
        return String.format(Locale.US, "%s\t%s\t%s", frequency, amplitude, phase);
    }
}
