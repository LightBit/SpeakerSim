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

public class PowerResponseSimulation
{
    private final BaffleSimulation[] bs;
    private final DistanceSimulation[] distance;
    
    public PowerResponseSimulation(Baffle baffle, Driver driver, Position sourcePos, Position centerPos, Environment env, boolean dipole)
    {
        bs = new BaffleSimulation[70];
        distance = new DistanceSimulation[70];
        
        for (int i = 0; i < 35; i++)
        {
            Position pos;
            int angle = i * 10;
            
            pos = centerPos.move(2, angle, 0);
            bs[i] = new BaffleSimulation(baffle, driver, sourcePos, pos, env, dipole);
            distance[i] = new DistanceSimulation(sourcePos, pos, env);
            
            pos = centerPos.move(2, 0, angle);
            bs[35 + i] = new BaffleSimulation(baffle, driver, sourcePos, pos, env, dipole);
            distance[35 + i] = new DistanceSimulation(sourcePos, pos, env);
        }
    }
    
    public Complex response(double f)
    {
        Complex sum = new Complex();
        
        for (int i = 0; i < bs.length; i++)
        {
            sum = sum.add(bs[i].response(f).multiply(distance[i].response(f)));
        }
        
        return sum.divide(bs.length / 2);
    }
}
