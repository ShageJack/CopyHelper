package shagejack.copyhelper;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.Optional;
import java.util.function.Predicate;

public class EventHandle {

    public static final String KEY_CATEGORIES_COPYHELPER = "key.categories.copyhelper";
    public static final String KEY_COPY = "key.quickcopy";
    public static KeyMapping copyKeyMapping;

    public static void registerKey(final FMLClientSetupEvent event) {
        copyKeyMapping = new KeyMapping(KEY_COPY, KeyConflictContext.UNIVERSAL, InputConstants.getKey("key.keyboard.z"), KEY_CATEGORIES_COPYHELPER);
        ClientRegistry.registerKeyBinding(copyKeyMapping);
    }


    public static void onKeyInput(final InputEvent.KeyInputEvent event) {
        if (copyKeyMapping.consumeClick()) {
            Screen screen = Minecraft.getInstance().screen;

            // copying item
            if (screen instanceof AbstractContainerScreen<?> containerScreen && !screen.isPauseScreen()) {
                Slot slot = containerScreen.getSlotUnderMouse();
                if (slot == null)
                    return;

                ItemStack stack = slot.getItem();
                if (stack.isEmpty())
                    return;

                ResourceLocation itemName = stack.getItem().getRegistryName();
                if (itemName != null) {
                    Minecraft.getInstance().keyboardHandler.setClipboard(itemName.toString());
                }

                return;
            }

            //copying entities
            ResourceLocation entityName = getEntityLookingAt();
            if (entityName != null) {
                Minecraft.getInstance().keyboardHandler.setClipboard(entityName.toString());
                return;
            }

            // copying block
            ResourceLocation blockName = getBlockLookingAt();
            if (blockName != null) {
                Minecraft.getInstance().keyboardHandler.setClipboard(blockName.toString());
            }
        }
    }

    public static ResourceLocation getBlockLookingAt() {
        ClientLevel level = Minecraft.getInstance().level;
        LocalPlayer player = Minecraft.getInstance().player;
        if (level == null || player == null)
            return null;

        float f = player.getXRot();
        float f1 = player.getYRot();
        Vec3 vec3 = player.getEyePosition();
        float f2 = Mth.cos(-f1 * ((float)Math.PI / 180F) - (float)Math.PI);
        float f3 = Mth.sin(-f1 * ((float)Math.PI / 180F) - (float)Math.PI);
        float f4 = -Mth.cos(-f * ((float)Math.PI / 180F));
        float f5 = Mth.sin(-f * ((float)Math.PI / 180F));
        float f6 = f3 * f4;
        float f7 = f2 * f4;
        double d0 = player.getReachDistance();
        Vec3 vec31 = vec3.add((double)f6 * d0, (double)f5 * d0, (double)f7 * d0);
        BlockHitResult result = level.clip(new ClipContext(vec3, vec31, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        Block block = level.getBlockState(result.getBlockPos()).getBlock();
        if (block != Blocks.AIR) {
            return block.getRegistryName();
        }
        return null;
    }

    public static ResourceLocation getEntityLookingAt() {
        ClientLevel level = Minecraft.getInstance().level;
        LocalPlayer player = Minecraft.getInstance().player;
        if (level == null || player == null)
            return null;

        double distance = player.getReachDistance();
        double distanceSqr = distance * distance;
        Vec3 eyePos = player.getEyePosition();
        Vec3 viewVec = player.getViewVector(1.0F).scale(distance);
        Vec3 endPos = eyePos.add(viewVec);
        AABB aabb = player.getBoundingBox().expandTowards(viewVec).inflate(1.0D);
        Predicate<Entity> predicate = (entity) -> !entity.isSpectator() && entity.isPickable();
        EntityHitResult entityhitresult = ProjectileUtil.getEntityHitResult(player, eyePos, endPos, aabb, predicate, distanceSqr);

        if (entityhitresult != null) {
            Entity entity = eyePos.distanceToSqr(entityhitresult.getLocation()) > distanceSqr ? null : entityhitresult.getEntity();
            if (entity != null) {
                return entity.getType().getRegistryName();
            }
        }

        return null;
    }
}
