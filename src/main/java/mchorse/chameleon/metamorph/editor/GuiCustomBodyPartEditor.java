package mchorse.chameleon.metamorph.editor;

import mchorse.metamorph.bodypart.BodyPart;
import mchorse.metamorph.bodypart.GuiBodyPartEditor;
import mchorse.metamorph.client.gui.editor.GuiAbstractMorph;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiCustomBodyPartEditor extends GuiBodyPartEditor implements IBonePicker
{
	public GuiCustomBodyPartEditor(Minecraft mc, GuiAbstractMorph editor)
	{
		super(mc, editor);
	}

	@Override
	protected void setPart(BodyPart part)
	{
		super.setPart(part);

		if (part != null)
		{
			GuiChameleonMorph parent = (GuiChameleonMorph) this.editor;

			parent.chameleonModelRenderer.boneName = part.limb;
		}
	}

	@Override
	protected void pickLimb(String limbName)
	{
		GuiChameleonMorph parent = (GuiChameleonMorph) this.editor;

		super.pickLimb(limbName);
		parent.chameleonModelRenderer.boneName = limbName;
	}

	@Override
	public void pickBone(String limb)
	{
		try
		{
			this.pickLimb(limb);
			this.limbs.setCurrent(limb);
		}
		catch (Exception e) {}
	}
}
