<template>
  <div class="app-wrapper">
    <!-- Sidebar -->
    <aside class="sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="sidebar-header">
        <div class="logo">
          <i class="fas fa-flask"></i>
          <span v-if="!sidebarCollapsed">LabManager</span>
        </div>
        <button class="toggle-btn" @click="sidebarCollapsed = !sidebarCollapsed">
          <i :class="sidebarCollapsed ? 'fas fa-chevron-right' : 'fas fa-chevron-left'"></i>
        </button>
      </div>

      <nav class="sidebar-nav">
        <div class="nav-section">
          <span class="nav-section-title" v-if="!sidebarCollapsed">PRINCIPAL</span>
          <router-link to="/technicien" class="nav-item" exact-active-class="active">
            <i class="fas fa-tachometer-alt"></i>
            <span v-if="!sidebarCollapsed">Tableau de bord</span>
          </router-link>
        </div>

        <div class="nav-section">
          <span class="nav-section-title" v-if="!sidebarCollapsed">GESTION</span>
          <router-link to="/technicien/departements" class="nav-item" active-class="active">
            <i class="fas fa-building"></i>
            <span v-if="!sidebarCollapsed">Départements</span>
          </router-link>
          <router-link to="/technicien/laboratoires" class="nav-item" active-class="active">
            <i class="fas fa-flask"></i>
            <span v-if="!sidebarCollapsed">Laboratoires</span>
          </router-link>
          <router-link to="/technicien/equipements" class="nav-item" active-class="active">
            <i class="fas fa-cogs"></i>
            <span v-if="!sidebarCollapsed">Équipements</span>
          </router-link>
        </div>

        <div class="nav-section">
          <span class="nav-section-title" v-if="!sidebarCollapsed">UTILISATEURS</span>
          <router-link to="/technicien/etudiants" class="nav-item" active-class="active">
            <i class="fas fa-user-graduate"></i>
            <span v-if="!sidebarCollapsed">Étudiants</span>
          </router-link>
          <router-link to="/technicien/enseignants" class="nav-item" active-class="active">
            <i class="fas fa-chalkboard-teacher"></i>
            <span v-if="!sidebarCollapsed">Enseignants</span>
          </router-link>
        </div>

        <div class="nav-section">
          <span class="nav-section-title" v-if="!sidebarCollapsed">SUIVI</span>
 <!-- Réclamations avec sous-menu -->
<div class="nav-item has-submenu" @click="toggleReclamationsMenu">
  <i class="fas fa-exclamation-circle"></i>
  <span v-if="!sidebarCollapsed">Réclamations</span>
  <i
    v-if="!sidebarCollapsed"
    class="fas"
    :class="showReclamationsMenu ? 'fa-chevron-down' : 'fa-chevron-right'"
    style="margin-left:auto"
  ></i>
</div>

<!-- Sous-menu -->
<div class="submenu" v-if="showReclamationsMenu && !sidebarCollapsed">
  <router-link
    to="/technicien/reclamations/etudiants"
    class="submenu-item"
    active-class="active"
  >
    <i class="fas fa-user-graduate"></i>
    Étudiants
  </router-link>

  <router-link
    to="/technicien/reclamations/enseignants"
    class="submenu-item"
    active-class="active"
  >
    <i class="fas fa-chalkboard-teacher"></i>
    Enseignants
  </router-link>
