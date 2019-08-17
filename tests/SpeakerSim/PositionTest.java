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

import org.junit.Test;
import static org.junit.Assert.*;

public class PositionTest
{
    @Test
    public void anglesAreInRange()
    {
        Position a = new Position(1, 2, 3, 0, 0);
        Position b = new Position(5, 6, 4);
        
        for (a.HorizontalAngle = -180; a.HorizontalAngle <= 180; a.HorizontalAngle += 1)
        {
            for (a.VerticalAngle = -180; a.VerticalAngle <= 180; a.VerticalAngle += 0.5)
            {
                double horizontal = a.horizontalAngle(b);
                assertTrue(-180 <= horizontal && horizontal <= 180);
                
                double vertical = a.verticalAngle(b);
                assertTrue(-180 <= vertical && vertical <= 180);
            }
        }
    }
    
    @Test
    public void horizontalAngle_compensate()
    {
        Position a = new Position();
        Position b = new Position(0.3, 6, 1);
        
        for (a.X = 0; a.X <= 1; a.X += 0.01)
        {
            for (a.Y = 0; a.Y <= 7; a.Y += 1)
            {
                for (a.Z = 0; a.Z <= 5; a.Z += 0.5)
                {
                    for (a.VerticalAngle = -180; a.VerticalAngle <= 180; a.VerticalAngle += 10)
                    {
                        Position p = new Position(a.X, a.Y, a.Z, a.horizontalAngle(b), a.VerticalAngle);
                        assertEquals(0, p.horizontalAngle(b), 0.001);
                    }
                }
            }
        }
    }
    
    @Test
    public void verticalAngle_compensate()
    {
        Position a = new Position();
        Position b = new Position(0.3, 6, 1);
        
        for (a.X = 0; a.X <= 1; a.X += 0.01)
        {
            for (a.Y = 0; a.Y <= 7; a.Y += 1)
            {
                for (a.Z = 0; a.Z <= 5; a.Z += 0.5)
                {
                    for (a.HorizontalAngle = -180; a.HorizontalAngle <= 180; a.HorizontalAngle += 10)
                    {
                        Position p = new Position(a.X, a.Y, a.Z, a.HorizontalAngle, a.verticalAngle(b));
                        assertEquals(0, p.verticalAngle(b), 0.001);
                    }
                }
            }
        }
    }
    
    @Test
    public void moveHorizontallyToCalculated()
    {
        Position a = new Position(0, 0, 0);
        Position b = new Position(0.3, 6, 0);
        
        for (a.X = 0; a.X <= 1; a.X += 0.1)
        {
            for (a.Y = 0; a.Y <= 7; a.Y += 1)
            {
                Position p = a.moveHorizontally(a.distance(b), a.horizontalAngle(b));
                assertEquals(b.X, p.X, 0.001);
                assertEquals(b.Y, p.Y, 0.001);
                assertEquals(b.Z, p.Z, 0.001);
            }
        }
    }
    
    @Test
    public void moveVerticallyToCalculated()
    {
        Position a = new Position(0, 0, 0);
        Position b = new Position(0.3, 0, 6);
        
        for (a.X = 0; a.X <= 1; a.X += 0.1)
        {
            for (a.Z = 0; a.Z <= 7; a.Z += 1)
            {
                Position p = a.moveVertically(a.distance(b), a.verticalAngle(b));
                assertEquals(b.X, p.X, 0.001);
                assertEquals(b.Y, p.Y, 0.001);
                assertEquals(b.Z, p.Z, 0.001);
            }
        }
    }
    
    @Test
    public void horizontalAngle_1()
    {
        Position a = new Position(1, 1, 1, 0, 0);
        Position b = new Position(2, 1, 1);
        assertEquals(0, a.horizontalAngle(b), 0);
    }
    
    @Test
    public void horizontalAngle_2()
    {
        Position a = new Position(1, 1, 1, 0, 45);
        Position b = new Position(2, 1, 2);
        assertEquals(0, a.horizontalAngle(b), 0);
    }
    
    @Test
    public void horizontalAngle_3()
    {
        Position a = new Position(1, 1, 1, 0, 90);
        Position b = new Position(1, 1, 2);
        assertEquals(0, a.horizontalAngle(b), 0);
    }
    
    @Test
    public void horizontalAngle_4()
    {
        Position a = new Position(1, 1, 1, 0, 180);
        Position b = new Position(0, 1, 1);
        assertEquals(0, a.horizontalAngle(b), 0);
    }
    
    @Test
    public void horizontalAngle_5()
    {
        Position a = new Position(1, 1, 1, 0, -135);
        Position b = new Position(0, 1, 0);
        assertEquals(0, a.horizontalAngle(b), 0);
    }
    
