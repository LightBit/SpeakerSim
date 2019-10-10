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
    private final PositionSimulation[] s;
    
    public ListeningWindowSimulation(Baffle baffle, ISource source, Position sourcePos, Position centerPos, Environment env, boolean dipole)
    {
        s = new PositionSimulation[9];
        Position pos;
        
        pos = centerPos.moveVertically(2, 0);
        s[0] = new PositionSimulation(env, source, baffle, sourcePos, pos, dipole);
        
        pos = centerPos.moveVertically(2, 10);
        s[1] = new PositionSimulation(env, source, baffle, sourcePos, pos, dipole);
        
        pos = centerPos.moveVertically(2, -10);
        s[2] = new PositionSimulation(env, source, baffle, sourcePos, pos, dipole);
        
        pos = centerPos.moveHorizontally(2, 10);
        s[3] = new PositionSimulation(env, source, baffle, sourcePos, pos, dipole);
        
        pos = centerPos.moveHorizontally(2, -10);
        s[4] = new PositionSimulation(env, source, baffle, sourcePos, pos, dipole);
        
        pos = centerPos.moveHorizontally(2, 20);
        s[5] = new PositionSimulation(env, source, baffle, sourcePos, pos, dipole);
        
        pos = centerPos.moveHorizontally(2, -20);
        s[6] = new PositionSimulation(env, source, baffle, sourcePos, pos, dipole);
        
        pos = centerPos.moveHorizontally(2, 30);
        s[7] = new PositionSimulation(env, source, baffle, sourcePos, pos, dipole);
        
        pos = centerPos.moveHorizontally(2, -30);
        s[8] = new PositionSimulation(env, source, baffle, sourcePos, pos, dipole);
    }
    
    public Complex response(double f)
    {
        Complex sum = new Complex();
        
        for (PositionSimulation item : s)
        {
            sum = sum.add(item.response(f));
        }
        
        return sum.divide(s.length / 2);
    }
}
