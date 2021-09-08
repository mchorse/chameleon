package mchorse.chameleon;

import mchorse.chameleon.client.ChameleonPack;
import mchorse.chameleon.lib.ChameleonLoader;
import mchorse.chameleon.lib.ChameleonModel;
import mchorse.chameleon.lib.MolangHelper;
import mchorse.chameleon.lib.data.animation.Animations;
import mchorse.chameleon.lib.data.model.Model;
import mchorse.chameleon.mclib.ChameleonTree;
import mchorse.mclib.math.Variable;
import mchorse.mclib.math.molang.MolangParser;
import mchorse.mclib.utils.ReflectionUtils;
import mchorse.mclib.utils.files.GlobalTree;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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

        MolangHelper.registerVars(parser);

        /* Additional Chameleon specific variables */
        parser.register(new Variable("query.head_yaw", 0));
        parser.register(new Variable("query.head_pitch", 0));

        parser.register(new Variable("query.velocity", 0));
        parser.register(new Variable("query.limb_swing", 0));
        parser.register(new Variable("query.limb_swing_amount", 0));
        parser.register(new Variable("query.age", 0));
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

        List<String> toCheck = new ArrayList<String>(chameleonModels.keySet());

        this.recursiveReloadModel(modelsFile, "", toCheck);

        /* Check and remove model if it got removed */
        for (String key : toCheck)
        {
            ChameleonModel model = chameleonModels.get(key);

            if (model != null && !model.isStillPresent())
            {
                chameleonModels.remove(key);
            }
        }
    }

    private void recursiveReloadModel(File folder, String prefix, List<String> toCheck)
    {
        File[] files = folder.listFiles();

        if (files == null)
        {
            return;
        }

        for (File modelFile : files)
        {
            if (modelFile.isDirectory())
            {
                if (!this.reloadModelFolder(modelFile, prefix, toCheck) && !modelFile.getName().equals("skins"))
                {
                    this.recursiveReloadModel(modelFile, prefix + modelFile.getName() + "/", toCheck);
                }
            }
        }
    }

    private boolean reloadModelFolder(File modelFolder, String prefix, List<String> toCheck)
    {
        File model = null;
        List<File> animations = new ArrayList<File>();
        File[] files = modelFolder.listFiles();
        long lastUpdated = 0;

        if (files == null)
        {
            return false;
        }

        for (File file : files)
        {
            if (model == null && file.getName().endsWith(".geo.json"))
            {
                model = file;
                lastUpdated = Math.max(file.lastModified(), lastUpdated);
            }
            else if (animations.isEmpty() && file.getName().endsWith(".animation.json"))
            {
                animations.add(file);
                lastUpdated = Math.max(file.lastModified(), lastUpdated);
            }
        }

        /* Scan for animation files also in animations folder */
        File animationsFolder = new File(modelFolder, "animations");

        if (animationsFolder.isDirectory())
        {
            File[] animationsInFolder = animationsFolder.listFiles();

            if (animationsInFolder != null)
            {
                for (File animationFile : animationsInFolder)
                {
                    if (animationFile.getName().endsWith(".animation.json"))
                    {
                        animations.add(animationFile);
                        lastUpdated = Math.max(animationFile.lastModified(), lastUpdated);
                    }
                }
            }
        }

        /* Load model and animation */
        String key = prefix + modelFolder.getName();
        ChameleonModel oldModel = chameleonModels.get(key);

        if (model != null && (oldModel == null || oldModel.lastUpdate < lastUpdated))
        {
            Model theModel = this.loader.loadModel(model);
            Animations theAnimations = this.loadAnimations(animations);

            if (theModel != null)
            {
                List<File> trackingFiles = new ArrayList<File>();

                trackingFiles.add(model);
                chameleonModels.put(key, new ChameleonModel(theModel, theAnimations, trackingFiles, lastUpdated));
                toCheck.remove(key);
            }
        }

        return chameleonModels.containsKey(key);
    }

    private Animations loadAnimations(List<File> files)
    {
        Animations animations = new Animations();

        for (File animationFile : files)
        {
            this.loader.loadAllAnimations(parser, animationFile, animations);
        }

        return animations;
    }

    @Override
    public Collection<String> getModelKeys()
    {
        return chameleonModels.keySet();
    }
}