package dev.rylune.utils.mixin;
import dev.rylune.utils.RyluneUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(KeyBinding.class)
public class KeyBindingMixin {
    @Inject(method="isPressed",at=@At("RETURN"),cancellable=true)
    private void rylune$sprint(CallbackInfoReturnable<Boolean> cir) {
        if(!RyluneUtils.config.sprint || !RyluneUtils.sprintActive) return;
        var mc=MinecraftClient.getInstance();
        if(mc!=null && mc.player!=null && mc.currentScreen==null && mc.options.sprintKey==(Object)this) cir.setReturnValue(true);
    }
}
