package mchorse.chameleon.metamorph.editor;

import mchorse.chameleon.ClientProxy;
import mchorse.chameleon.geckolib.ChameleonModel;
import mchorse.chameleon.metamorph.ChameleonMorph;
import mchorse.mclib.client.gui.framework.GuiBase;
import mchorse.mclib.client.gui.framework.elements.GuiModelRenderer;
import mchorse.mclib.client.gui.utils.Icons;
import mchorse.mclib.client.gui.utils.Label;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.mclib.utils.files.entries.AbstractEntry;
import mchorse.mclib.utils.files.entries.FileEntry;
import mchorse.mclib.utils.files.entries.FolderEntry;
import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.client.gui.editor.GuiAbstractMorph;
import mchorse.metamorph.client.gui.editor.GuiMorphPanel;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiChameleonMorph extends GuiAbstractMorph<ChameleonMorph>
{
    public GuiActionsPanel actionsPanel;
    public GuiCustomBodyPartEditor bodyPart;
    public GuiChameleonMainPanel mainPanel;
    public GuiChameleonModelRenderer chameleonModelRenderer;

    public GuiChameleonMorph(Minecraft mc)
    {
        super(mc);

        this.mainPanel = new GuiChameleonMainPanel(mc, this);
        this.bodyPart = new GuiCustomBodyPartEditor(mc, this);
        this.actionsPanel = new GuiActionsPanel(mc, this);
        this.defaultPanel = this.mainPanel;

        this.registerPanel(this.actionsPanel, IKey.lang("chameleon.gui.editor.actions.actions"), Icons.MORE);
        this.registerPanel(this.bodyPart, IKey.lang("chameleon.gui.editor.body_part"), Icons.LIMB);
        this.registerPanel(this.mainPanel, IKey.lang("chameleon.gui.editor.main"), Icons.GEAR);

        this.keys().register(IKey.lang("chameleon.gui.editor.pick_skin"), Keyboard.KEY_P, () ->
        {
            this.setPanel(this.mainPanel);

            this.mainPanel.skin.clickItself(GuiBase.getCurrent());
        }).held(Keyboard.KEY_LSHIFT);
    }

    @Override
    protected GuiModelRenderer createMorphRenderer(Minecraft mc)
    {
        this.chameleonModelRenderer = new GuiChameleonModelRenderer(mc);
        this.chameleonModelRenderer.picker(this::pickLimb);

        return this.chameleonModelRenderer;
    }

    private void pickLimb(String limb)
    {
        if (this.view.delegate instanceof IBonePicker)
        {
            ((IBonePicker) this.view.delegate).pickBone(limb);
        }
    }

    @Override
    public boolean canEdit(AbstractMorph morph)
    {
        return morph instanceof ChameleonMorph;
    }

    @Override
    public void startEdit(ChameleonMorph morph)
    {
        super.startEdit(morph);

        ChameleonModel model = morph.getModel();

        morph.parts.reinitBodyParts();

        if (model == null)
        {
            this.bodyPart.setLimbs(Collections.emptyList());
        }
        else
        {
            this.bodyPart.setLimbs(model.getBoneNames());
        }
    }

    @Override
    public void setPanel(GuiMorphPanel panel)
    {
        this.chameleonModelRenderer.boneName = "";

        super.setPanel(panel);
    }

    @Override
    public List<Label<NBTTagCompound>> getPresets(ChameleonMorph morph)
    {
        List<Label<NBTTagCompound>> list = new ArrayList<Label<NBTTagCompound>>();
        String key = morph.getKey();

        this.addSkins(morph, list, "Skin", ClientProxy.tree.getByPath(key + "/skins", null));

        return list;
    }

    public void addSkins(AbstractMorph morph, List<Label<NBTTagCompound>> list, String name, FolderEntry entry)
    {
        if (entry == null)
        {
            return;
        }

        for (AbstractEntry childEntry : entry.getEntries())
        {
            if (childEntry instanceof FileEntry)
            {
                ResourceLocation location = ((FileEntry) childEntry).resource;
                String label = location.getResourcePath();
                int index = label.indexOf("/skins/");

                if (index != -1)
                {
                    label = label.substring(index + 7);
                }

                this.addPreset(morph, list, name, label, location);
            }
            else if (childEntry instanceof FolderEntry)
            {
                FolderEntry childFolder = (FolderEntry) childEntry;

                if (!childFolder.isTop())
                {
                    this.addSkins(morph, list, name, childFolder);
                }
            }
        }
    }

    public void addPreset(AbstractMorph morph, List<Label<NBTTagCompound>> list, String name, String label, ResourceLocation skin)
    {
        try
        {
            NBTTagCompound tag = morph.toNBT();

            tag.setString(name, skin.toString());
            list.add(new Label<>(IKey.str(label), tag));
        }
        catch (Exception e)
        {}
    }

}