    @Test
    public void horizontalAngle_6()
    {
        Position a = new Position(1, 1, 1, 0, -90);
        Position b = new Position(1, 1, 0);
        assertEquals(0, a.horizontalAngle(b), 0);
    }
    
    @Test
    public void horizontalAngle_7()
    {
        Position a = new Position(1, 1, 1, 0, -180);
        Position b = new Position(0, 1, 1);
        assertEquals(0, a.horizontalAngle(b), 0);
    }
    
    @Test
    public void horizontalAngle_8()
    {
        Position a = new Position(1, 1, 1, 0, 0);
        Position b = new Position(2, 2, 2);
        assertEquals(45, a.horizontalAngle(b), 0);
    }
    
    @Test
    public void horizontalAngle_9()
    {
        Position a = new Position(1, 1, 1, 0, 0);
        Position b = new Position(0, 1, 1);
        assertEquals(180, a.horizontalAngle(b), 0);
    }
    
    @Test
    public void horizontalAngle_10()
    {
        Position a = new Position(1, 1, 1, 0, 0);
        Position b = new Position(1, 0, 1);
        assertEquals(-90, a.horizontalAngle(b), 0);
    }
    
    
    @Test
    public void horizontalAngle_11()
    {
        Position a = new Position(1, 1, 1, 0, 0);
        Position b = new Position(1, 2, 1);
        assertEquals(90, a.horizontalAngle(b), 0);
    }
    
    @Test
    public void horizontalAngle_12()
    {
        Position a = new Position(1, 1, 1, 90, 0);
        Position b = new Position(1, 2, 1);
        assertEquals(0, a.horizontalAngle(b), 0.001);
    }
    
    @Test
    public void horizontalAngle_13()
    {
        Position a = new Position(1, 1, 1, -90, 0);
        Position b = new Position(1, 0, 1);
        assertEquals(0, a.horizontalAngle(b), 0.001);
    }
    
    @Test
    public void horizontalAngle_14()
    {
        Position a = new Position(1, 1, 1, 0, 0);
        Position b = new Position(2, 0, 9);
        assertEquals(-45, a.horizontalAngle(b), 0);
    }
    
    @Test
    public void horizontalAngle_15()
    {
        Position a = new Position(1, 1, 1, -45, 90);
        Position b = new Position(1, 0, 2);
        assertEquals(0, a.horizontalAngle(b), 0.001);
    }
    
    @Test
    public void horizontalAngle_16()
    {
        Position a = new Position(0, 1, 1, -100, 0);
        Position b = new Position(1, 1, 1);
        assertEquals(100, a.horizontalAngle(b), 0);
    }
    
    @Test
    public void horizontalAngle_17()
    {
        Position a = new Position(3, 2, 1, 0, 0);
        Position b = new Position(5, 6, 4);
        assertEquals(63.434, a.horizontalAngle(b), 0.001);
    }

    @Test
    public void verticalAngle_1()
    {
        Position a = new Position(1, 1, 1, 0, 0);
        Position b = new Position(2, 1, 1);
        assertEquals(0, a.verticalAngle(b), 0);
    }
    
    @Test
    public void verticalAngle_2()
    {
        Position a = new Position(1, 1, 1, 45, 0);
        Position b = new Position(2, 2, 1);
        assertEquals(0, a.verticalAngle(b), 0);
    }
    
    @Test
    public void verticalAngle_3()
    {
        Position a = new Position(1, 1, 1, 90, 0);
        Position b = new Position(2, 1, 1);
        assertEquals(0, a.verticalAngle(b), 0);
    }
    
    @Test
    public void verticalAngle_4()
    {
        Position a = new Position(1, 1, 1, 180, 0);
        Position b = new Position(0, 1, 1);
        assertEquals(0, a.verticalAngle(b), 0);
    }
    
    @Test
    public void verticalAngle_5()
    {
        Position a = new Position(1, 1, 1, -135, 0);
        Position b = new Position(0, 0, 1);
        assertEquals(0, a.verticalAngle(b), 0);
    }
    
    @Test
    public void verticalAngle_6()
    {
        Position a = new Position(1, 1, 1, -90, 0);
        Position b = new Position(1, 0, 1);
        assertEquals(0, a.verticalAngle(b), 0);
    }
    
    @Test
    public void verticalAngle_7()
    {
        Position a = new Position(1, 1, 1, -180, 0);
        Position b = new Position(0, 1, 1);
        assertEquals(0, a.verticalAngle(b), 0);
    }
    
