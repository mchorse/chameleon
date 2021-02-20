package mchorse.chameleon.metamorph.pose;

import net.minecraft.nbt.NBTTagCompound;

import java.util.HashMap;
import java.util.Map;

public class AnimatedPose
{
    public final Map<String, AnimatedPoseTransform> bones = new HashMap<String, AnimatedPoseTransform>();
    public float animated = AnimatedPoseTransform.ANIMATED;

    public void copy(AnimatedPose pose)
    {
        for (Map.Entry<String, AnimatedPoseTransform> entry : this.bones.entrySet())
        {
            AnimatedPoseTransform transform = pose.bones.get(entry.getKey());

            if (transform != null)
            {
                entry.getValue().copy(transform);
            }
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof AnimatedPose)
        {
            AnimatedPose pose = (AnimatedPose) obj;

            return this.bones.equals(pose.bones)
                && this.animated == pose.animated;
        }

        return super.equals(obj);
    }

    public AnimatedPose clone()
    {
        AnimatedPose pose = new AnimatedPose();

        for (Map.Entry<String, AnimatedPoseTransform> entry : this.bones.entrySet())
        {
            pose.bones.put(entry.getKey(), entry.getValue().clone());
        }

        pose.animated = this.animated;

        return pose;
    }

    public void fromNBT(NBTTagCompound tag)
    {
        if (tag.hasKey("Pose"))
        {
            NBTTagCompound pose = tag.getCompoundTag("Pose");

            for (String key : pose.getKeySet())
            {
                AnimatedPoseTransform config = new AnimatedPoseTransform(key);

                config.fromNBT(pose.getCompoundTag(key));
                this.bones.put(key, config);
            }
        }

        if (tag.hasKey("Animated"))
        {
            this.animated = tag.getBoolean("Animated") ? AnimatedPoseTransform.ANIMATED : AnimatedPoseTransform.FIXED;
        }
    }

    public NBTTagCompound toNBT()
    {
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagCompound pose = new NBTTagCompound();

        for (Map.Entry<String, AnimatedPoseTransform> entry : this.bones.entrySet())
        {
            pose.setTag(entry.getKey(), entry.getValue().toNBT(null));
        }

        tag.setTag("Pose", pose);

        if (this.animated != AnimatedPoseTransform.ANIMATED)
        {
            tag.setBoolean("Animated", false);
        }

        return tag;
    }
}