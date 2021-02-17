package mchorse.chameleon.mclib;

import mchorse.chameleon.ClientProxy;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiButtonElement;
import mchorse.mclib.client.gui.utils.Elements;
import mchorse.mclib.client.gui.utils.GuiUtils;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.mclib.config.gui.GuiConfigPanel;
import mchorse.mclib.config.values.ValueGUI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.List;

public class ValueButtons extends ValueGUI
{
    public ValueButtons(String id)
    {
        super(id);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public List<GuiElement> getFields(Minecraft mc, GuiConfigPanel config)
    {
        GuiButtonElement models = new GuiButtonElement(mc, IKey.lang("chameleon.gui.config.models"), (button) -> GuiUtils.openWebLink(ClientProxy.modelsFile.toURI()));
        GuiButtonElement tutorial = new GuiButtonElement(mc, IKey.lang("chameleon.gui.config.tutorial"), (button) -> GuiUtils.openWebLink(this.getTutorialURL()));
        GuiButtonElement discord = new GuiButtonElement(mc, IKey.lang("chameleon.gui.config.discord"), (button) -> GuiUtils.openWebLink(this.getDiscordURL()));
        GuiButtonElement wiki = new GuiButtonElement(mc, IKey.lang("chameleon.gui.config.wiki"), (button) -> GuiUtils.openWebLink(this.getWikiURL()));

        GuiElement first = Elements.row(mc, 5, 0, 20, models, tutorial);
        GuiElement second = Elements.row(mc, 5, 0, 20, discord, wiki);

        return Arrays.asList(first, second);
    }

    @SideOnly(Side.CLIENT)
    private String getTutorialURL()
    {
        return this.getLangOrDefault("chameleon.gui.url.tutorial", "https://www.youtube.com/playlist?list=PLLnllO8nnzE94k_xh3tqX58_tJzx92NcG");
    }

    @SideOnly(Side.CLIENT)
    private String getDiscordURL()
    {
        return this.getLangOrDefault("chameleon.gui.url.discord", "https://discord.gg/qfxrqUF");
    }

    @SideOnly(Side.CLIENT)
    private String getWikiURL()
    {
        return this.getLangOrDefault("chameleon.gui.url.wiki", "https://github.com/mchorse/chameleon/wiki");
    }

    @SideOnly(Side.CLIENT)
    private String getLangOrDefault(String key, String def)
    {
        String string = I18n.format(key);

        return key.equals(key) ? def : key;
    }
}