package rs.ac.bg.etf.dm130240d.poligon.game;


import java.io.Serializable;

public class TimerModel implements Serializable {
	protected static enum State {
		STOPPED, RUNNING, PAUSED
	}

	protected State state;
	protected int hh;
	protected int mm;
	protected int ss;

	protected long lastTime;


	public TimerModel() {
		state = State.STOPPED;
		resetTime(0);
	}
	
	public synchronized boolean resetTime(long currTime) {
		boolean result;
		if (hh==0 && mm==0 && ss==0){
			result = false;
		} else {
			result = true;
		}
		
		hh = 0;
		mm = 0;
		ss = 0;
		this.lastTime = currTime;
		
		return result;
	}
	
	public synchronized boolean incSec() {
		if (ss<59) {
			ss++;
		} else {
			ss = 0;
			if (mm<59) {
				mm++;
			} else {
				mm = 0;
				hh++;
			}
		}
		return true;
	}
	
	public synchronized boolean tick(long newTime) {
		if (state != State.RUNNING){
			return false;
		}
		return calculateNewTime(newTime);
	}
	
	protected synchronized boolean calculateNewTime(long newTime) {
		long passedTime = newTime - lastTime;
		if (passedTime < 1000){
			return false;
		}
		while (passedTime >= 1000){
			lastTime += 1000;
			passedTime -= 1000;
			incSec();
		}
		return true;
	}
	
	public synchronized String getTime() {
		return "" + (hh < 10 ? "0" + hh : hh) + ":" + (mm < 10 ? "0" + mm : mm) + ":" + (ss < 10 ? "0" + ss : ss);
	}

	public synchronized boolean start(long newTime) {
		if (state == State.PAUSED) {
			state = State.RUNNING;
			lastTime = newTime;
			return false;//calculateNewTime(newTime);
		} else {
			state = State.RUNNING;
			return resetTime(newTime);
		}
	}

	public synchronized boolean pause(long newTime) {
		boolean result = false;
		if (state == State.RUNNING) {
			state = State.PAUSED;
			result = calculateNewTime(newTime);
			return result;
		}
		/*
		if (state == State.PAUSED) {
			state = State.RUNNING;
			result = calculateNewTime(newTime);
		}
		*/
		return result;
	}

	public synchronized boolean stop(long newTime) {
		if (state == State.STOPPED) {
			return false;
		}
		boolean result = calculateNewTime(newTime);
		state = State.STOPPED;
		return result;
	}
	
	public synchronized boolean isStopped(){
		return (state == State.STOPPED);
	}

	public synchronized int getTimeInSec() {
		return (hh * 60 * 60) + (mm * 60) + ss;
	}

}
