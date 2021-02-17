package mchorse.chameleon.geckolib.render;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoCube;
import software.bernie.geckolib3.geo.render.built.GeoQuad;
import software.bernie.geckolib3.geo.render.built.GeoVertex;
import software.bernie.geckolib3.util.MatrixStack;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

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

    public void setColor(float r, float g, float b, float a)
    {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    @Override
    public boolean renderBone(BufferBuilder builder, MatrixStack stack, GeoBone bone)
    {
        for (GeoCube cube : bone.childCubes)
        {
            renderCube(builder, stack, cube);
        }

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
            this.normal.set(quad.normal.getX(), quad.normal.getY(), quad.normal.getZ());
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

            for (GeoVertex vertex : quad.vertices)
            {
                this.vertex.set(vertex.position);
                this.vertex.w = 1;
                stack.getModelMatrix().transform(this.vertex);

                builder.pos(this.vertex.getX(), this.vertex.getY(), this.vertex.getZ())
                    .tex(vertex.textureU, vertex.textureV)
                    .color(this.r, this.g, this.b, this.a)
                    .normal(this.normal.getX(), this.normal.getY(), this.normal.getZ())
                    .endVertex();
            }
        }

        stack.pop();
    }
}