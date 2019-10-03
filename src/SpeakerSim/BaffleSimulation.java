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

public class BaffleSimulation
{
    private final double horizontalAngle;
    private final double verticalAngle;
    private final double angle;
    private final boolean enabled;
    private final double[] mean;
    private final double[] length;
    private final double averageWavelength;
    private final double edge;
    private final Driver driver;
    private final boolean dipole;
    
    private static double edgeDistance(double angle, double distanceX, double distanceY)
    {
        double x = Math.toRadians(angle);
        
        if (distanceX * Math.tan(x) < distanceY)
        {
            return distanceX / Math.cos(x);
        }
        else
        {
            return distanceY / Math.cos(Math.toRadians(90 - angle));
        }
    }
    
    public BaffleSimulation(Baffle baffle, Driver driver, double distance, double horizontalAngle, double verticalAngle, Environment env, boolean dipole)
    {
        this.horizontalAngle = horizontalAngle;
        this.verticalAngle = verticalAngle;
        this.driver = driver;
        this.dipole = dipole;
        angle = Math.max(Math.abs(horizontalAngle), Math.abs(verticalAngle));
        
        if (baffle == null || !baffle.Enabled)
        {
            mean = null;
            length = null;
            averageWavelength = 0;
            edge = 0;
            enabled = baffle == null;
            
            return;
        }
        
        enabled = true;
        
        double[] edges = new double[36];
        edges[0] = edgeDistance(90, baffle.X, baffle.Y); // 0
        edges[1] = edgeDistance(80, baffle.X, baffle.Y); // 10
        edges[2] = edgeDistance(70, baffle.X, baffle.Y); // 20
        edges[3] = edgeDistance(60, baffle.X, baffle.Y); // 30
        edges[4] = edgeDistance(50, baffle.X, baffle.Y); // 40
        edges[5] = edgeDistance(40, baffle.X, baffle.Y); // 50
        edges[6] = edgeDistance(30, baffle.X, baffle.Y); // 60
        edges[7] = edgeDistance(20, baffle.X, baffle.Y); // 70
        edges[8] = edgeDistance(10, baffle.X, baffle.Y); // 80
        edges[9] = edgeDistance(0, baffle.X, baffle.Height - baffle.Y); // 90
        edges[10] = edgeDistance(10, baffle.X, baffle.Height - baffle.Y); // 100
        edges[11] = edgeDistance(20, baffle.X, baffle.Height - baffle.Y); // 110
        edges[12] = edgeDistance(30, baffle.X, baffle.Height - baffle.Y); // 120
        edges[13] = edgeDistance(40, baffle.X, baffle.Height - baffle.Y); // 130
        edges[14] = edgeDistance(50, baffle.X, baffle.Height - baffle.Y); // 140
        edges[15] = edgeDistance(60, baffle.X, baffle.Height - baffle.Y); // 150
        edges[16] = edgeDistance(70, baffle.X, baffle.Height - baffle.Y); // 160
        edges[17] = edgeDistance(80, baffle.X, baffle.Height - baffle.Y); // 170
        edges[18] = edgeDistance(90, baffle.Width - baffle.X, baffle.Height - baffle.Y); // 180
        edges[19] = edgeDistance(80, baffle.Width - baffle.X, baffle.Height - baffle.Y); // 190
        edges[20] = edgeDistance(70, baffle.Width - baffle.X, baffle.Height - baffle.Y); // 200
        edges[21] = edgeDistance(60, baffle.Width - baffle.X, baffle.Height - baffle.Y); // 210
        edges[22] = edgeDistance(50, baffle.Width - baffle.X, baffle.Height - baffle.Y); // 220
        edges[23] = edgeDistance(40, baffle.Width - baffle.X, baffle.Height - baffle.Y); // 230
        edges[24] = edgeDistance(30, baffle.Width - baffle.X, baffle.Height - baffle.Y); // 240
        edges[25] = edgeDistance(20, baffle.Width - baffle.X, baffle.Height - baffle.Y); // 250
        edges[26] = edgeDistance(10, baffle.Width - baffle.X, baffle.Height - baffle.Y); // 260
        edges[27] = edgeDistance(0, baffle.Width - baffle.X, baffle.Y); // 270
        edges[28] = edgeDistance(10, baffle.Width - baffle.X, baffle.Y); // 280
        edges[29] = edgeDistance(20, baffle.Width - baffle.X, baffle.Y); // 290
        edges[30] = edgeDistance(30, baffle.Width - baffle.X, baffle.Y); // 300
        edges[31] = edgeDistance(40, baffle.Width - baffle.X, baffle.Y); // 310
        edges[32] = edgeDistance(50, baffle.Width - baffle.X, baffle.Y); // 320
        edges[33] = edgeDistance(60, baffle.Width - baffle.X, baffle.Y); // 330
        edges[34] = edgeDistance(70, baffle.Width - baffle.X, baffle.Y); // 340
        edges[35] = edgeDistance(80, baffle.Width - baffle.X, baffle.Y); // 350
        
        double[] axis = new double[36];
        mean = new double[36];
        length = new double[36];
        
        double horizontal = Math.cos(Math.toRadians(90 - horizontalAngle));
        double vertical = Math.sin(Math.toRadians(verticalAngle));
        double averageDifference = 0;
        
        for (int i = 0; i < 36; i++)
        {
            double radians = Math.toRadians(i * 10);
            axis[i] = Math.sqrt(Math.max(distance * distance + edges[i] * edges[i] - (2 * distance * edges[i] * (Math.sin(radians) * horizontal + Math.cos(radians) * vertical)), 0)) + edges[i] - distance;
            
            mean[i] = axis[i] - edges[i];
            averageDifference += mean[i];
            
            length[i] = axis[i] * (2 * Math.PI / env.SpeedOfSound);
        }
        averageDifference /= 36;
        
        for (int i = 0; i < 36; i++)
        {
            mean[i] = Math.pow((averageDifference - mean[i] + averageDifference + distance) / (averageDifference + distance), 2);
        }
        
        averageWavelength = Math.pow((distance + averageDifference) / distance, 2);
        edge = (env.SpeedOfSound / 4) / (baffle.EdgeRadius + Double.MIN_VALUE) / 2;
    }
    
