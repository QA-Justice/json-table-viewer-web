import { FileUtils, NotificationManager, DataUtils } from './utils.js';

export class ExportManager {
  constructor(dataProcessor) {
    this.dataProcessor = dataProcessor;
  }

  // CSV 다운로드
  downloadCSV() {
    const dataToExport = this.dataProcessor.getDataToExport();
    
    if (!DataUtils.isValidData(dataToExport)) {
      NotificationManager.showError('No data to download. Please convert JSON first.');
      return;
    }

    try {
      const csvContent = this.dataProcessor.convertToCSV(dataToExport);
      const fileName = `json-table-${new Date().toISOString().slice(0, 10)}.csv`;
      
      FileUtils.downloadCSV(csvContent, fileName);
      NotificationManager.showSuccess('📥 CSV file downloaded successfully!');
    } catch (error) {
      NotificationManager.showError('An error occurred while downloading CSV');
    }
  }

  // 클립보드에 복사
  async copyToClipboard(format = 'markdown') {
    const dataToCopy = this.dataProcessor.getDataToExport();
    
    if (!DataUtils.isValidData(dataToCopy)) {
      NotificationManager.showError('No data to copy. Please convert JSON first.');
      return;
    }

    let text;
    if (format === 'markdown') {
      text = this.dataProcessor.convertToMarkdown(dataToCopy);
    } else {
      text = this.dataProcessor.convertToText(dataToCopy);
    }

    try {
      const success = await FileUtils.copyToClipboard(text);
      if (success) {
        NotificationManager.showSuccess(`📋 Data copied to clipboard successfully! (${format})`);
      } else {
        throw new Error('Copy failed');
      }
    } catch (error) {
      NotificationManager.showError('Failed to copy to clipboard');
    }
  }
}
