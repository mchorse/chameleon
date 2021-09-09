package mchorse.chameleon.lib.data.animation;

import mchorse.chameleon.lib.MolangHelper;
import mchorse.mclib.utils.Interpolation;
import mchorse.mclib.utils.Interpolations;
import net.minecraft.util.EnumFacing;

import java.util.Objects;

public enum AnimationInterpolation
{
    LINEAR(null, Interpolation.LINEAR), HERMITE()
    {
        @Override
        public double interpolate(AnimationVector vector, MolangHelper.Component component, EnumFacing.Axis axis, double factor)
        {
            double start = MolangHelper.getValue(vector.getStart(axis), component, axis);
            double destination = MolangHelper.getValue(vector.getEnd(axis), component, axis);

            double pre = start;
            double post = destination;

            if (vector.prev != null)
            {
                pre = MolangHelper.getValue(vector.prev.getStart(axis), component, axis);
            }

            if (vector.next != null)
            {
                post = MolangHelper.getValue(vector.next.getEnd(axis), component, axis);
            }

            return Interpolations.cubicHermite(pre, start, destination, post, factor);
        }
    },
    QUAD_IN("easeInQuad", Interpolation.QUAD_IN), QUAD_OUT("easeOutQuad", Interpolation.QUAD_OUT), QUAD_INOUT("easeInOutQuad", Interpolation.QUAD_INOUT),
    CUBIC_IN("easeInCubic", Interpolation.CUBIC_IN), CUBIC_OUT("easeOutCubic", Interpolation.CUBIC_OUT), CUBIC_INOUT("easeInOutCubic", Interpolation.CUBIC_INOUT),
    EXP_IN("easeInExpo", Interpolation.EXP_IN), EXP_OUT("easeOutExpo", Interpolation.EXP_OUT), EXP_INOUT("easeInOutExpo", Interpolation.EXP_INOUT),
    BACK_IN("easeInBack", Interpolation.BACK_IN), BACK_OUT("easeOutBack", Interpolation.BACK_OUT), BACK_INOUT("easeInOutBack", Interpolation.BACK_INOUT),
    /* These are inverted (i.e. in and out swapped places) because that's how the GeckoLib plugin shows */
    ELASTIC_IN("easeInElastic", Interpolation.ELASTIC_OUT), ELASTIC_OUT("easeOutElastic", Interpolation.ELASTIC_IN), ELASTIC_INOUT("easeInOutElastic", Interpolation.ELASTIC_INOUT),
    BOUNCE_IN("easeInBounce", Interpolation.BOUNCE_OUT), BOUNCE_OUT("easeOutBounce", Interpolation.BOUNCE_IN), BOUNCE_INOUT("easeInOutBounce", Interpolation.BOUNCE_INOUT);

    public final String name;
    public final Interpolation interp;

    public static AnimationInterpolation byName(String easing)
    {
        for (AnimationInterpolation interp : values())
        {
            if (Objects.equals(interp.name, easing))
            {
                return interp;
            }
        }

        return LINEAR;
    }

    private AnimationInterpolation(String name, Interpolation interp)
    {
        this.name = name;
        this.interp = interp;
    }

    private AnimationInterpolation()
    {
        this.name = null;
        this.interp = null;
    }

    public double interpolate(AnimationVector vector, MolangHelper.Component component, EnumFacing.Axis axis, double factor)
    {
        if (vector.next != null && vector.next.interp != null && vector.next.interp.interp != null)
        {
            factor = vector.next.interp.interp.interpolate(0, 1, factor);
        }

        double start = MolangHelper.getValue(vector.getStart(axis), component, axis);
        double destination = MolangHelper.getValue(vector.getEnd(axis), component, axis);

        return Interpolations.lerp(start, destination, factor);
    }
}