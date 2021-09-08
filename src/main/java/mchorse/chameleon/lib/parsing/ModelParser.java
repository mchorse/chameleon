package mchorse.chameleon.lib.parsing;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mchorse.chameleon.lib.data.model.Model;
import mchorse.chameleon.lib.data.model.ModelBone;
import mchorse.chameleon.lib.data.model.ModelCube;
import mchorse.chameleon.lib.data.model.ModelUV;
import net.minecraft.client.util.JsonException;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModelParser
{
    public static Model parse(JsonObject object) throws JsonException
    {
        Model model = new Model();

        object = object.get("minecraft:geometry").getAsJsonArray().get(0).getAsJsonObject();

        if (object.has("description"))
        {
            parseDescription(model, object.get("description").getAsJsonObject());
        }

        if (object.has("bones"))
        {
            parseBones(model, object.get("bones").getAsJsonArray());
        }

        return model;
    }

    private static void parseDescription(Model model, JsonObject object)
    {
        if (object.has("identifier"))
        {
            model.id = object.get("identifier").getAsString();
        }

        if (object.has("texture_width"))
        {
            model.textureWidth = object.get("texture_width").getAsInt();
        }

        if (object.has("texture_height"))
        {
            model.textureHeight = object.get("texture_height").getAsInt();
        }
    }

    private static void parseBones(Model model, JsonArray bones)
    {
        Map<String, List<String>> hierarchy = new HashMap<String, List<String>>();
        Map<String, ModelBone> flatBones = new HashMap<String, ModelBone>();

        for (JsonElement element : bones)
        {
            JsonObject boneElement = element.getAsJsonObject();
            ModelBone bone = new ModelBone(boneElement.get("name").getAsString());

            /* Fill hierarchy information */
            String parent = boneElement.has("parent") ? boneElement.get("parent").getAsString() : "";
            List<String> list = hierarchy.computeIfAbsent(parent, (a) -> new ArrayList<String>());

            list.add(bone.id);

            /* Setup initial transformations */
            if (boneElement.has("pivot"))
            {
                parsePositionVector(boneElement.get("pivot"), bone.initial.translate);
            }

            if (boneElement.has("scale"))
            {
                parseVector(boneElement.get("scale"), bone.initial.scale);
            }

            if (boneElement.has("rotation"))
            {
                parseVector(boneElement.get("rotation"), bone.initial.rotation);
            }

            /* Setup cubes */
            if (boneElement.has("cubes"))
            {
                parseCubes(model, bone, boneElement.get("cubes").getAsJsonArray());
            }

            flatBones.put(bone.id, bone);
        }

        /* Setup hierarchy */
        for (Map.Entry<String, List<String>> entry : hierarchy.entrySet())
        {
            if (entry.getKey().isEmpty())
            {
                continue;
            }

            ModelBone bone = flatBones.get(entry.getKey());

            for (String child : entry.getValue())
            {
                bone.children.add(flatBones.get(child));
            }
        }

        List<String> topLevel = hierarchy.get("");

        if (topLevel != null)
        {
            for (String topLevelBone : topLevel)
            {
                model.bones.add(flatBones.get(topLevelBone));
            }
        }
    }

    private static void parseCubes(Model model, ModelBone bone, JsonArray cubes)
    {
        for (JsonElement element : cubes)
        {
            bone.cubes.add(parseCube(model, element.getAsJsonObject()));
        }
    }

    private static ModelCube parseCube(Model model, JsonObject object)
    {
        ModelCube cube = new ModelCube();

        parseVector(object.get("origin"), cube.origin);
        parseVector(object.get("size"), cube.size);

        if (object.has("pivot"))
        {
            parseVector(object.get("pivot"), cube.pivot);
        }
        else
        {
            cube.pivot.set(cube.origin);
        }

        if (object.has("rotation"))
        {
            parseVector(object.get("rotation"), cube.rotation);
        }

        if (object.has("uv"))
        {
            if (object.has("mirror"))
            {
                cube.mirror = object.get("mirror").getAsBoolean();
            }

            parseUV(cube, object.get("uv"));
        }

        cube.generateQuads(model);

        return cube;
    }

    private static void parseUV(ModelCube cube, JsonElement element)
    {
        if (element.isJsonArray())
        {
            cube.boxUV = new Vector2f();

            parseVector(element.getAsJsonArray(), cube.boxUV);
        }
        else if (element.isJsonObject())
        {
            JsonObject sides = element.getAsJsonObject();

            if (sides.has("north")) cube.north = parseUVSide(sides.get("north").getAsJsonObject());
            if (sides.has("east")) cube.east = parseUVSide(sides.get("east").getAsJsonObject());
            if (sides.has("south")) cube.south = parseUVSide(sides.get("south").getAsJsonObject());
            if (sides.has("west")) cube.west = parseUVSide(sides.get("west").getAsJsonObject());
            if (sides.has("up")) cube.up = parseUVSide(sides.get("up").getAsJsonObject());
            if (sides.has("down")) cube.down = parseUVSide(sides.get("down").getAsJsonObject());
        }
    }

    private static ModelUV parseUVSide(JsonObject uvSide)
    {
        ModelUV uv = new ModelUV();

        parseVector(uvSide.get("uv"), uv.origin);
        parseVector(uvSide.get("uv_size"), uv.size);

        return uv;
    }

    private static void parseVector(JsonElement element, Vector3f vector)
    {
        JsonArray array = element.getAsJsonArray();

        vector.x = array.get(0).getAsFloat();
        vector.y = array.get(1).getAsFloat();
        vector.z = array.get(2).getAsFloat();
    }

    private static void parsePositionVector(JsonElement element, Vector3f vector)
    {
        parseVector(element, vector);

        vector.x /= 16F;
        vector.y /= 16F;
        vector.z /= 16F;
    }

    private static void parseVector(JsonElement element, Vector2f vector)
    {
        JsonArray array = element.getAsJsonArray();

        vector.x = array.get(0).getAsFloat();
        vector.y = array.get(1).getAsFloat();
    }
}