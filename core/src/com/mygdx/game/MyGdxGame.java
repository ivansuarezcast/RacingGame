package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class MyGdxGame extends ApplicationAdapter {
	SpriteBatch batch;
	OrthographicCamera camera;

	Texture img;
	Sprite carSprite;
	TiledMap tiledMap;
	TiledMapRenderer tiledMapRenderer;

	World world;
	Box2DDebugRenderer debugRenderer;
	TDCar tdCar;

	int PPM = 10;

	@Override
	public void create () {

		camera = new OrthographicCamera();
		camera.setToOrtho(false, Gdx.graphics.getWidth()/PPM , Gdx.graphics.getHeight()/PPM);

		camera.update();

		batch = new SpriteBatch();
		img = new Texture("car.png");
		world = new World(new Vector2(0, 0), true);


		world.setContactListener(new ListenerClass());
		debugRenderer = new Box2DDebugRenderer();
		tdCar = new TDCar(world);

		carSprite = new Sprite(img);
		carSprite.setSize(5,10);
		carSprite.setOrigin(carSprite.getWidth()/2, carSprite.getHeight()/2);

		tiledMap = new TmxMapLoader().load("untitled.tmx");
		tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, (float)1/PPM);
		tiledMapRenderer.setView(camera);

		//Map objects
		BodyDef bdef = new BodyDef();
		PolygonShape shape = new PolygonShape();
		FixtureDef fdef = new FixtureDef();
		Body body;
		// tiledMap.getLayers().getCount();
		for(MapObject object: tiledMap.getLayers().get(1).getObjects().getByType(RectangleMapObject.class)){
			Rectangle rect = ((RectangleMapObject)object).getRectangle();

			bdef.type = BodyDef.BodyType.StaticBody;
			bdef.position.set((rect.getX()+rect.getWidth()/2)/PPM,(rect.getY()+rect.getHeight()/2)/PPM);
			body = world.createBody(bdef);
			shape.setAsBox(rect.getWidth()/2/PPM, rect.getHeight()/2/PPM);
			fdef.shape=shape;
			body.createFixture(fdef);

		}
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		camera.update();


		batch.setProjectionMatrix(camera.combined); //or your matrix to draw GAME WORLD, not UI
		tiledMapRenderer.setView(camera);
		tiledMapRenderer.render();
		camera.position.set(tdCar.getX(),tdCar.getY(),0);
		batch.begin();

		carSprite.setX(tdCar.getX()-carSprite.getWidth()/2);
		carSprite.setY(tdCar.getY()-carSprite.getHeight()/2);
		carSprite.setRotation((float)Math.toDegrees(tdCar.body.getAngle()));

		carSprite.draw(batch);
		//batch.draw(img, 0, 0);
		batch.end();
		debugRenderer.render(world, camera.combined);
		debugRenderer.setDrawVelocities(true);

		if (Gdx.input.isKeyPressed(Input.Keys.W)) {
			tdCar.accelerateTrue();
		}else{
			tdCar.accelerateFalse();
		}
		if (Gdx.input.isKeyPressed(Input.Keys.A)) {
			tdCar.TCRleft=true;
		}else{
			tdCar.TCRleft=false;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.S)) {
			//tdCar.accelerateFalse();
			tdCar.brakeTrue();

		}else{
			tdCar.brakeFalse();
		}
		if (Gdx.input.isKeyPressed(Input.Keys.D)) {
			tdCar.TCRright=true;
		}else{
			tdCar.TCRright=false;
		}

		if ((!Gdx.input.isKeyPressed(Input.Keys.D))&&(!Gdx.input.isKeyPressed(Input.Keys.A))) {
		}
		if(Gdx.input.isTouched()){
			if(Gdx.input.getX()<500){
				tdCar.accelerateFalse();
				tdCar.reverseTrue();
			}else{
				tdCar.reverseFalse();
				tdCar.accelerateTrue();
			}

			Gdx.app.log("acelX",Gdx.input.getAccelerometerX()+"");
			Gdx.app.log("acelY",Gdx.input.getAccelerometerY()+"");//this one for turning -10 10
			Gdx.app.log("acelZ",Gdx.input.getAccelerometerZ()+"");
			Gdx.app.log("X",Gdx.input.getX()+"");
		}else if((!(Gdx.input.isKeyPressed(Input.Keys.W)))&&(!(Gdx.input.isKeyPressed(Input.Keys.S)))){
			tdCar.accelerateFalse();
			tdCar.reverseFalse();
		}
		if(Gdx.input.getAccelerometerY()>0){
			tdCar.TCRleft=false;
			tdCar.TCRright=true;
			tdCar.tireAngle=Gdx.input.getAccelerometerY()*3.5f;
		}
		if(Gdx.input.getAccelerometerY()<0){
			tdCar.TCRright=false;
			tdCar.TCRleft=true;
			tdCar.tireAngle=-Gdx.input.getAccelerometerY()*3.5f;
		}

		tdCar.update();
		world.step(1/60f, 6, 2);
		world.clearForces();
	}

	@Override
	public void dispose () {
		batch.dispose();
		img.dispose();
	}

	private class ListenerClass implements ContactListener {
		@Override
		public void beginContact(Contact contact) {

		}

		@Override
		public void endContact(Contact contact) {

		}

		@Override
		public void preSolve(Contact contact, Manifold oldManifold) {

		}

		@Override
		public void postSolve(Contact contact, ContactImpulse impulse) {

		}
	}
}
