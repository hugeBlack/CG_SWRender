package com.hb.swrender.shaders;

import org.ejml.data.FMatrix;
import org.ejml.data.FMatrix4;

import java.util.ArrayList;
import java.util.List;

public class VertexShaderResult {
    public FMatrix4 computedPos;
    public List<FMatrix> outParams;


    public VertexShaderResult(FMatrix4 computedPos, List<FMatrix> outParams) {
        this.computedPos = computedPos;
        this.outParams = outParams;
    }

    public static VertexShaderResult lerp(VertexShaderResult v1, VertexShaderResult v2, float weight){
        // a + w*(b-a);
        VertexShaderResult ans = new VertexShaderResult(new FMatrix4(), new ArrayList<>());
        for(int i = 0; i < 4; ++i){
            ans.computedPos.set(i, 0, floatLerp(v1.computedPos.get(i,0), v2.computedPos.get(i,0), weight));
        }
        int l = v1.outParams.size();
        for(int i = 0; i < l; ++i){
            FMatrix newMatrix = v1.outParams.get(i).createLike();
            int row = newMatrix.getNumRows();
            int col = newMatrix.getNumCols();
            for(int r = 0; r < row; ++r){
                for(int c = 0; c < col; ++c){
                    newMatrix.set(r,c, floatLerp(v1.outParams.get(i).get(r,c), v2.outParams.get(i).get(r,c), weight));
                }
            }
            ans.outParams.add(newMatrix);
        }
        return ans;

    }

    private static float floatLerp(float a, float b, float w){
        return a + w*(b-a);
    }
}
