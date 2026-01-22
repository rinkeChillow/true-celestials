package com.rinke_.truecelestials.mixin;

import com.rinke_.truecelestials.config.TrueCelestialsConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.DimensionEffects;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @Unique private static boolean flipped = false;
    @Unique private static float currentTickDelta = 0.0F;

    /*CAPTURE TICK DELTA */

    @Inject(method = "renderSky", at = @At("HEAD"))
    private void captureTickDelta(
            Matrix4f matrix4f,
            Matrix4f projectionMatrix,
            float tickDelta,
            Camera camera,
            boolean thickFog,
            Runnable fogCallback,
            CallbackInfo ci
    ) {
        currentTickDelta = tickDelta;
    }

    /*ORIENTATION ENFORCEMENT */

    @Unique
    private static void enforceFlipState() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        // mod config
        if (!TrueCelestialsConfig.get().enableMod) {
            flipped = false;
            return;
        }

        // Overworld only
        if (client.world.getDimensionEffects().getSkyType() != DimensionEffects.SkyType.NORMAL) {
            flipped = false;
            return;
        }

        float skyAngle = client.world.getSkyAngle(currentTickDelta);

        //orientation rule
        boolean shouldBeFlipped = skyAngle >= 0.50F;

        flipped = shouldBeFlipped;
    }

    /*SUN + MOON UV FLIP*/

    @ModifyArgs(
            method = "renderSky",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/BufferBuilder;vertex(Lorg/joml/Matrix4f;FFF)Lnet/minecraft/client/render/VertexConsumer;"
            )
    )
    private void flipSunAndMoonGeometry(Args args) {
        enforceFlipState();
        if (!flipped) return;

        float x = args.get(1);
        float y = args.get(2);
        float z = args.get(3);

        if (y == 100.0F || y == -100.0F) {
            args.set(1, -x); // flip X
            args.set(3, -z); // flip Z
        }
    }
}

// if you are reading this , most of this code was made with the assistance of AI , I don't claim that I made the whole thing myself