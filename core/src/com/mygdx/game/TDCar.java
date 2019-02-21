package com.mygdx.game;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.utils.Array;

/**
 * Created by Iván on 27/07/2017.
 */

public class TDCar {
    Body body;
    Array<Tire> tires;
    RevoluteJoint flJoint, frJoint;
    public boolean TCRright, TCRleft;
    public float tireAngle=35;
    float[] gears = {2.66f,1.78f,1.3f,1f,0.74f,0.50f};

    public TDCar(World world){
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        //bodyDef.position.set(40,100);
        body = world.createBody(bodyDef);

        PolygonShape polygonShape = new PolygonShape();
        polygonShape.setAsBox(2f, 4f);
        body.createFixture(polygonShape,1);
        body.setUserData(this);

        tires = new Array<Tire>();

        RevoluteJointDef jointDef=new RevoluteJointDef();

        jointDef.bodyA = body;
        jointDef.enableLimit = true;
        jointDef.lowerAngle = 0;//with both these at zero...
        jointDef.upperAngle = 0;//...the joint will not move
        jointDef.localAnchorB.setZero();//joint anchor in tire is always center

        Tire tire = new Tire(world);
        jointDef.bodyB = tire.body;
        jointDef.localAnchorA.set(-2f,-2f);
        world.createJoint(jointDef);
        tires.add(tire);



        Tire tire2 = new Tire(world);
        jointDef.bodyB = tire2.body;
        jointDef.localAnchorA.set(2f,-2f);
        world.createJoint(jointDef);
        tires.add(tire2);

        Tire tire3 = new Tire(world);
        jointDef.bodyB = tire3.body;
        jointDef.localAnchorA.set(2f,2f);
        flJoint= (RevoluteJoint) world.createJoint(jointDef);
        tires.add(tire3);

        Tire tire4 = new Tire(world);
        jointDef.bodyB = tire4.body;
        jointDef.localAnchorA.set(-2f,2f);
        frJoint= (RevoluteJoint) world.createJoint(jointDef);
        tires.add(tire4);



    }
    public void update() {
        for(Tire t : tires){
            t.updateFriction();
            t.updateDrive();
        }


        float lockAngle = (float)Math.toRadians(tireAngle);//  35 * DEGTORAD;
        float turnSpeedPerSec = (float)Math.toRadians(160);//320 * DEGTORAD;//from lock to lock in 0.25 sec
        float turnPerTimeStep = turnSpeedPerSec / 60.0f;
        float desiredAngle = 0;
       /* switch ( controlState & (TDCR_LEFT|TDCR_RIGHT) ) {
            case TDCR_LEFT:  desiredAngle = lockAngle;  break;
            case TDCR_RIGHT: desiredAngle = -lockAngle; break;
            default: ;//nothing
        }*/
        if(TCRleft){
            desiredAngle = lockAngle;
        }
        if(TCRright){
            desiredAngle = -lockAngle;
        }
        float angleNow = flJoint.getJointAngle();
        float angleToTurn = desiredAngle - angleNow;
        angleToTurn = Math.max(-turnPerTimeStep,Math.min(angleToTurn,turnPerTimeStep));
        float newAngle = angleNow + angleToTurn;
        flJoint.setLimits( newAngle, newAngle );
        frJoint.setLimits( newAngle, newAngle );

    }

    public void accelerateTrue(){
        for(Tire t : tires){
            t.TDC_UP = true;
        }
    }
    public void accelerateFalse(){
        for(Tire t : tires){
            t.TDC_UP = false;
        }
    }
    public void reverseTrue(){
        for(Tire t : tires){
            t.TDC_DOWN = true;
        }
    }

    public void reverseFalse(){
        for(Tire t : tires){
            t.TDC_DOWN = false;
        }
    }

    public void brakeTrue(){
        for(Tire t : tires){
            t.TDC_DOWN = true;
        }
    }
    public void brakeFalse(){
        for(Tire t : tires){
            t.TDC_DOWN = false;
        }
    }

    public float getX(){
        return body.getPosition().x;
    }

    public float getY(){
        return body.getPosition().y;
    }
}
