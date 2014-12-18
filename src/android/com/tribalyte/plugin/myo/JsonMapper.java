package com.tribalyte.plugin.myo;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

import com.thalmic.myo.Myo;
import com.thalmic.myo.Quaternion;
import com.thalmic.myo.Vector3;

public class JsonMapper{
	
	private static final String TAG = JsonMapper.class.getSimpleName();
	
	//JSON representation of a Myo instance is an array like: [name, mac, fwver]
	public static JSONArray toJson(Myo myo){
		JSONArray res = new JSONArray();
		res.put(myo.getName());
		res.put(myo.getMacAddress());
		res.put(myo.getFirmwareVersion().toDisplayString());
		return res;
	}

	public static JSONObject toJson(Vector3 v3){
		JSONObject v3Json = new JSONObject();
		try{
			v3Json.put("x", v3.x());
			v3Json.put("y", v3.y());
			v3Json.put("z", v3.z());
		}catch(Exception e){
			loge("ERROR mapping Vector3", e);
		}
		return v3Json;
	}

	public static JSONObject toJson(Quaternion q) {
		JSONObject res = new JSONObject();
		try{
			res.put("x", q.x());
			res.put("y", q.y());
			res.put("z", q.z());
			res.put("w", q.w());
			//TODO: the following should be optional
			res.put("roll", Quaternion.roll(q));
			res.put("pitch", Quaternion.pitch(q));
			res.put("yaw", Quaternion.yaw(q));
		}catch(Exception e){
			loge("ERROR mapping Quaternion", e);
		}
		return res;
	}
	
	private static void loge(String msg, Throwable t) {
		if(MyoApi.LOG_ENABLED){
			Log.d(TAG, msg, t);
		}
	}

}
