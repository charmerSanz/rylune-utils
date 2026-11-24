package dev.rylune.utils.ui;

import dev.rylune.utils.*;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import java.util.*;
import java.util.function.*;

public final class ModuleScreen extends Screen {
    private static final int BG=0xFF101010, SIDE=0xFF141414, CARD=0xFF1A1A1A, HOVER=0xFF252525,
        BORDER=0xFF2C2C2C, WHITE=0xFFF5F5F5, MUTED=0xFFA3A3A3, DIM=0xFF777777, ACCENT=0xFFE8E8E8;
    private static final String[] NAMES={"Zoom","Toggle Sprint","Custom Crosshair","Block Overlay"};
    private static final String[] DESC={"See farther with smooth zoom","Sprint without holding a key","Make the reticle your own","Highlight the block you target"};
    private static final String[] TYPE={"VISUAL","MOVEMENT","HUD","VISUAL"};
    private static final String[] FILTERS={"All","Visual","Movement","HUD","Enabled"};
    private final List<Hit> hits=new ArrayList<>();
    private final float[] hoverState=new float[4], switchState=new float[4];
    private record Hit(int x,int y,int w,int h,Runnable action) {boolean contains(double mx,double my){return mx>=x&&mx<x+w&&my>=y&&my<y+h;}}
    private record Setting(String label,Supplier<String> text,Runnable minus,Runnable plus,IntSupplier color) {}
    private TextFieldWidget search,hex;
    private int px,py,pw,ph,selected=-1,filter=0,page=0,side,contentX,contentW,searchX,searchW;
    private String query="";
    private long opened=System.nanoTime(),lastFrame=opened;
    private float frame=0.15f;
    private Config c(){return RyluneUtils.config;}
    public ModuleScreen(){super(Text.literal("Rylune Utils"));for(int i=0;i<4;i++)switchState[i]=c().enabled(i)?1:0;}
    @Override protected void init(){
        pw=Math.min(660,width-12);ph=Math.min(390,height-12);px=(width-pw)/2;py=(height-ph)/2;
        side=pw>=390&&ph>=183?104:0;contentX=px+side+12;contentW=pw-side-24;
        clearChildren();search=null;hex=null;
        if(selected<0){
            searchW=Math.min(148,Math.max(84,contentW/2));searchX=px+pw-searchW-38;
            search=new TextFieldWidget(textRenderer,searchX+19,py+15,searchW-25,12,Text.literal("Search modules"));
            search.setDrawsBackground(false);search.setTextShadow(false);search.setEditableColor(WHITE);search.setMaxLength(60);search.setText(query);search.setPlaceholder(Text.literal("Search..."));
            search.setChangedListener(s->{query=s;page=0;});addDrawableChild(search);
        }else if(selected!=0){
            hex=new TextFieldWidget(textRenderer,px+pw-76,py+ph-21,62,12,Text.literal("RGB hex color"));
            hex.setDrawsBackground(false);hex.setTextShadow(false);hex.setEditableColor(WHITE);hex.setMaxLength(7);hex.setText(String.format("#%06X",getColor()));
            hex.setChangedListener(s->{if(s.matches("#?[0-9a-fA-F]{6}")){setColor(Integer.parseInt(s.replace("#",""),16));c().save();}});addDrawableChild(hex);
        }
    }
    private void change(int id){selected=id;page=0;init();}
    private void toggle(int id){c().toggle(id);if(id==1&&!c().sprint)RyluneUtils.sprintActive=false;if(id==0&&!c().zoom)RyluneUtils.zoomLatched=false;c().save();}
    @Override public boolean shouldPause(){return false;}
    @Override public void renderBackground(DrawContext d,int x,int y,float delta){}
    private void text(DrawContext d,String s,int x,int y,int color){d.drawText(textRenderer,s,x,y,color,false);}
    private void center(DrawContext d,String s,int x,int y,int color){text(d,s,x-textRenderer.getWidth(s)/2,y,color);}
    private void border(DrawContext d,int x,int y,int w,int h,int col){if(w<1||h<1)return;d.fill(x,y,x+w,y+1,col);d.fill(x,y+h-1,x+w,y+h,col);d.fill(x,y,x+1,y+h,col);d.fill(x+w-1,y,x+w,y+h,col);}
    private void round(DrawContext d,int x,int y,int w,int h,int r,int color){
        if(w<=0||h<=0)return;r=Math.max(0,Math.min(r,Math.min(w,h)/2));
        d.fill(x,y+r,x+w,y+h-r,color);
        for(int i=0;i<r;i++){double dy=r-i-.5;int inset=(int)Math.ceil(r-Math.sqrt(r*r-dy*dy));d.fill(x+inset,y+i,x+w-inset,y+i+1,color);d.fill(x+inset,y+h-i-1,x+w-inset,y+h-i,color);}
    }
    private void panel(DrawContext d,int x,int y,int w,int h,int r,int fill,int stroke){round(d,x,y,w,h,r,stroke);round(d,x+1,y+1,w-2,h-2,Math.max(0,r-1),fill);}
    private boolean hover(int x,int y,int w,int h,int mx,int my){return mx>=x&&mx<x+w&&my>=y&&my<y+h;}
    private int mix(int a,int b,float t){int out=0;for(int s=0;s<=24;s+=8)out|=Math.round(((a>>>s)&255)*(1-t)+((b>>>s)&255)*t)<<s;return out;}
    private void button(DrawContext d,int x,int y,int w,int h,String label,int mx,int my,boolean active,Runnable r){
        boolean over=hover(x,y,w,h,mx,my);round(d,x,y,w,h,4,active?ACCENT:over?HOVER:CARD);
        center(d,label,x+w/2,y+(h-8)/2,active?BG:over?WHITE:MUTED);hits.add(new Hit(x,y,w,h,r));
    }
    private void switchControl(DrawContext d,int id,int x,int y,int mx,int my){
        float t=switchState[id];round(d,x,y,28,14,7,mix(0xFF414141,ACCENT,t));round(d,x+2+Math.round(t*14),y+2,10,10,5,mix(MUTED,BG,t));
        hits.add(new Hit(x-3,y-4,34,22,()->toggle(id)));
    }
    private void logo(DrawContext d,int x,int y){round(d,x,y,20,20,6,WHITE);round(d,x+4,y+3,13,14,6,BG);round(d,x+9,y+1,11,12,5,WHITE);}
    private void searchIcon(DrawContext d,int x,int y){border(d,x,y,8,8,MUTED);d.fill(x+7,y+7,x+9,y+9,MUTED);d.fill(x+8,y+8,x+10,y+10,MUTED);}
    private void gear(DrawContext d,int x,int y,int col){border(d,x-4,y-4,9,9,col);d.fill(x-1,y-6,x+2,y-4,col);d.fill(x-1,y+5,x+2,y+7,col);d.fill(x-6,y-1,x-4,y+2,col);d.fill(x+5,y-1,x+7,y+2,col);d.fill(x-1,y-1,x+2,y+2,col);}
    @Override public void render(DrawContext d,int mx,int my,float delta){
        long now=System.nanoTime();frame=(float)(1-Math.exp(-Math.min(.05,(now-lastFrame)/1e9)*20));lastFrame=now;
        hits.clear();for(int i=0;i<4;i++){float target=c().enabled(i)?1:0;switchState[i]=Math.abs(target-switchState[i])<.01f?target:switchState[i]+(target-switchState[i])*frame;}
        // Draw the dimmer directly: never invokes a shader or blur pass.
        d.fill(0,0,width,height,0xDA000000);
        round(d,px-3,py-2,pw+6,ph+7,12,0x70000000);panel(d,px,py,pw,ph,9,BG,BORDER);
        if(side>0){round(d,px+1,py+1,side-1,ph-2,8,SIDE);d.fill(px+side-1,py+12,px+side,py+ph-12,BORDER);renderSidebar(d,mx,my);}else if(selected<0){logo(d,px+9,py+10);text(d,"RYLUNE",px+34,py+16,WHITE);}
        if(selected<0)renderModules(d,mx,my);else renderSettings(d,mx,my);
        button(d,px+pw-28,py+10,18,20,"X",mx,my,false,this::close);
        super.render(d,mx,my,delta);
    }
    private void renderSidebar(DrawContext d,int mx,int my){
        logo(d,px+12,py+12);text(d,"RYLUNE",px+38,py+14,WHITE);text(d,"UTILS",px+38,py+25,DIM);
        text(d,"MODULES",px+12,py+48,DIM);
        for(int i=0;i<FILTERS.length;i++){
            final int f=i;int x=px+8,y=py+62+i*23,w=side-16;boolean active=filter==i&&selected<0;
            if(active||hover(x,y,w,21,mx,my))round(d,x,y,w,21,4,active?0xFF303030:HOVER);
            if(active)round(d,x,y+4,2,13,1,WHITE);
            if(i==0){for(int k=0;k<4;k++)round(d,x+10+(k%2)*5,y+6+(k/2)*5,4,4,1,active?WHITE:DIM);}else if(i==4){round(d,x+10,y+7,9,7,3,active?WHITE:DIM);}else icon(d,i==1?0:i==2?1:2,x+14,y+11,active?WHITE:DIM,4);
            text(d,FILTERS[i],x+25,y+7,active?WHITE:MUTED);hits.add(new Hit(x,y,w,21,()->{filter=f;change(-1);}));
        }
        if(ph>=220){d.fill(px+12,py+ph-40,px+side-12,py+ph-39,BORDER);text(d,"RYLUNE UTILS",px+12,py+ph-29,DIM);text(d,"v1.2.0",px+12,py+ph-17,MUTED);}
    }
    private void renderModules(DrawContext d,int mx,int my){
        int x=contentX; if(side>0)text(d,"Modules",x,py+16,WHITE);
        panel(d,searchX,py+10,searchW,20,5,SIDE,search!=null&&search.isFocused()?DIM:BORDER);searchIcon(d,searchX+6,py+15);
        d.fill(contentX,py+39,contentX+contentW,py+40,BORDER);
        int top=py+49;
        if(side==0){int gap=3,tab=(contentW-gap*4)/5;for(int i=0;i<5;i++){final int f=i;button(d,x+i*(tab+gap),top,tab,18,FILTERS[i],mx,my,filter==i,()->{filter=f;page=0;});}top+=24;}
        var ids=new ArrayList<Integer>();String q=query.toLowerCase(Locale.ROOT).trim();int active=0;
        for(int i=0;i<4;i++){if(c().enabled(i))active++;if((NAMES[i]+" "+DESC[i]+" "+TYPE[i]).toLowerCase(Locale.ROOT).contains(q)&&(filter==0||filter==1&&TYPE[i].equals("VISUAL")||filter==2&&TYPE[i].equals("MOVEMENT")||filter==3&&TYPE[i].equals("HUD")||filter==4&&c().enabled(i)))ids.add(i);}
        text(d,FILTERS[filter].equals("All")?"All modules":FILTERS[filter]+" modules",x,top,WHITE);
        String count=active+" enabled";text(d,count,x+contentW-textRenderer.getWidth(count),top,MUTED);top+=17;
        int cols=contentW>=230?2:1,gap=7,cardW=(contentW-(cols-1)*gap)/cols;
        int usable=Math.max(1,py+ph-28-top),rows=usable>=129?2:1,cardH=Math.min(104,Math.max(46,(usable-(rows-1)*gap)/rows)),per=rows*cols,maxPage=Math.max(0,(ids.size()-1)/per);page=Math.min(page,maxPage);
        for(int j=page*per;j<Math.min(ids.size(),(page+1)*per);j++){int k=j-page*per;renderCard(d,ids.get(j),x+(k%cols)*(cardW+gap),top+(k/cols)*(cardH+gap),cardW,cardH,mx,my);}
        if(ids.isEmpty()){center(d,"No modules found",x+contentW/2,top+22,WHITE);center(d,"Try a different search",x+contentW/2,top+36,MUTED);}
        int foot=py+ph-23;d.fill(x,foot-4,x+contentW,foot-3,BORDER);text(d,ids.size()+" modules",x,foot+6,MUTED);
        if(maxPage>0){button(d,x+contentW-43,foot,19,18,"<",mx,my,false,()->page=Math.max(0,page-1));button(d,x+contentW-21,foot,19,18,">",mx,my,false,()->page=Math.min(maxPage,page+1));}else{String hint="RIGHT SHIFT";text(d,hint,x+contentW-textRenderer.getWidth(hint),foot+6,DIM);}
    }
    private void renderCard(DrawContext d,int id,int x,int y,int w,int h,int mx,int my){
        boolean enabled=c().enabled(id),over=hover(x,y,w,h,mx,my);hoverState[id]+=((over?1:0)-hoverState[id])*frame;
        panel(d,x,y,w,h,7,mix(CARD,HOVER,hoverState[id]),enabled?0xFF525252:BORDER);
        if(h>=84){round(d,x+9,y+9,29,29,6,0xFF303030);icon(d,id,x+23,y+23,WHITE,8);text(d,textRenderer.trimToWidth(NAMES[id],w-18),x+9,y+43,WHITE);if(h>=94)text(d,textRenderer.trimToWidth(DESC[id],w-18),x+9,y+56,MUTED);}
        else{int iconSize=h>=58?22:18;round(d,x+7,y+4,iconSize,iconSize,5,0xFF303030);icon(d,id,x+7+iconSize/2,y+4+iconSize/2,WHITE,6);text(d,textRenderer.trimToWidth(NAMES[id],w-39),x+34,y+8,WHITE);if(h>=58)text(d,TYPE[id],x+34,y+20,DIM); }
        int fy=y+h-23;d.fill(x+8,fy-1,x+w-8,fy,BORDER);switchControl(d,id,x+9,fy+5,mx,my);
        text(d,enabled?"On":"Off",x+43,fy+8,enabled?WHITE:DIM);
        int gx=x+w-23;round(d,gx-5,fy+2,20,20,4,hover(gx-5,fy+2,20,20,mx,my)?0xFF414141:0xFF252525);gear(d,gx+5,fy+11,MUTED);hits.add(new Hit(gx-5,fy+2,20,20,()->change(id)));
        if(h>=84&&w>=115){String type=TYPE[id];text(d,type,x+w-8-textRenderer.getWidth(type),y+11,DIM);}
    }
    private void icon(DrawContext d,int id,int x,int y,int col,int r){
        switch(id){case 0->{border(d,x-r,y-r,2*r-3,2*r-3,col);for(int i=0;i<4;i++)d.fill(x+r-4+i,y+r-4+i,x+r-2+i,y+r-2+i,col);}case 1->{for(int i=0;i<3;i++){int a=(i==1?2:0);d.fill(x-r+a,y-r+i*5,x+r-2+a,y-r+i*5+2,col);}}case 2->{d.fill(x-r,y,x-2,y+1,col);d.fill(x+3,y,x+r+1,y+1,col);d.fill(x,y-r,x+1,y-2,col);d.fill(x,y+3,x+1,y+r+1,col);d.fill(x,y,x+1,y+1,col);}case 3->{border(d,x-r+1,y-r+1,2*r-1,2*r-1,col);d.fill(x-r+4,y-r+4,x-r+6,y-r+6,col);}}
    }
    private Setting number(String name,IntSupplier get,IntConsumer set,int min,int max,int step,String suffix){return new Setting(name,()->get.getAsInt()+suffix,()->set.accept(Config.clamp(get.getAsInt()-step,min,max)),()->set.accept(Config.clamp(get.getAsInt()+step,min,max)),null);}
    private Setting bool(String name,BooleanSupplier get,Consumer<Boolean> set){Runnable t=()->set.accept(!get.getAsBoolean());return new Setting(name,()->get.getAsBoolean()?"On":"Off",t,t,null);}
    private int getColor(){return switch(selected){case 1->c().sprintColor;case 2->c().crossColor;default->c().blockColor;};}
    private void setColor(int rgb){switch(selected){case 1->c().sprintColor=rgb;case 2->c().crossColor=rgb;case 3->c().blockColor=rgb;}}
    private void updateChannel(int shift,int change){int rgb=getColor(),v=Config.clamp(((rgb>>shift)&255)+change,0,255);setColor((rgb&~(255<<shift))|(v<<shift));if(hex!=null)hex.setText(String.format("#%06X",getColor()));}
    private List<Setting> settings(){var s=new ArrayList<Setting>();Config c=c();switch(selected){
        case 0->{s.add(number("Magnification",()->c.zoomFactor,v->c.zoomFactor=v,2,12,1,"x"));s.add(bool("Toggle mode",()->c.zoomToggle,v->{c.zoomToggle=v;RyluneUtils.zoomLatched=false;}));s.add(bool("Smooth transition",()->c.zoomSmooth,v->c.zoomSmooth=v));s.add(number("Transition speed",()->c.zoomSpeed,v->c.zoomSpeed=v,3,24,1,""));}
        case 1->{s.add(bool("Show sprint status",()->c.sprintHud,v->c.sprintHud=v));s.add(bool("Reset after death / world",()->c.sprintReset,v->c.sprintReset=v));s.add(number("HUD X",()->c.sprintX,v->c.sprintX=v,0,200,4,""));s.add(number("HUD Y",()->c.sprintY,v->c.sprintY=v,0,200,4,""));}
        case 2->{String[] shapes={"Plus","Dot","T-shape","Square","Drawn"};s.add(new Setting("Shape",()->shapes[c.crossShape],()->c.crossShape=(c.crossShape+4)%5,()->c.crossShape=(c.crossShape+1)%5,null));s.add(number("Length",()->c.crossLength,v->c.crossLength=v,1,16,1,""));s.add(number("Gap",()->c.crossGap,v->c.crossGap=v,0,12,1,""));s.add(number("Thickness",()->c.crossThickness,v->c.crossThickness=v,1,5,1,""));s.add(number("Opacity",()->c.crossOpacity,v->c.crossOpacity=v,10,100,5,"%"));s.add(bool("Center dot",()->c.crossDot,v->c.crossDot=v));s.add(bool("Black outline",()->c.crossOutline,v->c.crossOutline=v));}
        case 3->{s.add(number("Outline width",()->c.blockWidth,v->c.blockWidth=v,1,6,1,""));s.add(number("Outline opacity",()->c.blockOpacity,v->c.blockOpacity=v,10,100,5,"%"));s.add(number("Fill opacity",()->c.blockFillOpacity,v->c.blockFillOpacity=v,0,70,5,"%"));}
    }if(selected==2){s.add(0,new Setting("Draw your crosshair",()->"Edit",()->client.setScreen(new CrosshairEditor(this)),()->client.setScreen(new CrosshairEditor(this)),null));}
        if(selected==2||selected==3){s.add(selected==2?1:0,new Setting("Color wheel",()->"Open",()->client.setScreen(new ColorWheelScreen(this,()->getColor(),v->setColor(v))),()->client.setScreen(new ColorWheelScreen(this,()->getColor(),v->setColor(v))),null));}
        if(selected!=0){String[] labels={"Red","Green","Blue"};for(int i=0;i<3;i++){final int shift=(2-i)*8;s.add(new Setting(labels[i],()->Integer.toString((getColor()>>shift)&255),()->updateChannel(shift,-5),()->updateChannel(shift,5),()->0xFF000000|getColor()));}}return s;}
    private void renderSettings(DrawContext d,int mx,int my){
        int x=contentX,w=contentW;button(d,x,py+10,23,20,"<",mx,my,false,()->change(-1));text(d,textRenderer.trimToWidth(NAMES[selected],w-62),x+30,py+16,WHITE);
        d.fill(x,py+39,x+w,py+40,BORDER);
        int top=py+48; text(d,"Module settings",x,top+4,MUTED);switchControl(d,selected,x+w-29,top,mx,my);top+=23;
        int available=Math.max(1,py+ph-34-top),preview=w>=250?102:0,listW=w-preview-(preview>0?9:0),rows=Math.max(1,available/24);
        var options=settings();int maxPage=Math.max(0,(options.size()-1)/rows);page=Math.min(maxPage,page);
        for(int i=page*rows;i<Math.min(options.size(),(page+1)*rows);i++){
            var a=options.get(i);int y=top+(i-page*rows)*24;round(d,x,y,listW,22,4,CARD);String value=a.text.get();
            text(d,textRenderer.trimToWidth(a.label,Math.max(15,listW-93)),x+7,y+7,WHITE);
            if(value.equals("Edit")||value.equals("Open")){button(d,x+listW-47,y+3,42,16,value,mx,my,false,a.plus);}
            else if(value.equals("On")||value.equals("Off")){boolean on=value.equals("On");int sx=x+listW-35;round(d,sx,y+4,28,14,7,on?ACCENT:0xFF414141);round(d,sx+(on?16:2),y+6,10,10,5,on?BG:MUTED);hits.add(new Hit(sx-2,y,32,22,()->{a.plus.run();c().save();}));}
            else{if(a.color!=null&&listW>=160)round(d,x+listW-87,y+7,8,8,2,a.color.getAsInt());button(d,x+listW-75,y+3,16,16,"-",mx,my,false,()->{a.minus.run();c().save();});center(d,value,x+listW-38,y+7,WHITE);button(d,x+listW-18,y+3,16,16,"+",mx,my,false,()->{a.plus.run();c().save();});}
        }
        if(preview>0){int bx=x+listW+9,bh=available;panel(d,bx,top,preview,bh,6,SIDE,BORDER);center(d,"LIVE PREVIEW",bx+preview/2,top+9,DIM);int cx=bx+preview/2,cy=top+bh/2+6;
            if(bh>=55){if(selected==2)Crosshair.draw(d,cx,cy);else if(selected==3){int color=RyluneUtils.argb(c().blockColor,c().blockOpacity);d.fill(cx-16,cy-16,cx+16,cy+16,RyluneUtils.argb(c().blockColor,c().blockFillOpacity));for(int i=0;i<c().blockWidth;i++)border(d,cx-16+i,cy-16+i,32-2*i,32-2*i,color);}else if(selected==1)center(d,"Sprint: ready",cx,cy,0xFF000000|c().sprintColor);else{icon(d,0,cx,cy-8,WHITE,8);center(d,c().zoomFactor+"x",cx,cy+8,WHITE);}}}
        int y=py+ph-27;d.fill(x,y-3,x+w,y-2,BORDER);button(d,x,y,18,20,"<",mx,my,false,()->page=Math.max(0,page-1));center(d,(page+1)+"/"+(maxPage+1),x+34,y+6,MUTED);button(d,x+50,y,18,20,">",mx,my,false,()->page=Math.min(maxPage,page+1));
        button(d,x+73,y,39,20,"Reset",mx,my,false,()->{reset();init();});
        if(selected!=0){int hx=px+pw-82;panel(d,hx,y,70,20,4,SIDE,BORDER);if(w>=235){round(d,hx-20,y+4,12,12,3,0xFF000000|getColor());text(d,"HEX",hx-43,y+6,DIM);}}else if(w>=235){String hint="Change keys in Controls";text(d,hint,x+w-textRenderer.getWidth(hint),y+6,DIM);}
    }
    private void reset(){Config n=new Config(),c=c();switch(selected){case 0->{c.zoomFactor=n.zoomFactor;c.zoomSpeed=n.zoomSpeed;c.zoomSmooth=n.zoomSmooth;c.zoomToggle=n.zoomToggle;RyluneUtils.zoomLatched=false;}case 1->{c.sprintHud=n.sprintHud;c.sprintReset=n.sprintReset;c.sprintX=n.sprintX;c.sprintY=n.sprintY;c.sprintColor=n.sprintColor;}case 2->{c.crossColor=n.crossColor;c.crossOpacity=n.crossOpacity;c.crossLength=n.crossLength;c.crossGap=n.crossGap;c.crossThickness=n.crossThickness;c.crossShape=n.crossShape;c.crossDot=n.crossDot;c.crossOutline=n.crossOutline;c.customPixels=n.customPixels;}case 3->{c.blockColor=n.blockColor;c.blockOpacity=n.blockOpacity;c.blockWidth=n.blockWidth;c.blockFillOpacity=n.blockFillOpacity;}}c.save();}
    @Override public boolean mouseClicked(Click click,boolean doubled){if(click.button()==0){for(Hit h:List.copyOf(hits))if(h.contains(click.x(),click.y())){h.action.run();return true;}}return super.mouseClicked(click,doubled);}
    @Override public boolean keyPressed(KeyInput input){if(input.key()==GLFW.GLFW_KEY_RIGHT_SHIFT){close();return true;}if(input.key()==GLFW.GLFW_KEY_ESCAPE&&selected>=0){change(-1);return true;}return super.keyPressed(input);}
    @Override public void close(){c().save();super.close();}
}
