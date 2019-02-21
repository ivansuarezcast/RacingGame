package com.mygdx.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

/**
 * Created by Iván on 27/07/2017.
 */

public class Tire {
    Body body;
    int state;
    boolean TDC_LEFT   =false;
    boolean TDC_RIGHT  =false;
    boolean TDC_UP     = false;
    boolean TDC_DOWN   =false;

    float m_maxForwardSpeed=100;  // 100;
    float m_maxBackwardSpeed=-20; // -20;
    float m_maxDriveForce=300;    // 150;
    float maxLateralImpulse = 25;

    public Tire(World world){
        BodyDef bodyDef= new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        //bodyDef.position.set(0,0);
        body = world.createBody(bodyDef);

        PolygonShape polygonShape = new PolygonShape();
        polygonShape.setAsBox(0.5f, 1.25f);
        body.createFixture(polygonShape,1);
        body.setUserData(this);

    }

    public Vector2 getLateralVelocity(){
        Vector2 currentRightNormal = body.getWorldVector(new Vector2(1,0));
        Float dotProd = currentRightNormal.dot(body.getLinearVelocity());
        return new Vector2(currentRightNormal.x*dotProd,currentRightNormal.y*dotProd);
    }

    public Vector2 getForwardVelocity() {
        Vector2 currentForwardNormal = body.getWorldVector(new Vector2(0,1));
        Float dotProd = currentForwardNormal.dot(body.getLinearVelocity());
        return new Vector2(currentForwardNormal.x*dotProd,currentForwardNormal.y*dotProd);
    }

    public void updateFriction() {
        Vector2 impulse = new Vector2(-getLateralVelocity().x*body.getMass(),-getLateralVelocity().y*body.getMass());
        if(impulse.len()>maxLateralImpulse)
            impulse =new Vector2(impulse.x*maxLateralImpulse/impulse.len(),impulse.y*maxLateralImpulse/impulse.len());
        body.applyLinearImpulse(impulse,body.getWorldCenter(),true);
        body.applyAngularImpulse(0.1f*body.getInertia()*(-body.getAngularVelocity()),true);
    }

    public void updateDrive(){
        float desiredSpeed = 0;
        if(TDC_UP){
            desiredSpeed=m_maxForwardSpeed;
            //desiredSpeed=m_maxBackwardSpeed;
        }
        if(TDC_DOWN){
            desiredSpeed=m_maxBackwardSpeed;
        }

        Vector2 currentForwardNormal = body.getWorldVector(new Vector2(0,1));
        float currentSpeed = getForwardVelocity().dot(currentForwardNormal);


        float force = 0;
        if(desiredSpeed>currentSpeed){
            force =m_maxDriveForce;
        }else if(desiredSpeed<currentSpeed) {
            force = -m_maxDriveForce;
        }
        body.applyForce(currentForwardNormal.x*force,currentForwardNormal.y*force,body.getWorldCenter().x,body.getWorldCenter().y,true);
    }
}
