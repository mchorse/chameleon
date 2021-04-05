package mchorse.chameleon.metamorph;

import mchorse.chameleon.Chameleon;
import mchorse.chameleon.ClientProxy;
import mchorse.mclib.utils.files.entries.AbstractEntry;
import mchorse.mclib.utils.files.entries.FileEntry;
import mchorse.mclib.utils.files.entries.FolderEntry;
import mchorse.metamorph.api.creative.categories.MorphCategory;
import mchorse.metamorph.api.creative.sections.MorphSection;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Map;

public class ChameleonSection extends MorphSection
{
    public ChameleonSection(String title)
    {
        super(title);
    }

    @Override
    public void update(World world)
    {
        /* Reload models */
        Chameleon.proxy.reloadModels();

        this.categories.clear();

        Map<String, ChameleonCategory> categories = new HashMap<String, ChameleonCategory>();

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

                        break;
                    }
                }
            }

            morph.name = "chameleon." + key;

            String categoryKey = key.contains("/") ? key.substring(0, key.lastIndexOf("/")) : "";
            ChameleonCategory category = categories.get(categoryKey);

            if (category == null)
            {
                category = new ChameleonCategory(this, "chameleon", categoryKey);
                categories.put(categoryKey, category);
            }

            category.add(morph);
        }

        for (ChameleonCategory category : categories.values())
        {
            category.sort();
            this.categories.add(category);
        }

        this.categories.sort((a, b) -> a.getTitle().compareTo(b.getTitle()));
    }

    public static class ChameleonCategory extends MorphCategory
    {
        private String subtitle;

        public ChameleonCategory(MorphSection parent, String title, String subtitle)
        {
            super(parent, title);

            this.subtitle = subtitle;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public String getTitle()
        {
            return super.getTitle() + (this.subtitle == null || this.subtitle.isEmpty() ? "" : " (" + this.subtitle + ")");
        }
    }
}