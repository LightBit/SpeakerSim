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

public class DistanceSimulation
{
    private final double amplitude;
    private final double phase;
    
    public DistanceSimulation(double distance, double speedOfSound)
    {
        if (Math.abs(distance) < 0.0000001)
        {
            distance = 0.0000001;
        }
        
        amplitude = 1 / distance;
        phase = -distance / speedOfSound * 2 * Math.PI;
    }
    
    public DistanceSimulation(double distance, Environment env)
    {
        this(distance, env.SpeedOfSound);
    }
    
    public DistanceSimulation(double distance)
    {
        this(distance, Environment.getInstance());
    }
    
    public DistanceSimulation(Position sourcePos, Position listeningPos, double speedOfSound)
    {
        this(sourcePos.distance(listeningPos), speedOfSound);
    }
    
    public DistanceSimulation(Position sourcePos, Position listeningPos, Environment env)
    {
        this(sourcePos.distance(listeningPos), env.SpeedOfSound);
    }
    
    public Complex response(double f)
    {
        return Fnc.toComplex(amplitude, f * phase);
    }
}
