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

public class RoomSimulation
{
    private final double[] wall;
    private final BaffleSimulation[] wallSource;
    private final double speedOfSound;
    
    public RoomSimulation(final ISource source, final Position sourcePos, final Environment env, final Baffle baffle, boolean dipole)
    {
        // boundary reinforcement
        wall = new double[24];
        speedOfSound = env.SpeedOfSound;
        
        wall[0] = sourcePos.X;
        wall[1] = sourcePos.Y;
        wall[2] = sourcePos.Z;
        wall[3] = env.RoomX - sourcePos.X;
        wall[4] = env.RoomY - sourcePos.Y;
        wall[5] = env.RoomZ - sourcePos.Z;
        
        wall[6] = Math.hypot(sourcePos.X, sourcePos.Y);
        wall[7] = Math.hypot(sourcePos.X, sourcePos.Z);
        wall[8] = Math.hypot(sourcePos.X, wall[3]);
        wall[9] = Math.hypot(sourcePos.X, wall[4]);
        wall[10] = Math.hypot(sourcePos.X, wall[5]);
        wall[11] = Math.hypot(sourcePos.Z, sourcePos.Y);
        wall[12] = Math.hypot(sourcePos.Z, wall[3]);
        wall[13] = Math.hypot(sourcePos.Z, wall[4]);
        wall[14] = Math.hypot(sourcePos.Z, wall[5]);
        wall[15] = Math.hypot(sourcePos.Y, wall[3]);
        wall[16] = Math.hypot(sourcePos.Y, wall[4]);
        wall[17] = Math.hypot(sourcePos.Y, wall[5]);
        wall[18] = Math.hypot(wall[3], wall[4]);
        wall[19] = Math.hypot(wall[3], wall[5]);
        wall[20] = Math.hypot(wall[5], wall[4]);
        
        wall[21] = Math.hypot(sourcePos.X, wall[11]);
        wall[22] = Math.hypot(wall[3], wall[20]);
        
        wall[23] = Math.hypot(wall[21], wall[22]);
        
        for (int i = 0; i < wall.length; i++)
        {
            wall[i] *= 4 * Math.PI;
        }
        
        wallSource = new BaffleSimulation[6];
        wallSource[0] = new BaffleSimulation(baffle, source, sourcePos, sourcePos.addX(-sourcePos.X), env, dipole);
        wallSource[1] = new BaffleSimulation(baffle, source, sourcePos, sourcePos.addY(-sourcePos.Y), env, dipole);
        wallSource[2] = new BaffleSimulation(baffle, source, sourcePos, sourcePos.addZ(-sourcePos.Z), env, dipole);
        wallSource[3] = new BaffleSimulation(baffle, source, sourcePos, sourcePos.addX(env.RoomX - sourcePos.X), env, dipole);
        wallSource[4] = new BaffleSimulation(baffle, source, sourcePos, sourcePos.addY(env.RoomY - sourcePos.Y), env, dipole);
        wallSource[5] = new BaffleSimulation(baffle, source, sourcePos, sourcePos.addZ(env.RoomZ - sourcePos.Z), env, dipole);
    }
    
    public Complex response(double f)
    {
        Complex r = new Complex(1);
        
        for (int i = 0; i < wallSource.length; i++)
        {
            double x = wall[i] / (speedOfSound / f);
            r = r.add(wallSource[i].response(f).multiply(Math.sin(x) / x));
        }
        
        for (int i = wallSource.length - 1; i < wall.length; i++)
        {
            double x = wall[i] / (speedOfSound / f);
            r = r.add(Math.sin(x) / x);
        }
        
        return r;
    }
}
