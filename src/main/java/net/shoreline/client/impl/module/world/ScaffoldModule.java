package net.shoreline.client.impl.module.world;

import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.event.listener.EventListener;
import net.shoreline.client.api.module.BlockPlacerModule;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.impl.event.entity.player.PlayerMoveEvent;
import net.shoreline.client.impl.event.network.PlayerTickEvent;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.player.*;

/**
 * @author xgraza
 * @since 04/13/24
 */
public final class ScaffoldModule extends BlockPlacerModule
{
    Config<Boolean> towerConfig = new BooleanConfig("Tower", "Goes up faster when holding down space", true);
    Config<Boolean> keepYConfig = new BooleanConfig("KeepY", "Keeps your Y level", false);
    Config<Boolean> safeWalkConfig = new BooleanConfig("SafeWalk", "If to prevent you from falling off edges", true);
    Config<BlockPicker> pickerConfig = new EnumConfig<>("Picker", "How to pick a block from the hotbar", BlockPicker.NORMAL, BlockPicker.values());

    public ScaffoldModule()
    {
        super("Scaffold", "Rapidly places blocks under your feet", ModuleCategory.WORLD);
        unregister(strictDirectionConfig); // Legit never used lol
    }

    @Override
    protected void onDisable()
    {
        super.onDisable();
        if (mc.player != null)
        {
            Managers.INVENTORY.syncToClient();
        }
    }

    @EventListener
    public void onUpdate(final PlayerTickEvent event)
    {
        final BlockData data = getBlockData();
        if (data == null)
        {
            return;
        }

        final int blockSlot = getBlockSlot();
        if (blockSlot == -1)
        {
            return;
        }

        getRotationAnglesFor(data);
        if (grimConfig.getValue() && data.getHitResult() == null)
        {
            return;
        }

        final boolean result = Managers.INTERACT.placeBlock(data.getHitResult(), blockSlot, false, (state) ->
        {
            float[] angles = data.getAngles();
            if (angles == null)
            {
                return;
            }
            if (grimConfig.getValue())
            {
                if (state)
                {
                    Managers.ROTATION.setRotationSilent(angles[0], angles[1], true);
                }
                else
                {
                    Managers.ROTATION.setRotationSilentSync(true);
                }
            }
            else if (state)
            {
                setRotation(angles[0], angles[1]);
            }
        });

        if (result)
        {
            // if someone uses this on grim and complains "it doesnt work!!!" im gonna find their house
            if (towerConfig.getValue() && mc.options.jumpKey.isPressed())
            {
                final Vec3d velocity = mc.player.getVelocity();
                final double velocityY = velocity.y;
                if ((mc.player.isOnGround() || velocityY < 0.1) || velocityY <= 0.16477328182606651)
                {
                    mc.player.setVelocity(velocity.x, 0.42f, velocity.z);
                }
            }
        }
    }

    @EventListener
    public void onMove(final PlayerMoveEvent event)
    {
        if (safeWalkConfig.getValue() && MovementUtil.isInputtingMovement() && mc.player.isOnGround())
        {
            final Vec2f movement = MovementUtil.applySafewalk(event.getX(), event.getZ());

            // Not the best, but it somewhat works
            if (grimConfig.getValue())
            {
                final Vec3d velocity = mc.player.getVelocity();
                final double deltaX = velocity.getX() - movement.x;
                final double deltaZ = velocity.getZ() - movement.y;

                mc.options.sneakKey.setPressed(Math.abs(deltaX) > 9.0E-4
                        || Math.abs(deltaZ) > 9.0E-4);
                return;
            }

            event.setX(movement.x);
            event.setZ(movement.y);
            event.cancel();
        }
    }

