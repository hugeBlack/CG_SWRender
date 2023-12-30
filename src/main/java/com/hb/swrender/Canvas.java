package com.hb.swrender;

import com.hb.swrender.objects.RenderableObject;
import com.hb.swrender.shaders.VertexShader;
import com.hb.swrender.shaders.VertexShaderResult;
import com.hb.swrender.utils.QueryTable;
import org.ejml.data.FMatrix4;
import com.hb.swrender.shaders.VertexBuffer;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

public class Canvas extends JPanel {
    public int screenWidth;
    public int screenHeight;
    public int screenWidthHalf;
    public int screenHeightHalf;
    public int screenSize;
    
    public float[] depthBuffer;
    public int[] frameBuffer;
    public RenderableObject[] frameObjectBuffer;
    public int triangleCount;
    private int frameIndex;

    //cpu睡眠时间，数字越小说明运算效率越高
    private int sleepTime;
    //刷新率，及计算刷新率所用到一些辅助参数
    private int framePerSecond;
    private long lastDraw;
    private double lastTime;
    public int frameInterval;
    private BufferedImage screenBuffer;
    public LinkedList<RenderableObject> objects;
    private boolean stopped = false;

    public Canvas(int screenWidth, int screenHeight, int frameInterval){
        this.frameInterval = frameInterval;
        this.screenHeight = screenHeight;
        this.screenHeightHalf = screenHeight / 2;
        this.screenWidth = screenWidth;
        this.screenWidthHalf = screenWidth;
        this.screenSize = screenWidth * screenHeight;
        depthBuffer = new float[screenSize];
        frameBuffer = new int[screenSize];
        frameObjectBuffer = new RenderableObject[screenSize];
        screenBuffer = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_RGB);;
        this.objects = new LinkedList<>();
        setSize(screenWidth, screenHeight);

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();
                int ind = y*screenWidth + x;
                RenderableObject obj =  frameObjectBuffer[ind];
                if(obj == null)
                    return;
                obj.onClick();
            }
        });

    }

    public void startPaint(){
        setVisible(true);
        QueryTable.init();
        Camera.init(0,0,5);

        VertexShaderResult[] vertexShaderResults = new VertexShaderResult[3];

        Rasterizer r = new Rasterizer(640, 480, frameBuffer, frameObjectBuffer);

        while(true) {
            if(stopped)
                return;

            long renderStartTime = System.currentTimeMillis();
            triangleCount = 0;
            //更新视角
            Camera.update();

            r.clearBuffer();

            // 设置背景色
            frameBuffer[0] = (50 << 16) | (50 << 8) | 50;
            for(int i = 1; i < screenSize; i+=i)
                System.arraycopy(frameBuffer, 0, frameBuffer, i, screenSize - i >= i ? i : screenSize - i);
            frameObjectBuffer[0] = null;
            for(int i = 1; i < screenSize; i+=i)
                System.arraycopy(frameObjectBuffer, 0, frameObjectBuffer, i, screenSize - i >= i ? i : screenSize - i);

            // 流程：1. 从RenderableObjects中去取出其VAO，按照三个一组获取其VBO中对应的顶点，调用其所需的着色器，
            // 然后将着色后的三个顶点与其对应的像素着色器丢给Rasterizer进行光栅化
            for(RenderableObject object : objects){
                VertexBuffer[] thisVBO = object.getMyVBO();
                VertexShaderResult[] vsResultBuffer = new VertexShaderResult[thisVBO.length];
                int[] thisVAO = object.getMyVAO();
                if(thisVAO.length % 3 != 0)
                    throw new RuntimeException("Length of VAO should be a multiple of 3!");
                int triangleCountNow = thisVAO.length / 3;
                this.triangleCount += triangleCountNow;
                for(int i = 0; i < triangleCountNow; ++i){
                    int offset = i *3;
                    // 把vs的结果缓存下来
                    for(int j = 0; j < 3; ++j){
                        if(vsResultBuffer[thisVAO[offset + j]] == null){
                            VertexShader vs = object.getVertexShader(thisVAO[offset + j]);
                            FMatrix4 ans = vs.run(thisVBO[thisVAO[offset + j]].shaderParams);
                            vsResultBuffer[thisVAO[offset + j]] = new VertexShaderResult(ans, vs.result);
                        }
                    }
                    vertexShaderResults[0] = vsResultBuffer[thisVAO[offset]];
                    vertexShaderResults[1] = vsResultBuffer[thisVAO[offset + 1]];
                    vertexShaderResults[2] = vsResultBuffer[thisVAO[offset + 2]];

                    r.rasterize(object, vertexShaderResults, object.getFragmentShader(i));
                }
            }



            frameIndex++;
            long renderEndTime = System.currentTimeMillis();
            //尽量让刷新率保持恒定。
            int mySleepTime = 0;
            while(System.currentTimeMillis()-lastDraw<frameInterval){
                try {
                    Thread.sleep(1);
                    mySleepTime++;
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
            sleepTime+=mySleepTime;

            //显示当前刷新率
            Graphics2D g2 =(Graphics2D)screenBuffer.getGraphics();
            System.arraycopy(frameBuffer, 0, ((DataBufferInt)screenBuffer.getRaster().getDataBuffer()).getData(), 0, screenSize);
            lastDraw=System.currentTimeMillis();

            g2.setColor(Color.WHITE);
            g2.drawString("FPS: " + framePerSecond + "      "  +  "MSPF: " + (renderEndTime - renderStartTime) +  "       " + "三角形总数： " + triangleCount, 5, 15);
            repaint();

            //计算当前的刷新率
            if(frameIndex%30==0){
                double thisTime = System.currentTimeMillis();
                framePerSecond = (int)(1000/((thisTime - lastTime)/30));
                lastTime = thisTime;
                sleepTime = 0;
            }
        }
    }

    @Override
    public void paint(Graphics g) {
        g.drawImage(screenBuffer, 0,0, null);
    }

    public void screenShot(){
        File outputfile = new File("Z:/image.png");
        try {
            ImageIO.write(screenBuffer, "png", outputfile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void stopRender() {
        this.stopped = true;
    }
}
