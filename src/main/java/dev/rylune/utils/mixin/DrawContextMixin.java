package dev.rylune.utils.mixin;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.rylune.utils.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(DrawContext.class)
public class DrawContextMixin {
    private boolean rylune$replace(Identifier id,int x,int y,int w,int h) {
        if(!RyluneUtils.config.crosshair || !id.getNamespace().equals("minecraft") || !id.getPath().equals("hud/crosshair")) return false;
        Crosshair.draw((DrawContext)(Object)this,x+w/2,y+h/2); return true;
    }
    @Inject(method="drawGuiTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIII)V",at=@At("HEAD"),cancellable=true)
    private void rylune$texture(RenderPipeline p,Identifier id,int x,int y,int w,int h,CallbackInfo ci) { if(rylune$replace(id,x,y,w,h)) ci.cancel(); }
    @Inject(method="drawGuiTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIIII)V",at=@At("HEAD"),cancellable=true,require=0)
    private void rylune$tinted(RenderPipeline p,Identifier id,int x,int y,int w,int h,int tint,CallbackInfo ci) { if(rylune$replace(id,x,y,w,h)) ci.cancel(); }
    @Inject(method="drawGuiTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIIIF)V",at=@At("HEAD"),cancellable=true,require=0)
    private void rylune$alpha(RenderPipeline p,Identifier id,int x,int y,int w,int h,float alpha,CallbackInfo ci) { if(rylune$replace(id,x,y,w,h)) ci.cancel(); }
}
