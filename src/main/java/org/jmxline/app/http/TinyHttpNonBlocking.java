package org.jmxline.app.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class TinyHttpNonBlocking {

    public TinyHttpNonBlocking() throws IOException {

        Selector selector = null;
        try {
            // Create the selector
            selector = Selector.open();

            // Create two non-blocking sockets. This method is implemented in
            // Creating a Non-Blocking Socket.
            SocketChannel sChannel1 = createSocketChannel("hostname.com", 80);
            SocketChannel sChannel2 = createSocketChannel("hostname.com", 80);

            // Register the channel with selector, listening for all events
            sChannel1.register(selector, sChannel1.validOps());
            sChannel2.register(selector, sChannel1.validOps());

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Wait for events
        while (true) {
            try {
                // Wait for an event
                selector.select();
            } catch (IOException e) {
                // Handle error with selector
                break;
            }

            // Get list of selection keys with pending events
            Iterator it = selector.selectedKeys().iterator();

            // Process each key at a time
            while (it.hasNext()) {
                // Get the selection key
                SelectionKey selKey = (SelectionKey) it.next();

                // Remove it from the list to indicate that it is being
                // processed
                it.remove();

                try {
                    processSelectionKey(selKey);
                } catch (IOException e) {
                    // Handle error with channel and unregister
                    selKey.cancel();
                }
            }
        }

    }

    // Creates a non-blocking socket channel for the specified host name and
    // port.
    // connect() is called on the new channel before it is returned.
    public static SocketChannel createSocketChannel(String hostName, int port) throws IOException {
        // Create a non-blocking socket channel
        SocketChannel sChannel = SocketChannel.open();
        sChannel.configureBlocking(false);

        // Send a connection request to the server; this method is non-blocking
        sChannel.connect(new InetSocketAddress(hostName, port));
        return sChannel;
    }

    public void processSelectionKey(SelectionKey selKey) throws IOException {
        // Since the ready operations are cumulative,
        // need to check readiness for each operation
        if (selKey.isValid() && selKey.isConnectable()) {
            // Get channel with connection request
            SocketChannel sChannel = (SocketChannel) selKey.channel();

            boolean success = sChannel.finishConnect();
            if (!success) {
                // An error occurred; handle it

                // Unregister the channel with this selector
                selKey.cancel();
            }
        }
        if (selKey.isValid() && selKey.isReadable()) {
            // Get channel with bytes to read
            SocketChannel sChannel = (SocketChannel) selKey.channel();

            // See Reading from a SocketChannel
            readFromChannel(sChannel);
        }
        if (selKey.isValid() && selKey.isWritable()) {
            // Get channel that's ready for more bytes
            SocketChannel sChannel = (SocketChannel) selKey.channel();

            // See Writing to a SocketChannel
            writeToChannel(sChannel);
        }
    }

    private void writeToChannel(SocketChannel sChannel) {
        // Create a direct buffer to get bytes from socket.
        // Direct buffers should be long-lived and be reused as much as
        // possible.
        ByteBuffer buf = ByteBuffer.allocateDirect(1024);

        try {
            // Fill the buffer with the bytes to write;
            // see Putting Bytes into a ByteBuffer
            buf.put((byte) 0xFF);

            // Prepare the buffer for reading by the socket
            buf.flip();

            // Write bytes
            int numBytesWritten = sChannel.write(buf);
        } catch (IOException e) {
            // Connection may have been closed
        }
    }

    private void readFromChannel(SocketChannel sChannel) {
        // Create a direct buffer to get bytes from socket.
        // Direct buffers should be long-lived and be reused as much as
        // possible.
        ByteBuffer buf = ByteBuffer.allocateDirect(1024);

        try {
            // Clear the buffer and read bytes from socket
            buf.clear();
            int numBytesRead = sChannel.read(buf);

            if (numBytesRead == -1) {
                // No more bytes can be read from the channel
                sChannel.close();
            } else {
                // To read the bytes, flip the buffer
                buf.flip();

                // Read the bytes from the buffer ...;
                // see Getting Bytes from a ByteBuffer
            }
        } catch (IOException e) {
            // Connection may have been closed
        }
    }

    public static void main(String[] args) {

    }
}
