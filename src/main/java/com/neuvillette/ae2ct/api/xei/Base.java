package com.neuvillette.ae2ct.api.xei;

import appeng.api.stacks.GenericStack;
import com.neuvillette.ae2ct.api.xei.emi.EmiItem;
import com.neuvillette.ae2ct.api.xei.jei.JeiItem;
import net.neoforged.fml.ModList;

public class Base {
    public static boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    public static void openRecipe(GenericStack itemStack, Boolean isOutput ){
        if(isModLoaded("emi")){
            EmiItem.openRecipe(itemStack, isOutput);
        } else if(isModLoaded("jei")){
            JeiItem.openRecipe(itemStack, isOutput);
        }
    }
}
