package mchorse.chameleon.lib;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mchorse.chameleon.lib.data.animation.Animations;
import mchorse.chameleon.lib.data.model.Model;
import mchorse.chameleon.lib.parsing.AnimationParser;
import mchorse.chameleon.lib.parsing.ModelParser;
import mchorse.mclib.math.molang.MolangParser;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class ChameleonLoader
{
    public void loadAllAnimations(MolangParser parser, File file, Animations animations)
    {
        JsonObject json = this.loadFile(file);

        if (json != null)
        {
            for (Map.Entry<String, JsonElement> entry : this.getAnimations(json).entrySet())
            {
                String key = entry.getKey();

                try
                {
                    animations.add(AnimationParser.parse(parser, key, entry.getValue().getAsJsonObject()));
                }
                catch (Exception e)
                {
                    System.err.println("An error happened when parsing animation file: " + file.getAbsolutePath());
                    e.printStackTrace();
                }
            }
        }
    }

    private Map<String, JsonElement> getAnimations(JsonObject json)
    {
        Map<String, JsonElement> map = new HashMap<String, JsonElement>();

        if (json.has("animations") && json.get("animations").isJsonObject())
        {
            for (Map.Entry<String, JsonElement> entry : json.get("animations").getAsJsonObject().entrySet())
            {
                map.put(entry.getKey(), entry.getValue());
            }
        }

        return map;
    }

    public Model loadModel(File file)
    {
        try
        {
            return ModelParser.parse(this.loadFile(file));
        }
        catch (Exception e)
        {
            System.err.println("An error happened when parsing model file: " + file.getAbsolutePath());
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