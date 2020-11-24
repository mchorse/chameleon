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
import software.bernie.geckolib3.core.builder.Animation;
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
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@SideOnly(Side.CLIENT)
public class ChameleonLoader
{
	public AnimationFile loadAllAnimations(MolangParser parser, File file)
	{
		AnimationFile animationFile = new AnimationFile();
		JsonObject jsonRepresentation = this.loadFile(file);

		if (jsonRepresentation == null)
		{
			return null;
		}

		Set<Map.Entry<String, JsonElement>> entrySet = JsonAnimationUtils.getAnimations(jsonRepresentation);
		Iterator<Map.Entry<String, JsonElement>> it = entrySet.iterator();

		while(it.hasNext())
		{
			Map.Entry<String, JsonElement> entry = it.next();
			String animationName = entry.getKey();

			try
			{
				Animation animation = JsonAnimationUtils.deserializeJsonToAnimation(JsonAnimationUtils.getAnimation(jsonRepresentation, animationName), parser);
				animationFile.putAnimation(animationName, animation);
			}
			catch (JsonException e)
			{
				return null;
			}
		}

		return animationFile;
	}

	public GeoModel loadModel(File file)
	{
		try {
			RawGeoModel rawModel = Converter.fromJsonString(this.loadStringFile(file));

			if (rawModel.getFormatVersion() != FormatVersion.VERSION_1_12_0)
			{
				throw new GeoModelException(new ResourceLocation(Chameleon.MOD_ID, file.getAbsolutePath()), "Wrong geometry json version, expected 1.12.0");
			}
			else
			{
				RawGeometryTree rawGeometryTree = RawGeometryTree.parseHierarchy(rawModel, new ResourceLocation(Chameleon.MOD_ID, file.getAbsolutePath()));

				return GeoBuilder.constructGeoModel(rawGeometryTree);
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
			String content = this.loadStringFile(file);
			Gson GSON = new Gson();

			return JsonUtils.fromJson(GSON, new StringReader(content), JsonObject.class);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}

	private String loadStringFile(File file) throws IOException
	{
		return IOUtils.toString(new FileInputStream(file), Charset.defaultCharset());
	}
}