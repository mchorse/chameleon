package mchorse.chameleon;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * Chameleon mod
 *
 * GeckoLib powered mod to import animated Blockbench models into
 * Metamorph
 */
@Mod(modid = Chameleon.MOD_ID, name = Chameleon.MODNAME, version = Chameleon.VERSION, dependencies = "required-after:mclib@[%MCLIB%,);required-after:metamorph@[%METAMORPH%,)")
public class Chameleon
{
    public static final String MOD_ID = "chameleon";
    public static final String MODNAME = "Chameleon";
    public static final String VERSION = "%VERSION%";

    @SidedProxy(serverSide = "mchorse.chameleon.CommonProxy", clientSide = "mchorse.chameleon.ClientProxy")
    public static CommonProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        proxy.preInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {}
}