    private int getBlockSlot()
    {
        final ItemStack serverStack = Managers.INVENTORY.getServerItem();
        if (!serverStack.isEmpty() && serverStack.getItem() instanceof BlockItem)
        {
            return Managers.INVENTORY.getServerSlot();
        }

        int blockSlot = -1;
        int count = 0;
        for (int i = 0; i < 9; ++i)
        {
            final ItemStack itemStack = mc.player.getInventory().getStack(i);
            if (!itemStack.isEmpty() && itemStack.getItem() instanceof BlockItem)
            {
                if (pickerConfig.getValue() == BlockPicker.NORMAL)
                {
                    return i;
                }

                if (blockSlot == -1 || itemStack.getCount() > count)
                {
                    blockSlot = i;
                    count = itemStack.getCount();
                }
            }
        }

        return blockSlot;
    }

    private void getRotationAnglesFor(final BlockData data)
    {
        final BlockPos pos = data.getPos();
        final Direction direction = data.getSide();

        if (!grimConfig.getValue())
        {
            data.setAngles(RotationUtil.getRotationsTo(mc.player.getEyePos(), pos.toCenterPos()));
            return;
        }

        // I could just make dynamic rotations and call it a day
        // but what's the fun in a simple approach?
        // This is aids.

        float yawOffset = 180;
        if (direction.equals(mc.player.getHorizontalFacing().getOpposite()))
        {
            yawOffset = 0;
        }

        final float rotationYaw = MovementUtil.getYawOffset(
                mc.player.input, mc.player.getYaw() - yawOffset);
        for (float yaw = rotationYaw - 55; yaw <= rotationYaw + 55; yaw += 0.5f)
        {
            for (float pitch = 75; pitch <= 90; pitch += 0.5f)
            {
                final float[] angles = { yaw, pitch };
                final HitResult result = RayCastUtil.rayCast(4.0, angles);
                if (result instanceof BlockHitResult hitResult
                        && hitResult.getBlockPos().equals(data.getPos())
                        && hitResult.getSide() == direction)
                {
                    data.setHitResult(hitResult);
                    data.setAngles(angles);
                    return;
                }
            }
        }

        data.setHitResult(null);
        data.setAngles(null);
    }

    private BlockData getBlockData()
    {
        double posY = mc.player.getY();
        if (!keepYConfig.getValue() || mc.player.isOnGround())
        {
            posY = (int) Math.round(mc.player.getY());
        }

        final BlockPos pos = PlayerUtil.getRoundedBlockPos(posY).down();

        for (final Direction direction : Direction.values())
        {
            final BlockPos neighbor = pos.offset(direction);
            if (!mc.world.getBlockState(neighbor).isReplaceable())
            {
                return new BlockData(neighbor, direction.getOpposite());
            }
        }

        for (final Direction direction : Direction.values())
        {
            final BlockPos neighbor = pos.offset(direction);
            if (mc.world.getBlockState(neighbor).isReplaceable())
            {
                for (final Direction direction1 : Direction.values())
                {
                    final BlockPos neighbor1 = neighbor.offset(direction1);
                    if (!mc.world.getBlockState(neighbor1).isReplaceable())
                    {
                        return new BlockData(neighbor1, direction1.getOpposite());
                    }
                }
            }
        }

        return null;
    }

    private static class BlockData
    {
        private BlockHitResult hitResult;
        private float[] angles;

        public BlockData(final BlockPos pos, final Direction direction)
        {
            this(new BlockHitResult(pos.toCenterPos(), direction, pos, false), null);
        }

        public BlockData(final BlockHitResult hitResult, final float[] angles)
        {
            this.hitResult = hitResult;
            this.angles = angles;
        }

        public BlockHitResult getHitResult()
        {
            return hitResult;
        }

        public void setHitResult(BlockHitResult hitResult)
        {
            this.hitResult = hitResult;
        }

        public BlockPos getPos()
        {
            return hitResult.getBlockPos();
        }

        public Direction getSide()
        {
            return hitResult.getSide();
        }

        public float[] getAngles()
        {
            return angles;
        }

        public void setAngles(float[] angles)
        {
            this.angles = angles;
        }
    }

    private enum BlockPicker
    {
        NORMAL,
        GREATEST
    }
}
