package com.hb.swrender;

import com.hb.swrender.objects.RenderableObject;
import com.hb.swrender.shaders.FragmentShader;
import com.hb.swrender.shaders.VertexShaderResult;
import com.hb.swrender.utils.MatrixHelper;
import org.ejml.data.FMatrix;
import org.ejml.data.FMatrix3;
import org.ejml.data.FMatrix4;
import org.ejml.data.FMatrix4x4;
import org.ejml.dense.fixed.CommonOps_FDF3;
import com.hb.swrender.utils.QueryTable;
import com.hb.swrender.utils.Vector3dHelper;

import java.util.ArrayList;
import java.util.List;

// 任务：把三角形三个点贴到屏幕上，执行顶点着色器，得到裁切平面的坐标，进行裁切，然后光栅化，调用片元着色器进行着色
public class Rasterizer {
    public int screenWidth;
    public int screenHeight;
    public int screenSize;

    //屏幕的像素组
    public int[] screen;

    //屏幕的深度缓冲
    public float[] zBuffer;
    public int[] msaaBuf;

    //变换后的三角形顶点
    public FMatrix3[] updatedVertices;
    VertexShaderResult[] vertexShaderResults;


    //三角形变换后的顶点投影在屏幕上的2D坐标
    public float[][] vertices2D = new float[4][2];


    //用于记录三角形顶点的深度值
    public float[] vertexDepth = new float[4];

    //三角形的最高和最低, 最左和最右的位置
    public float leftMostPosition, rightMostPosition, upperMostPosition, lowerMostPosition;

    //Z裁剪平面离视角原点的距离
    public float zNear = 0.1f;
    public float zFar;

    //三角形所在对象本身坐标系进行的平移变换
    public FMatrix3 localTranslation;

    //辅助渲染计算的矢量类变量
    public FMatrix3 surfaceNormal, edge1, edge2, tempVector1, clippedVertices[];

    //判断三角形是否与屏幕的左边和右边相切
    public boolean isClippingRightOrLeft;

    public static FMatrix4x4 projectionMatrix;
    public static float[][] msaaOffsets = {
        {0.25f, 0.25f},
        {0.75f, 0.25f},
        {0.25f, 0.75f},
        {0.75f, 0.75f}
    };
    private FragmentShader nowShader;

    private RenderableObject[] frameObjectBuffer;
    private RenderableObject currentObject;

    public Rasterizer(int screenWidth, int screenHeight, int[] screen, RenderableObject[] frameObjectBuffer){
        this.frameObjectBuffer = frameObjectBuffer;
        this.screenHeight = screenHeight;
        this.screenWidth = screenWidth;
        this.screenSize = screenHeight * screenWidth;

        this.screen = screen;
        this.zBuffer = new float[screenSize * 4];
        this.msaaBuf = new int[screenSize * 4];
        this.vertexShaderResults = new VertexShaderResult[3];

        //初始化三角形变换后的顶点
        updatedVertices = new FMatrix3[]{
                new FMatrix3(0,0,0),
                new FMatrix3(0,0,0),
                new FMatrix3(0,0,0),
        };

        //初始本身坐标系进行的平移变换
        localTranslation = new FMatrix3(0,0,0);

        //初始化辅助渲染的临时定义的矢量变量
        surfaceNormal = new FMatrix3(0,0,0);
        edge1 = new FMatrix3(0,0,0);
        edge2 = new FMatrix3(0,0,0);
        tempVector1 = new FMatrix3(0,0,0);
        clippedVertices = new FMatrix3[]{
                new FMatrix3(0,0,0),
                new FMatrix3(0,0,0),
                new FMatrix3(0,0,0),
                new FMatrix3(0,0,0)
        };
        zFar = 50;
        projectionMatrix = getProjectionMatrix(45);
    }

