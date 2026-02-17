import { StyleSheet, Text, TouchableOpacity } from 'react-native';

const MyButton = ({text, func, infoText}) => {
  if (infoText==="") {
    infoText = null;
  }
  else if (infoText.length > 20) {
    infoText = "\""+infoText.substring(0, 15) + "..." + "\"";
  }
  
  return (
    <TouchableOpacity onPress={func} style={styles.button}>
      <Text style={styles.text}>{text}</Text>
      {infoText && <Text style={styles.infoText} >{infoText}</Text>}
    </TouchableOpacity>
  );
};

const styles = StyleSheet.create({
  button: {
    width: '85%',
    padding: 8,
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: 50,
    marginBottom: 30,
    height: 70,
    backgroundColor: '#ecdbc9',
    boxShadow: "0px 0px 5px 1px #ecdbc9",
  },
  text: {
    fontSize: 21,
    fontWeight: 'bold',
  },
  infoText: {
    fontSize: 14,
    marginTop: 5,
    color: '#7D7D7D',
  },
});

export default MyButton;
