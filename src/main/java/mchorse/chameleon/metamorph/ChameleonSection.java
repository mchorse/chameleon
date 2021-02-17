package mchorse.chameleon.metamorph;

import mchorse.chameleon.Chameleon;
import mchorse.chameleon.ClientProxy;
import mchorse.mclib.utils.files.entries.AbstractEntry;
import mchorse.mclib.utils.files.entries.FileEntry;
import mchorse.mclib.utils.files.entries.FolderEntry;
import mchorse.metamorph.api.creative.categories.MorphCategory;
import mchorse.metamorph.api.creative.sections.MorphSection;
import net.minecraft.world.World;

public class ChameleonSection extends MorphSection
{
    public MorphCategory models;

    public ChameleonSection(String title)
    {
        super(title);

        this.models = new MorphCategory(this, "chameleon");
    }

    @Override
    public void update(World world)
    {
        /* Reload models */
        Chameleon.proxy.reloadModels();

        this.categories.clear();
        this.models.clear();

        for (String key : Chameleon.proxy.getModelKeys())
        {
            ChameleonMorph morph = new ChameleonMorph();
            FolderEntry skins = ClientProxy.tree.getByPath(key + "/skins/", null);

            if (skins != null)
            {
                for (AbstractEntry entry : skins.getEntries())
                {
                    if (entry instanceof FileEntry)
                    {
                        morph.skin = ((FileEntry) entry).resource;
                    }
                }
            }

            morph.name = "chameleon." + key;

            this.models.add(morph);
        }

        this.add(this.models);
    }
}