package com.hb.swrender;

import com.hb.swrender.shaders.VertexShaderResult;
import org.ejml.data.FMatrix4;

import java.util.ArrayList;
import java.util.List;

public class ClipUtils {

    private static FMatrix4[] ViewLines = {
        //near
        new FMatrix4(0,0,1,1),
        //far
        new FMatrix4(0,0,-1,1),
        //left
        new FMatrix4(1,0,0,1),
        //right
        new FMatrix4(-1,0,0,1),
        //top
        new FMatrix4(0,-1,0,1),
        //bottom
        new FMatrix4(0,1,0,1)
    };

    // 检测一个裁切空间内的点是能落在NDC的[-1,1]^3内
    public static boolean vertexInside(FMatrix4 v) {
        if(v.a4 == 0)
            return false;
        if(v.a1 / v.a4 <-1 || v.a1 / v.a4 > 1)
            return false;
        if(v.a2 / v.a4 <-1 || v.a2 / v.a4 > 1)
            return false;
        if(v.a3 / v.a4 <-1 || v.a3 / v.a4 > 1)
            return false;
        return true;
    }

    public static boolean inside(FMatrix4 line,FMatrix4 p) {
        return p.a4 * (line.a1 * p.a1 + line.a2 * p.a2 + line.a3 * p.a3 + line.a4 * p.a4) >= 0;
    }

    private static VertexShaderResult intersect(VertexShaderResult v1, VertexShaderResult v2, FMatrix4 line) {
        float da = v1.computedPos.a1 * line.a1 + v1.computedPos.a2 * line.a2 + v1.computedPos.a3 * line.a3 + v1.computedPos.a4 * line.a4;
        float db = v2.computedPos.a1 * line.a1 + v2.computedPos.a2 * line.a2 + v2.computedPos.a3 * line.a3 + v2.computedPos.a4 * line.a4;

        float weight = da / (da-db);

        return VertexShaderResult.lerp(v1, v2, weight);
    }

    /**
     * 使用SutherlandHodgeman算法对3个顶点进行裁切，原地对传入的数组进行修改，返回3n个顶点
     * @param vsResults 3个顶点
     */
    public static void SutherlandHodgeman(List<VertexShaderResult> vsResults) {
        List<VertexShaderResult> output = vsResults;

        if (vertexInside(output.get(0).computedPos) && vertexInside(output.get(1).computedPos) && vertexInside(output.get(2).computedPos)) {
            return;
        }
        for (int i = 0; i < ViewLines.length ; i++) {
            ArrayList<VertexShaderResult> input = new ArrayList<>(output);
            output.clear();

            for (int j = 0; j < input.size(); j++) {
                VertexShaderResult current = input.get(j);
                VertexShaderResult last = input.get((j + input.size() - 1) % input.size());
                if (inside(ViewLines[i], current.computedPos)) {
                    if (!inside(ViewLines[i],last.computedPos)) {
                        VertexShaderResult intersecting = intersect(last, current, ViewLines[i]);
                        output.add(intersecting);
                    }
                    output.add(current);
                }
                else if(inside(ViewLines[i], last.computedPos)){
                    VertexShaderResult intersecting = intersect(last, current, ViewLines[i]);
                    output.add(intersecting);
                }
            }
        }
    }


}
