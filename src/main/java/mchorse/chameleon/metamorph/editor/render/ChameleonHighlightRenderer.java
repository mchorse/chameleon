package mchorse.chameleon.metamorph.editor.render;

import mchorse.chameleon.geckolib.render.ChameleonPostRenderer;
import mchorse.chameleon.geckolib.render.IChameleonRenderProcessor;
import mchorse.mclib.client.Draw;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoCube;
import software.bernie.geckolib3.geo.render.built.GeoQuad;
import software.bernie.geckolib3.geo.render.built.GeoVertex;
import software.bernie.geckolib3.util.MatrixStack;

import javax.vecmath.Vector4f;

@SideOnly(Side.CLIENT)
public class ChameleonHighlightRenderer implements IChameleonRenderProcessor
{
	private String boneName;

	public void setBoneName(String boneName)
	{
		this.boneName = boneName;
	}

	@Override
	public boolean renderBone(BufferBuilder builder, MatrixStack stack, GeoBone bone)
	{
		if (bone.name.equals(this.boneName))
		{
			builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);

			for (GeoCube cube : bone.childCubes)
			{
				this.renderCubeForHighlight(builder, stack, cube);
			}

			Tessellator.getInstance().draw();

			GlStateManager.pushMatrix();
			ChameleonPostRenderer.multiplyMatrix(stack, bone);

			Draw.axis(0.25F * 1.5F);

			GlStateManager.popMatrix();

			return true;
		}

		return false;
	}

	private void renderCubeForHighlight(BufferBuilder builder, MatrixStack stack, GeoCube cube)
	{
		stack.moveToPivot(cube);
		stack.rotate(cube);
		stack.moveBackFromPivot(cube);
		GeoQuad[] quads = cube.quads;

		for (GeoQuad quad : quads)
		{
			for (GeoVertex vertex : quad.vertices)
			{
				Vector4f vector4f = new Vector4f(vertex.position.getX(), vertex.position.getY(), vertex.position.getZ(), 1.0F);

				stack.getModelMatrix().transform(vector4f);
				builder.pos(vector4f.getX(), vector4f.getY(), vector4f.getZ()).endVertex();
			}
		}
	}
}