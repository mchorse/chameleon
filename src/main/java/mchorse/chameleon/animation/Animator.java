package mchorse.chameleon.animation;

import mchorse.chameleon.metamorph.ChameleonMorph;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.core.builder.Animation;
import software.bernie.geckolib3.file.AnimationFile;
import software.bernie.geckolib3.geo.render.built.GeoModel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Animator class
 * 
 * This class is responsible for applying currently running actions onto 
 * morph (more specifically onto an armature).
 */
@SideOnly(Side.CLIENT)
public class Animator
{
    /* Actions */
    public ActionPlayback idle;
    public ActionPlayback running;
    public ActionPlayback sprinting;
    public ActionPlayback crouching;
    public ActionPlayback crouchingIdle;
    public ActionPlayback swimming;
    public ActionPlayback swimmingIdle;
    public ActionPlayback flying;
    public ActionPlayback flyingIdle;
    public ActionPlayback riding;
    public ActionPlayback ridingIdle;
    public ActionPlayback dying;
    public ActionPlayback falling;
    public ActionPlayback sleeping;

    public ActionPlayback jump;
    public ActionPlayback swipe;
    public ActionPlayback hurt;
    public ActionPlayback land;
    public ActionPlayback shoot;
    public ActionPlayback consume;

    /* Action pipeline properties */
    public ActionPlayback active;
    public ActionPlayback lastActive;
    public List<ActionPlayback> actions = new ArrayList<ActionPlayback>();

    public double prevX = Float.MAX_VALUE;
    public double prevZ = Float.MAX_VALUE;
    public double prevMY;

    /* States */
    public boolean wasOnGround = true;
    public boolean wasShooting = false;
    public boolean wasConsuming = false;

    private ChameleonMorph morph;
    private AnimationFile animations;

    public Animator(ChameleonMorph morph, AnimationFile animations)
    {
        this.morph = morph;
        this.animations = animations;
        this.refresh();
    }

    public void refresh()
    {
        ActionsConfig actions = this.morph.actions;

        this.idle = this.createAction(this.idle, actions.getConfig("idle"), true);
        this.running = this.createAction(this.running, actions.getConfig("running"), true);
        this.sprinting = this.createAction(this.sprinting, actions.getConfig("sprinting"), true);
        this.crouching = this.createAction(this.crouching, actions.getConfig("crouching"), true);
        this.crouchingIdle = this.createAction(this.crouchingIdle, actions.getConfig("crouching_idle"), true);
        this.swimming = this.createAction(this.swimming, actions.getConfig("swimming"), true);
        this.swimmingIdle = this.createAction(this.swimmingIdle, actions.getConfig("swimming_idle"), true);
        this.flying = this.createAction(this.flying, actions.getConfig("flying"), true);
        this.flyingIdle = this.createAction(this.flyingIdle, actions.getConfig("flying_idle"), true);
        this.riding = this.createAction(this.riding, actions.getConfig("riding"), true);
        this.ridingIdle = this.createAction(this.ridingIdle, actions.getConfig("riding_idle"), true);
        this.dying = this.createAction(this.dying, actions.getConfig("dying"), false);
        this.falling = this.createAction(this.falling, actions.getConfig("falling"), true);
        this.sleeping = this.createAction(this.sleeping, actions.getConfig("sleeping"), true);

        this.swipe = this.createAction(this.swipe, actions.getConfig("swipe"), false);
        this.jump = this.createAction(this.jump, actions.getConfig("jump"), false, 2);
        this.hurt = this.createAction(this.hurt, actions.getConfig("hurt"), false, 3);
        this.land = this.createAction(this.land, actions.getConfig("land"), false);
        this.shoot = this.createAction(this.shoot, actions.getConfig("shoot"), true);
        this.consume = this.createAction(this.consume, actions.getConfig("consume"), true);
    }

    /**
     * Create an action with default priority
     */
    public ActionPlayback createAction(ActionPlayback old, ActionConfig config, boolean looping)
    {
        return this.createAction(old, config, looping, 1);
    }

    /**
     * Create an action playback based on given arguments. This method
     * is used for creating actions so it was easier to tell which
     * actions are missing. Beside that, you can pass an old action so
     * in morph merging situation it wouldn't interrupt animation.
     */
    public ActionPlayback createAction(ActionPlayback old, ActionConfig config, boolean looping, int priority)
    {
        if (this.animations == null)
        {
            return null;
        }

        Animation action = this.animations.getAnimation(config.name);

        /* If given action is missing, then omit creation of ActionPlayback */
        if (action == null)
        {
            return null;
        }

        /* If old is the same, then there is no point creating a new one */
        if (old != null && old.action == action)
        {
            old.config = config;
            old.setSpeed(1);

            return old;
        }

        return new ActionPlayback(action, config, looping, priority);
    }

    /**
     * Update animator. This method is responsible for updating action 
     * pipeline and also change current actions based on entity's state.
     */
    public void update(EntityLivingBase target)
    {
        /* Fix issue with morphs sudden running action */
        if (this.prevX == Float.MAX_VALUE)
        {
            this.prevX = target.posX;
            this.prevZ = target.posZ;
        }

        this.controlActions(target);

        /* Update primary actions */
        if (this.active != null)
        {
            this.active.update();
        }

        if (this.lastActive != null)
        {
            this.lastActive.update();
        }

        /* Update secondary actions */
        Iterator<ActionPlayback> it = this.actions.iterator();

        while (it.hasNext())
        {
            ActionPlayback action = it.next();

            action.update();

            if (action.finishedFading() && action.isFadingModeOut())
            {
                action.stopFade();
                it.remove();
            }
        }
    }

