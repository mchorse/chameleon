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

/**
 * Highlight renderer
 *
 * This bad boy is responsible for rendering given model's bone
 * as a blue box highlight (in addition with axes identifiers)
 */
@SideOnly(Side.CLIENT)
public class ChameleonHighlightRenderer implements IChameleonRenderProcessor
{
    private String boneName;
    private Vector4f vertex = new Vector4f();

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
        stack.push();
        stack.moveToPivot(cube);
        stack.rotate(cube);
        stack.moveBackFromPivot(cube);

        for (GeoQuad quad : cube.quads)
        {
            for (GeoVertex vertex : quad.vertices)
            {
                this.vertex.set(vertex.position);
                this.vertex.w = 1;
                stack.getModelMatrix().transform(this.vertex);

                builder.pos(this.vertex.getX(), this.vertex.getY(), this.vertex.getZ()).endVertex();
            }
        }

        stack.pop();
    }
}