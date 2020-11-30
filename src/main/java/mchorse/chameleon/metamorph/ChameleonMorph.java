package mchorse.chameleon.metamorph;

import mchorse.chameleon.ClientProxy;
import mchorse.chameleon.animation.Animator;
import mchorse.chameleon.geckolib.ChameleonModel;
import mchorse.chameleon.geckolib.render.ChameleonRenderer;
import mchorse.chameleon.metamorph.pose.AnimatedPose;
import mchorse.chameleon.metamorph.pose.AnimatorPoseTransform;
import mchorse.mclib.client.render.RenderLightmap;
import mchorse.mclib.utils.Interpolations;
import mchorse.mclib.utils.MatrixUtils;
import mchorse.mclib.utils.resources.RLUtils;
import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.bodypart.BodyPart;
import mchorse.metamorph.bodypart.BodyPartManager;
import mchorse.metamorph.bodypart.IBodyPartProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.core.snapshot.BoneSnapshot;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoModel;

import java.util.Objects;

public class ChameleonMorph extends AbstractMorph implements IBodyPartProvider
{
	public ResourceLocation skin;
	public AnimatedPose pose;
	public BodyPartManager parts = new BodyPartManager();

	/**
	 * Cached key value
	 */
	private String key;

	@SideOnly(Side.CLIENT)
	private Animator animator;

	@SideOnly(Side.CLIENT)
	protected Animator getAnimator()
	{
		if (this.animator == null)
		{
			ChameleonModel model = this.getModel();

			this.animator = new Animator(model == null ? null : model.animation);
		}

		return this.animator;
	}

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
		GlStateManager.enableDepth();
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, 0);
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.rotate(45.0F, -1.0F, 0.0F, 0.0F);
		GlStateManager.rotate(135.0F, 0.0F, -1.0F, 0.0F);
		GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);

		RenderHelper.enableStandardItemLighting();

		this.renderModel(target, 0F);

		RenderHelper.disableStandardItemLighting();

		GlStateManager.popMatrix();
		GlStateManager.disableDepth();
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

		this.applyPose(model, this.getAnimator().applyActions(target, model, partialTicks));

		/* Render the model */
		if (this.skin != null)
		{
			Minecraft.getMinecraft().renderEngine.bindTexture(this.skin);
		}

		boolean hurtLight = RenderLightmap.set(target, partialTicks);

		ChameleonRenderer.render(model);

		if (hurtLight)
		{
			RenderLightmap.unset();
		}

		/* Render body parts */
		GlStateManager.color(1, 1, 1);

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
	}

	private void applyPose(GeoModel model, boolean applied)
	{
		for (GeoBone bone : model.topLevelBones)
		{
			this.applyPose(bone, this.pose, applied);
		}
	}

	private void applyPose(GeoBone bone, AnimatedPose pose, boolean applied)
	{
		if (pose != null && pose.bones.containsKey(bone.name))
		{
			AnimatorPoseTransform transform = pose.bones.get(bone.name);
			BoneSnapshot snapshot = bone.getInitialSnapshot();
			float factor = !pose.animated || !applied ? 0 : transform.fixed;

			bone.setPositionX(Interpolations.lerp(snapshot.positionOffsetX, bone.getPositionX(), factor) + transform.x);
			bone.setPositionY(Interpolations.lerp(snapshot.positionOffsetY, bone.getPositionY(), factor) + transform.y);
			bone.setPositionZ(Interpolations.lerp(snapshot.positionOffsetZ, bone.getPositionZ(), factor) + transform.z);

			bone.setRotationX(Interpolations.lerp(snapshot.rotationValueX, bone.getRotationX(), factor) + transform.rotateX);
			bone.setRotationY(Interpolations.lerp(snapshot.rotationValueY, bone.getRotationY(), factor) + transform.rotateY);
			bone.setRotationZ(Interpolations.lerp(snapshot.rotationValueZ, bone.getRotationZ(), factor) + transform.rotateZ);

			bone.setScaleX(Interpolations.lerp(snapshot.scaleValueX, bone.getScaleX(), factor) + (transform.scaleX - 1));
			bone.setScaleY(Interpolations.lerp(snapshot.scaleValueY, bone.getScaleY(), factor) + (transform.scaleY - 1));
			bone.setScaleZ(Interpolations.lerp(snapshot.scaleValueZ, bone.getScaleZ(), factor) + (transform.scaleZ - 1));
		}
		else if (!applied)
		{
			BoneSnapshot snapshot = bone.getInitialSnapshot();

			bone.setPositionX(snapshot.positionOffsetX);
			bone.setPositionY(snapshot.positionOffsetY);
			bone.setPositionZ(snapshot.positionOffsetZ);

			bone.setRotationX(snapshot.rotationValueX);
			bone.setRotationY(snapshot.rotationValueY);
			bone.setRotationZ(snapshot.rotationValueZ);

			bone.setScaleX(snapshot.scaleValueX);
			bone.setScaleY(snapshot.scaleValueY);
			bone.setScaleZ(snapshot.scaleValueZ);
		}

		for (GeoBone childBone : bone.childBones)
		{
			this.applyPose(childBone, pose, applied);
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

		if (target.world.isRemote)
		{
			this.updateClient(target);
		}
	}

	@SideOnly(Side.CLIENT)
	private void updateClient(EntityLivingBase target)
	{
		this.getAnimator().update(target);
	}

	@Override
	public boolean equals(Object obj)
	{
		boolean result = super.equals(obj);

		if (obj instanceof ChameleonMorph)
		{
			ChameleonMorph morph = (ChameleonMorph) obj;

			result = result && Objects.equals(morph.skin, this.skin);
			result = result && Objects.equals(morph.pose, this.pose);
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

			if (morph.pose != null)
			{
				this.pose = morph.pose.clone();
			}

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

		if (this.pose != null)
		{
			tag.setTag("Pose", this.pose.toNBT());
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

		if (tag.hasKey("Pose", Constants.NBT.TAG_COMPOUND))
		{
			this.pose = new AnimatedPose();
			this.pose.fromNBT(tag.getCompoundTag("Pose"));
		}

		if (tag.hasKey("BodyParts", 9))
		{
			this.parts.fromNBT(tag.getTagList("BodyParts", 10));
		}
	}
}