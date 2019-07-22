package edu.cg.models.Car;

import com.jogamp.opengl.GL2;
import edu.cg.models.Car.Back;
import edu.cg.models.Car.Center;
import edu.cg.models.Car.Front;
import edu.cg.models.IRenderable;

public class F1Car implements IRenderable {
    @Override
    public void render(GL2 gl) {
        new Center().render(gl);
        gl.glPushMatrix();
        gl.glTranslated(-(Specification.C_BASE_DEPTH/2+Specification.B_BASE_LENGTH/2), 0, 0);
        new Back().render(gl);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslated(Specification.C_BASE_LENGTH/2+Specification.F_HOOD_LENGTH_1, 0, 0);
        new Front().render(gl);
        gl.glPopMatrix();
    }

    public String toString() {
        return "F1Car";
    }

    @Override
    public void init(GL2 gl) {
    }

    @Override
    public void destroy(GL2 gl) {

    }
}

