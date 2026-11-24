package dev.rylune.utils.ui;
import dev.rylune.utils.*;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import java.util.Arrays;

public final class CrosshairEditor extends Screen {
    private final Screen parent;
    private final char[] pixels;
    private int cell,gx,gy,left,top,panelW,panelH,last=-1,stroke=-1;
    private boolean erase=false;
    public CrosshairEditor(Screen parent){
        super(Text.literal("Crosshair editor"));this.parent=parent;pixels=RyluneUtils.config.customPixels.toCharArray();
        RyluneUtils.config.crossShape=4;Crosshair.invalidate();
    }
    @Override protected void init(){
        panelW=Math.min(300,width-8);panelH=Math.min(188,height-8);left=(width-panelW)/2;top=(height-panelH)/2;
        cell=Math.max(2,Math.min(8,Math.min((panelH-47)/15,(panelW-96)/15)));
        gx=left+9;gy=top+24;last=-1;stroke=-1;
    }
    @Override public boolean shouldPause(){return false;}
    @Override public void renderBackground(DrawContext d,int x,int y,float delta){}
    private void text(DrawContext d,String s,int x,int y,int color){d.drawText(textRenderer,s,x,y,color,false);}
    private void btn(DrawContext d,int x,int y,int w,String s,int mx,int my,boolean active){
        d.fill(x,y,x+w,y+16,active?0xFFE5E5E5:inside(mx,my,x,y,w,16)?0xFF3A3A3A:0xFF252525);
        text(d,s,x+(w-textRenderer.getWidth(s))/2,y+4,active?0xFF151515:0xFFE5E5E5);
    }
    private static boolean inside(double x,double y,int bx,int by,int w,int h){return x>=bx&&x<bx+w&&y>=by&&y<by+h;}
    private int bx(){return gx+15*cell+9;}
    private int bw(){return left+panelW-9-bx();}
    @Override public void render(DrawContext d,int mx,int my,float delta){
        d.fill(0,0,width,height,0xDA000000);d.fill(left,top,left+panelW,top+panelH,0xFF303030);d.fill(left+1,top+1,left+panelW-1,top+panelH-1,0xFF141414);
        text(d,"Draw your crosshair",left+9,top+8,0xFFF5F5F5);text(d,"15 x 15",left+panelW-49,top+8,0xFFAAAAAA);
        int rgb=0xFF000000|RyluneUtils.config.crossColor;
        d.fill(gx,gy,gx+15*cell,gy+15*cell,0xFF202020);
        for(int row=0;row<15;row++)for(int col=0;col<15;col++)if(pixels[row*15+col]=='1')d.fill(gx+col*cell,gy+row*cell,gx+(col+1)*cell,gy+(row+1)*cell,rgb);
        for(int i=0;i<=15;i++){d.fill(gx+i*cell,gy,gx+i*cell+1,gy+15*cell+1,0xFF383838);d.fill(gx,gy+i*cell,gx+15*cell+1,gy+i*cell+1,0xFF383838);}
        int x=bx(),w=bw();btn(d,x,gy,w,"Draw",mx,my,!erase);btn(d,x,gy+19,w,"Erase",mx,my,erase);btn(d,x,gy+38,w,"Color wheel",mx,my,false);btn(d,x,gy+57,w,"Clear",mx,my,false);
        if(panelH>=155){int cy=gy+93;d.fill(x,cy-12,x+w,cy+12,0xFF242424);Crosshair.draw(d,x+w/2,cy);}
        int fy=top+panelH-20;btn(d,left+panelW-58,fy,49,"Done",mx,my,false);
        if(panelW>=220)text(d,"Drag to paint / right-click erase",left+9,fy+4,0xFFAAAAAA);
    }
    private int index(double x,double y){return inside(x,y,gx,gy,15*cell,15*cell)?((int)(y-gy)/cell)*15+(int)(x-gx)/cell:-1;}
    private void sync(){RyluneUtils.config.customPixels=new String(pixels);RyluneUtils.config.crossShape=4;Crosshair.invalidate();}
    private void paint(int pos,char value){
        if(pos<0){last=-1;return;}
        int x=pos%15,y=pos/15,ox=last<0?x:last%15,oy=last<0?y:last/15;
        int dx=Math.abs(x-ox),dy=-Math.abs(y-oy),sx=ox<x?1:-1,sy=oy<y?1:-1,err=dx+dy;
        boolean changed=false;
        while(true){int p=oy*15+ox;if(pixels[p]!=value){pixels[p]=value;changed=true;}if(ox==x&&oy==y)break;int e=2*err;if(e>=dy){err+=dy;ox+=sx;}if(e<=dx){err+=dx;oy+=sy;}}
        last=pos;if(changed)sync();
    }
    @Override public boolean mouseClicked(Click click,boolean doubled){
        double x=click.x(),y=click.y();int p=index(x,y);
        if(p>=0&&(click.button()==0||click.button()==1)){stroke=click.button();last=-1;paint(p,erase||stroke==1?'0':'1');setDragging(true);return true;}
        if(click.button()==0){
            if(inside(x,y,bx(),gy,bw(),16)){erase=false;return true;}
            if(inside(x,y,bx(),gy+19,bw(),16)){erase=true;return true;}
            if(inside(x,y,bx(),gy+38,bw(),16)){sync();client.setScreen(new ColorWheelScreen(this,()->RyluneUtils.config.crossColor,v->RyluneUtils.config.crossColor=v));return true;}
            if(inside(x,y,bx(),gy+57,bw(),16)){Arrays.fill(pixels,'0');sync();return true;}
            if(inside(x,y,left+panelW-58,top+panelH-20,49,16)){close();return true;}
        }
        return super.mouseClicked(click,doubled);
    }
    @Override public boolean mouseDragged(Click click,double dx,double dy){if(stroke==click.button()){paint(index(click.x(),click.y()),erase||stroke==1?'0':'1');return true;}return super.mouseDragged(click,dx,dy);}
    @Override public boolean mouseReleased(Click click){if(stroke==click.button()){stroke=-1;last=-1;setDragging(false);return true;}return super.mouseReleased(click);}
    @Override public void close(){sync();RyluneUtils.config.save();client.setScreen(parent);}
}
