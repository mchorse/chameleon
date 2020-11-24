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

	/* Specific utility methods */

	/**
	 * Just render given model
	 *
	 * The texture should be bind beforehand
	 */
	public static void render(GeoModel model)
	{
		CUBE_RENDERER.setColor(1F, 1F, 1F, 1F);

		GlStateManager.disableCull();
		GlStateManager.enableRescaleNormal();
		GlStateManager.enableBlend();

		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

		BufferBuilder builder = Tessellator.getInstance().getBuffer();
		builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);

		processRenderModel(CUBE_RENDERER, builder, MATRIX_STACK, model);

		Tessellator.getInstance().draw();

		GlStateManager.disableBlend();
		GlStateManager.disableRescaleNormal();
		GlStateManager.enableCull();
	}

	/**
	 * Post render (multiply the current matrix stack by bone's
	 * transformations by given name)
	 */
	public static boolean postRender(GeoModel model, String boneName)
	{
		POST_RENDERER.setBoneName(boneName);

		/* Buffer builder isn't used in this render processor, but just in case */
		return processRenderModel(POST_RENDERER, Tessellator.getInstance().getBuffer(), MATRIX_STACK, model);
	}

	/* Generic render methods */

	/**
	 * Process/render given model
	 *
	 * This method recursively goes through all bones in the model, and
	 * applies given render processor. Processor may return true from its
	 * sole method which means that iteration should be halted
	 */
	public static boolean processRenderModel(IChameleonRenderProcessor renderProcessor, BufferBuilder builder, MatrixStack stack, GeoModel model)
	{
		for (GeoBone bone : model.topLevelBones)
		{
			if (processRenderRecursively(renderProcessor, builder, stack, bone))
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Apply the render processor, recursively
	 */
	private static boolean processRenderRecursively(IChameleonRenderProcessor renderProcessor, BufferBuilder builder, MatrixStack stack, GeoBone bone)
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
				if (processRenderRecursively(renderProcessor, builder, stack, childBone))
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