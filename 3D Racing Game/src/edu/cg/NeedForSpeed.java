package edu.cg;

import java.awt.Component;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.fixedfunc.GLLightingFunc;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;

import edu.cg.algebra.Vec;
import edu.cg.models.Track;
import edu.cg.models.TrackSegment;
import edu.cg.models.Car.F1Car;
import edu.cg.models.Car.Specification;


public class NeedForSpeed implements GLEventListener {
    private GameState gameState = null; // Tracks the car movement and orientation
    private F1Car car = null; // The F1 car we want to render
    private Vec carCameraTranslation = null; // The accumulated translation that should be applied on the car, camera
    // and light sources
    private Track gameTrack = null; // The game track we want to render
    private FPSAnimator ani; // This object is responsible to redraw the model with a constant FPS
    private Component glPanel; // The canvas we draw on.
    private boolean isModelInitialized = false; // Whether model.init() was called.
    private boolean isDayMode = true; // Indicates whether the lighting mode is day/night.


    public NeedForSpeed(Component glPanel) {
        this.glPanel = glPanel;
        gameState = new GameState();
        gameTrack = new Track();
        carCameraTranslation = new Vec(0.0);
        car = new F1Car();
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        if (!isModelInitialized) {
            initModel(gl);
        }
        if (isDayMode) {
            gl.glClearColor(0.52f, 0.824f, 1.0f, 1.0f);
        } else {
            // TODO: Setup background color when night mode is on (You can choose differnt color)
            gl.glClearColor(0.0f, 0.0f, 0.32f, 1.0f);
        }
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
        // TODO: This is the flow in which we render the scene. You can use different flow.
        // Step (1) You should update the accumulated translation that needs to be
        // applied on the car, camera and light sources.
        updateCarCameraTranslation(gl);
        // Step (2) Position the camera and setup its orientation
        setupCamera(gl);
        // Step (3) setup the lighting.
        setupLights(gl);
        // Step (4) render the car.
        renderCar(gl);
        // Step (5) render the track.
        renderTrack(gl);
    }

    private void updateCarCameraTranslation(GL2 gl) {
        Vec ret = gameState.getNextTranslation();
        this.carCameraTranslation = this.carCameraTranslation.add(ret);
        double dx = Math.max((double) this.carCameraTranslation.x, -7.0);
        this.carCameraTranslation.x = (float) Math.min(dx, 7.0);
        if ((double) Math.abs(this.carCameraTranslation.z) >= TrackSegment.TRACK_LENGTH) {
            this.carCameraTranslation.z = -((float) ((double) Math.abs(this.carCameraTranslation.z) % TrackSegment.TRACK_LENGTH));
            gameTrack.changeTrack(gl);
        }
    }

    private void setupCamera(GL2 gl) {
        GLU glu = new GLU();

        double centerX = this.carCameraTranslation.x;
        double centerY = this.carCameraTranslation.y + 1.5;
        double centerZ = this.carCameraTranslation.z - 4;
        double xPointOfView = this.carCameraTranslation.x;
        double yPointOfView = this.carCameraTranslation.y + 1.8;
        double zPointOfView = this.carCameraTranslation.z + 2;
        int xUp = 0;
        int yUp = 1;
        int zUp = -1;
        glu.gluLookAt(xPointOfView, yPointOfView, zPointOfView, centerX, centerY, centerZ, xUp, yUp, zUp);
    }


