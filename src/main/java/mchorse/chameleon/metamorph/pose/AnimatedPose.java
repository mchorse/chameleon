package mchorse.chameleon.metamorph.pose;

import net.minecraft.nbt.NBTTagCompound;

import java.util.HashMap;
import java.util.Map;

public class AnimatedPose
{
    public final Map<String, AnimatorPoseTransform> bones = new HashMap<String, AnimatorPoseTransform>();
    public boolean animated = true;

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

        for (Map.Entry<String, AnimatorPoseTransform> entry : this.bones.entrySet())
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
                AnimatorPoseTransform config = new AnimatorPoseTransform(key);

                config.fromNBT(pose.getCompoundTag(key));
                this.bones.put(key, config);
            }
        }

        if (tag.hasKey("Animated"))
        {
            this.animated = tag.getBoolean("Animated");
        }
    }

    public NBTTagCompound toNBT()
    {
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagCompound pose = new NBTTagCompound();

        for (Map.Entry<String, AnimatorPoseTransform> entry : this.bones.entrySet())
        {
            pose.setTag(entry.getKey(), entry.getValue().toNBT(null));
        }

        tag.setTag("Pose", pose);

        if (!this.animated)
        {
            tag.setBoolean("Animated", this.animated);
        }

        return tag;
    }
}