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

        float minX = (this.origin.x - this.inflate) / 16F;
        float minY = (this.origin.y - this.inflate) / 16F;
        float minZ = (this.origin.z - this.inflate) / 16F;

        float maxX = (this.origin.x + this.size.x + this.inflate) / 16F;
        float maxY = (this.origin.y + this.size.y + this.inflate) / 16F;
        float maxZ = (this.origin.z + this.size.z + this.inflate) / 16F;

        if (this.boxUV != null)
        {
            /* North */
            float tMinX = this.boxUV.x + this.size.z;
            float tMinY = this.boxUV.y + this.size.z;
            float tMaxX = tMinX + this.size.x;
            float tMaxY = tMinY + this.size.y;

            if (this.mirror)
            {
                float tmp = tMaxX;

                tMaxX = tMinX;
                tMinX = tmp;
            }

            this.quads.add(new ModelQuad()
                .vertex(maxX, minY, minZ, tMaxX * tw, tMaxY * th)
                .vertex(minX, minY, minZ, tMinX * tw, tMaxY * th)
                .vertex(minX, maxY, minZ, tMinX * tw, tMinY * th)
                .vertex(maxX, maxY, minZ, tMaxX * tw, tMinY * th)
                .normal(0, 0, -1));

            /* East */
            tMinX = this.boxUV.x;
            tMinY = this.boxUV.y + this.size.z;
            tMaxX = tMinX + this.size.z;
            tMaxY = tMinY + this.size.y;

            if (this.mirror)
            {
                tMinX = this.boxUV.x + this.size.z + this.size.x;
                tMinY = this.boxUV.y + this.size.z;
                tMaxX = tMinX + this.size.z;
                tMaxY = tMinY + this.size.y;
            }

            this.quads.add(new ModelQuad()
                .vertex(maxX, minY, maxZ, tMinX * tw, tMaxY * th)
                .vertex(maxX, minY, minZ, tMaxX * tw, tMaxY * th)
                .vertex(maxX, maxY, minZ, tMaxX * tw, tMinY * th)
                .vertex(maxX, maxY, maxZ, tMinX * tw, tMinY * th)
                .normal(1, 0, 0));

            /* South */
            tMinX = this.boxUV.x + this.size.z * 2 + this.size.x;
            tMinY = this.boxUV.y + this.size.z;
            tMaxX = tMinX + this.size.x;
            tMaxY = tMinY + this.size.y;

            if (this.mirror)
            {
                float tmp = tMaxX;

                tMaxX = tMinX;
                tMinX = tmp;
            }

            this.quads.add(new ModelQuad()
                .vertex(minX, minY, maxZ, tMinX * tw, tMaxY * th)
                .vertex(maxX, minY, maxZ, tMaxX * tw, tMaxY * th)
                .vertex(maxX, maxY, maxZ, tMaxX * tw, tMinY * th)
                .vertex(minX, maxY, maxZ, tMinX * tw, tMinY * th)
                .normal(0, 0, 1));

            /* West */
            tMinX = this.boxUV.x + this.size.z + this.size.x;
            tMinY = this.boxUV.y + this.size.z;
            tMaxX = tMinX + this.size.z;
            tMaxY = tMinY + this.size.y;

            if (this.mirror)
            {
                tMinX = this.boxUV.x;
                tMinY = this.boxUV.y + this.size.z;
                tMaxX = tMinX + this.size.z;
                tMaxY = tMinY + this.size.y;
            }

            this.quads.add(new ModelQuad()
                .vertex(minX, minY, minZ, tMinX * tw, tMaxY * th)
                .vertex(minX, minY, maxZ, tMaxX * tw, tMaxY * th)
                .vertex(minX, maxY, maxZ, tMaxX * tw, tMinY * th)
                .vertex(minX, maxY, minZ, tMinX * tw, tMinY * th)
                .normal(-1, 0, 0));

            /* Up */
            tMinX = this.boxUV.x + this.size.z;
            tMinY = this.boxUV.y;
            tMaxX = tMinX + this.size.x;
            tMaxY = tMinY + this.size.z;

            if (this.mirror)
            {
                float tmp = tMaxX;

                tMaxX = tMinX;
                tMinX = tmp;
            }

            this.quads.add(new ModelQuad()
                .vertex(maxX, maxY, minZ, tMaxX * tw, tMaxY * th)
                .vertex(minX, maxY, minZ, tMinX * tw, tMaxY * th)
                .vertex(minX, maxY, maxZ, tMinX * tw, tMinY * th)
                .vertex(maxX, maxY, maxZ, tMaxX * tw, tMinY * th)
                .normal(0, 1, 0));

            /* Down */
            tMinX = this.boxUV.x + this.size.z + this.size.x;
            tMinY = this.boxUV.y;
            tMaxX = tMinX + this.size.x;
            tMaxY = tMinY + this.size.z;

            if (this.mirror)
            {
                float tmp = tMaxX;

                tMaxX = tMinX;
                tMinX = tmp;
            }

            this.quads.add(new ModelQuad()
                .vertex(minX, minY, minZ, tMinX * tw, tMaxY * th)
                .vertex(maxX, minY, minZ, tMaxX * tw, tMaxY * th)
                .vertex(maxX, minY, maxZ, tMaxX * tw, tMinY * th)
                .vertex(minX, minY, maxZ, tMinX * tw, tMinY * th)
                .normal(0, -1, 0));
        }
        else
        {

        }
    }
}