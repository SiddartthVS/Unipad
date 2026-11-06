import { NativeModules } from 'react-native';

/*Discovery.js
Finds the PC on the local network (see DiscoveryModule.kt)
    - findPC(timeoutMs) -> { ip, port, name } or null
*/
const { Discovery } = NativeModules;

export const findPC = (timeoutMs = 3000) => Discovery.find(timeoutMs);
