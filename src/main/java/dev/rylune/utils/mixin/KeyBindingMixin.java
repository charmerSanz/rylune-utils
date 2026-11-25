package dev.rylune.utils.mixin;
import dev.rylune.utils.RyluneUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.util.PlayerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(KeyboardInput.class)
public abstract class KeyBindingMixin extends Input {
    @Inject(method="tick",at=@At("TAIL"))
    private void rylune$sprint(CallbackInfo ci){
        if(!RyluneUtils.config.sprint || !RyluneUtils.sprintActive || playerInput.sprint())return;
        if(MinecraftClient.getInstance().currentScreen!=null)return;
        var p=playerInput;
        playerInput=new PlayerInput(p.forward(),p.backward(),p.left(),p.right(),p.jump(),p.sneak(),true);
    }
}
