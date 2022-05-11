package mchorse.chameleon.lib.render;

import mchorse.chameleon.lib.data.model.ModelBone;
import mchorse.chameleon.lib.data.model.Model;
import mchorse.chameleon.lib.utils.MatrixStack;
import mchorse.chameleon.metamorph.pose.AnimatedPose;
import mchorse.mclib.client.render.VertexBuilder;
import mchorse.mclib.utils.files.GlobalTree;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class ChameleonRenderer
{
    private static final MatrixStack MATRIX_STACK = new MatrixStack();
    private static final ChameleonCubeRenderer CUBE_RENDERER = new ChameleonCubeRenderer();
    private static final ChameleonPostRenderer POST_RENDERER = new ChameleonPostRenderer();
    private static final ChameleonAxisRenderer AXIS_RENDERER = new ChameleonAxisRenderer();

    /* Specific utility methods */

    /**
     * Just render given model
     *
     * The texture should be bind beforehand
     */
    public static void render(Model model)
    {
        GlStateManager.disableCull();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableBlend();

        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        BufferBuilder builder = Tessellator.getInstance().getBuffer();
        builder.begin(GL11.GL_QUADS, VertexBuilder.getFormat(true, true, true, true));

        processRenderModel(CUBE_RENDERER, builder, MATRIX_STACK, model);

        Tessellator.getInstance().draw();

        GlStateManager.disableBlend();
        GlStateManager.disableRescaleNormal();
        GlStateManager.enableCull();

        GlStateManager.disableLighting();
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();

        // processRenderModel(AXIS_RENDERER, null, MATRIX_STACK, model);

        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
    }

    /**
     * Post render (multiply the current matrix stack by bone's
     * transformations by given name)
     */
    public static boolean postRender(Model model, String boneName)
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
    public static boolean processRenderModel(IChameleonRenderProcessor renderProcessor, BufferBuilder builder, MatrixStack stack, Model model)
    {
        for (ModelBone bone : model.bones)
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
    private static boolean processRenderRecursively(IChameleonRenderProcessor renderProcessor, BufferBuilder builder, MatrixStack stack, ModelBone bone)
    {
        stack.push();
        stack.translateBone(bone);
        stack.moveToBonePivot(bone);
        stack.rotateBone(bone);
        stack.scaleBone(bone);
        stack.moveBackFromBonePivot(bone);

        if (bone.visible)
        {
            if (renderProcessor.renderBone(builder, stack, bone))
            {
                stack.pop();

                return true;
            }

            for (ModelBone childBone : bone.children)
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