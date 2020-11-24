package mchorse.chameleon.geckolib.render;

import mchorse.mclib.utils.MatrixUtils;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.util.MatrixStack;

import javax.vecmath.Matrix4f;

@SideOnly(Side.CLIENT)
public class ChameleonPostRenderer implements IChameleonRenderProcessor
{
	private String boneName = "";

	/**
	 * Multiply given matrix stack onto OpenGL's matrix stack
	 */
	public static void multiplyMatrix(MatrixStack stack, GeoBone bone)
	{
		Matrix4f matrix4f = new Matrix4f(stack.getModelMatrix());

		matrix4f.transpose();
		MatrixUtils.matrixToFloat(MatrixUtils.floats, matrix4f);

		MatrixUtils.buffer.clear();
		MatrixUtils.buffer.put(MatrixUtils.floats);
		MatrixUtils.buffer.flip();

		GlStateManager.multMatrix(MatrixUtils.buffer);
		GlStateManager.translate(bone.rotationPointX / 16.0F, bone.rotationPointY / 16.0F, bone.rotationPointZ / 16.0F);
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