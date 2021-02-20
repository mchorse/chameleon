package mchorse.chameleon.metamorph.editor;

import mchorse.chameleon.geckolib.ChameleonModel;
import mchorse.chameleon.metamorph.ChameleonMorph;
import mchorse.chameleon.metamorph.pose.AnimatedPose;
import mchorse.chameleon.metamorph.pose.AnimatedPoseTransform;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiButtonElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiToggleElement;
import mchorse.mclib.client.gui.framework.elements.context.GuiContextMenu;
import mchorse.mclib.client.gui.framework.elements.context.GuiSimpleContextMenu;
import mchorse.mclib.client.gui.framework.elements.input.GuiTexturePicker;
import mchorse.mclib.client.gui.framework.elements.input.GuiTrackpadElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTransformations;
import mchorse.mclib.client.gui.framework.elements.list.GuiStringListElement;
import mchorse.mclib.client.gui.utils.Elements;
import mchorse.mclib.client.gui.utils.Icons;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.mclib.utils.resources.RLUtils;
import mchorse.metamorph.client.gui.editor.GuiAnimation;
import mchorse.metamorph.client.gui.editor.GuiMorphPanel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;

import java.util.List;
import java.util.function.Consumer;

/**
 * Custom model morph panel which allows editing custom textures
 * for materials of the custom model morph
 */
public class GuiChameleonMainPanel extends GuiMorphPanel<ChameleonMorph, GuiChameleonMorph> implements IBonePicker
{
    /* Materials */
    public GuiButtonElement skin;
    public GuiTexturePicker picker;

    public GuiButtonElement createPose;
    public GuiStringListElement bones;
    public GuiToggleElement fixed;
    public GuiToggleElement animated;
    public GuiPoseTransformations transforms;
    public GuiAnimation animation;

    public GuiTrackpadElement scale;
    public GuiTrackpadElement scaleGui;

    private IKey createLabel = IKey.lang("chameleon.gui.editor.create_pose");
    private IKey resetLabel = IKey.lang("chameleon.gui.editor.reset_pose");

    private AnimatedPoseTransform transform;

    public static GuiContextMenu createCopyPasteMenu(Runnable copy, Consumer<AnimatedPose> paste)
    {
        GuiSimpleContextMenu menu = new GuiSimpleContextMenu(Minecraft.getMinecraft());
        AnimatedPose pose = null;

        try
        {
            NBTTagCompound tag = JsonToNBT.getTagFromJson(GuiScreen.getClipboardString());
            AnimatedPose loaded = new AnimatedPose();

            loaded.fromNBT(tag);

            pose = loaded;
        }
        catch (Exception e)
        {}

        menu.action(Icons.COPY, IKey.lang("chameleon.gui.editor.context.copy"), copy);

        if (pose != null)
        {
            final AnimatedPose innerPose = pose;

            menu.action(Icons.PASTE, IKey.lang("chameleon.gui.editor.context.paste"), () -> paste.accept(innerPose));
        }

        return menu;
    }

    public GuiChameleonMainPanel(Minecraft mc, GuiChameleonMorph editor)
    {
        super(mc, editor);

        /* Materials view */
        this.skin = new GuiButtonElement(mc, IKey.lang("chameleon.gui.editor.pick_skin"), (b) ->
        {
            this.picker.refresh();
            this.picker.fill(this.morph.skin);
            this.add(this.picker);
            this.picker.resize();
        });
        this.picker = new GuiTexturePicker(mc, (rl) -> this.morph.skin = RLUtils.clone(rl));

        this.createPose = new GuiButtonElement(mc, this.createLabel, this::createResetPose);
        this.bones = new GuiStringListElement(mc, this::pickBone);
        this.bones.background().context(() -> createCopyPasteMenu(this::copyCurrentPose, this::pastePose));
        this.fixed = new GuiToggleElement(mc, IKey.lang("chameleon.gui.editor.fixed"), this::toggleFixed);
        this.animated = new GuiToggleElement(mc, IKey.lang("chameleon.gui.editor.animated"), this::toggleAnimated);
        this.transforms = new GuiPoseTransformations(mc);
        this.animation = new GuiAnimation(mc, false);

        this.scale = new GuiTrackpadElement(mc, (value) -> this.morph.scale = value.floatValue());
        this.scale.tooltip(IKey.lang("chameleon.gui.editor.scale"));
        this.scaleGui = new GuiTrackpadElement(mc, (value) -> this.morph.scaleGui = value.floatValue());
        this.scaleGui.tooltip(IKey.lang("chameleon.gui.editor.scale_gui"));

        this.skin.flex().relative(this).set(10, 10, 110, 20);
        this.picker.flex().relative(this).wh(1F, 1F);

        this.createPose.flex().relative(this.skin).y(1F, 5).w(1F).h(20);
        this.bones.flex().relative(this.createPose).y(1F, 5).w(1F).hTo(this.fixed.flex(), -5);
        this.animated.flex().relative(this).x(10).y(1F, -10).w(110).anchorY(1);
        this.fixed.flex().relative(this.animated).y(-1F, -5).w(1F);
        this.transforms.flex().relative(this).set(0, 0, 190, 70).x(0.5F, -95).y(1, -80);
        this.animation.flex().relative(this).x(1F, -130).w(130);

        GuiElement lowerBottom = new GuiElement(mc);

        lowerBottom.flex().relative(this).xy(1F, 1F).w(130).anchor(1F, 1F).column(5).vertical().stretch().padding(10);
        lowerBottom.add(this.scale, this.scaleGui);

        this.add(this.skin, this.createPose, this.animated, this.fixed, this.bones, this.transforms, this.animation, lowerBottom);
    }

