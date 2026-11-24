package dev.rylune.utils.ui;
import dev.rylune.utils.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import java.awt.Color;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public final class ColorWheelScreen extends Screen {
    private static final int SIZE=64;
    private static final Identifier ID=Identifier.of("ryluneutils","color_wheel_cache");
    private static NativeImageBackedTexture wheel;
    private final Screen parent;private final IntConsumer setter;
    private float hue,sat,value;
    private int left,top,wx,wy,barX,barY,drag=0;
    private String hex;
    public ColorWheelScreen(Screen parent,IntSupplier getter,IntConsumer setter){
        super(Text.literal("Color picker"));this.parent=parent;this.setter=setter;
        int rgb=getter.getAsInt();float[] hsv=Color.RGBtoHSB((rgb>>16)&255,(rgb>>8)&255,rgb&255,null);
        hue=hsv[0];sat=hsv[1];value=hsv[2];hex=String.format("#%06X",rgb&0xFFFFFF);
    }
    @Override protected void init(){left=(width-176)/2;top=(height-120)/2;wx=left+10;wy=top+24;barX=wx+72;barY=wy;drag=0;}
    @Override public boolean shouldPause(){return false;}
    @Override public void renderBackground(DrawContext d,int x,int y,float delta){}
    private static void ensureWheel(){
        if(wheel!=null&&wheel.getImage()!=null)return;
        wheel=new NativeImageBackedTexture(()->"Rylune HSV wheel",SIZE,SIZE,true);
        var image=wheel.getImage();
        for(int y=0;y<SIZE;y++)for(int x=0;x<SIZE;x++){
            double dx=(x+.5-SIZE/2.0)/(SIZE/2.0),dy=(y+.5-SIZE/2.0)/(SIZE/2.0),s=Math.hypot(dx,dy);
            float h=(float)((Math.atan2(dy,dx)/(Math.PI*2)+1)%1);
            image.setColorArgb(x,y,s<=1?Color.HSBtoRGB(h,(float)s,1):0);
        }
        MinecraftClient.getInstance().getTextureManager().registerTexture(ID,wheel);wheel.upload();
    }
    private int rgb(){return Color.HSBtoRGB(hue,sat,value)&0xFFFFFF;}
    private void apply(){setter.accept(rgb());Crosshair.invalidate();hex=String.format("#%06X",rgb());}
    private void text(DrawContext d,String s,int x,int y,int col){d.drawText(textRenderer,s,x,y,col,false);}
    private static boolean inside(double x,double y,int bx,int by,int w,int h){return x>=bx&&x<bx+w&&y>=by&&y<by+h;}
    @Override public void render(DrawContext d,int mx,int my,float delta){
        ensureWheel();d.fill(0,0,width,height,0xDA000000);d.fill(left,top,left+176,top+120,0xFF303030);d.fill(left+1,top+1,left+175,top+119,0xFF141414);
        text(d,"Color picker",left+10,top+8,0xFFF5F5F5);
        d.drawTexture(RenderPipelines.GUI_TEXTURED,ID,wx,wy,0f,0f,SIZE,SIZE,SIZE,SIZE,0xFFFFFFFF);
        int sx=wx+32+Math.round((float)Math.cos(hue*Math.PI*2)*sat*30),sy=wy+32+Math.round((float)Math.sin(hue*Math.PI*2)*sat*30);
        d.fill(sx-3,sy-3,sx+4,sy+4,0xFF101010);d.fill(sx-2,sy-2,sx+3,sy+3,0xFFFFFFFF);d.fill(sx-1,sy-1,sx+2,sy+2,0xFF000000|rgb());
        for(int i=0;i<32;i++)d.fill(barX,barY+i*2,barX+10,barY+i*2+2,Color.HSBtoRGB(hue,sat,1-i/31f));
        int by=barY+Math.round((1-value)*63);d.fill(barX-2,by-1,barX+12,by+2,0xFFFFFFFF);
        int px=left+100;d.fill(px,wy,px+66,wy+30,0xFF000000|rgb());text(d,hex,px,wy+36,0xFFF5F5F5);text(d,"Brightness",px,wy+50,0xFFAAAAAA);
        int fy=top+98;d.fill(left+118,fy,left+166,fy+16,inside(mx,my,left+118,fy,48,16)?0xFF404040:0xFF292929);text(d,"Done",left+131,fy+4,0xFFF5F5F5);text(d,"Select hue & brightness",left+10,fy+4,0xFFAAAAAA);
    }
    private void pick(double x,double y){
        if(drag==1){double dx=(x-wx-32)/32,dy=(y-wy-32)/32;hue=(float)((Math.atan2(dy,dx)/(Math.PI*2)+1)%1);sat=(float)Math.min(1,Math.hypot(dx,dy));if(value==0)value=1;}
        else if(drag==2)value=(float)Math.max(0,Math.min(1,1-(y-barY)/63));
        apply();
    }
    @Override public boolean mouseClicked(Click click,boolean doubled){
        if(click.button()==0){double x=click.x(),y=click.y();
            if(Math.hypot(x-wx-32,y-wy-32)<=32){drag=1;pick(x,y);setDragging(true);return true;}
            if(inside(x,y,barX-2,barY,14,64)){drag=2;pick(x,y);setDragging(true);return true;}
            if(inside(x,y,left+118,top+98,48,16)){close();return true;}
        }return super.mouseClicked(click,doubled);
    }
    @Override public boolean mouseDragged(Click click,double dx,double dy){if(drag!=0&&click.button()==0){pick(click.x(),click.y());return true;}return super.mouseDragged(click,dx,dy);}
    @Override public boolean mouseReleased(Click click){if(drag!=0&&click.button()==0){drag=0;setDragging(false);return true;}return super.mouseReleased(click);}
    @Override public void close(){RyluneUtils.config.save();client.setScreen(parent);}
}
