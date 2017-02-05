package rs.ac.bg.etf.dm130240d.poligon.game;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;

import rs.ac.bg.etf.dm130240d.poligon.R;
import rs.ac.bg.etf.dm130240d.poligon.db.DbModel;
import rs.ac.bg.etf.dm130240d.poligon.interfaces.ViewInterface;
import rs.ac.bg.etf.dm130240d.poligon.statistics.ListStatisticsActivity;

public class GameActivity extends AppCompatActivity implements ViewInterface, SensorEventListener, Serializable {

    private static final String KEY_GAME_CONTROLLER = "rs.ac.bg.etf.dm130240d.poligon.game.GameController";

    private GameView gameView;
    private TextView clockView;

    protected TimerModel model;
    private GameController controller;
    private String mapPath, mapName;
    private boolean paramsCalculated, canSetDefaultSensorPar, canUpdateBall;

    private Sensor mySensor;
    private SensorManager SM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_game);

        paramsCalculated = false;
        canSetDefaultSensorPar = false;
        canUpdateBall = false;

        ActionBar actionBar = this.getSupportActionBar();
        actionBar.hide();

        model = new TimerModel();

        if (savedInstanceState == null) {

            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                mapPath = null;
            } else {
                mapPath = extras.getString("MAP_PLAN");
                mapName = extras.getString("MAP_NAME");
            }
            controller = new GameController(this, mapPath, model, mapName);

            DbModel dbModel = new DbModel(this);
            controller.setDbModel(dbModel);
        } else {
            controller = (GameController) savedInstanceState.getSerializable(KEY_GAME_CONTROLLER);
            controller.setDisplayView(this);
            controller.start_timer();

            DbModel dbModel = new DbModel(this);
            controller.setDbModel(dbModel);

            mapPath = (String) savedInstanceState.getSerializable("MAP_PLAN");
        }

        gameView = (GameView) findViewById(R.id.gameImageView);
        gameView.setController(controller);

        SM = (SensorManager) getSystemService(SENSOR_SERVICE);

        mySensor = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        SM.registerListener(this, mySensor, SensorManager.SENSOR_DELAY_GAME);

        if(mapPath == null){
            Toast.makeText(this, "Došlo je do greške. Molimo Vas, pokušajte ponovo.", Toast.LENGTH_SHORT).show();
            finish();
        }

        clockView = (TextView) findViewById(R.id.gameClockView);
        clockView.setText("00:00:00");

        this.updateView();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(!paramsCalculated) {
            controller.initFields();
            controller.setCorrectParameters(gameView);
            canSetDefaultSensorPar = true;
            paramsCalculated = true;
            controller.start_timer();
            this.updateView();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(KEY_GAME_CONTROLLER, controller);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        this.updateView();
    }

    @Override
    public void updateView() {

        this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                gameView.invalidate();
                String time = model.getTime();
                clockView.setText(time);
                if(controller.isGameOver() && controller.isHasWon()){
                    FragmentManager fm = getSupportFragmentManager();
                    SaveResultFragment dialogFragment = new SaveResultFragment().newInstance(controller.getEndTime());
                    dialogFragment.show(fm, "Save result Fragment");
                }
                if(controller.isGameOver() && !controller.isHasWon()) finish();
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(canSetDefaultSensorPar){
            controller.setDefaultSensorPar(sensorEvent.values[0], sensorEvent.values[1]);
            canSetDefaultSensorPar = false;
            canUpdateBall = true;
        }
        if(canUpdateBall && !controller.isGameOver()) controller.moveBall(sensorEvent.values[0], sensorEvent.values[1]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void doPositiveClick(String username) {
        finish();
        boolean updatedSuccess = controller.saveResult(username);
        if(updatedSuccess) {
            Intent i = new Intent(this, ListStatisticsActivity.class);
            i.putExtra("MAP_PLAN", mapPath);
            i.putExtra("MAP_NAME", mapName);
            startActivity(i);
        }
    }

    public void doNegativeClick() {
        // Do stuff here.
        finish();
    }
}
