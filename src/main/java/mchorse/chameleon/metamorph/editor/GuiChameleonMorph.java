package mchorse.chameleon.metamorph.editor;

import mchorse.chameleon.ClientProxy;
import mchorse.chameleon.geckolib.ChameleonModel;
import mchorse.chameleon.metamorph.ChameleonMorph;
import mchorse.mclib.client.gui.framework.elements.GuiModelRenderer;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiButtonElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTexturePicker;
import mchorse.mclib.client.gui.utils.Icons;
import mchorse.mclib.client.gui.utils.Label;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.mclib.utils.files.entries.AbstractEntry;
import mchorse.mclib.utils.files.entries.FileEntry;
import mchorse.mclib.utils.files.entries.FolderEntry;
import mchorse.mclib.utils.resources.RLUtils;
import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.bodypart.GuiBodyPartEditor;
import mchorse.metamorph.client.gui.editor.GuiAbstractMorph;
import mchorse.metamorph.client.gui.editor.GuiMorphPanel;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@SideOnly(Side.CLIENT)
public class GuiChameleonMorph extends GuiAbstractMorph<ChameleonMorph>
{
	public GuiCustomBodyPartEditor bodyPart;
	public GuiChameleonMainPanel mainPanel;
	public GuiChameleonModelRenderer chameleonModelRenderer;

	public GuiChameleonMorph(Minecraft mc)
	{
		super(mc);

		this.bodyPart = new GuiCustomBodyPartEditor(mc, this);
		this.mainPanel = new GuiChameleonMainPanel(mc, this);
		this.defaultPanel = this.mainPanel;

		this.registerPanel(this.bodyPart, IKey.lang("chameleon.gui.editor.body_part"), Icons.LIMB);
		this.registerPanel(this.mainPanel, IKey.lang("chameleon.gui.editor.main"), Icons.MATERIAL);
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
		if (this.view.delegate == this.bodyPart)
		{
			this.bodyPart.setLimb(limb);
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

		this.chameleonModelRenderer.boneName = "";
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

	/**
	 * Custom model morph panel which allows editing custom textures
	 * for materials of the custom model morph
	 */
	public static class GuiChameleonMainPanel extends GuiMorphPanel<ChameleonMorph, GuiChameleonMorph>
	{
		/* Materials */
		public GuiButtonElement skin;
		public GuiTexturePicker picker;

		public GuiChameleonMainPanel(Minecraft mc, GuiChameleonMorph editor)
		{
			super(mc, editor);

			Consumer<ResourceLocation> skin = (rl) ->
			{
				this.morph.skin = RLUtils.clone(rl);
			};

			/* Materials view */
			this.skin = new GuiButtonElement(mc, IKey.lang("chameleon.gui.editor.pick_skin"), (b) ->
			{
				this.picker.refresh();
				this.picker.fill(this.morph.skin);
				this.picker.callback = skin;
				this.add(this.picker);
				this.picker.resize();
			});
			this.picker = new GuiTexturePicker(mc, skin);

			this.skin.flex().relative(this).set(10, 10, 110, 20);
			this.picker.flex().relative(this).wh(1F, 1F);

			this.add(this.skin);
		}

		@Override
		public void fillData(ChameleonMorph morph)
		{
			super.fillData(morph);

			this.picker.removeFromParent();
		}
	}
}
