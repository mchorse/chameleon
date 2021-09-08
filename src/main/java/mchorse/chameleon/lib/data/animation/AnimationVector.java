package mchorse.chameleon.lib.data.animation;

import mchorse.mclib.math.molang.expressions.MolangExpression;
import net.minecraft.util.EnumFacing;

public class AnimationVector
{
    public AnimationVector next;

    public double time;
    public MolangExpression x;
    public MolangExpression y;
    public MolangExpression z;

    public double getLengthInTicks()
    {
        return this.next == null ? 0 : (this.next.time - this.time) * 20;
    }

    public MolangExpression getStart(EnumFacing.Axis axis)
    {
        if (axis == EnumFacing.Axis.X)
        {
            return this.x;
        }
        else if (axis == EnumFacing.Axis.Y)
        {
            return this.y;
        }

        return this.z;
    }

    public MolangExpression getEnd(EnumFacing.Axis axis)
    {
        if (axis == EnumFacing.Axis.X)
        {
            return this.next == null ? this.x : this.next.x;
        }
        else if (axis == EnumFacing.Axis.Y)
        {
            return this.next == null ? this.y : this.next.z;
        }

        return this.next == null ? this.z : this.next.z;
    }
}