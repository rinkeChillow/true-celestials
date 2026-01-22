package com.rinke_.truecelestials.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

@Config(name = "truecelestials")
public class TrueCelestialsConfig implements ConfigData {

    @ConfigEntry.Gui.Tooltip
    public boolean enableMod = true;

    public static void init() {
        AutoConfig.register(TrueCelestialsConfig.class, GsonConfigSerializer::new);
    }

    public static TrueCelestialsConfig get() {
        return AutoConfig.getConfigHolder(TrueCelestialsConfig.class).getConfig();
    }
}
