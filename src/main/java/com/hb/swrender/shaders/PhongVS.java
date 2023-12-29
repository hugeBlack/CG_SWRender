package com.hb.swrender.shaders;

import com.hb.swrender.utils.MatrixHelper;
import org.ejml.data.FMatrix;
import org.ejml.data.FMatrix3;
import org.ejml.data.FMatrix4;
import org.ejml.data.FMatrix4x4;
import org.ejml.dense.fixed.CommonOps_FDF3;
import org.ejml.dense.fixed.CommonOps_FDF4;

import java.util.ArrayList;
import java.util.List;

public class PhongVS extends VertexShader{

    private FMatrix4x4 worldPos;
    private FMatrix3 color;
    // 按照普通的投影几何将当前顶点转换到裁切空间
    public PhongVS(FMatrix4x4 worldPos, int color){
        this.worldPos = worldPos;
        this.color = new FMatrix3(color >> 16, (color & 0x00ff00) >> 8, color & 0x0000ff);
    }
    @Override
    public FMatrix4 run(List<FMatrix> params) {
        FMatrix4 pos = MatrixHelper.calcPerspective(worldPos, (FMatrix3) params.get(0));
        result = new ArrayList<>(3);
        result.add(this.color);

        FMatrix3 vPos = (FMatrix3) params.get(0);
        FMatrix4 fragPos4d = new FMatrix4();
        CommonOps_FDF4.mult(worldPos, new FMatrix4(vPos.a1, vPos.a2, vPos.a3, 1), fragPos4d);
        FMatrix3 fragPos = new FMatrix3(fragPos4d.a1, fragPos4d.a2, fragPos4d.a3);
        result.add(fragPos);
        // normal
        result.add(params.get(1));

        return pos;
    }
}
