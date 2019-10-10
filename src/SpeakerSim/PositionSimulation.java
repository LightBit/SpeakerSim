/*
 * Copyright (C) 2019 Gregor Pintar <grpintar@gmail.com>
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

public class PositionSimulation
{
    private final ISource source;
    private final BaffleSimulation baffle;
    private final DistanceSimulation distance;
    private final boolean dipole;
    private final double horizontalAngle;
    private final double verticalAngle;
    
    public PositionSimulation(Environment env, ISource source, Baffle baffle, Position sourcePos, Position listeningPos, boolean dipole)
    {
        this.source = source;
        this.distance = new DistanceSimulation(sourcePos, listeningPos, env);
        this.baffle = new BaffleSimulation(baffle, source, sourcePos, listeningPos, env, dipole);
        this.dipole = dipole;
        horizontalAngle = sourcePos.horizontalAngle(listeningPos);
        verticalAngle = sourcePos.verticalAngle(listeningPos);
    }
    
    public Complex response(double f)
    {
        Complex x = source.relativeOffAxis(f, horizontalAngle, verticalAngle, dipole);
        x = x.multiply(baffle.response(f));
        x = x.multiply(distance.response(f));
        
        return x;
    }
}
