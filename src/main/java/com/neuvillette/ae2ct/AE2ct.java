package com.neuvillette.ae2ct;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(AE2ct.MODID)
public class AE2ct
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "ae2ct";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    public AE2ct(IEventBus modEventBus, ModContainer modContainer)
    {
    }

}
