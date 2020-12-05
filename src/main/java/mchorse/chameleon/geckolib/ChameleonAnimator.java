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

@SideOnly(Side.CLIENT)
public class ChameleonAnimator
{
	public static void resetPose(GeoModel model)
	{
		for (GeoBone bone : model.topLevelBones)
		{
			resetBone(bone);
		}
	}

	private static void resetBone(GeoBone bone)
	{
		BoneSnapshot initial = bone.getInitialSnapshot();

		bone.setPositionX(initial.positionOffsetX);
		bone.setPositionY(initial.positionOffsetY);
		bone.setPositionZ(initial.positionOffsetZ);

		bone.setRotationX(initial.rotationValueX);
		bone.setRotationY(initial.rotationValueY);
		bone.setRotationZ(initial.rotationValueZ);

		bone.setScaleX(initial.scaleValueX);
		bone.setScaleY(initial.scaleValueY);
		bone.setScaleZ(initial.scaleValueZ);

		for (GeoBone childBone : bone.childBones)
		{
			resetBone(childBone);
		}
	}

	public static void animate(EntityLivingBase target, GeoModel model, Animation animation, float frame, float blend, boolean skipInitial)
	{
		MolangHelper.setMolangVariables(ClientProxy.parser, target, frame);

		for (GeoBone bone : model.topLevelBones)
		{
			animateBone(bone, animation, frame, blend, skipInitial);
		}
	}

	private static void animateBone(GeoBone bone, Animation animation, float frame, float blend, boolean skipInitial)
	{
		boolean applied = false;

		for (BoneAnimation boneAnimation : animation.boneAnimations)
		{
			if (boneAnimation.boneName.equals(bone.name))
			{
				applyBoneAnimation(bone, boneAnimation, frame, blend);

				applied = true;

				break;
			}
		}

		if (!applied && !skipInitial)
		{
			BoneSnapshot initial = bone.getInitialSnapshot();

			bone.setPositionX(Interpolations.lerp(bone.getPositionX(), initial.positionOffsetX, blend));
			bone.setPositionY(Interpolations.lerp(bone.getPositionY(), initial.positionOffsetY, blend));
			bone.setPositionZ(Interpolations.lerp(bone.getPositionZ(), initial.positionOffsetZ, blend));

			bone.setRotationX(Interpolations.lerp(bone.getRotationX(), initial.rotationValueX, blend));
			bone.setRotationY(Interpolations.lerp(bone.getRotationY(), initial.rotationValueY, blend));
			bone.setRotationZ(Interpolations.lerp(bone.getRotationZ(), initial.rotationValueZ, blend));

			bone.setScaleX(Interpolations.lerp(bone.getScaleX(), initial.scaleValueX, blend));
			bone.setScaleY(Interpolations.lerp(bone.getScaleY(), initial.scaleValueY, blend));
			bone.setScaleZ(Interpolations.lerp(bone.getScaleZ(), initial.scaleValueZ, blend));
		}

		for (GeoBone childBone : bone.childBones)
		{
			animateBone(childBone, animation, frame, blend, skipInitial);
		}
	}

	private static void applyBoneAnimation(GeoBone geoBone, BoneAnimation boneAnimation, float frame, float blend)
	{
		Vector3d pos = interpolateList(boneAnimation.positionKeyFrames, frame, MolangHelper.Component.POSITION);
		Vector3d rot = interpolateList(boneAnimation.rotationKeyFrames, frame, MolangHelper.Component.ROTATION);
		Vector3d scale = interpolateList(boneAnimation.scaleKeyFrames, frame, MolangHelper.Component.SCALE);
		BoneSnapshot initial = geoBone.getInitialSnapshot();

		geoBone.setPositionX(Interpolations.lerp(geoBone.getPositionX(), (float) pos.x + initial.positionOffsetX, blend));
		geoBone.setPositionY(Interpolations.lerp(geoBone.getPositionY(), (float) pos.y + initial.positionOffsetY, blend));
		geoBone.setPositionZ(Interpolations.lerp(geoBone.getPositionZ(), (float) pos.z + initial.positionOffsetZ, blend));

		geoBone.setRotationX(Interpolations.lerp(geoBone.getRotationX(), (float) (rot.x / 180 * Math.PI) + initial.rotationValueX, blend));
		geoBone.setRotationY(Interpolations.lerp(geoBone.getRotationY(), (float) (rot.y / 180 * Math.PI) + initial.rotationValueY, blend));
		geoBone.setRotationZ(Interpolations.lerp(geoBone.getRotationZ(), (float) (rot.z / 180 * Math.PI) + initial.rotationValueZ, blend));

		geoBone.setScaleX(Interpolations.lerp(geoBone.getScaleX(), (float) scale.x + initial.scaleValueX, blend));
		geoBone.setScaleY(Interpolations.lerp(geoBone.getScaleY(), (float) scale.y + initial.scaleValueY, blend));
		geoBone.setScaleZ(Interpolations.lerp(geoBone.getScaleZ(), (float) scale.z + initial.scaleValueZ, blend));
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
				double factor = EasingManager.ease((frame - duration) / kf.getLength().floatValue(), kf.easingType, kf.easingArgs);
				double start = MolangHelper.getValue(kf.getStartValue(), component, axis);
				double destination = MolangHelper.getValue(kf.getEndValue(), component, axis);

				return Interpolations.lerp(start, destination, factor);
			}

			duration += kf.getLength();
			previous = kf;
		}

		return MolangHelper.getValue(previous.getEndValue(), component, axis);
	}
}
