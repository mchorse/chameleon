package mchorse.chameleon.lib.parsing;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import mchorse.chameleon.lib.data.animation.Animation;
import mchorse.chameleon.lib.data.animation.AnimationChannel;
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
        AnimationVector previous = null;

        if (element.isJsonArray())
        {
            channel.keyframes.add(parseAnimationVector(parser, element));
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

                vector.time = time;

                if (previous != null)
                {
                    previous.next = vector;
                }

                channel.keyframes.add(vector);

                previous = vector;
            }
        }
    }

    private static AnimationVector parseAnimationVector(MolangParser parser, JsonElement element) throws Exception
    {
        JsonArray array = element.isJsonArray() ? element.getAsJsonArray() : element.getAsJsonObject().get("vector").getAsJsonArray();
        AnimationVector vector = new AnimationVector();

        vector.x = parseValue(parser, array.get(0));
        vector.y = parseValue(parser, array.get(1));
        vector.z = parseValue(parser, array.get(2));

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