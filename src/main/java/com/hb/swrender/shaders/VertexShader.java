package com.hb.swrender.shaders;

import org.ejml.data.*;

import java.util.List;

// 任务：把传过来的数据传计算后传出给后面的片段着色器。需要设置该点在裁切平面的坐标！
// 如果整个面片都不可见，则不会调用这个顶点着色器
// 例如：渲染模型时，传入模型的一个点的世界坐标、模型在该点在世界的坐标、法线向量、材质坐标。设置该点在裁切平面的位置，并传出要绘制的材质的坐标
public abstract class VertexShader {
    public List<FMatrix> result;
    // 处理完参数后要把结果放在result中，之后光栅化程序会去取出几个顶点的结果result，然后进行插值，最后传入FragmentShader
    public abstract FMatrix4 run(List<FMatrix> params);

}
