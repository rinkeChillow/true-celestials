package com.rinke_.truecelestials.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.WorldRenderer;
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

    // === persistent state ===
    @Unique private static boolean flipped = false;
    @Unique private static float lastSkyAngle = 0.0F;

    // === per-frame captured tickDelta ===
    @Unique private static float currentTickDelta = 0.0F;

    /**
     * Capture tickDelta once per renderSky call.
     * This runs exactly once per frame.
     */
    @Inject(method = "renderSky", at = @At("HEAD"))
    private void captureTickDelta(
            Matrix4f matrix4f,
            Matrix4f projectionMatrix,
            float tickDelta,
            net.minecraft.client.render.Camera camera,
            boolean thickFog,
            Runnable fogCallback,
            CallbackInfo ci
    ) {
        currentTickDelta = tickDelta;
    }

    @Unique
    private static void updateFlipState() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        float skyAngle = client.world.getSkyAngle(currentTickDelta);

        // Midday (sun at zenith)
        if (lastSkyAngle < 0.50F && skyAngle >= 0.50F) {
            flipped = !flipped;
        }

        // Midnight
        if (lastSkyAngle < 0.99F && skyAngle >= 0.99F) {
            flipped = !flipped;
        }

        lastSkyAngle = skyAngle;
    }

    /**
     * Flip UVs of sun & moon quads ONLY.
     */
    @ModifyArgs(
            method = "renderSky",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/VertexConsumer;texture(FF)Lnet/minecraft/client/render/VertexConsumer;"
            )
    )
    private void flipCelestialUVs(Args args) {
        updateFlipState();

        if (!flipped) return;

        float u = args.get(0);
        float v = args.get(1);

        // 180° rotation around quad center
        args.set(0, 1.0F - u);
        args.set(1, 1.0F - v);
    }
}
