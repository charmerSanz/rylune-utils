package dev.rylune.utils;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
public final class BlockFill {
    private static VoxelShape cachedShape;
    private static float[] vertices=new float[0];
    public static void register(){
        WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register((context,state)->{
            Config c=RyluneUtils.config;
            if(!c.block||c.blockFillOpacity==0||state==null||context.matrices()==null||context.consumers()==null)return true;
            VoxelShape shape=state.shape();
            if(shape!=cachedShape){cache(shape);cachedShape=shape;}
            if(vertices.length==0)return true;
            var camera=MinecraftClient.getInstance().gameRenderer.getCamera().getCameraPos();
            var pos=state.pos();var matrix=context.matrices().peek().getPositionMatrix();
            float dx=(float)(pos.getX()-camera.x),dy=(float)(pos.getY()-camera.y),dz=(float)(pos.getZ()-camera.z);
            var buffer=context.consumers().getBuffer(RenderLayers.debugQuads());
            int color=RyluneUtils.argb(c.blockColor,c.blockFillOpacity);
            for(int i=0;i<vertices.length;i+=3)buffer.vertex(matrix,vertices[i]+dx,vertices[i+1]+dy,vertices[i+2]+dz).color(color);
            return true;
        });
    }
    private static void cache(VoxelShape shape){
        var boxes=shape.getBoundingBoxes();vertices=new float[boxes.size()*72];int off=0;
        for(Box b:boxes){
            float x=(float)b.minX-.002f,y=(float)b.minY-.002f,z=(float)b.minZ-.002f,X=(float)b.maxX+.002f,Y=(float)b.maxY+.002f,Z=(float)b.maxZ+.002f;
            float[] a={x,y,z,x,y,Z,X,y,Z,X,y,z, x,Y,z,X,Y,z,X,Y,Z,x,Y,Z, x,y,z,x,Y,z,x,Y,Z,x,y,Z, X,y,z,X,y,Z,X,Y,Z,X,Y,z, x,y,z,X,y,z,X,Y,z,x,Y,z, x,y,Z,x,Y,Z,X,Y,Z,X,y,Z};
            System.arraycopy(a,0,vertices,off,a.length);off+=a.length;
        }
    }
}
