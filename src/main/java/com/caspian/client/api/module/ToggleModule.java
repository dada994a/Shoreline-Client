package com.caspian.client.api.module;

import com.caspian.client.api.config.Config;
import com.caspian.client.api.config.setting.BooleanConfig;
import com.caspian.client.api.config.setting.MacroConfig;
import com.caspian.client.api.config.setting.ToggleConfig;
import com.caspian.client.api.macro.Macro;
import com.caspian.client.api.render.Hideable;
import com.caspian.client.api.render.anim.Animation;
import com.caspian.client.api.render.anim.Easing;
import org.lwjgl.glfw.GLFW;

/**
 * {@link Module} implementation with enabled state and keybinding. The
 * enabled state dictates when the module is running and subscribed to the
 * EventBus. The keybinding is used to {@link #enable()} and
 * {@link #disable()} the module.
 *
 * <p>The user cannot directly interact with the {@link #enabledConfig}. This
 * is the only config which cannot be interacted with through the configuration
 * menu in the ClickGui. Instead, the user can {@link #toggle()} the module
 * to change the enabled state.</p>
 *
 * @author linus
 * @since 1.0
 *
 * @see Macro
 * @see ToggleConfig
 */
public class ToggleModule extends Module implements Hideable
{
    // Config for keybinding implementation. Module keybind is used to
    // interact with the #enabledConfig.
    Config<Macro> keybindingConfig = new MacroConfig("Keybind", "The module " +
            "keybinding. Pressing this key will toggle the module enabled " +
            "state. Press [BACKSPACE] to delete the keybind.",
            new Macro(getId(), GLFW.GLFW_KEY_UNKNOWN, () -> toggle()));
    // Config representing the module enabled state. Cannot interact with
    // this configuration unless using #toggle() #enable() or #disable().
    Config<Boolean> enabledConfig = new ToggleConfig("Enabled", "The module" +
            " enabled state. This state is true when the module is running.", false);
    // Arraylist rendering info
    Config<Boolean> hiddenConfig = new BooleanConfig("Hidden", "The hidden " +
            "state of the module in the Arraylist", false);
    private final Animation animation = new Animation(Easing.CUBIC_IN_OUT);

    /**
     *
     *
     * @param name     The module unique identifier
     * @param desc     The module description
     * @param category The module category
     */
    public ToggleModule(String name, String desc, ModuleCategory category)
    {
        super(name, desc, category);
        // Toggle settings
        register(keybindingConfig, enabledConfig, hiddenConfig);
    }

    /**
     *
     * @param hidden
     */
    @Override
    public void setHidden(boolean hidden)
    {
        hiddenConfig.setValue(hidden);
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isHidden()
    {
        return hiddenConfig.getValue();
    }

    /**
     *
     *
     * @param name     The module unique identifier
     * @param desc     The module description
     * @param category The module category
     * @param keycode  The module default keybind
     */
    public ToggleModule(String name, String desc, ModuleCategory category,
                        Integer keycode)
    {
        super(name, desc, category);
        keybind(keycode);
    }

    /**
     * Toggles the module {@link #enabledConfig} state (i.e. If the module is
     * <tt>enabled</tt>, the module enabled state will now be <tt>disabled</tt>
     * and vice versa).
     *
     * @see #enable()
     * @see #disable()
     */
    public void toggle()
    {
        if (isEnabled())
        {
            disable();
        }
        else
        {
            enable();
        }
    }

    /**
     * Sets the module {@link #enabledConfig} state to <tt>true</tt>. Runs
     * the {@link #onEnable()} callback.
     *
     * @see #onEnable()
     * @see ToggleConfig#setValue(Boolean)
     */
    public void enable()
    {
        enabledConfig.setValue(true);
        onEnable();
    }

    /**
     * Sets the module {@link #enabledConfig} state to <tt>false</tt>. Runs
     * the {@link #onDisable()} callback.
     *
     * @see #onDisable()
     * @see ToggleConfig#setValue(Boolean)
     */
    public void disable()
    {
        enabledConfig.setValue(false);
        onDisable();
    }

    /**
     * Runs callback after {@link #enable()}. Part of the module
     * implementation specifications.
     *
     * @see #enable()
     */
    protected void onEnable()
    {

    }

    /**
     * Runs callback after {@link #disable()}. Part of the module
     * implementation specifications.
     *
     * @see #disable()
     */
    protected void onDisable()
    {

    }

    /**
     * Sets the module keybinding to the param {@link GLFW} keycode. The
     * config {@link Macro#runMacro()} will invoke {@link #toggle()} when
     * keybind is pressed.
     *
     * @param keycode The keybind
     *
     * @see Macro
     * @see #keybindingConfig
     */
    public void keybind(int keycode)
    {
        keybindingConfig.setContainer(this);
        ((MacroConfig) keybindingConfig).setValue(keycode);
    }

    /**
     * Returns <tt>true</tt> if the module is currently enabled and running.
     * Wrapper method for {@link ToggleConfig#getValue()}.
     *
     * @return <tt>true</tt> if the module is enabled
     *
     * @see #enabledConfig
     */
    public boolean isEnabled()
    {
        return enabledConfig.getValue();
    }

    /**
     *
     *
     * @return
     */
    public Macro getKeybinding()
    {
        return keybindingConfig.getValue();
    }

    /**
     *
     * @return
     */
    public Animation getAnimation()
    {
        return animation;
    }
}
