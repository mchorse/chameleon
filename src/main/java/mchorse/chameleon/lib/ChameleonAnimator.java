package mchorse.chameleon.lib;

import mchorse.chameleon.ClientProxy;
import mchorse.chameleon.lib.data.animation.Animation;
import mchorse.chameleon.lib.data.animation.AnimationChannel;
import mchorse.chameleon.lib.data.animation.AnimationPart;
import mchorse.chameleon.lib.data.animation.AnimationVector;
import mchorse.chameleon.lib.data.model.Model;
import mchorse.chameleon.lib.data.model.ModelBone;
import mchorse.chameleon.lib.data.model.ModelTransform;
import mchorse.mclib.utils.Interpolations;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.vecmath.Vector3d;
import java.util.List;

@SideOnly(Side.CLIENT)
public class ChameleonAnimator
{
    public static void resetPose(Model model)
    {
        for (ModelBone bone : model.bones)
        {
            resetBone(bone);
        }
    }

    private static void resetBone(ModelBone bone)
    {
        bone.current.translate.set(bone.initial.translate);
        bone.current.scale.set(bone.initial.scale);
        bone.current.rotation.set(bone.initial.rotation);

        for (ModelBone childBone : bone.children)
        {
            resetBone(childBone);
        }
    }

    public static void animate(EntityLivingBase target, Model model, Animation animation, float frame, float blend, boolean skipInitial)
    {
        MolangHelper.setMolangVariables(ClientProxy.parser, target, frame);

        for (ModelBone bone : model.bones)
        {
            animateBone(bone, animation, frame, blend, skipInitial);
        }
    }

    private static void animateBone(ModelBone bone, Animation animation, float frame, float blend, boolean skipInitial)
    {
        boolean applied = false;

        AnimationPart part = animation.parts.get(bone.id);

        if (part != null)
        {
            applyBoneAnimation(bone, part, frame, blend);

            applied = true;
        }

        if (!applied && !skipInitial)
        {
            ModelTransform initial = bone.initial;
            ModelTransform current = bone.current;

            current.translate.x = Interpolations.lerp(current.translate.x, initial.translate.x, blend);
            current.translate.y = Interpolations.lerp(current.translate.y, initial.translate.y, blend);
            current.translate.z = Interpolations.lerp(current.translate.z, initial.translate.z, blend);

            current.scale.x = Interpolations.lerp(current.scale.x, initial.scale.x, blend);
            current.scale.y = Interpolations.lerp(current.scale.y, initial.scale.y, blend);
            current.scale.z = Interpolations.lerp(current.scale.z, initial.scale.z, blend);

            current.rotation.x = Interpolations.lerp(current.rotation.x, initial.rotation.x, blend);
            current.rotation.y = Interpolations.lerp(current.rotation.y, initial.rotation.y, blend);
            current.rotation.z = Interpolations.lerp(current.rotation.z, initial.rotation.z, blend);
        }

        for (ModelBone childBone : bone.children)
        {
            animateBone(childBone, animation, frame, blend, skipInitial);
        }
    }

    private static void applyBoneAnimation(ModelBone bone, AnimationPart animation, float frame, float blend)
    {
        Vector3d pos = interpolateList(animation.position, frame, MolangHelper.Component.POSITION);
        Vector3d rot = interpolateList(animation.rotation, frame, MolangHelper.Component.ROTATION);
        Vector3d scale = interpolateList(animation.scale, frame, MolangHelper.Component.SCALE);

        ModelTransform initial = bone.initial;
        ModelTransform current = bone.current;

        current.translate.x = Interpolations.lerp(current.translate.x, (float) pos.x + initial.translate.x, blend);
        current.translate.y = Interpolations.lerp(current.translate.y, (float) pos.y + initial.translate.y, blend);
        current.translate.z = Interpolations.lerp(current.translate.z, (float) pos.z + initial.translate.z, blend);

        current.scale.x = Interpolations.lerp(current.scale.x, (float) scale.x + initial.scale.x, blend);
        current.scale.y = Interpolations.lerp(current.scale.y, (float) scale.y + initial.scale.y, blend);
        current.scale.z = Interpolations.lerp(current.scale.z, (float) scale.z + initial.scale.z, blend);

        current.rotation.x = Interpolations.lerp(current.rotation.x, (float) rot.x + initial.rotation.x, blend);
        current.rotation.y = Interpolations.lerp(current.rotation.y, (float) rot.y + initial.rotation.y, blend);
        current.rotation.z = Interpolations.lerp(current.rotation.z, (float) rot.z + initial.rotation.z, blend);
    }

    private static Vector3d interpolateList(AnimationChannel channel, float frame, MolangHelper.Component component)
    {
        Vector3d vector = new Vector3d();

        vector.x = interpolate(channel.keyframes, frame, component, EnumFacing.Axis.X);
        vector.y = interpolate(channel.keyframes, frame, component, EnumFacing.Axis.Y);
        vector.z = interpolate(channel.keyframes, frame, component, EnumFacing.Axis.Z);

        return vector;
    }

    private static double interpolate(List<AnimationVector> keyframes, float frame, MolangHelper.Component component, EnumFacing.Axis axis)
    {
        if (keyframes.isEmpty())
        {
            return 0;
        }

        AnimationVector first = keyframes.get(0);

        if (frame < first.getLengthInTicks())
        {
            return MolangHelper.getValue(first.getStart(axis), component, axis);
        }

        double duration = 0;
        AnimationVector previous = null;

        for (AnimationVector vector : keyframes)
        {
            double length = vector.getLengthInTicks();

            if (frame >= duration && frame < duration + length)
            {
                // TODO: double factor = EasingManager.ease((frame - duration) / length, vector.easingType, vector.easingArgs);
                double factor = Interpolations.lerp(0, 1, (frame - duration) / length);
                double start = MolangHelper.getValue(vector.getStart(axis), component, axis);
                double destination = MolangHelper.getValue(vector.getEnd(axis), component, axis);

                return Interpolations.lerp(start, destination, factor);
            }

            duration += length;
            previous = vector;
        }

        return MolangHelper.getValue(previous.getEnd(axis), component, axis);
    }
}
