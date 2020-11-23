package mchorse.chameleon.metamorph;

import mchorse.chameleon.ClientProxy;
import mchorse.chameleon.client.RenderLightmap;
import mchorse.chameleon.geckolib.ChameleonAnimator;
import mchorse.chameleon.geckolib.ChameleonModel;
import mchorse.chameleon.geckolib.ChameleonRenderer;
import mchorse.mclib.client.Draw;
import mchorse.mclib.client.gui.framework.elements.GuiModelRenderer;
import mchorse.mclib.utils.Interpolations;
import mchorse.mclib.utils.MatrixUtils;
import mchorse.mclib.utils.resources.RLUtils;
import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.bodypart.BodyPart;
import mchorse.metamorph.bodypart.BodyPartManager;
import mchorse.metamorph.bodypart.GuiBodyPartEditor;
import mchorse.metamorph.bodypart.IBodyPartProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.core.builder.Animation;
import software.bernie.geckolib3.geo.render.built.GeoModel;

import java.util.Objects;

public class ChameleonMorph extends AbstractMorph implements IBodyPartProvider
{
	public ResourceLocation skin;
	public BodyPartManager parts = new BodyPartManager();

	/**
	 * Cached key value
	 */
	private String key;

	private int tick;

	@Override
	public BodyPartManager getBodyPart()
	{
		return this.parts;
	}

	public String getKey()
	{
		if (this.key == null)
		{
			this.key = this.name.replaceAll("^chameleon\\.", "");
		}

		return this.key;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderOnScreen(EntityPlayer target, int x, int y, float scale, float alpha)
	{
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, 0);
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.rotate(45.0F, -1.0F, 0.0F, 0.0F);
		GlStateManager.rotate(135.0F, 0.0F, -1.0F, 0.0F);
		GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);

		this.renderModel(target, 0F);

		GlStateManager.popMatrix();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void render(EntityLivingBase target, double x, double y, double z, float entityYaw, float partialTicks)
	{
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);

		boolean captured = MatrixUtils.captureMatrix();
		float renderYawOffset = Interpolations.lerp(target.prevRenderYawOffset, target.renderYawOffset, partialTicks);

		GlStateManager.rotate(-renderYawOffset + 180, 0, 1, 0);
		GlStateManager.color(1, 1, 1, 1);

		this.renderModel(target, partialTicks);

		if (captured)
		{
			MatrixUtils.releaseMatrix();
		}

		GlStateManager.popMatrix();
	}

	@SideOnly(Side.CLIENT)
	private void renderModel(EntityLivingBase target, float partialTicks)
	{
		ChameleonModel chameleonModel = this.getModel();

		if (chameleonModel == null)
		{
			return;
		}

		GeoModel model = chameleonModel.model;
		Animation animation = chameleonModel.animation.getAnimation("animation.bat.fly");

		if (animation != null)
		{
			ChameleonAnimator.animate(target, model, animation, GuiModelRenderer.isRendering() ? 0F : this.tick + partialTicks);
		}

		if (this.skin != null)
		{
			Minecraft.getMinecraft().renderEngine.bindTexture(this.skin);
		}

		boolean hurtLight = RenderLightmap.set(target, partialTicks);

		ChameleonRenderer.render(model);

		this.parts.initBodyParts();

		for (BodyPart part : this.parts.parts)
		{
			GlStateManager.pushMatrix();

			if (ChameleonRenderer.postRender(model, part.limb))
			{
				part.render(target, partialTicks);
			}

			GlStateManager.popMatrix();
		}

		if (hurtLight)
		{
			RenderLightmap.unset();
		}
	}

	@SideOnly(Side.CLIENT)
	public ChameleonModel getModel()
	{
		return ClientProxy.chameleonModels.get(this.getKey());
	}

	@Override
	public void update(EntityLivingBase target)
	{
		super.update(target);

		this.tick++;
	}

	@Override
	public boolean equals(Object obj)
	{
		boolean result = super.equals(obj);

		if (obj instanceof ChameleonMorph)
		{
			ChameleonMorph morph = (ChameleonMorph) obj;

			result = result && Objects.equals(morph.skin, this.skin);
			result = result && Objects.equals(morph.parts, this.parts);
		}

		return result;
	}

	@Override
	public AbstractMorph create()
	{
		return new ChameleonMorph();
	}

	@Override
	public void copy(AbstractMorph from)
	{
		super.copy(from);

		if (from instanceof ChameleonMorph)
		{
			ChameleonMorph morph = (ChameleonMorph) from;

			this.skin = RLUtils.clone(morph.skin);
			this.parts.copy(morph.parts);
		}
	}

	@Override
	public float getWidth(EntityLivingBase target)
	{
		return 0.6F;
	}

	@Override
	public float getHeight(EntityLivingBase target)
	{
		return 1.8F;
	}

	@Override
	public void reset()
	{
		super.reset();

		this.key = null;
		this.skin = null;
		this.parts.reset();
	}

	@Override
	public void toNBT(NBTTagCompound tag)
	{
		super.toNBT(tag);

		if (this.skin != null)
		{
			tag.setTag("Skin", RLUtils.writeNbt(this.skin));
		}

		NBTTagList bodyParts = this.parts.toNBT();

		if (bodyParts != null)
		{
			tag.setTag("BodyParts", bodyParts);
		}
	}

	@Override
	public void fromNBT(NBTTagCompound tag)
	{
		super.fromNBT(tag);

		if (tag.hasKey("Skin"))
		{
			this.skin = RLUtils.create(tag.getTag("Skin"));
		}

		if (tag.hasKey("BodyParts", 9))
		{
			this.parts.fromNBT(tag.getTagList("BodyParts", 10));
		}
	}
}