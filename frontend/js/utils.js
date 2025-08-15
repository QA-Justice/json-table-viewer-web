// ===== 공통 유틸리티 =====
export class DOMUtils {
  static getElement(selector) {
    const element = document.querySelector(selector);
    if (!element) {
      console.warn(`Element not found: ${selector}`);
    }
    return element;
  }

  static safeDOMOperation(selector, operation) {
    const element = this.getElement(selector);
    if (element) {
      operation(element);
    }
  }

  static createElement(tag, className, id = null) {
    const element = document.createElement(tag);
    if (className) element.className = className;
    if (id) element.id = id;
    return element;
  }

  static removeElement(selector) {
    const element = document.querySelector(selector);
    if (element) element.remove();
  }
}

export class NotificationManager {
  static show(message, type = 'info') {
    this.removeExisting();
    
    const notification = DOMUtils.createElement('div', `notification notification-${type}`);
    notification.textContent = message;
    document.body.appendChild(notification);
    
    setTimeout(() => {
      if (notification.parentNode) {
        notification.remove();
      }
    }, 3000);
  }

  static showSuccess(message) {
    this.show(message, 'success');
  }

  static showError(message) {
    this.show(message, 'error');
  }

  static removeExisting() {
    const existing = document.querySelector('.notification');
    if (existing) existing.remove();
  }
}

export class DataUtils {
  static isValidData(data) {
    return data && Array.isArray(data) && data.length > 0;
  }

  static getSafeData(dataArray) {
    return this.isValidData(dataArray) ? dataArray : [];
  }

  static formatCellValue(value) {
    if (value === null || value === undefined) return 'null';
    if (typeof value === 'boolean') return value ? 'true' : 'false';
    if (typeof value === 'object') return JSON.stringify(value);
    return String(value);
  }

  static getCellClassName(value) {
    if (value === null || value === undefined) return 'cell-null';
    if (typeof value === 'boolean') return 'cell-boolean';
    if (typeof value === 'number') return 'cell-number';
    if (typeof value === 'string') return 'cell-string';
    return 'cell-object';
  }

  static escapeCSVField(field) {
    if (field === null || field === undefined) return '';
    
    const stringField = String(field);
    const needsQuotes = stringField.includes(',') || 
                       stringField.includes('"') || 
                       stringField.includes('\n') || 
                       stringField.includes('\r');
    
    if (needsQuotes) {
      const escapedField = stringField.replace(/"/g, '""');
      return `"${escapedField}"`;
    }
    
    return stringField;
  }

  static escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
  }
}

export class FileUtils {
  static downloadCSV(content, filename) {
    const blob = new Blob(['\ufeff' + content], { type: 'text/csv;charset=utf-8;' });
    const link = DOMUtils.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }

  static async copyToClipboard(text) {
    if (navigator.clipboard && window.isSecureContext) {
      await navigator.clipboard.writeText(text);
      return true;
    } else {
      return this.fallbackCopyToClipboard(text);
    }
  }

  static fallbackCopyToClipboard(text) {
    const textArea = DOMUtils.createElement('textarea');
    textArea.value = text;
    textArea.style.position = 'fixed';
    textArea.style.left = '-999999px';
    textArea.style.top = '-999999px';
    document.body.appendChild(textArea);
    textArea.focus();
    textArea.select();
    
    try {
      const successful = document.execCommand('copy');
      document.body.removeChild(textArea);
      return successful;
    } catch (err) {
      document.body.removeChild(textArea);
      return false;
    }
  }
}
