package com.hb.swrender.shaders;

import com.hb.swrender.utils.MatrixHelper;
import org.ejml.data.FMatrix;
import org.ejml.data.FMatrix3;
import org.ejml.data.FMatrix4;
import org.ejml.data.FMatrix4x4;

import java.util.ArrayList;
import java.util.List;

public class GradientVS extends VertexShader{

    private FMatrix4x4 worldPos;
    private FMatrix3 color;
    // 按照普通的投影几何将当前顶点转换到裁切空间
    public GradientVS(FMatrix4x4 worldPos, int color){
        this.worldPos = worldPos;
        this.color = new FMatrix3(color >> 16, (color & 0x00ff00) >> 8, color & 0x0000ff);
    }
    @Override
    public FMatrix4 run(List<FMatrix> params) {
        FMatrix4 pos = MatrixHelper.calcPerspective(worldPos, (FMatrix3) params.get(0));
        result = new ArrayList<>(1);
        result.add(this.color);
        return pos;
    }
}
