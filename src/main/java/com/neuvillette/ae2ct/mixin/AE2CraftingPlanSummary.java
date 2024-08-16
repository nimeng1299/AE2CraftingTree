package com.neuvillette.ae2ct.mixin;

import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.security.IActionSource;
import appeng.crafting.CraftingPlan;
import appeng.menu.me.crafting.CraftingPlanSummary;
import com.neuvillette.ae2ct.api.ICraftingPlanSummary;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CraftingPlanSummary.class)
public class AE2CraftingPlanSummary implements ICraftingPlanSummary {
    @Unique
    private ICraftingPlan jobs;
    @Inject(at = @At("TAIL"), method = "fromJob", cancellable = true)
    private static void buildEX(IGrid grid, IActionSource actionSource, ICraftingPlan job, CallbackInfoReturnable<CraftingPlanSummary> cir){
        var r = cir.getReturnValue();
        ((ICraftingPlanSummary)r).setJob(job);
        cir.setReturnValue(r);
    }

    @Override
    public ICraftingPlan getJob(){
        return jobs;
    }

    @Override
    public void setJob(ICraftingPlan job) {
        this.jobs = job;
    }

}
