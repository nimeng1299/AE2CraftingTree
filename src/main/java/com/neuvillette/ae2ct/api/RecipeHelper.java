package com.neuvillette.ae2ct.api;

import appeng.api.stacks.GenericStack;
import appeng.crafting.CraftingPlan;
import net.minecraft.network.RegistryFriendlyByteBuf;

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
                var g = genericStacks[0];
                inputs.add(new GenericStack(g.what(), g.amount() * time));
            }
            recipes.add(new Recipe(inputs, ouputs, v));
        });
        return new RecipeHelper(plan.finalOutput(), recipes);
    }

    public void write(RegistryFriendlyByteBuf buffer){
        GenericStack.writeBuffer(output, buffer);
        buffer.writeVarInt(recipes.size());
        for(var r : recipes){
            r.write(buffer);
        }
    }

    public static RecipeHelper read(RegistryFriendlyByteBuf buffer){
        var output = GenericStack.readBuffer(buffer);
        var recipes = new ArrayList<Recipe>();
        var recipesSize = buffer.readVarInt();
        for(int i = 0; i < recipesSize; i++){
            recipes.add(Recipe.read(buffer));
        }
        return new RecipeHelper(output, recipes);
    }


    public record Recipe(
            List<GenericStack> inputs,
            List<GenericStack> outputs,
            Long times
    ) {
        public void write(RegistryFriendlyByteBuf buffer){
            buffer.writeVarInt(inputs.size());
            for(var i : inputs){
                GenericStack.writeBuffer(i, buffer);
            }
            buffer.writeVarInt(outputs.size());
            for(var o : outputs){
                GenericStack.writeBuffer(o, buffer);
            }
            buffer.writeVarLong(times);
        }

        public static Recipe read(RegistryFriendlyByteBuf buffer){
            var inputs = new ArrayList<GenericStack>();
            var inputsSize = buffer.readVarInt();
            for(int i = 0; i < inputsSize; i++){
                inputs.add(GenericStack.readBuffer(buffer));
            }
            var outputs = new ArrayList<GenericStack>();
            var outputsSize = buffer.readVarInt();
            for(int i = 0; i < outputsSize; i++){
                outputs.add(GenericStack.readBuffer(buffer));
            }
            var times = buffer.readVarLong();
            return new Recipe(inputs, outputs, times);
        }

    }
}
