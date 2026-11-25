package dev.rylune.utils.mixin;
import dev.rylune.utils.RyluneUtils;
import dev.rylune.utils.MotionBlur;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.memory.ObjectPool;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Shadow @Final private ObjectPool pool;
    @Inject(method="renderWorld",at=@At("TAIL"))
    private void rylune$blur(RenderTickCounter ticks,CallbackInfo ci){
        if(!RyluneUtils.config.motionBlur){MotionBlur.reset();return;}
        MotionBlur.render((GameRenderer)(Object)this,pool);
    }
    @Inject(method="getFov",at=@At("RETURN"),cancellable=true)
    private void rylune$zoom(Camera camera,float tickProgress,boolean changingFov,CallbackInfoReturnable<Float> cir) {
        if(changingFov && RyluneUtils.config.zoom){float old=cir.getReturnValueF();float next=RyluneUtils.zoomFov(old);if(next!=old)cir.setReturnValue(next);}
    }
}
