package mchorse.chameleon.metamorph.editor.render;

import mchorse.chameleon.lib.data.model.ModelBone;
import mchorse.chameleon.lib.data.model.ModelCube;
import mchorse.chameleon.lib.data.model.ModelQuad;
import mchorse.chameleon.lib.data.model.ModelVertex;
import mchorse.chameleon.lib.render.IChameleonRenderProcessor;
import mchorse.chameleon.lib.utils.MatrixStack;
import mchorse.mclib.utils.Interpolation;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

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
    private float r;
    private float g;
    private float b;
    private float a;

    public void setBones(List<String> bones)
    {
        this.bones = bones;
    }

    @Override
    public boolean renderBone(BufferBuilder builder, MatrixStack stack, ModelBone bone)
    {
        this.r = bone.color.r;
        this.g = bone.color.g;
        this.b = bone.color.b;
        this.a = bone.color.a;

        GL11.glStencilFunc(GL11.GL_ALWAYS, this.bones.indexOf(bone.id) + 1, -1);

        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

        for (ModelCube cube : bone.cubes)
        {
            renderCube(builder, stack, cube);
        }

        Tessellator.getInstance().draw();

        return false;
    }

    private void renderCube(BufferBuilder builder, MatrixStack stack, ModelCube cube)
    {
        stack.push();
        stack.moveToCubePivot(cube);
        stack.rotateCube(cube);
        stack.moveBackFromCubePivot(cube);

        for (ModelQuad quad : cube.quads)
        {
            for (ModelVertex vertex : quad.vertices)
            {
                this.vertex.set(vertex.position);
                this.vertex.w = 1;
                stack.getModelMatrix().transform(this.vertex);

                builder.pos(this.vertex.getX(), this.vertex.getY(), this.vertex.getZ())
                    .tex(vertex.uv.x, vertex.uv.y)
                    .color(this.r, this.g, this.b, this.a)
                    .endVertex();
            }
        }

        stack.pop();
    }
}