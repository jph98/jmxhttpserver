package se.rupy.http;

import java.util.*;

public class Chain extends LinkedList {
	private int next;

	/*
	 * Dynamic size list with positional integrity. If anyone has a better
	 * solution to this please tell me!
	 */
	protected Link put(Link link) {
		for(int i = 0; i < size(); i++) {
			Link tmp = (Link) super.get(i);
			
			if (link.index() == tmp.index()) {
				return (Link) set(i, link);
			}
			else if (link.index() < tmp.index()) {
				add(i, link);
				return null;
			}
		}
		
		add(link);
		
		return null;
	}

	public void filter(Event event) throws Event, Exception {
		for (int i = 0; i < size(); i++) {
			Service service = (Service) get(i);

			if (event.daemon().timeout > 0) {
				event.session(service);
			}

			service.filter(event);
		}
	}

	protected void exit(Session session, int type) throws Exception {
		for (int i = 0; i < size(); i++) {
			Service service = (Service) get(i);
			service.session(session, type);
		}
	}

	protected void reset() {
		next = 0;
	}

	protected Link next() {
		if (next >= size()) {
			next = 0;
			return null;
		}

		return (Link) get(next++);
	}

	public interface Link {
		public int index();
	}
	
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		Iterator it = iterator();

		buffer.append('[');
		
		while(it.hasNext()) {
			Object object = it.next();
			String name = object.getClass().getName();
			
			if(name.equals("se.rupy.http.Event")) {
				buffer.append(object);
			}
			else {
				int dollar = name.lastIndexOf('$');
				int dot = name.lastIndexOf('.');
				if(dollar > 0) {
					name = name.substring(dollar + 1);
				}
				else if(dot > 0) {
					name = name.substring(dot + 1);
				}				
				buffer.append(name);
			}
			
			if(it.hasNext()) {
				buffer.append(", ");
			}
		}
		
		buffer.append(']');
		
		return buffer.toString();
	}
}
