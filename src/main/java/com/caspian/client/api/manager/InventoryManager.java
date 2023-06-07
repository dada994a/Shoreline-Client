package com.caspian.client.api.manager;

import com.caspian.client.Caspian;
import com.caspian.client.api.handler.InventoryHandler;
import com.caspian.client.util.Globals;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.ClickType;

/**
 *
 *
 * @author linus
 * @since 1.0
 */
public class InventoryManager implements Globals
{
    //
    private final InventoryHandler handler;
    
    /**
     *
     *
     */
    public InventoryManager()
    {
        handler = new InventoryHandler();
        Caspian.EVENT_HANDLER.subscribe(handler);
    }
    
    /**
     *
     *
     * @param slot
     */
    public void pickupSlot(int slot)
    {
        click(slot, SlotActionType.PICKUP, false);
    }
    
    /**
     *
     *
     * @param slot
     * @param type
     * @param tick
     */
    private void click(int slot, SlotActionType type, boolean tick)
    {
        mc.interactionManager.clickSlot(0, slot, 0, type, mc.player);
        if (tick)
        {
            mc.interactionManager.tick();
        }
    }
    
    /**
     *
     *
     * @return
     */
    public int getServerSlot()
    {
        return handler.getServerSlot();
    }
    
    /**
     *
     *
     * @return
     */
    public ItemStack getServerItem()
    {
        if (mc.player != null)
        {
            return mc.player.getInventory().getStack(getServerSlot());
        }
        return null;
    }
}