package mchorse.chameleon.geckolib.render;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoCube;
import software.bernie.geckolib3.geo.render.built.GeoQuad;
import software.bernie.geckolib3.geo.render.built.GeoVertex;
import software.bernie.geckolib3.util.MatrixStack;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

@SideOnly(Side.CLIENT)
public class ChameleonCubeRenderer implements IChameleonRenderProcessor
{
	private float r;
	private float g;
	private float b;
	private float a;

	public void setColor(float r, float g, float b, float a)
	{
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}

	@Override
	public boolean renderBone(BufferBuilder builder, MatrixStack stack, GeoBone bone)
	{
		for (GeoCube cube : bone.childCubes)
		{
			renderCube(builder, stack, cube);
		}

		return false;
	}

	private void renderCube(BufferBuilder builder, MatrixStack stack, GeoCube cube)
	{
		stack.moveToPivot(cube);
		stack.rotate(cube);
		stack.moveBackFromPivot(cube);
		GeoQuad[] quads = cube.quads;

		for (GeoQuad quad : quads)
		{
			Vector3f normal = new Vector3f(quad.normal.getX(), quad.normal.getY(), quad.normal.getZ());

			stack.getNormalMatrix().transform(normal);

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

				stack.getModelMatrix().transform(vector4f);
				builder.pos(vector4f.getX(), vector4f.getY(), vector4f.getZ()).tex(vertex.textureU, vertex.textureV).color(this.r, this.g, this.b, this.a).normal(normal.getX(), normal.getY(), normal.getZ()).endVertex();
			}
		}
	}
}