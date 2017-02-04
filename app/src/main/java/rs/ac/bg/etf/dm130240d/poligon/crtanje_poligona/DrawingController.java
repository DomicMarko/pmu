package rs.ac.bg.etf.dm130240d.poligon.crtanje_poligona;

import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Stack;

import rs.ac.bg.etf.dm130240d.poligon.ParameterCord;
import rs.ac.bg.etf.dm130240d.poligon.interfaces.ViewInterface;

/**
 * Created by Marko on 1/31/2017.
 */

public class DrawingController implements Serializable {

    enum insertMode {nista, dobraRupa, losaRupa, zid, startRupa, obrisanaLosaRupa, obrisanoSve};

    private ParameterCord trueHole, startHole;
    private ParameterCord erasedFalseHole;
    private Stack<ParameterCord> trueHoles, startHoles;
    private ArrayList<ParameterCord> falseHoles, startWall, endWall;
    private transient ViewInterface displayView;
    private insertMode currentMode;
    private insertMode lastAdded;
    private AllTempSave tempSave;
    private AllTempSave uniformDimensions;

    public DrawingController(ViewInterface displayView){

        this.displayView = displayView;

        falseHoles = new ArrayList<ParameterCord>();
        startWall = new ArrayList<ParameterCord>();
        endWall = new ArrayList<ParameterCord>();

        trueHole = new ParameterCord(0, 0);
        trueHoles = new Stack<ParameterCord>();

        startHole = new ParameterCord(0, 0);
        startHoles = new Stack<ParameterCord>();

        currentMode = insertMode.nista;
        lastAdded = insertMode.nista;
    }

    public int getCurrentMode() {
        return currentMode.ordinal();
    }

    public void setCurrentMode(int currentIndex) {
        this.currentMode = insertMode.values()[currentIndex];
    }

    public ParameterCord getStartHole() {
        return startHole;
    }

    public ParameterCord getTrueHole() {
        return trueHole;
    }

    public ArrayList<ParameterCord> getFalseHoles() {
        return falseHoles;
    }

    public void setDisplayView(ViewInterface displayView) {
        this.displayView = displayView;
    }

    public ArrayList<ParameterCord> getStartWall() {
        return startWall;
    }

    public ArrayList<ParameterCord> getEndWall() {
        return endWall;
    }

    public void setStartHole(float x, float y, DrawingView imageView, int actionBarHeight){

        startHoles.push(new ParameterCord(startHole.x, startHole.y));

        startHole.x = x; // viewCoords[0] is the X coordinate
        startHole.y = y; // viewCoords[1] is the y coordinate

        lastAdded = insertMode.startRupa;

        displayView.updateView();
    }

    public void setTrueHole(float x, float y, DrawingView imageView, int actionBarHeight){

        trueHoles.push(new ParameterCord(trueHole.x, trueHole.y));

        trueHole.x = x; // viewCoords[0] is the X coordinate
        trueHole.y = y; // viewCoords[1] is the y coordinate

        lastAdded = insertMode.dobraRupa;

        displayView.updateView();
    }

    public void setFalseHole(float x, float y, DrawingView imageView, int actionBarHeight){

        float cordX = x; // viewCoords[0] is the X coordinate
        float cordY = y; // viewCoords[1] is the y coordinate

        float startCordX = cordX - 90;
        float endCordX = cordX + 90;
        float startCordY = cordY - 90;
        float endCordY = cordY + 90;

        boolean canAdd = true;

        float startX = trueHole.x - 90;
        float endX = trueHole.x + 90;
        float startY = trueHole.y - 90;
        float endY = trueHole.y + 90;

        boolean xIn = false;
        boolean yIn = false;

        if((startCordX >= startX && startCordX <= endX) || (endCordX >= startX && endCordX <= endX)) xIn = true;
        if((startCordY >= startY && startCordY <= endY) || (endCordY >= startY && endCordY <= endY)) yIn = true;

        if(xIn && yIn){
            canAdd = false;
            Toast.makeText((AppCompatActivity)displayView, "Preklapanje sa početnom rupom i brisanje.", Toast.LENGTH_SHORT).show();
        }
        else{

            for(int i = 0; i < falseHoles.size(); i++){

                ParameterCord pc = falseHoles.get(i);

                startX = pc.x - 90;
                endX = pc.x + 90;
                startY = pc.y - 90;
                endY = pc.y + 90;

                xIn = false;
                yIn = false;

                if((startCordX >= startX && startCordX <= endX) || (endCordX >= startX && endCordX <= endX)) xIn = true;
                if((startCordY >= startY && startCordY <= endY) || (endCordY >= startY && endCordY <= endY)) yIn = true;

                if(xIn && yIn){
                    canAdd = false;
                    falseHoles.remove(pc);
                    i--;
                    erasedFalseHole = pc;
                    lastAdded = insertMode.obrisanaLosaRupa;
                    Toast.makeText((AppCompatActivity)displayView, "Preklapanje sa drugom rupom i brisanje.", Toast.LENGTH_SHORT).show();
                    break;
                }
            }
        }

        if(canAdd){ falseHoles.add(new ParameterCord(cordX, cordY)); lastAdded = insertMode.losaRupa; }

        displayView.updateView();
    }

