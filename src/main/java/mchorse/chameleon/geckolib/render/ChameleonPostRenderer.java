package mchorse.chameleon.geckolib.render;

import mchorse.mclib.utils.MatrixUtils;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.util.MatrixStack;

import javax.vecmath.Matrix4f;

/**
 * Post render processor
 *
 * This render processors is responsible for applying given bone's transformation
 * onto OpenGL's matrix stack. Make sure you push before and pop after the matrix
 * stack when you using this!
 */
@SideOnly(Side.CLIENT)
public class ChameleonPostRenderer implements IChameleonRenderProcessor
{
    private static Matrix4f matrix = new Matrix4f();

    private String boneName = "";

    /**
     * Multiply given matrix stack onto OpenGL's matrix stack
     */
    public static void multiplyMatrix(MatrixStack stack, GeoBone bone)
    {
        matrix.set(stack.getModelMatrix());
        matrix.transpose();

        MatrixUtils.matrixToFloat(MatrixUtils.floats, matrix);
        MatrixUtils.buffer.clear();
        MatrixUtils.buffer.put(MatrixUtils.floats);
        MatrixUtils.buffer.flip();

        GlStateManager.multMatrix(MatrixUtils.buffer);
        GlStateManager.translate(bone.rotationPointX / 16, bone.rotationPointY / 16, bone.rotationPointZ / 16);
    }

    public void setBoneName(String boneName)
    {
        this.boneName = boneName;
    }

    @Override
    public boolean renderBone(BufferBuilder builder, MatrixStack stack, GeoBone bone)
    {
        if (bone.name.equals(this.boneName))
        {
            multiplyMatrix(stack, bone);

            return true;
        }

        return false;
    }
}