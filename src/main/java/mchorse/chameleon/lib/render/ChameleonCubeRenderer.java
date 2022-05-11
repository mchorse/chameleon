package mchorse.chameleon.lib.render;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import mchorse.chameleon.lib.data.model.ModelBone;
import mchorse.chameleon.lib.data.model.ModelCube;
import mchorse.chameleon.lib.data.model.ModelQuad;
import mchorse.chameleon.lib.data.model.ModelVertex;
import mchorse.chameleon.lib.utils.MatrixStack;
import mchorse.chameleon.metamorph.pose.AnimatedPose;
import mchorse.chameleon.metamorph.pose.AnimatedPoseTransform;
import mchorse.mclib.utils.Interpolation;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Cube renderer
 *
 * Renders given bones from the model as cubes, fully
 */
@SideOnly(Side.CLIENT)
public class ChameleonCubeRenderer implements IChameleonRenderProcessor
{
    private float r;
    private float g;
    private float b;
    private float a;

    /* Temporary variables to avoid allocating and GC vectors */
    private Vector3f normal = new Vector3f();
    private Vector4f vertex = new Vector4f();

    @Override
    public boolean renderBone(BufferBuilder builder, MatrixStack stack, ModelBone bone)
    {
        int lightX = (int) OpenGlHelper.lastBrightnessX;
        int lightY = (int) OpenGlHelper.lastBrightnessY;

        this.r = bone.color.r;
        this.g = bone.color.g;
        this.b = bone.color.b;
        this.a = bone.color.a;

        if (bone.absoluteBrightness)
        {
            lightX = 0;
        }

        lightX = (int) Interpolation.LINEAR.interpolate(lightX, 240, bone.glow);

        for (ModelCube cube : bone.cubes)
        {
            renderCube(builder, stack, cube, lightX, lightY);
        }

        return false;
    }

    private void renderCube(BufferBuilder builder, MatrixStack stack, ModelCube cube, int lightX, int lightY)
    {
        stack.push();
        stack.moveToCubePivot(cube);
        stack.rotateCube(cube);
        stack.moveBackFromCubePivot(cube);

        for (ModelQuad quad : cube.quads)
        {
            this.normal.set(quad.normal.x, quad.normal.y, quad.normal.z);
            stack.getNormalMatrix().transform(this.normal);

            /* For 0 sized cubes on either axis, to avoid getting dark shading on models
             * which didn't correctly setup the UV faces.
             *
             * For example two wings, first wing uses top face for texturing the flap,
             * and second wing uses bottom face as a flap. In the end, the second wing
             * will appear dark shaded without this fix.
             */
            if (this.normal.getX() < 0 && (cube.size.y == 0 || cube.size.z == 0)) this.normal.x *= -1;
            if (this.normal.getY() < 0 && (cube.size.x == 0 || cube.size.z == 0)) this.normal.y *= -1;
            if (this.normal.getZ() < 0 && (cube.size.x == 0 || cube.size.y == 0)) this.normal.z *= -1;

            for (ModelVertex vertex : quad.vertices)
            {
                this.vertex.set(vertex.position);
                this.vertex.w = 1;
                stack.getModelMatrix().transform(this.vertex);

                builder.pos(this.vertex.x, this.vertex.y, this.vertex.z)
                    .color(this.r, this.g, this.b, this.a)
                    .tex(vertex.uv.x, vertex.uv.y)
                    .lightmap(lightY, lightX)
                    .normal(this.normal.x, this.normal.y, this.normal.z)
                    .endVertex();
            }
        }

        stack.pop();
    }
}