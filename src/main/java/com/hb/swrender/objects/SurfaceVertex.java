package com.hb.swrender.objects;

import org.ejml.data.FMatrix2;
import org.ejml.data.FMatrix3;

public class SurfaceVertex {
    public FMatrix3 vertex;
    public FMatrix2 texturePos;
    public FMatrix3 normal;

    public SurfaceVertex(FMatrix3 vertex, FMatrix2 texturePos, FMatrix3 normal) {
        this.vertex = vertex;
        this.texturePos = texturePos;
        this.normal = normal;
    }
}
