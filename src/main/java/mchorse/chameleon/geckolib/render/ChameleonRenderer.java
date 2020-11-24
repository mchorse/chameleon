package mchorse.chameleon.geckolib.render;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.util.MatrixStack;

@SideOnly(Side.CLIENT)
public class ChameleonRenderer
{
	private static final MatrixStack MATRIX_STACK = new MatrixStack();
	private static final ChameleonCubeRenderer CUBE_RENDERER = new ChameleonCubeRenderer();
	private static final ChameleonPostRenderer POST_RENDERER = new ChameleonPostRenderer();

	public static void render(GeoModel model)
	{
		CUBE_RENDERER.setColor(1F, 1F, 1F, 1F);

		GlStateManager.disableCull();
		GlStateManager.enableRescaleNormal();

		BufferBuilder builder = Tessellator.getInstance().getBuffer();
		builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);

		renderProcessModel(CUBE_RENDERER, builder, MATRIX_STACK, model);

		Tessellator.getInstance().draw();

		GlStateManager.disableRescaleNormal();
		GlStateManager.enableCull();
	}

	public static boolean postRender(GeoModel model, String boneName)
	{
		POST_RENDERER.setBoneName(boneName);

		return renderProcessModel(POST_RENDERER, null, MATRIX_STACK, model);
	}

	public static boolean renderProcessModel(IChameleonRenderProcessor renderProcessor, BufferBuilder builder, MatrixStack stack, GeoModel model)
	{
		for (GeoBone bone : model.topLevelBones)
		{
			if (renderRecursively(renderProcessor, builder, stack, bone))
			{
				return true;
			}
		}

		return false;
	}

	private static boolean renderRecursively(IChameleonRenderProcessor renderProcessor, BufferBuilder builder, MatrixStack stack, GeoBone bone)
	{
		stack.push();
		stack.translate(bone);
		stack.moveToPivot(bone);
		stack.rotate(bone);
		stack.scale(bone);
		stack.moveBackFromPivot(bone);

		if (!bone.isHidden)
		{
			if (renderProcessor.renderBone(builder, stack, bone))
			{
				stack.pop();

				return true;
			}

			for (GeoBone childBone : bone.childBones)
			{
				if (renderRecursively(renderProcessor, builder, stack, childBone))
				{
					stack.pop();

					return true;
				}
			}
		}

		stack.pop();

		return false;
	}
}