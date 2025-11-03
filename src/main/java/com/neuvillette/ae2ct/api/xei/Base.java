package com.neuvillette.ae2ct.api.xei;

import appeng.api.stacks.GenericStack;
import com.neuvillette.ae2ct.api.xei.jei.JeiItem;
import net.minecraftforge.fml.ModList;

public class Base {
    public static boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    public static void openRecipe(GenericStack itemStack, Boolean isOutput ){
        if(isModLoaded("jei")){
            JeiItem.openRecipe(itemStack, isOutput);
        }
    }
}
