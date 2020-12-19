package mchorse.chameleon.metamorph;

import mchorse.chameleon.ClientProxy;
import mchorse.chameleon.animation.ActionsConfig;
import mchorse.chameleon.animation.Animator;
import mchorse.chameleon.geckolib.ChameleonAnimator;
import mchorse.chameleon.geckolib.ChameleonModel;
import mchorse.chameleon.geckolib.render.ChameleonRenderer;
import mchorse.chameleon.metamorph.pose.AnimatedPose;
import mchorse.chameleon.metamorph.pose.AnimatedPoseTransform;
import mchorse.chameleon.metamorph.pose.PoseAnimation;
import mchorse.mclib.client.render.RenderLightmap;
import mchorse.mclib.utils.Interpolations;
import mchorse.mclib.utils.MatrixUtils;
import mchorse.mclib.utils.resources.RLUtils;
import mchorse.metamorph.api.models.IMorphProvider;
import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.api.morphs.utils.Animation;
import mchorse.metamorph.api.morphs.utils.IAnimationProvider;
import mchorse.metamorph.api.morphs.utils.ISyncableMorph;
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

public class ChameleonMorph extends AbstractMorph implements IBodyPartProvider, ISyncableMorph, IAnimationProvider
{
	public ResourceLocation skin;
	public AnimatedPose pose;
	public ActionsConfig actions = new ActionsConfig();
	public BodyPartManager parts = new BodyPartManager();

	public float scale = 1F;
	public float scaleGui = 1F;
	private float lastScale = 1F;

	public PoseAnimation animation = new PoseAnimation();

	/**
	 * Cached key value
	 */
	private String key;

	@SideOnly(Side.CLIENT)
	private Animator animator;

	private boolean updateAnimator = false;

	public float getScale(float partialTick)
	{
		if (this.animation.isInProgress())
		{
			return this.animation.interp.interpolate(this.lastScale, this.scale, this.animation.getFactor(partialTick));
		}

		return this.scale;
	}

	@SideOnly(Side.CLIENT)
	protected Animator getAnimator()
	{
		if (this.animator == null)
		{
			ChameleonModel model = this.getModel();

			this.animator = new Animator(this, model == null ? null : model.animation);
		}

		return this.animator;
	}

	@Override
	public void pause(AbstractMorph previous, int offset)
	{
		this.animation.pause(offset);

		if (previous instanceof IMorphProvider)
		{
			previous = ((IMorphProvider) previous).getMorph();
		}

		AnimatedPose pose = null;

		if (previous instanceof ChameleonMorph)
		{
			pose = ((ChameleonMorph) previous).pose;

			if (pose != null)
			{
				pose = pose.clone();
			}
		}

		this.animation.last = pose == null ? (previous == null ? this.pose : new AnimatedPose()) : pose;
		this.parts.pause(previous, offset);
	}

	@Override
	public boolean isPaused()
	{
		return this.animation.paused;
	}

	@Override
	public Animation getAnimation()
	{
		return this.animation;
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
		scale *= this.scaleGui;

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
		float scale = this.getScale(partialTicks);

		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		GlStateManager.scale(scale, scale, scale);

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

		this.checkAnimator();

		GeoModel model = chameleonModel.model;

		ChameleonAnimator.resetPose(model);

		if (!chameleonModel.isStatic())
		{
			this.getAnimator().applyActions(target, model, partialTicks);
		}

		this.applyPose(model, partialTicks);

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

	@SideOnly(Side.CLIENT)
	private void checkAnimator()
	{
		if (this.updateAnimator)
		{
			this.updateAnimator = false;
			this.getAnimator().refresh();
		}
	}

	@SideOnly(Side.CLIENT)
	private void applyPose(GeoModel model, float partialTicks)
	{
		AnimatedPose pose = this.pose;
		boolean inProgress = this.animation.isInProgress();

		if (inProgress)
		{
			pose = this.animation.calculatePose(this.pose, this.getModel(), partialTicks);
		}

		for (GeoBone bone : model.topLevelBones)
		{
			this.applyPose(bone, pose);
		}
	}

	@SideOnly(Side.CLIENT)
	private void applyPose(GeoBone bone, AnimatedPose pose)
	{
		if (pose != null && pose.bones.containsKey(bone.name))
		{
			AnimatedPoseTransform transform = pose.bones.get(bone.name);
			BoneSnapshot snapshot = bone.getInitialSnapshot();
			float factor = transform.fixed * pose.animated;

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

		for (GeoBone childBone : bone.childBones)
		{
			this.applyPose(childBone, pose);
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
		this.animation.update();

		super.update(target);

		if (target.world.isRemote)
		{
			this.updateClient(target);
		}
	}

	@SideOnly(Side.CLIENT)
	private void updateClient(EntityLivingBase target)
	{
		ChameleonModel model = getModel();

		if (model == null || model.isStatic())
		{
			return;
		}

		this.checkAnimator();
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
			result = result && Objects.equals(morph.actions, this.actions);
			result = result && morph.scale == this.scale;
			result = result && morph.scaleGui == this.scaleGui;
		}

		return result;
	}

	@Override
	public boolean canMerge(AbstractMorph morph)
	{
		if (morph instanceof ChameleonMorph)
		{
			ChameleonMorph animated = (ChameleonMorph) morph;

			if (Objects.equals(this.getKey(), animated.getKey()))
			{
				this.mergeBasic(morph);

				this.lastScale = this.getScale(0);
				this.animation.paused = false;
				this.animation.last = this.pose == null ? new AnimatedPose() : this.pose.clone();

				this.skin = RLUtils.clone(animated.skin);
				this.pose = animated.pose == null ? null : animated.pose.clone();
				this.actions.copy(animated.actions);
				this.parts.merge(animated.parts);
				this.animation.merge(animated.animation);
				this.scale = animated.scale;
				this.scaleGui = animated.scaleGui;

				this.updateAnimator = true;

				return true;
			}
		}

		return false;
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

			this.actions.copy(morph.actions);
			this.parts.copy(morph.parts);
			this.animation.copy(morph.animation);
			this.scale = morph.scale;
			this.scaleGui = morph.scaleGui;
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
		this.pose = null;
		this.actions = new ActionsConfig();
		this.parts.reset();

		this.scale = this.scaleGui = this.lastScale = 1;

		this.updateAnimator = true;
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

		NBTTagCompound animation = this.animation.toNBT();

		if (!animation.hasNoTags())
		{
			tag.setTag("Transition", animation);
		}

		NBTTagCompound actions = this.actions.toNBT();

		if (actions != null)
		{
			tag.setTag("Actions", actions);
		}

		if (this.scale != 1F)
		{
			tag.setFloat("Scale", this.scale);
		}

		if (this.scaleGui != 1F)
		{
			tag.setFloat("ScaleGUI", this.scaleGui);
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

		if (tag.hasKey("Transition"))
		{
			this.animation.fromNBT(tag.getCompoundTag("Transition"));
		}

		if (tag.hasKey("Actions"))
		{
			this.actions.fromNBT(tag.getCompoundTag("Actions"));
		}

		if (tag.hasKey("Scale"))
		{
			this.scale = tag.getFloat("Scale");
		}

		if (tag.hasKey("ScaleGUI"))
		{
			this.scaleGui = tag.getFloat("ScaleGUI");
		}
	}
}