package com.hb.swrender.shaders;

import org.ejml.data.FMatrix3;
import org.ejml.data.FMatrix4;
import org.ejml.data.FMatrix;
import org.ejml.data.FMatrix4x4;
import com.hb.swrender.utils.MatrixHelper;

import java.util.List;

public class SimpleVS extends VertexShader{

    private FMatrix4x4 worldPos;
    // 按照普通的投影几何将当前顶点转换到裁切空间
    public SimpleVS(FMatrix4x4 worldPos){
        this.worldPos = worldPos;
    }
    @Override
    public FMatrix4 run(List<FMatrix> params) {
//        FMatrix4x4 tmp = new FMatrix4x4();
//        CommonOps_FDF4.mult(Rasterizer.projectionMatrix, Camera.viewMatrix, tmp);
//        FMatrix4x4 tmp2 = new FMatrix4x4();
//        CommonOps_FDF4.mult(tmp, worldPos, tmp2);
//        FMatrix3 pos =(FMatrix3) params.get(0);
//        FMatrix4 tmp3 = new FMatrix4();
//        CommonOps_FDF4.mult(tmp2, new FMatrix4(pos.a1, pos.a2, pos.a3, 1), tmp3);
        return MatrixHelper.calcPerspective(worldPos, (FMatrix3) params.get(0));
    }
}
