package se.rupy.http;

import java.nio.*;

/**
 * Worker gets the job done. The worker holds the in/out/chunk buffers in order to
 * save resources, since the worker is assigned per event until a request is
 * completed.
 * 
 * @author marc
 */
public class Worker implements Runnable, Chain.Link {
	private Daemon daemon;
	private ByteBuffer in, out;
	private byte[] chunk;
	private Thread thread;
	private Event event;
	private int index;
	private boolean awake, alive;

	Worker(Daemon daemon, int index) {
		this.daemon = daemon;
		this.index = index;

		in = ByteBuffer.allocateDirect(daemon.size);
		out = ByteBuffer.allocateDirect(daemon.size);

		alive = true;
		
		thread = new Thread(this);
		thread.start();
	}

	ByteBuffer in() {
		return in;
	}

	ByteBuffer out() {
		return out;
	}

	byte[] chunk() {
		if(chunk == null) {
			chunk = new byte[daemon.size + Output.Chunked.OFFSET + 2];
		}
	
		return chunk;
	}
	
	void wakeup() {
		if(event != null)
			event.log("wakeup", Event.DEBUG);
		
		synchronized (thread) {
			thread.notify();
		}
		
		awake = true;
	}

	void snooze() {
		snooze(0);
	}

	void snooze(long delay) {
		if(event != null)
			event.log("snooze " + delay, Event.DEBUG);
		
		synchronized (thread) {
			try {
				if (delay > 0) {
					if(awake) {
						awake = false;
						return;
					}
					
					thread.wait(delay);
				} else {
					thread.wait();
				}
			} catch (InterruptedException e) {
				event.disconnect(e);
			}
			
			awake = false;
		}
	}

	void event(Event event) {
		this.event = event;
	}

	boolean busy() {
		return event != null;
	}

	public int index() {
		return index;
	}
	
	public void stop() {
		synchronized (thread) {
			thread.notify();
		}
		
		alive = false;
	}

	public void run() {
		while (alive) {
			try {
				if (event != null) {
					if (event.push()) {
						event.write();
						event.push(false);
					} else {
						event.read();
					}
				}
			} catch (Exception e) {
				event.disconnect(e);
			} finally {
				if (event != null) {
					event.worker(null);
					event = daemon.next(this);

					if (event != null) {
						event.worker(this);
					} else {
						snooze();
					}
				} else {
					snooze();
				}
			}
		}
	}
}
