import { DOMUtils, NotificationManager, DataUtils } from './utils.js';

export class UIManager {
  constructor(dataProcessor, tableRenderer, exportManager) {
    this.dataProcessor = dataProcessor;
    this.tableRenderer = tableRenderer;
    this.exportManager = exportManager;
    this.initializeEventListeners();
  }

  // 이벤트 리스너 초기화
  initializeEventListeners() {
    this.setSampleJSON();
    this.bindConvertButton();
    this.bindKeyboardShortcut();
    this.bindFileUpload();
    this.bindGlobalEvents();
  }

  // 변환 버튼 이벤트 바인딩
  bindConvertButton() {
    DOMUtils.safeDOMOperation('#convertBtn', (convertBtn) => {
      convertBtn.addEventListener('click', () => this.handleConvert());
    });
  }

  // 키보드 단축키 바인딩
  bindKeyboardShortcut() {
    DOMUtils.safeDOMOperation('#jsonInput', (jsonInput) => {
      jsonInput.addEventListener('keydown', (e) => {
        if (e.ctrlKey && e.key === 'Enter') {
          this.handleConvert();
        }
      });
    });
  }

  // 파일 업로드 이벤트 바인딩
  bindFileUpload() {
    const fileBtn = DOMUtils.getElement('#fileUploadBtn');
    const fileInput = DOMUtils.getElement('#jsonFileInput');
    
    if (fileBtn && fileInput) {
      fileBtn.addEventListener('click', (e) => {
        e.preventDefault();
        fileInput.click();
      });

      fileInput.addEventListener('change', (e) => {
        this.handleFileUpload(e);
      });
    }
  }

  // 전역 이벤트 바인딩
  bindGlobalEvents() {
    document.addEventListener('click', (e) => {
      if (e.target.matches('#downloadExcelBtn')) {
        this.exportManager.downloadCSV();
      }
      if (e.target.matches('#copyToClipboardBtn')) {
        this.toggleCopyOptions();
      }
      if (e.target.matches('.dropdown-item')) {
        const format = e.target.dataset.format;
        this.exportManager.copyToClipboard(format);
        this.hideCopyOptions();
      }
      if (e.target.matches('#pivotBtn')) {
        this.togglePivot();
      }
      if (e.target.matches('#resetBtn')) {
        this.resetToSample();
      }
    });

    document.addEventListener('click', (e) => {
      if (!e.target.closest('.dropdown-container')) {
        this.hideCopyOptions();
      }
    });
  }

  // JSON 변환 처리
  handleConvert() {
    const input = this.getJsonInput();
    
    if (!input) {
      NotificationManager.showError('Please enter JSON data.');
      return;
    }

    try {
      const flattened = this.dataProcessor.parseAndFlatten(input);
      this.dataProcessor.setCurrentData(flattened);
      this.tableRenderer.renderTable(flattened);
      this.updatePivotButton();
      this.initializeSearchAfterTableCreation();
      NotificationManager.showSuccess(`✅ Conversion successful! (${flattened.length} rows)`);
    } catch (error) {
      NotificationManager.showError(`⚠️ JSON parse error: ${error.message}`);
    }
  }

  // 파일 업로드 처리
  handleFileUpload(event) {
    const file = event.target.files[0];
    if (!file) return;

    // JSON 파일 검증
    if (!file.name.toLowerCase().endsWith('.json')) {
      NotificationManager.showError('Please select a JSON file (.json extension)');
      return;
    }

    const reader = new FileReader();
    
    reader.onload = (event) => {
      try {
        const text = event.target.result;
        DOMUtils.safeDOMOperation('#jsonInput', (jsonInput) => {
          jsonInput.value = text;
          this.handleConvert();
        });
      } catch (error) {
        NotificationManager.showError('An error occurred while reading the file.');
      }
    };

    reader.onerror = () => {
      NotificationManager.showError('An error occurred while reading the file.');
    };

    reader.readAsText(file, 'utf-8');
  }

  // Pivot 토글
  togglePivot() {
    if (!DataUtils.isValidData(this.dataProcessor.currentData)) {
      NotificationManager.showError('No data to pivot. Please convert JSON first.');
      return;
    }

    if (this.dataProcessor.isPivoted) {
      this.dataProcessor.resetPivot();
      this.tableRenderer.renderTable(this.dataProcessor.currentData);
    } else {
      const pivotData = this.dataProcessor.createPivotTable(this.dataProcessor.currentData);
      this.dataProcessor.setPivotedData(pivotData);
      this.tableRenderer.renderTable(pivotData);
    }
    
    this.updatePivotButton();
  }

  // 샘플 데이터로 리셋
  resetToSample() {
    DOMUtils.safeDOMOperation('#jsonInput', (jsonInput) => {
      jsonInput.value = this.dataProcessor.getSampleJSON();
      this.resetTableToInitialState();
    });
  }

  // 테이블을 초기 상태로 리셋
  resetTableToInitialState() {
    this.dataProcessor.setCurrentData(null);
    this.tableRenderer.resetTableToInitialState();
    this.updatePivotButton();
    
    if (window.tableSearch) {
      window.tableSearch.hideSearch();
      window.tableSearch.resetSearchInput();
    }
  }

  // Copy 옵션 토글
  toggleCopyOptions() {
    const copyOptions = document.getElementById('copyOptions');
    const isVisible = copyOptions.classList.contains('show');
    
    if (isVisible) {
      this.hideCopyOptions();
    } else {
      this.showCopyOptions();
    }
  }

  // Copy 옵션 보이기
  showCopyOptions() {
    const copyOptions = document.getElementById('copyOptions');
    copyOptions.classList.add('show');
    this.updateCopyButtonArrow();
  }

  // Copy 옵션 숨기기
  hideCopyOptions() {
    const copyOptions = document.getElementById('copyOptions');
    copyOptions.classList.remove('show');
    this.updateCopyButtonArrow();
  }

  // Pivot 버튼 텍스트 업데이트
  updatePivotButton() {
    DOMUtils.safeDOMOperation('#pivotBtn', (pivotBtn) => {
      pivotBtn.textContent = this.dataProcessor.isPivoted ? '🔄 Restore' : '🔄 Pivot';
    });
  }

  // Copy 버튼 화살표 업데이트
  updateCopyButtonArrow() {
    DOMUtils.safeDOMOperation('#copyToClipboardBtn', (copyBtn) => {
      const copyOptions = document.getElementById('copyOptions');
      const isVisible = copyOptions.classList.contains('show');
      copyBtn.innerHTML = isVisible ? '📋 Copy ▲' : '📋 Copy ▼';
    });
  }

  // JSON 입력값 가져오기
  getJsonInput() {
    const jsonInput = DOMUtils.getElement('#jsonInput');
    return jsonInput ? jsonInput.value.trim() : '';
  }

  // 샘플 JSON 설정
  setSampleJSON() {
    DOMUtils.safeDOMOperation('#jsonInput', (jsonInput) => {
      if (!jsonInput.value.trim()) {
        jsonInput.value = this.dataProcessor.getSampleJSON();
      }
    });
  }

  // 테이블 생성 후 검색 기능 초기화
  initializeSearchAfterTableCreation() {
    if (window.tableSearch) {
      window.tableSearch.hideSearch();
    }
    
    setTimeout(() => {
      if (!window.tableSearch) {
        window.tableSearch = new TableSearch();
      } else {
        window.tableSearch.resetSearchInput();
      }
    }, 100);
  }
}
