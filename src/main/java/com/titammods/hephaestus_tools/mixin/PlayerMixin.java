package com.titammods.hephaestus_tools.mixin;

import com.titammods.hephaestus_tools.tools.item.ToolItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @Inject(method = "getProjectile", at = @At("HEAD"), cancellable = true)
    private void hephaestusTools$bowProjectile(ItemStack heldWeapon, CallbackInfoReturnable<ItemStack> cir) {
        if (heldWeapon.getItem() instanceof ToolItems.BowItem) {
            cir.setReturnValue(((Player) (Object) this).getProjectile(new ItemStack(Items.BOW)));
        }
    }
}
