package cn.edu.hdu.artalk2.dto;

import org.json.JSONException;
import org.json.JSONObject;

public class Coordinate {
    private float Cx;
    private float Cy;

    public Coordinate(float Cx, float Cy) {
        this.Cx = Cx;
        this.Cy = Cy;
    }

    public float getCx() {
        return Cx;
    }

    public void setCx(float cx) {
        this.Cx = cx;
    }

    public float getCy() {
        return Cy;
    }

    public void setCy(float cy) {
        this.Cy = cy;
    }

    @Override
    public String toString() {
        return "Coordinate{" +
                "Cx=" + Cx +
                ", Cy=" + Cy +
                '}';
    }

    public JSONObject toJSONObject(){

        JSONObject jo = new JSONObject();
        try {
            jo.put("Cx", this.Cx);
            jo.put("Cy", this.Cy);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jo;
    }
}
