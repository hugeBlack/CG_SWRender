package com.hb.swrender.shaders;

import com.hb.swrender.utils.MatrixHelper;
import org.ejml.data.FMatrix;
import org.ejml.data.FMatrix3;

import java.util.List;

public class GradientFS extends FragmentShader{
    private int color;

    public GradientFS(){
    }
    @Override
    public int run(List<FMatrix> params) {
        FMatrix3 color = (FMatrix3) params.get(0);
        return MatrixHelper.vecColorToInt(color);
    }
}
