package com.rinke_.truecelestials;

import com.rinke_.truecelestials.config.TrueCelestialsConfig;
import net.fabricmc.api.ClientModInitializer;

public class TrueCelestialsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        TrueCelestialsConfig.init();

    }
}
