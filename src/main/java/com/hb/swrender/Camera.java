package com.hb.swrender;

import org.ejml.data.FMatrix3;
import org.ejml.data.FMatrix4x4;
import org.ejml.dense.fixed.CommonOps_FDF3;
import org.ejml.dense.fixed.CommonOps_FDF4;
import org.ejml.dense.fixed.NormOps_FDF3;
import com.hb.swrender.utils.QueryTable;
import com.hb.swrender.utils.Vector3dHelper;

public class Camera {
    //视角的位置矢量
    public static FMatrix3 position;

    //视角的方向矢量
    public static FMatrix3 viewDirection;
    public static FMatrix3 worldUp;
    public static FMatrix3 right;
    public static FMatrix3 up;


    //判断视角走位，观察方向的变量
    public static boolean MOVE_FORWARD, MOVE_BACKWARD, SLIDE_LEFT, SLIDE_RIGHT, LOOK_UP, LOOK_DOWN, LOOK_RIGHT, LOOK_LEFT, MOVE_UP, MOVE_DOWN;

    //视角在Y轴上的旋转， 用来控制向左或向右看
    public static int pitch;

    //视角在X轴上的旋转, 用来控制向上或向下看
    public static int yaw;

    //视角改变观察方向的速率,每频可旋转2度
    public static int turnRate = 2;

    //视角改变位置的速度，每频可移动0.03f个单位长度
    public static float moveSpeed = 0.03f;
    public static FMatrix4x4 viewMatrix;

    //初始化方法
    public static void init(float x, float y, float z){
        position = new FMatrix3(x,y,z);
        viewDirection = new FMatrix3(0, 0, -1);
        worldUp = new FMatrix3(0,1,0);
        right = new FMatrix3();
        up = new FMatrix3();
        yaw = 270;
        pitch = 0;
        updateCameraVectors();
        updateRotationMatrix();
    }

    //更新视角状态
    public static void update(){
        //处理向上看， 并保证仰角不大于等于90度
        if(!(MOVE_FORWARD || MOVE_BACKWARD || SLIDE_LEFT || SLIDE_RIGHT || LOOK_LEFT || LOOK_RIGHT || LOOK_UP || LOOK_DOWN || MOVE_UP || MOVE_DOWN))
            return;

        if(LOOK_RIGHT || LOOK_UP || LOOK_DOWN || LOOK_LEFT){
            if(LOOK_RIGHT){
                yaw += turnRate;

            }

            //处理向下看， 并保证俯角不大于等于90度
            if(LOOK_LEFT){
                yaw -= turnRate;

            }

            if(LOOK_UP){
                pitch += turnRate;
            }

            // 处理向左看
            if(LOOK_DOWN){
                pitch -= turnRate;
            }

            if (pitch > 89)
                pitch = 89;
            if (pitch < -89)
                pitch = -89;

            //将 Y_angle 和 X_angle 的值限制在 0-359 的范围内
            // pitch = (pitch + 360) % 360;
            yaw = (yaw + 360) % 360;

            // 更新视角的方向
//            viewDirection.setTo(0,0,-1);
//            Vector3dHelper.rotateX(viewDirection, yaw);
//            Vector3dHelper.rotateY(viewDirection, pitch);
//            NormOps_FDF3.normF(viewDirection);
            updateCameraVectors();
        }


        //处理向前移动
        if(MOVE_FORWARD){
            FMatrix3 tmp = new FMatrix3();
            CommonOps_FDF3.scale(moveSpeed, viewDirection, tmp);
            CommonOps_FDF3.addEquals(position, tmp);
        }

        //处理后前移动
        if(MOVE_BACKWARD){
            FMatrix3 tmp = new FMatrix3();
            CommonOps_FDF3.scale(moveSpeed, viewDirection, tmp);
            CommonOps_FDF3.subtractEquals(position, tmp);
        }

        //视角方向与一个向下的矢量的叉积结果为视角需要向左移动的方向
        if(SLIDE_LEFT){
            FMatrix3 tmp = new FMatrix3();
            CommonOps_FDF3.scale(moveSpeed, right, tmp);
            CommonOps_FDF3.subtractEquals(position, tmp);

        }

        //视角方向与一个向上的矢量的叉积结果为视角需要向右移动的方向
        if(SLIDE_RIGHT){
            FMatrix3 tmp = new FMatrix3();
            CommonOps_FDF3.scale(moveSpeed, right, tmp);
            CommonOps_FDF3.addEquals(position, tmp);
        }

        if(MOVE_UP){
            position.a2 += moveSpeed;
        }

        if(MOVE_DOWN){
            position.a2 -= moveSpeed;
        }
        System.out.println("x=" + position.a1 + ",y=" + position.a2 + ",z=" + position.a3);

        updateRotationMatrix();
    }

    public static void updateRotationMatrix(){
        // yaw = x, pitch = y, row = z

        viewMatrix = new FMatrix4x4(1,0,0,0,
            0,1,0,0,
            0,0,1,0,
            0,0,0,1);

        FMatrix3 eye = position;
        FMatrix3 center = new FMatrix3();
        CommonOps_FDF3.add(position, viewDirection, center);
        FMatrix3 up = worldUp;

        FMatrix3 tmp = new FMatrix3();

        CommonOps_FDF3.subtract(center, eye, tmp);
        NormOps_FDF3.normalizeF(tmp);
        FMatrix3 f = new FMatrix3(tmp);
        FMatrix3 s = Vector3dHelper.cross(f, up);
        NormOps_FDF3.normalizeF(s);
        FMatrix3 u = Vector3dHelper.cross(s,f);

        viewMatrix.a11 = s.a1;
        viewMatrix.a12 = s.a2;
        viewMatrix.a13 = s.a3;
        viewMatrix.a21 = u.a1;
        viewMatrix.a22 = u.a2;
        viewMatrix.a23 = u.a3;
        viewMatrix.a31 = -f.a1;
        viewMatrix.a32 = -f.a2;
        viewMatrix.a33 = -f.a3;
        viewMatrix.a14 = -1 * CommonOps_FDF3.dot(s, eye);
        viewMatrix.a24 = -1 * CommonOps_FDF3.dot(u, eye);
        viewMatrix.a34 = CommonOps_FDF3.dot(f, eye);
        // CommonOps_FDF4.transpose(viewMatrix);



    }

    private static void updateCameraVectors(){
        // 		glm::vec3 front;
        //		front.x = cos(glm::radians(Yaw)) * cos(glm::radians(Pitch));
        //		front.y = sin(glm::radians(Pitch));
        //		front.z = sin(glm::radians(Yaw)) * cos(glm::radians(Pitch));
        //		Front = glm::normalize(front);
        //		// also re-calculate the Right and Up vector
        //		Right = glm::normalize(glm::cross(Front, WorldUp));  // normalize the vectors, because their length gets closer to 0 the more you look up or down which results in slower movement.
        //		Up = glm::normalize(glm::cross(Right, Front));
        int positivePitch = (pitch + 360) % 360;

        FMatrix3 newFront = new FMatrix3();
        newFront.a1 = QueryTable.cos[yaw] * QueryTable.cos[positivePitch];
        newFront.a2 = QueryTable.sin[positivePitch];
        newFront.a3 = QueryTable.sin[yaw] * QueryTable.cos[positivePitch];
        NormOps_FDF3.normalizeF(newFront);
        viewDirection.setTo(newFront);

        right.setTo(Vector3dHelper.cross(newFront, worldUp));
        NormOps_FDF3.normalizeF(right);

        up.setTo(Vector3dHelper.cross(right, newFront));
        NormOps_FDF3.normalizeF(up);
    }

}