import { DataUtils } from './utils.js';

export class DataProcessor {
  constructor() {
    this.currentData = null;
    this.pivotedData = null;
    this.isPivoted = false;
  }

  // JSON 파싱 및 평면화
  parseAndFlatten(jsonText) {
    const data = JSON.parse(jsonText);
    
    if (!Array.isArray(data)) {
      return [this.flattenObject(data)];
    }
    
    return data.map(item => this.flattenObject(item));
  }

  // JSON 평면화 메인 함수
  flattenObject(obj, prefix = '', result = {}) {
    if (obj === null || obj === undefined) {
      result[prefix] = obj;
      return result;
    }

    if (Array.isArray(obj)) {
      this.flattenArray(obj, prefix, result);
    } else if (typeof obj === 'object') {
      this.flattenObjectProperties(obj, prefix, result);
    } else {
      result[prefix] = obj;
    }
    
    return result;
  }

  // 배열 평면화
  flattenArray(arr, prefix, result) {
    if (arr.length === 0) {
      result[prefix] = '[]';
    } else {
      arr.forEach((item, index) => {
        this.flattenObject(item, `${prefix}[${index}]`, result);
      });
    }
  }

  // 객체 속성 평면화
  flattenObjectProperties(obj, prefix, result) {
    const keys = Reflect.ownKeys(obj);
    if (keys.length === 0) {
      result[prefix] = '{}';
    } else {
      keys.forEach(key => {
        const newPrefix = prefix ? `${prefix}.${key}` : key;
        this.flattenObject(obj[key], newPrefix, result);
      });
    }
  }

  // 모든 키 수집
  getAllKeys(dataArray) {
    const keySet = new Set();
    
    dataArray.forEach(obj => {
      Reflect.ownKeys(obj).forEach(key => keySet.add(key));
    });
    
    return Array.from(keySet);
  }

  // Pivot 키 생성 로직
  generateKeys(dataArray) {
    if (this.isPivoted) {
      const originalRowCount = this.currentData ? this.currentData.length : 0;
      return ['Field', ...Array.from({length: originalRowCount}, (_, i) => `Row ${i + 1}`)];
    }
    return this.getAllKeys(dataArray);
  }

  // Pivot 테이블 생성
  createPivotTable(dataArray) {
    if (!DataUtils.isValidData(dataArray)) return [];

    const allKeys = this.getAllKeys(dataArray);
    const pivotData = [];
    
    allKeys.forEach(key => {
      const row = { Field: key };
      
      for (let i = 0; i < dataArray.length; i++) {
        const value = dataArray[i][key] ?? '';
        row[`Row ${i + 1}`] = value;
      }
      
      pivotData.push(row);
    });

    return pivotData;
  }

  // 데이터 변환 (CSV, Markdown, Text)
  transformData(dataArray, formatter, separator) {
    if (!DataUtils.isValidData(dataArray)) return '';
    
    const allKeys = this.generateKeys(dataArray);
    const headers = allKeys.join(separator);
    const rows = dataArray.map(row => {
      return allKeys.map(key => {
        const value = row[key] ?? '';
        return formatter(DataUtils.formatCellValue(value));
      }).join(separator);
    });
    
    return [headers, ...rows].join('\n');
  }

  // CSV 변환
  convertToCSV(dataArray) {
    return this.transformData(dataArray, (value) => DataUtils.escapeCSVField(value), ',');
  }

  // Markdown 테이블 변환
  convertToMarkdown(dataArray) {
    if (!DataUtils.isValidData(dataArray)) return '';
    
    const allKeys = this.generateKeys(dataArray);
    
    const headers = allKeys.map(key => `| ${key} `).join('') + '|';
    const separator = allKeys.map(() => '| --- ').join('') + '|';
    const rows = dataArray.map(row => {
      return allKeys.map(key => {
        const value = row[key] ?? '';
        const formattedValue = DataUtils.formatCellValue(value);
        const escapedValue = formattedValue.replace(/\|/g, '\\|');
        return `| ${escapedValue} `;
      }).join('') + '|';
    });
    
    return [headers, separator, ...rows].join('\n');
  }

  // Text 테이블 변환 (탭으로 구분)
  convertToText(dataArray) {
    return this.transformData(dataArray, (value) => value, '\t');
  }

  // 데이터 상태 관리
  setCurrentData(data) {
    this.currentData = data;
    this.pivotedData = null;
    this.isPivoted = false;
  }

  setPivotedData(data) {
    this.pivotedData = data;
    this.isPivoted = true;
  }

  resetPivot() {
    this.isPivoted = false;
    this.pivotedData = null;
  }

  getDataToExport() {
    return this.isPivoted ? this.pivotedData : this.currentData;
  }

  getSampleJSON() {
    return `[
  {
    "name": "John Doe",
    "age": 30,
    "email": "john@example.com",
    "active": true,
    "scores": [85, 92, 78],
    "address": {
      "street": "123 Main St",
      "city": "New York",
      "zip": "10001"
    }
  },
  {
    "name": "Jane Smith",
    "age": 25,
    "email": "jane@example.com",
    "active": false,
    "scores": [90, 88, 95],
    "address": {
      "street": "456 Oak Ave",
      "city": "Los Angeles",
      "zip": "90210"
    }
  },
  {
    "name": "Bob Johnson",
    "age": 35,
    "email": "bob@example.com",
    "active": true,
    "scores": [75, 82, 88],
    "address": {
      "street": "789 Pine Rd",
      "city": "Chicago",
      "zip": "60601"
    }
  }
]`;
  }
}
