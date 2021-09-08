package mchorse.chameleon.lib.data.model;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.List;

public class ModelCube
{
    public List<ModelQuad> quads = new ArrayList<ModelQuad>();
    public Vector3f origin = new Vector3f();
    public Vector3f size = new Vector3f();
    public Vector3f pivot = new Vector3f();
    public Vector3f rotation = new Vector3f();
    public float inflate = 0;

    /* Texture mapping */
    public Vector2f boxUV;
    public boolean mirror;

    public ModelUV north;
    public ModelUV east;
    public ModelUV south;
    public ModelUV west;
    public ModelUV up;
    public ModelUV down;

    public void generateQuads(Model model)
    {
        float tw = 1F / model.textureWidth;
        float th = 1F / model.textureHeight;

        float minX = this.origin.x - this.inflate;
        float minY = this.origin.y - this.inflate;
        float minZ = this.origin.z - this.inflate;

        float maxX = this.origin.x + this.size.x + this.inflate;
        float maxY = this.origin.y + this.size.y + this.inflate;
        float maxZ = this.origin.z + this.size.z + this.inflate;

        if (this.boxUV != null)
        {
            /* North */
            float tx = this.boxUV.x + this.size.z;
            float ty = this.boxUV.y + this.size.z;

            this.quads.add(new ModelQuad()
                .vertex(maxX, minY, minZ, (tx + this.size.x) * tw, (ty + this.size.y) * th)
                .vertex(minX, minY, minZ, (tx) * tw, (ty + this.size.y) * th)
                .vertex(minX, maxY, minZ, (tx) * tw, (ty) * th)
                .vertex(maxX, maxY, minZ, (tx + this.size.x) * tw, (ty) * th)
                .normal(0, 0, -1));

            /* East */
            tx = this.boxUV.x;
            ty = this.boxUV.y + this.size.z;

            this.quads.add(new ModelQuad()
                .vertex(maxX, minY, maxZ, (tx) * tw, (ty + this.size.y) * th)
                .vertex(maxX, minY, minZ, (tx + this.size.z) * tw, (ty + this.size.y) * th)
                .vertex(maxX, maxY, minZ, (tx + this.size.z) * tw, (ty) * th)
                .vertex(maxX, maxY, maxZ, (tx) * tw, (ty) * th)
                .normal(1, 0, 0));

            /* South */
            tx = this.boxUV.x + this.size.z * 2 + this.size.x;
            ty = this.boxUV.y + this.size.z;

            this.quads.add(new ModelQuad()
                .vertex(minX, minY, maxZ, (tx) * tw, (ty + this.size.y) * th)
                .vertex(maxX, minY, maxZ, (tx + this.size.x) * tw, (ty + this.size.y) * th)
                .vertex(maxX, maxY, maxZ, (tx + this.size.x) * tw, (ty) * th)
                .vertex(minX, maxY, maxZ, (tx) * tw, (ty) * th)
                .normal(0, 0, 1));

            /* West */
            tx = this.boxUV.x + this.size.z;
            ty = this.boxUV.y + this.size.z;

            this.quads.add(new ModelQuad()
                .vertex(minX, minY, minZ, (tx) * tw, (ty + this.size.y) * th)
                .vertex(minX, minY, maxZ, (tx + this.size.z) * tw, (ty + this.size.y) * th)
                .vertex(minX, maxY, maxZ, (tx + this.size.z) * tw, (ty) * th)
                .vertex(minX, maxY, minZ, (tx) * tw, (ty) * th)
                .normal(-1, 0, 0));

            /* Up */
            tx = this.boxUV.x + this.size.z;
            ty = this.boxUV.y;

            this.quads.add(new ModelQuad()
                .vertex(maxX, maxY, minZ, (tx + this.size.x) * tw, (ty + this.size.z) * th)
                .vertex(minX, maxY, minZ, (tx) * tw, (ty + this.size.z) * th)
                .vertex(minX, maxY, maxZ, (tx) * tw, (ty) * th)
                .vertex(maxX, maxY, maxZ, (tx + this.size.x) * tw, (ty) * th)
                .normal(0, 1, 0));

            /* Down */
            tx = this.boxUV.x + this.size.z + this.size.x;
            ty = this.boxUV.y;

            this.quads.add(new ModelQuad()
                .vertex(minX, minY, minZ, (tx) * tw, (ty + this.size.z) * th)
                .vertex(maxX, minY, minZ, (tx + this.size.x) * tw, (ty + this.size.z) * th)
                .vertex(maxX, minY, maxZ, (tx + this.size.x) * tw, (ty) * th)
                .vertex(minX, minY, maxZ, (tx) * tw, (ty) * th)
                .normal(0, -1, 0));
        }
        else
        {

        }
    }
}