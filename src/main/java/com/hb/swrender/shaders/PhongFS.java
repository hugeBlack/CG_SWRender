package com.hb.swrender.shaders;

import com.hb.swrender.Camera;
import com.hb.swrender.utils.MatrixHelper;
import com.hb.swrender.utils.Vector3dHelper;
import org.ejml.data.FMatrix;
import org.ejml.data.FMatrix3;
import org.ejml.dense.fixed.CommonOps_FDF3;
import org.ejml.dense.fixed.CommonOps_FDF4;
import org.ejml.dense.fixed.NormOps_FDF3;

import java.util.List;

public class PhongFS extends FragmentShader{

    public static FMatrix3 lightColor = new FMatrix3(1,1,1);
    public static FMatrix3 lightPos = new FMatrix3(2, 2, -1.0f);

    @Override
    public int run(List<FMatrix> params) {
        // in vec3 Normal;
        FMatrix3 normal = (FMatrix3) params.get(2);
        // objectColor
        FMatrix3 objectColor = (FMatrix3) params.get(0);
        // in vec3 fragPos
        FMatrix3 fragPos = (FMatrix3) params.get(1);

        float ambientStrength = 0.1f;

        // vec3 ambient = ambientStrength * lightColor;
        FMatrix3 ambient = new FMatrix3();
        CommonOps_FDF3.scale(ambientStrength, lightColor, ambient);

        // vec3 norm = normalize(Normal);
        float t1 = NormOps_FDF3.normF(normal);
        FMatrix3 norm = new FMatrix3();
        CommonOps_FDF3.scale(1.0f / t1, normal, norm);

        // vec3 lightDir = normalize(lightPos - FragPos);
        FMatrix3 lightDir = new FMatrix3();
        CommonOps_FDF3.subtract(lightPos, fragPos, lightDir);
        NormOps_FDF3.normalizeF(lightDir);

        // float diff = max(dot(norm, lightDir), 0.0);
        float diff = Math.max(CommonOps_FDF3.dot(norm, lightDir), 0f);



        // vec3 diffuse = diff * lightColor;
        FMatrix3 diffuse = new FMatrix3();
        CommonOps_FDF3.scale(diff, lightColor, diffuse);

        float specularStrength = 0.5f;

        // vec3 viewDir = normalize(viewPos - FragPos);
        FMatrix3 viewDir = new FMatrix3();
        CommonOps_FDF3.subtract(Camera.position, fragPos, viewDir);
        NormOps_FDF3.normalizeF(viewDir);


        // vec3 reflectDir = reflect(-lightDir, norm);
        FMatrix3 nLightDir = new FMatrix3();
        CommonOps_FDF3.scale(-1, lightDir, nLightDir);
        FMatrix3 reflectDir = Vector3dHelper.reflect(nLightDir, norm);



        // float spec = pow(max(dot(viewDir, reflectDir), 0.0), 32);
        float t = Math.max(CommonOps_FDF3.dot(viewDir, reflectDir), 0.0f);
        float spec = (float) Math.pow(t, 100);

        // vec3 specular = specularStrength * spec * lightColor;
        FMatrix3 specular = new FMatrix3();
        CommonOps_FDF3.scale(specularStrength * spec, lightColor, specular);

        // vec3 result = (ambient + diffuse + specular) * objectColor;
        FMatrix3 tmp1 = new FMatrix3();
        FMatrix3 result = new FMatrix3();
        CommonOps_FDF3.add(ambient, diffuse, tmp1);
        CommonOps_FDF3.addEquals(tmp1, specular);
        CommonOps_FDF3.elementMult(tmp1, objectColor, result);
        // FragColor = vec4(result, 1.0);
        return MatrixHelper.vecColorToInt(result);
    }
}
