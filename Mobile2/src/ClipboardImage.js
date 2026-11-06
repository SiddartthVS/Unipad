import { NativeModules } from 'react-native';

/*ClipboardImage.js
JS side of the custom Android module in
android/app/src/main/java/com/anonymous/Unipad/ClipboardImageModule.kt

  ClipboardImage.setImage(filePath) -> Promise
    Puts the image file (PNG/JPEG) on the Android clipboard.
    Replaces expo-clipboard's setImageAsync().
*/
const { ClipboardImage } = NativeModules;

export default ClipboardImage;
