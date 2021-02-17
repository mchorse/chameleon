package mchorse.chameleon.client;

import com.google.common.collect.ImmutableSet;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

@SideOnly(Side.CLIENT)
public class ChameleonPack implements IResourcePack
{
    private static final Set<String> DOMAINS = ImmutableSet.of("c.s");

    public File file;

    public ChameleonPack(File file)
    {
        this.file = file;
    }

    @Override
    public InputStream getInputStream(ResourceLocation location) throws IOException
    {
        return new FileInputStream(new File(this.file, location.getResourcePath()));
    }

    @Override
    public boolean resourceExists(ResourceLocation location)
    {
        return new File(this.file, location.getResourcePath()).exists();
    }

    @Override
    public Set<String> getResourceDomains()
    {
        return DOMAINS;
    }

    @Nullable
    @Override
    public <T extends IMetadataSection> T getPackMetadata(MetadataSerializer metadataSerializer, String metadataSectionName) throws IOException
    {
        return null;
    }

    @Override
    public BufferedImage getPackImage() throws IOException
    {
        return null;
    }

    @Override
    public String getPackName()
    {
        return "Chameleon's skin pack";
    }
}