    //光栅渲染器的入口
    public void rasterize(RenderableObject currentObject, VertexShaderResult[] vertexShaderResults, FragmentShader fs){
        nowShader = fs;
        float f1 = (zFar - zNear) / 2.0f;
        float f2 = (zFar + zNear) / 2.0f;
        this.currentObject = currentObject;

        // 变换到NDC之前先进行裁切
        ArrayList<VertexShaderResult> clippedVertices = new ArrayList<>();
        clippedVertices.add(vertexShaderResults[0]);
        clippedVertices.add(vertexShaderResults[1]);
        clippedVertices.add(vertexShaderResults[2]);
        // 三个一组进行光栅化
        ClipUtils.SutherlandHodgeman(clippedVertices);
        int l = clippedVertices.size() - 3 + 1;

        for(int id = 0; id < l; ++id){
            this.vertexShaderResults[0] = clippedVertices.get(0);
            this.vertexShaderResults[1] = clippedVertices.get(id + 1);
            this.vertexShaderResults[2] = clippedVertices.get(id + 2);


            for(int i = 0; i < 3; ++i){
                // 先变换到NDC
                float w = this.vertexShaderResults[i].computedPos.a4;
//                updatedVertices[i].setTo(updatedPos[i].a1 / w,updatedPos[i].a2 / w, updatedPos[i].a3 / w);
                updatedVertices[i].setTo(this.vertexShaderResults[i].computedPos.a1 / w,this.vertexShaderResults[i].computedPos.a2 / w, this.vertexShaderResults[i].computedPos.a3 / w);
                // vertexDepth[i] = 1.0f / updatedPos[i].a4;
            }

            // 变换到视口
            for(int i = 0; i < 3; ++i){
                updatedVertices[i].setTo(
                        0.5f*screenWidth*(updatedVertices[i].a1+1.0f),
                        0.5f*screenHeight*(updatedVertices[i].a2+1.0f),
                        -updatedVertices[i].a3 * f1 + f2);
            }


            //测试三角形是否该被渲染出来
            if(testHidden() == true)
                return;


            // 光栅化&着色
            scanTriangle();

        }



    }


    //测试隐藏面
    public boolean testHidden() {
        //测试 1: 如果三角形的顶点全部在Z裁剪平面后面，则这个三角形可视为隐藏面
        boolean allBehindClippingPlane = true;
        for(int i = 0; i < 3; i++) {
            if(updatedVertices[i].a3 >= zNear) {
                allBehindClippingPlane = false;
                break;
            }else{
                System.out.println(1);
            }
        }
        if(allBehindClippingPlane)
            return true;

        //测试 3: 判断三角形是否在屏幕外
        leftMostPosition = screenWidth;
        rightMostPosition = -1;
        upperMostPosition = screenHeight;
        lowerMostPosition = -1;
        for(int i = 0; i < 3; i++) {
            //计算这个三角形的最左边和最右边
            if(vertices2D[i][0] <= leftMostPosition)
                leftMostPosition = vertices2D[i][0];
            if(vertices2D[i][0] >= rightMostPosition)
                rightMostPosition = vertices2D[i][0];

            //计算这个三角形的最上边和最下边
            if(vertices2D[i][1] <= upperMostPosition)
                upperMostPosition = (int)vertices2D[i][1];
            if(vertices2D[i][1] >= lowerMostPosition)
                lowerMostPosition = (int)vertices2D[i][1];
        }

        //如果这个三角形的最左边或最右或最上或最下都没有被重新赋值，那么这个三角形肯定在屏幕范围之外，所以不对其进行渲染。
        if(leftMostPosition == screenWidth ||  rightMostPosition == -1 || upperMostPosition == screenHeight || lowerMostPosition == -1) {
            return true;
        }

        //判断三角形是否和屏幕的左边和右边相切
        isClippingRightOrLeft = leftMostPosition < 0 || rightMostPosition >= screenWidth;

        return false;
    }

