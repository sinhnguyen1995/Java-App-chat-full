//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class ReceiverHandler implements Runnable {
    private static final ByteBuffer REUSABLE_BYTE_BUFFER = ByteBuffer.allocate(1024);
    private static final CharBuffer REUSABLE_CHAR_BUFFER = CharBuffer.allocate(1024);
    private final SocketChannel sChannel;
    private final CharsetDecoder decoder;
    private final CharsetEncoder encoder;

    public ReceiverHandler(SocketChannel sChannel) {
        this.decoder = StandardCharsets.UTF_8.newDecoder();
        this.encoder = StandardCharsets.UTF_8.newEncoder();
        this.sChannel = sChannel;
    }

    public void run() {
        while(true) {
            if (this.sChannel.isConnected()) {
                try {
                    CharBuffer charBuffer = this.read(this.sChannel);
                    Object obj = JSONValue.parse(charBuffer.toString());
                    JSONObject jsonObject = (JSONObject)obj;
                    String outputMessage = (String)jsonObject.get("message");
                    String action = (String)jsonObject.get("type");
                    if (action.equalsIgnoreCase("broadcast")) {
                        System.out.println();
                        System.out.println("----Someone is broadcasting----");
                        System.out.println("Content: " + outputMessage);
                    }

                    if (action.equalsIgnoreCase("hot")) {
                        System.out.println();
                        System.out.println("----Someone is broadcasting to your subscribe topic----");
                        System.out.println("Content: " + outputMessage);
                        continue;
                    }

                    System.out.println("SERVER>>> " + outputMessage);
                    continue;
                } catch (IOException var6) {
                    var6.printStackTrace();
                }
            }

            return;
        }
    }

    public CharBuffer read(SocketChannel client) throws IOException {
        REUSABLE_BYTE_BUFFER.clear();
        boolean eof = client.read(REUSABLE_BYTE_BUFFER) == -1;
        REUSABLE_BYTE_BUFFER.flip();

        CoderResult decodeResult;
        do {
            REUSABLE_CHAR_BUFFER.clear();
            decodeResult = this.decoder.decode(REUSABLE_BYTE_BUFFER, REUSABLE_CHAR_BUFFER, false);
            REUSABLE_CHAR_BUFFER.flip();
        } while(decodeResult == CoderResult.OVERFLOW);

        return REUSABLE_CHAR_BUFFER;
    }
}
