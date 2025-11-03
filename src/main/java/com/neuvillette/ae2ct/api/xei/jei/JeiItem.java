package com.neuvillette.ae2ct.api.xei.jei;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import com.neuvillette.ae2ct.AE2ct;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.runtime.IRecipesGui;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

@JeiPlugin
public class JeiItem implements IModPlugin {
    private static  IJeiRuntime jeiRuntime;
    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return AE2ct.id("favorite_item");
    }

    @Override
    public void onRuntimeAvailable(@NotNull IJeiRuntime jeiRuntime) {
        JeiItem.jeiRuntime = jeiRuntime;

    }

    public static void openRecipe(GenericStack itemStack, Boolean isOutput ) {
        if(jeiRuntime == null || itemStack == null){
            return;
        }
        IFocusFactory focusFactory = jeiRuntime.getJeiHelpers().getFocusFactory();
        IRecipesGui recipesGui = jeiRuntime.getRecipesGui();


        var what = itemStack.what();
        if(what instanceof AEItemKey){
            ItemStack stack = ((AEItemKey) what).getReadOnlyStack();

            IFocus<ItemStack> focus = focusFactory.createFocus(
                    isOutput ? RecipeIngredientRole.OUTPUT : RecipeIngredientRole.INPUT,
                    VanillaTypes.ITEM_STACK,
                    stack
            );

            recipesGui.show(focus);
        } else if(what instanceof AEFluidKey){
            FluidStack stack = ((AEFluidKey) what).toStack(1000);

            IFocus<FluidStack> focus = focusFactory.createFocus(
                    isOutput ? RecipeIngredientRole.OUTPUT : RecipeIngredientRole.INPUT,
                    NeoForgeTypes.FLUID_STACK,
                    stack
            );

            recipesGui.show(focus);
        } else {
            return;
        }



    }
}