package com.xq.websocketcom;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class WebSocketClientCom {

    private final Map<String, WebSocketClientChannel> socketClientMap = new HashMap<>();

    public void connect(final String address, Map<String,?> header, Map<String,?> urlParam,final OnConnectListener onConnectListener) {
        synchronized (socketClientMap){
            try {
                final AtomicReference<WebSocketClientChannel> reference = new AtomicReference<>();
                WebSocketClient webSocketClient = new WebSocketClient(new URI(concatUrlParam(address,urlParam)),toStringMap(header)) {
                    final AtomicBoolean firstConnectCallback = new AtomicBoolean(false);
                    @Override
                    public void onOpen(ServerHandshake handshakedata) {
                        if (firstConnectCallback.compareAndSet(false,true)){
                            onConnectListener.onSuccess(reference.get());
                        }
                    }

                    @Override
                    public void onMessage(String message) {
                        reference.get().onTextMessage(message);
                    }

                    @Override
                    public void onMessage(ByteBuffer byteBuffer) {
                        reference.get().onOctMessage(byteBuffer.array(),0,byteBuffer.position());
                    }

                    @Override
                    public void onClose(int code, String reason, boolean remote) {
                        if (firstConnectCallback.compareAndSet(false,true)){
                            onConnectListener.onError(reason,String.valueOf(code));
                        } else {
                            if (containAndRemoveAddress()){
                                reference.get().onDisConnected();
                            }
                        }
                    }

                    @Override
                    public void onError(Exception ex) {

                    }

                    private boolean containAndRemoveAddress(){
                        if (socketClientMap.containsKey(address)){
                            synchronized (socketClientMap){
                                if (socketClientMap.containsKey(address)){
                                    socketClientMap.remove(address);
                                    return true;
                                }
                            }
                        }
                        return false;
                    }

                };
                reference.set(new WebSocketClientChannel(webSocketClient));
                socketClientMap.put(address, reference.get());
                webSocketClient.connect();
            } catch (URISyntaxException e) {
                e.printStackTrace();
                onConnectListener.onError(e.getMessage(),"");
            }
        }
    }

    public void disConnect(String address){
        if (socketClientMap.containsKey(address)){
            synchronized (socketClientMap){
                if (socketClientMap.containsKey(address)){
                    WebSocketClientChannel webSocketClientChannel = socketClientMap.remove(address);
                    webSocketClientChannel.close();
                }
            }
        }
    }

    public boolean isConnected(String address){
        return socketClientMap.containsKey(address);
    }

    private String concatUrlParam(String url,final Map<String, ?> form){
        return form == null || form.isEmpty()? url : url + (url.contains("?")?"&":"?") + mapToFromString(form);
    }

    private Map<String,String> toStringMap(Map<String,?> map){
        Map<String,String> finalMap = new HashMap<>();
        for (Map.Entry<String,?> entry : map.entrySet()){
            finalMap.put(entry.getKey(), Objects.toString(map.values()));
        }
        return finalMap;
    }

    private String mapToFromString(final Map<String, ?> form){
        final StringBuilder sb = new StringBuilder();
        for (String key : form.keySet()) {
            if (sb.length() > 0) sb.append("&");
            sb.append(key).append("=").append(form.get(key));
        }
        return sb.toString();
    }

    public interface OnConnectListener{
        void onSuccess(WebSocketClientChannel webSocketClientChannel);
        void onError(String info,String code);
    }

    interface OnTextMessage{
        void onTextMessage(String text);
    }

    interface OnOctMessage{
        void onOctMessage(byte[] bytes,int offset,int length);
    }

    interface OnDisConnected{
        void onDisConnected();
    }

}
