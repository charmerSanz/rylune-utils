package dev.rylune.utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
public final class Crosshair {
    private static final int SIZE=65,C=SIZE/2;
    private static final Identifier ID=Identifier.of("ryluneutils","crosshair_cache");
    private static NativeImageBackedTexture texture;
    private static boolean dirty=true;
    public static void invalidate(){dirty=true;}
    public static void draw(DrawContext ctx,int x,int y){
        if(texture==null || texture.getImage()==null){
            texture=new NativeImageBackedTexture(()->"Rylune crosshair",SIZE,SIZE,true);
            MinecraftClient.getInstance().getTextureManager().registerTexture(ID,texture);dirty=true;
        }
        if(dirty){rebuild(texture.getImage());texture.upload();dirty=false;}
        ctx.drawTexture(RenderPipelines.GUI_TEXTURED,ID,x-C,y-C,0f,0f,SIZE,SIZE,SIZE,SIZE,0xFFFFFFFF);
    }
    private static void rect(NativeImage image,int x1,int y1,int x2,int y2,int pad,int color){
        for(int y=Math.max(0,y1-pad);y<Math.min(SIZE,y2+pad);y++)for(int x=Math.max(0,x1-pad);x<Math.min(SIZE,x2+pad);x++)image.setColorArgb(x,y,color);
    }
    private static void rebuild(NativeImage image){
        for(int y=0;y<SIZE;y++)for(int x=0;x<SIZE;x++)image.setColorArgb(x,y,0);
        Config c=RyluneUtils.config;
        if(c.crossOutline) paint(image,1,RyluneUtils.argb(0,c.crossOpacity));
        paint(image,0,RyluneUtils.argb(c.crossColor,c.crossOpacity));
    }
    private static void paint(NativeImage d,int pad,int color){
        Config c=RyluneUtils.config;int x=C,y=C,t=c.crossThickness,g=c.crossGap,l=c.crossLength,a=t/2,b=t-a;
        if(c.crossShape==4){
            String cells=c.customPixels;
            for(int i=0;i<225;i++)if(cells.charAt(i)=='1')rect(d,x+i%15-7,y+i/15-7,x+i%15-6,y+i/15-6,pad,color);
            return;
        }
        if(c.crossShape==0||c.crossShape==2){
            rect(d,x-g-l,y-a,x-g,y+b,pad,color);rect(d,x+g+1,y-a,x+g+l+1,y+b,pad,color);
            if(c.crossShape==0)rect(d,x-a,y-g-l,x+b,y-g,pad,color);
            rect(d,x-a,y+g+1,x+b,y+g+l+1,pad,color);
        }else if(c.crossShape==3){
            int r=g+l;rect(d,x-r,y-r,x+r+1,y-r+t,pad,color);rect(d,x-r,y+r-t+1,x+r+1,y+r+1,pad,color);
            rect(d,x-r,y-r+t,x-r+t,y+r-t+1,pad,color);rect(d,x+r-t+1,y-r+t,x+r+1,y+r-t+1,pad,color);
        }
        if(c.crossDot||c.crossShape==1)rect(d,x-a,y-a,x+b,y+b,pad,color);
    }
}
