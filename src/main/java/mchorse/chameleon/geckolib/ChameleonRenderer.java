package mchorse.chameleon.geckolib;

import mchorse.mclib.utils.MatrixUtils;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoCube;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.geo.render.built.GeoQuad;
import software.bernie.geckolib3.geo.render.built.GeoVertex;
import software.bernie.geckolib3.util.MatrixStack;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

@SideOnly(Side.CLIENT)
public class ChameleonRenderer
{
	private static final MatrixStack MATRIX_STACK = new MatrixStack();

	public static void render(GeoModel model)
	{
		GlStateManager.disableCull();
		GlStateManager.enableRescaleNormal();

		BufferBuilder builder = Tessellator.getInstance().getBuffer();
		builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);

		for (GeoBone bone : model.topLevelBones)
		{
			renderRecursively(builder, bone, 1F, 1F, 1F, 1F);
		}

		Tessellator.getInstance().draw();

		GlStateManager.disableRescaleNormal();
		GlStateManager.enableCull();
	}

	private static void renderRecursively(BufferBuilder builder, GeoBone bone, float red, float green, float blue, float alpha)
	{
		MATRIX_STACK.push();
		MATRIX_STACK.translate(bone);
		MATRIX_STACK.moveToPivot(bone);
		MATRIX_STACK.rotate(bone);
		MATRIX_STACK.scale(bone);
		MATRIX_STACK.moveBackFromPivot(bone);

		if (!bone.isHidden)
		{
			for (GeoCube cube : bone.childCubes)
			{
				renderCube(builder, cube, red, green, blue, alpha);
			}

			for (GeoBone childBone : bone.childBones)
			{
				renderRecursively(builder, childBone, red, green, blue, alpha);
			}
		}

		MATRIX_STACK.pop();
	}

	private static void renderCube(BufferBuilder builder, GeoCube cube, float red, float green, float blue, float alpha)
	{
		MATRIX_STACK.moveToPivot(cube);
		MATRIX_STACK.rotate(cube);
		MATRIX_STACK.moveBackFromPivot(cube);
		GeoQuad[] quads = cube.quads;

		for (GeoQuad quad : quads)
		{
			Vector3f normal = new Vector3f(quad.normal.getX(), quad.normal.getY(), quad.normal.getZ());

			MATRIX_STACK.getNormalMatrix().transform(normal);

			if ((cube.size.y == 0.0F || cube.size.z == 0.0F) && normal.getX() < 0.0F)
			{
				normal.x *= -1.0F;
			}

			if ((cube.size.x == 0.0F || cube.size.z == 0.0F) && normal.getY() < 0.0F)
			{
				normal.y *= -1.0F;
			}

			if ((cube.size.x == 0.0F || cube.size.y == 0.0F) && normal.getZ() < 0.0F)
			{
				normal.z *= -1.0F;
			}

			for (GeoVertex vertex : quad.vertices)
			{
				Vector4f vector4f = new Vector4f(vertex.position.getX(), vertex.position.getY(), vertex.position.getZ(), 1.0F);

				MATRIX_STACK.getModelMatrix().transform(vector4f);
				builder.pos(vector4f.getX(), vector4f.getY(), vector4f.getZ()).tex(vertex.textureU, vertex.textureV).color(red, green, blue, alpha).normal(normal.getX(), normal.getY(), normal.getZ()).endVertex();
			}
		}
	}

	public static boolean postRender(GeoModel model, String boneName)
	{
		for (GeoBone bone : model.topLevelBones)
		{
			if (postRenderRecursively(bone, boneName))
			{
				return true;
			}
		}

		return false;
	}

	private static boolean postRenderRecursively(GeoBone bone, String boneName)
	{
		MATRIX_STACK.push();
		MATRIX_STACK.translate(bone);
		MATRIX_STACK.moveToPivot(bone);
		MATRIX_STACK.rotate(bone);
		MATRIX_STACK.scale(bone);
		MATRIX_STACK.moveBackFromPivot(bone);

		if (!bone.isHidden)
		{
			for (GeoBone childBone : bone.childBones)
			{
				if (childBone.name.equals(boneName))
				{
					Matrix4f matrix4f = new Matrix4f(MATRIX_STACK.getModelMatrix());

					matrix4f.transpose();
					MatrixUtils.matrixToFloat(MatrixUtils.floats, matrix4f);

					MatrixUtils.buffer.clear();
					MatrixUtils.buffer.put(MatrixUtils.floats);
					MatrixUtils.buffer.flip();

					GlStateManager.multMatrix(MatrixUtils.buffer);

					MATRIX_STACK.pop();

					return true;
				}

				if (postRenderRecursively(childBone, boneName))
				{
					MATRIX_STACK.pop();

					return true;
				}
			}
		}

		MATRIX_STACK.pop();

		return false;
	}
}