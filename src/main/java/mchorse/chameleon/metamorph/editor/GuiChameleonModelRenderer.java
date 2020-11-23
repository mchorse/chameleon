package mchorse.chameleon.metamorph.editor;

import mchorse.chameleon.geckolib.ChameleonModel;
import mchorse.chameleon.metamorph.ChameleonMorph;
import mchorse.mclib.client.Draw;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.utils.MatrixUtils;
import mchorse.metamorph.client.gui.creative.GuiMorphRenderer;
import net.minecraft.client.Minecraft;
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

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector4f;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiChameleonModelRenderer extends GuiMorphRenderer
{
	private static final MatrixStack MATRIX_STACK = new MatrixStack();

	public String boneName = "";

	public GuiChameleonModelRenderer(Minecraft mc)
	{
		super(mc);
	}

	@Override
	protected void drawUserModel(GuiContext context)
	{
		super.drawUserModel(context);
		this.drawHighlight(context);
		this.tryPicking(context);
	}

	private void drawHighlight(GuiContext context)
	{
		if (!(this.morph instanceof ChameleonMorph))
		{
			return;
		}

		ChameleonModel model = ((ChameleonMorph) this.morph).getModel();

		if (model == null || this.boneName.isEmpty())
		{
			return;
		}

		GlStateManager.disableLighting();
		GlStateManager.disableTexture2D();
		GlStateManager.disableDepth();
		GlStateManager.pushMatrix();
		GlStateManager.color(0, 0.5F, 1, 0.33F);
		GlStateManager.rotate(180, 0, 1, 0);

		for (GeoBone bone : model.model.topLevelBones)
		{
			if (this.renderRecursivelyForHighlight(bone))
			{
				break;
			}
		}

		GlStateManager.popMatrix();
		GlStateManager.enableTexture2D();
		GlStateManager.enableDepth();
		GlStateManager.enableLighting();
	}

	private boolean renderRecursivelyForHighlight(GeoBone bone)
	{
		MATRIX_STACK.push();
		MATRIX_STACK.translate(bone);
		MATRIX_STACK.moveToPivot(bone);
		MATRIX_STACK.rotate(bone);
		MATRIX_STACK.scale(bone);
		MATRIX_STACK.moveBackFromPivot(bone);

		if (!bone.isHidden)
		{
			if (bone.name.equals(this.boneName))
			{
				BufferBuilder builder = Tessellator.getInstance().getBuffer();
				builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);

				for (GeoCube cube : bone.childCubes)
				{
					renderCubeForHighlight(builder, cube);
				}

				Tessellator.getInstance().draw();

				GlStateManager.pushMatrix();
				Matrix4f matrix4f = new Matrix4f(MATRIX_STACK.getModelMatrix());

				matrix4f.transpose();
				MatrixUtils.matrixToFloat(MatrixUtils.floats, matrix4f);

				MatrixUtils.buffer.clear();
				MatrixUtils.buffer.put(MatrixUtils.floats);
				MatrixUtils.buffer.flip();

				GlStateManager.multMatrix(MatrixUtils.buffer);
				GlStateManager.translate(bone.rotationPointX / 16.0F, bone.rotationPointY / 16.0F, bone.rotationPointZ / 16.0F);

				Draw.axis(0.25F * 1.5F);

				GlStateManager.popMatrix();

				MATRIX_STACK.pop();

				return true;
			}

			for (GeoBone childBone : bone.childBones)
			{
				if (this.renderRecursivelyForHighlight(childBone))
				{
					MATRIX_STACK.pop();

					return true;
				}
			}
		}

		MATRIX_STACK.pop();

		return false;
	}

	private void renderCubeForHighlight(BufferBuilder builder, GeoCube cube)
	{
		MATRIX_STACK.moveToPivot(cube);
		MATRIX_STACK.rotate(cube);
		MATRIX_STACK.moveBackFromPivot(cube);
		GeoQuad[] quads = cube.quads;

		for (GeoQuad quad : quads)
		{
			for (GeoVertex vertex : quad.vertices)
			{
				Vector4f vector4f = new Vector4f(vertex.position.getX(), vertex.position.getY(), vertex.position.getZ(), 1.0F);

				MATRIX_STACK.getModelMatrix().transform(vector4f);
				builder.pos(vector4f.getX(), vector4f.getY(), vector4f.getZ()).endVertex();
			}
		}
	}

	@Override
	protected void drawForStencil(GuiContext context)
	{
		if (!(this.morph instanceof ChameleonMorph))
		{
			return;
		}

		ChameleonModel model = ((ChameleonMorph) this.morph).getModel();

		if (model == null)
		{
			return;
		}

		List<String> bones = model.getBoneNames();

		GlStateManager.disableCull();
		GlStateManager.disableTexture2D();
		GlStateManager.pushMatrix();
		GlStateManager.color(1, 1, 1, 1);
		GlStateManager.rotate(180, 0, 1, 0);

		for (GeoBone bone : model.model.topLevelBones)
		{
			this.renderRecursivelyForStencil(bones, bone);
		}

		GlStateManager.popMatrix();
		GlStateManager.enableTexture2D();
		GlStateManager.enableCull();
	}

	private void renderRecursivelyForStencil(List<String> bones, GeoBone bone)
	{
		MATRIX_STACK.push();
		MATRIX_STACK.translate(bone);
		MATRIX_STACK.moveToPivot(bone);
		MATRIX_STACK.rotate(bone);
		MATRIX_STACK.scale(bone);
		MATRIX_STACK.moveBackFromPivot(bone);

		if (!bone.isHidden)
		{
			int index = bones.indexOf(bone.name) + 1;
			GL11.glStencilFunc(GL11.GL_ALWAYS, index, -1);

			BufferBuilder builder = Tessellator.getInstance().getBuffer();
			builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);

			for (GeoCube cube : bone.childCubes)
			{
				renderCube(builder, cube);
			}

			Tessellator.getInstance().draw();

			for (GeoBone childBone : bone.childBones)
			{
				renderRecursivelyForStencil(bones, childBone);
			}
		}

		MATRIX_STACK.pop();
	}

	private void renderCube(BufferBuilder builder, GeoCube cube)
	{
		MATRIX_STACK.moveToPivot(cube);
		MATRIX_STACK.rotate(cube);
		MATRIX_STACK.moveBackFromPivot(cube);
		GeoQuad[] quads = cube.quads;

		for (GeoQuad quad : quads)
		{
			for (GeoVertex vertex : quad.vertices)
			{
				Vector4f vector4f = new Vector4f(vertex.position.getX(), vertex.position.getY(), vertex.position.getZ(), 1.0F);

				MATRIX_STACK.getModelMatrix().transform(vector4f);
				builder.pos(vector4f.getX(), vector4f.getY(), vector4f.getZ()).endVertex();
			}
		}
	}

	@Override
	protected String getStencilValue(int value)
	{
		if (value == 0)
		{
			return null;
		}

		value -= 1;

		if (this.morph instanceof ChameleonMorph)
		{
			ChameleonMorph morph = (ChameleonMorph) this.morph;
			ChameleonModel model = morph.getModel();

			if (model != null)
			{
				List<String> bones = model.getBoneNames();

				if (value >= 0 && value < bones.size())
				{
					return bones.get(value);
				}
			}
		}

		return super.getStencilValue(value);
	}
}