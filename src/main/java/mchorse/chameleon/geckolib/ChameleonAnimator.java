package mchorse.chameleon.geckolib;

import mchorse.chameleon.ClientProxy;
import mchorse.mclib.utils.Interpolations;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.core.builder.Animation;
import software.bernie.geckolib3.core.easing.EasingManager;
import software.bernie.geckolib3.core.keyframe.BoneAnimation;
import software.bernie.geckolib3.core.keyframe.KeyFrame;
import software.bernie.geckolib3.core.keyframe.VectorKeyFrameList;
import software.bernie.geckolib3.core.snapshot.BoneSnapshot;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.shadowed.eliotlash.mclib.math.IValue;

import javax.vecmath.Vector3d;
import java.util.List;
import java.util.Optional;

@SideOnly(Side.CLIENT)
public class ChameleonAnimator
{
	public static void animate(EntityLivingBase target, GeoModel model, Animation animation, float frame)
	{
		frame %= animation.animationLength;

		MolangHelper.setMolangVariables(ClientProxy.parser, target, frame);

		for (BoneAnimation boneAnimation : animation.boneAnimations)
		{
			Optional<GeoBone> opBone = model.getBone(boneAnimation.boneName);

			if (opBone.isPresent())
			{
				applyBoneAnimation(opBone.get(), boneAnimation, frame);
			}
		}
	}

	private static void applyBoneAnimation(GeoBone geoBone, BoneAnimation boneAnimation, float frame)
	{
		Vector3d pos = interpolateList(boneAnimation.positionKeyFrames, frame, MolangHelper.Component.POSITION);
		Vector3d rot = interpolateList(boneAnimation.rotationKeyFrames, frame, MolangHelper.Component.ROTATION);
		Vector3d scale = interpolateList(boneAnimation.scaleKeyFrames, frame, MolangHelper.Component.SCALE);
		BoneSnapshot initial = geoBone.getInitialSnapshot();

		geoBone.setPositionX((float) pos.x + initial.positionOffsetX);
		geoBone.setPositionY((float) pos.y + initial.positionOffsetY);
		geoBone.setPositionZ((float) pos.z + initial.positionOffsetZ);

		geoBone.setRotationX((float) (rot.x / 180 * Math.PI) + initial.rotationValueX);
		geoBone.setRotationY((float) (rot.y / 180 * Math.PI) + initial.rotationValueY);
		geoBone.setRotationZ((float) (rot.z / 180 * Math.PI) + initial.rotationValueZ);

		geoBone.setScaleX((float) scale.x + initial.scaleValueX);
		geoBone.setScaleY((float) scale.y + initial.scaleValueY);
		geoBone.setScaleZ((float) scale.z + initial.scaleValueZ);
	}

	private static Vector3d interpolateList(VectorKeyFrameList<KeyFrame<IValue>> keyframes, float frame, MolangHelper.Component component)
	{
		Vector3d vector = new Vector3d();

		vector.x = interpolate(keyframes.xKeyFrames, frame, component, EnumFacing.Axis.X);
		vector.y = interpolate(keyframes.yKeyFrames, frame, component, EnumFacing.Axis.Y);
		vector.z = interpolate(keyframes.zKeyFrames, frame, component, EnumFacing.Axis.Z);

		return vector;
	}

	private static double interpolate(List<KeyFrame<IValue>> keyframes, float frame, MolangHelper.Component component, EnumFacing.Axis axis)
	{
		if (keyframes.isEmpty())
		{
			return 0;
		}

		KeyFrame<IValue> first = keyframes.get(0);

		if (frame < first.getLength())
		{
			return MolangHelper.getValue(first.getStartValue(), component, axis);
		}

		double duration = 0;
		KeyFrame<IValue> previous = null;

		for (KeyFrame<IValue> kf : keyframes)
		{
			if (frame >= duration && frame < duration + kf.getLength())
			{
				double factor = (frame - duration) / kf.getLength().floatValue();

				factor = EasingManager.ease(factor, kf.easingType, kf.easingArgs);

				return Interpolations.lerp(MolangHelper.getValue(kf.getStartValue(), component, axis), MolangHelper.getValue(kf.getEndValue(), component, axis), factor);
			}

			duration += kf.getLength();
			previous = kf;
		}

		return MolangHelper.getValue(previous.getEndValue(), component, axis);
	}
}