</div>


          <router-link to="/technicien/notifications" class="nav-item" active-class="active">
            <i class="fas fa-bell"></i>
            <span v-if="!sidebarCollapsed">Notifications</span>
          </router-link>
        </div>
      </nav>

      <div class="sidebar-footer">
        <div class="user-info" v-if="!sidebarCollapsed">
          <div class="user-avatar">
            <i class="fas fa-user-cog"></i>
          </div>
          <div class="user-details">
            <span class="user-name">{{ userName }}</span>
            <span class="user-role">Technicien</span>
          </div>
        </div>
        <router-link to="/technicien/profil" class="profile-btn" v-if="!sidebarCollapsed">
          <i class="fas fa-user-cog"></i>
          <span>Mon profil</span>
        </router-link>
        <button class="logout-btn" @click="handleLogout">
          <i class="fas fa-sign-out-alt"></i>
          <span v-if="!sidebarCollapsed">Déconnexion</span>
        </button>
      </div>
    </aside>

    <!-- Main Content -->
    <main class="main-content">
      <!-- Top Header -->
      <header class="top-header">
        <div class="header-left">
          <h1 class="page-title">{{ currentPageTitle }}</h1>
        </div>
        <div class="header-right">
          <div class="header-actions">
            <!-- Composant Notification -->
            <NotificationBelle role="TECHNICIEN" />
            
            <!-- User Profile Dropdown -->
            <div class="user-dropdown-wrapper">
              <div class="user-dropdown" @click="toggleUserDropdown">
                <div class="avatar-with-status">
                  <div class="user-avatar-small" v-if="!profilePhotoUrl">{{ getInitials() }}</div>
                  <img v-else :src="profilePhotoUrl" class="user-avatar-img" alt="Photo profil">
                  <span class="online-indicator"></span>
                </div>
                <span class="user-name">{{ userFullName }}</span>
                <i class="fas fa-chevron-down"></i>
              </div>
              
              <!-- Dropdown Menu -->
              <div class="user-menu" v-if="showUserDropdown">
                <div class="user-menu-header">
                  <div class="user-avatar-large" v-if="!profilePhotoUrl">{{ getInitials() }}</div>
                  <img v-else :src="profilePhotoUrl" class="user-avatar-large-img" alt="Photo profil">
                  <div class="user-menu-info">
                    <span class="user-menu-name">{{ userFullName }}</span>
                    <span class="user-menu-role">Technicien</span>
                  </div>
                </div>
                <div class="user-menu-divider"></div>
                <router-link to="/technicien/profil" class="user-menu-item" @click="showUserDropdown = false">
                  <i class="fas fa-user-circle"></i>
                  <span>Mon profil</span>
                </router-link>
                <div class="user-menu-item" @click="handleLogout">
                  <i class="fas fa-sign-out-alt"></i>
                  <span>Déconnexion</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </header>

      <!-- Page Content -->
      <div class="page-content">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import NotificationBelle from '@/components/NotificationBelle.vue'
import api from '@/Service/api'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const sidebarCollapsed = ref(false)
const showUserDropdown = ref(false)
const profilePhotoUrl = ref<string | null>(null)
const showReclamationsMenu = ref(false)

const userName = computed(() => authStore.currentUser?.nom || '')
const userFullName = computed(() => {
  const user = authStore.currentUser
  return user ? `${user.prenom} ${user.nom}` : 'Technicien'
})

const currentPageTitle = computed(() => {
  const titles: Record<string, string> = {
    '/technicien': 'Tableau de bord',
    '/technicien/departements': 'Départements',
    '/technicien/laboratoires': 'Laboratoires',
    '/technicien/equipements': 'Équipements',
    '/technicien/etudiants': 'Étudiants',
    '/technicien/enseignants': 'Enseignants',
    '/technicien/reservations': 'Réservations',
    '/technicien/reclamations': 'Réclamations',
    '/technicien/profil': 'Mon Profil'
  }
  return titles[route.path] || 'Tableau de bord'
})

function getInitials(): string {
  const user = authStore.currentUser
  if (user) {
    return `${user.prenom?.charAt(0) || ''}${user.nom?.charAt(0) || ''}`.toUpperCase()
  }
  return 'TE'
}

function toggleUserDropdown() {
  showUserDropdown.value = !showUserDropdown.value
}

function toggleReclamationsMenu() {
  showReclamationsMenu.value = !showReclamationsMenu.value
}

function closeDropdownOnClickOutside(event: MouseEvent) {
  const target = event.target as HTMLElement
  if (!target.closest('.user-dropdown-wrapper')) {
    showUserDropdown.value = false
  }
}

async function loadProfilePhoto() {
  try {
    const res = await api.get('/users/photo', { responseType: 'blob' })
    if (res.data && res.data.size > 0) {
      profilePhotoUrl.value = URL.createObjectURL(res.data)
    }
  } catch (error) {
    profilePhotoUrl.value = null
  }
}

function handleLogout() {
  showUserDropdown.value = false
  authStore.logout()
  router.push('/')
}

