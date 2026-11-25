package dev.rylune.utils.mixin;
import dev.rylune.utils.RyluneUtils;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method="getFov",at=@At("RETURN"),cancellable=true)
    private void rylune$zoom(Camera camera,float tickProgress,boolean changingFov,CallbackInfoReturnable<Float> cir) {
        if(changingFov && RyluneUtils.config.zoom){float old=cir.getReturnValueF();float next=RyluneUtils.zoomFov(old);if(next!=old)cir.setReturnValue(next);}
    }
}
