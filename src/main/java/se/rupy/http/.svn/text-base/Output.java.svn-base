package se.rupy.http;

import java.io.*;
import java.nio.*;
import java.util.*;

/**
 * Handles the outgoing response data.
 * 
 * @author marc.larue
 */
public abstract class Output extends OutputStream implements Event.Block {
	private final static String EOL = "\r\n";
	private final static byte[] server = ("Server: Rupy/0.3.8" + EOL).getBytes();
	private final static byte[] close = ("Connection: Close" + EOL).getBytes();
	private final static byte[] alive = ("Connection: Keep-Alive" + EOL).getBytes();
	private final static byte[] chunked = ("Transfer-Encoding: Chunked" + EOL).getBytes();
	
	private ByteArrayOutputStream array;
	private byte[] one = new byte[1];
	private boolean chunk, cache;
	protected int length, size;
	protected Reply reply;
	protected boolean init, push, fixed, done;

	Output(Reply reply) throws IOException {
		this.reply = reply;
		size = reply.event().daemon().size;
	}

	protected boolean chunk() {
		return chunk;
	}

	/**
	 * Used for comet applications to be able to prune 
	 * disconnected clients.
	 * @return If the push has been completed.
	 */
	public boolean complete() {
		return !push && done;
	}
	
	public void println(Object o) throws IOException {
		write((o.toString() + EOL).getBytes("UTF-8"));
	}

	public void println(long l) throws IOException {
		write((String.valueOf(l) + EOL).getBytes("UTF-8"));
	}

	public void println(boolean b) throws IOException {
		write((String.valueOf(b) + EOL).getBytes("UTF-8"));
	}

	public void print(Object o) throws IOException {
		write(o.toString().getBytes("UTF-8"));
	}

	public void print(long l) throws IOException {
		write(String.valueOf(l).getBytes("UTF-8"));
	}

	public void print(boolean b) throws IOException {
		write(String.valueOf(b).getBytes("UTF-8"));
	}

	protected void init(long length) throws IOException {
		if (init) {
			reply.event().log("already inited", Event.DEBUG);
			return;
		} else
			reply.event().log("init " + reply.event().query().version() + " " + length,
					Event.DEBUG);

		done = false;
		
		chunk = reply.event().query().version().equalsIgnoreCase("HTTP/1.1");
		reply.event().interest(Event.WRITE);

		init = true;
		
		if(length > 0) {
			fixed = true;
			headers(length);
		} else if (chunk) {
			/*
			 * TODO: What am I doing wrong?
			 * 
			 * Browsers do NOT support "Transfer-Encoding: Chunked" with length
			 * 0 from status codes that they expect to have "Content-Length: 0"
			 * for some reason it seems? Or is it my fault?
			 * 
			 * If an Event returns chunked with length 0 it will, upon next use,
			 * leave the browser waiting for more... then "An established
			 * connection was aborted by the software in your host machine" with
			 * IE and "Socket closed" with Firefox. Help needed!
			 * 
			 * This workaround works fine for now though. See end() and
			 * Chunked.flush().
			 */
			if (reply.code().startsWith("302")
					|| reply.code().startsWith("304")) {
				headers(0);
			} else {
				headers(-1);
			}
		} else {
			cache = true;

			if (array == null) {
				array = new ByteArrayOutputStream();
			}
		}
	}

	protected void end() throws IOException {
		reply.event().log("end", Event.DEBUG);

		if (!chunk) {
			if (array != null && array.size() > 0) {
				array.flush();

				int length = array.size();
				byte[] data = array.toByteArray();

				headers(length);
				write(data, 0, length);

				array.reset();
			} else if (reply.code().startsWith("302")
					|| reply.code().startsWith("304")) {
				headers(0);
			}
		}

		done = true;

		flush();

		/*
		 * Added this to fix the push bug that adding the 
		 * fixed flag caused. If you look in the flush method 
		 * at the bottom of this file you will see why this is needed.
		 * TODO: Add test unit for fixed length!
		 */
		fixed = false;
		
		if (length > 0) {
			reply.event().log("reply " + length, Event.VERBOSE);
		}

		reply.event().interest(Event.READ);

		init = false;
		length = 0;
	}

