package org.mobicents.restcomm.android.client.sdk;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import org.mobicents.restcomm.android.sipua.impl.SipManager;

import java.util.HashMap;

// Client object that will send all asynchronous requests from UI towards signaling thread
public class UIClient {

   // Interface the UIClient listener needs to implement, to get events from us
   public interface UIClientListener {
      // Replies
      void onOpenReply(String id, RCDeviceListener.RCConnectivityStatus connectivityStatus, RCClient.ErrorCodes status, String text);

      void onCloseReply(String id, RCClient.ErrorCodes status, String text);

      void onReconfigureReply(String id, RCClient.ErrorCodes status, String text);

      void onCallReply(String id, RCClient.ErrorCodes status, String text);

      void onMessageReply(String id, RCClient.ErrorCodes status, String text);

      // Unsolicited Events
      //void onCallArrivedEvent(String id, String peer);

      void onMessageArrivedEvent(String id, String peer, String text);

      void onErrorEvent(String id, RCClient.ErrorCodes status, String text);

      void onConnectivityEvent(String id, RCDeviceListener.RCConnectivityStatus connectivityStatus);

      // Call related events that are delegated to RCConnection
      void onCallRelatedMessage(SignalingMessage message);

   }

   /*
   public interface UICallListener {
       void onCallPeerHangupEvent(String jobId);
       void onCallPeerRingingEvent(String jobId);
       void onCallConnectedEvent(String jobId, String sdpAnswer);
   }
   */
   // handler at signaling thread to send messages to
   SignalingHandlerThread signalingHandlerThread;
   Handler signalingHandler;
   UIHandler uiHandler;
   Context context;

   public UIClient(UIClientListener listener, Context context)
   {
      uiHandler = new UIHandler(listener);
      this.context = context;

      signalingHandlerThread = new SignalingHandlerThread(uiHandler);
      signalingHandler = signalingHandlerThread.getHandler();

      ///// Defines a Handler object that's attached to the UI thread
        /*
        handler = new Handler(context.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                // Gets the image task from the incoming Message object.
                SipProfile profile = (SipProfile) inputMessage.obj;
            }
        };
        */
      /////
   }

    /*
    void setCallListener(UICallListener callListener)
    {

    }
    */

   String open(HashMap<String, Object> parameters)
   {
      String id = generateId();
      SignalingMessage signalingMessage = new SignalingMessage(id, SignalingMessage.MessageType.OPEN_REQUEST);
      signalingMessage.setParameters(parameters);
      signalingMessage.setAndroidContext(context);

      Message message = signalingHandler.obtainMessage(1, signalingMessage);
      message.sendToTarget();

      return id;
   }

   // Change signaling configuration, like update username/password, change domain, etc
   String reconfigure(HashMap<String, Object> parameters)
   {
      String id = generateId();
      SignalingMessage signalingMessage = new SignalingMessage(id, SignalingMessage.MessageType.RECONFIGURE_REQUEST);
      signalingMessage.setParameters(parameters);

      Message message = signalingHandler.obtainMessage(1, signalingMessage);
      message.sendToTarget();

      return id;
   }

   // -- Call related methods. For these the id is already generated by the application
   void call(String id, HashMap<String, Object> parameters)
   {
      //String id = generateId();
      SignalingMessage signalingMessage = new SignalingMessage(id, SignalingMessage.MessageType.CALL_REQUEST);
      signalingMessage.setParameters(parameters);
      Message message = signalingHandler.obtainMessage(1, signalingMessage);
      message.sendToTarget();

      //return id;
   }

   void accept(String id, HashMap<String, Object> parameters)
   {
      SignalingMessage signalingMessage = new SignalingMessage(id, SignalingMessage.MessageType.CALL_ACCEPT_REQUEST);
      signalingMessage.setParameters(parameters);
      Message message = signalingHandler.obtainMessage(1, signalingMessage);
      message.sendToTarget();
   }


   void disconnect(String id)
   {
      SignalingMessage signalingMessage = new SignalingMessage(id, SignalingMessage.MessageType.CALL_DISCONNECT_REQUEST);
      Message message = signalingHandler.obtainMessage(1, signalingMessage);
      message.sendToTarget();
   }

   void sendDigits(String id, String digits)
   {
      SignalingMessage signalingMessage = new SignalingMessage(id, SignalingMessage.MessageType.SEND_DIGITS_REQUEST);
      signalingMessage.dtmfDigits = digits;
      Message message = signalingHandler.obtainMessage(1, signalingMessage);
      message.sendToTarget();
   }

   String sendMessage(HashMap<String, Object> parameters)
   {
      String id = generateId();

      SignalingMessage signalingMessage = new SignalingMessage(id, SignalingMessage.MessageType.MESSAGE_REQUEST);
      signalingMessage.parameters = parameters;
      Message message = signalingHandler.obtainMessage(1, signalingMessage);
      message.sendToTarget();

      return id;
   }

   String close()
   {
      String id = generateId();
      SignalingMessage signalingMessage = new SignalingMessage(id, SignalingMessage.MessageType.CLOSE_REQUEST);
      //signalingMessage.setParameters(parameters);

      Message message = signalingHandler.obtainMessage(1, signalingMessage);
      message.sendToTarget();

      return id;
   }

   // Helpers

   // Generate unique identifier for 'transactions' created by UIClient, this can then be used as call-id when it enters JAIN SIP
   private String generateId()
   {
      return Long.toString(System.currentTimeMillis());
   }
}
