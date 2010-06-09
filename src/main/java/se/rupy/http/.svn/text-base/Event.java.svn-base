package se.rupy.http;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.text.*;
import java.util.*;

import java.nio.channels.*;

/**
 * Asynchronous HTTP request/response, this virtually represents a client
 * socket, but in the case where the server is behind a proxy we cannot depend
 * on that fact since sockets will be reused by multiple different external
 * clients. It's a performance tradeoff that we gladly accept though, since
 * hiding behind an Apache or some other proxy is this servers most probable
 * use.
 * 
 * @author marc
 */
public class Event extends Throwable implements Chain.Link {
	static int READ = 1 << 0;
	static int WRITE = 1 << 2;
	static int VERBOSE = 1 << 0;
	static int DEBUG = 1 << 1;

	private static char[] BASE_24 = { 'B', 'C', 'D', 'F', 'G', 'H', 'J', 'K',
		'M', 'P', 'Q', 'R', 'T', 'V', 'W', 'X', 'Y', '2', '3', '4', '6',
		'7', '8', '9' };
	static DateFormat DATE;
	static Mime MIME;

	static {
		MIME = new Mime();
		DATE = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
		DATE.setTimeZone(TimeZone.getTimeZone("GMT"));
		READ = SelectionKey.OP_READ;
		WRITE = SelectionKey.OP_WRITE;
	}

	private SocketChannel channel;
	private SelectionKey key;

	private Query query;
	private Reply reply;
	private Session session;

	private Daemon daemon;
	private Worker worker;

	private int index, interest;
	private String remote;
	private boolean close, push;

	protected Event(Daemon daemon, SelectionKey key, int index) throws IOException {
		channel = ((ServerSocketChannel) key.channel()).accept();
		channel.configureBlocking(false);

		this.daemon = daemon;
		this.index = index;
		this.key = key;

		query = new Query(this);
		reply = new Reply(this);

		key = channel.register(key.selector(), READ, this);
		key.selector().wakeup();
	}

	protected int interest() {
		return interest;
	}

	protected void interest(int interest) {
		this.interest = interest;
	}

	public Daemon daemon() {
		return daemon;
	}

	public Query query() {
		return query;
	}

	public Reply reply() {
		return reply;
	}

	public Session session() {
		return session;
	}

	//void session(Session session) {
	//	this.session = session;
	//}

	public String remote() {
		return remote;
	}

	public boolean close() {
		return close;
	}

	public Worker worker() {
		return worker;
	}

	public int index() {
		return index;
	}

	protected void close(boolean close) {
		this.close = close;
	}

	protected void worker(Worker worker) {
		this.worker = worker;
		register(READ);
	}

	protected SocketChannel channel() {
		return channel;
	}

	protected void log(Object o) {
		log(o, Event.DEBUG);
	}

	protected void log(Object o, int level) {
		if (o instanceof Exception && daemon.debug) {
			daemon.out.print("[" + (worker == null ? "*" : "" + worker.index())
					+ "-" + index + "] ");
			((Exception) o).printStackTrace(daemon.out);
		} else if (daemon.debug || daemon.verbose && level == Event.VERBOSE)
			daemon.out.println("["
					+ (worker == null ? "*" : "" + worker.index()) + "-"
					+ index + "] " + o);
	}

	/**
	 * @return same as {@link Query#big(String)}.
	 */
	public long big(String key) {
		return query.big(key);
	}

	/**
	 * @return same as {@link Query#medium(String)}.
	 */
	public int medium(String key) {
		return query.medium(key);
	}

	/**
	 * @return same as {@link Query#small(String)}.
	 */
	public short small(String key) {
		return query.small(key);
	}

	/**
	 * @return same as {@link Query#tiny(String)}.
	 */
	public byte tiny(String key) {
		return query.tiny(key);
	}

	/**
	 * @return same as {@link Query#bit(String, boolean)}.
	 */
	public boolean bit(String key) {
		return query.bit(key, true);
	}

	/**
	 * @return same as {@link Query#string(String)}.
	 */
	public String string(String key) {
		return query.string(key);
	}

	/**
	 * @return same as {@link Query#input()}.
	 */
	public Input input() {
		return query.input();
	}

	/**
	 * @return same as {@link Reply#output()}.
	 * @throws IOException
	 */
	public Output output() throws IOException {
		return reply.output();
	}

	protected void read() throws IOException {
		query.headers();
		remote = address();

		if (!content() && !service()) {
			reply.code("404 Not Found");
			reply.output().print(
					"<pre>'" + query.path() + "' was not found.</pre>");
		}

		reply.done();
		query.done();
	}

	protected String address() {
		String remote = query.header("x-forwarded-for");

		if (remote == null) {
			InetSocketAddress address = (InetSocketAddress) channel.socket()
			.getRemoteSocketAddress();
			remote = address.getAddress().getHostAddress();
		}

		log("remote " + remote, VERBOSE);

		return remote;
	}

	protected boolean content() throws IOException {
		Deploy.Stream stream = daemon.content(query);

		if (stream == null)
			return false;

		String type = MIME.content(query.path(), "application/octet-stream");

		reply.type(type);
		reply.modified(stream.date());

		if (query.modified() == 0 || query.modified() < reply.modified()) {
			Deploy.pipe(stream.input(), reply.output(stream.length()));
			log("content " + type, VERBOSE);
		} else {
			reply.code("304 Not Modified");
		}

		return true;
	}

