import { View, Image, TouchableOpacity, TextInput, StyleSheet } from 'react-native';
import { useRef, useState, useEffect } from 'react';

const Inputs = ({ ip, setIP, func }) => {
  const textInputRef = useRef(null);
  const [isEditable, setIsEditable] = useState(false);

  const handleEditPress = () => {
    setIsEditable(true);
  };

  const handleBlur = () => {
    setIsEditable(false);
  };

  useEffect(() => {
    if (isEditable && textInputRef.current) {
      setTimeout(() => {
        textInputRef.current?.focus();
      }, 100);
    }
  }, [isEditable]);


  return (
    <View style={styles.container}>
      <TextInput
        ref={textInputRef}
        style={styles.input}
        fontFamily='Monospace'
        placeholder="Enter PC IP Address"
        placeholderTextColor="#666"
        keyboardType="numeric"
        value={ip}
        onChangeText={(text) => setIP(text)}
        editable={isEditable}
        onBlur={handleBlur}
      />
      <TouchableOpacity 
        style={[styles.editButton, styles.editButtonDark]} 
        onPress={handleEditPress}
      >
        <Image
          source={require('../assets/icon edit.png')}
          style={{ height: 28, width: 28 }}
        />
      </TouchableOpacity>
      <TouchableOpacity 
        style={styles.refreshButton} 
        onPress={func}
      >
        <Image
          source={require('../assets/icon connect.png')}
          style={{ height: 21, width: 20 }}
        />
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  input: {
    height: 60,
    width: '68%',
    fontSize: 22,
    borderRadius: 5,
    padding: 10,
    margin: 20,
    marginTop: 40,
    backgroundColor: '#0F0F17',
    color: '#fdfdfd',
    boxShadow: '0px 0px 10px 2px rgb(0, 0, 0)',
  },

  refreshButton: {
    height: 60,
    width: 60,
    marginTop: 40,
    borderRadius: 20,
    justifyContent: 'center',
    alignItems: 'center',
    boxShadow: '0px 0px 5px 1px #ecdbc9',
    backgroundColor: '#ecdbc9',
  },
  editButton: {
    height: 60,
    width: 60,
    position: 'absolute',
    right: 100,
    marginTop: 40,
    borderRadius: 5,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#2d2d39',
  },
  
  container: {
    flexDirection: 'row',
    height: '18%',
    width: '100%',
  }
});

export default Inputs;
