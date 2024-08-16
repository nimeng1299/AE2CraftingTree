package com.neuvillette.ae2ct.api;

import appeng.api.networking.crafting.ICraftingPlan;
import appeng.crafting.CraftingPlan;

public interface ICraftingPlanSummary {
    public RecipeHelper getJob();
    public void setJob(RecipeHelper job);
}
