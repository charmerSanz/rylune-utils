package dev.rylune.utils.mixin;
import dev.rylune.utils.RyluneUtils;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @ModifyVariable(method="drawBlockOutline",at=@At("HEAD"),argsOnly=true,ordinal=0)
    private int rylune$color(int vanilla) { var c=RyluneUtils.config; return c.block?RyluneUtils.argb(c.blockColor,c.blockOpacity):vanilla; }
    @ModifyVariable(method="drawBlockOutline",at=@At("HEAD"),argsOnly=true,ordinal=0)
    private float rylune$width(float vanilla) { var c=RyluneUtils.config; return c.block?c.blockWidth:vanilla; }
}
