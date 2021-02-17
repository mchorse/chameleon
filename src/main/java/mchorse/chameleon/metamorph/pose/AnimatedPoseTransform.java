package mchorse.chameleon.metamorph.pose;

import net.minecraft.nbt.NBTTagCompound;

public class AnimatedPoseTransform extends AnimatedTransform
{
    public static final int FIXED = 0;
    public static final int ANIMATED = 1;

    public float fixed = ANIMATED;

    public AnimatedPoseTransform(String name)
    {
        super(name);
    }

    public AnimatedPoseTransform clone()
    {
        AnimatedPoseTransform item = new AnimatedPoseTransform(this.boneName);

        item.x = this.x;
        item.y = this.y;
        item.z = this.z;
        item.scaleX = this.scaleX;
        item.scaleY = this.scaleY;
        item.scaleZ = this.scaleZ;
        item.rotateX = this.rotateX;
        item.rotateY = this.rotateY;
        item.rotateZ = this.rotateZ;
        item.fixed = this.fixed;

        return item;
    }

    @Override
    public boolean equals(Object obj)
    {
        boolean result = super.equals(obj);

        if (obj instanceof AnimatedPoseTransform)
        {
            result = result && this.fixed == ((AnimatedPoseTransform) obj).fixed;
        }

        return result;
    }

    @Override
    public void fromNBT(NBTTagCompound tag)
    {
        super.fromNBT(tag);

        if (tag.hasKey("F")) this.fixed = tag.getBoolean("F") ? ANIMATED : FIXED;
    }

    @Override
    public NBTTagCompound toNBT(NBTTagCompound tag)
    {
        tag = super.toNBT(tag);

        if (this.fixed != ANIMATED) tag.setBoolean("F", false);

        return tag;
    }
}