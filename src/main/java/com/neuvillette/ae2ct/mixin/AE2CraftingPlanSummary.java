package com.neuvillette.ae2ct.mixin;

import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.security.IActionSource;
import appeng.crafting.CraftingPlan;
import appeng.menu.me.crafting.CraftingPlanSummary;
import com.neuvillette.ae2ct.api.ICraftingPlanSummary;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CraftingPlanSummary.class)
public class AE2CraftingPlanSummary implements ICraftingPlanSummary {
    @Unique
    private CraftingPlan jobs;
    @Inject(at = @At("TAIL"), method = "fromJob", cancellable = true)
    private static void buildEX(IGrid grid, IActionSource actionSource, ICraftingPlan job, CallbackInfoReturnable<CraftingPlanSummary> cir){
        var r = cir.getReturnValue();
        ((ICraftingPlanSummary)r).setJob((CraftingPlan) job);
        cir.setReturnValue(r);
    }

    @Inject(at = @At("TAIL"), method = "write")
    private void write(RegistryFriendlyByteBuf buffer, CallbackInfo ci){
        buffer.writeInt(jobs.patternTimes().size());
    }

    @Inject(at = @At("TAIL"), method = "read", cancellable = true)
    private static void read(RegistryFriendlyByteBuf buffer, CallbackInfoReturnable<CraftingPlanSummary> cir){
        var r = cir.getReturnValue();
        ((ICraftingPlanSummary)r).setJob((new CraftingPlan(null, buffer.readInt(), false, false, null, null, null, null)));

    }

    @Override
    public CraftingPlan getJob(){
        return jobs;
    }

    @Override
    public void setJob(CraftingPlan job) {
        this.jobs = job;
    }

}