    /**
     * This method is designed specifically to isolate any controlling 
     * code (i.e. the ones that is responsible for switching between 
     * actions).
     */
    protected void controlActions(EntityLivingBase target)
    {
        double dx = target.posX - this.prevX;
        double dz = target.posZ - this.prevZ;
        boolean creativeFlying = target instanceof EntityPlayer && ((EntityPlayer) target).capabilities.isFlying;
        boolean wet = target.isInWater();
        final float threshold = creativeFlying ? 0.1F : (wet ? 0.025F : 0.01F);
        boolean moves = Math.abs(dx) > threshold || Math.abs(dz) > threshold;

        if (target.getHealth() <= 0)
        {
            this.setActiveAction(this.dying);
        }
        else if (target.isPlayerSleeping())
        {
            this.setActiveAction(this.sleeping);
        }
        else if (wet)
        {
            this.setActiveAction(!moves ? this.swimmingIdle : this.swimming);
        }
        else if (target.isRiding())
        {
            Entity riding = target.getRidingEntity();
            moves = Math.abs(riding.posX - this.prevX) > threshold || Math.abs(riding.posZ - this.prevZ) > threshold;

            this.prevX = riding.posX;
            this.prevZ = riding.posZ;
            this.setActiveAction(!moves ? this.ridingIdle : this.riding);
        }
        else if (creativeFlying || target.isElytraFlying())
        {
            this.setActiveAction(!moves ? this.flyingIdle : this.flying);
        }
        else
        {
            double speed = target.limbSwingAmount;

            if (target.isSneaking())
            {
                speed /= 0.65;

                this.setActiveAction(!moves ? this.crouchingIdle : this.crouching);

                if (this.crouching != null)
                {
                    this.crouching.setSpeed(target.moveForward > 0 ? speed : -speed);
                }
            }
            else if (!target.onGround && target.motionY < 0 && target.fallDistance > 1.25)
            {
                this.setActiveAction(this.falling);
            }
            else if (target.isSprinting() && this.sprinting != null)
            {
                this.setActiveAction(this.sprinting);

                this.sprinting.setSpeed(speed);
            }
            else
            {
                this.setActiveAction(!moves ? this.idle : this.running);

                speed /= 0.85;

                if (this.running != null)
                {
                    this.running.setSpeed(target.moveForward >= 0 ? speed : -speed);
                }
            }

            if (target.onGround && !this.wasOnGround && !target.isSprinting() && this.prevMY < -0.5)
            {
                this.addAction(this.land);
            }
        }

        if (!target.onGround && this.wasOnGround && Math.abs(target.motionY) > 0.2F)
        {
            this.addAction(this.jump);
            this.wasOnGround = false;
        }

        /* Bow and consumables */
        boolean shooting = this.wasShooting;
        boolean consuming = this.wasConsuming;
        ItemStack stack = target.getHeldItemMainhand();

        if (!stack.isEmpty())
        {
            if (target.getItemInUseCount() > 0)
            {
                EnumAction action = stack.getItemUseAction();

                if (action == EnumAction.BOW)
                {
                    if (!this.actions.contains(this.shoot))
                    {
                        this.addAction(this.shoot);
                    }

                    this.wasShooting = true;
                }
                else if (action == EnumAction.DRINK || action == EnumAction.EAT)
                {
                    if (!this.actions.contains(this.consume))
                    {
                        this.addAction(this.consume);
                    }

                    this.wasConsuming = true;
                }
            }
            else
            {
                this.wasShooting = false;
                this.wasConsuming = false;
            }
        }
        else
        {
            this.wasShooting = false;
            this.wasConsuming = false;
        }

        if (shooting && !this.wasShooting && this.shoot != null)
        {
            this.shoot.fadeOut();
        }

        if (consuming && !this.wasConsuming && this.consume != null)
        {
            this.consume.fadeOut();
        }

        if (target.hurtTime == target.maxHurtTime - 1)
        {
            this.addAction(this.hurt);
        }

        if (target.isSwingInProgress && target.swingProgress == 0 && !target.isPlayerSleeping())
        {
            this.addAction(this.swipe);
        }

        this.prevX = target.posX;
        this.prevZ = target.posZ;
        this.prevMY = target.motionY;

        this.wasOnGround = target.onGround;
    }

    /**
     * Set current active (primary) action 
     */
    public void setActiveAction(ActionPlayback action)
    {
        if (this.active == action || action == null)
        {
            return;
        }

        if (this.active != null && action.priority < this.active.priority)
        {
            return;
        }

        if (this.active != null)
        {
            this.lastActive = this.active;
        }

        this.active = action;
        this.active.reset();
        this.active.fadeIn();
    }

    /**
     * Add an additional secondary action to the playback 
     */
    public void addAction(ActionPlayback action)
    {
        if (action == null)
        {
            return;
        }

        if (this.actions.contains(action))
        {
            action.reset();

            return;
        }

        action.reset();
        action.fadeIn();
        this.actions.add(action);
    }

    /**
     * Apply currently running action pipeline onto given armature
     */
    public void applyActions(EntityLivingBase target, GeoModel armature, float partialTicks)
    {
        if (this.lastActive != null && this.active.isFading())
        {
            this.lastActive.apply(target, armature, partialTicks, 1F, false);
        }

        if (this.active != null)
        {
            float fade = this.active.isFading() ? this.active.getFadeFactor(partialTicks) : 1F;

            this.active.apply(target, armature, partialTicks, fade, false);
        }

        for (ActionPlayback action : this.actions)
        {
            if (action.isFading())
            {
                action.apply(target, armature, partialTicks, action.getFadeFactor(partialTicks), true);
            }
            else
            {
                action.apply(target, armature, partialTicks, 1F, true);
            }
        }
    }
}