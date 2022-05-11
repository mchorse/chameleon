package mchorse.chameleon.metamorph;

import mchorse.chameleon.ClientProxy;
import mchorse.chameleon.animation.ActionPlayback;
import mchorse.chameleon.animation.ActionsConfig;
import mchorse.chameleon.animation.Animator;
import mchorse.chameleon.lib.ChameleonAnimator;
import mchorse.chameleon.lib.ChameleonModel;
import mchorse.chameleon.lib.data.model.Model;
import mchorse.chameleon.lib.data.model.ModelBone;
import mchorse.chameleon.lib.data.model.ModelTransform;
import mchorse.chameleon.lib.render.ChameleonRenderer;
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
import mchorse.metamorph.api.morphs.utils.IMorphGenerator;
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
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Objects;

public class ChameleonMorph extends AbstractMorph implements IBodyPartProvider, ISyncableMorph, IAnimationProvider, IMorphGenerator
{
    public ResourceLocation skin;
    public AnimatedPose pose;
    public ActionsConfig actions = new ActionsConfig();
    public BodyPartManager parts = new BodyPartManager();

    public float scale = 1F;
    public float scaleGui = 1F;
    private float lastScale = 1F;

    public PoseAnimation animation = new PoseAnimation();

    /* Syncable action */
    public boolean isActionPlayer;

    public ActionPlayback lastAnimAction;

    /**
     * Cached key value
     */
    private String key;

    @SideOnly(Side.CLIENT)
    private Animator animator;

