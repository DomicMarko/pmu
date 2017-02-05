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
    private ParameterCord tempStartW, tempEndW;
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

        trueHole = new ParameterCord(-100, -100);
        trueHoles = new Stack<ParameterCord>();

        startHole = new ParameterCord(-100, -100);
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

    public ParameterCord getTempStartW() {
        return tempStartW;
    }

    public ParameterCord getTempEndW() {
        return tempEndW;
    }

    public void drawingWall(ParameterCord startW, ParameterCord endW){
        if(startW != null && endW != null){
            tempStartW = startW;
            tempEndW = endW;
        }
        displayView.updateView();
    }

    public boolean checkPosition(ParameterCord hole1, ParameterCord hole2){

        float startCordX = hole1.x - 90;
        float endCordX = hole1.x + 90;
        float startCordY = hole1.y - 90;
        float endCordY = hole1.y + 90;

        boolean canAdd = true;

        float startX = hole2.x - 90;
        float endX = hole2.x + 90;
        float startY = hole2.y - 90;
        float endY = hole2.y + 90;

        boolean xIn = false;
        boolean yIn = false;

        if((startCordX >= startX && startCordX <= endX) || (endCordX >= startX && endCordX <= endX)) xIn = true;
        if((startCordY >= startY && startCordY <= endY) || (endCordY >= startY && endCordY <= endY)) yIn = true;

        return xIn && yIn;
    }

    public boolean checkWallPositionHole(ParameterCord hole, ParameterCord startW, ParameterCord endW){

        float startCordX = hole.x - 90;
        float endCordX = hole.x + 90;
        float startCordY = hole.y - 90;
        float endCordY = hole.y + 90;

        float left = startW.x > endW.x ? endW.x : startW.x;
        float top = startW.y > endW.y ? endW.y : startW.y;
        float right = startW.x > endW.x ? startW.x : endW.x;
        float bottom = startW.y > endW.y ? startW.y : endW.y;

        boolean holeIn = false;

        if(hole.y >= top && hole.y <= bottom){
            if(startCordX < right && startCordX > left) holeIn = true;
            if(startCordX <= left && endCordX >= right) holeIn = true;
            if(endCordX < right && endCordX > left) holeIn = true;

        }

        if(hole.x >= left && hole.x <= right){
            if(startCordY < bottom && startCordY > top) holeIn = true;
            if(startCordY <= top && endCordY >= bottom) holeIn = true;
            if(endCordY < bottom && endCordY > top) holeIn = true;
        }

        return holeIn;
    }

    public void setStartHole(float x, float y, DrawingView imageView){

        startHoles.push(new ParameterCord(startHole.x, startHole.y));

        startHole.x = x; // viewCoords[0] is the X coordinate
        startHole.y = y; // viewCoords[1] is the y coordinate

        if(startHole.x - 90 <= 0) startHole.x = 90;
        if(startHole.x + 90 >= imageView.getWidth()) startHole.x = imageView.getWidth() - 90;

        if(startHole.y - 90 <= 0) startHole.y = 90;
        if(startHole.y + 90 >= imageView.getHeight()) startHole.y = imageView.getHeight() - 90;

        if(checkPosition(startHole, trueHole)){
            //Toast.makeText((AppCompatActivity)displayView, "Preklapanje sa ciljnom rupom.", Toast.LENGTH_SHORT).show();
            startHole = startHoles.pop();
            displayView.updateView();
            return;
        }

        for(ParameterCord pc: falseHoles)
            if(checkPosition(startHole, pc)){
                //Toast.makeText((AppCompatActivity)displayView, "Preklapanje sa pogrešnom rupom.", Toast.LENGTH_SHORT).show();
                startHole = startHoles.pop();
                displayView.updateView();
                return;
            }

        for(int i = 0; i < startWall.size(); i++)
            if(checkWallPositionHole(startHole, startWall.get(i), endWall.get(i))){
                //Toast.makeText((AppCompatActivity)displayView, "Preklapanje sa zidom.", Toast.LENGTH_SHORT).show();
                startHole = startHoles.pop();
                displayView.updateView();
                return;
            }

        lastAdded = insertMode.startRupa;
        displayView.updateView();
    }

    public void setTrueHole(float x, float y, DrawingView imageView){

        trueHoles.push(new ParameterCord(trueHole.x, trueHole.y));

        trueHole.x = x; // viewCoords[0] is the X coordinate
        trueHole.y = y; // viewCoords[1] is the y coordinate

        if(trueHole.x - 90 <= 0) trueHole.x = 90;
        if(trueHole.x + 90 >= imageView.getWidth()) trueHole.x = imageView.getWidth() - 90;

        if(trueHole.y - 90 <= 0) trueHole.y = 90;
        if(trueHole.y + 90 >= imageView.getHeight()) trueHole.y = imageView.getHeight() - 90;

        if(checkPosition(trueHole, startHole)){
            //Toast.makeText((AppCompatActivity)displayView, "Preklapanje sa startnom rupom.", Toast.LENGTH_SHORT).show();
            trueHole = trueHoles.pop();
            displayView.updateView();
            return;
        }

        for(ParameterCord pc: falseHoles)
            if(checkPosition(trueHole, pc)){
                //Toast.makeText((AppCompatActivity)displayView, "Preklapanje sa pogrešnom rupom.", Toast.LENGTH_SHORT).show();
                trueHole = trueHoles.pop();
                displayView.updateView();
                return;
            }

        for(int i = 0; i < startWall.size(); i++)
            if(checkWallPositionHole(trueHole, startWall.get(i), endWall.get(i))){
                //Toast.makeText((AppCompatActivity)displayView, "Preklapanje sa zidom.", Toast.LENGTH_SHORT).show();
                trueHole = trueHoles.pop();
                displayView.updateView();
                return;
            }

        lastAdded = insertMode.dobraRupa;
        displayView.updateView();
    }

    public void setFalseHole(float x, float y, DrawingView imageView){

        float cordX = x; // viewCoords[0] is the X coordinate
        float cordY = y; // viewCoords[1] is the y coordinate

        if(cordX - 90 <= 0) cordX = 90;
        if(cordX + 90 >= imageView.getWidth()) cordX = imageView.getWidth() - 90;

        if(cordY - 90 <= 0) cordY = 90;
        if(cordY + 90 >= imageView.getHeight()) cordY = imageView.getHeight() - 90;

        boolean canAdd = true;
        ParameterCord tempFalseHole = new ParameterCord(cordX, cordY);


        if(checkPosition(tempFalseHole, startHole)){
            canAdd = false;
            //Toast.makeText((AppCompatActivity)displayView, "Preklapanje sa početnom rupom.", Toast.LENGTH_SHORT).show();
        }

        if(checkPosition(tempFalseHole, trueHole)){
            canAdd = false;
            //Toast.makeText((AppCompatActivity)displayView, "Preklapanje sa ciljnom rupom.", Toast.LENGTH_SHORT).show();
        }

        for(int i = 0; i < startWall.size(); i++)
            if(checkWallPositionHole(tempFalseHole, startWall.get(i), endWall.get(i))){
                canAdd = false;
                //Toast.makeText((AppCompatActivity)displayView, "Preklapanje sa zidom.", Toast.LENGTH_SHORT).show();
            }

        if(canAdd){
            for(int i = 0; i < falseHoles.size(); i++){
                ParameterCord pc = falseHoles.get(i);
                if(checkPosition(tempFalseHole, pc)){
                    canAdd = false;
                    falseHoles.remove(pc);
                    i--;
                    erasedFalseHole = pc;
                    lastAdded = insertMode.obrisanaLosaRupa;
                    //Toast.makeText((AppCompatActivity)displayView, "Preklapanje sa drugom rupom i brisanje.", Toast.LENGTH_SHORT).show();
                    break;
                }
            }
        }

        if(canAdd){ falseHoles.add(tempFalseHole); lastAdded = insertMode.losaRupa; }

        displayView.updateView();
    }

    public void setZid(ParameterCord startW, ParameterCord endW){

        boolean canAdd = true;

        if(checkWallPositionHole(startHole, startW, endW)){
            canAdd = false;
            //Toast.makeText((AppCompatActivity)displayView, "Preklapanje zida sa startnom rupom.", Toast.LENGTH_SHORT).show();
        }

        if(checkWallPositionHole(trueHole, startW, endW)){
            canAdd = false;
            //Toast.makeText((AppCompatActivity)displayView, "Preklapanje zida sa ciljnom rupom.", Toast.LENGTH_SHORT).show();
        }

        for(ParameterCord pc: falseHoles)
            if(checkWallPositionHole(pc, startW, endW)){
                canAdd = false;
                //Toast.makeText((AppCompatActivity)displayView, "Preklapanje zida sa pogrešnom rupom.", Toast.LENGTH_SHORT).show();
            }

        if(canAdd){
            startWall.add(startW);
            endWall.add(endW);
            lastAdded = insertMode.zid;
        }

        tempStartW = tempEndW = null;
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

        trueHole = new ParameterCord(-100, -100);
        trueHoles = new Stack<ParameterCord>();

        startHole = new ParameterCord(-100, -100);
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
                ParameterCord tempPc = new ParameterCord(-100, -100);
                tempPc.x = (pc.x * 100) / imageView.getWidth();
                tempPc.y = (pc.y * 100) / imageView.getHeight();
                falseHolesUniform.add(tempPc);
            }

            ArrayList<ParameterCord> startWallUniform = new ArrayList<ParameterCord>();
            ArrayList<ParameterCord> endWallUniform = new ArrayList<ParameterCord>();
            for(int i = 0; i < startWall.size(); i++){

                ParameterCord tempPc1 = new ParameterCord(-100, -100);

                tempPc1.x = (startWall.get(i).x * 100) / imageView.getWidth();
                tempPc1.y = (startWall.get(i).y * 100) / imageView.getHeight();

                ParameterCord tempPc2 = new ParameterCord(-100, -100);

                tempPc2.x = (endWall.get(i).x * 100) / imageView.getWidth();
                tempPc2.y = (endWall.get(i).y * 100) / imageView.getHeight();

                startWallUniform.add(tempPc1);
                endWallUniform.add(tempPc2);
            }

            uniformDimensions = new AllTempSave(falseHolesUniform, startWallUniform, endWallUniform, trueUniform, startUniform);

            return true;

        } catch (Exception ex){ return false; }
    }

    private class AllTempSave implements Serializable{
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

        if(startHole.x == -100 && startHole.y == -100){
            canWriteFile = false;
            Toast.makeText((AppCompatActivity)displayView, "Molimo Vas, ubacite startno polje.", Toast.LENGTH_SHORT).show();
        }
        if(trueHole.x == -100 && trueHole.y == -100){
            canWriteFile = false;
            Toast.makeText((AppCompatActivity)displayView, "Molimo Vas, ubacite ciljnu rupu.", Toast.LENGTH_SHORT).show();
        }
        if(startHole == null || trueHole == null || falseHoles == null || startWall == null || endWall == null) canWriteFile = false;
        if(canWriteFile) canWriteFile = setUniformDimensions(imageView);
        if(canWriteFile){

            try{

                File dir1 = new File(root.getAbsolutePath() + "/Poligoni");
                if(!dir1.exists()) dir1.mkdir();
                String poligonName;
                File[] files = dir1.listFiles();
                if(files != null){
                    ArrayList<String> folderNames = new ArrayList<String>();
                    for(File f: files) folderNames.add(f.getName());
                    int numOfFile = 1;
                    poligonName = "Level" + numOfFile;
                    boolean foundName = true;
                    while (foundName){
                        boolean equals = false;
                        for(String name: folderNames)
                            if(name.equals(poligonName)){ equals = true; break; }
                        if(equals){
                            numOfFile++;
                            poligonName = "Level" + numOfFile;
                        } else foundName = false;
                    }
                } else poligonName = "Level1";


                File dir2 = new File(dir1.getAbsolutePath() + "/" + poligonName);
                if(!dir2.exists()) dir2.mkdir();
                File polFile = new File(dir2.getAbsolutePath() + "/" + poligonName + ".obj");
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
