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
    protected Complex itemFilter(double f)
    {
        return super.itemFilter(f).multiply(thisFilter(f));
    }
    
    @Override
    protected Complex itemResponse(double f)
    {
        return super.itemResponse(f).multiply(thisFilter(f));
    }
    
    @Override
    protected Complex itemResponse1W(double f)
    {
        return super.itemResponse1W(f).multiply(thisFilter(f));
    }
    
    @Override
    protected Complex itemListeningWindowResponse(double f)
    {
        return super.itemListeningWindowResponse(f).multiply(thisFilter(f));
    }
    
    @Override
    protected Complex itemPowerResponse(double f)
    {
        return super.itemPowerResponse(f).multiply(thisFilter(f));
    }
    
    @Override
    protected Complex itemResponseWithBaffle(double f)
    {
        return super.itemResponseWithBaffle(f).multiply(thisFilter(f));
    }
    
    @Override
    protected Complex itemResponseWithRoom(double f)
    {
        return super.itemResponseWithRoom(f).multiply(thisFilter(f));
    }
    
    @Override
    protected double itemMaxPower(double f)
    {
        return super.itemMaxPower(f) / Math.pow(thisFilter(f).abs(), 2);
    }
    
    @Override
    protected double itemExcursion(double f, double power)
    {
        return super.itemExcursion(f, power) * thisFilter(f).abs();
    }
}
