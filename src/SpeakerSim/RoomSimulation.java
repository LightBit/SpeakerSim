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
    /*private final PositionSimulation[] s;
    private final DistanceSimulation[] reflection;
    
    private static double reflectionPoint(double a1, double a2, double b1, double b2)
    {
        return b1 * Math.abs(a1 - a2) / (b1 + b2) + Math.min(a1, a2);
    }*/
    
    public RoomSimulation(final Baffle baffle, final ISource source, final Position sourcePos, final Position listeningPos, final Environment env, boolean dipole)
    {
        // boundary reinforcement
        wall = new double[6];
        wallSource = new BaffleSimulation[6];
        
        double lambda = 4 * Math.PI / env.SpeedOfSound;
        wall[0] = lambda * sourcePos.X;
        wall[1] = lambda * sourcePos.Y;
        wall[2] = lambda * sourcePos.Z;
        wall[3] = lambda * (env.RoomX - sourcePos.X);
        wall[4] = lambda * (env.RoomY - sourcePos.Y);
        wall[5] = lambda * (env.RoomZ - sourcePos.Z);
        
        wallSource[0] = new BaffleSimulation(baffle, source, sourcePos, sourcePos.addX(-sourcePos.X), env, dipole);
        wallSource[1] = new BaffleSimulation(baffle, source, sourcePos, sourcePos.addY(-sourcePos.Y), env, dipole);
        wallSource[2] = new BaffleSimulation(baffle, source, sourcePos, sourcePos.addZ(-sourcePos.Z), env, dipole);
        wallSource[3] = new BaffleSimulation(baffle, source, sourcePos, sourcePos.addX(env.RoomX - sourcePos.X), env, dipole);
        wallSource[4] = new BaffleSimulation(baffle, source, sourcePos, sourcePos.addY(env.RoomY - sourcePos.Y), env, dipole);
        wallSource[5] = new BaffleSimulation(baffle, source, sourcePos, sourcePos.addZ(env.RoomZ - sourcePos.Z), env, dipole);
        
        // first reflections
        /*s = new PositionSimulation[6];
        reflection = new DistanceSimulation[6];
        Position pos;
        
        // X (front wall)
        pos = new Position
        (
            0,
            reflectionPoint(listeningPos.Y, sourcePos.Y, listeningPos.X, sourcePos.X),
            reflectionPoint(listeningPos.Z, sourcePos.Z, listeningPos.X, sourcePos.X)
        );
        s[0] = new PositionSimulation(env, source, baffle, sourcePos, pos, dipole);
        reflection[0] = new DistanceSimulation(pos.distance(listeningPos), env);
        
        // RoomX (back wall)
        pos = new Position
        (
            env.RoomX,
            reflectionPoint(listeningPos.Y, sourcePos.Y, env.RoomX - listeningPos.X, env.RoomX - sourcePos.X),
            reflectionPoint(listeningPos.Z, sourcePos.Z, env.RoomX - listeningPos.X, env.RoomX - sourcePos.X)
        );
        s[1] = new PositionSimulation(env, source, baffle, sourcePos, pos, dipole);
        reflection[1] = new DistanceSimulation(pos.distance(listeningPos), env);
        
        // Y (left wall)
        pos = new Position
        (
            reflectionPoint(listeningPos.X, sourcePos.X, listeningPos.Y, sourcePos.Y),
            0,
            reflectionPoint(listeningPos.Z, sourcePos.Z, listeningPos.Y, sourcePos.Y)
        );
        s[2] = new PositionSimulation(env, source, baffle, sourcePos, pos, dipole);
        reflection[2] = new DistanceSimulation(pos.distance(listeningPos), env);
        
        // RoomY (right wall)
        pos = new Position
        (
            reflectionPoint(listeningPos.X, sourcePos.X, env.RoomY - listeningPos.Y, env.RoomY - sourcePos.Y),
            env.RoomY,
            reflectionPoint(listeningPos.Z, sourcePos.Z, env.RoomY - listeningPos.Y, env.RoomY - sourcePos.Y)
        );
        s[3] = new PositionSimulation(env, source, baffle, sourcePos, pos, dipole);
        reflection[3] = new DistanceSimulation(pos.distance(listeningPos), env);
       
        // Z (floor)
        pos = new Position
        (
            reflectionPoint(listeningPos.X, sourcePos.X, listeningPos.Z, sourcePos.Z),
            reflectionPoint(listeningPos.Y, sourcePos.Y, listeningPos.Z, sourcePos.Z),
            0
        );
        s[4] = new PositionSimulation(env, source, baffle, sourcePos, pos, dipole);
        reflection[4] = new DistanceSimulation(pos.distance(listeningPos), env);
        
        // RoomZ (ceiling)
        pos = new Position
        (
            reflectionPoint(listeningPos.X, sourcePos.X, env.RoomZ - listeningPos.Z, env.RoomZ - sourcePos.Z),
            reflectionPoint(listeningPos.Y, sourcePos.Y, env.RoomZ - listeningPos.Z, env.RoomZ - sourcePos.Z),
            env.RoomZ
        );
        s[5] = new PositionSimulation(env, source, baffle, sourcePos, pos, dipole);
        reflection[5] = new DistanceSimulation(pos.distance(listeningPos), env);*/
    }
   
    public Complex response(double f)
    {
        Complex r = new Complex(1);
        
        for (int i = 0; i < wall.length; i++)
        {
            double x = wall[i] * f;
            r = r.add(wallSource[i].response(f).multiply(Math.sin(x) / x));
        }
        
        /*for (int i = 0; i < reflection.length; i++)
        {
            r = r.add(s[i].response(f).multiply(reflection[i].response(f)));
        }*/
        
        return r;
    }
}
