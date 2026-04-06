<script setup lang="ts">
import { ref, nextTick, onMounted, computed, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import chatbotService, { type ChatMessage, type ChatLink, type ChatSession } from '@/Service/ChatbotService';

const router = useRouter();
const authStore = useAuthStore();

// État du chat
const isOpen = ref(false);
const showHistory = ref(false);
const isLoading = ref(false);
const isServiceAvailable = ref(true);
const userInput = ref('');
const messages = ref<ChatMessage[]>([]);
const chatContainer = ref<HTMLElement | null>(null);

// Sessions
const sessions = ref<ChatSession[]>([]);
const currentSession = ref<ChatSession | null>(null);
const editingSessionId = ref<string | null>(null);
const editingTitle = ref('');

// Informations utilisateur
const userName = computed(() => {
  if (authStore.currentUser) {
    return `${authStore.currentUser.prenom} ${authStore.currentUser.nom}`;
  }
  return chatbotService.getUserName() || 'Utilisateur';
});

const userRole = computed(() => authStore.currentUser?.role?.toLowerCase() || '');

// Message de bienvenue personnalisé
const getWelcomeMessage = (): ChatMessage => ({
  id: 'welcome',
  role: 'assistant',
  content: `Bonjour ${userName.value} ! 👋 Je suis votre assistant virtuel LabManager. Comment puis-je vous aider aujourd'hui ?`,
  timestamp: new Date()
});

// Liens rapides selon le rôle
const quickLinks = computed(() => {
  const basePath = `/${userRole.value}`;
  const links = [
    { label: '🔬 Laboratoires', path: `${basePath}/laboratoires`, icon: 'bi-building' },
    { label: '🖥️ Équipements', path: `${basePath}/equipements`, icon: 'bi-pc-display' },
  ];
  
  if (userRole.value === 'etudiant') {
    links.push({ label: '📅 Réservations', path: `${basePath}/reservations`, icon: 'bi-calendar' });
    links.push({ label: '⚠️ Réclamations', path: `${basePath}/reclamations`, icon: 'bi-exclamation-triangle' });
  } else if (userRole.value === 'enseignant') {
    links.push({ label: '⚠️ Réclamations', path: `${basePath}/reclamations`, icon: 'bi-exclamation-triangle' });
  } else if (userRole.value === 'technicien') {
    links.push({ label: '📅 Réservations', path: `${basePath}/reservations`, icon: 'bi-calendar' });
    links.push({ label: '⚠️ Réclamations', path: `${basePath}/reclamations`, icon: 'bi-exclamation-triangle' });
    links.push({ label: '🏢 Départements', path: `${basePath}/departements`, icon: 'bi-diagram-3' });
  }
  
  return links;
});

onMounted(async () => {
  if (authStore.currentUser) {
    chatbotService.setUser(
      authStore.currentUser.id,
      `${authStore.currentUser.prenom} ${authStore.currentUser.nom}`,
      authStore.currentUser.role,
      authStore.currentUser.departement?.id
    );
  }
  
  isServiceAvailable.value = await chatbotService.checkHealth();
  
  if (isServiceAvailable.value) {
    await loadSessions();
    
    // Restaurer automatiquement la dernière session active
    if (sessions.value.length > 0) {
      const lastSession = sessions.value[0]; // sessions triées par updated_at desc
      if (lastSession) {
        await selectSession(lastSession);
      }
    } else {
      messages.value.push(getWelcomeMessage());
    }
  } else {
    messages.value.push(getWelcomeMessage());
    messages.value.push({
      id: 'service_unavailable',
      role: 'assistant',
      content: '⚠️ Le service de chatbot est actuellement indisponible. Veuillez réessayer plus tard.',
      timestamp: new Date()
    });
  }
});

// Réessayer la connexion au service
const retryConnection = async () => {
  isLoading.value = true;
  isServiceAvailable.value = await chatbotService.checkHealth();
  isLoading.value = false;
  
  if (isServiceAvailable.value) {
    // Supprimer les anciens messages d'erreur de service
    messages.value = messages.value.filter(m => m.id !== 'service_unavailable');
    messages.value.push({
      id: `reconnected_${Date.now()}`,
      role: 'assistant',
      content: '✅ Connexion au service rétablie ! Vous pouvez maintenant poser vos questions.',
      timestamp: new Date()
    });
    await loadSessions();
  } else {
    messages.value.push({
      id: `retry_failed_${Date.now()}`,
      role: 'assistant',
      content: '❌ Le service est toujours indisponible. Assurez-vous que le serveur chatbot est démarré (python app.py dans chatbot-api/).',
      timestamp: new Date()
    });
  }
  await scrollToBottom();
};

// Charger les sessions
const loadSessions = async () => {
  sessions.value = await chatbotService.getSessions();
};

// Sélectionner une session
const selectSession = async (session: ChatSession) => {
  currentSession.value = session;
  chatbotService.setCurrentSessionId(session.id);
  
  // Charger les messages de la session
  const sessionDetails = await chatbotService.getSession(session.id);
  if (sessionDetails && sessionDetails.messages) {
    messages.value = [getWelcomeMessage()];
    sessionDetails.messages.forEach((msg: any) => {
      messages.value.push({
        id: `${msg.role}_${Date.now()}_${Math.random()}`,
        role: msg.role,
        content: msg.content,
        timestamp: new Date(msg.timestamp)
      });
    });
  }
  showHistory.value = false;
};

// Créer une nouvelle session
const createNewSession = async () => {
  const session = await chatbotService.createSession();
  if (session) {
    currentSession.value = session;
    await loadSessions();
    messages.value = [getWelcomeMessage()];
    showHistory.value = false;
  }
};

// Supprimer une session
const deleteSession = async (sessionId: string, event: Event) => {
  event.stopPropagation();
  if (confirm('Supprimer cette conversation ?')) {
    await chatbotService.deleteSession(sessionId);
    if (currentSession.value?.id === sessionId) {
      currentSession.value = null;
      chatbotService.setCurrentSessionId(null);
      messages.value = [getWelcomeMessage()];
    }
    await loadSessions();
  }
};

// Commencer l'édition du titre
const startEditTitle = (session: ChatSession, event: Event) => {
  event.stopPropagation();
  editingSessionId.value = session.id;
  editingTitle.value = session.title;
};

// Sauvegarder le titre
const saveTitle = async (sessionId: string) => {
  if (editingTitle.value.trim()) {
    await chatbotService.updateSessionTitle(sessionId, editingTitle.value);
    await loadSessions();
  }
  editingSessionId.value = null;
};

// Annuler l'édition
const cancelEdit = () => {
  editingSessionId.value = null;
  editingTitle.value = '';
};

// Toggle like
const toggleLike = async (sessionId: string, event: Event) => {
  event.stopPropagation();
  await chatbotService.toggleSessionLike(sessionId);
  await loadSessions();
};

// Surveiller les changements d'utilisateur
watch(() => authStore.currentUser, async (newUser) => {
  if (newUser) {
    chatbotService.setUser(newUser.id, `${newUser.prenom} ${newUser.nom}`, newUser.role);
    isServiceAvailable.value = await chatbotService.checkHealth();
    if (isServiceAvailable.value) {
      await loadSessions();
    }
    messages.value = [getWelcomeMessage()];
  }
}, { immediate: false });

// Basculer l'ouverture du chat
const toggleChat = async () => {
  isOpen.value = !isOpen.value;
  if (!isOpen.value) {
    showHistory.value = false;
  } else if (!isServiceAvailable.value) {
    // Re-tenter la connexion automatiquement à l'ouverture
    isServiceAvailable.value = await chatbotService.checkHealth();
    if (isServiceAvailable.value) {
      // Supprimer les anciens messages d'erreur
      messages.value = messages.value.filter(m => m.id !== 'service_unavailable');
      messages.value.push({
        id: `reconnected_${Date.now()}`,
        role: 'assistant',
        content: '✅ Connexion au service rétablie ! Vous pouvez maintenant poser vos questions.',
        timestamp: new Date()
      });
      await loadSessions();
    }
  }
};

// Basculer l'affichage de l'historique
const toggleHistory = async () => {
  showHistory.value = !showHistory.value;
  if (showHistory.value) {
    await loadSessions();
  }
};

// Faire défiler vers le bas
const scrollToBottom = async () => {
  await nextTick();
  if (chatContainer.value) {
    chatContainer.value.scrollTop = chatContainer.value.scrollHeight;
  }
};

// Naviguer vers un lien
const navigateToLink = (path: string) => {
  router.push(path);
  isOpen.value = false;
};

// Envoyer un message
const sendMessage = async () => {
  const message = userInput.value.trim();
  if (!message || isLoading.value) return;

  // Vérifier si le service est disponible avant d'envoyer
  if (!isServiceAvailable.value) {
    messages.value.push({
      id: `error_${Date.now()}`,
      role: 'assistant',
      content: '⚠️ Le service de chatbot est actuellement indisponible. Cliquez sur le bouton "Reconnecter" pour réessayer.',
      timestamp: new Date()
    });
    await scrollToBottom();
    return;
  }

  const userMessage: ChatMessage = {
    id: `user_${Date.now()}`,
    role: 'user',
    content: message,
    timestamp: new Date()
  };
  messages.value.push(userMessage);
  userInput.value = '';
  
  chatbotService.saveConversationHistory(messages.value);
  await scrollToBottom();

  isLoading.value = true;
  
  try {
    const assistantMessage: ChatMessage = {
      id: `assistant_${Date.now()}`,
      role: 'assistant',
      content: '',
      timestamp: new Date(),
      links: []
    };
    messages.value.push(assistantMessage);
    
    await chatbotService.sendMessageStream(
      message,
      (chunk: string) => {
        const lastMessage = messages.value[messages.value.length - 1];
        if (lastMessage && lastMessage.role === 'assistant') {
          lastMessage.content += chunk;
          scrollToBottom();
        }
      },
      (links?: ChatLink[], navigateTo?: string, sessionId?: string) => {
        const lastMessage = messages.value[messages.value.length - 1];
        if (lastMessage && lastMessage.role === 'assistant') {
          lastMessage.links = links || [];
          lastMessage.navigateTo = navigateTo;
        }
        isLoading.value = false;
        chatbotService.saveConversationHistory(messages.value);
        
        // Mettre à jour la session courante
        if (sessionId && !currentSession.value) {
          loadSessions();
        }
        
        if (navigateTo) {
          setTimeout(() => {
            navigateToLink(navigateTo);
          }, 1500);
        }
      },
      // Callback pour quand la réponse est arrêtée
      () => {
        const lastMessage = messages.value[messages.value.length - 1];
        if (lastMessage && lastMessage.role === 'assistant') {
          lastMessage.content += '\n\n⏹️ Réponse arrêtée par l\'utilisateur.';
        }
        isLoading.value = false;
        chatbotService.saveConversationHistory(messages.value);
      }
    );
  } catch (error) {
    console.error('Erreur:', error);
    
    const errorMessage: ChatMessage = {
      id: `error_${Date.now()}`,
      role: 'assistant',
      content: '❌ Désolé, une erreur s\'est produite. Veuillez réessayer plus tard.',
      timestamp: new Date()
    };
    
    messages.value[messages.value.length - 1] = errorMessage;
    isLoading.value = false;
  }
  
  await scrollToBottom();
};

// Arrêter la réponse en cours
const stopResponse = () => {
  chatbotService.stopCurrentResponse();
};

// Effacer la conversation
const clearChat = async () => {
  await chatbotService.clearConversation();
  messages.value = [getWelcomeMessage()];
  currentSession.value = null;
  chatbotService.setCurrentSessionId(null);
  chatbotService.saveConversationHistory(messages.value);
};

// Formater la date
const formatDate = (dateStr: string): string => {
  const date = new Date(dateStr);
  const now = new Date();
  
  if (date.toDateString() === now.toDateString()) {
    return "Aujourd'hui";
  }
  
  const yesterday = new Date(now);
  yesterday.setDate(yesterday.getDate() - 1);
  if (date.toDateString() === yesterday.toDateString()) {
    return "Hier";
  }
  
  return date.toLocaleDateString('fr-FR', { day: 'numeric', month: 'short' });
};

// Sessions groupées par date
const groupedSessions = computed(() => {
  const groups: { date: string; sessions: ChatSession[] }[] = [];
  let currentDate = '';
  
  sessions.value.forEach(session => {
    const sessionDate = formatDate(session.updated_at);
    if (sessionDate !== currentDate) {
      currentDate = sessionDate;
      groups.push({ date: sessionDate, sessions: [session] });
    } else {
      const lastGroup = groups[groups.length - 1];
      if (lastGroup) {
        lastGroup.sessions.push(session);
      }
    }
  });
  
  return groups;
});
</script>

<template>
  <div class="chatbot-wrapper">
    <!-- Bouton flottant -->
    <button class="chat-toggle-btn" @click="toggleChat" :class="{ 'is-open': isOpen }">
      <i v-if="!isOpen" class="bi bi-chat-dots-fill"></i>
      <i v-else class="bi bi-x-lg"></i>
    </button>

    <!-- Fenêtre de chat -->
    <transition name="slide-up">
      <div v-if="isOpen" class="chat-window" :class="{ 'with-history': showHistory }">
        <!-- Panneau d'historique des sessions -->
        <div v-if="showHistory" class="history-panel">
          <div class="history-header">
            <h6><i class="bi bi-clock-history"></i> Sessions</h6>
            <div class="history-actions">
              <button @click="createNewSession" class="new-session-btn" title="Nouvelle session">
                <i class="bi bi-plus-lg"></i>
              </button>
              <button @click="toggleHistory" class="close-history-btn">
                <i class="bi bi-x"></i>
              </button>
            </div>
          </div>
          <div class="history-content">
            <div v-for="group in groupedSessions" :key="group.date" class="history-group">
              <div class="history-date">{{ group.date }}</div>
              <div 
                v-for="session in group.sessions" 
                :key="session.id"
                class="session-item"
                :class="{ active: currentSession?.id === session.id }"
                @click="selectSession(session)"
              >
                <div class="session-content">
                  <!-- Mode édition -->
                  <div v-if="editingSessionId === session.id" class="edit-mode">
                    <input 
                      v-model="editingTitle" 
                      @keyup.enter="saveTitle(session.id)"
                      @keyup.esc="cancelEdit"
                      class="edit-input"
                      autofocus
                    />
                    <button @click="saveTitle(session.id)" class="save-btn">
                      <i class="bi bi-check"></i>
                    </button>
                    <button @click="cancelEdit" class="cancel-btn">
                      <i class="bi bi-x"></i>
                    </button>
                  </div>
                  <!-- Mode affichage -->
                  <div v-else class="session-info">
                    <i class="bi bi-chat-left-text"></i>
                    <span class="session-title">{{ session.title }}</span>
                    <i v-if="session.liked" class="bi bi-heart-fill liked"></i>
                  </div>
                </div>
                <div v-if="editingSessionId !== session.id" class="session-actions">
                  <button @click="startEditTitle(session, $event)" class="action-icon" title="Modifier">
                    <i class="bi bi-pencil"></i>
                  </button>
                  <button @click="toggleLike(session.id, $event)" class="action-icon" title="J'aime">
                    <i :class="session.liked ? 'bi bi-heart-fill text-danger' : 'bi bi-heart'"></i>
                  </button>
                  <button @click="deleteSession(session.id, $event)" class="action-icon delete" title="Supprimer">
                    <i class="bi bi-trash"></i>
                  </button>
                </div>
              </div>
            </div>
            <div v-if="sessions.length === 0" class="no-sessions">
              <i class="bi bi-chat-square-text"></i>
              <p>Aucune session</p>
              <button @click="createNewSession" class="create-session-btn">
                Nouvelle conversation
              </button>
            </div>
          </div>
        </div>

        <!-- Chat principal -->
        <div class="chat-main">
          <!-- Header -->
          <div class="chat-header">
            <div class="header-info">
              <div class="bot-avatar">
                <i class="bi bi-robot"></i>
              </div>
              <div class="header-text">
                <h6>Assistant IA</h6>
                <span class="user-name">{{ userName }}</span>
              </div>
            </div>
            <div class="header-actions">
              <button @click="toggleHistory" class="action-btn" title="Sessions" :class="{ active: showHistory }">
                <i class="bi bi-clock-history"></i>
              </button>
              <button @click="createNewSession" class="action-btn" title="Nouvelle session">
                <i class="bi bi-plus-lg"></i>
              </button>
              <button @click="clearChat" class="action-btn" title="Effacer">
                <i class="bi bi-trash"></i>
              </button>
              <button @click="toggleChat" class="action-btn" title="Fermer">
                <i class="bi bi-dash-lg"></i>
              </button>
            </div>
          </div>

          <!-- Liens rapides -->
          <div class="quick-links">
            <button v-for="link in quickLinks" :key="link.path" @click="navigateToLink(link.path)" class="quick-link-btn">
              {{ link.label }}
            </button>
          </div>

          <!-- Zone des messages -->
          <div class="chat-messages" ref="chatContainer">
            <div v-for="msg in messages" :key="msg.id" class="message" :class="msg.role">
              <div class="message-avatar">
                <i v-if="msg.role === 'assistant'" class="bi bi-robot"></i>
                <i v-else class="bi bi-person-fill"></i>
              </div>
              <div class="message-content">
                <p>{{ msg.content }}</p>
                <div v-if="msg.links && msg.links.length > 0" class="message-links">
                  <button v-for="link in msg.links" :key="link.path" @click="navigateToLink(link.path)" class="message-link-btn">
                    <i :class="link.icon"></i>
                    {{ link.label }}
                  </button>
                </div>
                <span class="message-time">
                  {{ msg.timestamp.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' }) }}
                </span>
              </div>
            </div>

            <div v-if="isLoading && messages[messages.length - 1]?.content === ''" class="message assistant">
              <div class="message-avatar">
                <i class="bi bi-robot"></i>
              </div>
              <div class="message-content">
                <div class="typing-indicator">
                  <span></span>
                  <span></span>
                  <span></span>
                </div>
              </div>
            </div>
          </div>

          <!-- Zone de saisie -->
          <div class="chat-input">
            <!-- Bouton de reconnexion si service indisponible -->
            <div v-if="!isServiceAvailable" class="reconnect-bar">
              <span class="reconnect-text"><i class="bi bi-exclamation-triangle"></i> Service déconnecté</span>
              <button @click="retryConnection" class="reconnect-btn" :disabled="isLoading">
                <i class="bi bi-arrow-clockwise" :class="{ 'spin': isLoading }"></i>
                {{ isLoading ? 'Connexion...' : 'Reconnecter' }}
              </button>
            </div>
            <div class="input-wrapper">
              <input
                type="text"
                v-model="userInput"
                @keyup.enter="sendMessage"
                placeholder="Tapez votre message..."
                :disabled="isLoading"
                class="chat-text-input"
              />
              <!-- Bouton Stop si en cours de chargement -->
              <button 
                v-if="isLoading" 
                @click="stopResponse" 
                class="stop-btn" 
                title="Arrêter la réponse"
              >
                <i class="bi bi-stop-fill"></i>
              </button>
              <!-- Bouton Envoyer sinon -->
              <button 
                v-else 
                @click="sendMessage" 
                :disabled="!userInput.trim()" 
                class="send-btn"
              >
                <i class="bi bi-send-fill"></i>
              </button>
            </div>
            <div class="powered-by">
              Propulsé par <strong>Llama AI</strong>
            </div>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<style scoped>
.chatbot-wrapper {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 99999;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  pointer-events: auto;
}

.chat-toggle-btn {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
  font-size: 24px;
  cursor: pointer;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chat-toggle-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
}

.chat-toggle-btn.is-open {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.chat-window {
  position: absolute;
  bottom: 80px;
  right: 0;
  width: 400px;
  height: 600px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  display: flex;
  overflow: hidden;
  z-index: 99999;
  pointer-events: auto;
  transition: width 0.3s ease;
}

.chat-window.with-history {
  width: 700px;
}

.history-panel {
  width: 300px;
  background: #f8f9fa;
  border-right: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
}

.history-header {
  padding: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.history-header h6 {
  margin: 0;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.history-actions {
  display: flex;
  gap: 8px;
}

.new-session-btn, .close-history-btn {
  background: rgba(255,255,255,0.2);
  border: none;
  color: white;
  width: 28px;
  height: 28px;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.new-session-btn:hover, .close-history-btn:hover {
  background: rgba(255,255,255,0.3);
}

.history-content {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

.history-group {
  margin-bottom: 16px;
}

.history-date {
  font-size: 11px;
  color: #64748b;
  font-weight: 600;
  text-transform: uppercase;
  margin-bottom: 8px;
  padding-left: 4px;
}

.session-item {
  padding: 10px 12px;
  background: white;
  border-radius: 8px;
  margin-bottom: 6px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.session-item:hover {
  background: #e2e8f0;
}

.session-item.active {
  background: #667eea;
  color: white;
}

.session-item.active .session-actions .action-icon {
  color: white;
}

.session-content {
  flex: 1;
  min-width: 0;
}

.session-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}

.session-info i:first-child {
  color: #667eea;
  font-size: 12px;
}

.session-item.active .session-info i:first-child {
  color: white;
}

.session-title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.liked {
  color: #f5576c;
  font-size: 10px;
}

.session-actions {
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.2s;
}

.session-item:hover .session-actions {
  opacity: 1;
}

.action-icon {
  background: none;
  border: none;
  padding: 4px;
  cursor: pointer;
  color: #64748b;
  font-size: 12px;
  border-radius: 4px;
}

.action-icon:hover {
  background: rgba(0,0,0,0.1);
}

.action-icon.delete:hover {
  color: #f5576c;
}

.edit-mode {
  display: flex;
  gap: 4px;
  align-items: center;
}

.edit-input {
  flex: 1;
  padding: 4px 8px;
  border: 1px solid #667eea;
  border-radius: 4px;
  font-size: 12px;
}

.save-btn, .cancel-btn {
  background: none;
  border: none;
  padding: 4px;
  cursor: pointer;
  font-size: 14px;
}

.save-btn { color: #22c55e; }
.cancel-btn { color: #ef4444; }

.no-sessions {
  text-align: center;
  padding: 40px 20px;
  color: #64748b;
}

.no-sessions i {
  font-size: 48px;
  opacity: 0.3;
}

.no-sessions p {
  margin: 12px 0;
}

.create-session-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.chat-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.bot-avatar {
  width: 45px;
  height: 45px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
}

.header-text h6 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.header-text .user-name {
  font-size: 12px;
  opacity: 0.9;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  width: 36px;
  height: 36px;
  background: rgba(255, 255, 255, 0.15);
  border: none;
  border-radius: 8px;
  color: white;
  cursor: pointer;
  transition: background 0.2s;
}

.action-btn:hover, .action-btn.active {
  background: rgba(255, 255, 255, 0.3);
}

.quick-links {
  padding: 10px 16px;
  background: #f8f9fa;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  border-bottom: 1px solid #e2e8f0;
}

.quick-link-btn {
  padding: 6px 12px;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 20px;
  font-size: 11px;
  color: #475569;
  cursor: pointer;
  transition: all 0.2s;
}

.quick-link-btn:hover {
  background: #667eea;
  color: white;
  border-color: #667eea;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #f8f9fa;
}

.message {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.message.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  flex-shrink: 0;
}

.message.assistant .message-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.message.user .message-avatar {
  background: #e2e8f0;
  color: #475569;
}

.message-content {
  max-width: 75%;
  padding: 12px 16px;
  border-radius: 16px;
  position: relative;
}

.message.assistant .message-content {
  background: white;
  border-bottom-left-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.message.user .message-content {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-bottom-right-radius: 4px;
}

.message-content p {
  margin: 0;
  font-size: 14px;
  line-height: 1.5;
  white-space: pre-wrap;
}

.message-time {
  display: block;
  font-size: 10px;
  opacity: 0.6;
  margin-top: 6px;
}

.message-links {
  margin-top: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.message-link-btn {
  padding: 6px 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 8px;
  color: white;
  font-size: 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: transform 0.2s, box-shadow 0.2s;
}

.message-link-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 4px 0;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  background: #667eea;
  border-radius: 50%;
  animation: bounce 1.4s infinite ease-in-out;
}

.typing-indicator span:nth-child(1) { animation-delay: -0.32s; }
.typing-indicator span:nth-child(2) { animation-delay: -0.16s; }

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

.chat-input {
  padding: 16px;
  background: white;
  border-top: 1px solid #e2e8f0;
}

.input-wrapper {
  display: flex;
  gap: 10px;
  align-items: center;
}

.chat-text-input {
  flex: 1;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  padding: 12px 16px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
  background: white;
  height: 44px;
  box-sizing: border-box;
}

.chat-text-input:focus {
  border-color: #667eea;
}

.chat-text-input:disabled {
  background: #f1f5f9;
  cursor: not-allowed;
}

.send-btn {
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 12px;
  color: white;
  font-size: 18px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.send-btn:hover:not(:disabled) {
  transform: scale(1.05);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Bouton Stop */
.stop-btn {
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  border: none;
  border-radius: 12px;
  color: white;
  font-size: 18px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: pulse 1.5s infinite;
}

.stop-btn:hover {
  transform: scale(1.05);
  background: linear-gradient(135deg, #dc2626 0%, #b91c1c 100%);
}

@keyframes pulse {
  0%, 100% {
    box-shadow: 0 0 0 0 rgba(239, 68, 68, 0.4);
  }
  50% {
    box-shadow: 0 0 0 8px rgba(239, 68, 68, 0);
  }
}

.powered-by {
  text-align: center;
  font-size: 11px;
  color: #94a3b8;
  margin-top: 10px;
}

.reconnect-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fef3c7;
  border: 1px solid #f59e0b;
  border-radius: 8px;
  padding: 8px 12px;
  margin-bottom: 8px;
}

.reconnect-text {
  font-size: 12px;
  color: #92400e;
  font-weight: 500;
}

.reconnect-text i {
  margin-right: 4px;
  color: #f59e0b;
}

.reconnect-btn {
  background: #f59e0b;
  color: white;
  border: none;
  border-radius: 6px;
  padding: 4px 12px;
  font-size: 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: background 0.2s;
}

.reconnect-btn:hover:not(:disabled) {
  background: #d97706;
}

.reconnect-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.reconnect-btn i.spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.slide-up-enter-active, .slide-up-leave-active {
  transition: all 0.3s ease;
}

.slide-up-enter-from, .slide-up-leave-to {
  opacity: 0;
  transform: translateY(20px);
}

.chat-messages::-webkit-scrollbar, .history-content::-webkit-scrollbar {
  width: 6px;
}

.chat-messages::-webkit-scrollbar-track, .history-content::-webkit-scrollbar-track {
  background: transparent;
}

.chat-messages::-webkit-scrollbar-thumb, .history-content::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;
}

.text-danger {
  color: #f5576c !important;
}

@media (max-width: 768px) {
  .chat-window {
    width: calc(100vw - 40px);
    height: calc(100vh - 120px);
    bottom: 70px;
    right: -10px;
  }
  
  .chat-window.with-history {
    width: calc(100vw - 40px);
  }
  
  .history-panel {
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 80%;
    z-index: 10;
    box-shadow: 2px 0 10px rgba(0,0,0,0.1);
  }
}
</style>
