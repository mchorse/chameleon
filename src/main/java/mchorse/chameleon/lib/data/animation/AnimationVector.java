package mchorse.chameleon.lib.data.animation;

import mchorse.mclib.math.molang.expressions.MolangExpression;
import net.minecraft.util.EnumFacing;

public class AnimationVector
{
    public AnimationVector prev;
    public AnimationVector next;

    public double time;
    public AnimationInterpolation interp = AnimationInterpolation.LINEAR;
    public MolangExpression x;
    public MolangExpression y;
    public MolangExpression z;

    public MolangExpression preX;
    public MolangExpression preY;
    public MolangExpression preZ;

    public double getLengthInTicks()
    {
        return this.next == null ? 0 : (this.next.time - this.time) * 20D;
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
            return this.next == null ? this.x : this.next.preX;
        }
        else if (axis == EnumFacing.Axis.Y)
        {
            return this.next == null ? this.y : this.next.preY;
        }

        return this.next == null ? this.z : this.next.preZ;
    }
}