    private void copyCurrentPose()
    {
        GuiScreen.setClipboardString(this.morph.pose.toNBT().toString());
    }

    private void pastePose(AnimatedPose pose)
    {
        this.morph.pose.copy(pose);
        this.transforms.set(this.transforms.trans);
    }

    private void createResetPose(GuiButtonElement button)
    {
        if (this.morph.pose == null)
        {
            AnimatedPose pose = new AnimatedPose();
            List<String> bones = this.morph.getModel().getBoneNames();

            for (String bone : bones)
            {
                pose.bones.put(bone, new AnimatedPoseTransform(bone));
            }

            this.morph.pose = pose;
        }
        else
        {
            this.morph.pose = null;
            this.editor.chameleonModelRenderer.boneName = "";
        }

        this.setPoseEditorVisible();
    }

    private void pickBone(List<String> bone)
    {
        this.pickBone(bone.get(0));
    }

    @Override
    public void pickBone(String bone)
    {
        if (this.morph.pose == null)
        {
            return;
        }

        this.transform = this.morph.pose.bones.get(bone);

        this.bones.setCurrentScroll(bone);
        this.animated.toggled(this.morph.pose.animated == AnimatedPoseTransform.ANIMATED);
        this.fixed.toggled(this.transform.fixed == AnimatedPoseTransform.FIXED);
        this.transforms.set(this.transform);
        this.editor.chameleonModelRenderer.boneName = bone;
    }

    private void toggleFixed(GuiToggleElement toggle)
    {
        this.transform.fixed = toggle.isToggled() ? AnimatedPoseTransform.FIXED : AnimatedPoseTransform.ANIMATED;
    }

    private void toggleAnimated(GuiToggleElement toggle)
    {
        this.morph.pose.animated = toggle.isToggled() ? AnimatedPoseTransform.ANIMATED : AnimatedPoseTransform.FIXED;
    }

    @Override
    public void fillData(ChameleonMorph morph)
    {
        super.fillData(morph);

        this.picker.removeFromParent();
        this.setPoseEditorVisible();

        this.animation.fill(morph.animation);
        this.scale.setValue(morph.scale);
        this.scaleGui.setValue(morph.scaleGui);
    }

    @Override
    public void finishEditing()
    {
        this.picker.close();
    }

    private void setPoseEditorVisible()
    {
        ChameleonModel model = this.morph.getModel();
        AnimatedPose pose = this.morph.pose;

        this.createPose.setVisible(model != null && !model.getBoneNames().isEmpty());
        this.createPose.label = pose == null ? this.createLabel : this.resetLabel;
        this.bones.setVisible(model != null && pose != null);
        this.fixed.setVisible(model != null && pose != null);
        this.animated.setVisible(model != null && pose != null);
        this.transforms.setVisible(model != null && pose != null);

        if (model != null)
        {
            this.bones.clear();
            this.bones.add(model.getBoneNames());
            this.bones.sort();

            if (this.morph.pose != null)
            {
                this.pickBone(model.getBoneNames().get(0));
            }
        }
    }

    public static class GuiPoseTransformations extends GuiTransformations
    {
        public AnimatedPoseTransform trans;

        public GuiPoseTransformations(Minecraft mc)
        {
            super(mc);
        }

        public void set(AnimatedPoseTransform trans)
        {
            this.trans = trans;

            if (trans != null)
            {
                this.fillT(trans.x, trans.y, trans.z);
                this.fillS(trans.scaleX, trans.scaleY, trans.scaleZ);
                this.fillR(trans.rotateX / (float) Math.PI * 180, trans.rotateY / (float) Math.PI * 180, trans.rotateZ / (float) Math.PI * 180);
            }
        }

        @Override
        public void setT(double x, double y, double z)
        {
            this.trans.x = (float) x;
            this.trans.y = (float) y;
            this.trans.z = (float) z;
        }

        @Override
        public void setS(double x, double y, double z)
        {
            this.trans.scaleX = (float) x;
            this.trans.scaleY = (float) y;
            this.trans.scaleZ = (float) z;
        }

        @Override
        public void setR(double x, double y, double z)
        {
            this.trans.rotateX = (float) (x / 180F * (float) Math.PI);
            this.trans.rotateY = (float) (y / 180F * (float) Math.PI);
            this.trans.rotateZ = (float) (z / 180F * (float) Math.PI);
        }
    }
}
