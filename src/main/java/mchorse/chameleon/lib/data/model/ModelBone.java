package mchorse.chameleon.lib.data.model;

import java.util.ArrayList;
import java.util.List;

public class ModelBone
{
    public final String id;
    public List<ModelBone> children = new ArrayList<ModelBone>();
    public List<ModelCube> cubes = new ArrayList<ModelCube>();
    public boolean visible = true;

    public ModelTransform initial = new ModelTransform();
    public ModelTransform current = new ModelTransform();

    public ModelBone(String id)
    {
        this.id = id;
    }
}