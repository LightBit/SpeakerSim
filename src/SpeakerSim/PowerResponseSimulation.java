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
    private final PositionSimulation[] s;
    
    public PowerResponseSimulation(Baffle baffle, ISource source, Position sourcePos, Position centerPos, Environment env, boolean dipole)
    {
        s = new PositionSimulation[70];
        
        for (int i = 0; i < 35; i++)
        {
            Position pos;
            int angle = i * 10;
            
            pos = centerPos.moveHorizontally(2, angle);
            s[i] = new PositionSimulation(env, source, baffle, sourcePos, pos, dipole);
            
            pos = centerPos.moveVertically(2, angle);
            s[35 + i] = new PositionSimulation(env, source, baffle, sourcePos, pos, dipole);
        }
    }
    
    public Complex response(double f)
    {
        Complex sum = new Complex();
        
        for (PositionSimulation item : s)
        {
            sum = sum.add(item.response(f));
        }
        
        return sum.divide(35);
    }
}
