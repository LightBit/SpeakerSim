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
    public void horizontalAngle_1()
    {
        Position a = new Position(1, 1, 1, 0, 0);
        Position b = new Position(1, 2, 1);
        assertEquals(0, a.horizontalAngle(b), 0);
    }
    
    @Test
    public void horizontalAngle_2()
    {
        Position a = new Position(1, 1, 1, 0, 45);
        Position b = new Position(1, 2, 2);
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
        Position b = new Position(1, 0, 1);
        assertEquals(0, a.horizontalAngle(b), 0);
    }
    
    @Test
    public void horizontalAngle_5()
    {
        Position a = new Position(1, 1, 1, 0, -45);
        Position b = new Position(1, 0, 0);
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
        Position b = new Position(1, 0, 1);
        assertEquals(0, a.horizontalAngle(b), 0);
    }

    @Test
    public void horizontalAngle_8()
    {
        Position a = new Position(1, 1, 1, 0, -180);
        Position b = new Position(1, 2, 1);
        assertEquals(-180, a.horizontalAngle(b), 0);
    }

    @Test
    public void verticalAngle_1()
    {
        Position a = new Position(1, 1, 1, 0, 0);
        Position b = new Position(1, 2, 1);
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
        Position b = new Position(1, 2, 1);
        assertEquals(0, a.verticalAngle(b), 0);
    }
    
    @Test
    public void verticalAngle_4()
    {
        Position a = new Position(1, 1, 1, 180, 0);
        Position b = new Position(1, 0, 1);
        assertEquals(0, a.verticalAngle(b), 0);
    }
    
    @Test
    public void verticalAngle_5()
    {
        Position a = new Position(1, 1, 1, -45, 0);
        Position b = new Position(0, 0, 1);
        assertEquals(0, a.verticalAngle(b), 0);
    }
    
    @Test
    public void verticalAngle_6()
    {
        Position a = new Position(1, 1, 1, -90, 0);
        Position b = new Position(0, 1, 1);
        assertEquals(0, a.verticalAngle(b), 0);
    }
    
    @Test
    public void verticalAngle_7()
    {
        Position a = new Position(1, 1, 1, -180, 0);
        Position b = new Position(1, 0, 1);
        assertEquals(0, a.verticalAngle(b), 0);
    }

    @Test
    public void verticalAngle_8()
    {
        Position a = new Position(1, 1, 1, 180, 0);
        Position b = new Position(1, 2, 1);
        assertEquals(180, a.verticalAngle(b), 0);
    }
}
