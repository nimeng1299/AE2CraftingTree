package com.neuvillette.ae2ct.mixin;

import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.security.IActionSource;
import appeng.menu.me.crafting.CraftingPlanSummary;
import com.neuvillette.ae2ct.api.ICraftingPlanSummary;
import com.neuvillette.ae2ct.api.RecipeHelper;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import appeng.crafting.CraftingPlan;

@Mixin(CraftingPlanSummary.class)
public class AE2CraftingPlanSummary implements ICraftingPlanSummary {
    @Unique
    private RecipeHelper jobs;

    @Inject(at = @At("TAIL"), method = "fromJob", cancellable = true, remap = false)
    private static void buildEX(IGrid grid, IActionSource actionSource, ICraftingPlan job,CallbackInfoReturnable<CraftingPlanSummary> cir){
        var r = cir.getReturnValue();
        ((ICraftingPlanSummary)r).setJob(RecipeHelper.fromCraftingPlan((CraftingPlan) job));
        cir.setReturnValue(r);
    }

    @Inject(at = @At("TAIL"), method = "write", remap = false)
    private void write(FriendlyByteBuf buffer, CallbackInfo ci){
        jobs.write(buffer);
    }

    @Inject(at = @At("TAIL"), method = "read", cancellable = true, remap = false)
    private static void read(FriendlyByteBuf buffer, CallbackInfoReturnable<CraftingPlanSummary> cir){
        var r = cir.getReturnValue();
        var h = RecipeHelper.read(buffer);
        ((ICraftingPlanSummary)r).setJob(h);
        cir.setReturnValue(r);

    }

    @Override
    public RecipeHelper getJob() {
        return jobs;
    }

    @Override
    public void setJob(RecipeHelper job) {
        this.jobs = job;
    }
}
