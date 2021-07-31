package mchorse.chameleon.metamorph.editor;

import mchorse.chameleon.animation.ActionConfig;
import mchorse.chameleon.lib.data.animation.Animation;
import mchorse.chameleon.metamorph.ChameleonMorph;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiToggleElement;
import mchorse.mclib.client.gui.framework.elements.input.GuiTrackpadElement;
import mchorse.mclib.client.gui.framework.elements.list.GuiStringListElement;
import mchorse.mclib.client.gui.framework.elements.list.GuiStringSearchListElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.metamorph.client.gui.editor.GuiMorphPanel;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiActionsPanel extends GuiMorphPanel<ChameleonMorph, GuiChameleonMorph>
{
    public static final String[] ACTIONS = new String[] {"Idle", "Running", "Sprinting", "Crouching", "CrouchingIdle", "Swimming", "SwimmingIdle", "Flying", "FlyingIdle", "Riding", "RidingIdle", "Dying", "Falling", "Sleeping", "Jump", "Swipe", "Hurt", "Land", "Shoot", "Consume", "Animation"};

    public ActionConfig config;

    public GuiStringListElement configs;
    public GuiElement fields;
    public GuiStringSearchListElement action;
    public GuiToggleElement clamp;
    public GuiToggleElement reset;
    public GuiTrackpadElement speed;
    public GuiTrackpadElement fade;
    public GuiTrackpadElement tick;

    private IKey actionsTitle = IKey.lang("chameleon.gui.editor.actions.actions");
    private IKey actionTitle = IKey.lang("chameleon.gui.editor.actions.action");

    public GuiActionsPanel(Minecraft mc, GuiChameleonMorph editor)
    {
        super(mc, editor);

        this.configs = new GuiStringListElement(mc, (str) -> this.selectAction(str.get(0)));

        for (String action : ACTIONS)
        {
            this.configs.add(action);
        }

        this.fields = new GuiElement(mc).noCulling();
        this.configs.sort();
        this.configs.flex().relative(this).set(10, 22, 110, 90).h(1, -35);

        this.action = new GuiStringSearchListElement(mc, (value) -> 
        {
            this.config.name = value.get(0);
            this.morph.updateAnimator();
        });
        this.clamp = new GuiToggleElement(mc, IKey.lang("chameleon.gui.editor.actions.clamp"), false, (b) -> this.config.clamp = b.isToggled());
        this.reset = new GuiToggleElement(mc, IKey.lang("chameleon.gui.editor.actions.reset"), false, (b) -> this.config.reset = b.isToggled());
        this.speed = new GuiTrackpadElement(mc, (value) -> this.config.speed = value.floatValue());
        this.speed.tooltip(IKey.lang("chameleon.gui.editor.actions.speed"));
        this.fade = new GuiTrackpadElement(mc, (value) -> this.config.fade = value.intValue());
        this.fade.tooltip(IKey.lang("chameleon.gui.editor.actions.fade"));
        this.fade.limit(0, Integer.MAX_VALUE, true);
        this.tick = new GuiTrackpadElement(mc, (value) -> this.config.tick = value.intValue());
        this.tick.tooltip(IKey.lang("chameleon.gui.editor.actions.tick"));
        this.tick.limit(0, Integer.MAX_VALUE, true);

        GuiElement fields = new GuiElement(mc);

        fields.flex().relative(this).xy(1F, 1F).w(130).anchor(1, 1).column(5).vertical().stretch().padding(10);
        fields.add(this.clamp, this.reset, this.speed, this.fade, this.tick);

        this.action.flex().relative(this.area).x(1F, -10).y(22).w(110).hTo(fields.area, 5).anchorX(1F);
        this.action.createContextMenu(null);

        this.fields.add(fields, this.action);
        this.fields.setVisible(false);

        this.add(this.configs, this.fields);
    }

    private void selectAction(String name)
    {
        name = this.morph.actions.toKey(name);

        ActionConfig config = this.morph.actions.actions.get(name);

        if (config == null)
        {
            config = this.morph.actions.actions.get(name);
            config = config == null ? new ActionConfig(name) : config.clone();

            this.morph.actions.actions.put(name, config);
        }

        this.config = config;
        this.fields.setVisible(true);
        this.fillFields(config);
    }

    @Override
    public void fillData(ChameleonMorph morph)
    {
        super.fillData(morph);

        this.action.list.clear();

        for (Animation animation : morph.getModel().animations.getAll())
        {
            this.action.list.add(animation.id);
        }

        this.action.list.sort();

        this.action.list.setCurrent("");
        this.fields.setVisible(false);
        this.configs.setCurrent("");
    }

    private void fillFields(ActionConfig config)
    {
        this.action.list.setCurrentScroll(config.name);
        this.clamp.toggled(config.clamp);
        this.reset.toggled(config.reset);
        this.speed.setValue(config.speed);
        this.fade.setValue((int) config.fade);
        this.tick.setValue(config.tick);
    }

    @Override
    public void draw(GuiContext context)
    {
        this.configs.area.draw(0x88000000);
        this.font.drawStringWithShadow(this.actionsTitle.get(), this.configs.area.x, this.configs.area.y - 12, 0xffffff);

        if (this.fields.isVisible())
        {
            this.font.drawStringWithShadow(this.actionTitle.get(), this.action.area.x, this.action.area.y - 12, 0xffffff);
            this.action.area.draw(0x88000000);
        }

        super.draw(context);
    }
}