package rs.ac.bg.etf.dm130240d.poligon.game;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;

import rs.ac.bg.etf.dm130240d.poligon.ParameterCord;
import rs.ac.bg.etf.dm130240d.poligon.db.DbModel;
import rs.ac.bg.etf.dm130240d.poligon.interfaces.ViewInterface;

/**
 * Created by Marko on 2/1/2017.
 */

public class GameController implements Serializable {

    private String mapPath, mapName;
    private ParameterCord currentBall, trueHole, startHole, imageViewCord, startSensorCord, lastRemainder;
    private ArrayList<ParameterCord> falseHoles, startWall, endWall;
    private transient ViewInterface displayView;
    private boolean gameOver = false;
    private boolean hasWon = false;
    private transient DbModel dbModel;
    private boolean sqlInsertResult;
    private int otpor;
    private float hitWallLeft, hitWallRight, hitWallTop, hitWallBottom;
    private float currentHitWallLeft, currentHitWallRight, currentHitWallTop, currentHitWallBottom;

    protected static final int SLEEP_INTERVAL = 50;

    protected Thread worker;

    protected TimerModel model;

    public GameController(ViewInterface displayView, String mapPath, TimerModel m, String mapName){

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences((AppCompatActivity)displayView);
        otpor = sp.getInt("otpor", 5);

        this.displayView = displayView;
        this.mapPath = mapPath;
        this.mapName = mapName;

        falseHoles = new ArrayList<ParameterCord>();
        startWall = new ArrayList<ParameterCord>();
        endWall = new ArrayList<ParameterCord>();

        currentBall = new ParameterCord(0, 0);
        trueHole = new ParameterCord(0, 0);
        startHole = new ParameterCord(0, 0);
        lastRemainder = new ParameterCord(0, 0);

        hitWallLeft = hitWallTop = hitWallRight = hitWallBottom = 0;
        currentHitWallLeft = currentHitWallTop = currentHitWallRight = currentHitWallBottom = 0;

        model = m;

        worker = null;
    }

    public void setDisplayView(ViewInterface displayView) {
        this.displayView = displayView;
    }

    public void setDbModel(DbModel dbModel) {
        this.dbModel = dbModel;
    }

    public ParameterCord getCurrentBall() {
        return currentBall;
    }

    public ParameterCord getTrueHole() {
        return trueHole;
    }

