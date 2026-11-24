package dev.rylune.utils.mixin;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.rylune.utils.RyluneUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(KeyboardInput.class)
public class KeyBindingMixin {
    @WrapOperation(method="tick",at=@At(value="INVOKE",target="Lnet/minecraft/client/option/KeyBinding;isPressed()Z"),require=0)
    private boolean rylune$sprint(KeyBinding key,Operation<Boolean> original){
        if(RyluneUtils.config.sprint && RyluneUtils.sprintActive){
            var mc=MinecraftClient.getInstance();
            if(mc.currentScreen==null && key==mc.options.sprintKey) return true;
        }
        return original.call(key);
    }
}