onMounted(() => {
  loadProfilePhoto()
  document.addEventListener('click', closeDropdownOnClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', closeDropdownOnClickOutside)
})
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.app-wrapper {
  display: flex;
  min-height: 100vh;
  background: #f1f5f9;
  font-family: 'Poppins', sans-serif;
}

/* Sidebar */
.sidebar {
  width: 280px;
  background: linear-gradient(180deg, #1e293b 0%, #0f172a 100%);
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  position: fixed;
  height: 100vh;
  z-index: 100;
}

.sidebar.collapsed {
  width: 80px;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #fff;
  font-size: 20px;
  font-weight: 700;
}

.logo i {
  font-size: 28px;
  color: #14b8a6;
}

.toggle-btn {
  background: rgba(255,255,255,0.1);
  border: none;
  color: #94a3b8;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.toggle-btn:hover {
  background: rgba(255,255,255,0.2);
  color: #fff;
}

.sidebar-nav {
  flex: 1;
  padding: 20px 12px;
  overflow-y: auto;
}

.nav-section {
  margin-bottom: 24px;
}

.nav-section-title {
  font-size: 11px;
  font-weight: 600;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 1px;
  padding: 0 12px;
  margin-bottom: 8px;
  display: block;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  color: #94a3b8;
  text-decoration: none;
  border-radius: 10px;
  margin-bottom: 4px;
  transition: all 0.2s;
  font-size: 14px;
  font-weight: 500;
}

.nav-item:hover {
  background: rgba(255,255,255,0.05);
  color: #fff;
}

.nav-item.active {
  background: linear-gradient(135deg, #14b8a6 0%, #0d9488 100%);
  color: #fff;
  box-shadow: 0 4px 12px rgba(20, 184, 166, 0.4);
}

.nav-item i {
  font-size: 18px;
  width: 24px;
  text-align: center;
}

.sidebar.collapsed .nav-item {
  justify-content: center;
  padding: 14px;
}

.sidebar-footer {
  padding: 20px;
  border-top: 1px solid rgba(255,255,255,0.1);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.user-avatar {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #14b8a6 0%, #0d9488 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.user-details {
  display: flex;
  flex-direction: column;
}

.user-details .user-name {
  color: #fff;
  font-weight: 600;
  font-size: 14px;
}

.user-details .user-role {
  color: #64748b;
  font-size: 12px;
}

.logout-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 12px;
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.3);
  color: #ef4444;
  border-radius: 10px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
}

.logout-btn:hover {
  background: #ef4444;
  color: #fff;
}

/* Bouton profil */
.profile-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 10px 14px;
  background: rgba(59, 130, 246, 0.1);
  border: 1px solid rgba(59, 130, 246, 0.3);
  color: #3b82f6;
  border-radius: 8px;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 10px;
  transition: all 0.2s;
}

.profile-btn:hover {
  background: rgba(59, 130, 246, 0.2);
  color: #fff;
}

/* Main Content */
.main-content {
  flex: 1;
  margin-left: 280px;
  transition: margin-left 0.3s ease;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.sidebar.collapsed + .main-content {
  margin-left: 80px;
}

.top-header {
  background: #fff;
  padding: 20px 32px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #e2e8f0;
  position: sticky;
  top: 0;
  z-index: 50;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 20px;
}

.action-btn {
  position: relative;
  background: #f1f5f9;
  border: none;
  width: 44px;
  height: 44px;
  border-radius: 12px;
  cursor: pointer;
  color: #64748b;
  font-size: 18px;
  transition: all 0.2s;
}

.action-btn:hover {
  background: #e2e8f0;
  color: #1e293b;
}

.action-btn .badge {
  position: absolute;
  top: -4px;
  right: -4px;
  background: #ef4444;
  color: #fff;
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 10px;
  font-weight: 600;
}

/* User Dropdown Wrapper */
.user-dropdown-wrapper {
  position: relative;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: #f1f5f9;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.user-dropdown:hover {
  background: #e2e8f0;
}

.user-avatar-small {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
}

.user-avatar-img {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
}

.avatar-with-status {
  position: relative;
  display: inline-block;
}

.online-indicator {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 10px;
  height: 10px;
  background-color: #28a745;
  border-radius: 50%;
  border: 2px solid #fff;
  box-shadow: 0 0 4px rgba(40, 167, 69, 0.5);
}

.user-dropdown .user-name {
  font-weight: 600;
  color: #1e293b;
  font-size: 14px;
}

.user-dropdown i {
  color: #64748b;
  font-size: 12px;
}

/* User Menu Dropdown */
.user-menu {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 8px;
  width: 260px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0,0,0,0.15);
  z-index: 1000;
  overflow: hidden;
}

.user-menu-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: white;
}

