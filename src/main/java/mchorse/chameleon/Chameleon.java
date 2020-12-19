package mchorse.chameleon;

import mchorse.chameleon.mclib.ValueButtons;
import mchorse.mclib.McLib;
import mchorse.mclib.config.ConfigBuilder;
import mchorse.mclib.events.RegisterConfigEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Chameleon mod
 *
 * GeckoLib powered mod to import animated Blockbench models into
 * Metamorph as morphs
 */
@Mod(modid = Chameleon.MOD_ID, name = Chameleon.MODNAME, version = Chameleon.VERSION, dependencies = "required-after:mclib@[%MCLIB%,);required-after:metamorph@[%METAMORPH%,);required-after:geckolib3@[%GECKOLIB%,)", updateJSON = "https://raw.githubusercontent.com/mchorse/chameleon/master/version.json")
public class Chameleon
{
    /* Sadly "chameleon" mod ID conflicts with another popular mod... */
    public static final String MOD_ID = "chameleon_morph";
    public static final String MODNAME = "Chameleon";
    public static final String VERSION = "%VERSION%";

    @SidedProxy(serverSide = "mchorse.chameleon.CommonProxy", clientSide = "mchorse.chameleon.ClientProxy")
    public static CommonProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        McLib.EVENT_BUS.register(this);

        proxy.preInit(event);
    }

    @SubscribeEvent
    public void onConfig(RegisterConfigEvent event)
    {
        ConfigBuilder builder = event.createBuilder("chameleon");

        /* General */
        builder.category("general").register(new ValueButtons("buttons"));

        event.modules.add(builder.build());
    }
}
