/*App.js - Main Application File
- Websocket connection
- Clipboard operations
- User Interface
*/

import { StyleSheet, View, Text, Animated, Image } from 'react-native';
import { useEffect, useState, useRef } from 'react';

import * as Clipboard from 'expo-clipboard';    //for clipboard access
import * as FileSystem from "expo-file-system"; //for logging

import { Video } from 'expo-av';  //for background videos

//import * as Notifications from 'expo-notifications';//for android notifications
//-> Android notifications are completely commented out for now


// My assets
import MyButton from './src/MyButton.js';
import Log from './src/Log.js';
import Inputs from './src/Inputs.js';
import { log } from './src/LogManager.js';
import Toggle from './src/Toggle.js';

/*Notifications.setNotificationHandler({
  handleNotification: async () => ({
    shouldShowAlert: true,
    shouldPlaySound: true,
    shouldSetBadge: false,
  }),
});*/


export default function App() {

  const [lastReceivedText, setLastReceivedText] = useState(""); //displays below COPY button + buffer
  const [pasteText, setPasteText] = useState("");               //displays below PASTE button
  const [ip, setIP] = useState("192.168.0.124");                //default IP, can be changed in the app
  const [connected, setConnected] = useState(false);            //connection status
  const [isAutoCopy, setIsAutoCopy] = useState(false);          //auto copy toggle state
  const [notifications, setNotifications] = useState([]);       //for in-app notifications
  const [trying, setTrying] = useState(false); //when clicked retry

  const wsRef = useRef(null);   //reference to WebSocket instance to manage connection across re-renders
  const isAutoCopyRef = useRef(isAutoCopy); // ref so onmessage closure always reads latest value
  useEffect(() => { isAutoCopyRef.current = isAutoCopy; }, [isAutoCopy]);
  
  const connectedOpacity = useRef(new Animated.Value(0.5)).current;     //for video crossfade animation
  const disconnectedOpacity = useRef(new Animated.Value(0.5)).current;  //for video crossfade animation
  const isLoading = useRef(new Animated.Value(1)).current;             //for fake loading screen animation

  const notify = (message) => {
  /*This function:
    - Generates a unique ID for each notification
    - Positions it randomly without overlapping the text:
      |-----------------------------------------------------|
      |             1)top 10-40%, left 10-80%               | 
      |                                                     |
      | 3)top 20-80%,       Connected O      4)top 20-80%   |
      |   left 5-20%                           left 75-90%  |   
      |                                                     |
      |             2)bottom 60-90%, left 10-80%            |
      |-----------------------------------------------------|

    - Animates the notification in and out smoothly
      -fade in 250ms
      -stay for 500ms
      -fade out 250ms
    - Deletes itself from memory
  */

    const id = Date.now() + Math.random();
    let top, left;    
    const zone = Math.floor(Math.random() * 4);
    const opacity = new Animated.Value(0);
    
    switch(zone) {
      case 0: // Top area
        top = Math.random() * 30 + 10; // 10% to 40%
        left = Math.random() * 70 + 10; //  10% to 80%
        break;
      case 1: // Bottom area
        top = Math.random() * 25 + 60; // 60% to 90%
        left = Math.random() * 70 + 10; // 10% to 80%
        break;
      case 2: // Left area
        top = Math.random() * 60 + 20; // 20% to 80%
        left = Math.random() * 15 + 5; // 5% to 20%
        break;
      case 3: // Right area
        top = Math.random() * 60 + 20; // 20% to 80%
        left = Math.random() * 15 + 75; // 75% to 90%
        break;
    }
    
    
    setNotifications(prev => [...prev, { id, message, opacity, top, left }]);
    
    Animated.sequence([
      Animated.timing(opacity, { toValue: 1, duration: 250, useNativeDriver: true }),
      Animated.delay(500),
      Animated.timing(opacity, { toValue: 0, duration: 250, useNativeDriver: true }),
    ]).start(() => {
      setNotifications(p => p.filter(n => n.id !== id));
    });
  };

  const connect = async () => {
/*This function:
  - Creates a new Websocket conection:
    - by closing already existing connection
    - to the IP specified in the input field
  - Listens for incoming messages:
    - "~~Image":
      - Converts base64 -> image
      - Sets it to lastReceivedText
      - Notify and Log
      - Autocopy if enabled
    - "Text":
      - Sets it to lastReceivedText
      - Displays it below COPY button
      - Notify and Log
      - Autocopy if enabled
*/
    if (wsRef.current) {
      wsRef.current.close();
    }

    const socket = new WebSocket("ws://" + ip + ":8080");
    wsRef.current = socket;

    setTrying(true);
    setTimeout(() => {
      setTrying(false);
    }, 1000);

    socket.onopen = () => {
      setConnected(true);
      console.log("Connected to PC");
      log("Connected to PC: " + ip);
      //aNotify("Connected", "Successfully connected to PC at " + ip);
      
    };

    socket.onmessage = async (e) => {

      //~~Image
      if (typeof e.data === "string" && e.data.startsWith("~~")) {
        //aNotify("Image received", "Received an image from PC");

        try {
          const base64 = e.data.slice(2);
          const uri = FileSystem.cacheDirectory + "clipboard.png";

          console.log("Writing image file...");
          await FileSystem.writeAsStringAsync(
            uri,
            base64,
            { encoding: "base64" }
          );

          console.log("Setting clipboard image...");
          await Clipboard.setImageAsync(uri);

          console.log("Image pasted to clipboard");


        } catch (err) {
          console.error("IMAGE PIPELINE FAILED:", err);
          notify("Image failed");
        }

        return;
      }

      // Text
      setLastReceivedText(e.data);
      
      // Autocopy
      if (isAutoCopyRef.current) {
        copy();
        notify("Auto Copied");
      }
      else {
        notify("Text received");
      }

    };

    socket.onerror = (e) => {
      setConnected(false);
      console.log("WebSocket error:", e.message);
      log("Connection error: " + e.message);
      notify("Connection error");
      //aNotify("Connection error", "Failed to connect to PC at " + ip);
    };

    socket.onclose = () => {
      setConnected(false);
      console.log("WebSocket closed");
      log("Connection closed");
      notify("Disconnected");
      //aNotify("Disconnected", "Connection to PC at " + ip + " was closed");
    };
  }

  const copy = async () => {
/*This function:
  - Copies lastReceivedText to clipboard
  - Logs and Notifies the user
*/
    if (lastReceivedText.length === 0) {
      console.log("No text received yet");
      return;
    }
    await Clipboard.setStringAsync(lastReceivedText);
    await log("Copied: " + lastReceivedText);
    console.log("Copied to Android clipboard:", lastReceivedText);
    notify("Copied");
    //aNotify("Copied", lastReceivedText);
  };

  const paste = async () => {
/*This function:
  - Sets pasteText from clipboard 
  - Sends it to PC if connected
  - Logs and Notifies the user
*/
    const text = await Clipboard.getStringAsync();
    setPasteText(text);
    await log("Pasted: " + text);

    if (wsRef.current && wsRef.current.readyState === WebSocket.OPEN) {
      wsRef.current.send(text);
      console.log("Sent to PC:", text);
      notify("Sent");
      //aNotify("Sent", text);
    }
  };

  const fakeLoad = () => {
  /*Animated.sequence([
    Animated.timing(isLoading, { toValue: false, duration: 300, useNativeDriver: true }),
  ]).start();*/
  }

/*const aNotify = async (title, body) => {
//This function:
//- Creates a notification channel
//- Displays a native Android notification with the given title and body
//
    await Notifications.scheduleNotificationAsync({
      content: {
        title,
        body,
        sound: 'true',
      },
      trigger: null,
    });
  };

  const requestNotificationPermission = async () => {
    const { status } = await Notifications.getPermissionsAsync();
    if (status !== 'granted') {
      await Notifications.requestPermissionsAsync();
    }
  };*/

  // Connect on app start and clean up on unmount //+ Android notifications setup + Fake loading screen
  useEffect(() => {
    connect();
    fakeLoad();
    //requestNotificationPermission();
    return () => wsRef.current?.close();
  }, []);

  // Animate video opacity on connection status change
  useEffect(() => {
    if (connected) {
      Animated.parallel([Animated.timing(connectedOpacity, {toValue: 1,duration: 500,useNativeDriver: true,}),
                        Animated.timing(disconnectedOpacity, {toValue: 0,duration: 500,useNativeDriver: true,}),
                        ]).start();
    } 
    else {
      Animated.parallel([Animated.timing(connectedOpacity, {toValue: 0,duration: 500,useNativeDriver: true,}),
                        Animated.timing(disconnectedOpacity, {toValue: 1,duration: 500,useNativeDriver: true,}),
                        ]).start();
    }
  }, [connected]);

  // Animate video opacity on connection status change
  useEffect(() => {
    if (isLoading) {
      Animated.parallel([Animated.timing(isLoading, {toValue: false,duration: 500,useNativeDriver: true,}),
                        ]).start();
    } 
  }, [isLoading]);

  return (
    <View style={{ flex: 1 }}>
      {/*Fake Loading Screen*/}
      <Animated.View style={{
        position: 'absolute',
        width: '100%',
        height: '100%',
        backgroundColor: '#1e1e28ff',
        alignItems: 'center',
        justifyContent: 'center',
      }}>
        <Image source={require('./assets/icon.png')} style={{ width: '14%', height: '7%' }} />
      </Animated.View>

      {/* Connected/Disconnected(Video/Text) + Notification(in app) */}
      <View style={{ height: '25%', width: '100%', justifyContent: 'center', alignItems: 'center' }}>
        {/* Disconnected Video */}
        <Animated.View style={{ 
          position: 'absolute', 
          width: '100%', 
          height: '120%',
          opacity: disconnectedOpacity 
        }}>
          <Video
            source={require('./assets/disconnected.mp4')}
            style={{ width: '100%', height: '100%' }}
            resizeMode="cover"
            shouldPlay
            isLooping
            isMuted={true}
          />
        </Animated.View>

        {/* Connected Video */}
        <Animated.View style={{ 
          position: 'absolute', 
          width: '100%', 
          height: '120%',
          opacity: connectedOpacity 
        }}>
          <Video
            source={require('./assets/connected.mp4')}
            style={{ width: '100%', height: '100%' }}
            resizeMode="cover"
            shouldPlay
            isLooping
            isMuted={true}
          />
        </Animated.View>

        {notifications.map(notification => (
          <Animated.View
            key={notification.id}
            style={[
              styles.notification,
              {
                opacity: notification.opacity,
                top: notification.top,
                left: notification.left,
              }
            ]}
          >
            <Text style={styles.notificationText}>{notification.message}</Text>
          </Animated.View>
        ))}

        <Text style={{ 
          position: 'absolute', 
          top: '50%', 
          left: '25%', 
          fontSize: 25, 
          color: connected ? '#3cf242' : '#fff200', 
          fontWeight: 'bold' 
        }}>
          {connected ? "     Connected" : trying ? "Reconnecting.." : "Disconnected"}
        </Text>
        <View style={connected ? styles.dot1 : styles.dot2}></View>
      </View>
      
      {/* Main App Interface */}
      <View style={[styles.container, connected ? styles.connected : styles.disconnected]}>
        
        {/*Auto copy toggle*/}
        <Toggle isAutoCopy={isAutoCopy} setIsAutoCopy={setIsAutoCopy}/>

        {/*Input+Edit+Reconnect*/}
        <Inputs 
          ip={ip} 
          setIP={setIP} 
          func={connect}
          style={{ height: '10%' }}
        />

        {/*Log*/}
        <Log 
          style={{ height: '50%' }}
        />

        {/*Copy/Paste buttons*/}
        <MyButton 
          text=" COPY " 
          func={copy} 
          infoText={lastReceivedText}
        />
        <MyButton 
          text=" PASTE " 
          func={paste} 
          infoText={pasteText}
        />
      </View>
      <Text style={{color:'#bababa',justifyContent:'center',alignSelf:'center',fontSize:12}}>​© 2026 Siddartth V S | All rights reserved.</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'flex-start',
    backgroundColor: '#1e1e28ff',
    boxShadow: '0 0 30px 10px rgb(0, 0, 0)',
    borderRadius: 20,
    width: '100%',
  },
  notification: {
    position: 'absolute',
    paddingHorizontal: 20,
    paddingVertical: 10,
    borderRadius: 25,
  },
  notificationText: {
    color: '#ffffff',
    fontSize: 12,
    fontWeight: 'bold',
  },
  dot1: {
    position: 'absolute',
    top: '54%',
    left: '65%',
    height: 20,
    width: 20,
    borderRadius: 10,
    backgroundColor: '#3cf242',
    boxShadow: '0 0 20px #3cf242',
  },
  dot2: {
    position: 'absolute',
    top: '54%',
    left: '65%',
    height: 20,
    width: 20,
    borderRadius: 10,
    backgroundColor: '#fff200',
    boxShadow: '0 0 20px #fff200',
  },
});
