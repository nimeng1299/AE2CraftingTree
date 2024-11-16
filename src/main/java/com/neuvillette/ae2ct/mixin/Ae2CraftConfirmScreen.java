package com.neuvillette.ae2ct.mixin;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.Icon;
import appeng.client.gui.me.crafting.CraftConfirmScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.menu.me.crafting.CraftConfirmMenu;
import com.neuvillette.ae2ct.api.ToolTipText;
import com.neuvillette.ae2ct.gui.ChangeButton;
import com.neuvillette.ae2ct.gui.CraftingTreeScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftConfirmScreen.class)
public class Ae2CraftConfirmScreen extends AEBaseScreen<CraftConfirmMenu> {
    public Ae2CraftConfirmScreen(CraftConfirmMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }

    @Inject(at = @At("RETURN"), method = "<init>")
    private void CraftConfirmScreenInit(CraftConfirmMenu menu, Inventory playerInventory, Component title,
                      ScreenStyle style, CallbackInfo info){
        this.addToLeftToolbar(new ChangeButton(() -> {
            var parent = (CraftConfirmScreen) (Object) this;
            if(parent.getMenu().getPlan() == null)
            {
                parent.getMenu().getPlayer().sendSystemMessage(Component.translatable("ae2ct.openscreen.plannull"));
            }
            else{
                switchToScreen(new CraftingTreeScreen(parent));
            }
        }, Icon.CRAFT_HAMMER, ToolTipText.ShowTree));

    }
}
