package mchorse.chameleon.lib.render;

import mchorse.chameleon.lib.data.model.ModelBone;
import mchorse.chameleon.lib.utils.MatrixStack;
import net.minecraft.client.renderer.BufferBuilder;

public interface IChameleonRenderProcessor
{
    public boolean renderBone(BufferBuilder builder, MatrixStack stack, ModelBone bone);
}