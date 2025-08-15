// ===== 커스텀 검색 기능 =====
class TableSearch {
  constructor() {
    this.searchContainer = null;
    this.searchInput = null;
    this.prevBtn = null;
    this.nextBtn = null;
    this.matchCount = null;
    this.currentMatchIndex = -1;
    this.matches = [];
    this.isSearchActive = false;
    
    this.init();
  }

  init() {
    this.createSearchUI();
    this.bindEvents();
  }

  // 검색 UI 생성
  createSearchUI() {
    // 기존 검색창이 있다면 제거
    this.removeSearchUI();
    
    this.searchContainer = document.createElement('div');
    this.searchContainer.id = 'custom-search-container';
    this.searchContainer.className = 'custom-search-container';
    this.searchContainer.style.display = 'none';
    
         this.searchContainer.innerHTML = `
       <div class="search-input-wrapper">
         <input type="text" id="custom-search-input" placeholder="Search columns and rows..." class="search-input">
         <div class="search-controls">
           <button id="custom-search-prev" class="search-btn" title="Previous (Shift+Enter)">▲</button>
           <button id="custom-search-next" class="search-btn" title="Next (Enter)">▼</button>
           <span id="custom-search-count" class="search-count">0/0</span>
           <button id="custom-search-close" class="search-close-btn" title="Close (Esc)">✕</button>
         </div>
       </div>
     `;
    
    document.body.appendChild(this.searchContainer);
    
    // 요소 참조 저장
    this.searchInput = document.getElementById('custom-search-input');
    this.prevBtn = document.getElementById('custom-search-prev');
    this.nextBtn = document.getElementById('custom-search-next');
    this.matchCount = document.getElementById('custom-search-count');
  }

  // 검색 UI 제거
  removeSearchUI() {
    const existing = document.getElementById('custom-search-container');
    if (existing) {
      existing.remove();
    }
  }

