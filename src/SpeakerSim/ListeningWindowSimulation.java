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

public class ListeningWindowSimulation
{
    private final BaffleSimulation[] bs;
    private final DistanceSimulation[] distance;
    
    public ListeningWindowSimulation(Baffle baffle, Driver driver, Position sourcePos, Position centerPos, Environment env, boolean dipole)
    {
        bs = new BaffleSimulation[9];
        distance = new DistanceSimulation[9];
        Position pos;
        
        pos = centerPos.moveVertically(2, 0);
        bs[0] = new BaffleSimulation(baffle, driver, sourcePos, pos, env, dipole);
        distance[0] = new DistanceSimulation(sourcePos, pos, env);
        
        pos = centerPos.moveVertically(2, 10);
        bs[1] = new BaffleSimulation(baffle, driver, sourcePos, pos, env, dipole);
        distance[1] = new DistanceSimulation(sourcePos, pos, env);
        
        pos = centerPos.moveVertically(2, -10);
        bs[2] = new BaffleSimulation(baffle, driver, sourcePos, pos, env, dipole);
        distance[2] = new DistanceSimulation(sourcePos, pos, env);
        
        pos = centerPos.moveHorizontally(2, 10);
        bs[3] = new BaffleSimulation(baffle, driver, sourcePos, pos, env, dipole);
        distance[3] = new DistanceSimulation(sourcePos, pos, env);
        
        pos = centerPos.moveHorizontally(2, -10);
        bs[4] = new BaffleSimulation(baffle, driver, sourcePos, pos, env, dipole);
        distance[4] = new DistanceSimulation(sourcePos, pos, env);
        
        pos = centerPos.moveHorizontally(2, 20);
        bs[5] = new BaffleSimulation(baffle, driver, sourcePos, pos, env, dipole);
        distance[5] = new DistanceSimulation(sourcePos, pos, env);
        
        pos = centerPos.moveHorizontally(2, -20);
        bs[6] = new BaffleSimulation(baffle, driver, sourcePos, pos, env, dipole);
        distance[6] = new DistanceSimulation(sourcePos, pos, env);
        
        pos = centerPos.moveHorizontally(2, 30);
        bs[7] = new BaffleSimulation(baffle, driver, sourcePos, pos, env, dipole);
        distance[7] = new DistanceSimulation(sourcePos, pos, env);
        
        pos = centerPos.moveHorizontally(2, -30);
        bs[8] = new BaffleSimulation(baffle, driver, sourcePos, pos, env, dipole);
        distance[8] = new DistanceSimulation(sourcePos, pos, env);
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