    public ParameterCord getStartHole() {
        return startHole;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean isHasWon() {
        return hasWon;
    }

    public ArrayList<ParameterCord> getFalseHoles() {
        return falseHoles;
    }

    public ArrayList<ParameterCord> getStartWall() {
        return startWall;
    }

    public ArrayList<ParameterCord> getEndWall() {
        return endWall;
    }

    public void initFields(){

        File readDir = new File(mapPath);
        if(readDir.exists()){
            File readFile = readDir.listFiles()[0];
            if(readFile.exists()){
                try{
                    FileInputStream fileInputStream = new FileInputStream(readFile);
                    ObjectInputStream inputStreamReader = new ObjectInputStream(fileInputStream);

                    startHole = (ParameterCord) inputStreamReader.readObject();
                    trueHole = (ParameterCord) inputStreamReader.readObject();
                    falseHoles = (ArrayList<ParameterCord>) inputStreamReader.readObject();
                    startWall = (ArrayList<ParameterCord>) inputStreamReader.readObject();
                    endWall = (ArrayList<ParameterCord>) inputStreamReader.readObject();

                    inputStreamReader.close();
                    fileInputStream.close();

                    currentBall.x = startHole.x;
                    currentBall.y = startHole.y;

                } catch(FileNotFoundException ex){
                    ex.printStackTrace();
                } catch (IOException ex){
                    ex.printStackTrace();
                } catch (Exception ex){
                    ex.printStackTrace();
                }

            }
        }

    }

    public void setDefaultSensorPar(float x, float y){
        startSensorCord = new ParameterCord(x, y);
    }

    public void setCorrectParameters(GameView imageView){
        try{

            this.imageViewCord = new ParameterCord(imageView.getWidth(), imageView.getHeight());

            startHole.x = (startHole.x * imageView.getWidth()) / 100;
            startHole.y = (startHole.y * imageView.getHeight()) / 100;

            currentBall.x = startHole.x;
            currentBall.y = startHole.y;

            trueHole.x = (trueHole.x * imageView.getWidth()) / 100;
            trueHole.y = (trueHole.y * imageView.getHeight()) / 100;

            for(ParameterCord pc: falseHoles){

                pc.x = (pc.x * imageView.getWidth()) / 100;
                pc.y = (pc.y * imageView.getHeight()) / 100;
            }

            for(int i = 0; i < startWall.size(); i++){

                startWall.get(i).x = (startWall.get(i).x * imageView.getWidth()) / 100;
                startWall.get(i).y = (startWall.get(i).y * imageView.getHeight()) / 100;

                endWall.get(i).x = (endWall.get(i).x * imageView.getWidth()) / 100;
                endWall.get(i).y = (endWall.get(i).y * imageView.getHeight()) / 100;
            }

        } catch (Exception ex){ }
    }

    public void moveBall(float x, float y){

        float nextX = (x - startSensorCord.x) + lastRemainder.x;
        float nextY = (y - startSensorCord.y) + lastRemainder.y;

        if(hitWallTop != 0 || hitWallBottom != 0){
            if(hitWallTop != 0){
                nextX = hitWallTop > 0 ? currentHitWallTop++ : currentHitWallTop--;
                if(hitWallTop == currentHitWallTop) currentHitWallTop = hitWallTop = 0;
            }

            if(hitWallBottom != 0){
                nextX = hitWallBottom > 0 ? currentHitWallBottom++ : currentHitWallBottom--;
                if(hitWallBottom == currentHitWallBottom) currentHitWallBottom = hitWallBottom = 0;
            }
        }

        if(hitWallLeft != 0 || hitWallRight != 0){
            if(hitWallLeft != 0){
                nextX = hitWallLeft > 0 ? currentHitWallLeft++ : currentHitWallLeft--;
                if(hitWallLeft == currentHitWallLeft) currentHitWallLeft = hitWallLeft = 0;
            }

            if(hitWallRight != 0){
                nextX = hitWallRight > 0 ? currentHitWallRight++ : currentHitWallRight--;
                if(hitWallRight == currentHitWallRight) currentHitWallRight = hitWallRight = 0;
            }
        }

        lastRemainder.x = nextX;
        lastRemainder.y = nextY;

        float testX = currentBall.x + nextY;
        float testY = currentBall.y + nextX;

        boolean canChangeX = true;
        boolean canChangeY = true;

        float distanceTrueHole = (float) Math.sqrt(Math.pow((testX - trueHole.x), 2) + Math.pow((testY - trueHole.y), 2));
        if((distanceTrueHole + 75) <= 90){
            Toast.makeText((AppCompatActivity)displayView, "CESTITAMO!", Toast.LENGTH_SHORT).show();
            currentBall.x = 0;
            currentBall.y = 0;
            lastRemainder.x = 0;
            lastRemainder.y = 0;
            gameOver = true;
            hasWon = true;
            stop_timer();
            displayView.updateView();
            return;
        }

        for(ParameterCord pc: falseHoles){
            float distanceFalseHole = (float) Math.sqrt(Math.pow((testX - pc.x), 2) + Math.pow((testY - pc.y), 2));
            if((distanceFalseHole + 75) <= 90){
                Toast.makeText((AppCompatActivity)displayView, "GAME OVER!", Toast.LENGTH_SHORT).show();
                currentBall.x = 0;
                currentBall.y = 0;
                lastRemainder.x = 0;
                lastRemainder.y = 0;
                gameOver = true;
                hasWon = false;
                stop_timer();
                displayView.updateView();
                return;
            }
        }

        if((nextX + currentBall.y) <= 75){ nextX *= -1; lastRemainder.x = 0; hitWallTop = nextX < 0 ? 2 : -2; currentHitWallTop = 0; currentBall.y = 75; canChangeY = false; }
        if((nextX + currentBall.y + 75) >= imageViewCord.y){ nextX *= -1; lastRemainder.x = 0; hitWallBottom = nextX < 0 ? 2 : -2; currentHitWallBottom = 0;  currentBall.y = imageViewCord.y - 75; canChangeY = false; }


        if((nextY + currentBall.x) <= 75){ nextY *= -1; lastRemainder.y = 0; hitWallLeft = nextY < 0 ? 2 : -2; currentHitWallLeft = 0; currentBall.x = 75; canChangeX = false; }
        if((nextY + currentBall.x + 75) >= imageViewCord.x){ nextY *= -1; lastRemainder.y = 0; hitWallRight = nextY < 0 ? 2 : -2; currentHitWallRight = 0; currentBall.x = imageViewCord.x - 75; canChangeX = false; }

        if(!canChangeX && !canChangeY){ displayView.updateView(); return; }

        for(int i = 0; i < startWall.size(); i++){

            float left = startWall.get(i).x > endWall.get(i).x ? endWall.get(i).x : startWall.get(i).x;
            float top = startWall.get(i).y > endWall.get(i).y ? endWall.get(i).y : startWall.get(i).y;
            float right = startWall.get(i).x > endWall.get(i).x ? startWall.get(i).x : endWall.get(i).x;
            float bottom = startWall.get(i).y > endWall.get(i).y ? startWall.get(i).y : endWall.get(i).y;

            if(canChangeY && ((nextY + currentBall.x) < right) && ((nextY + currentBall.x) > left)){
                if(((nextX + currentBall.y) >= (top - 75)) && ((nextX + currentBall.y) <= (75 + bottom))){

                    if((currentBall.y <= ((top + ((bottom - top)/2)) - 75))){
                        nextX *= -1;
                        lastRemainder.x = nextX < 0 ? 2 : -2;
                        currentBall.y = top - 75;
                    }
                    if(currentBall.y >= ((top + ((bottom - top)/2)) - 75)){
                        nextX *= -1;
                        lastRemainder.x = nextX < 0 ? 2 : -2;
                        currentBall.y = bottom + 75;
                    }
                    canChangeY = false;
                }
            }

            float helpY = canChangeY ? nextX + currentBall.y : currentBall.y;

            if(canChangeX && (helpY < bottom) && (helpY > top)){
                if(((nextY + currentBall.x) >= (left - 75)) && ((nextY + currentBall.x) <= (75 + right))){

                    if(currentBall.x <= ((left + ((right - left)/2)) - 75)){
                        nextY *= -1;
                        lastRemainder.y = nextY < 0 ? 2 : -2;
                        currentBall.x = left - 75;
                    }
                    if(currentBall.x >= ((left + ((right - left)/2)) - 75)){
                        nextY *= -1;
                        lastRemainder.y = nextY < 0 ? 2 : -2;
                        currentBall.x = right + 75;
                    }
                    canChangeX = false;
                }
            }
        }

        if(!canChangeX && !canChangeY){ displayView.updateView(); return; }

        boolean foundAngle = false;

        for(int i = 0; i < startWall.size(); i++) {

            boolean angleChecked = false;

            float left = startWall.get(i).x > endWall.get(i).x ? endWall.get(i).x : startWall.get(i).x;
            float top = startWall.get(i).y > endWall.get(i).y ? endWall.get(i).y : startWall.get(i).y;
            float right = startWall.get(i).x > endWall.get(i).x ? startWall.get(i).x : endWall.get(i).x;
            float bottom = startWall.get(i).y > endWall.get(i).y ? startWall.get(i).y : endWall.get(i).y;

            ParameterCord topLeft = new ParameterCord(left, top);
            ParameterCord topRight = new ParameterCord(right, top);
            ParameterCord bottomRight = new ParameterCord(right, bottom);
            ParameterCord bottomLeft = new ParameterCord(left, bottom);

            if (!angleChecked) {

                float distance = (float) Math.sqrt(Math.pow((testX - topLeft.x), 2) + Math.pow((testY - topLeft.y), 2));

                if (distance <= 75 && testX <= left && testY <= top) {

                    if(foundAngle){
                        canChangeX = false;
                        canChangeY = false;
                        break;
                    }

                    if(!canChangeX) canChangeY = false;
                    if(!canChangeY) canChangeX = false;

                    if((canChangeX || canChangeY) && !foundAngle){

                        float duzinaX = ((topLeft.x - testX) * ((float) 75.1)) / distance;
                        float noviX = topLeft.x - duzinaX;

                        float duzinaY = ((topLeft.y - testY) * ((float) 75.1)) / distance;
                        float noviY = topLeft.y - duzinaY;

                        if(canChangeX){ currentBall.x = noviX; canChangeX = false; }
                        if(canChangeY){ currentBall.y = noviY; canChangeY = false; }
                    }

                    angleChecked = true;
                    foundAngle = true;
                }
            }

            if (!angleChecked) {

                float distance = (float) Math.sqrt(Math.pow((testX - topRight.x), 2) + Math.pow((testY - topRight.y), 2));

                if (distance <= 75 && testX >= right && testY <= top) {

                    if(foundAngle){
                        canChangeX = false;
                        canChangeY = false;
                        break;
                    }

                    if(!canChangeX) canChangeY = false;
                    if(!canChangeY) canChangeX = false;

                    if((canChangeX || canChangeY) && !foundAngle){

                        float duzinaX = ((testX - topRight.x) * ((float) 75.1)) / distance;
                        float noviX = topRight.x + duzinaX;

                        float duzinaY = ((topRight.y - testY) * ((float) 75.1)) / distance;
                        float noviY = topRight.y - duzinaY;

                        if(canChangeX){ currentBall.x = noviX; canChangeX = false; }
                        if(canChangeY){ currentBall.y = noviY; canChangeY = false; }
                    }

                    angleChecked = true;
                    foundAngle = true;
                }
            }

            if (!angleChecked) {

                float distance = (float) Math.sqrt(Math.pow((testX - bottomRight.x), 2) + Math.pow((testY - bottomRight.y), 2));

                if (distance <= 75 && testX >= right && testY >= bottom) {

                    if(foundAngle){
                        canChangeX = false;
                        canChangeY = false;
                        break;
                    }

                    if(!canChangeX) canChangeY = false;
                    if(!canChangeY) canChangeX = false;

                    if((canChangeX || canChangeY) && !foundAngle){

                        float duzinaX = ((testX - bottomRight.x) * ((float) 75.1)) / distance;
                        float noviX = bottomRight.x + duzinaX;

                        float duzinaY = ((testY - bottomRight.y) * ((float) 75.1)) / distance;
                        float noviY = bottomRight.y + duzinaY;

                        if(canChangeX){ currentBall.x = noviX; canChangeX = false; }
                        if(canChangeY){ currentBall.y = noviY; canChangeY = false; }
                    }

                    angleChecked = true;
                    foundAngle = true;
                }
            }

            if (!angleChecked) {

                float distance = (float) Math.sqrt(Math.pow((testX - bottomLeft.x), 2) + Math.pow((testY - bottomLeft.y), 2));

                if (distance <= 75 && testX <= left && testY >= bottom) {

                    if(foundAngle){
                        canChangeX = false;
                        canChangeY = false;
                        break;
                    }

                    if(!canChangeX) canChangeY = false;
                    if(!canChangeY) canChangeX = false;

                    if((canChangeX || canChangeY) && !foundAngle){

                        float duzinaX = ((bottomLeft.x - testX) * ((float) 75.1)) / distance;
                        float noviX = bottomLeft.x - duzinaX;

                        float duzinaY = ((testY - bottomRight.y) * ((float) 75.1)) / distance;
                        float noviY = bottomRight.y + duzinaY;

                        if(canChangeX){ currentBall.x = noviX; canChangeX = false; }
                        if(canChangeY){ currentBall.y = noviY; canChangeY = false; }
                    }

                    angleChecked = true;
                    foundAngle = true;
                }
            }
        }
        if(!canChangeX && !canChangeY){ displayView.updateView(); return; }

        float otporX, otporY;
        if(nextX == 0) otporX = 0;
        else otporX = nextX > 0 ? ((float)-0.1) : ((float)0.1);

        if(nextY == 0) otporY = 0;
        else otporY = nextY > 0 ? ((float)-0.1) : ((float)0.1);

        if(canChangeY){ currentBall.y += (nextX);} //+ (otporX*otpor)); }
        if(canChangeX){ currentBall.x += (nextY);} //+ (otporY*otpor)); }

        displayView.updateView();
    }

    public void start_timer() {

        if (model.start(System.currentTimeMillis())) {
            displayView.updateView();
        }

        if (worker == null) {
            worker = new Thread(){
                @Override
                public void run() {
                    while(!(GameController.this.isFinished())){
                        tick();
                        try {
                            sleep(SLEEP_INTERVAL);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }
            };
            worker.start();
        }

    }

    public void pause_timer() {

        if (model.pause(System.currentTimeMillis())) {
            displayView.updateView();
        }

    }

    public void stop_timer() {

        if (model.stop(System.currentTimeMillis())){
            displayView.updateView();
        }

        if (worker != null) {
            worker.interrupt();
            worker = null;
        }
    }

    protected void tick() {
        if (model.tick(System.currentTimeMillis())) {
            displayView.updateView();
        }
    }

    protected boolean isFinished() {

        return model.isStopped();
    }

    public String getEndTime(){
        return model.getTime();
    }

    public void resetTimer() { model.resetTime(0); }

    public boolean saveResult(String username){
        sqlInsertResult = true;
        final int finalTime = model.getTimeInSec();
        (new AsyncTask<Object, Void, Long>() {

            @Override
            protected Long doInBackground(Object... params) {
                try{

                    return dbModel.insert((String) params[0], (String) params[1], finalTime);
                } catch (Exception ex) {
                    Toast.makeText((Activity)displayView, "Došlo je do greške kod formatiranja vremena.", Toast.LENGTH_LONG).show();
                    sqlInsertResult = false;
                    return null;
                }

            }

            @Override
            protected void onPostExecute(Long result) {
                super.onPostExecute(result);
                if(result != null){
                    if (result >= 0) {
                        Toast.makeText((Activity)displayView, "Ubačen novi score sa id-em: " + result + ".", Toast.LENGTH_LONG).show();
                    } else sqlInsertResult = false;
                } else sqlInsertResult = false;
            }
        }).execute(mapName, username);

        return sqlInsertResult;
    }

}
