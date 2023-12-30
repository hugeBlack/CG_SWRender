package com.hb.swrender.objects;

import com.hb.swrender.shaders.PhongObjectVS;
import org.ejml.data.FMatrix3;

public class CubeObject extends ObjModel {

    public FMatrix3 color;
    public CubeObject() {
        super("assets/cube.obj");
        color = new FMatrix3();
        getNewColor();
    }

    private void getNewColor() {
        color.setTo((float) (Math.random() * 255), (float) (Math.random() * 255), (float) (Math.random() * 255));
        ((PhongObjectVS)(cachedVS)).color.setTo(color);
    }

    @Override
    public void onClick(){
        getNewColor();
    }
}
