package rs.ac.bg.etf.dm130240d.poligon;

import java.io.Serializable;

/**
 * Created by Marko on 1/31/2017.
 */

public class ParameterCord implements Serializable {

    public float x = 0;
    public float y = 0;

    public ParameterCord(float x, float y){
        this.x = x;
        this.y = y;
    }
}
