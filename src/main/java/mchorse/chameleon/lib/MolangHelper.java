package mchorse.chameleon.lib;

import mchorse.mclib.math.Variable;
import mchorse.mclib.math.molang.MolangParser;
import mchorse.mclib.math.molang.expressions.MolangExpression;
import mchorse.mclib.utils.Interpolations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MolangHelper
{
    public static void registerVars(MolangParser parser)
    {
        parser.register(new Variable("query.anim_time", 0));
        parser.register(new Variable("query.life_time", 0));
        parser.register(new Variable("query.actor_count", 0));
        parser.register(new Variable("query.time_of_day", 0));
        parser.register(new Variable("query.moon_phase", 0));
        parser.register(new Variable("query.distance_from_camera", 0));
        parser.register(new Variable("query.is_on_ground", 0));
        parser.register(new Variable("query.is_in_water", 0));
        parser.register(new Variable("query.is_in_water_or_rain", 0));
        parser.register(new Variable("query.health", 0));
        parser.register(new Variable("query.max_health", 0));
        parser.register(new Variable("query.is_on_fire", 0));
        parser.register(new Variable("query.ground_speed", 0));
        parser.register(new Variable("query.yaw_speed", 0));
    }

    public static void setMolangVariables(MolangParser parser, EntityLivingBase target, float frame)
    {
        parser.setValue("query.anim_time", frame / 20);
        parser.setValue("query.life_time", frame / 20);

        Minecraft mc = Minecraft.getMinecraft();
        float partialTick = mc.getRenderPartialTicks();

        parser.setValue("query.actor_count", mc.world.loadedEntityList.size());
        parser.setValue("query.time_of_day", normalizeTime(mc.world.getTotalWorldTime()));
        parser.setValue("query.moon_phase", mc.world.getMoonPhase());

        Entity camera = mc.getRenderViewEntity();
        Vec3d entityCamera = new Vec3d(
            camera.prevPosX + (camera.posX - camera.prevPosX) * partialTick,
            camera.prevPosY + (camera.posY - camera.prevPosY) * partialTick,
            camera.prevPosZ + (camera.posZ - camera.prevPosZ) * partialTick
        );
        Vec3d entityPosition = new Vec3d(
            target.prevPosX + (target.posX - target.prevPosX) * partialTick,
            target.prevPosY + (target.posY - target.prevPosY) * partialTick,
            target.prevPosZ + (target.posZ - target.prevPosZ) * partialTick
        );
        double distance = entityCamera.add(ActiveRenderInfo.getCameraPosition()).distanceTo(entityPosition);

        parser.setValue("query.distance_from_camera", distance);
        parser.setValue("query.is_on_ground", booleanToDouble(target.onGround));
        parser.setValue("query.is_in_water", booleanToDouble(target.isInWater()));
        parser.setValue("query.is_in_water_or_rain", booleanToDouble(target.isWet()));

        parser.setValue("query.health", target.getHealth());
        parser.setValue("query.max_health", target.getMaxHealth());
        parser.setValue("query.is_on_fire", booleanToDouble(target.isBurning()));

        double dx = target.motionX;
        double dz = target.motionZ;
        float groundSpeed = MathHelper.sqrt((dx * dx) + (dz * dz));
        parser.setValue("query.ground_speed", groundSpeed);

        parser.setValue("query.yaw_speed", target.rotationYawHead - target.prevRotationYawHead);

        /* Chameleon specific queries */
        float yaw = Interpolations.lerp(target.prevRotationYawHead, target.rotationYawHead, partialTick);
        float bodyYaw = Interpolations.lerp(target.prevRenderYawOffset, target.renderYawOffset, partialTick);

        parser.setValue("query.head_yaw", yaw - bodyYaw);
        parser.setValue("query.head_pitch", Interpolations.lerp(target.prevRotationPitch, target.rotationPitch, partialTick));

        double velocity = Math.sqrt(target.motionX * target.motionX + target.motionY * target.motionY + target.motionZ * target.motionZ);
        float limbSwingAmount = Interpolations.lerp(target.prevLimbSwingAmount, target.limbSwingAmount, partialTick);
        float limbSwing = target.limbSwing - target.limbSwingAmount * (1.0F - partialTick);

        /* There is still a tiny bit of vertical velocity (gravity) when an
         * entity stands still, so set it to zero in that case */
        if (target.onGround && target.motionY < 0 && (Math.abs(target.motionX) < 0.001 || Math.abs(target.motionZ) < 0.001))
        {
            velocity = 0;
        }

        if (limbSwingAmount > 1.0F)
        {
            limbSwingAmount = 1.0F;
        }

        parser.setValue("query.velocity", velocity);
        parser.setValue("query.limb_swing", limbSwing);
        parser.setValue("query.limb_swing_amount", limbSwingAmount);
        parser.setValue("query.age", target.ticksExisted + partialTick);
    }

    private static double normalizeTime(long totalWorldTime)
    {
        return totalWorldTime / 24000D;
    }

    private static double booleanToDouble(boolean bool)
    {
        return bool ? 1D : 0D;
    }

    /**
     * Get value from given value of a keyframe (end or start)
     *
     * This method is responsible for processing keyframe value, because
     * for some reason constant values are exported in radians, while molang
     * expressions are in degrees
     *
     * Plus X and Y axis of rotation are inverted for some reason ...
     */
    public static double getValue(MolangExpression value, Component component, EnumFacing.Axis axis)
    {
        double out = value.get();

        if (component == Component.ROTATION)
        {
            if (axis == EnumFacing.Axis.X || axis == EnumFacing.Axis.Y)
            {
                out *= -1;
            }
        }
        else if (component == Component.SCALE)
        {
            out = out - 1;
        }

        return out;
    }

    /**
     * Component enum determines which part of the animation is being
     * calculated
     */
    public static enum Component
    {
        POSITION, ROTATION, SCALE
    }
}