    @Test
    public void verticalAngle_8()
    {
        Position a = new Position(1, 1, 1, 0, 0);
        Position b = new Position(0, 0, 0);
        assertEquals(-135, a.verticalAngle(b), 0);
    }
    
    @Test
    public void verticalAngle_9()
    {
        Position a = new Position(1, 1, 1, 0, 0);
        Position b = new Position(0, 1, 1);
        assertEquals(180, a.verticalAngle(b), 0);
    }
    
    @Test
    public void verticalAngle_10()
    {
        Position a = new Position(1, 1, 1, 0, 0);
        Position b = new Position(1, 1, 0);
        assertEquals(-90, a.verticalAngle(b), 0);
    }
    
    @Test
    public void verticalAngle_11()
    {
        Position a = new Position(1, 1, 1, 0, 0);
        Position b = new Position(1, 1, 2);
        assertEquals(90, a.verticalAngle(b), 0);
    }
    
    @Test
    public void verticalAngle_12()
    {
        Position a = new Position(1, 1, 1, 0, 90);
        Position b = new Position(1, 1, 2);
        assertEquals(0, a.verticalAngle(b), 0.001);
    }
    
    @Test
    public void verticalAngle_13()
    {
        Position a = new Position(1, 1, 1, 0, -90);
        Position b = new Position(1, 1, 0);
        assertEquals(0, a.verticalAngle(b), 0.001);
    }
    
    @Test
    public void verticalAngle_14()
    {
        Position a = new Position(1, 1, 1, 0, 0);
        Position b = new Position(2, 9, 0);
        assertEquals(-45, a.verticalAngle(b), 0);
    }
    
    @Test
    public void verticalAngle_15()
    {
        Position a = new Position(1, 1, 1, 90, -45);
        Position b = new Position(1, 2, 0);
        assertEquals(0, a.verticalAngle(b), 0.001);
    }
    
    @Test
    public void verticalAngle_16()
    {
        Position a = new Position(0, 1, 1, 0, 100);
        Position b = new Position(1, 1, 1);
        assertEquals(-100, a.verticalAngle(b), 0);
    }
    
    @Test
    public void verticalAngle_17()
    {
        Position a = new Position(3, 2, 1, 0, 0);
        Position b = new Position(5, 6, 4);
        assertEquals(56.309, a.verticalAngle(b), 0.001);
    }
    
    @Test
    public void moveHorizontally_1()
    {
        Position a = new Position(1, 2, 5, 30, 70);
        Position b = a.moveHorizontally(2, 0);
        assertEquals(a.X + 2, b.X, 0.001);
        assertEquals(a.Y, b.Y, 0.001);
        assertEquals(a.Z, b.Z, 0);
        assertEquals(a.HorizontalAngle, b.HorizontalAngle, 0);
        assertEquals(a.VerticalAngle, b.VerticalAngle, 0);
    }
    
    @Test
    public void moveHorizontally_2()
    {
        Position a = new Position(1, 2, 3);
        Position b = a.moveHorizontally(2, 90);
        assertEquals(a.X, b.X, 0.001);
        assertEquals(a.Y + 2, b.Y, 0.001);
        assertEquals(a.Z, b.Z, 0);
    }
    
    @Test
    public void moveHorizontally_3()
    {
        Position a = new Position(4, 50, 1);
        Position b = a.moveHorizontally(1, -100);
        assertEquals(3.826, b.X, 0.001);
        assertEquals(49.015, b.Y, 0.001);
        assertEquals(1, b.Z, 0);
    }
    
    @Test
    public void moveVertically_1()
    {
        Position a = new Position(1, 2, 5, 30, 70);
        Position b = a.moveVertically(2, 0);
        assertEquals(a.X + 2, b.X, 0.001);
        assertEquals(a.Y, b.Y, 0);
        assertEquals(a.Z, b.Z, 0.001);
        assertEquals(a.HorizontalAngle, b.HorizontalAngle, 0);
        assertEquals(a.VerticalAngle, b.VerticalAngle, 0);
    }
    
    @Test
    public void moveVertically_4()
    {
        Position a = new Position(1, 2, 5);
        Position b = a.moveVertically(2, 90);
        assertEquals(a.X, b.X, 0.001);
        assertEquals(a.Y, b.Y, 0);
        assertEquals(a.Z + 2, b.Z, 0.001);
    }
    
    @Test
    public void moveVertically_3()
    {
        Position a = new Position(4, 50, 1);
        Position b = a.moveVertically(1, -100);
        assertEquals(3.826, b.X, 0.001);
        assertEquals(a.Y, b.Y, 0);
        assertEquals(0.015, b.Z, 0.001);
    }
}
