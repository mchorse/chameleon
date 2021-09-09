package mchorse.chameleon.lib.data.model;

import javax.vecmath.Vector2f;

public class ModelUV
{
    public Vector2f origin = new Vector2f();
    public Vector2f size = new Vector2f();

    public static ModelUV from(float x1, float y1, float x2, float y2)
    {
        ModelUV uv = new ModelUV();

        uv.origin.x = x1;
        uv.origin.y = y1;
        uv.size.x = x2 - x1;
        uv.size.y = y2 - y1;

        return uv;
    }

    public float sx()
    {
        return this.origin.x;
    }

    public float sy()
    {
        return this.origin.y;
    }

    public float ex()
    {
        return this.origin.x + this.size.x;
    }

    public float ey()
    {
        return this.origin.y + this.size.y;
    }
}