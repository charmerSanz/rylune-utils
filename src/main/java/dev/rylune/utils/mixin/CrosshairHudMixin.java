package dev.rylune.utils.mixin;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.rylune.utils.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(InGameHud.class)
public class CrosshairHudMixin {
    @Unique private static final Identifier RYLUNE_CROSSHAIR=Identifier.of("minecraft","hud/crosshair");
    @Unique private static boolean rylune$replace(DrawContext ctx,Identifier id,int x,int y,int w,int h) {
        if(!RyluneUtils.config.crosshair || !RYLUNE_CROSSHAIR.equals(id)) return false;
        Crosshair.draw(ctx,x+w/2,y+h/2);return true;
    }
    @WrapWithCondition(method="renderCrosshair",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIII)V"),require=0)
    private boolean rylune$basic(DrawContext ctx,RenderPipeline p,Identifier id,int x,int y,int w,int h){return !rylune$replace(ctx,id,x,y,w,h);}
    @WrapWithCondition(method="renderCrosshair",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIIII)V"),require=0)
    private boolean rylune$tint(DrawContext ctx,RenderPipeline p,Identifier id,int x,int y,int w,int h,int color){return !rylune$replace(ctx,id,x,y,w,h);}
    @WrapWithCondition(method="renderCrosshair",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIIIF)V"),require=0)
    private boolean rylune$alpha(DrawContext ctx,RenderPipeline p,Identifier id,int x,int y,int w,int h,float alpha){return !rylune$replace(ctx,id,x,y,w,h);}
}
