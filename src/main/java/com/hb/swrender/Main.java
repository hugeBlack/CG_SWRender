package com.hb.swrender;

import com.hb.swrender.objects.SquareObject;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Main {
    public static void main(String[] args) {
        System.setProperty("sun.java2d.uiScale", "1");

        JFrame window = new JFrame();
        window.setSize(660, 535);
        com.hb.swrender.Canvas c = new Canvas(640, 480, 17);
        window.add(c);
        window.setContentPane(c);
        window.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyChar() == 'w' || e.getKeyChar() == 'W')
                    Camera.MOVE_FORWARD = true;
                else if(e.getKeyChar() == 's' || e.getKeyChar() == 'S')
                    Camera.MOVE_BACKWARD = true;
                else if(e.getKeyChar() == 'a' || e.getKeyChar() == 'A')
                    Camera.SLIDE_LEFT = true;
                else if(e.getKeyChar() == 'd' || e.getKeyChar() == 'D')
                    Camera.SLIDE_RIGHT = true;
                else if(e.getKeyChar() == ' ' || e.getKeyChar() == ' ')
                    Camera.MOVE_UP = true;
                else if(e.getKeyChar() == 'z' || e.getKeyChar() == 'Z')
                    Camera.MOVE_DOWN = true;


                if(e.getKeyCode() == KeyEvent.VK_UP)
                    Camera.LOOK_UP= true;
                else if(e.getKeyCode() == KeyEvent.VK_DOWN)
                    Camera.LOOK_DOWN = true;
                else if(e.getKeyCode() == KeyEvent.VK_LEFT)
                    Camera.LOOK_LEFT = true;
                else if(e.getKeyCode() == KeyEvent.VK_RIGHT)
                    Camera.LOOK_RIGHT = true;
                else if(e.getKeyChar() == 'k' || e.getKeyChar() == 'K')
                    c.screenShot();

            }

            @Override
            public void keyReleased(KeyEvent e) {
                if(e.getKeyChar() == 'w' || e.getKeyChar() == 'W')
                    Camera.MOVE_FORWARD = false;
                else if(e.getKeyChar() == 's' || e.getKeyChar() == 'S')
                    Camera.MOVE_BACKWARD = false;
                else if(e.getKeyChar() == 'a' || e.getKeyChar() == 'A')
                    Camera.SLIDE_LEFT = false;
                else if(e.getKeyChar() == 'd' || e.getKeyChar() == 'D')
                    Camera.SLIDE_RIGHT = false;
                else if(e.getKeyChar() == ' ' || e.getKeyChar() == ' ')
                    Camera.MOVE_UP = false;
                else if(e.getKeyChar() == 'z' || e.getKeyChar() == 'Z')
                    Camera.MOVE_DOWN = false;


                if(e.getKeyCode() == KeyEvent.VK_UP)
                    Camera.LOOK_UP= false;
                else if(e.getKeyCode() == KeyEvent.VK_DOWN)
                    Camera.LOOK_DOWN = false;
                else if(e.getKeyCode() == KeyEvent.VK_LEFT)
                    Camera.LOOK_LEFT = false;
                else if(e.getKeyCode() == KeyEvent.VK_RIGHT)
                    Camera.LOOK_RIGHT = false;

            }

            @Override
            public void keyTyped(KeyEvent e) {
                // TODO Auto-generated method stub

            }
        });

        window.addWindowListener(new WindowAdapter()
                                 {
                                     @Override
                                     public void windowClosing(WindowEvent e)
                                     {
                                         System.out.println("Closed");
                                         e.getWindow().dispose();
                                         c.stopRender();
                                     }
                                 });

        window.setVisible(true);
        c.objects.add(new SquareObject());
        c.startPaint();

    }
}