package com.hb.swrender.shaders;

import org.ejml.data.FMatrix;
import org.ejml.data.FMatrix4;

import java.util.List;

public class VertexShaderResult {
    public FMatrix4 computedPos;
    public List<FMatrix> outParams;


    public VertexShaderResult(FMatrix4 computedPos, List<FMatrix> outParams) {
        this.computedPos = computedPos;
        this.outParams = outParams;
    }
}
