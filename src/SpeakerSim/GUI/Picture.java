/*
 * Written in 2017 by Gregor Pintar <grpintar@gmail.com>
 * 
 * To the extent possible under law, the author(s) have dedicated
 * all copyright and related and neighboring rights to this software
 * to the public domain worldwide.
 * 
 * This software is distributed without any warranty.
 * 
 * You should have received a copy of the CC0 Public Domain Dedication.
 * If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package SpeakerSim.GUI;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.JComponent;

public class Picture extends JComponent
{
    private final BufferedImage image;

    public Picture(File file) throws IOException
    {
       image = ImageIO.read(file);
    }
    
    public Picture(InputStream file) throws IOException
    {
       image = ImageIO.read(file);
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        g.drawImage(image, (getWidth() - image.getWidth()) / 2, (getHeight() - image.getHeight()) / 2, this);
    }
    
    @Override
    public Dimension getPreferredSize()
    {
        return new Dimension(image.getWidth(), image.getHeight());
    }
    
    @Override
    public Dimension getMinimumSize()
    {
        return getPreferredSize();
    }
}