	protected void headers(long length) throws IOException {
		cache = false;

		reply.event().log("code " + reply.code(), Event.VERBOSE);

		wrote((reply.event().query().version() + " " + reply.code() + EOL)
				.getBytes());
		wrote(("Date: " + reply.event().DATE.format(new Date()) + EOL)
				.getBytes());
		wrote(server);
		wrote(("Content-Type: " + reply.type() + EOL).getBytes());

		if (length > -1) {
			wrote(("Content-Length: " + length + EOL).getBytes());
		} else {
			wrote(chunked);
		}

		if (reply.modified() > 0) {
			wrote(("Last-Modified: "
					+ reply.event().DATE.format(new Date(reply.modified())) + EOL)
					.getBytes());
		}
		
		if (fixed && reply.event().daemon().properties.getProperty("live") != null) {
			wrote(("Cache-Control: max-age=3600, must-revalidate" + EOL)
					.getBytes());
			wrote(("Expires: "
					+ reply.event().DATE.format(new Date(System.currentTimeMillis() + ((long) 1000 * 60 * 60 * 24 * 365))) + EOL)
					.getBytes());
		}

		if (reply.event().session() != null && !reply.event().session().set()) {
			Session session = reply.event().session();
			String cookie = "Set-Cookie: key="
					+ reply.event().session().key()
					+ ";"
					+ (session.expires() > 0 ? " expires="
							+ reply.event().DATE.format(new Date(session
									.expires())) + ";" : "")
					+ (session.domain() != null ? " domain=" + session.domain()
							+ ";" : "") + " path=/;";

			wrote((cookie + EOL).getBytes());

			reply.event().session().set(true);
			reply.event().log("cookie " + cookie, Event.VERBOSE);
		}

		if (reply.event().close()) {
			wrote(close);
		} else {
			wrote(alive);
		}

		HashMap headers = reply.headers();

		if (headers != null) {
			Iterator it = headers.keySet().iterator();

			while (it.hasNext()) {
				String name = (String) it.next();
				String value = (String) reply.headers().get(name);

				wrote((name + ": " + value + EOL).getBytes());
			}
		}

		wrote(EOL.getBytes());
		// flush();
		length = 0;
	}

	protected void wrote(int b) throws IOException {
		one[0] = (byte) b;
		wrote(one);
	}

	protected void wrote(byte[] b) throws IOException {
		wrote(b, 0, b.length);
	}

	protected void wrote(byte[] b, int off, int len) throws IOException {
		try {
			if (cache) {
				array.write(b, off, len);
			} else {
				ByteBuffer out = reply.event().worker().out();
				int remaining = out.remaining();

				while (len > remaining) {
					out.put(b, off, remaining);

					internal(false);

					off += remaining;
					len -= remaining;

					// reply.event().log("wrote off " + off + " len " + len + "
					// remaining " + remaining, Event.DEBUG);

					/*
					 * oh, nasty little bugger, added this when a page with
					 * exact multiple of IO buffer length was sent and the
					 * trailing empty chunked line blocked the server at 99%
					 * CPU!
					 */
					remaining = out.remaining();
				}

				if (len > 0) {
					out.put(b, off, len);
				}
			}
		} catch (IOException e) {
			Failure.chain(e);
		} catch (Exception e) {
			throw (IOException) new IOException().initCause(e);
		}
	}

