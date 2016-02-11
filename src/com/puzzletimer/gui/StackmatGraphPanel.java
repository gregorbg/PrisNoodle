package com.puzzletimer.gui;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class StackmatGraphPanel extends JPanel {
    byte[] data;

    public void setData(byte[] data) {
    	this.data = data;
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        g2.setColor(Color.BLACK);

        g2.setColor(Color.RED);
        if(this.data != null) {
	        for(int i = 0; i < this.data.length - 1; i++) {
	        	g2.drawLine((int)((double)getWidth()/(double)this.data.length*(double)i), (int)(-(((double)this.data[i] * ((double)getHeight()/(double)(Byte.MAX_VALUE-Byte.MIN_VALUE))))+(double)getHeight()/2), (int)((double)getWidth()/(double)this.data.length*(i+1)), (int)(-(((double)this.data[i+1] * ((double)getHeight()/(double)(Byte.MAX_VALUE-Byte.MIN_VALUE))))+(double)getHeight()/2));
	        }
        }
    }
}