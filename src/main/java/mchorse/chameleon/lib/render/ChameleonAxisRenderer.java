package mchorse.chameleon.lib.render;

import mchorse.chameleon.lib.data.model.ModelBone;
import mchorse.chameleon.lib.utils.MatrixStack;
import mchorse.mclib.client.Draw;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

@SideOnly(Side.CLIENT)
public class ChameleonAxisRenderer implements IChameleonRenderProcessor
{
    @Override
    public boolean renderBone(BufferBuilder builder, MatrixStack stack, ModelBone bone)
    {
        GlStateManager.pushMatrix();
        ChameleonPostRenderer.multiplyMatrix(stack, bone);

        Draw.axis(0.2F);

        GlStateManager.popMatrix();

        return false;
    }
}