    public BaffleSimulation(Baffle baffle, Driver driver, Position sourcePos, Position listeningPos, Environment env, boolean dipole)
    {
        this(baffle, driver, sourcePos.distance(listeningPos) + 0.0000001, sourcePos.horizontalAngle(listeningPos), sourcePos.verticalAngle(listeningPos), env, dipole);
    }
    
    private double offAxis(double f, double horizontalAngle, double verticalAngle)
    {
        return driver != null ? driver.relativeOffAxis(f, horizontalAngle, verticalAngle, dipole) : 1;
    }
    
    private double diffraction(double f)
    {
        if (mean == null)
        {
            return 1;
        }
        
        double t = offAxis(f, 90, 90) / Math.sqrt(1 + Math.pow(f / edge, 2)); // TODO: 90, 90!
        double average = 0;
        
        for (int i = 0; i < 36; i++)
        {
            average += mean[i] * Math.cos(f * length[i] - Math.PI) * t;
        }
        average /= 36;
        
        t = (average + averageWavelength) / averageWavelength + 1;
        
        if (dipole)
        {
            return t * (1 - Math.sin(Math.toRadians(angle)) * 0.9);
        }
        else
        {
            double a = angle / 180;
            
            return (1 - a) * t + a / t;
        }
    }
    
    public Complex response(double f)
    {
        Complex x = Complex.toComplex((enabled ? 0.5 : 1) * offAxis(f, horizontalAngle, verticalAngle) * diffraction(f), 0);
        
        // invert phase, if behind dipole
        if (dipole && (angle < 270 && angle > 90))
        {
            x = x.conjugate();
        }
        
        return x;
    }
}
