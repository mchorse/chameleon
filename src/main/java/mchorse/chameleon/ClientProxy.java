package mchorse.chameleon;

import mchorse.chameleon.client.ChameleonPack;
import mchorse.chameleon.geckolib.ChameleonLoader;
import mchorse.chameleon.geckolib.ChameleonModel;
import mchorse.chameleon.mclib.ChameleonTree;
import mchorse.mclib.utils.ReflectionUtils;
import mchorse.mclib.utils.files.GlobalTree;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.file.AnimationFile;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.molang.MolangRegistrar;
import software.bernie.shadowed.eliotlash.molang.MolangParser;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
	public static final Map<String, ChameleonModel> chameleonModels = new HashMap<String, ChameleonModel>();
	public static final MolangParser parser;

	public static ChameleonPack pack;
	public static ChameleonTree tree;

	public static File modelsFile;

	private ChameleonLoader loader = new ChameleonLoader();

	static
	{
		parser = new MolangParser();

		MolangRegistrar.registerVars(parser);
	}

	@Override
	public void preInit(FMLPreInitializationEvent event)
	{
		super.preInit(event);

		modelsFile = new File(this.configFile, "models");

		this.reloadModels();
		pack = new ChameleonPack(modelsFile);

		ReflectionUtils.registerResourcePack(pack);
		GlobalTree.TREE.register(tree = new ChameleonTree(modelsFile));
	}

	@Override
	public void reloadModels()
	{
		modelsFile.mkdirs();

		if (!modelsFile.isDirectory())
		{
			return;
		}

		File[] files = modelsFile.listFiles();

		if (files == null)
		{
			return;
		}

		chameleonModels.clear();

		for (File modelFolder : files)
		{
			if (!modelFolder.isDirectory())
			{
				continue;
			}

			this.reloadModelFolder(modelFolder);
		}
	}

	private void reloadModelFolder(File modelFolder)
	{
		File model = null;
		File animation = null;
		File[] files = modelFolder.listFiles();
		long lastUpdated = 0;

		if (files == null)
		{
			return;
		}

		for (File file : files)
		{
			if (model == null && file.getName().endsWith(".geo.json"))
			{
				model = file;
				lastUpdated = Math.max(file.lastModified(), lastUpdated);
			}
			else if (animation == null && file.getName().endsWith(".animation.json"))
			{
				animation = file;
				lastUpdated = Math.max(file.lastModified(), lastUpdated);
			}
		}

		ChameleonModel oldModel = chameleonModels.get(modelFolder.getName());

		if (model != null && animation != null && (oldModel == null || oldModel.lastUpdate < lastUpdated))
		{
			AnimationFile animationFile = this.loader.loadAllAnimations(parser, animation);
			GeoModel geoModel = this.loader.loadModel(model);

			if (animationFile != null && geoModel != null)
			{
				chameleonModels.put(modelFolder.getName(), new ChameleonModel(geoModel, animationFile, lastUpdated));
			}
		}
	}

	@Override
	public Collection<String> getModelKeys()
	{
		return chameleonModels.keySet();
	}
}