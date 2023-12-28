package com.hb.swrender.utils;

import org.ejml.data.FMatrix3;
import org.ejml.data.FMatrix4;
import org.ejml.data.FMatrix4x4;
import org.ejml.dense.fixed.CommonOps_FDF4;
import com.hb.swrender.Camera;
import com.hb.swrender.Rasterizer;

public class MatrixHelper {
    /**
     * 计算一个物体上的一个顶点如何变换到裁切空间
     * @param worldMat 整个模型变换到世界系的矩阵
     * @param vertexPos 该顶点相对于模型的坐标
     * @return
     */
    public static FMatrix4 calcPerspective(FMatrix4x4 worldMat, FMatrix3 vertexPos){
        FMatrix4x4 tmp = new FMatrix4x4();
        CommonOps_FDF4.mult(Rasterizer.projectionMatrix, Camera.viewMatrix, tmp);
        FMatrix4x4 tmp2 = new FMatrix4x4();
        CommonOps_FDF4.mult(tmp, worldMat, tmp2);
        FMatrix3 pos = vertexPos;
        FMatrix4 tmp3 = new FMatrix4();
        CommonOps_FDF4.mult(tmp2, new FMatrix4(pos.a1, pos.a2, pos.a3, 1), tmp3);
        return tmp3;
    }

    public static int vecColorToInt(FMatrix3 color){
        int r = clamp((int) color.a1, 0, 255);
        int g = clamp((int) color.a2, 0, 255);
        int b = clamp((int) color.a3, 0, 255);
        return (r << 16) | (g << 8) | b;
    }

    public static int clamp(int a, int low, int high){
        if(a < low)
            return low;
        if(a > high)
            return high;
        return a;
    }
}