    private void setupLights(GL2 gl) {
        if (isDayMode) {
            gl.glDisable(GLLightingFunc.GL_LIGHT0);
            gl.glDisable(GLLightingFunc.GL_LIGHT1);

            int light = GLLightingFunc.GL_LIGHT0;
            float[] dayModeColor = new float[]{1.0f, 1.0f, 1.0f, 1.0f};
            Vec dir = new Vec(0.0, 1.0, 1.0).normalize();
            float[] pos = new float[]{dir.x, dir.y, dir.z, 0.0f};

            gl.glLightfv(light, GLLightingFunc.GL_SPECULAR, dayModeColor, 0);
            gl.glLightfv(light, GLLightingFunc.GL_DIFFUSE, dayModeColor, 0);
            gl.glLightfv(light, GLLightingFunc.GL_POSITION, pos, 0);
            gl.glLightfv(light, GLLightingFunc.GL_AMBIENT, new float[]{0.1f, 0.1f, 0.1f, 1.0f}, 0);
            gl.glEnable(light);
        } else {

            gl.glDisable(GLLightingFunc.GL_LIGHT0);
            int firstSpotLight = GLLightingFunc.GL_LIGHT0;
            int SecondSpotLight = GLLightingFunc.GL_LIGHT1;
            Vec firstSpotLightPosition = new Vec(5.0f, 10.0f, 0);
            Vec secondSpotLightPosition = new Vec(-5.0f, 10.0f, 0);
            //initialize ambient light for moon
            gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, new float[]{0.1f, 0.1f, 0.1f, 1.0f}, 0);


            float[] pos1 = new float[]{firstSpotLightPosition.x + this.carCameraTranslation.x,
                    firstSpotLightPosition.y + this.carCameraTranslation.y, firstSpotLightPosition.z + this.carCameraTranslation.z, 1.0f};
            this.setupSpotlight(gl, firstSpotLight, pos1);
            float[] pos2 = new float[]{secondSpotLightPosition.x + this.carCameraTranslation.x,
                    secondSpotLightPosition.y + this.carCameraTranslation.y, secondSpotLightPosition.z + this.carCameraTranslation.z, 1.0f};
            this.setupSpotlight(gl, SecondSpotLight, pos2);
        }
    }

    private void setupSpotlight(GL2 gl, int light, float[] pos) {
        float[] spotDirection = new float[]{0.0f, -1.0f, 0.0f};
        float[] spotColor = new float[]{0.9f, 0.9f, 0.9f, 1.0f};
        float cutOffAngle = 65.0f;

        gl.glLightfv(light, GLLightingFunc.GL_POSITION, pos, 0);
        gl.glLightf(light, GLLightingFunc.GL_SPOT_CUTOFF, cutOffAngle);
        gl.glLightfv(light, GLLightingFunc.GL_SPOT_DIRECTION, spotDirection, 0);
        gl.glLightfv(light, GLLightingFunc.GL_SPECULAR, spotColor, 0);
        gl.glLightfv(light, GLLightingFunc.GL_DIFFUSE, spotColor, 0);
        gl.glEnable(light);

    }

    private void renderTrack(GL2 gl) {
        gl.glPushMatrix();
        gameTrack.render(gl);
        gl.glPopMatrix();
    }

    private void renderCar(GL2 gl) {
        double carRotation = gameState.getCarRotation();
        gl.glPushMatrix();
        gl.glTranslated((double) carCameraTranslation.x, (double) carCameraTranslation.y + 0.15,
                (double) carCameraTranslation.z - 6.6);
        gl.glRotated(-carRotation, 0.0, 1.0, 0.0);
        gl.glRotated(90.0, 0.0, 0.1, 0.0);
        gl.glScaled(4.0, 4.0, 4.0);
        this.car.render(gl);
        gl.glPopMatrix();
    }

    public GameState getGameState() {
        return gameState;
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();

        // Initialize display callback timer
        ani = new FPSAnimator(30, true);
        ani.add(drawable);
        glPanel.repaint();

        initModel(gl);
        ani.start();
    }

    public void initModel(GL2 gl) {
        gl.glCullFace(GL2.GL_BACK);
        gl.glEnable(GL2.GL_CULL_FACE);

        gl.glEnable(GL2.GL_NORMALIZE);
        gl.glEnable(GL2.GL_DEPTH_TEST);
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEnable(GL2.GL_SMOOTH);

        car.init(gl);
        gameTrack.init(gl);
        isModelInitialized = true;
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();
        GLU glu = new GLU();

        double aspect = (double) width / height;
        gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(57.0, aspect, 2.0, TrackSegment.TRACK_LENGTH);
    }

    /**
     * Start redrawing the scene with 30 FPS
     */
    public void startAnimation() {
        if (!ani.isAnimating())
            ani.start();
    }

    /**
     * Stop redrawing the scene with 30 FPS
     */
    public void stopAnimation() {
        if (ani.isAnimating())
            ani.stop();
    }

    public void toggleNightMode() {
        isDayMode = !isDayMode;
    }

}