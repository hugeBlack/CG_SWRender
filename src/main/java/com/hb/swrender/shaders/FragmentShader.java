package com.hb.swrender.shaders;


import org.ejml.data.FMatrix;

import java.util.List;

// 任务：程序将三角形的三个点通过光栅化后，对每一个点都执行一遍像素着色器的程序
// 传入的几个参数都是经过对三个顶点的插值后得到的，不是顶点着色器得到的原始值
// 深度会由光栅化程序管理，不该显示的像素不会被调用这个着色器
// 输出该像素的颜色
public abstract class FragmentShader {

    public abstract int run(List<FMatrix> params);
}
