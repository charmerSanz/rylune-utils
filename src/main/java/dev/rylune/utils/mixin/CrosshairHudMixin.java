package dev.rylune.utils.mixin;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.rylune.utils.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(InGameHud.class)
public class CrosshairHudMixin {
    private static boolean rylune$replace(DrawContext ctx,Identifier id,int x,int y,int w,int h) {
        if(!RyluneUtils.config.crosshair || !id.getNamespace().equals("minecraft") || !id.getPath().equals("hud/crosshair")) return false;
        Crosshair.draw(ctx,x+w/2,y+h/2);return true;
    }
    @WrapOperation(method="renderCrosshair",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIII)V"),require=0)
    private void rylune$basic(DrawContext ctx,RenderPipeline p,Identifier id,int x,int y,int w,int h,Operation<Void> original){
        if(!rylune$replace(ctx,id,x,y,w,h)) original.call(ctx,p,id,x,y,w,h);
    }
    @WrapOperation(method="renderCrosshair",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIIII)V"),require=0)
    private void rylune$tint(DrawContext ctx,RenderPipeline p,Identifier id,int x,int y,int w,int h,int color,Operation<Void> original){
        if(!rylune$replace(ctx,id,x,y,w,h)) original.call(ctx,p,id,x,y,w,h,color);
    }
    @WrapOperation(method="renderCrosshair",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIIIF)V"),require=0)
    private void rylune$alpha(DrawContext ctx,RenderPipeline p,Identifier id,int x,int y,int w,int h,float alpha,Operation<Void> original){
        if(!rylune$replace(ctx,id,x,y,w,h)) original.call(ctx,p,id,x,y,w,h,alpha);
    }
}