	protected boolean service() throws IOException {
		Chain chain = daemon.chain(query);

		if (chain == null) {
			chain = daemon.chain("null");

			if(chain == null)
				return false;
		}

		try {
			chain.filter(this);
		} catch (Failure f) {
			throw f;
		} catch (Event e) {
			// Break the filter chain.
		} catch (Exception e) {
			log(e);

			StringWriter trace = new StringWriter();
			PrintWriter print = new PrintWriter(trace);
			e.printStackTrace(print);

			reply.code("500 Internal Server Error");
			reply.output().print("<pre>" + trace.toString() + "</pre>");
		}

		return true;
	}

	protected void write() throws IOException {
		service();
		reply.done();
		query.done();
	}

	protected void register() throws IOException {
		if (interest != key.interestOps()) {
			log((interest == READ ? "read" : "write") + " prereg " + interest
					+ " " + key.interestOps() + " " + key.readyOps(), DEBUG);
			key = channel.register(key.selector(), interest, this);
			log((interest == READ ? "read" : "write") + " postreg " + interest
					+ " " + key.interestOps() + " " + key.readyOps(), DEBUG);
		}

		key.selector().wakeup();

		log((interest == READ ? "read" : "write") + " wakeup", DEBUG);
	}

	protected void register(int interest) {
		interest(interest);

		try {
			if (channel.isOpen())
				register();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected int block(Block block) throws Exception {
		long max = System.currentTimeMillis() + daemon.delay;

		while (System.currentTimeMillis() < max) {
			register();
			
			int available = block.fill(true);

			if (available > 0) {
				long delay = daemon.delay - (max - System.currentTimeMillis());
				log("delay " + delay + " " + available, VERBOSE);
				return available;
			}
			
			worker.snooze(10);

			key.selector().wakeup();
		}

		throw new Exception("IO timeout.");
	}

	interface Block {
		public int fill(boolean debug) throws IOException;
	}

	protected void disconnect(Exception e) {
		try {
			if (channel != null) { // && channel.isOpen()) {
				channel.close();
			}

			if (key != null) {
				key.cancel();
			}

			if (session != null) {
				session.remove(this);
			}

			log("disconnect " + e);

			worker.snooze(5); // to avoid deadlock when proxy closes socket
		} catch (Exception de) {
			de.printStackTrace(daemon.out);
		}
	}

	protected final void session(Service service) throws Exception {
		String key = cookie(query.header("cookie"), "key");

		if(key == null && query.method() == Query.GET) {
			/*
			 * XSS comet cookie: this means GETs are parsed!
			 */
			query.parse();
			String cookie = query.string("cookie");
			key = cookie.length() > 0 ? cookie : null;
			
		}

		if (key != null) {
			session = (Session) daemon.session().get(key);

			if (session != null) {
				log("old key " + key, VERBOSE);

				session.add(this);
				session.touch();

				return;
			}
		}

		if(service.index() == 0) {
			session = new Session(daemon);
			session.add(service);
			session.add(this);
			session.key(key);

			if (session.key() == null) {
				do {
					key = random(daemon.cookie);
				} while (daemon.session().get(key) != null);
				session.key(key);
			}

			synchronized (daemon.session()) {
				log("new key " + session.key(), VERBOSE);
				daemon.session().put(session.key(), session);
			}
		}

		try {
			service.session(session, Service.CREATE);
		} catch (Exception e) {
			e.printStackTrace(daemon.out);
		}
	}

	public static String cookie(String cookie, String key) {
		String value = null;

		if (cookie != null) {
			StringTokenizer tokenizer = new StringTokenizer(cookie, " ");

			while (tokenizer.hasMoreTokens()) {
				String part = tokenizer.nextToken();
				int equals = part.indexOf("=");

				if (equals > -1 && part.substring(0, equals).equals(key)) {
					String subpart = part.substring(equals + 1);

					if (subpart.endsWith(";")) {
						value = subpart.substring(0, subpart.length() - 1);
					} else {
						value = subpart;
					}
				}
			}
		}

		return value;
	}

	public static String random(int length) {
		Random random = new Random();
		StringBuffer buffer = new StringBuffer();

		while (buffer.length() < length) {
			buffer.append(BASE_24[Math.abs(random.nextInt() % 24)]);
		}

		return buffer.toString();
	}

	public String toString() {
		return String.valueOf(index);
	}

	/**
	 * @return true if the Event is being recycled due to a call to
	 *         {@link Reply#wakeup()}.
	 */
	public boolean push() {
		return push;
	}

	protected void push(boolean push) {
		this.push = push;
	}

	/**
	 * Keeps the chunked reply open for asynchronous writes. If you are 
	 * streaming data and you need to send something upon the first request 
	 * you have to call this in order to avoid that the trailing zero length 
	 * chunk is sent to complete the response.
	 * @throws IOException
	 */
	public void hold() throws IOException {
		output().push = true;
		this.push = true;
	}

	static class Mime extends Properties {
		public Mime() {
			try {
				load(Mime.class.getResourceAsStream("/mime.txt"));
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}

		String content(String path, String fail) {
			int index = path.lastIndexOf('.') + 1;

			if(index > 0) {
				return getProperty(path.substring(index), fail);
			}

			return fail;
		}
	}
}
