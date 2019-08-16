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
    private final BaffleSimulation[] source;
    private final DistanceSimulation[] reflection;
    private final double[] wall;
    private final BaffleSimulation[] wallSource;
    
    private static double reflectionPoint(double a1, double a2, double b1, double b2)
    {
        return b1 * Math.abs(a1 - a2) / (b1 + b2) + Math.min(a1, a2);
    }
    
    public RoomSimulation(final Baffle baffle, final Driver driver, final Position sourcePos, final Position listeningPos, final Environment env, boolean dipole)
    {
        source = new BaffleSimulation[6];
        reflection = new DistanceSimulation[6];
        wall = new double[6];
        wallSource = new BaffleSimulation[6];
        
        // boundary reinforcement
        double lambda = 4 * Math.PI / env.SpeedOfSound;
        wall[0] = lambda * sourcePos.X;
        wall[1] = lambda * sourcePos.Y;
        wall[2] = lambda * sourcePos.Z;
        wall[3] = lambda * (env.RoomX - sourcePos.X);
        wall[4] = lambda * (env.RoomY - sourcePos.Y);
        wall[5] = lambda * (env.RoomZ - sourcePos.Z);
        
        wallSource[0] = new BaffleSimulation(baffle, driver, sourcePos.X, sourcePos.HorizontalAngle, 0, env, dipole);
        wallSource[1] = new BaffleSimulation(baffle, driver, sourcePos.Y, 90 - sourcePos.HorizontalAngle, 0, env, dipole);
        wallSource[2] = new BaffleSimulation(baffle, driver, sourcePos.Z, 0, 90 - sourcePos.VerticalAngle, env, dipole);
        wallSource[3] = new BaffleSimulation(baffle, driver, env.RoomX - sourcePos.X, 180 - sourcePos.HorizontalAngle, 0, env, dipole);
        wallSource[4] = new BaffleSimulation(baffle, driver, env.RoomY - sourcePos.Y, 270 - sourcePos.HorizontalAngle, 0, env, dipole);
        wallSource[5] = new BaffleSimulation(baffle, driver, env.RoomZ - sourcePos.Z, 0, 270 - sourcePos.VerticalAngle, env, dipole);
        
        // first reflections
        Position reflectionPos;
        
        // X (front wall)
        reflectionPos = new Position
        (
            0,
            reflectionPoint(listeningPos.Y, sourcePos.Y, listeningPos.X, sourcePos.X),
            reflectionPoint(listeningPos.Z, sourcePos.Z, listeningPos.X, sourcePos.X)
        );
        source[0] = new BaffleSimulation(baffle, driver, sourcePos, reflectionPos, env, dipole);
        reflection[0] = new DistanceSimulation(sourcePos.distance(reflectionPos) + reflectionPos.distance(listeningPos), env);
        
        // RoomX (back wall)
        reflectionPos = new Position
        (
            env.RoomX,
            reflectionPoint(listeningPos.Y, sourcePos.Y, env.RoomX - listeningPos.X, env.RoomX - sourcePos.X),
            reflectionPoint(listeningPos.Z, sourcePos.Z, env.RoomX - listeningPos.X, env.RoomX - sourcePos.X)
        );
        source[1] = new BaffleSimulation(baffle, driver, sourcePos, reflectionPos, env, dipole);
        reflection[1] = new DistanceSimulation(sourcePos.distance(reflectionPos) + reflectionPos.distance(listeningPos), env);
        
        // Y (left wall)
        reflectionPos = new Position
        (
            reflectionPoint(listeningPos.X, sourcePos.X, listeningPos.Y, sourcePos.Y),
            0,
            reflectionPoint(listeningPos.Z, sourcePos.Z, listeningPos.Y, sourcePos.Y)
        );
        source[2] = new BaffleSimulation(baffle, driver, sourcePos, reflectionPos, env, dipole);
        reflection[2] = new DistanceSimulation(sourcePos.distance(reflectionPos) + reflectionPos.distance(listeningPos), env);
        
        // RoomY (right wall)
        reflectionPos = new Position
        (
            reflectionPoint(listeningPos.X, sourcePos.X, env.RoomY - listeningPos.Y, env.RoomY - sourcePos.Y),
            env.RoomY,
            reflectionPoint(listeningPos.Z, sourcePos.Z, env.RoomY - listeningPos.Y, env.RoomY - sourcePos.Y)
        );
        source[3] = new BaffleSimulation(baffle, driver, sourcePos, reflectionPos, env, dipole);
        reflection[3] = new DistanceSimulation(sourcePos.distance(reflectionPos) + reflectionPos.distance(listeningPos), env);
       
        // Z (floor)
        reflectionPos = new Position
        (
            reflectionPoint(listeningPos.X, sourcePos.X, listeningPos.Z, sourcePos.Z),
            reflectionPoint(listeningPos.Y, sourcePos.Y, listeningPos.Z, sourcePos.Z),
            0
        );
        source[4] = new BaffleSimulation(baffle, driver, sourcePos, reflectionPos, env, dipole);
        reflection[4] = new DistanceSimulation(sourcePos.distance(reflectionPos) + reflectionPos.distance(listeningPos), env);
        
        // RoomZ (ceiling)
        reflectionPos = new Position
        (
            reflectionPoint(listeningPos.X, sourcePos.X, env.RoomZ - listeningPos.Z, env.RoomZ - sourcePos.Z),
            reflectionPoint(listeningPos.Y, sourcePos.Y, env.RoomZ - listeningPos.Z, env.RoomZ - sourcePos.Z),
            env.RoomZ
        );
        source[5] = new BaffleSimulation(baffle, driver, sourcePos, reflectionPos, env, dipole);
        reflection[5] = new DistanceSimulation(sourcePos.distance(reflectionPos) + reflectionPos.distance(listeningPos), env);
    }
   
    public Complex response(double f)
    {
        Complex r = new Complex(1);
        
        for (int i = 0; i < wall.length; i++)
        {
            double x = wall[i] * f;
            r = r.add(wallSource[i].response(f).multiply(Math.sin(x) / x));
        }
        
        for (int i = 0; i < reflection.length; i++)
        {
            r = r.add(source[i].response(f).multiply(reflection[i].response(f)));
        }
        
        return r;
    }
}
