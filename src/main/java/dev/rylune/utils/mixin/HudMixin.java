package dev.rylune.utils.mixin;
import dev.rylune.utils.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(InGameHud.class)
public class HudMixin {
    @Inject(method="render",at=@At("TAIL"))
    private void rylune$hud(DrawContext ctx,RenderTickCounter ticks,CallbackInfo ci) {
        var c=RyluneUtils.config; var mc=MinecraftClient.getInstance();
        if(!c.sprint || !c.sprintHud || mc.player==null || mc.options.hudHidden || mc.currentScreen!=null) return;
        String text=RyluneUtils.sprintActive?(mc.player.isSprinting()?"Sprint: active":"Sprint: ready"):"Sprint: off";
        ctx.drawTextWithShadow(mc.textRenderer,text,Math.min(c.sprintX,Math.max(0,ctx.getScaledWindowWidth()-mc.textRenderer.getWidth(text))),Math.min(c.sprintY,Math.max(0,ctx.getScaledWindowHeight()-10)),0xFF000000|c.sprintColor);
    }
}
