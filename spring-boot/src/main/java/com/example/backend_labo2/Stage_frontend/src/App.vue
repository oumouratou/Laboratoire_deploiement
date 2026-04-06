<template>
  <router-view />
  <!-- Chatbot IA flottant - uniquement pour les utilisateurs connectés -->
  <ChatBot v-if="isAuthenticated && !isLoginPage" />
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import ChatBot from '@/components/ChatBot.vue';

const route = useRoute();
const authStore = useAuthStore();

// Vérifier si l'utilisateur est connecté
const isAuthenticated = computed(() => authStore.isAuthenticated);

// Vérifier si on est sur la page de connexion
const isLoginPage = computed(() => route.path === '/' || route.name === 'login');

// Charger les paramètres de thème au démarrage
onMounted(() => {
  // Charger le mode sombre
  const savedDarkMode = localStorage.getItem('darkMode');
  if (savedDarkMode === 'true') {
    document.body.classList.add('dark-mode');
  }
  
  // Charger la couleur d'accent
  const savedAccentColor = localStorage.getItem('accentColor');
  if (savedAccentColor) {
    document.documentElement.style.setProperty('--accent-color', savedAccentColor);
  }
});
</script>

<style>
html,
body,
#app {
  width: 100%;
  min-height: 100%;
  margin: 0;
  padding: 0;
}

body {
  min-height: 100vh;
  overflow-x: hidden;
}

#app {
  min-height: 100vh;
}

.app-wrapper {
  width: 100%;
  min-height: 100vh;
}

.main-sidebar {
  min-height: 100vh;
}
</style>