    //将三角形转换为扫描线
    public void scanTriangle() {
        int min_x,max_x,min_y,max_y;
        var v = this.updatedVertices;
        min_x = (int) Math.min(Math.min(v[0].a1, v[1].a1), v[2].a1);
        max_x = (int) Math.max(Math.max(v[0].a1, v[1].a1), v[2].a1);
        min_y = (int) Math.min(Math.min(v[0].a2, v[1].a2), v[2].a2);
        max_y = (int) Math.max(Math.max(v[0].a2, v[1].a2), v[2].a2);
        min_x = MatrixHelper.clamp(min_x, 0, screenWidth - 1);
        max_x = MatrixHelper.clamp(max_x, 0, screenWidth - 1);
        min_y = MatrixHelper.clamp(min_y, 0, screenHeight - 1);
        max_y = MatrixHelper.clamp(max_y, 0, screenHeight - 1);


        for(int x=min_x; x<=max_x; x++) {
            for(int y=min_y; y<=max_y; y++) {
                // 4个子像素有几个在三角形内部

                int orgColor = -1;
                int s = 0;
                // 检测4个子像素的深度是否在前面，如果有一个在就进行着色器的计算，
                // 然后储存msaa子像素的颜色与深度
                // 最后将4个像素压成一个
                for(int i = 0; i < 4; ++i){
                    float x1 = x + msaaOffsets[i][0];
                    float y1 = y + msaaOffsets[i][1];
                    if(!insideTriangle(x1,y1))
                        continue;
                    int ind = getMassIndex(x, y, i);
                    float z_interpolated = getInterpolatedZ(x1, y1);
                    if (zBuffer[ind] < z_interpolated)
                        continue;

                    ++s;
                    // 深度较浅，需要绘制
                    if(orgColor == -1){
                        List<FMatrix> params = vertexShaderResults[0].outParams==null ? null : getInterpolatedParams(x, y);
                        orgColor = nowShader.run(params);
                    }
                    zBuffer[ind] = z_interpolated;
                    msaaBuf[ind] = orgColor;

                }
                if(s == 0)
                    continue;

                int ind = getIndex(x,y);

                if(s == 4){
                    screen[ind] = orgColor;
                }else{
                    int ind1 = getMassIndex(x, y, 0);
                    int ind2 = getMassIndex(x, y, 1);
                    int ind3 = getMassIndex(x, y, 2);
                    int ind4 = getMassIndex(x, y, 3);
                    int tmp1 = getAverageColor(msaaBuf[ind1], msaaBuf[ind2], 0.5);
                    int tmp2 = getAverageColor(msaaBuf[ind3], msaaBuf[ind4], 0.5);
                    int ans = getAverageColor(tmp1, tmp2, 0.5);
                    screen[ind] = ans;
                }
                frameObjectBuffer[ind] = currentObject;

            }
        }
    }

    public FMatrix4x4 getProjectionMatrix(int eyeFov){
        float aspectRatio = (float)screenWidth / screenHeight;
        float n = -zNear, f = -zFar;
        int alpha = eyeFov / 2;
        float height = 2 * zNear * QueryTable.sin[alpha] / QueryTable.cos[alpha];
        float width = height * aspectRatio;
        float l = -width/2, r = width/2, b = -height/2, t = height/2;
        FMatrix4x4 ans = new FMatrix4x4(2*n/(r-l), 0, 0, 0,
                0, 2*n/(t-b), 0, 0,
                0, 0, (f+n)/(n-f), 2*f*n/(f-n),
                0, 0, 1, 0);

//        System.out.println(ans);
        return ans;
    }

    public boolean insideTriangle(float x, float y)
    {
        FMatrix3[] _v = updatedVertices;
        double[] v_ab = new double[2];
        double[] v_bc = new double[2];
        double[] v_ca = new double[2];

        double[] v_ap = new double[2];
        double[] v_bp = new double[2];
        double[] v_cp = new double[2];

        v_ab[0] = _v[1].a1-_v[0].a1; v_ab[1] = _v[1].a2-_v[0].a2;
        v_bc[0] = _v[2].a1-_v[1].a1; v_bc[1] = _v[2].a2-_v[1].a2;
        v_ca[0] = _v[0].a1-_v[2].a1; v_ca[1] = _v[0].a2-_v[2].a2;

        v_ap[0] = x-_v[0].a1; v_ap[1] = y-_v[0].a2;
        v_bp[0] = x-_v[1].a1; v_bp[1] = y-_v[1].a2;
        v_cp[0] = x-_v[2].a1; v_cp[1] = y-_v[2].a2;

        boolean dir1 = cross(v_ap, v_ab) >= 0;
        boolean dir2 = cross(v_bp, v_bc) >= 0;
        boolean dir3 = cross(v_cp, v_ca) >= 0;

        return (dir1==dir2 && dir2==dir3);
    }

    private double cross(double[] v1, double[] v2) {
        return v1[0] * v2[1] - v1[1] * v2[0];
    }

    private FMatrix3 computeBarycentric2D(float x, float y)
    {
        FMatrix3[] v = updatedVertices;
        float c1 = (x*(v[1].a2 - v[2].a2) + (v[2].a1 - v[1].a1)*y + v[1].a1*v[2].a2 - v[2].a1*v[1].a2) / (v[0].a1*(v[1].a2 - v[2].a2) + (v[2].a1 - v[1].a1)*v[0].a2 + v[1].a1*v[2].a2 - v[2].a1*v[1].a2);
        float c2 = (x*(v[2].a2 - v[0].a2) + (v[0].a1 - v[2].a1)*y + v[2].a1*v[0].a2 - v[0].a1*v[2].a2) / (v[1].a1*(v[2].a2 - v[0].a2) + (v[0].a1 - v[2].a1)*v[1].a2 + v[2].a1*v[0].a2 - v[0].a1*v[2].a2);
        float c3 = (x*(v[0].a2 - v[1].a2) + (v[1].a1 - v[0].a1)*y + v[0].a1*v[1].a2 - v[1].a1*v[0].a2) / (v[2].a1*(v[0].a2 - v[1].a2) + (v[1].a1 - v[0].a1)*v[2].a2 + v[0].a1*v[1].a2 - v[1].a1*v[0].a2);
        return new FMatrix3(c1,c2,c3);
    }

