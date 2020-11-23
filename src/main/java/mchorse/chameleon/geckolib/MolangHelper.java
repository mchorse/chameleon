package mchorse.chameleon.geckolib;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.core.ConstantValue;
import software.bernie.geckolib3.util.MolangUtils;
import software.bernie.shadowed.eliotlash.mclib.math.IValue;
import software.bernie.shadowed.eliotlash.molang.MolangParser;

@SideOnly(Side.CLIENT)
public class MolangHelper
{
	public static void setMolangVariables(MolangParser parser, EntityLivingBase target, float frame)
	{
		parser.setValue("query.anim_time", frame / 20);
		parser.setValue("query.life_time", frame / 20);

		Minecraft minecraftInstance = Minecraft.getMinecraft();
		float partialTick = minecraftInstance.getRenderPartialTicks();

		parser.setValue("query.actor_count", minecraftInstance.world.loadedEntityList.size());
		parser.setValue("query.time_of_day", MolangUtils.normalizeTime(minecraftInstance.world.getTotalWorldTime()));
		parser.setValue("query.moon_phase", minecraftInstance.world.getMoonPhase());

		Entity camera = minecraftInstance.getRenderViewEntity();

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
		parser.setValue("query.is_on_ground", MolangUtils.booleanToFloat(target.onGround));
		parser.setValue("query.is_in_water", MolangUtils.booleanToFloat(target.isInWater()));
		//Should probably check specifically whether it's in rain?
		parser.setValue("query.is_in_water_or_rain", MolangUtils.booleanToFloat(target.isWet()));

		parser.setValue("query.health", target.getHealth());
		parser.setValue("query.max_health", target.getMaxHealth());
		parser.setValue("query.is_on_fire", MolangUtils.booleanToFloat(target.isBurning()));

		double dx = target.motionX;
		double dz = target.motionZ;
		float groundSpeed = MathHelper.sqrt((dx * dx) + (dz * dz));
		parser.setValue("query.ground_speed", groundSpeed);

		parser.setValue("query.yaw_speed", target.rotationYaw - target.prevRotationYaw);
	}

	public static double getValue(IValue value, Component component, EnumFacing.Axis axis)
	{
		double out = value.get();

		if (component == Component.ROTATION)
		{
			if (value instanceof ConstantValue)
			{
				out = Math.toDegrees(out);
			}
			else if (axis == EnumFacing.Axis.X || axis == EnumFacing.Axis.Y)
			{
				out *= -1;
			}
		}

		return out;
	}

	public static enum Component
	{
		POSITION, ROTATION, SCALE
	}
}