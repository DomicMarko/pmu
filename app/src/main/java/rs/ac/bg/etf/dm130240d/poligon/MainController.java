package rs.ac.bg.etf.dm130240d.poligon;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.Serializable;

import rs.ac.bg.etf.dm130240d.poligon.db.DbModel;
import rs.ac.bg.etf.dm130240d.poligon.interfaces.ViewInterface;

/**
 * Created by Marko on 2/1/2017.
 */

public class MainController implements Serializable {

    private transient ViewInterface displayView;
    private String[] names;
    private String[] filePaths;

    public MainController(ViewInterface displayView) {
        this.displayView = displayView;
        findFiles();
    }

    public String[] getNames() {
        return names;
    }

    public String[] getFilePaths() {
        return filePaths;
    }

    public void findFiles(){
        File root = Environment.getExternalStorageDirectory();
        File dir1 = new File(root.getAbsolutePath() + "/Poligoni");
        if(dir1.exists()){
            names = new String[dir1.listFiles().length];
            filePaths = new String[dir1.listFiles().length];

            for(int i = 0; i < dir1.listFiles().length; i++){
                names[i] = dir1.listFiles()[i].getName();
                filePaths[i] = dir1.listFiles()[i].getAbsolutePath();
            }
        }

    }

    public boolean deletePoligon(int index){
        boolean status = true;
        String fileName = names[index];
        File fileForDelete = new File(filePaths[index]);
        if(fileForDelete.exists()) {
            File[] filesIn = fileForDelete.listFiles();
            for(File f: filesIn) f.delete();
            DbModel dbModel = new DbModel((Context)displayView);
            int numOfAffectedRows = dbModel.deletePoligonRows(fileName);
            status = fileForDelete.delete();
        }

        findFiles();
        displayView.updateView();

        return status;
    }

    public void setDisplayView(ViewInterface displayView) {
        this.displayView = displayView;
    }
}
