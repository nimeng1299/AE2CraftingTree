package com.neuvillette.ae2ct.api;

import appeng.api.networking.crafting.ICraftingPlan;

public interface ICraftingPlanSummary {
    public ICraftingPlan getJob();
    public void setJob(ICraftingPlan job);
}
