package com.neuvillette.ae2ct.api.xei.emi;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

// check emi loaded before use
public class EmiItem {
    public static void openRecipe(GenericStack itemStack, Boolean isOutput ){
        var what = itemStack.what();
        EmiIngredient emiIngredient;
        if(what instanceof AEItemKey){
            ItemStack stack = ((AEItemKey) what).getReadOnlyStack();
            emiIngredient = EmiStack.of(stack);
        }else if(what instanceof AEFluidKey){
            FluidStack stack = ((AEFluidKey) what).toStack(1000);
            emiIngredient = EmiStack.of(stack.getFluid());
        }else{
            return;
        }
        if (isOutput){
            EmiApi.displayRecipes(emiIngredient);
        }else{
            EmiApi.displayUses(emiIngredient);
        }

    }
}