    public void setZid(ParameterCord startW, ParameterCord endW){

        boolean canAdd = true;

        float startX = trueHole.x - 90;
        float endX = trueHole.x + 90;
        float startY = trueHole.y - 90;
        float endY = trueHole.y + 90;

        boolean xIn = false;
        boolean yIn = false;

        float left = startW.x > endW.x ? endW.x : startW.x;
        float top = startW.y > endW.y ? endW.y : startW.y;
        float right = startW.x > endW.x ? startW.x : endW.x;
        float bottom = startW.y > endW.y ? startW.y : endW.y;
/*
        if((startCordX >= startX && startCordX <= endX) || (endCordX >= startX && endCordX <= endX)) xIn = true;
        if((startCordY >= startY && startCordY <= endY) || (endCordY >= startY && endCordY <= endY)) yIn = true;
*/
        if(xIn && yIn){
            canAdd = false;
            Toast.makeText((AppCompatActivity)displayView, "Preklapanje sa početnom rupom i brisanje.", Toast.LENGTH_SHORT).show();
        }


        startWall.add(startW);
        endWall.add(endW);
        lastAdded = insertMode.zid;
        displayView.updateView();
    }

    public void undo(){
        switch (lastAdded.ordinal()){
            case 1:
                trueHole = trueHoles.pop();
                break;
            case 2:
                falseHoles.remove(falseHoles.size()-1);
                break;
            case 3:
                startWall.remove(startWall.size()-1);
                endWall.remove(endWall.size()-1);
                break;
            case 4:
                startHole = startHoles.pop();
                break;
            case 5:
                falseHoles.add(erasedFalseHole);
                break;
            case 6:
                startHole = tempSave.oldStartHole;
                trueHole = tempSave.oldTrueHole;
                falseHoles = tempSave.oldFalseHoles;
                startWall = tempSave.oldStartWall;
                endWall = tempSave.oldEndWall;
                break;
            default:
                Toast.makeText((AppCompatActivity)displayView, "Samo jednom može undo.", Toast.LENGTH_SHORT).show();
                break;
        }

        lastAdded = insertMode.nista;
        displayView.updateView();
    }

    public void eraseAll(){

        tempSave = new AllTempSave(falseHoles, startWall, endWall, trueHole, startHole);

        falseHoles = new ArrayList<ParameterCord>();
        startWall = new ArrayList<ParameterCord>();
        endWall = new ArrayList<ParameterCord>();

        trueHole = new ParameterCord(0, 0);
        trueHoles = new Stack<ParameterCord>();

        startHole = new ParameterCord(0, 0);
        startHoles = new Stack<ParameterCord>();

        lastAdded = insertMode.obrisanoSve;

        displayView.updateView();
    }

