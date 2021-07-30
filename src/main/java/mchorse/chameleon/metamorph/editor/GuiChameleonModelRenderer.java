package mchorse.chameleon.metamorph.editor;

import mchorse.chameleon.lib.ChameleonModel;
import mchorse.chameleon.lib.render.ChameleonRenderer;
import mchorse.chameleon.lib.utils.MatrixStack;
import mchorse.chameleon.metamorph.ChameleonMorph;
import mchorse.chameleon.metamorph.editor.render.ChameleonHighlightRenderer;
import mchorse.chameleon.metamorph.editor.render.ChameleonStencilRenderer;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.metamorph.client.gui.creative.GuiMorphRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiChameleonModelRenderer extends GuiMorphRenderer
{
    private static final MatrixStack MATRIX_STACK = new MatrixStack();
    private static final ChameleonStencilRenderer STENCIL_RENDERER = new ChameleonStencilRenderer();
    private static final ChameleonHighlightRenderer HIGHLIGHT_RENDERER = new ChameleonHighlightRenderer();

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

        float scale = ((ChameleonMorph) this.morph).scale;

        GlStateManager.enableBlend();
        GlStateManager.disableLighting();
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.pushMatrix();
        GlStateManager.color(0, 0.5F, 1, 0.33F);
        GlStateManager.rotate(180 - (this.customEntity ? this.entityYawBody : 0), 0, 1, 0);
        GlStateManager.scale(scale, scale, scale);

        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        HIGHLIGHT_RENDERER.setBoneName(this.boneName);

        ChameleonRenderer.processRenderModel(HIGHLIGHT_RENDERER, Tessellator.getInstance().getBuffer(), MATRIX_STACK, model.model);

        GlStateManager.popMatrix();
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
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

        float scale = ((ChameleonMorph) this.morph).scale;

        GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT);

        GlStateManager.enableDepth();
        GlStateManager.disableCull();
        GlStateManager.disableTexture2D();
        GlStateManager.pushMatrix();
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.rotate(180 - (this.customEntity ? this.entityYawBody : 0), 0, 1, 0);
        GlStateManager.scale(scale, scale, scale);

        STENCIL_RENDERER.setBones(model.getBoneNames());

        ChameleonRenderer.processRenderModel(STENCIL_RENDERER, Tessellator.getInstance().getBuffer(), MATRIX_STACK, model.model);

        GlStateManager.popMatrix();
        GlStateManager.enableTexture2D();
        GlStateManager.enableCull();
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