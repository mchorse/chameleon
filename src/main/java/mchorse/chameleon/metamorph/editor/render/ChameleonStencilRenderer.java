package mchorse.chameleon.metamorph.editor.render;

import mchorse.chameleon.geckolib.render.IChameleonRenderProcessor;
import net.minecraft.client.renderer.BufferBuilder;
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
import java.util.List;

/**
 * Stencil render processor
 *
 * This bad boy is responsible for rendering given GeoModel for
 * stencil limb picking
 */
@SideOnly(Side.CLIENT)
public class ChameleonStencilRenderer implements IChameleonRenderProcessor
{
    private List<String> bones;
    private Vector4f vertex = new Vector4f();

    public void setBones(List<String> bones)
    {
        this.bones = bones;
    }

    @Override
    public boolean renderBone(BufferBuilder builder, MatrixStack stack, GeoBone bone)
    {
        GL11.glStencilFunc(GL11.GL_ALWAYS, this.bones.indexOf(bone.name) + 1, -1);

        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);

        for (GeoCube cube : bone.childCubes)
        {
            renderCube(builder, stack, cube);
        }

        Tessellator.getInstance().draw();

        return false;
    }

    private void renderCube(BufferBuilder builder, MatrixStack stack, GeoCube cube)
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