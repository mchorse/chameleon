package mchorse.chameleon;

import mchorse.chameleon.metamorph.ChameleonFactory;
import mchorse.metamorph.api.MorphManager;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.File;
import java.util.Collection;
import java.util.Collections;

public class CommonProxy
{
    public File configFile;

    public void preInit(FMLPreInitializationEvent event)
    {
        this.configFile = new File(event.getModConfigurationDirectory(), "chameleon");

        MorphManager.INSTANCE.factories.add(new ChameleonFactory());
    }

    public void reloadModels()
    {}

    public Collection<String> getModelKeys()
    {
        return Collections.emptyList();
    }
}