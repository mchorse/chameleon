package mchorse.chameleon.metamorph;

import mchorse.chameleon.metamorph.editor.GuiChameleonMorph;
import mchorse.metamorph.api.IMorphFactory;
import mchorse.metamorph.api.MorphManager;
import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.client.gui.editor.GuiAbstractMorph;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;

import java.util.List;

/**
 * Blockbuster morph factory
 *
 * This factory is responsible for adding all custom modeled morphs provided by
 * a user (in his config folder), the server (in world save's blockbuster
 * folder) or added by API (steve, alex and fred).
 */
public class ChameleonFactory implements IMorphFactory
{
    public ChameleonSection section;

    @Override
    public void register(MorphManager manager)
    {
        manager.list.register(this.section = new ChameleonSection("chameleon"));
    }

    @Override
    public void registerMorphEditors(Minecraft mc, List<GuiAbstractMorph> editors)
    {
        editors.add(new GuiChameleonMorph(mc));
    }

    @Override
    public AbstractMorph getMorphFromNBT(NBTTagCompound tag)
    {
        ChameleonMorph morph = new ChameleonMorph();

        morph.fromNBT(tag);

        return morph;
    }

    @Override
    public boolean hasMorph(String morph)
    {
        return morph.startsWith("chameleon.");
    }
}