    public boolean setUniformDimensions(DrawingView imageView) {

        try{
            float startXUniform = (startHole.x * 100) / imageView.getWidth();
            float startYUniform = (startHole.y * 100) / imageView.getHeight();

            ParameterCord startUniform = new ParameterCord(startXUniform, startYUniform);

            float trueXUniform = (trueHole.x * 100) / imageView.getWidth();
            float trueYUniform = (trueHole.y * 100) / imageView.getHeight();

            ParameterCord trueUniform = new ParameterCord(trueXUniform, trueYUniform);

            ArrayList<ParameterCord> falseHolesUniform = new ArrayList<ParameterCord>();
            for(ParameterCord pc: falseHoles){
                ParameterCord tempPc = new ParameterCord(0, 0);
                tempPc.x = (pc.x * 100) / imageView.getWidth();
                tempPc.y = (pc.y * 100) / imageView.getHeight();
                falseHolesUniform.add(tempPc);
            }

            ArrayList<ParameterCord> startWallUniform = new ArrayList<ParameterCord>();
            ArrayList<ParameterCord> endWallUniform = new ArrayList<ParameterCord>();
            for(int i = 0; i < startWall.size(); i++){

                ParameterCord tempPc1 = new ParameterCord(0, 0);

                tempPc1.x = (startWall.get(i).x * 100) / imageView.getWidth();
                tempPc1.y = (startWall.get(i).y * 100) / imageView.getHeight();

                ParameterCord tempPc2 = new ParameterCord(0, 0);

                tempPc2.x = (endWall.get(i).x * 100) / imageView.getWidth();
                tempPc2.y = (endWall.get(i).y * 100) / imageView.getHeight();

                startWallUniform.add(tempPc1);
                endWallUniform.add(tempPc2);
            }

            uniformDimensions = new AllTempSave(falseHolesUniform, startWallUniform, endWallUniform, trueUniform, startUniform);

            return true;

        } catch (Exception ex){ return false; }
    }

    private class AllTempSave{
        public ArrayList<ParameterCord> oldFalseHoles;
        public ArrayList<ParameterCord> oldStartWall;
        public ArrayList<ParameterCord> oldEndWall;

        public ParameterCord oldTrueHole;
        public ParameterCord oldStartHole;

        public AllTempSave(ArrayList<ParameterCord> oldFalseHoles, ArrayList<ParameterCord> oldStartWall, ArrayList<ParameterCord> oldEndWall, ParameterCord oldTrueHole, ParameterCord oldStartHole) {
            this.oldFalseHoles = oldFalseHoles;
            this.oldStartWall = oldStartWall;
            this.oldEndWall = oldEndWall;
            this.oldTrueHole = oldTrueHole;
            this.oldStartHole = oldStartHole;
        }
    }

    public boolean save(File root, DrawingView imageView){

        boolean canWriteFile = true;

        if(startHole.x == 0 && startHole.y == 0){
            canWriteFile = false;
            Toast.makeText((AppCompatActivity)displayView, "Molimo Vas, ubacite startno polje.", Toast.LENGTH_SHORT).show();
        }
        if(trueHole.x == 0 && trueHole.y == 0){
            canWriteFile = false;
            Toast.makeText((AppCompatActivity)displayView, "Molimo Vas, ubacite ciljnu rupu.", Toast.LENGTH_SHORT).show();
        }
        if(startHole == null || trueHole == null || falseHoles == null || startWall == null || endWall == null) canWriteFile = false;
        if(canWriteFile) canWriteFile = setUniformDimensions(imageView);
        if(canWriteFile){

            try{

                File dir1 = new File(root.getAbsolutePath() + "/Poligoni");
                if(!dir1.exists()) dir1.mkdir();
                String poligonName = new Date().toString();
                File dir2 = new File(dir1.getAbsolutePath() + "/poligon_" + poligonName);
                if(!dir2.exists()) dir2.mkdir();
                File polFile = new File(dir2.getAbsolutePath() + "/poligon_" + poligonName + ".obj");
                if(!polFile.exists()) dir2.mkdir();

                FileOutputStream fileOutputStream = new FileOutputStream(polFile);
                ObjectOutputStream outStream = new ObjectOutputStream(fileOutputStream);
                outStream.writeObject(uniformDimensions.oldStartHole);
                outStream.writeObject(uniformDimensions.oldTrueHole);
                outStream.writeObject(uniformDimensions.oldFalseHoles);
                outStream.writeObject(uniformDimensions.oldStartWall);
                outStream.writeObject(uniformDimensions.oldEndWall);

                outStream.close();
                fileOutputStream.close();

                Toast.makeText((AppCompatActivity)displayView, "Uspeštno ste sačuvali poligon.", Toast.LENGTH_SHORT).show();

            } catch (FileNotFoundException e){
                Toast.makeText((AppCompatActivity)displayView, "Greška: " + e.toString(), Toast.LENGTH_SHORT).show();
                canWriteFile = false;
            } catch (IOException e){
                Toast.makeText((AppCompatActivity)displayView, "Greška: " + e.toString(), Toast.LENGTH_SHORT).show();
                canWriteFile = false;
            } catch (Exception e){
                Toast.makeText((AppCompatActivity)displayView, "Greška: " + e.toString(), Toast.LENGTH_SHORT).show();
                canWriteFile = false;
            }
        }

        return canWriteFile;
    }
}