  // 이벤트 바인딩
  bindEvents() {
    // Ctrl+F / Cmd+F 이벤트
    document.addEventListener('keydown', (e) => {
      if ((e.ctrlKey || e.metaKey) && e.key === 'f') {
        e.preventDefault();
        this.showSearch();
      }
    });

    // ESC 키 이벤트
    document.addEventListener('keydown', (e) => {
      if (e.key === 'Escape' && this.isSearchActive) {
        e.preventDefault();
        this.hideSearch();
      }
    });

    // 검색 입력 이벤트
    if (this.searchInput) {
      this.searchInput.addEventListener('input', (e) => {
        this.performSearch(e.target.value);
      });

      this.searchInput.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') {
          e.preventDefault();
          if (e.shiftKey) {
            this.previousMatch();
          } else {
            this.nextMatch();
          }
        }
      });
    }

    // 버튼 이벤트
    if (this.prevBtn) {
      this.prevBtn.addEventListener('click', () => this.previousMatch());
    }
    
    if (this.nextBtn) {
      this.nextBtn.addEventListener('click', () => this.nextMatch());
    }

    // 닫기 버튼 이벤트
    const closeBtn = document.getElementById('custom-search-close');
    if (closeBtn) {
      closeBtn.addEventListener('click', () => this.hideSearch());
    }
  }

  // 검색창 표시
  showSearch() {
    if (!this.searchContainer) {
      this.createSearchUI();
      this.bindEvents();
    }
    
    this.searchContainer.style.display = 'block';
    this.searchInput.focus();
    this.isSearchActive = true;
    
    // 기존 하이라이트 제거
    this.clearHighlights();
    
    // 검색 입력값 초기화 (테이블이 새로 생성된 경우를 대비)
    if (this.searchInput) {
      this.searchInput.value = '';
    }
  }

  // 검색창 숨김
  hideSearch() {
    if (this.searchContainer) {
      this.searchContainer.style.display = 'none';
    }
    this.isSearchActive = false;
    this.clearHighlights();
    this.matches = [];
    this.currentMatchIndex = -1;
    
    // 검색 입력값 초기화
    if (this.searchInput) {
      this.searchInput.value = '';
    }
    
    // 매치 카운트 초기화
    this.updateMatchCount();
  }

  // 검색 실행
  performSearch(query) {
    this.clearHighlights();
    this.matches = [];
    this.currentMatchIndex = -1;

    if (!query.trim()) {
      this.updateMatchCount();
      return;
    }

    // 테이블의 td 요소들에서 검색 (여러 방법으로 테이블 찾기)
    let table = document.querySelector('#json-table');
    if (!table) {
      // ID로 찾지 못한 경우 클래스로 찾기
      table = document.querySelector('.json-table');
    }
    if (!table) {
      // 테이블이 없으면 검색 중단
      this.updateMatchCount();
      return;
    }

    const tdElements = table.querySelectorAll('td');
    const thElements = table.querySelectorAll('th');
    const searchTerm = query.toLowerCase();

    // 컬럼명(th) 검색
    thElements.forEach((th, index) => {
      const text = th.textContent || th.innerText;
      if (text.toLowerCase().includes(searchTerm)) {
        this.matches.push({
          element: th,
          index: index,
          text: text,
          isHeader: true
        });
      }
    });

    // 데이터 셀(td) 검색
    tdElements.forEach((td, index) => {
      const text = td.textContent || td.innerText;
      if (text.toLowerCase().includes(searchTerm)) {
        this.matches.push({
          element: td,
          index: index + thElements.length, // 헤더 인덱스 이후로 설정
          text: text,
          isHeader: false
        });
      }
    });

    // 매치된 텍스트 하이라이트
    this.highlightMatches(query);
    
    if (this.matches.length > 0) {
      this.currentMatchIndex = 0;
      const headerMatches = this.matches.filter(m => m.isHeader).length;
      const dataMatches = this.matches.filter(m => !m.isHeader).length;
      console.log(`Search completed: ${this.matches.length} matches found (${headerMatches} headers, ${dataMatches} data), currentIndex = ${this.currentMatchIndex}`);
      this.scrollToMatch(0);
      // 하이라이트 업데이트 (현재 매치 표시)
      this.updateHighlights();
    } else {
      console.log('Search completed: no matches found');
      this.updateMatchCount();
    }
  }

  // 매치된 텍스트 하이라이트
  highlightMatches(query) {
    const searchTerm = query.toLowerCase();
    
    this.matches.forEach((match, index) => {
      const element = match.element;
      const text = match.text;
      const lowerText = text.toLowerCase();
      const searchIndex = lowerText.indexOf(searchTerm);
      
      if (searchIndex !== -1) {
        const before = text.substring(0, searchIndex);
        const matched = text.substring(searchIndex, searchIndex + query.length);
        const after = text.substring(searchIndex + query.length);
        
        // HTML 이스케이프 처리
        const escapeHtml = (text) => {
          const div = document.createElement('div');
          div.textContent = text;
          return div.innerHTML;
        };
        
        // 현재 매치 인덱스에 따라 current-match 클래스 적용
        const isCurrentMatch = index === this.currentMatchIndex;
        const highlightClass = `search-highlight ${isCurrentMatch ? 'current-match' : ''} ${match.isHeader ? 'header-highlight' : ''}`;
        element.innerHTML = `${escapeHtml(before)}<mark class="${highlightClass}">${escapeHtml(matched)}</mark>${escapeHtml(after)}`;
      }
    });
  }

  // 하이라이트 제거
  clearHighlights() {
    const highlights = document.querySelectorAll('.search-highlight');
    highlights.forEach(highlight => {
      const parent = highlight.parentNode;
      if (parent) {
        // 원본 텍스트로 복원
        const textContent = parent.textContent || parent.innerText;
        parent.innerHTML = '';
        parent.textContent = textContent;
      }
    });
  }

  // 다음 매치로 이동
  nextMatch() {
    if (this.matches.length === 0) return;
    
    this.currentMatchIndex = (this.currentMatchIndex + 1) % this.matches.length;
    console.log(`Next match: currentIndex = ${this.currentMatchIndex}`);
    this.scrollToMatch(this.currentMatchIndex);
    this.updateHighlights();
  }

  // 이전 매치로 이동
  previousMatch() {
    if (this.matches.length === 0) return;
    
    this.currentMatchIndex = this.currentMatchIndex <= 0 
      ? this.matches.length - 1 
      : this.currentMatchIndex - 1;
    console.log(`Previous match: currentIndex = ${this.currentMatchIndex}`);
    this.scrollToMatch(this.currentMatchIndex);
    this.updateHighlights();
  }

  // 매치 위치로 스크롤
  scrollToMatch(index) {
    if (index < 0 || index >= this.matches.length) return;
    
    const match = this.matches[index];
    const element = match.element;
    
    // 요소가 보이는 위치로 스크롤
    element.scrollIntoView({
      behavior: 'smooth',
      block: 'center',
      inline: 'nearest'
    });
  }

  // 하이라이트 업데이트
  updateHighlights() {
    // 모든 하이라이트에서 current-match 클래스 제거
    const highlights = document.querySelectorAll('.search-highlight');
    highlights.forEach(highlight => {
      highlight.classList.remove('current-match');
    });
    
    // 현재 매치에 current-match 클래스 추가
    if (this.currentMatchIndex >= 0 && this.currentMatchIndex < this.matches.length) {
      const currentMatch = this.matches[this.currentMatchIndex];
      const highlights = currentMatch.element.querySelectorAll('.search-highlight');
      highlights.forEach(highlight => {
        highlight.classList.add('current-match');
      });
    }
    
    // 매치 카운트 업데이트
    this.updateMatchCount();
  }

  // 매치 카운트 업데이트
  updateMatchCount() {
    if (this.matchCount) {
      const total = this.matches.length;
      const current = this.currentMatchIndex >= 0 ? this.currentMatchIndex + 1 : 0;
      const countText = total > 0 ? `${current}/${total}` : '0/0';
      this.matchCount.textContent = countText;
      
      // 디버깅용 로그
      console.log(`Match count updated: ${countText} (currentIndex: ${this.currentMatchIndex}, total: ${total})`);
    }
  }

  // 검색 입력값 초기화
  resetSearchInput() {
    if (this.searchInput) {
      this.searchInput.value = '';
      // matches와 currentMatchIndex는 이미 초기화되어 있을 수 있으므로 조건부로만 초기화
      if (this.matches.length > 0 || this.currentMatchIndex >= 0) {
        this.matches = [];
        this.currentMatchIndex = -1;
        this.updateMatchCount();
        console.log('Search input reset');
      }
    }
    // 하이라이트도 제거
    this.clearHighlights();
  }
}

// 페이지 로드 시 검색 기능 초기화
document.addEventListener('DOMContentLoaded', () => {
  // 테이블이 생성된 후에 검색 기능 초기화
  const initSearch = () => {
    const table = document.querySelector('#json-table') || document.querySelector('.json-table');
    if (table) {
      if (!window.tableSearch) {
        window.tableSearch = new TableSearch();
      }
    } else {
      // 테이블이 아직 생성되지 않았다면 잠시 후 다시 시도
      setTimeout(initSearch, 100);
    }
  };
  
  initSearch();
});

// 테이블이 동적으로 생성될 때를 대비한 MutationObserver
const observer = new MutationObserver((mutations) => {
  mutations.forEach((mutation) => {
    if (mutation.type === 'childList') {
      mutation.addedNodes.forEach((node) => {
        if (node.nodeType === Node.ELEMENT_NODE) {
          if (node.id === 'json-table' || 
              node.classList.contains('json-table') ||
              node.querySelector('#json-table') ||
              node.querySelector('.json-table')) {
            if (!window.tableSearch) {
              window.tableSearch = new TableSearch();
            }
          }
        }
      });
    }
  });
});

observer.observe(document.body, {
  childList: true,
  subtree: true
});
