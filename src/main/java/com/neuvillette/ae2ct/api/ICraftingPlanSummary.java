package com.neuvillette.ae2ct.api;

import appeng.api.networking.crafting.ICraftingPlan;
import appeng.crafting.CraftingPlan;

public interface ICraftingPlanSummary {
    public CraftingPlan getJob();
    public void setJob(CraftingPlan job);
}
