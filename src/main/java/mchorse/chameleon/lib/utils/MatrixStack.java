package mchorse.chameleon.lib.utils;

import mchorse.chameleon.lib.data.model.ModelBone;
import mchorse.chameleon.lib.data.model.ModelCube;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.util.Stack;

/**
 * Simple implementation of a matrix stack
 */
public class MatrixStack
{
    private Stack<Matrix4f> model = new Stack<Matrix4f>();
    private Stack<Matrix3f> normal = new Stack<Matrix3f>();

    private Matrix4f tempModelMatrix = new Matrix4f();
    private Matrix3f tempNormalMatrix = new Matrix3f();

    public MatrixStack()
    {
        Matrix4f model = new Matrix4f();
        Matrix3f normal = new Matrix3f();

        model.setIdentity();
        normal.setIdentity();

        this.model.add(model);
        this.normal.add(normal);
    }

    public Matrix4f getModelMatrix()
    {
        return this.model.peek();
    }

    public Matrix3f getNormalMatrix()
    {
        return this.normal.peek();
    }

    public void push()
    {
        this.model.add(new Matrix4f(this.model.peek()));
        this.normal.add(new Matrix3f(this.normal.peek()));
    }

    public void pop()
    {
        if (this.model.size() == 1)
        {
            throw new IllegalStateException("A one level stack can't be popped!");
        }

        this.model.pop();
        this.normal.pop();
    }

    /* Translate */

    public void translate(float x, float y, float z)
    {
        this.translate(new Vector3f(x, y, z));
    }

    public void translate(Vector3f vec)
    {
        this.tempModelMatrix.setIdentity();
        this.tempModelMatrix.setTranslation(vec);

        this.model.peek().mul(this.tempModelMatrix);
    }

    public void moveToCubePivot(ModelCube cube)
    {
        Vector3f pivot = cube.pivot;

        this.translate(pivot.x, pivot.y, pivot.z);
    }

    public void moveBackFromCubePivot(ModelCube cube)
    {
        Vector3f pivot = cube.pivot;

        this.translate(-pivot.x, -pivot.y, -pivot.z);
    }

    public void moveToBonePivot(ModelBone bone)
    {
        Vector3f pivot = bone.initial.translate;

        this.translate(pivot.x, pivot.y, pivot.z);
    }

    public void moveBackFromBonePivot(ModelBone bone)
    {
        Vector3f pivot = bone.initial.translate;

        this.translate(-pivot.x, -pivot.y, -pivot.z);
    }

    public void translateBone(ModelBone bone)
    {
        Vector3f translate = bone.current.translate;

        this.translate(-translate.x, translate.y, translate.z);
    }

    /* Scale */

    public void scale(float x, float y, float z)
    {
        this.tempModelMatrix.setIdentity();
        this.tempModelMatrix.setM00(x);
        this.tempModelMatrix.setM11(y);
        this.tempModelMatrix.setM22(z);

        this.model.peek().mul(this.tempModelMatrix);

        if (x < 0 || y < 0 || z < 0)
        {
            this.tempNormalMatrix.setIdentity();
            this.tempNormalMatrix.setM00(x < 0 ? -1 : 1);
            this.tempNormalMatrix.setM11(y < 0 ? -1 : 1);
            this.tempNormalMatrix.setM22(z < 0 ? -1 : 1);

            this.normal.peek().mul(this.tempNormalMatrix);
        }
    }

    public void scaleBone(ModelBone bone)
    {
        this.scale(bone.current.scale.x, bone.current.scale.y, bone.current.scale.z);
    }

    /* Rotate */

    public void rotateX(float radian)
    {
        this.tempModelMatrix.setIdentity();
        this.tempModelMatrix.rotX(radian);

        this.tempNormalMatrix.setIdentity();
        this.tempNormalMatrix.rotX(radian);

        this.model.peek().mul(this.tempModelMatrix);
        this.normal.peek().mul(this.tempNormalMatrix);
    }

    public void rotateY(float radian)
    {
        this.tempModelMatrix.setIdentity();
        this.tempModelMatrix.rotY(radian);

        this.tempNormalMatrix.setIdentity();
        this.tempNormalMatrix.rotY(radian);

        this.model.peek().mul(this.tempModelMatrix);
        this.normal.peek().mul(this.tempNormalMatrix);
    }

    public void rotateZ(float radian)
    {
        this.tempModelMatrix.setIdentity();
        this.tempModelMatrix.rotZ(radian);

        this.tempNormalMatrix.setIdentity();
        this.tempNormalMatrix.rotZ(radian);

        this.model.peek().mul(this.tempModelMatrix);
        this.normal.peek().mul(this.tempNormalMatrix);
    }

    public void rotateBone(ModelBone bone)
    {
        final float degreesToPi = (float) (Math.PI / 180D);

        float z = bone.current.rotation.z;
        float y = bone.current.rotation.y;
        float x = bone.current.rotation.x;

        if (z != 0F) this.rotateZ(z * degreesToPi);
        if (y != 0F) this.rotateY(y * degreesToPi);
        if (x != 0F) this.rotateX(x * degreesToPi);
    }

    public void rotateCube(ModelCube bone)
    {
        final float degreesToPi = (float) (Math.PI / 180D);

        Vector3f rotation = bone.rotation;
        Matrix4f matrix4f = new Matrix4f();
        Matrix3f matrix3f = new Matrix3f();

        this.tempModelMatrix.setIdentity();
        matrix4f.rotZ(rotation.z * degreesToPi);
        this.tempModelMatrix.mul(matrix4f);

        matrix4f.rotY(rotation.y * degreesToPi);
        this.tempModelMatrix.mul(matrix4f);

        matrix4f.rotX(rotation.x * degreesToPi);
        this.tempModelMatrix.mul(matrix4f);

        this.tempNormalMatrix.setIdentity();
        matrix3f.rotZ(rotation.z * degreesToPi);
        this.tempNormalMatrix.mul(matrix3f);

        matrix3f.rotY(rotation.y * degreesToPi);
        this.tempNormalMatrix.mul(matrix3f);

        matrix3f.rotX(rotation.x * degreesToPi);
        this.tempNormalMatrix.mul(matrix3f);

        this.model.peek().mul(this.tempModelMatrix);
        this.normal.peek().mul(this.tempNormalMatrix);
    }
}
