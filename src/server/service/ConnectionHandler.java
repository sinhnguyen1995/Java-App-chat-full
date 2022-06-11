package server.service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;

public class ConnectionHandler {
    private static final ByteBuffer REUSABLE_BYTE_BUFFER = ByteBuffer.allocate(1024);
    private static final CharBuffer REUSABLE_CHAR_BUFFER = CharBuffer.allocate(1024);

    private final CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
    private final CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder();

    public ConnectionHandler() {
    }

    public CharBuffer read(SocketChannel client) throws IOException {
        REUSABLE_BYTE_BUFFER.clear();
        boolean eof = client.read(REUSABLE_BYTE_BUFFER) == -1;
        REUSABLE_BYTE_BUFFER.flip();
        CoderResult decodeResult;
        do {
            REUSABLE_CHAR_BUFFER.clear();
            decodeResult = decoder.decode(REUSABLE_BYTE_BUFFER, REUSABLE_CHAR_BUFFER, false);
            REUSABLE_CHAR_BUFFER.flip();
        } while (decodeResult == CoderResult.OVERFLOW);

        return REUSABLE_CHAR_BUFFER;
    }

}