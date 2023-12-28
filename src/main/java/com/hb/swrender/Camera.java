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

    //判断视角走位，观察方向的变量
    public static boolean MOVE_FORWARD, MOVE_BACKWARD, SLIDE_LEFT, SLIDE_RIGHT, LOOK_UP, LOOK_DOWN, LOOK_RIGHT, LOOK_LEFT, MOVE_UP, MOVE_DOWN;

    //视角在Y轴上的旋转， 用来控制向左或向右看
    public static int Y_angle;

    //视角在X轴上的旋转, 用来控制向上或向下看
    public static int X_angle;

    //视角改变观察方向的速率,每频可旋转2度
    public static int turnRate= 2;

    //视角改变位置的速度，每频可移动0.03f个单位长度
    public static float moveSpeed = 0.03f;
    public static FMatrix4x4 viewMatrix;

    //初始化方法
    public static void init(float x, float y, float z){
        position = new FMatrix3(x,y,z);
        viewDirection = new FMatrix3(0, 0, 1);
        viewMatrix = getRotationMatrix();
    }

    //更新视角状态
    public static void update(){
        //处理向上看， 并保证仰角不大于等于90度
        if(!(MOVE_FORWARD || MOVE_BACKWARD || SLIDE_LEFT || SLIDE_RIGHT || LOOK_LEFT || LOOK_RIGHT || LOOK_UP || LOOK_DOWN || MOVE_UP || MOVE_DOWN))
            return;
        if(LOOK_UP){
            X_angle+=turnRate;
            if(X_angle > 89 && X_angle < 180)
                X_angle = 89;
        }

        //处理向下看， 并保证俯角不大于等于90度
        if(LOOK_DOWN){
            X_angle-=turnRate;
            if(X_angle < 271 && X_angle > 180)
                X_angle = -89;
        }

        // 处理向右看
        if(LOOK_RIGHT){
            Y_angle+=turnRate;
        }

        // 处理向左看
        if(LOOK_LEFT){
            Y_angle-=turnRate;
        }

        //将 Y_angle 和 X_angle 的值限制在 0-359 的范围内
        Y_angle = (Y_angle + 360) % 360;
        X_angle = (X_angle + 360) % 360;

        //更新视角的方向
        viewDirection.setTo(0,0,1);
        Vector3dHelper.rotateX(viewDirection, X_angle);
        Vector3dHelper.rotateY(viewDirection, Y_angle);
        NormOps_FDF3.normF(viewDirection);

        //处理向前移动
        if(MOVE_FORWARD){
            FMatrix3 tmp = new FMatrix3();
            CommonOps_FDF3.scale(moveSpeed, viewDirection, tmp);
            CommonOps_FDF3.subtractEquals(position, tmp);
        }

        //处理后前移动
        if(MOVE_BACKWARD){
            FMatrix3 tmp = new FMatrix3();
            CommonOps_FDF3.scale(moveSpeed, viewDirection, tmp);
            CommonOps_FDF3.addEquals(position, tmp);
        }

        //视角方向与一个向下的矢量的叉积结果为视角需要向左移动的方向
        if(SLIDE_LEFT){
            FMatrix3 left = Vector3dHelper.cross(viewDirection, new FMatrix3(0, -1, 0));
            NormOps_FDF3.normF(left);
            FMatrix3 tmp = new FMatrix3();
            CommonOps_FDF3.scale(moveSpeed, left, tmp);
            CommonOps_FDF3.subtractEquals(position, tmp);

        }

        //视角方向与一个向上的矢量的叉积结果为视角需要向右移动的方向
        if(SLIDE_RIGHT){
            FMatrix3 right = Vector3dHelper.cross(viewDirection, new FMatrix3(0, 1, 0));
            NormOps_FDF3.normF(right);
            FMatrix3 tmp = new FMatrix3();
            CommonOps_FDF3.scale(moveSpeed, right, tmp);
            CommonOps_FDF3.subtractEquals(position, tmp);
        }

        if(MOVE_UP){
            position.a2 += moveSpeed;
        }

        if(MOVE_DOWN){
            position.a2 -= moveSpeed;
        }

        viewMatrix = getRotationMatrix();
    }

    public static FMatrix4x4 getRotationMatrix(){
        // yaw = x, pitch = y, row = z
        FMatrix4x4 yaw = new FMatrix4x4(
                1,0,0,-position.a1,
                0, QueryTable.cos[X_angle], QueryTable.sin[X_angle],-position.a2,
                0, -QueryTable.sin[X_angle], QueryTable.cos[X_angle], -position.a3,
                0,0,0,1);

        FMatrix4x4 pitch = new FMatrix4x4(QueryTable.cos[Y_angle],0,QueryTable.sin[Y_angle],0,
                0, 1, 0, 0,
                -QueryTable.sin[Y_angle], 0, QueryTable.cos[Y_angle], 0,
                0,0,0,1);

        //FMatrix4x4 yaw = new FMatrix4x4(QueryTable.cos[X_angle], 0, -QueryTable.sin[X_angle], 0,
        //        0,1,0,0,
        //        QueryTable.sin[X_angle],0, QueryTable.cos[Y_angle], 0,
        //        0,0,0,1);
        FMatrix4x4 ans = new FMatrix4x4();
        CommonOps_FDF4.mult(yaw, pitch, ans);
        return ans;

    }
}