package edu.cg;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLCapabilitiesImmutable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLJPanel;
import edu.cg.GameState;
import edu.cg.NeedForSpeed;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JFrame;

public class Main {
    static Point prevMouse;
    static int currentModel;
    static Frame frame;

    public static void main(String[] args) {
        frame = new JFrame();
        GLProfile.initSingleton();
        GLProfile glp = GLProfile.get("GL2");
        GLCapabilities caps = new GLCapabilities(glp);
        GLJPanel canvas = new GLJPanel(caps);
        frame.setSize(500, 500);
        frame.setLayout(new BorderLayout());
        frame.add((Component)canvas, "Center");
        final NeedForSpeed game = new NeedForSpeed(canvas);
        canvas.addGLEventListener(game);
        frame.addWindowListener(new WindowAdapter(){

            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                System.exit(1);
            }
        });
        canvas.addKeyListener(new KeyAdapter(){

            @Override
            public void keyPressed(KeyEvent e) {
                GameState gameState = game.getGameState();
                switch (e.getKeyCode()) {
                    case 38: {
                        gameState.updateAccelaration(GameState.AccelarationState.GAS);
                        break;
                    }
                    case 40: {
                        gameState.updateAccelaration(GameState.AccelarationState.BREAKS);
                        break;
                    }
                    case 37: {
                        gameState.updateSteering(GameState.SteeringState.LEFT);
                        break;
                    }
                    case 39: {
                        gameState.updateSteering(GameState.SteeringState.RIGHT);
                        break;
                    }
                    case 76: {
                        game.toggleNightMode();
                    }
                }
                super.keyPressed(e);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                GameState gameState = game.getGameState();
                switch (e.getKeyCode()) {
                    case 38: 
                    case 40: {
                        gameState.updateAccelaration(GameState.AccelarationState.CRUISE);
                        break;
                    }
                    case 37: 
                    case 39: {
                        gameState.updateSteering(GameState.SteeringState.STRAIGHT);
                        break;
                    }
                }
                super.keyPressed(e);
            }

            @Override
            public void keyTyped(KeyEvent e) {
                super.keyTyped(e);
            }
        });
        canvas.setFocusable(true);
        canvas.requestFocus();
        frame.setVisible(true);
        canvas.repaint();
    }

}

