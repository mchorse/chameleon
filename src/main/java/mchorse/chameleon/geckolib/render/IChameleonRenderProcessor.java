package mchorse.chameleon.geckolib.render;

import net.minecraft.client.renderer.BufferBuilder;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.util.MatrixStack;

public interface IChameleonRenderProcessor
{
    public boolean renderBone(BufferBuilder builder, MatrixStack stack, GeoBone bone);
}