package com.hb.swrender.objects;

import com.hb.swrender.shaders.FragmentShader;
import com.hb.swrender.shaders.VertexShader;
import org.ejml.data.FMatrix3;
import com.hb.swrender.shaders.VertexBuffer;

public abstract class RenderableObject {

    public FMatrix3 pos;

    public abstract VertexBuffer[] getMyVBO();
    public abstract int[] getMyVAO();

    // 获得第VAO中第i个面要用的片元着色器
    public abstract FragmentShader getFragmentShader(int i);

    // 获得第VBO中第i个顶点的要使用的顶点着色器
    public abstract VertexShader getVertexShader(int i);

    public void onClick(){

    }

}
