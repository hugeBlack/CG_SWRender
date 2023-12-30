package com.hb.swrender.objects;

import com.hb.swrender.shaders.*;
import org.ejml.data.FMatrix2;
import org.ejml.data.FMatrix3;
import org.ejml.data.FMatrix4x4;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ObjModel extends RenderableObject{
    private List<FMatrix3> vertices = new ArrayList<>();
    private List<FMatrix2> texturePos = new ArrayList<>();
    private List<FMatrix3> normals = new ArrayList<>();
    private List<List<SurfaceVertex>> faces = new ArrayList<>();

    private VertexBuffer[] myVBO = null;
    private int[] myVAO = null;
    private int faceVertexCount = 0;
    private FMatrix4x4 modelMatrix = new FMatrix4x4(1,0,0,0,
                                                                          0,1,0,0,
                                                                          0,0,1,0,
                                                                          0,0,0,1);

    private FragmentShader cachedFS = new PhongFS();
    private VertexShader cachedVS = new PhongObjectVS(modelMatrix);

    public ObjModel (String filePath) {

        List<Integer> tmpVAO = new ArrayList<>();
        List<VertexBuffer> tmpVBO = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("v ")) {
                    // Vertex information
                    String[] parts = line.split("\\s+");
                    float x = Float.parseFloat(parts[1]);
                    float y = Float.parseFloat(parts[2]);
                    float z = Float.parseFloat(parts[3]);
                    vertices.add(new FMatrix3(x, y, z));
                }else if (line.startsWith("vt ")) {
                    // Face information
                    String[] parts = line.split("\\s+");
                    float x = Float.parseFloat(parts[1]);
                    float y = Float.parseFloat(parts[2]);
                    texturePos.add(new FMatrix2(x, y));
                } else if (line.startsWith("vn ")) {
                    // Face information
                    String[] parts = line.split("\\s+");
                    float x = Float.parseFloat(parts[1]);
                    float y = Float.parseFloat(parts[2]);
                    float z = Float.parseFloat(parts[3]);
                    normals.add(new FMatrix3(x, y, z));
                } else if (line.startsWith("f ")) {
                    // Face information
                    String[] parts = line.split("\\s+");
                    List<SurfaceVertex> surfaceVertexList = new ArrayList<>();
                    faces.add(surfaceVertexList);
                    for(String part : parts){
                        if(part.charAt(0) == 'f')
                            continue;
                        String ids[] = part.split("/");
                        int vId = Integer.parseInt(ids[0]) - 1;
                        faceVertexCount++;
                        if(ids.length < 2){
                            surfaceVertexList.add(new SurfaceVertex(vertices.get(vId), null, null));
                            continue;
                        }
                        int tId = Integer.parseInt(ids[1]) - 1;
                        if(ids.length < 3){
                            surfaceVertexList.add(new SurfaceVertex(vertices.get(vId), texturePos.get(tId), null));
                            continue;
                        }
                        int nId = Integer.parseInt(ids[2]) - 1;
                        surfaceVertexList.add(new SurfaceVertex(vertices.get(vId), texturePos.get(tId), normals.get(nId)));
                    }
                }
            }
        } catch (IOException e){
            throw new RuntimeException("Failed To Read Model!");
        }

    }

    public void setPos(float x, float y, float z){
        modelMatrix.a14 = x;
        modelMatrix.a24 = y;
        modelMatrix.a34 = z;
    }


    @Override
    public VertexBuffer[] getMyVBO() {
        if(myVBO == null) {
            myVBO = new VertexBuffer[faceVertexCount];
            int i = 0;
            // 0: pos, 1: texture, 2: normal
            for(List<SurfaceVertex> face : faces){
                for(SurfaceVertex vertex : face){
                    VertexBuffer vb = new VertexBuffer();
                    vb.shaderParams = new ArrayList<>();
                    vb.shaderParams.add(vertex.vertex);
                    vb.shaderParams.add(vertex.normal);
                    vb.shaderParams.add(vertex.texturePos);
                    myVBO[i] = vb;
                    ++i;
                }
            }
        }

        return myVBO;
    }

    @Override
    public int[] getMyVAO() {
        if(myVAO == null){
            myVAO = new int[faceVertexCount];
            for(int i = 0; i < faceVertexCount; ++i){
                myVAO[i] = i;
            }
        }

        return myVAO;
    }

    @Override
    public FragmentShader getFragmentShader(int i) {
        return cachedFS;
    }

    @Override
    public VertexShader getVertexShader(int i) {
        return cachedVS;
    }
}
