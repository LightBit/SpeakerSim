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

public abstract class Filter extends Item implements IFilter
{
    @Override
    public Complex filter(double f)
    {
        return super.filter(f).multiply(thisFilter(f));
    }
    
    @Override
    public Complex response(double f)
    {
        return super.response(f).multiply(thisFilter(f));
    }
    
    @Override
    public Complex response1W(double f)
    {
        return super.response1W(f).multiply(thisFilter(f));
    }
    
    @Override
    public Complex listeningWindowResponse(double f)
    {
        return super.listeningWindowResponse(f).multiply(thisFilter(f));
    }
    
    @Override
    public Complex powerResponse(double f)
    {
        return super.powerResponse(f).multiply(thisFilter(f));
    }
    
    @Override
    public double maxPower(double f)
    {
        return super.maxPower(f) / Math.pow(thisFilter(f).abs(), 2);
    }
    
    @Override
    public double excursion(double f, double power)
    {
        return super.excursion(f, power) * thisFilter(f).abs();
    }
    
    @Override
    public Complex responseWithBaffle(double f)
    {
        return super.responseWithBaffle(f).multiply(thisFilter(f));
    }
    
    @Override
    public Complex responseWithRoom(double f)
    {
        return super.responseWithRoom(f).multiply(thisFilter(f));
    }
}
