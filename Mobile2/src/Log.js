import { View, Text, StyleSheet, TouchableOpacity, Image } from 'react-native';
import { useEffect, useState } from 'react';

import { readLog,clearLogs } from './LogManager.js';// Log functions

const Log = () => {
  const [logs, setLogs] = useState([]);

  //Refresh logs every 2 secs
  useEffect(() => {
    loadLogs();

    const interval = setInterval(() => {
      loadLogs();
    }, 2000);

    return () => clearInterval(interval);
  }, []);

  //Read logs
  const loadLogs = async () => {
    const logEntries = await readLog();
    setLogs(logEntries);
  };

  //Erase logs
  const eraseLogs = async () => {
    await clearLogs();
    const logEntries = await readLog();
    setLogs(logEntries);
  };

  return (
    <View style={styles.around}>
      {/*Main window*/}
      <TouchableOpacity onPress={eraseLogs} style={{ position: 'absolute', right: 15, top: 15 ,backgroundColor: '#2d2d39', padding: 8, borderRadius: 5, zIndex: 1}}>
        {/*Clear Button*/}
        <Image source={require('../assets/icon clear.png')} style={{ width: 10, height: 10, transform: [{ rotate: '225deg' }] }} />
      </TouchableOpacity>
      <Text style={{ fontSize: 30, color: '#ffffff', marginBottom: 10, marginLeft: 10 }}>
        {/*Heading*/}
        Logs
      </Text>
      {/*Log entries*/}
      {formatLogEntries()}
    </View>
  );

  function formatLogEntries() {
    if (logs.length === 0) {
      return (
        <View style={{ backgroundColor: '#191923', padding: 10, borderRadius: 5 }}>
          <Text style={{ color: '#fdfdfdc6', fontStyle: 'italic' }}>
            No logs yet...
          </Text>
        </View>
      );
    }

    return logs.map((logEntry, index) => {
      let backgroundColor = index % 2 === 0 ? '#191923' : '#14141e';
      
      
      return (
        <View 
          style={{ 
            backgroundColor: backgroundColor, 
            padding: 8,
            paddingHorizontal: 12,
            borderRadius: 4,
            marginBottom: 2
          }} 
          key={index}
        >{/*Single log entry styling*/}
          <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start' }}>
            <View style={{ flex: 1, flexDirection: 'row', marginRight: 10 }}>
              <Text style={{ color: '#fdfdfdc6', fontWeight: 'bold', marginRight: 8 }}>
                {logEntry.date}
              </Text>
              <Text style={{ color: '#fdfdfd', flex: 1,fontStyle: 'italic' }} numberOfLines={1} ellipsizeMode="tail">
                "{logEntry.message}"
              </Text>
            </View>
            <Text style={{ color: '#fdfdfdc6', fontWeight: '600', minWidth: 45, textAlign: 'right' }}>
              {logEntry.time}
            </Text>
          </View>
        </View>
      );
    });
  }
};

const styles = StyleSheet.create({
  around: {
    flex: 1,
    margin: 20,
    padding: 10,
    borderRadius: 20,
    marginBottom: 35,
    width: '85%',
    height: 300,
    overflow: 'scroll',
    backgroundColor: '#0F0F17',
    boxShadow: '0px 0px 10px 2px rgb(0, 0, 0)',
  },
});

export default Log;
