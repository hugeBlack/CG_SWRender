package com.hb.swrender.shaders;

import org.ejml.data.FMatrix;


import java.util.List;

public class SimpleFS extends FragmentShader{
    private int color;

    public SimpleFS(int color){
        this.color = color;
    }
    @Override
    public int run(List<FMatrix> params) {
        return color;
    }
}
