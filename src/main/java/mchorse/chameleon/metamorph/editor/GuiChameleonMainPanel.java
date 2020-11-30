package mchorse.chameleon.metamorph.editor;

import mchorse.chameleon.geckolib.ChameleonModel;
import mchorse.chameleon.metamorph.ChameleonMorph;
import mchorse.chameleon.metamorph.pose.AnimatedPose;
import mchorse.chameleon.metamorph.pose.AnimatorPoseTransform;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiButtonElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiToggleElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTexturePicker;
import mchorse.mclib.client.gui.framework.elements.input.GuiTransformations;
import mchorse.mclib.client.gui.framework.elements.list.GuiStringListElement;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.mclib.utils.resources.RLUtils;
import mchorse.metamorph.client.gui.editor.GuiAnimation;
import mchorse.metamorph.client.gui.editor.GuiMorphPanel;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

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

	private IKey createLabel = IKey.lang("chameleon.gui.editor.create_pose");
	private IKey resetLabel = IKey.lang("chameleon.gui.editor.reset_pose");

	private AnimatorPoseTransform transform;

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
		this.bones.background();
		this.fixed = new GuiToggleElement(mc, IKey.lang("chameleon.gui.editor.fixed"), this::toggleFixed);
		this.animated = new GuiToggleElement(mc, IKey.lang("chameleon.gui.editor.animated"), this::toggleAnimated);
		this.transforms = new GuiPoseTransformations(mc);

		this.animation = new GuiAnimation(mc, false);

		this.skin.flex().relative(this).set(10, 10, 110, 20);
		this.picker.flex().relative(this).wh(1F, 1F);

		this.createPose.flex().relative(this.skin).y(1F, 5).w(1F).h(20);
		this.bones.flex().relative(this.createPose).y(1F, 5).w(1F).hTo(this.fixed.flex(), -5);
		this.animated.flex().relative(this).x(10).y(1F, -10).w(110).anchorY(1);
		this.fixed.flex().relative(this.animated).y(-1F, -5).w(1F);
		this.transforms.flex().relative(this).set(0, 0, 190, 70).x(0.5F, -95).y(1, -80);

		this.animation.flex().relative(this).x(1F, -130).w(130);

		this.add(this.skin, this.createPose, this.animated, this.fixed, this.bones, this.transforms, this.animation);
	}

	private void createResetPose(GuiButtonElement button)
	{
		if (this.morph.pose == null)
		{
			AnimatedPose pose = new AnimatedPose();
			List<String> bones = this.morph.getModel().getBoneNames();

			for (String bone : bones)
			{
				pose.bones.put(bone, new AnimatorPoseTransform(bone));
			}

			this.morph.pose = pose;
		}
		else
		{
			this.morph.pose = null;
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
		this.transform = this.morph.pose.bones.get(bone);

		this.bones.setCurrentScroll(bone);
		this.animated.toggled(this.morph.pose.animated);
		this.fixed.toggled(this.transform.fixed == AnimatorPoseTransform.FIXED);
		this.transforms.set(this.transform);
		this.editor.chameleonModelRenderer.boneName = bone;
	}

	private void toggleFixed(GuiToggleElement toggle)
	{
		this.transform.fixed = toggle.isToggled() ? AnimatorPoseTransform.FIXED : AnimatorPoseTransform.ANIMATED;
	}

	private void toggleAnimated(GuiToggleElement toggle)
	{
		this.morph.pose.animated = toggle.isToggled();
	}

	@Override
	public void fillData(ChameleonMorph morph)
	{
		super.fillData(morph);

		this.picker.removeFromParent();
		this.setPoseEditorVisible();

		this.animation.fill(morph.animation);
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
		public AnimatorPoseTransform trans;

		public GuiPoseTransformations(Minecraft mc)
		{
			super(mc);
		}

		public void set(AnimatorPoseTransform trans)
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
