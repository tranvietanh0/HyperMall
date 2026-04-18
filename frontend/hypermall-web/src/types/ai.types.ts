export interface AiChatContext {
  pageType: 'product-detail' | 'product-list' | 'search' | 'other';
  path: string;
  productId?: number;
  productName?: string;
}

export interface AiChatHistoryItem {
  role: 'user' | 'assistant';
  content: string;
}

export interface AiSuggestedAction {
  type: 'link' | 'search' | 'category';
  label: string;
  value: string;
}

export interface AiProductSuggestion {
  productId: number;
  productName: string;
  thumbnail?: string;
  price?: number;
}

export interface AiChatRequest {
  message: string;
  sessionId?: string;
  history: AiChatHistoryItem[];
  context: AiChatContext;
}

export interface AiChatResponse {
  message: string;
  sessionId: string;
  suggestedActions: AiSuggestedAction[];
  productSuggestions: AiProductSuggestion[];
  degraded: boolean;
}

export interface AiChatUiMessage {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  degraded?: boolean;
  suggestedActions?: AiSuggestedAction[];
  productSuggestions?: AiProductSuggestion[];
}