.user-avatar-large {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: rgba(255,255,255,0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 600;
}

.user-avatar-large-img {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid rgba(255,255,255,0.3);
}

.user-menu-info {
  display: flex;
  flex-direction: column;
}

.user-menu-name {
  font-weight: 600;
  font-size: 14px;
}

.user-menu-role {
  font-size: 12px;
  opacity: 0.9;
}

.user-menu-divider {
  height: 1px;
  background: #e2e8f0;
}

.user-menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  color: #475569;
  text-decoration: none;
  cursor: pointer;
  transition: all 0.2s;
}

.user-menu-item:hover {
  background: #f8fafc;
  color: #3b82f6;
}

.user-menu-item i {
  width: 20px;
  text-align: center;
}

.page-content {
  flex: 1;
  padding: 32px;
}

/* Notification Dropdown */
.notification-wrapper {
  position: relative;
}

.notification-dropdown {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 10px;
  width: 380px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  z-index: 1000;
  overflow: hidden;
  animation: dropdownSlide 0.2s ease-out;
}

@keyframes dropdownSlide {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.notification-header {
  padding: 18px 20px;
  border-bottom: 1px solid #e2e8f0;
  background: linear-gradient(135deg, #14b8a6 0%, #0d9488 100%);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.notification-header h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #fff;
}

.notification-count {
  background: rgba(255, 255, 255, 0.25);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 20px;
}

.notification-list {
  max-height: 380px;
  overflow-y: auto;
}

.notification-list::-webkit-scrollbar {
  width: 6px;
}

.notification-list::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 16px 20px;
  border-bottom: 1px solid #f1f5f9;
  cursor: pointer;
  transition: all 0.2s;
}

.notification-item:hover {
  background: linear-gradient(135deg, #f0fdfa 0%, #f8fafc 100%);
  transform: translateX(4px);
}

.notification-item:last-child {
  border-bottom: none;
}

.notification-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
  transition: all 0.2s;
}

.notification-icon.reservation {
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
  color: #2563eb;
}

.notification-icon.reclamation {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  color: #d97706;
}

.notification-item:hover .notification-icon {
  transform: scale(1.1);
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-message {
  margin: 0 0 6px 0;
  font-size: 14px;
  color: #1e293b;
  line-height: 1.5;
  font-weight: 500;
}

.notification-labo {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #14b8a6;
  background: #f0fdfa;
  padding: 3px 10px;
  border-radius: 20px;
  margin-right: 8px;
  font-weight: 500;
}

.notification-labo i {
  font-size: 10px;
}

.notification-time {
  font-size: 12px;
  color: #94a3b8;
  display: block;
  margin-top: 6px;
}

.notification-empty {
  padding: 50px 20px;
  text-align: center;
  color: #94a3b8;
}

.notification-empty i {
  font-size: 48px;
  color: #cbd5e1;
  margin-bottom: 16px;
  display: block;
}

.notification-empty p {
  margin: 0;
  font-size: 15px;
  color: #64748b;
}

/* Animation pour le badge de notification */
.action-btn .badge {
  position: absolute;
  top: -4px;
  right: -4px;
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  color: #fff;
  font-size: 11px;
  padding: 2px 7px;
  border-radius: 10px;
  font-weight: 700;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
}

/* Responsive */
@media (max-width: 1024px) {
  .sidebar {
    width: 80px;
  }
  
  .sidebar .logo span,
  .sidebar .nav-section-title,
  .sidebar .nav-item span,
  .sidebar .user-info,
  .sidebar .logout-btn span {
    display: none;
  }
  
  .main-content {
    margin-left: 80px;
  }
  
  .notification-dropdown {
    width: 300px;
    right: -50px;
  }
}
.has-submenu {
  cursor: pointer;
  justify-content: flex-start;
}

.submenu {
  margin-left: 38px;
  margin-top: 4px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.submenu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  font-size: 13px;
  color: #94a3b8;
  text-decoration: none;
  border-radius: 8px;
  transition: all 0.2s;
}

.submenu-item:hover {
  background: rgba(255,255,255,0.05);
  color: #fff;
}

.submenu-item.active {
  background: linear-gradient(135deg, #14b8a6 0%, #0d9488 100%);
  color: #fff;
}

</style>
