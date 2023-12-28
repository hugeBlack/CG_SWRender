package com.hb.swrender.utils;

import org.ejml.data.FMatrix3;

public class Vector3dHelper {
    //矢量在 vec.a1 vec.a2, vec.a3 轴上的分量

    //绕 Y 轴旋转矢量，使其顺时针旋转指定角度
    public static FMatrix3 rotateY(FMatrix3 vec, int angle) {
        float sin = QueryTable.sin[angle];
        float cos = QueryTable.cos[angle];
        float old_X = vec.a1;
        float old_Z = vec.a3;
        vec.a1 = cos * old_X + sin * old_Z;
        vec.a3 = -sin * old_X + cos * old_Z;
        return vec;
    }

    public static FMatrix3 rotateY(FMatrix3 vec, float sin, float cos) {
        float old_X = vec.a1;
        float old_Z = vec.a3;
        vec.a1 = cos * old_X + sin * old_Z;
        vec.a3 = -sin * old_X + cos * old_Z;
        return vec;
    }

    //绕 X 轴旋转矢量，使其顺时针旋转指定角度
    public static FMatrix3 rotateX(FMatrix3 vec, int angle) {
        float sin = QueryTable.sin[angle];
        float cos = QueryTable.cos[angle];
        float old_Y = vec.a2;
        float old_Z = vec.a3;
        vec.a2 = cos * old_Y + sin * old_Z;
        vec.a3 = -sin * old_Y + cos * old_Z;
        return vec;
    }

    public static FMatrix3 rotateX(FMatrix3 vec, float sin, float cos) {
        float old_Y = vec.a2;
        float old_Z = vec.a3;
        vec.a2 = cos * old_Y + sin * old_Z;
        vec.a3 = -sin * old_Y + cos * old_Z;
        return vec;
    }

    //绕 Z 轴旋转矢量，使其顺时针旋转指定角度
    public FMatrix3 rotateZ(FMatrix3 vec, int angle) {
        float sin = QueryTable.sin[angle];
        float cos = QueryTable.cos[angle];
        float old_X = vec.a1;
        float old_Y = vec.a2;
        vec.a1 = cos * old_X + sin * old_Y;
        vec.a2 = -sin * old_X + cos * old_Y;
        return vec;
    }

    public static FMatrix3 rotateZ(FMatrix3 vec, float sin, float cos) {
        float old_X = vec.a1;
        float old_Y = vec.a2;
        vec.a1 = cos * old_X + sin * old_Y;
        vec.a2 = -sin * old_X + cos * old_Y;
        return vec;
    }

    public static FMatrix3 cross(FMatrix3 a, FMatrix3 b) {
        FMatrix3 ans = new FMatrix3(a.a2*b.a3 - a.a3*b.a2, a.a3*b.a1 - a.a1*b.a3, a.a1*b.a2 - a.a2*b.a1);
        return ans;
    }

}