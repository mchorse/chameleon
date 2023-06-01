package mchorse.chameleon.lib.parsing;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import mchorse.chameleon.lib.data.animation.Animation;
import mchorse.chameleon.lib.data.animation.AnimationChannel;
import mchorse.chameleon.lib.data.animation.AnimationInterpolation;
import mchorse.chameleon.lib.data.animation.AnimationPart;
import mchorse.chameleon.lib.data.animation.AnimationVector;
import mchorse.mclib.math.Constant;
import mchorse.mclib.math.IValue;
import mchorse.mclib.math.molang.MolangParser;
import mchorse.mclib.math.molang.expressions.MolangExpression;
import mchorse.mclib.math.molang.expressions.MolangValue;

import java.util.Map;

public class AnimationParser
{
    public static Animation parse(MolangParser parser, String key, JsonObject object) throws Exception
    {
        Animation animation = new Animation(key);

        if (object.has("animation_length"))
        {
            animation.setLength(object.get("animation_length").getAsDouble());
        }

        if (object.has("bones"))
        {
            for (Map.Entry<String, JsonElement> entry : object.get("bones").getAsJsonObject().entrySet())
            {
                animation.parts.put(entry.getKey(), parsePart(parser, entry.getValue().getAsJsonObject()));
            }
        }

        return animation;
    }

    private static AnimationPart parsePart(MolangParser parser, JsonObject object) throws Exception
    {
        AnimationPart part = new AnimationPart();

        if (object.has("position"))
        {
            parseChannel(parser, part.position, object.get("position"));
        }

        if (object.has("scale"))
        {
            parseChannel(parser, part.scale, object.get("scale"));
        }

        if (object.has("rotation"))
        {
            parseChannel(parser, part.rotation, object.get("rotation"));
        }

        return part;
    }

    private static void parseChannel(MolangParser parser, AnimationChannel channel, JsonElement element) throws Exception
    {
        if (element.isJsonArray())
        {
            AnimationVector vector = parseAnimationVector(parser, element);

            if (vector != null)
            {
                channel.keyframes.add(vector);
                channel.sort();
            }

            return;
        }

        if (!element.isJsonObject())
        {
            return;
        }

        JsonObject object = element.getAsJsonObject();

        if (object.has("vector"))
        {
            channel.keyframes.add(parseAnimationVector(parser, object));
        }
        else
        {
            for (Map.Entry<String, JsonElement> entry : object.entrySet())
            {
                double time;

                try
                {
                    time = Double.parseDouble(entry.getKey());
                }
                catch (Exception e)
                {
                    continue;
                }

                AnimationVector vector = parseAnimationVector(parser, entry.getValue());

                if (vector != null)
                {
                    vector.time = time;
                    channel.keyframes.add(vector);
                }
            }
        }

        channel.sort();
    }

    private static AnimationVector parseAnimationVector(MolangParser parser, JsonElement element) throws Exception
    {
        JsonArray array = element.isJsonArray() ? element.getAsJsonArray() : null;
        JsonArray pre = element.isJsonArray() ? element.getAsJsonArray() : null;

        if (array == null)
        {
            JsonObject object = element.getAsJsonObject();

            if (object.has("vector"))
            {
                array = element.getAsJsonObject().get("vector").getAsJsonArray();
            }
            else if (object.has("post"))
            {
                if (object.get("post").isJsonArray())
                {
                    array = object.get("post").getAsJsonArray();
                }
                else if (object.get("post").isJsonObject() && object.get("post").getAsJsonObject().has("vector"))
                {
                    array = object.get("post").getAsJsonObject().get("vector").getAsJsonArray();
                }

                if (object.has("pre"))
                {
                    if (object.get("pre").isJsonArray())
                    {
                        pre = object.get("pre").getAsJsonArray();
                    }
                    else if (object.get("pre").isJsonObject() && object.get("pre").getAsJsonObject().has("vector"))
                    {
                        pre = object.get("pre").getAsJsonObject().get("vector").getAsJsonArray();
                    }
                }
            }
        }

        if (pre == null)
        {
            pre = array;
        }

        AnimationVector vector = new AnimationVector();

        vector.x = parseValue(parser, array.get(0));
        vector.y = parseValue(parser, array.get(1));
        vector.z = parseValue(parser, array.get(2));
        vector.preX = parseValue(parser, pre.get(0));
        vector.preY = parseValue(parser, pre.get(1));
        vector.preZ = parseValue(parser, pre.get(2));

        if (element.isJsonObject())
        {
            JsonObject object = element.getAsJsonObject();

            /* Hermite support */
            if (object.has("lerp_mode") && object.get("lerp_mode").isJsonPrimitive() && object.get("lerp_mode").getAsString().equals("catmullrom"))
            {
                vector.interp = AnimationInterpolation.HERMITE;
            }
            /* GeckoLib's partial easing support */
            else if (object.has("easing") && object.get("easing").isJsonPrimitive())
            {
                vector.interp = AnimationInterpolation.byName(object.get("easing").getAsString());
            }
        }

        return vector;
    }

    private static MolangExpression parseValue(MolangParser parser, JsonElement element) throws Exception
    {
        JsonPrimitive primitive = element.getAsJsonPrimitive();

        if (primitive.isNumber())
        {
            return new MolangValue(parser, new Constant(primitive.getAsDouble()));
        }

        return parser.parseExpression(primitive.getAsString());
    }
}