/**
 * 
 */
package com.raddle.dlna.ctrl;

import org.cybergarage.upnp.Action;
import org.cybergarage.upnp.ArgumentList;
import org.cybergarage.upnp.Device;
import org.cybergarage.upnp.Service;

import com.raddle.dlna.renderer.AVTransport;
import com.raddle.dlna.util.DurationUtils;

/**
 * @author raddle
 *
 */
public class ActionHelper {
	private Device device;

	public ActionHelper(Device device) {
		this.device = device;
	}

	public void play(String url) {
		Service avTransService = device.getService(AVTransport.SERVICE_TYPE);
		Action setUriAct = avTransService.getAction(AVTransport.SETAVTRANSPORTURI);
		setUriAct.setArgumentValue(AVTransport.INSTANCEID, "0");
		setUriAct.setArgumentValue(AVTransport.CURRENTURI, url);
		setUriAct.setArgumentValue(AVTransport.CURRENTURIMETADATA, "");
		if (!setUriAct.postControlAction()) {
			return;
		}
		Action playAct = avTransService.getAction(AVTransport.PLAY);
		playAct.setArgumentValue(AVTransport.INSTANCEID, "0");
		playAct.setArgumentValue(AVTransport.SPEED, "1");
		if (!playAct.postControlAction()) {
			return;
		}
	}

	public boolean isSupportNext() {
		Service avTransService = device.getService(AVTransport.SERVICE_TYPE);
		Action nextAct = avTransService.getAction(AVTransport.NEXT);
		return nextAct != null;
	}

	public void next(String url) {
		Service avTransService = device.getService(AVTransport.SERVICE_TYPE);
		Action setNextUriAct = avTransService.getAction(AVTransport.SETNEXTAVTRANSPORTURI);
		setNextUriAct.setArgumentValue(AVTransport.INSTANCEID, "0");
		setNextUriAct.setArgumentValue(AVTransport.NEXTURI, url);
		setNextUriAct.setArgumentValue(AVTransport.NEXTURIMETADATA, "");
		if (!setNextUriAct.postControlAction()) {
			return;
		}
		Action nextAct = avTransService.getAction(AVTransport.NEXT);
		nextAct.setArgumentValue(AVTransport.INSTANCEID, "0");
		if (!nextAct.postControlAction()) {
			return;
		}
	}

	public void resume() {
		Service avTransService = device.getService(AVTransport.SERVICE_TYPE);
		Action playAct = avTransService.getAction(AVTransport.PLAY);
		playAct.setArgumentValue(AVTransport.INSTANCEID, "0");
		playAct.setArgumentValue(AVTransport.SPEED, "1");
		if (!playAct.postControlAction()) {
			return;
		}
	}

	public void pause() {
		Service avTransService = device.getService(AVTransport.SERVICE_TYPE);
		Action pauseAct = avTransService.getAction(AVTransport.PAUSE);
		pauseAct.setArgumentValue(AVTransport.INSTANCEID, "0");
		if (!pauseAct.postControlAction()) {
			return;
		}
	}

	public ArgumentList getPositionInfo() {
		Service avTransService = device.getService(AVTransport.SERVICE_TYPE);
		Action getPosAct = avTransService.getAction(AVTransport.GETPOSITIONINFO);
		getPosAct.setArgumentValue(AVTransport.INSTANCEID, "0");
		if (!getPosAct.postControlAction()) {
			return null;
		}
		return getPosAct.getOutputArgumentList();
	}

	public void seak(int second) {
		Service avTransService = device.getService(AVTransport.SERVICE_TYPE);
		Action getPosAct = avTransService.getAction(AVTransport.SEEK);
		getPosAct.setArgumentValue(AVTransport.INSTANCEID, "0");
		getPosAct.setArgumentValue(AVTransport.UNIT, AVTransport.TRACK_NR);
		getPosAct.setArgumentValue(AVTransport.TARGET, DurationUtils.getTrackNRFormat(second));
		if (!getPosAct.postControlAction()) {
			return;
		}
	}

	public void stop() {
		Service avTransService = device.getService(AVTransport.SERVICE_TYPE);
		Action stopAct = avTransService.getAction(AVTransport.STOP);
		stopAct.setArgumentValue(AVTransport.INSTANCEID, "0");
		if (!stopAct.postControlAction()) {
			return;
		}

	}

	public Device getDevice() {
		return device;
	}
}
