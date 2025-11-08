import { DataProcessor } from './data-processor.js';
import { TableRenderer } from './table-renderer.js';
import { ExportManager } from './export-manager.js';
import { UIManager } from './ui-manager.js';

// ===== 메인 애플리케이션 클래스 =====
class JSONTableConverter {
  constructor() {
    // 의존성 주입
    this.dataProcessor = new DataProcessor();
    this.tableRenderer = new TableRenderer(this.dataProcessor);
    this.exportManager = new ExportManager(this.dataProcessor);
    this.uiManager = new UIManager(this.dataProcessor, this.tableRenderer, this.exportManager);
  }
}

// DOM 로드 완료 시 초기화
document.addEventListener('DOMContentLoaded', () => {
  new JSONTableConverter();
});
  