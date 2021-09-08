package mchorse.chameleon.lib.data.model;

import java.util.ArrayList;
import java.util.List;

public class Model
{
    public String id;
    public int textureWidth;
    public int textureHeight;

    public List<ModelBone> bones = new ArrayList<ModelBone>();
}
