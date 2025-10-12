package locators;

public class PageLocators {
    // 기본 버튼들
    public static final String CONVERT_BUTTON = "//button[@id='convertBtn']";
    public static final String UPLOAD_BUTTON = "//button[@id='fileUploadBtn']";
    public static final String COPY_BUTTON = "//button[@id='copyToClipboardBtn']";
    public static final String EXPORT_BUTTON = "//button[@id='downloadExcelBtn']";
    public static final String PIVOT_BUTTON = "//button[@id='pivotBtn']";
    public static final String RESET_BUTTON = "//button[@id='resetBtn']";
    
    // 입력 필드들
    public static final String JSON_INPUT = "//textarea[@id='jsonInput']";
    public static final String FILE_INPUT = "//input[@id='jsonFileInput']";
    
    // 테이블 관련
    public static final String TABLE_HEADER_TH = "//table[@id='json-table']//thead//th";
    public static final String TABLE_BODY_ROWS = "//table[@id='json-table']//tbody//tr";
    public static final String TABLE_CONTAINER = "//div[@id='tableContainer']";
    public static final String JSON_TABLE = "//table[@id='json-table']";
    
    // Copy 옵션 드롭다운
    public static final String COPY_OPTIONS = "//div[@id='copyOptions']";
    public static final String COPY_MARKDOWN_OPTION = "//div[@id='copyOptions']//button[@data-format='markdown']";
    public static final String COPY_TEXT_OPTION = "//div[@id='copyOptions']//button[@data-format='text']";
    
    // 검색 기능
    public static final String SEARCH_CONTAINER = "//div[@id='custom-search-container']";
    public static final String SEARCH_INPUT = "//input[@id='custom-search-input']";
    public static final String SEARCH_PREV_BUTTON = "//button[@id='custom-search-prev']";
    public static final String SEARCH_NEXT_BUTTON = "//button[@id='custom-search-next']";
    public static final String SEARCH_COUNT = "//span[@id='custom-search-count']";
    public static final String SEARCH_CLOSE_BUTTON = "//button[@id='custom-search-close']";
    
    // 알림 메시지
    public static final String NOTIFICATION = "//div[contains(@class, 'notification')]";
    public static final String SUCCESS_NOTIFICATION = "//div[contains(@class, 'notification') and contains(@class, 'notification-success')]";
    public static final String ERROR_NOTIFICATION = "//div[contains(@class, 'notification') and contains(@class, 'notification-error')]";
    
    // 데이터 없음 메시지
    public static final String NO_DATA_MESSAGE = "//div[contains(@class, 'no-data-message')]";
    
    // 하이라이트된 검색 결과
    public static final String SEARCH_HIGHLIGHT = "//mark[contains(@class, 'search-highlight')]";
    public static final String CURRENT_MATCH = "//mark[contains(@class, 'current-match')]";
}


