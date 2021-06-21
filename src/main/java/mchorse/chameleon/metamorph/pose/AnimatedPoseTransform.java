package mchorse.chameleon.metamorph.pose;

import mchorse.mclib.utils.Color;
import mchorse.mclib.utils.Interpolation;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants.NBT;

public class AnimatedPoseTransform extends AnimatedTransform
{
    public static final int FIXED = 0;
    public static final int ANIMATED = 1;

    public float fixed = ANIMATED;
    
    public float glow = 0.0f;
    public Color color = new Color(1f, 1f, 1f, 1f);

    public AnimatedPoseTransform(String name)
    {
        super(name);
    }

    public AnimatedPoseTransform clone()
    {
        AnimatedPoseTransform item = new AnimatedPoseTransform(this.boneName);

        item.copy(this);

        return item;
    }

    public void copy(AnimatedPoseTransform transform)
    {
        this.x = transform.x;
        this.y = transform.y;
        this.z = transform.z;
        this.scaleX = transform.scaleX;
        this.scaleY = transform.scaleY;
        this.scaleZ = transform.scaleZ;
        this.rotateX = transform.rotateX;
        this.rotateY = transform.rotateY;
        this.rotateZ = transform.rotateZ;
        this.fixed = transform.fixed;
        this.glow = transform.glow;
        this.color.copy(transform.color);
    }

    @Override
    public boolean equals(Object obj)
    {
        boolean result = super.equals(obj);

        if (obj instanceof AnimatedPoseTransform)
        {
            result = result && this.fixed == ((AnimatedPoseTransform) obj).fixed;
            result = result && Math.abs(this.glow - ((AnimatedPoseTransform) obj).glow) < 0.0001;
            result = result && this.color.equals(((AnimatedPoseTransform) obj).color);
        }

        return result;
    }

    @Override
    public void fromNBT(NBTTagCompound tag)
    {
        super.fromNBT(tag);

        if (tag.hasKey("F", NBT.TAG_BYTE)) this.fixed = tag.getBoolean("F") ? ANIMATED : FIXED;
        if (tag.hasKey("G", NBT.TAG_FLOAT)) this.glow = tag.getFloat("G");
        if (tag.hasKey("C", NBT.TAG_INT)) this.color.set(tag.getInteger("C"));
    }

    @Override
    public NBTTagCompound toNBT(NBTTagCompound tag)
    {
        tag = super.toNBT(tag);

        if (this.fixed != ANIMATED) tag.setBoolean("F", false);
        if (this.glow > 0.0001) tag.setFloat("G", this.glow);
        if (this.color.getRGBAColor() != 0xFFFFFFFF) tag.setInteger("C", this.color.getRGBAColor());

        return tag;
    }

    @Override
    public void interpolate(AnimatedTransform a, AnimatedTransform b, float x, Interpolation interp)
    {
        super.interpolate(a, b, x, interp);
        
        float glow = 0.0f;
        float cr, cg, cb, ca;
        cr = cg = cb = ca = 1.0f;
        
        if (a instanceof AnimatedPoseTransform)
        {
            AnimatedPoseTransform l = (AnimatedPoseTransform) a;
            glow = l.glow;
            cr = l.color.r;
            cg = l.color.g;
            cb = l.color.b;
            ca = l.color.a;
        }
        
        if (b instanceof AnimatedPoseTransform)
        {
            AnimatedPoseTransform l = (AnimatedPoseTransform) b;
            glow = interp.interpolate(glow, l.glow, x);
            cr = interp.interpolate(cr, l.color.r, x);
            cg = interp.interpolate(cg, l.color.g, x);
            cb = interp.interpolate(cb, l.color.b, x);
            ca = interp.interpolate(ca, l.color.a, x);
        }
        else
        {
            glow = interp.interpolate(glow, 0.0f, x);
            cr = interp.interpolate(cr, 1.0f, x);
            cg = interp.interpolate(cg, 1.0f, x);
            cb = interp.interpolate(cb, 1.0f, x);
            ca = interp.interpolate(ca, 1.0f, x);
        }
        
        this.glow = glow;
        this.color.set(cr, cg, cb, ca);
    }
}