	protected void internal(boolean debug) throws Exception {
		ByteBuffer out = reply.event().worker().out();

		if (out.remaining() < size) {
			out.flip();

			while (out.remaining() > 0) {
				int sent = fill(debug);

				if (debug) {
					reply.event().log(
							"sent " + sent + " remaining " + out.remaining(),
							Event.DEBUG);
				}

				if (sent == 0) {
					reply.event().block(this);

					if (debug) {
						reply.event().log("still in buffer " + out.remaining(),
								Event.DEBUG);
					}
				}
			}
		}

		out.clear();
	}

	public void flush() throws IOException {
		reply.event().log("flush " + length, Event.DEBUG);
		try {
			internal(true);
		} catch (Exception e) {
			throw (Failure) new Failure("No flush!").initCause(e);
		}
	}

	public int fill(boolean debug) throws IOException {
		ByteBuffer out = reply.event().worker().out();

		int remaining = 0;

		if (debug) {
			remaining = out.remaining();
		}

		int sent = reply.event().channel().write(out);

		if (debug) {
			reply.event().log("filled " + sent + " out of " + remaining,
					Event.DEBUG);
		}

		length += sent;
		return sent;
	}
	
	/**
	 * Flush the terminating empty chunk of a asynchronous stream push. An
	 * event becomes an asynchronous stream push if a request is not written
	 * any data to in the first {@link Service#filter(Event)} call.
	 * 
	 * @throws IOException
	 */
	public abstract void finish() throws IOException;

	/*
	 * Borrowed from sun.net.httpserver.ChunkedOutputStream.java
	 */
	static class Chunked extends Output {
		public static int OFFSET = 6;
		private int cursor = OFFSET, count = 0;

		// private byte[] chunk;

		Chunked(Reply reply) throws IOException {
			super(reply);
		}

		public void write(int b) throws IOException {
			/*
			if (!chunk() || fixed) {
				wrote(b);
				return;
			}
			*/
			reply.event().worker().chunk()[cursor++] = (byte) b;
			count++;

			if (count == size) {
				write();
			}
		}

		public void write(byte[] b) throws IOException {
			write(b, 0, b.length);
		}

		public void write(byte[] b, int off, int len) throws IOException {
			if (!chunk() || fixed) {
				wrote(b, off, len);
				return;
			}

			byte[] chunk = reply.event().worker().chunk();
			int remain = size - count;

			if (len > remain) {
				System.arraycopy(b, off, chunk, cursor, remain);

				count = size;
				write();

				len -= remain;
				off += remain;

				while (len > size) {
					System.arraycopy(b, off, chunk, OFFSET, size);

					len -= size;
					off += size;

					count = size;
					write();
				}

				cursor = OFFSET;
			}
			if (len > 0) {
				System.arraycopy(b, off, chunk, cursor, len);
				count += len;
				cursor += len;
			}
		}

		protected void write() throws IOException {
			byte[] chunk = reply.event().worker().chunk();
			char[] header = Integer.toHexString(count).toCharArray();
			int length = header.length, start = 4 - length, cursor;

			for (cursor = 0; cursor < length; cursor++) {
				chunk[start + cursor] = (byte) header[cursor];
			}

			chunk[start + (cursor++)] = '\r';
			chunk[start + (cursor++)] = '\n';
			chunk[start + (cursor++) + count] = '\r';
			chunk[start + (cursor++) + count] = '\n';

			wrote(chunk, start, cursor + count);

			count = 0;
			this.cursor = OFFSET;
		}

		public void finish() throws IOException {
			if (complete()) {
				throw new IOException("Reply already complete.");
			}

			push = false;
		}

		public void flush() throws IOException {
			if (chunk() && init) {
				if (reply.code().startsWith("302")
						|| reply.code().startsWith("304")) {
					reply.event().log("length " + length, Event.DEBUG);
				} else if (!fixed) {
					if (count > 0) {
						write();
					}

					if (complete()) {
						write();
					}

					reply.event().log("chunk flush " + length, Event.DEBUG);
				}
			} else if (!fixed) {
				reply.event().log("asynchronous push " + count, Event.DEBUG);
				push = true;
			}

			super.flush();
		}
	}
}
