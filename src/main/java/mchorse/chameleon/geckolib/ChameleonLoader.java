package mchorse.chameleon.geckolib;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mchorse.chameleon.Chameleon;
import net.minecraft.client.util.JsonException;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.IOUtils;
import software.bernie.geckolib3.file.AnimationFile;
import software.bernie.geckolib3.geo.exception.GeoModelException;
import software.bernie.geckolib3.geo.raw.pojo.Converter;
import software.bernie.geckolib3.geo.raw.pojo.FormatVersion;
import software.bernie.geckolib3.geo.raw.pojo.RawGeoModel;
import software.bernie.geckolib3.geo.raw.tree.RawGeometryTree;
import software.bernie.geckolib3.geo.render.GeoBuilder;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.util.json.JsonAnimationUtils;
import software.bernie.shadowed.eliotlash.molang.MolangParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class ChameleonLoader
{
    public void loadAllAnimations(MolangParser parser, File file, AnimationFile animationFile)
    {
        JsonObject json = this.loadFile(file);

        if (json != null)
        {
            for (Map.Entry<String, JsonElement> entry : JsonAnimationUtils.getAnimations(json))
            {
                String key = entry.getKey();

                try
                {
                    animationFile.putAnimation(key, JsonAnimationUtils.deserializeJsonToAnimation(JsonAnimationUtils.getAnimation(json, key), parser));
                }
                catch (JsonException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    public GeoModel loadModel(File file)
    {
        try
        {
            RawGeoModel rawModel = Converter.fromJsonString(this.loadStringFile(file));

            if (rawModel.getFormatVersion() != FormatVersion.VERSION_1_12_0)
            {
                throw new GeoModelException(new ResourceLocation(Chameleon.MOD_ID, file.getAbsolutePath()), "Given geometry JSON version" + rawModel.getFormatVersion().toValue() + ", expected 1.12.0");
            }
            else
            {
                return GeoBuilder.constructGeoModel(RawGeometryTree.parseHierarchy(rawModel, new ResourceLocation(Chameleon.MOD_ID, file.getAbsolutePath())));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    private JsonObject loadFile(File file)
    {
        try
        {
            return JsonUtils.fromJson(new Gson(), new StringReader(this.loadStringFile(file)), JsonObject.class);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    private String loadStringFile(File file) throws IOException
    {
        InputStream stream = new FileInputStream(file);
        String content = IOUtils.toString(stream, Charset.defaultCharset());

        stream.close();

        return content;
    }
}