package com.neuvillette.ae2ct.api;

import appeng.api.stacks.GenericStack;
import appeng.crafting.CraftingPlan;

import java.util.ArrayList;
import java.util.List;

public class RecipeHelper {
    public GenericStack output;
    public List<Recipe> recipes;
    public RecipeHelper(GenericStack output, List<Recipe> recipes) {
        this.output = output;
        this.recipes = recipes;
    }

    public static RecipeHelper fromCraftingPlan(CraftingPlan plan){
        var patternTimes = plan.patternTimes();
        var recipes = new ArrayList<Recipe>();
        patternTimes.forEach((k,v) -> {
            var is = k.getInputs();
            var inputs = new ArrayList<GenericStack>();
            var ouputs = k.getOutputs();
            for(var i : is){
                var genericStacks = i.getPossibleInputs();
                var time = i.getMultiplier();
                for(var g : genericStacks){
                    inputs.add(new GenericStack(g.what(), g.amount() * time));
                }
            }
            recipes.add(new Recipe(inputs, ouputs, v));
        });
        return new RecipeHelper(plan.finalOutput(), recipes);
    }


    public record Recipe(
            List<GenericStack> inputs,
            List<GenericStack> outputs,
            Long times
    ) {
    }
}
