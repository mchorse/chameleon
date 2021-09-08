package mchorse.chameleon.lib.data.animation;

import java.util.HashMap;
import java.util.Map;

public class Animation
{
    public final String id;

    /**
     * Animation length in seconds
     */
    public double length;

    public Map<String, AnimationPart> parts = new HashMap<String, AnimationPart>();

    public Animation(String id)
    {
        this.id = id;
    }

    public void setLength(double length)
    {
        this.length = length;
    }

    public int getLengthInTicks()
    {
        return (int) Math.round(this.length * 20);
    }
}