    private float getInterpolatedZ(float x, float y) {
        FMatrix3 b = computeBarycentric2D(x, y);
        float alpha = b.a1;
        float beta = b.a2;
        float gamma = b.a3;
        float z_interpolated = alpha * updatedVertices[0].a3 + beta * updatedVertices[1].a3  + gamma * updatedVertices[2].a3;
        return z_interpolated;
    }

    private int getIndex(int x, int y)
    {
        return (screenHeight-1-y)*screenWidth + x;
    }

    private int getMassIndex(int x, int y, int i)
    {
        int dx = i & 1;
        int dy = (i & 2) >> 1;
        return (screenHeight * 2 - 1 - y * 2 - dy)*2*screenWidth + 2*x + dx;
    }

    private int getAverageColor(int color1, int color2, double color1Ratio) {
        //return (int) (color1Ratio * color1 + (1 - color1Ratio) * color2);
        int r1 = (color1 & 0xff0000) >> 16;
        int r2 = (color2 & 0xff0000) >> 16;
        int g1 = (color1 & 0x00ff00) >> 8;
        int g2 = (color2 & 0x00ff00) >> 8;
        int b1 = (color1 & 0x0000ff);
        int b2 = (color2 & 0x0000ff);

        int r = MatrixHelper.clamp((int) (color1Ratio * r1 + (1 - color1Ratio) * r2), 0, 255);
        int g = MatrixHelper.clamp((int) (color1Ratio * g1 + (1 - color1Ratio) * g2), 0, 255);
        int b = MatrixHelper.clamp((int) (color1Ratio * b1 + (1 - color1Ratio) * b2), 0, 255);
        return (r << 16) | (g << 8) | b;
    }

    private int colorMultiply(int color, double ratio){
        int r1 = (color & 0xff0000) >> 16;
        int g1 = (color & 0x00ff00) >> 8;
        int b1 = color & 0x0000ff;
        int r = MatrixHelper.clamp((int) (ratio * r1), 0, 255);
        int g = MatrixHelper.clamp((int) (ratio * g1), 0, 255);
        int b = MatrixHelper.clamp((int) (ratio * b1), 0, 255);
        return (r << 16) | (g << 8) | b;
    }

    private List<FMatrix> getInterpolatedParams(float x, float y){
        FMatrix3 b = computeBarycentric2D(x, y);
        float alpha = b.a1;
        float beta = b.a2;
        float gamma = b.a3;

        // 对每个矩阵进行操作
        int length = vertexShaderResults[0].outParams.size();
        ArrayList<FMatrix> ans = new ArrayList<>(length);
        for(int i = 0; i < length; ++i){
            FMatrix m1 = vertexShaderResults[0].outParams.get(i);
            FMatrix m2 = vertexShaderResults[1].outParams.get(i);
            FMatrix m3 = vertexShaderResults[2].outParams.get(i);
            FMatrix now;

            now = m1.createLike();
            ans.add(now);

            int col = m1.getNumCols();
            int row = m1.getNumRows();
            for(int r = 0; r < row; ++r){
                for(int c = 0; c < col; ++c){
                    float result = m1.get(r,c) * alpha + m2.get(r,c) * beta + m3.get(r,c) * gamma;
                    now.set(r, c, result);
                }
            }
        }

        return ans;
    }

    public void clearBuffer(){
        // 重置缓冲区
        zBuffer[0] = 50.0f;
        int l = zBuffer.length;
        for(int i = 1; i < l; i+=i)
            System.arraycopy(zBuffer, 0, zBuffer, i, Math.min(l - i, i));

        msaaBuf[0] = (50 << 16) | (50 << 8) | 50;
        for(int i = 1; i < l; i+=i)
            System.arraycopy(msaaBuf, 0, msaaBuf, i, Math.min(l - i, i));
    }

}
