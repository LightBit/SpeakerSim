/*
 * Copyright (C) 2018 Gregor Pintar <grpintar@gmail.com>
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

package SpeakerSim.GUI;

import SpeakerSim.AperiodicSimulation;
import SpeakerSim.Aperiodic;
import SpeakerSim.Driver;
import SpeakerSim.Project;

public class VentCalculator extends PortCalculator
{
    final Aperiodic x;
    
    public VentCalculator(final java.awt.Frame parent, final Project project, final Aperiodic box, final AperiodicSimulation sim, final Driver driver)
    {
        super(parent, project, AperiodicSimulation.convert(box, driver), sim);
        this.x = box;
        super.setTitle("Vent calculator");
    }
    
    @Override
    protected void save()
    {
        super.save();
        
        x.Vb = box.Vb;
        x.Ql = box.Ql;
        x.Qa = box.Qa;
        x.VentShape = box.PortShape;
        x.Dv = box.Dv;
        x.Wv = box.Wv;
        x.Hv = box.Hv;
        x.Np = box.Np;
        x.Thickness = box.Thickness;
        x.Ends = box.Ends;
        x.VentPosition = box.PortPosition;
    }
}
