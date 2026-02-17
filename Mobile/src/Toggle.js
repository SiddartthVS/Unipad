import { View, Switch, StyleSheet, Text } from 'react-native';

const Toggle = ({ isAutoCopy, setIsAutoCopy}) => {
  const toggleAutoCopy = () => setIsAutoCopy(!isAutoCopy);

  return (
    <View style={styles.container}>
            <Text style={[styles.label, { color: '#fdfdfdfd' }]}>
        Auto Copy
      </Text>
      <Switch
        trackColor={{ false: "#d0d0d0", true: "#ecdbc9" }}
        thumbColor={isAutoCopy ? "#ffffff" : "#f4f3f4"}
        ios_backgroundColor="#d0d0d0"
        onValueChange={toggleAutoCopy}
        value={isAutoCopy}
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: '-10%',
    marginLeft: '140%',
    width: '100%',
  },
  label: {
    fontSize: 14,
    fontWeight: '500',
  },
});

export default Toggle;
