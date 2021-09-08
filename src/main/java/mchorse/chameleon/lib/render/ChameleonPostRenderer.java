package mchorse.chameleon.lib.render;

import mchorse.chameleon.lib.data.model.ModelBone;
import mchorse.chameleon.lib.data.model.ModelTransform;
import mchorse.chameleon.lib.utils.MatrixStack;
import mchorse.mclib.utils.MatrixUtils;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

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
    public static void multiplyMatrix(MatrixStack stack, ModelBone bone)
    {
        matrix.set(stack.getModelMatrix());
        matrix.transpose();

        MatrixUtils.matrixToFloat(MatrixUtils.floats, matrix);
        MatrixUtils.buffer.clear();
        MatrixUtils.buffer.put(MatrixUtils.floats);
        MatrixUtils.buffer.flip();

        Vector3f pivot = bone.initial.translate;

        GlStateManager.multMatrix(MatrixUtils.buffer);
        GlStateManager.translate(pivot.x / 16, pivot.y / 16, pivot.z / 16);
    }

    public void setBoneName(String boneName)
    {
        this.boneName = boneName;
    }

    @Override
    public boolean renderBone(BufferBuilder builder, MatrixStack stack, ModelBone bone)
    {
        if (bone.id.equals(this.boneName))
        {
            multiplyMatrix(stack, bone);

            return true;
        }

        return false;
    }
}