package dev.rylune.utils;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;

public final class BlockFill {
    public static void register() {
        WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register((context,state)-> {
            Config c=RyluneUtils.config;
            if(!c.block || c.blockFillOpacity==0 || state==null || context.matrices()==null || context.consumers()==null) return true;
            var camera=MinecraftClient.getInstance().gameRenderer.getCamera().getCameraPos();
            var pos=state.pos(); var matrix=context.matrices().peek();
            var buffer=context.consumers().getBuffer(RenderLayers.debugQuads());
            int color=RyluneUtils.argb(c.blockColor,c.blockFillOpacity);
            for(Box box:state.shape().getBoundingBoxes()) {
                double x=pos.getX()-camera.x,y=pos.getY()-camera.y,z=pos.getZ()-camera.z;
                cube(buffer,matrix,box.offset(x,y,z).expand(.002),color);
            }
            return true;
        });
    }
    private static void cube(VertexConsumer v,MatrixStack.Entry m,Box b,int c) {
        float x=(float)b.minX,y=(float)b.minY,z=(float)b.minZ,X=(float)b.maxX,Y=(float)b.maxY,Z=(float)b.maxZ;
        quad(v,m,c,x,y,z,x,y,Z,X,y,Z,X,y,z); quad(v,m,c,x,Y,z,X,Y,z,X,Y,Z,x,Y,Z);
        quad(v,m,c,x,y,z,x,Y,z,x,Y,Z,x,y,Z); quad(v,m,c,X,y,z,X,y,Z,X,Y,Z,X,Y,z);
        quad(v,m,c,x,y,z,X,y,z,X,Y,z,x,Y,z); quad(v,m,c,x,y,Z,x,Y,Z,X,Y,Z,X,y,Z);
    }
    private static void quad(VertexConsumer v,MatrixStack.Entry m,int c,float... a) {
        for(int i=0;i<12;i+=3) v.vertex(m.getPositionMatrix(),a[i],a[i+1],a[i+2]).color(c);
    }
}
