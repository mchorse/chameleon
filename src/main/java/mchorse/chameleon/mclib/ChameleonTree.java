package mchorse.chameleon.mclib;

import mchorse.mclib.utils.files.FileTree;
import mchorse.mclib.utils.files.entries.FolderImageEntry;

import java.io.File;

public class ChameleonTree extends FileTree
{
    public ChameleonTree(File folder)
    {
        this.root = new FolderImageEntry("c.s", folder, null);
    }
}