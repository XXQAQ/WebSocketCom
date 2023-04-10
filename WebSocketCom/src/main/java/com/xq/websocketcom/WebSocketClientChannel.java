package com.xq.websocketcom;

import org.java_websocket.client.WebSocketClient;

public class WebSocketClientChannel implements WebSocketClientCom.OnTextMessage, WebSocketClientCom.OnOctMessage, WebSocketClientCom.OnDisConnected {

    private WebSocketClient webSocketClient;

    public WebSocketClientChannel(WebSocketClient webSocketClient) {
        this.webSocketClient = webSocketClient;
    }

    public void close(){
        webSocketClient.close();
    }

    private OnReceiveTextListener onReceiveTextListener;
    public void setOnReceiveTextListener(OnReceiveTextListener onReceiveTextListener){
        this.onReceiveTextListener = onReceiveTextListener;
    }

    @Override
    public void onTextMessage(String text) {
        try {
            onReceiveTextListener.onReceiveText(text);
        } catch (NullPointerException e) {

        }
    }

    private OnReceiveOctetListener onReceiveOctetListener;
    public void setOnReceiveOctetListener(OnReceiveOctetListener onReceiveOctetListener) {
        this.onReceiveOctetListener = onReceiveOctetListener;
    }

    @Override
    public void onOctMessage(byte[] bytes, int offset, int length) {
        try {
            onReceiveOctetListener.onReceiveOctet(bytes,offset,length);
        } catch (NullPointerException e) {

        }
    }

    private OnDisConnectedListener onDisConnectedListener;
    public void setOnDisConnectedListener(OnDisConnectedListener onDisConnectedListener) {
        try{
            this.onDisConnectedListener = onDisConnectedListener;
        }catch (NullPointerException e){

        }
    }

    @Override
    public void onDisConnected() {
        this.onDisConnectedListener.onDisConnected();
    }

    public void sendText(String text){
        webSocketClient.send(text);
    }

    public void sendOctet(byte[] bytes){
        webSocketClient.send(bytes);
    }

    public void sendOctet(byte[] bytes,int offset,int length){
        byte[] sendBytes = new byte[length];
        System.arraycopy(bytes,offset,sendBytes,0,length);
        webSocketClient.send(sendBytes);
    }

    public interface OnDisConnectedListener{
        void onDisConnected();
    }

    public interface OnReceiveTextListener{
        public void onReceiveText(String text);
    }

    public interface OnReceiveOctetListener{
        public void onReceiveOctet(byte[] bytes, int offset, int length);
    }

}
