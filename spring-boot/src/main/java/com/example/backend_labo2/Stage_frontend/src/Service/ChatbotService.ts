// Service pour communiquer avec l'API Flask du chatbot

const CHATBOT_API_URL = 'http://localhost:5001/api';

export interface ChatMessage {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  timestamp: Date;
  links?: ChatLink[];
  navigateTo?: string;
}

export interface ChatLink {
  label: string;
  path: string;
  icon: string;
}

export interface ChatSession {
  id: string;
  title: string;
  messages: ChatMessage[];
  liked: boolean;
  created_at: string;
  updated_at: string;
}

export interface ChatResponse {
  response: string;
  session_id: string;
  links?: ChatLink[];
  navigate_to?: string;
}

class ChatbotService {
  private userId: number | null = null;
  private userName: string = '';
  private userRole: string = '';
  private departmentId: number | null = null;
  private currentSessionId: string | null = null;
  private currentAbortController: AbortController | null = null;

  constructor() {
    this.loadUserInfo();
  }

  loadUserInfo(): void {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      try {
        const user = JSON.parse(userStr);
        this.userId = user.id;
        this.userName = `${user.prenom} ${user.nom}`;
        this.userRole = user.role?.toLowerCase() || '';
        this.departmentId = user.departement?.id || null;
      } catch (e) {
        console.error('Erreur parsing user:', e);
      }
    }
  }

  setUser(userId: number, userName: string, userRole?: string, departmentId?: number): void {
    this.userId = userId;
    this.userName = userName;
    if (userRole) {
      this.userRole = userRole.toLowerCase();
    }
    if (departmentId) {
      this.departmentId = departmentId;
    }
  }

  getDepartmentId(): number | null {
    this.loadUserInfo();
    return this.departmentId;
  }

  getUserId(): number | null {
    this.loadUserInfo();
    return this.userId;
  }

  getUserName(): string {
    this.loadUserInfo();
    return this.userName;
  }

  getUserRole(): string {
    this.loadUserInfo();
    return this.userRole;
  }

  getCurrentSessionId(): string | null {
    return this.currentSessionId;
  }

  setCurrentSessionId(sessionId: string | null | undefined): void {
    this.currentSessionId = sessionId ?? null;
  }

  // Méthode pour arrêter la réponse en cours
  stopCurrentResponse(): void {
    if (this.currentAbortController) {
      this.currentAbortController.abort();
      this.currentAbortController = null;
    }
  }

  // Vérifier si une réponse est en cours
  isResponseInProgress(): boolean {
    return this.currentAbortController !== null;
  }

  private getAuthToken(): string | null {
    return localStorage.getItem('token');
  }

  // ============== GESTION DES SESSIONS ==============

  async getSessions(): Promise<ChatSession[]> {
    try {
      const response = await fetch(`${CHATBOT_API_URL}/sessions?user_id=${this.userId}`);
      if (response.ok) {
        const data = await response.json();
        return data.sessions || [];
      }
      return [];
    } catch (error) {
      console.error('Erreur lors de la récupération des sessions:', error);
      return [];
    }
  }

  async createSession(firstMessage: string = ''): Promise<ChatSession | null> {
    try {
      const response = await fetch(`${CHATBOT_API_URL}/sessions`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          user_id: this.userId,
          first_message: firstMessage
        })
      });
      if (response.ok) {
        const session = await response.json();
        this.currentSessionId = session.id;
        return session;
      }
      return null;
    } catch (error) {
      console.error('Erreur lors de la création de session:', error);
      return null;
    }
  }

  async getSession(sessionId: string): Promise<ChatSession | null> {
    try {
      const response = await fetch(`${CHATBOT_API_URL}/sessions/${sessionId}?user_id=${this.userId}`);
      if (response.ok) {
        return await response.json();
      }
      return null;
    } catch (error) {
      console.error('Erreur lors de la récupération de session:', error);
      return null;
    }
  }

  async updateSessionTitle(sessionId: string, newTitle: string): Promise<ChatSession | null> {
    try {
      const response = await fetch(`${CHATBOT_API_URL}/sessions/${sessionId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          user_id: this.userId,
          title: newTitle
        })
      });
      if (response.ok) {
        return await response.json();
      }
      return null;
    } catch (error) {
      console.error('Erreur lors de la mise à jour de session:', error);
      return null;
    }
  }

  async toggleSessionLike(sessionId: string): Promise<ChatSession | null> {
    try {
      const response = await fetch(`${CHATBOT_API_URL}/sessions/${sessionId}/like`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          user_id: this.userId
        })
      });
      if (response.ok) {
        return await response.json();
      }
      return null;
    } catch (error) {
      console.error('Erreur lors du toggle like:', error);
      return null;
    }
  }

  async deleteSession(sessionId: string): Promise<boolean> {
    try {
      const response = await fetch(`${CHATBOT_API_URL}/sessions/${sessionId}?user_id=${this.userId}`, {
        method: 'DELETE'
      });
      if (response.ok) {
        if (this.currentSessionId === sessionId) {
          this.currentSessionId = null;
        }
        return true;
      }
      return false;
    } catch (error) {
      console.error('Erreur lors de la suppression de session:', error);
      return false;
    }
  }

  // ============== GESTION DES MESSAGES ==============

  getConversationHistory(): ChatMessage[] {
    const userId = this.getUserId();
    const sessionId = this.currentSessionId;
    if (!userId || !sessionId) return [];
    
    const historyKey = `chatbot_session_${userId}_${sessionId}`;
    const historyStr = localStorage.getItem(historyKey);
    if (historyStr) {
      try {
        const history = JSON.parse(historyStr);
        return history.map((msg: any) => ({
          ...msg,
          timestamp: new Date(msg.timestamp)
        }));
      } catch (e) {
        return [];
      }
    }
    return [];
  }

  saveConversationHistory(messages: ChatMessage[]): void {
    const userId = this.getUserId();
    const sessionId = this.currentSessionId;
    if (!userId || !sessionId) return;
    
    const historyKey = `chatbot_session_${userId}_${sessionId}`;
    localStorage.setItem(historyKey, JSON.stringify(messages));
  }

  async sendMessage(message: string): Promise<ChatResponse> {
    try {
      const token = this.getAuthToken();
      const headers: Record<string, string> = {
        'Content-Type': 'application/json',
      };
      
      if (token) {
        headers['Authorization'] = `Bearer ${token}`;
      }

      const response = await fetch(`${CHATBOT_API_URL}/chat`, {
        method: 'POST',
        headers,
        body: JSON.stringify({
          message: message,
          user_id: this.userId,
          user_name: this.userName,
          user_role: this.userRole,
          department_id: this.departmentId,
          session_id: this.currentSessionId,
          token: token ? `Bearer ${token}` : null,
        }),
      });

      if (!response.ok) {
        throw new Error(`Erreur HTTP: ${response.status}`);
      }

      const data = await response.json();
      if (data.session_id) {
        this.currentSessionId = data.session_id;
      }
      return data;
    } catch (error) {
      console.error('Erreur lors de l\'envoi du message:', error);
      throw error;
    }
  }

  async sendMessageStream(
    message: string,
    onChunk: (content: string) => void,
    onComplete: (links?: ChatLink[], navigateTo?: string, sessionId?: string) => void,
    onAbort?: () => void
  ): Promise<void> {
    try {
      // Créer un nouveau AbortController pour cette requête
      this.currentAbortController = new AbortController();
      const signal = this.currentAbortController.signal;

      const token = this.getAuthToken();
      const headers: Record<string, string> = {
        'Content-Type': 'application/json',
      };
      
      if (token) {
        headers['Authorization'] = `Bearer ${token}`;
      }

      const response = await fetch(`${CHATBOT_API_URL}/chat/stream`, {
        method: 'POST',
        headers,
        signal, // Ajouter le signal d'abort
        body: JSON.stringify({
          message: message,
          user_id: this.userId,
          user_name: this.userName,
          user_role: this.userRole,
          department_id: this.departmentId,
          session_id: this.currentSessionId,
          token: token ? `Bearer ${token}` : null,
        }),
      });

      if (!response.ok) {
        throw new Error(`Erreur HTTP: ${response.status}`);
      }

      const reader = response.body?.getReader();
      const decoder = new TextDecoder();
      let links: ChatLink[] | undefined;
      let navigateTo: string | undefined;
      let sessionId: string | undefined;

      if (!reader) {
        throw new Error('Impossible de lire la réponse');
      }

      try {
        while (true) {
          const { done, value } = await reader.read();
          
          if (done) {
            this.currentAbortController = null;
            onComplete(links, navigateTo, sessionId);
            break;
          }

          const chunk = decoder.decode(value);
          const lines = chunk.split('\n');

          for (const line of lines) {
            if (line.startsWith('data: ')) {
              try {
                const data = JSON.parse(line.substring(6));
                if (data.content) {
                  onChunk(data.content);
                }
                if (data.links) {
                  links = data.links;
                }
                if (data.navigate_to) {
                  navigateTo = data.navigate_to;
                }
                if (data.session_id) {
                  sessionId = data.session_id;
                  this.currentSessionId = sessionId ?? null;
                }
                if (data.done) {
                  this.currentAbortController = null;
                  onComplete(links, navigateTo, sessionId);
                }
              } catch (e) {
                // Ignorer les erreurs de parsing
              }
            }
          }
        }
      } catch (readError) {
        // Si c'est une erreur d'abort, appeler onAbort
        if (readError instanceof Error && readError.name === 'AbortError') {
          if (onAbort) onAbort();
          return;
        }
        throw readError;
      }
    } catch (error) {
      this.currentAbortController = null;
      if (error instanceof Error && error.name === 'AbortError') {
        console.log('Requête annulée par l\'utilisateur');
        if (onAbort) onAbort();
        return;
      }
      console.error('Erreur lors du streaming:', error);
      throw error;
    }
  }

  async clearConversation(sessionId?: string): Promise<void> {
    const userId = this.getUserId();
    const targetSessionId = sessionId || this.currentSessionId;
    
    if (userId && targetSessionId) {
      const historyKey = `chatbot_session_${userId}_${targetSessionId}`;
      localStorage.removeItem(historyKey);
    }
    
    try {
      await fetch(`${CHATBOT_API_URL}/chat/clear`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          user_id: this.userId,
          session_id: targetSessionId,
        }),
      });
    } catch (error) {
      console.error('Erreur lors de l\'effacement:', error);
    }
  }

  async checkHealth(): Promise<boolean> {
    try {
      const response = await fetch(`${CHATBOT_API_URL}/health`);
      return response.ok;
    } catch {
      return false;
    }
  }
}

export const chatbotService = new ChatbotService();
export default chatbotService;