    private boolean updateAnimator = false;
    private long lastUpdate;

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
            this.animator = new Animator(this);
        }

        return this.animator;
    }

    @Override
    public void pause(AbstractMorph previous, int offset)
    {
        this.animation.pause(offset);

        while (previous instanceof IMorphProvider)
        {
            previous = ((IMorphProvider) previous).getMorph();
        }

        AnimatedPose pose = null;

        if (previous instanceof ChameleonMorph)
        {
            ChameleonMorph morph = (ChameleonMorph) previous;

            pose = morph.getCurrentPose(0F);

            if (pose != null)
            {
                pose = pose.clone();
            }

            this.lastScale = morph.scale;

            if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT && this.isActionPlayer && morph.isActionPlayer)
            {
                morph.checkAnimator();

                if (morph.animator != null && morph.actions.getConfig("animation") != null)
                {
                    this.lastAnimAction = morph.animator.createAction(morph.animator.animation, morph.actions.getConfig("animation").clone(), false);
                }
                else
                {
                    this.lastAnimAction = null;
                }

                if (this.lastAnimAction != null)
                {
                    this.lastAnimAction.config.tick += morph.animation.getFactor(0F) * morph.animation.duration * Math.abs(this.lastAnimAction.config.speed);
                }
            }
        }

        this.animation.last = pose == null ? (previous == null ? this.pose : new AnimatedPose()) : pose;
        this.parts.pause(previous, offset);
        
        this.updateAnimator = true;
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

    @Override
    public boolean canGenerate()
    {
        return this.animation.isInProgress();
    }

    @Override
    public AbstractMorph genCurrentMorph(float partialTicks)
    {
        ChameleonMorph morph = (ChameleonMorph) this.copy();

        morph.pose = this.getCurrentPose(partialTicks);
        morph.animation.duration = this.animation.progress;

        morph.parts.parts.clear();

        for (BodyPart part : this.parts.parts)
        {
            morph.parts.parts.add(part.genCurrentBodyPart(this, partialTicks));
        }

        return morph.copy();
    }

    public String getKey()
    {
        if (this.key == null)
        {
            this.key = this.name.replaceAll("^chameleon\\.", "");
        }

        return this.key;
    }

    @SideOnly(Side.CLIENT)
    public void updateAnimator()
    {
        this.updateAnimator = true;
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

        boolean captured = MatrixUtils.captureMatrix();

        GlStateManager.scale(scale, scale, scale);

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
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableAlpha();

        ChameleonModel chameleonModel = this.getModel();

        if (chameleonModel == null)
        {
            return;
        }

        this.checkAnimator();

        Model model = chameleonModel.model;

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
                part.render(this, target, partialTicks);
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
    private void applyPose(Model model, float partialTicks)
    {
        AnimatedPose pose = this.pose;
        boolean inProgress = this.animation.isInProgress();

        if (inProgress)
        {
            pose = this.animation.calculatePose(this.pose, this.getModel(), partialTicks);
        }

        for (ModelBone bone : model.bones)
        {
            this.applyPose(bone, pose);
        }
    }

    @SideOnly(Side.CLIENT)
    private void applyPose(ModelBone bone, AnimatedPose pose)
    {
        if (pose != null && pose.bones.containsKey(bone.id))
        {
            AnimatedPoseTransform transform = pose.bones.get(bone.id);
            ModelTransform initial = bone.initial;
            ModelTransform current = bone.current;
            float factor = transform.fixed * pose.animated;
            final float piToDegrees = (float) (180D / Math.PI);

            current.translate.x = Interpolations.lerp(initial.translate.x, current.translate.x, factor) + transform.x;
            current.translate.y = Interpolations.lerp(initial.translate.y, current.translate.y, factor) + transform.y;
            current.translate.z = Interpolations.lerp(initial.translate.z, current.translate.z, factor) + transform.z;

            current.rotation.x = Interpolations.lerp(initial.rotation.x, current.rotation.x, factor) + transform.rotateX * piToDegrees;
            current.rotation.y = Interpolations.lerp(initial.rotation.y, current.rotation.y, factor) + transform.rotateY * piToDegrees;
            current.rotation.z = Interpolations.lerp(initial.rotation.z, current.rotation.z, factor) + transform.rotateZ * piToDegrees;

            current.scale.x = Interpolations.lerp(initial.scale.x, current.scale.x, factor) + (transform.scaleX - 1);
            current.scale.y = Interpolations.lerp(initial.scale.y, current.scale.y, factor) + (transform.scaleY - 1);
            current.scale.z = Interpolations.lerp(initial.scale.z, current.scale.z, factor) + (transform.scaleZ - 1);

            bone.absoluteBrightness = transform.absoluteBrightness;
            bone.glow = transform.glow;
            bone.color.copy(transform.color);
        }

        for (ModelBone childBone : bone.children)
        {
            this.applyPose(childBone, pose);
        }
    }

    private AnimatedPose getCurrentPose(float partialTicks)
    {
        if (this.getModel() == null)
        {
            return this.pose == null ? new AnimatedPose() : this.pose.clone();
        }
        else
        {
            return this.animation.calculatePose(this.pose, this.getModel(), partialTicks).clone();
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
        this.parts.updateBodyLimbs(this, target);

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

        if (this.lastUpdate < model.lastUpdate)
        {
            this.lastUpdate = model.lastUpdate;
            this.updateAnimator = true;
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
            result = result && Objects.equals(morph.animation, this.animation);
            result = result && morph.scale == this.scale;
            result = result && morph.scaleGui == this.scaleGui;
            result = result && morph.isActionPlayer == this.isActionPlayer;
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
                this.animation.last = this.getCurrentPose(0F);

                if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT && this.isActionPlayer && animated.isActionPlayer)
                {
                    this.checkAnimator();

                    if (this.animator != null && this.actions.getConfig("animation") != null)
                    {
                        this.lastAnimAction = this.animator.createAction(this.animator.animation, this.actions.getConfig("animation").clone(), false);
                    }
                    else
                    {
                        this.lastAnimAction = null;
                    }

                    if (this.lastAnimAction != null)
                    {
                        this.lastAnimAction.config.tick += this.animation.getFactor(0F) * this.animation.duration * Math.abs(this.lastAnimAction.config.speed);
                    }
                }

                this.isActionPlayer = animated.isActionPlayer;

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
    public void afterMerge(AbstractMorph morph)
    {
        super.afterMerge(morph);

        while (morph instanceof IMorphProvider)
        {
            morph = ((IMorphProvider) morph).getMorph();
        }

        if (morph instanceof IBodyPartProvider)
        {
            this.recursiveAfterMerge(this, (IBodyPartProvider) morph);
        }

        if (morph instanceof ChameleonMorph)
        {
            ChameleonMorph animated = (ChameleonMorph) morph;

            if (Objects.equals(this.getKey(), animated.getKey()))
            {
                this.animation.last = animated.getCurrentPose(0F);

                if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT && this.isActionPlayer && animated.isActionPlayer)
                {
                    animated.checkAnimator();

                    if (animated.animator != null && animated.actions.getConfig("animation") != null)
                    {
                        this.lastAnimAction = animated.animator.createAction(animated.animator.animation, animated.actions.getConfig("animation").clone(), false);
                    }
                    else
                    {
                        this.lastAnimAction = null;
                    }

                    if (this.lastAnimAction != null)
                    {
                        this.lastAnimAction.config.tick += animated.animation.getFactor(0F) * animated.animation.duration * Math.abs(this.lastAnimAction.config.speed);
                    }
                }

                if (animated.animator != null)
                {
                    this.animator = animated.animator;
                    this.animator.morph = this;
                    this.updateAnimator = true;
                }
            }
        }
    }

    private void recursiveAfterMerge(IBodyPartProvider target, IBodyPartProvider destination)
    {
        for (int i = 0, c = target.getBodyPart().parts.size(); i < c; i++)
        {
            if (i >= destination.getBodyPart().parts.size())
            {
                break;
            }

            AbstractMorph a = target.getBodyPart().parts.get(i).morph.get();
            AbstractMorph b = destination.getBodyPart().parts.get(i).morph.get();

            if (a != null)
            {
                a.afterMerge(b);
            }
        }
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
            this.isActionPlayer = morph.isActionPlayer;
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

        this.isActionPlayer = false;

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

        if (this.isActionPlayer)
        {
            tag.setBoolean("ActionPlayer", this.isActionPlayer);
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

        if (tag.hasKey("ActionPlayer"))
        {
            this.isActionPlayer = tag.getBoolean("ActionPlayer");
        }
    }
}