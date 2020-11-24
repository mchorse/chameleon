package mchorse.chameleon.mclib;

import mchorse.mclib.utils.files.FileTree;
import mchorse.mclib.utils.files.entries.FolderImageEntry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.File;

@SideOnly(Side.CLIENT)
public class ChameleonTree extends FileTree
{
    public ChameleonTree(File folder)
    {
        this.root = new FolderImageEntry("c.s", folder, null);
    }
}