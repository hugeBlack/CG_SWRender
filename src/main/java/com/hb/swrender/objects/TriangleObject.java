package com.hb.swrender.objects;

import com.hb.swrender.shaders.*;
import com.hb.swrender.utils.Vector3dHelper;
import org.ejml.data.FMatrix3;
import org.ejml.data.FMatrix4x4;

import java.util.LinkedList;

public class TriangleObject extends RenderableObject {
    private VertexBuffer[] myVBO;
    private int[] myVAO;


    private FMatrix4x4 modelMatrix;

    public TriangleObject(){
        this.pos = new FMatrix3(0,0,1);
        myVBO = new VertexBuffer[6];
        for(int i = 0; i < 6; ++i){
            myVBO[i] = new VertexBuffer();
        }
//        myVBO[0].shaderParams = new LinkedList<>();
//        myVBO[0].shaderParams.add(new FMatrix3(0,0,0));
//        myVBO[1].shaderParams = new LinkedList<>();
//        myVBO[1].shaderParams.add(new FMatrix3(1,0,0));
//        myVBO[2].shaderParams = new LinkedList<>();
//        myVBO[2].shaderParams.add(new FMatrix3(1,1,0));
//        myVBO[3].shaderParams = new LinkedList<>();
//        myVBO[3].shaderParams.add(new FMatrix3(0,1,0));
        myVBO[0].shaderParams = new LinkedList<>();
        myVBO[0].shaderParams.add(new FMatrix3(2, 0, -2));
        myVBO[1].shaderParams = new LinkedList<>();
        myVBO[1].shaderParams.add(new FMatrix3(0, 2, -2));
        myVBO[2].shaderParams = new LinkedList<>();
        myVBO[2].shaderParams.add(new FMatrix3(-2, 0, -2));
        myVBO[3].shaderParams = new LinkedList<>();
        myVBO[3].shaderParams.add(new FMatrix3(3.5f, -1, -5));
        myVBO[4].shaderParams = new LinkedList<>();
        myVBO[4].shaderParams.add(new FMatrix3(2.5f, 1.5f, -5));
        myVBO[5].shaderParams = new LinkedList<>();
        myVBO[5].shaderParams.add(new FMatrix3(-1, 0.5f, -5));

        //myVAO = new int[]{0,2,3,0,1,2};
        myVAO = new int[]{5,4,3, 2,1,0};

        for(int i = 0; i < myVAO.length; i += 3){
            FMatrix3 norm = Vector3dHelper.triangleNormCompute((FMatrix3) myVBO[myVAO[i]].shaderParams.get(0),
                    (FMatrix3) myVBO[myVAO[i + 1]].shaderParams.get(0),
                    (FMatrix3) myVBO[myVAO[i + 2]].shaderParams.get(0));

            for(int j = 0; j < 3; ++j){
                myVBO[myVAO[i+j]].shaderParams.add(norm);
            }

        }

        modelMatrix = new FMatrix4x4(1,0,0,0,
                0,1,0,0,
                0,0,1,0,
                0,0,0,1);

    }
    @Override
    public VertexBuffer[] getMyVBO() {
        return myVBO;
    }

    @Override
    public int[] getMyVAO() {
        return myVAO;
    }

    @Override
    public FragmentShader getFragmentShader(int i) {
        // return new GradientFS();
        return new PhongFS();
    }

    @Override
    public VertexShader getVertexShader(int i) {
//        return new SimpleVS(modelMatrix);
        return switch (i % 3) {
            case 0 -> new PhongVS(modelMatrix, 255 << 16);
            case 1 -> new PhongVS(modelMatrix, 255 << 8);
            case 2 -> new PhongVS(modelMatrix, 255);
            default -> new GradientVS(modelMatrix, 114514);
        };
    }
}
