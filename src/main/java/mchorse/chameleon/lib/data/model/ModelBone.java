package mchorse.chameleon.lib.data.model;

import java.util.ArrayList;
import java.util.List;

import mchorse.mclib.utils.Color;

public class ModelBone
{
    public final String id;
    public List<ModelBone> children = new ArrayList<ModelBone>();
    public List<ModelCube> cubes = new ArrayList<ModelCube>();
    public boolean visible = true;

    public boolean absoluteBrightness = false;
    public float glow = 0F;
    public Color color = new Color(1F, 1F, 1F, 1F);

    public ModelTransform initial = new ModelTransform();
    public ModelTransform current = new ModelTransform();

    public ModelBone(String id)
    {
        this.id = id;
    }
}