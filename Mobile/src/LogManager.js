import * as FileSystem from 'expo-file-system/legacy';

const LOG_FILE_PATH = FileSystem.documentDirectory + 'clipboard_logs.txt';
const MAX_LOG_ENTRIES = 7;

/**
 * Logs a message to the clipboard log file
 * Format: DD-MMM "Message" HH:MM
 * If the new message is the same as the most recent, it updates the timestamp instead of adding a new line.
 * @param {string} message - The message to log
 */
export const log = async (message) => {
  try {
    if (message.length > 30) {
      message = message.substring(0, 25) + '...';
    }
    const now = new Date();
    
    // Format date as DD-MMM (e.g., 12-JAN)
    const day = String(now.getDate()).padStart(2, '0');
    const months = ['JAN', 'FEB', 'MAR', 'APR', 'MAY', 'JUN', 'JUL', 'AUG', 'SEP', 'OCT', 'NOV', 'DEC'];
    const month = months[now.getMonth()];
    
    // Format time as HH:MM (24-hour format)
    const hours = String(now.getHours()).padStart(2, '0');
    const minutes = String(now.getMinutes()).padStart(2, '0');
    
    const dateStr = `${day}-${month}`;
    const timeStr = `${hours}:${minutes}`;
    const messageStr = `"${message}"`;
    
    // Create log entry template
    const newLogEntry = `${dateStr} ${messageStr} ${timeStr}`;
    
    // Read existing logs
    let existingLogs = [];
    const fileInfo = await FileSystem.getInfoAsync(LOG_FILE_PATH);
    
    if (fileInfo.exists) {
      const content = await FileSystem.readAsStringAsync(LOG_FILE_PATH);
      if (content.trim()) {
        existingLogs = content.trim().split('\n');
      }
    }
    
    // Logic: Check if the most recent entry matches the current message
    let isDuplicate = false;
    if (existingLogs.length > 0) {
      const lastEntry = existingLogs[0];
      // Extract the message from between the quotes of the last entry
      const lastMessageMatch = lastEntry.match(/"([^"]*)"/);
      const lastMessage = lastMessageMatch ? lastMessageMatch[1] : null;

      if (lastMessage === message) {
        // If identical, replace the entire first entry with the updated date/time/message
        existingLogs[0] = newLogEntry;
        isDuplicate = true;
      }
    }

    // Add new log entry at the beginning only if it wasn't a duplicate update
    if (!isDuplicate) {
      existingLogs.unshift(newLogEntry);
    }
    
    // Keep only the last MAX_LOG_ENTRIES (rotate)
    if (existingLogs.length > MAX_LOG_ENTRIES) {
      existingLogs = existingLogs.slice(0, MAX_LOG_ENTRIES);
    }
    
    // Write back to file
    const newContent = existingLogs.join('\n') + '\n';
    await FileSystem.writeAsStringAsync(LOG_FILE_PATH, newContent);
    
    return true;
  } catch (error) {
    return false;
  }
};
/**
 * Reads all log entries from the log file
 * @returns {Promise<Array>} Array of log objects with date, message, and time
 */
export const readLog = async () => {
  try {
    const fileInfo = await FileSystem.getInfoAsync(LOG_FILE_PATH);
    
    if (!fileInfo.exists) {
      console.log('Log file does not exist yet');
      return [];
    }
    
    const content = await FileSystem.readAsStringAsync(LOG_FILE_PATH);
    
    if (!content.trim()) {
      return [];
    }
    
    const lines = content.trim().split('\n');
    const logs = [];
    
    // Parse each line: DD-MMM "Message" HH:MM
    for (const line of lines) {
      if (!line.trim()) continue;
      
      // Extract date (DD-MMM)
      const dateMatch = line.match(/^(\d{2}-[A-Z]{3})/);
      if (!dateMatch) continue;
      
      const date = dateMatch[1];
      
      // Extract message (between quotes)
      const messageMatch = line.match(/"([^"]*)"/);
      const message = messageMatch ? messageMatch[1] : '';
      
      // Extract time (HH:MM at the end)
      const timeMatch = line.match(/(\d{2}:\d{2})$/);
      const time = timeMatch ? timeMatch[1] : '';
      
      logs.push({
        date,
        message,
        time,
        fullLine: line
      });
    }
    
    return logs;
  } catch (error) {
    console.error('Error reading log:', error);
    return [];
  }
};

/**
 * Clears all log entries
 */
export const clearLogs = async () => {
  try {
    const fileInfo = await FileSystem.getInfoAsync(LOG_FILE_PATH);
    if (fileInfo.exists) {
      await FileSystem.deleteAsync(LOG_FILE_PATH);
      console.log('Logs cleared');
    }
    return true;
  } catch (error) {
    console.error('Error clearing logs:', error);
    return false;
  }
};
