package rs.ac.bg.etf.dm130240d.poligon.crtanje_poligona;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.File;

import rs.ac.bg.etf.dm130240d.poligon.ParameterCord;
import rs.ac.bg.etf.dm130240d.poligon.R;
import rs.ac.bg.etf.dm130240d.poligon.interfaces.ViewInterface;

public class DrawPoligonActivity extends AppCompatActivity implements ViewInterface {

    private static final String KEY_CONTROLLER = "rs.ac.bg.etf.dm130240d.poligon.crtanje_poligona.DrawingController";

    private DrawingView imageView;
    private DrawingController controller;
    private boolean startWall, endWall;
    private ParameterCord startWallCord, endWallCord;
    private double maxDist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw_poligon);

        final ActionBar actionBar = this.getSupportActionBar();
        actionBar.setTitle("Dodavanje poligona");

        if (savedInstanceState == null) {
            controller = new DrawingController(this);
        } else {
            controller = (DrawingController) savedInstanceState.getSerializable(KEY_CONTROLLER);
            controller.setDisplayView(this);
        }

        imageView = (DrawingView) findViewById(R.id.drawPoligonView);
        imageView.setController(controller);

        startWall = endWall = false;
        startWallCord = endWallCord = null;
        maxDist = 0;

        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (controller.getCurrentMode()){
                    case 0:
                        startWall = endWall = false;
                        startWallCord = endWallCord = null;
                        maxDist = 0;
                        break;
                    case 4:
                        startWall = endWall = false;
                        startWallCord = endWallCord = null;
                        maxDist = 0;
                        if(motionEvent.getAction() == MotionEvent.ACTION_UP) controller.setStartHole(motionEvent.getX(), motionEvent.getY(), imageView, actionBar.getHeight());
                        break;
                    case 1:
                        startWall = endWall = false;
                        startWallCord = endWallCord = null;
                        maxDist = 0;
                        if(motionEvent.getAction() == MotionEvent.ACTION_UP) controller.setTrueHole(motionEvent.getX(), motionEvent.getY(), imageView, actionBar.getHeight());
                        break;
                    case 2:
                        startWall = endWall = false;
                        startWallCord = endWallCord = null;
                        maxDist = 0;
                        if(motionEvent.getAction() == MotionEvent.ACTION_UP) controller.setFalseHole(motionEvent.getX(), motionEvent.getY(), imageView, actionBar.getHeight());
                        break;
                    case 3:

                        if(!startWall && motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                            startWallCord = new ParameterCord(motionEvent.getX(), motionEvent.getY());
                            startWall = true;
                            break;
                        }

                        final float x = motionEvent.getX();
                        final float y = motionEvent.getY();

                        final float dx = x - startWallCord.x;
                        final float dy = y - startWallCord.y;

                        double result = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));

                        if(result >= 100 && result >= maxDist){
                            maxDist = result;
                            endWallCord = new ParameterCord(motionEvent.getX(), motionEvent.getY());
                            endWall = true;
                        }

                        if(startWall && motionEvent.getAction() == MotionEvent.ACTION_UP && !endWall){
                            startWallCord = endWallCord = null;
                            startWall = endWall = false;
                            maxDist = 0;
                            break;
                        }

                        if(startWall && motionEvent.getAction() == MotionEvent.ACTION_UP && endWall){
                            controller.setZid(startWallCord, endWallCord);

                            startWallCord = endWallCord = null;
                            startWall = endWall = false;
                            maxDist = 0;
                            break;
                        }
                        break;
                    default:
                        startWall = endWall = false;
                        startWallCord = endWallCord = null;
                        maxDist = 0;
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(KEY_CONTROLLER, controller);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.select_mode_draw, menu);
        switch (controller.getCurrentMode()){
            case 0:
                menu.findItem(R.id.nista_option).setChecked(true);
                break;
            case 1:
                menu.findItem(R.id.true_rupa_option).setChecked(true);
                break;
            case 2:
                menu.findItem(R.id.false_rupa_option).setChecked(true);
                break;
            case 3:
                menu.findItem(R.id.zid_option).setChecked(true);
                break;
            default:
                menu.findItem(R.id.nista_option).setChecked(true);
                break;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()){
            case R.id.nista_option:
                controller.setCurrentMode(0);
                item.setChecked(true);
                return true;
            case R.id.start_ball_option:
                controller.setCurrentMode(4);
                item.setChecked(true);
                return true;
            case R.id.true_rupa_option:
                controller.setCurrentMode(1);
                item.setChecked(true);
                return true;
            case R.id.false_rupa_option:
                controller.setCurrentMode(2);
                item.setChecked(true);
                return true;
            case R.id.zid_option:
                controller.setCurrentMode(3);
                item.setChecked(true);
                return true;
            case R.id.save_draw_option:
                item.setChecked(true);
                String state = Environment.getExternalStorageState();
                if(Environment.MEDIA_MOUNTED.equals(state)){
                    File root = Environment.getExternalStorageDirectory();
                    boolean canFinish = controller.save(root, imageView);
                    if(canFinish) finish();
                } else {
                    Toast.makeText(this, "Greška: aplikacija nije spremna za kreiranje fajla.", Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.cancel_draw_option:
                finish();
                return true;
            case R.id.draw_undo_option:
                controller.undo();
                return true;
            case R.id.draw_erase_option:
                controller.eraseAll();
                return true;
            default:
                return true;
        }
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
        imageView.invalidate();
    }
}
