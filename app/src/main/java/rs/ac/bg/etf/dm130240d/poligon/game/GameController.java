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

    private static final float NS2S = 1.0f / 1000000000.0f;

    private String mapPath, mapName;
    private ParameterCord currentBall, oldBall, trueHole, startHole, imageViewCord, startSensorCord, lastRemainder;
    private ParameterCord speedStart;
    private ArrayList<ParameterCord> falseHoles, startWall, endWall;
    private transient ViewInterface displayView;
    private boolean gameOver = false;
    private boolean hasWon = false;
    private boolean canMoveBall = false;
    private boolean rotating;
    private transient DbModel dbModel;
    private boolean sqlInsertResult;
    private int otpor, odbijanje;
    private float izracunatOtpor, izracunatoOdbijanje;
    private double ballStopBorder;
    private long lastTime, lastRotatingTime;
    //private MediaPlayer hitWallSound;

    protected static final int SLEEP_INTERVAL = 50;

    protected transient Thread worker;

    protected TimerModel model;

    public GameController(ViewInterface displayView, String mapPath, TimerModel m, String mapName){

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences((AppCompatActivity)displayView);
        otpor = sp.getInt("otpor", 5);
        odbijanje = sp.getInt("faktor_odbijanja", 5);

        izracunatOtpor = ((float)1)/otpor;
        izracunatoOdbijanje = (15 + ((float)odbijanje * 5)) / 100;

        this.displayView = displayView;
        this.mapPath = mapPath;
        this.mapName = mapName;

        falseHoles = new ArrayList<ParameterCord>();
        startWall = new ArrayList<ParameterCord>();
        endWall = new ArrayList<ParameterCord>();

        currentBall = new ParameterCord(0, 0);
        oldBall = new ParameterCord(0, 0);
        trueHole = new ParameterCord(0, 0);
        startHole = new ParameterCord(0, 0);
        lastRemainder = new ParameterCord(0, 0);

        speedStart = new ParameterCord(0, 0);

        //hitWallSound = MediaPlayer.create((Context)displayView, R.raw.hit_wall);

        model = m;
        canMoveBall = false;
        rotating = false;

        lastRotatingTime = -1;

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
            double konstanta = 0.01;
            if(otpor >= 1 && otpor <= 3) konstanta = 0.03;
            ballStopBorder = (Math.abs(imageViewCord.y - imageViewCord.x)/2)*konstanta;

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

    public boolean isOnWall(float left, float top, float right, float bottom){

        if(oldBall.x <= right && oldBall.x >= left && oldBall.y >= (top - 75) && oldBall.y <= (75 + bottom))
            if(oldBall.y <= top - 73.5 || oldBall.y >= bottom + 73.5) return true;

        if(oldBall.y <= bottom && oldBall.y >= top && oldBall.x >= (left - 75) && oldBall.x <= (75 + right))
            if(oldBall.x <= left - 73.5 || oldBall.x >= right + 73.5) return true;
/*
        ParameterCord topLeft = new ParameterCord(left, top);
        ParameterCord topRight = new ParameterCord(right, top);
        ParameterCord bottomRight = new ParameterCord(right, bottom);
        ParameterCord bottomLeft = new ParameterCord(left, bottom);

        float distance = (float) Math.sqrt(Math.pow((oldBall.x - topLeft.x), 2) + Math.pow((oldBall.y - topLeft.y), 2));
        if (distance <= 78 && oldBall.x < left && oldBall.y < top) return true;

        distance = (float) Math.sqrt(Math.pow((oldBall.x - topRight.x), 2) + Math.pow((oldBall.y - topRight.y), 2));
        if (distance <= 78 && oldBall.x > right && oldBall.y < top) return true;

        distance = (float) Math.sqrt(Math.pow((oldBall.x - bottomRight.x), 2) + Math.pow((oldBall.y - bottomRight.y), 2));
        if (distance <= 78 && oldBall.x > right && oldBall.y > bottom) return true;

        distance = (float) Math.sqrt(Math.pow((oldBall.x - bottomLeft.x), 2) + Math.pow((oldBall.y - bottomLeft.y), 2));
        if (distance <= 78 && oldBall.x < left && oldBall.y > bottom) return true;
*/
        return false;
    }

    public void moveBall(float xx, float yy, long time){

        if(this.canMoveBall) {

            float deltaT = (time - lastTime)*NS2S;
            lastTime = time;

            oldBall.x = currentBall.x;
            oldBall.y = currentBall.y;

            float x = xx * 200;
            float y = yy * 200;

            float nextX = currentBall.x + (speedStart.x * deltaT + (float)(y*Math.pow(deltaT, 2))/2);
            float nextY = currentBall.y + (speedStart.y * deltaT + (float)(x*Math.pow(deltaT, 2))/2);

            speedStart.x += y * deltaT;
            speedStart.y += x * deltaT;

            boolean canChangeX = true;
            boolean canChangeY = true;

            float distanceTrueHole = (float) Math.sqrt(Math.pow((nextX - trueHole.x), 2) + Math.pow((nextY - trueHole.y), 2));
            if((distanceTrueHole + 75) <= 90){
                Toast.makeText((AppCompatActivity)displayView, "Čestitamo, pobedili ste!", Toast.LENGTH_SHORT).show();
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
                float distanceFalseHole = (float) Math.sqrt(Math.pow((nextX - pc.x), 2) + Math.pow((nextY - pc.y), 2));
                if(distanceFalseHole <= 90){
                    Toast.makeText((AppCompatActivity)displayView, "Nažalost, izgubili ste.", Toast.LENGTH_SHORT).show();
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

            if (nextX - 75 < 0) {
                currentBall.x = 75;
                speedStart.x = -speedStart.x * izracunatoOdbijanje;
                canChangeX = false;
                rotating = false;
            }
            if (nextX + 75 >= imageViewCord.x) {
                currentBall.x = imageViewCord.x - 75;
                speedStart.x = -speedStart.x * izracunatoOdbijanje;
                canChangeX = false;
                rotating = false;
            }
            if (nextY - 75 < 0) {
                currentBall.y = 75;
                speedStart.y = -speedStart.y * izracunatoOdbijanje;
                canChangeY = false;
                rotating = false;
            }
            if (nextY + 75 >= imageViewCord.y) {
                currentBall.y = imageViewCord.y - 75;
                speedStart.y = -speedStart.y * izracunatoOdbijanje;
                canChangeY = false;
                rotating = false;
            }

            if(!canChangeX && !canChangeY){ displayView.updateView(); return; }
            boolean foundAngle = false;
            for(int i = 0; i < startWall.size(); i++){

                float left = startWall.get(i).x > endWall.get(i).x ? endWall.get(i).x : startWall.get(i).x;
                float top = startWall.get(i).y > endWall.get(i).y ? endWall.get(i).y : startWall.get(i).y;
                float right = startWall.get(i).x > endWall.get(i).x ? startWall.get(i).x : endWall.get(i).x;
                float bottom = startWall.get(i).y > endWall.get(i).y ? startWall.get(i).y : endWall.get(i).y;

                boolean onWall = isOnWall(left,top,right, bottom);
                rotating = rotating || onWall;

                if(canChangeY && nextX <= right && nextX >= left && nextY >= (top - 75) && nextY <= (75 + bottom)){

                    speedStart.y = -speedStart.y * izracunatoOdbijanje;
                    if(currentBall.y <= top) {
                        currentBall.y = top - 75;
                        canChangeY = false;
                        rotating = false;
                    }
                    if(currentBall.y >= bottom) {
                        currentBall.y = bottom + 75;
                        canChangeY = false;
                        rotating = false;
                    }
                }

                float helpY = canChangeY ? nextY : currentBall.y;

                if(canChangeX && helpY <= bottom && helpY >= top && nextX >= (left - 75) && nextX <= (75 + right)){

                    speedStart.x = -speedStart.x * izracunatoOdbijanje;
                    if(currentBall.x <= left) {
                        currentBall.x = left - 75;
                        canChangeX = false;
                        rotating = false;
                    }
                    if(currentBall.x >= right) {
                        currentBall.x = right + 75;
                        canChangeX = false;
                        rotating = false;
                    }
                }

                if((canChangeX || canChangeY) && !foundAngle){

                    float helpEdgeX = canChangeX ? nextX : currentBall.x;
                    float helpEdgeY = canChangeY ? nextY : currentBall.y;

                    ParameterCord topLeft = new ParameterCord(left, top);
                    ParameterCord topRight = new ParameterCord(right, top);
                    ParameterCord bottomRight = new ParameterCord(right, bottom);
                    ParameterCord bottomLeft = new ParameterCord(left, bottom);

                    float distance = (float) Math.sqrt(Math.pow((helpEdgeX - topLeft.x), 2) + Math.pow((helpEdgeY - topLeft.y), 2));

                    if (distance <= 75 && helpEdgeX < left && helpEdgeY < top) {
                        foundAngle = true;
                        boolean changeSpeed = false;
                        for(int j = 0; j < startWall.size(); j++)
                            if(i != j){
                                float leftTemp = startWall.get(j).x > endWall.get(j).x ? endWall.get(j).x : startWall.get(j).x;
                                float topTemp = startWall.get(j).y > endWall.get(j).y ? endWall.get(j).y : startWall.get(j).y;
                                float rightTemp = startWall.get(j).x > endWall.get(j).x ? startWall.get(j).x : endWall.get(j).x;
                                float bottomTemp = startWall.get(j).y > endWall.get(j).y ? startWall.get(j).y : endWall.get(j).y;

                                if(canChangeY && helpEdgeX <= (rightTemp+150) && helpEdgeX >= (leftTemp-150) && helpEdgeY >= (topTemp - 75) && helpEdgeY <= (75 + bottomTemp)){

                                    speedStart.y = -speedStart.y * izracunatoOdbijanje;
                                    changeSpeed = true;
                                    if(currentBall.y <= topTemp) {
                                        helpEdgeY = currentBall.y = topTemp - 75;
                                        canChangeY = false;
                                    }
                                    if(currentBall.y >= bottomTemp) {
                                        helpEdgeY = currentBall.y = bottomTemp + 75;
                                        canChangeY = false;
                                    }
                                }

                                float helpYTemp = canChangeY ? helpEdgeY : currentBall.y;

                                if(canChangeX && helpYTemp <= (bottomTemp+150) && helpYTemp >= (topTemp-150) && helpEdgeX >= (leftTemp - 75) && helpEdgeX <= (75 + rightTemp)){

                                    speedStart.x = -speedStart.x * izracunatoOdbijanje;
                                    changeSpeed = true;
                                    if(currentBall.x <= leftTemp - 73.5) {
                                        helpEdgeX = currentBall.x = leftTemp - 75;
                                        canChangeX = false;
                                    }
                                    if(currentBall.x >= rightTemp + 73.5) {
                                        helpEdgeX = currentBall.x = rightTemp + 75;
                                        canChangeX = false;
                                    }
                                }
                            }

                        if(canChangeX || canChangeY){

                            float duzinaX = topLeft.x - helpEdgeX;
                            if(canChangeX && canChangeY) duzinaX = ((topLeft.x - helpEdgeX) * 75f) / distance;
                            else if(!canChangeY) duzinaX = (float)Math.sqrt(Math.pow(75, 2) - Math.pow(topLeft.y - helpEdgeY,2));
                            float noviX = topLeft.x - duzinaX;

                            float duzinaY = topLeft.y - helpEdgeY;
                            if(canChangeX && canChangeY) duzinaY = ((topLeft.y - helpEdgeY) * 75f) / distance;
                            else if(!canChangeX) duzinaY = (float)Math.sqrt(Math.pow(75, 2) - Math.pow(duzinaX,2));
                            float noviY = topLeft.y - duzinaY;

                            double angle1 = Math.atan2(noviY - topLeft.y, noviX - topLeft.x);
                            double angle2 = Math.atan2(topLeft.y - topLeft.y, noviX - topLeft.x);
                            double result = Math.toDegrees(angle2-angle1);
                            result = 360 - result;

                            boolean canChangeSpeedX = false;
                            boolean canChangeSpeedY = false;

                            if(result < 55 || !canChangeY) canChangeSpeedX = true;
                            if(result > 35 || !canChangeX) canChangeSpeedY = true;

                            if(canChangeX){
                                canChangeX = false;
                                currentBall.x = noviX;
                                if((!rotating || changeSpeed) && !onWall && canChangeSpeedX) speedStart.x = -speedStart.x * izracunatoOdbijanje;
                            }
                            if(canChangeY){
                                canChangeY = false;
                                currentBall.y = noviY;
                                if((!rotating || changeSpeed) && !onWall && canChangeSpeedY) speedStart.y = -speedStart.y * izracunatoOdbijanje;
                            }

                            if(!rotating && lastRotatingTime > 0){
                                long calcTime = lastRotatingTime - time;
                                if(calcTime * NS2S < 0.25) {
                                    rotating = true;
                                    lastRotatingTime = -1;
                                }
                            }
                            if(!rotating){
                                boolean canChangeTime = false;
                                if(lastRotatingTime > 0){
                                    long calcTime = time - lastRotatingTime;
                                    if(calcTime * NS2S > 0.25) canChangeTime = true;
                                }
                                if(lastRotatingTime < 0 || canChangeTime)
                                    lastRotatingTime = time;
                            }
                        }
                        if(!canChangeX && !canChangeY){ displayView.updateView(); return; }
                        continue;
                    }

                    distance = (float) Math.sqrt(Math.pow((helpEdgeX - topRight.x), 2) + Math.pow((helpEdgeY - topRight.y), 2));

                    if (distance <= 75 && helpEdgeX > right && helpEdgeY < top) {
                        foundAngle = true;
                        boolean changeSpeed = false;
                        for(int j = 0; j < startWall.size(); j++)
                            if(i != j){
                                float leftTemp = startWall.get(j).x > endWall.get(j).x ? endWall.get(j).x : startWall.get(j).x;
                                float topTemp = startWall.get(j).y > endWall.get(j).y ? endWall.get(j).y : startWall.get(j).y;
                                float rightTemp = startWall.get(j).x > endWall.get(j).x ? startWall.get(j).x : endWall.get(j).x;
                                float bottomTemp = startWall.get(j).y > endWall.get(j).y ? startWall.get(j).y : endWall.get(j).y;

                                if(canChangeY && helpEdgeX <= rightTemp && helpEdgeX >= leftTemp && helpEdgeY >= (topTemp - 75) && helpEdgeY <= (75 + bottomTemp)){

                                    speedStart.y = -speedStart.y * izracunatoOdbijanje;
                                    changeSpeed = true;
                                    if(currentBall.y <= topTemp) {
                                        helpEdgeY = currentBall.y = topTemp - 75;
                                        canChangeY = false;
                                    }
                                    if(currentBall.y >= bottomTemp) {
                                        helpEdgeY = currentBall.y = bottomTemp + 75;
                                        canChangeY = false;
                                    }
                                }

                                float helpYTemp = canChangeY ? helpEdgeY : currentBall.y;

                                if(canChangeX && helpYTemp <= bottomTemp && helpYTemp >= topTemp && helpEdgeX >= (leftTemp - 73.5) && helpEdgeX <= (73.5 + rightTemp)){

                                    speedStart.x = -speedStart.x * izracunatoOdbijanje;
                                    changeSpeed = true;
                                    if(currentBall.x <= leftTemp - 73.5) {
                                        helpEdgeX = currentBall.x = leftTemp - 75;
                                        canChangeX = false;
                                    }
                                    if(currentBall.x >= rightTemp + 73.5) {
                                        helpEdgeX = currentBall.x = rightTemp + 75;
                                        canChangeX = false;
                                    }
                                }
                            }

                        if(canChangeX || canChangeY){

                            float duzinaX = helpEdgeX - topRight.x;
                            if(canChangeX && canChangeY) duzinaX = ((helpEdgeX - topRight.x) * 75f) / distance;
                            else if(!canChangeY) duzinaX = (float)Math.sqrt(Math.pow(75, 2) - Math.pow(topRight.y - helpEdgeY,2));
                            float noviX = topRight.x + duzinaX;

                            float duzinaY = topRight.y - helpEdgeY;
                            if(canChangeX && canChangeY) duzinaY = ((topRight.y - helpEdgeY) * 75f) / distance;
                            else if(!canChangeX) duzinaY = (float)Math.sqrt(Math.pow(75, 2) - Math.pow(duzinaX,2));
                            float noviY = topRight.y - duzinaY;

                            double angle1 = Math.atan2(topRight.y - noviY, topRight.x - noviX);
                            double angle2 = Math.atan2(topRight.y - topRight.y, topRight.x - noviX);
                            double result = Math.toDegrees(angle2-angle1);

                            boolean canChangeSpeedX = false;
                            boolean canChangeSpeedY = false;

                            if(result < 55 || !canChangeY) canChangeSpeedX = true;
                            if(result > 35 || !canChangeX) canChangeSpeedY = true;

                            if(canChangeX){
                                canChangeX = false;
                                currentBall.x = noviX;
                                if((!rotating || changeSpeed) && !onWall && canChangeSpeedX) speedStart.x = -speedStart.x * izracunatoOdbijanje;
                            }
                            if(canChangeY){
                                canChangeY = false;
                                currentBall.y = noviY;
                                if((!rotating || changeSpeed) && !onWall && canChangeSpeedY) speedStart.y = -speedStart.y * izracunatoOdbijanje;
                            }

                            if(!rotating && lastRotatingTime > 0){
                                long calcTime = lastRotatingTime - time;
                                if(calcTime * NS2S < 0.25) {
                                    rotating = true;
                                    lastRotatingTime = -1;
                                }
                            }
                            if(!rotating){
                                boolean canChangeTime = false;
                                if(lastRotatingTime > 0){
                                    long calcTime = time - lastRotatingTime;
                                    if(calcTime * NS2S > 0.25) canChangeTime = true;
                                }
                                if(lastRotatingTime < 0 || canChangeTime)
                                    lastRotatingTime = time;
                            }
                        }
                        if(!canChangeX && !canChangeY){ displayView.updateView(); return; }
                        continue;
                    }

                    distance = (float) Math.sqrt(Math.pow((helpEdgeX - bottomRight.x), 2) + Math.pow((helpEdgeY - bottomRight.y), 2));

                    if (distance <= 75 && helpEdgeX > right && helpEdgeY > bottom) {
                        foundAngle = true;
                        boolean changeSpeed = false;
                        for(int j = 0; j < startWall.size(); j++)
                            if(i != j){
                                float leftTemp = startWall.get(j).x > endWall.get(j).x ? endWall.get(j).x : startWall.get(j).x;
                                float topTemp = startWall.get(j).y > endWall.get(j).y ? endWall.get(j).y : startWall.get(j).y;
                                float rightTemp = startWall.get(j).x > endWall.get(j).x ? startWall.get(j).x : endWall.get(j).x;
                                float bottomTemp = startWall.get(j).y > endWall.get(j).y ? startWall.get(j).y : endWall.get(j).y;

                                if(canChangeY && helpEdgeX <= rightTemp && helpEdgeX >= leftTemp && helpEdgeY >= (topTemp - 75) && helpEdgeY <= (75 + bottomTemp)){

                                    speedStart.y = -speedStart.y * izracunatoOdbijanje;
                                    changeSpeed = true;
                                    if(currentBall.y <= topTemp) {
                                        helpEdgeY = currentBall.y = topTemp - 75;
                                        canChangeY = false;
                                    }
                                    if(currentBall.y >= bottomTemp) {
                                        helpEdgeY = currentBall.y = bottomTemp + 75;
                                        canChangeY = false;
                                    }
                                }

                                float helpYTemp = canChangeY ? helpEdgeY : currentBall.y;

                                if(canChangeX && helpYTemp <= bottomTemp && helpYTemp >= topTemp && helpEdgeX >= (leftTemp - 73.5) && helpEdgeX <= (73.5 + rightTemp)){

                                    speedStart.x = -speedStart.x * izracunatoOdbijanje;
                                    changeSpeed = true;
                                    if(currentBall.x <= leftTemp - 73.5) {
                                        helpEdgeX = currentBall.x = leftTemp - 75;
                                        canChangeX = false;
                                    }
                                    if(currentBall.x >= rightTemp + 73.5) {
                                        helpEdgeX = currentBall.x = rightTemp + 75;
                                        canChangeX = false;
                                    }
                                }
                            }

                        if(canChangeX || canChangeY){

                            float duzinaX = helpEdgeX - bottomRight.x;
                            if(canChangeX && canChangeY) duzinaX = ((helpEdgeX - bottomRight.x) * 75f) / distance;
                            else if(!canChangeY) duzinaX = (float)Math.sqrt(Math.pow(75, 2) - Math.pow(helpEdgeY - bottomRight.y,2));
                            float noviX = bottomRight.x + duzinaX;

                            float duzinaY = helpEdgeY - bottomLeft.y;
                            if(canChangeX && canChangeY) duzinaY = ((helpEdgeY - bottomRight.y) * 75f) / distance;
                            else if(!canChangeX) duzinaY = (float)Math.sqrt(Math.pow(75, 2) - Math.pow(duzinaX,2));
                            float noviY = bottomRight.y + duzinaY;

                            double angle1 = Math.atan2(bottomRight.y - noviY, bottomRight.x - noviX);
                            double angle2 = Math.atan2(bottomRight.y - bottomRight.y, bottomRight.x - noviX);
                            double result = Math.toDegrees(angle2-angle1);
                            result = 360 - result;

                            boolean canChangeSpeedX = false;
                            boolean canChangeSpeedY = false;

                            if(result < 55 || !canChangeY) canChangeSpeedX = true;
                            if(result > 35 || !canChangeX) canChangeSpeedY = true;

                            if(canChangeX){
                                canChangeX = false;
                                currentBall.x = noviX;
                                if((!rotating || changeSpeed) && !onWall && canChangeSpeedX) speedStart.x = -speedStart.x * izracunatoOdbijanje;
                            }
                            if(canChangeY){
                                canChangeY = false;
                                currentBall.y = noviY;
                                if((!rotating || changeSpeed) && !onWall && canChangeSpeedY) speedStart.y = -speedStart.y * izracunatoOdbijanje;
                            }

                            if(!rotating && lastRotatingTime > 0){
                                long calcTime = lastRotatingTime - time;
                                if(calcTime * NS2S < 0.25) {
                                    rotating = true;
                                    lastRotatingTime = -1;
                                }
                            }
                            if(!rotating){
                                boolean canChangeTime = false;
                                if(lastRotatingTime > 0){
                                    long calcTime = time - lastRotatingTime;
                                    if(calcTime * NS2S > 0.25) canChangeTime = true;
                                }
                                if(lastRotatingTime < 0 || canChangeTime)
                                    lastRotatingTime = time;
                            }
                        }
                        if(!canChangeX && !canChangeY){ displayView.updateView(); return; }
                        continue;
                    }

                    distance = (float) Math.sqrt(Math.pow((helpEdgeX - bottomLeft.x), 2) + Math.pow((helpEdgeY - bottomLeft.y), 2));

                    if (distance <= 75 && helpEdgeX < left && helpEdgeY > bottom) {
                        foundAngle = true;
                        boolean changeSpeed = false;
                        for(int j = 0; j < startWall.size(); j++)
                            if(i != j){
                                float leftTemp = startWall.get(j).x > endWall.get(j).x ? endWall.get(j).x : startWall.get(j).x;
                                float topTemp = startWall.get(j).y > endWall.get(j).y ? endWall.get(j).y : startWall.get(j).y;
                                float rightTemp = startWall.get(j).x > endWall.get(j).x ? startWall.get(j).x : endWall.get(j).x;
                                float bottomTemp = startWall.get(j).y > endWall.get(j).y ? startWall.get(j).y : endWall.get(j).y;

                                if(canChangeY && helpEdgeX <= rightTemp && helpEdgeX >= leftTemp && helpEdgeY >= (topTemp - 75) && helpEdgeY <= (75 + bottomTemp)){

                                    speedStart.y = -speedStart.y * izracunatoOdbijanje;
                                    changeSpeed = true;
                                    if(currentBall.y <= topTemp) {
                                        helpEdgeY = currentBall.y = topTemp - 75;
                                        canChangeY = false;
                                    }
                                    if(currentBall.y >= bottomTemp) {
                                        helpEdgeY = currentBall.y = bottomTemp + 75;
                                        canChangeY = false;
                                    }
                                }

                                float helpYTemp = canChangeY ? helpEdgeY : currentBall.y;

                                if(canChangeX && helpYTemp <= bottomTemp && helpYTemp >= topTemp && helpEdgeX >= (leftTemp - 73.5) && helpEdgeX <= (73.5 + rightTemp)){

                                    speedStart.x = -speedStart.x * izracunatoOdbijanje;
                                    changeSpeed = true;
                                    if(currentBall.x <= leftTemp - 73.5) {
                                        helpEdgeX = currentBall.x = leftTemp - 75;
                                        canChangeX = false;
                                    }
                                    if(currentBall.x >= rightTemp + 73.5) {
                                        helpEdgeX = currentBall.x = rightTemp + 75;
                                        canChangeX = false;
                                    }
                                }
                            }

                        if(canChangeX || canChangeY){

                            float duzinaX = bottomLeft.x - helpEdgeX;
                            if(canChangeX && canChangeY) duzinaX = ((bottomLeft.x - helpEdgeX) * 75f) / distance;
                            else if(!canChangeY) duzinaX = (float)Math.sqrt(Math.pow(75, 2) - Math.pow(helpEdgeY - bottomLeft.y,2));
                            float noviX = bottomLeft.x - duzinaX;

                            float duzinaY = helpEdgeY - bottomLeft.y;
                            if(canChangeX && canChangeY) duzinaY = ((helpEdgeY - bottomRight.y) * 75f) / distance;
                            else if(!canChangeX) duzinaY = (float)Math.sqrt(Math.pow(75, 2) - Math.pow(duzinaX,2));
                            float noviY = bottomLeft.y + duzinaY;

                            double angle1 = Math.atan2(noviY - bottomLeft.y, noviX - bottomLeft.x);
                            double angle2 = Math.atan2(bottomLeft.y - bottomLeft.y, noviX - bottomLeft.x);
                            double result = Math.toDegrees(angle2-angle1);

                            boolean canChangeSpeedX = false;
                            boolean canChangeSpeedY = false;

                            if(result < 55 || !canChangeY) canChangeSpeedX = true;
                            if(result > 35 || !canChangeX) canChangeSpeedY = true;

                            if(canChangeX){
                                canChangeX = false;
                                currentBall.x = noviX;
                                if((!rotating || changeSpeed) && !onWall && canChangeSpeedX) speedStart.x = -speedStart.x * izracunatoOdbijanje;
                            }
                            if(canChangeY){
                                canChangeY = false;
                                currentBall.y = noviY;
                                if((!rotating || changeSpeed) && !onWall && canChangeSpeedY) speedStart.y = -speedStart.y * izracunatoOdbijanje;
                            }
                            if(!rotating && lastRotatingTime > 0){
                                long calcTime = lastRotatingTime - time;
                                if(calcTime * NS2S < 0.25) {
                                    rotating = true;
                                    lastRotatingTime = -1;
                                }
                            }
                            if(!rotating){
                                boolean canChangeTime = false;
                                if(lastRotatingTime > 0){
                                    long calcTime = time - lastRotatingTime;
                                    if(calcTime * NS2S > 0.25) canChangeTime = true;
                                }
                                if(lastRotatingTime < 0 || canChangeTime)
                                lastRotatingTime = time;
                            }
                        }
                        if(!canChangeX && !canChangeY){ displayView.updateView(); return; }
                        continue;
                    }
                }
                if(!canChangeX && !canChangeY){ displayView.updateView(); return; }
            }

            if(!canChangeX && !canChangeY){ displayView.updateView(); return; }
            rotating = false;

            if(canChangeY){ currentBall.y = nextY; } //+ (otporX*otpor)); }
            if(canChangeX){ currentBall.x = nextX; } //+ (otporY*otpor)); }

        } else{
            lastTime = time;
            this.canMoveBall = true;
        }
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

    public float getSpeedX(){
        return speedStart.x;
    }

    public float getSpeedY(){
        return speedStart.y;
    }

}
