package mchorse.chameleon.lib;

import mchorse.chameleon.lib.data.animation.Animations;
import mchorse.chameleon.lib.data.model.ModelBone;
import mchorse.chameleon.lib.data.model.Model;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class ChameleonModel
{
    public Model model;
    public Animations animations;
    public long lastUpdate;

    private List<String> boneNames;
    private boolean isStatic;
    private List<File> files;

    public ChameleonModel(Model model, Animations animations, List<File> files, long lastUpdate)
    {
        this.model = model;
        this.animations = animations;
        this.files = files;
        this.lastUpdate = lastUpdate;
        this.isStatic = animations == null || animations.getAll().isEmpty();
    }

    public List<String> getBoneNames()
    {
        if (this.boneNames != null)
        {
            return this.boneNames;
        }

        return this.boneNames = this.getBoneNames(new ArrayList<String>(), this.model.bones);
    }

    private List<String> getBoneNames(List<String> boneNames, List<ModelBone> bones)
    {
        for (ModelBone bone : bones)
        {
            boneNames.add(bone.id);

            this.getBoneNames(boneNames, bone.children);
        }

        return boneNames;
    }

    public boolean isStatic()
    {
        return this.isStatic;
    }

    public boolean isStillPresent()
    {
        for (File file : this.files)
        {
            if (!file.exists())
            {
                return false;
            }
        }

        return true;
    }
}
