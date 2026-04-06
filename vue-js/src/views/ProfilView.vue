<template>
  <div class="profil-container">
    <section class="content-header">
      <div class="container-fluid">
        <div class="row mb-2">
          <div class="col-sm-6">
            <h1><i class="fas fa-user-circle mr-2"></i> Mon Profil</h1>
          </div>
          <div class="col-sm-6 text-right">
            <button class="btn btn-secondary" @click="$router.back()">
              <i class="fas fa-arrow-left mr-1"></i> Retour
            </button>
          </div>
        </div>
      </div>
    </section>

    <section class="content">
      <div class="container-fluid">
        <div class="row">
          <!-- Carte Profil Photo -->
          <div class="col-md-4">
            <div class="card card-primary card-outline">
              <div class="card-body box-profile text-center">
                <!-- Photo de profil -->
                <div class="profile-avatar-container">
                  <img 
                    v-if="profilePhotoUrl" 
                    :src="profilePhotoUrl" 
                    class="profile-user-img img-fluid img-circle"
                    alt="Photo de profil"
                  >
                  <div v-else class="profile-initials">
                    {{ getInitials() }}
                  </div>
                  
                  <!-- Bouton upload photo -->
                  <label class="upload-btn" title="Changer la photo">
                    <i class="fas fa-camera"></i>
                    <input 
                      type="file" 
                      accept="image/*" 
                      @change="handlePhotoUpload"
                      style="display: none;"
                    >
                  </label>
                </div>

                <h3 class="profile-username mt-3">{{ user?.prenom }} {{ user?.nom }}</h3>
                <p class="text-muted">
                  <span :class="getRoleBadge(user?.role)">
                    <i :class="getRoleIcon(user?.role)" class="mr-1"></i>
                    {{ formatRole(user?.role) }}
                  </span>
                </p>

                <!-- Indicateur en ligne -->
                <p class="text-success mb-3">
                  <i class="fas fa-circle text-success mr-1" style="font-size: 10px;"></i>
                  En ligne
                </p>

                <ul class="list-group list-group-unbordered mb-3">
                  <li class="list-group-item">
                    <b><i class="fas fa-envelope mr-2"></i>Email</b> 
                    <a :href="'https://mail.google.com/mail/?view=cm&fs=1&to=' + encodeURIComponent(user?.email || '')" target="_blank" class="float-right text-primary">
                      {{ user?.email }}
                    </a>
                  </li>
                  <li class="list-group-item">
                    <b><i class="fas fa-id-card mr-2"></i>CIN</b> 
                    <span class="float-right">{{ user?.cin || 'N/A' }}</span>
                  </li>
                  <li class="list-group-item" v-if="user?.role !== 'TECHNICIEN'">
                    <b><i class="fas fa-building mr-2"></i>Département</b> 
                    <span class="float-right">{{ getDepartementNom() }}</span>
                  </li>
                </ul>

                <button class="btn btn-primary btn-block" @click="showEditModal = true">
                  <i class="fas fa-edit mr-1"></i> Modifier le profil
                </button>
              </div>
            </div>
          </div>

          <!-- Informations détaillées -->
          <div class="col-md-8">
            <div class="card">
              <div class="card-header p-2">
                <ul class="nav nav-pills">
                  <li class="nav-item">
                    <a class="nav-link" :class="{ active: activeTab === 'informations' }" href="#" @click.prevent="activeTab = 'informations'">
                      <i class="fas fa-info-circle mr-1"></i> Informations
                    </a>
                  </li>
                  <li class="nav-item">
                    <a class="nav-link" :class="{ active: activeTab === 'parametres' }" href="#" @click.prevent="activeTab = 'parametres'">
                      <i class="fas fa-cog mr-1"></i> Paramètres
                    </a>
                  </li>
                </ul>
              </div>

              <div class="card-body">
                <div class="tab-content">
                  <!-- Onglet Informations -->
                  <div v-if="activeTab === 'informations'" class="tab-pane active" id="informations">
                    <table class="table table-bordered">
                      <tbody>
                        <tr>
                          <th style="width: 200px;"><i class="fas fa-user mr-2"></i>Nom complet</th>
                          <td>{{ user?.prenom }} {{ user?.nom }}</td>
                        </tr>
                        <tr>
                          <th><i class="fas fa-envelope mr-2"></i>Email</th>
                          <td>
                            <a :href="'https://mail.google.com/mail/?view=cm&fs=1&to=' + encodeURIComponent(user?.email || '')" target="_blank" class="text-primary">
                              {{ user?.email }}
                            </a>
                          </td>
                        </tr>
                        <tr>
                          <th><i class="fas fa-id-card mr-2"></i>CIN</th>
                          <td>{{ user?.cin || 'Non renseigné' }}</td>
                        </tr>
                        <tr>
                          <th><i class="fas fa-user-tag mr-2"></i>Rôle</th>
                          <td>
                            <span :class="getRoleBadge(user?.role)">{{ formatRole(user?.role) }}</span>
                          </td>
                        </tr>
                        <tr v-if="user?.role !== 'TECHNICIEN'">
                          <th><i class="fas fa-building mr-2"></i>Département</th>
                          <td>{{ getDepartementNom() }}</td>
                        </tr>
                        <tr>
                          <th><i class="fas fa-calendar mr-2"></i>Date d'inscription</th>
                          <td>{{ formatDate(user?.dateCreation) }}</td>
                        </tr>
                      </tbody>
                    </table>
                  </div>

                  <!-- Onglet Paramètres -->
                  <div v-if="activeTab === 'parametres'" class="tab-pane active" id="parametres">
                    <!-- Section Mot de passe -->
                    <div class="settings-section">
                      <h5 class="settings-title"><i class="fas fa-lock mr-2"></i>Mot de passe</h5>
                      
                      <div class="form-group">
                        <label>Mot de passe actuel</label>
                        <div class="input-group">
                          <input 
                            :type="showPassword ? 'text' : 'password'" 
                            class="form-control" 
                            v-model="passwordForm.currentPassword"
                            placeholder="Votre mot de passe actuel"
                          >
                          <div class="input-group-append">
                            <button class="btn btn-outline-secondary" type="button" @click="showPassword = !showPassword">
                              <i :class="showPassword ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
                            </button>
                          </div>
                        </div>
                      </div>

                      <div class="form-group">
                        <label>Nouveau mot de passe</label>
                        <div class="input-group">
                          <input 
                            :type="showNewPassword ? 'text' : 'password'" 
                            class="form-control" 
                            v-model="passwordForm.newPassword"
                            placeholder="Nouveau mot de passe"
                          >
                          <div class="input-group-append">
                            <button class="btn btn-outline-secondary" type="button" @click="showNewPassword = !showNewPassword">
                              <i :class="showNewPassword ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
                            </button>
                          </div>
                        </div>
                      </div>

                      <div class="form-group">
                        <label>Confirmer le nouveau mot de passe</label>
                        <input 
                          type="password" 
                          class="form-control" 
                          v-model="passwordForm.confirmPassword"
                          placeholder="Confirmer le nouveau mot de passe"
                        >
                      </div>

                      <button class="btn btn-primary" @click="handleChangePassword" :disabled="isChangingPassword">
                        <i class="fas fa-key mr-1"></i>
                        {{ isChangingPassword ? 'Modification...' : 'Modifier le mot de passe' }}
                      </button>
                    </div>

                    <hr>

                    <div class="settings-section">
                      <h5 class="settings-title"><i class="fas fa-palette mr-2"></i>Apparence</h5>
                      
                      <div class="setting-item">
                        <div class="setting-info">
                          <label class="setting-label">Thème sombre</label>
                          <p class="setting-description">Activer le mode sombre pour réduire la fatigue oculaire</p>
                        </div>
                        <div class="setting-control">
                          <label class="switch">
                            <input type="checkbox" v-model="darkMode" @change="toggleDarkMode">
                            <span class="slider round"></span>
                          </label>
                        </div>
                      </div>

                      <div class="setting-item">
                        <div class="setting-info">
                          <label class="setting-label">Couleur d'accent</label>
                          <p class="setting-description">Personnalisez la couleur principale de l'interface</p>
                        </div>
                        <div class="setting-control">
                          <div class="color-options">
                            <button 
                              v-for="color in accentColors" 
                              :key="color.value"
                              class="color-btn"
                              :class="{ active: selectedAccentColor === color.value }"
                              :style="{ backgroundColor: color.value }"
                              :title="color.name"
                              @click="setAccentColor(color.value)"
                            ></button>
                          </div>
                        </div>
                      </div>
                    </div>

                    <hr>

                    <div class="settings-section">
                      <h5 class="settings-title"><i class="fas fa-bell mr-2"></i>Notifications</h5>
                      
                      <div class="setting-item">
                        <div class="setting-info">
                          <label class="setting-label">Notifications push</label>
                          <p class="setting-description">Recevoir des notifications en temps réel</p>
                        </div>
                        <div class="setting-control">
                          <label class="switch">
                            <input type="checkbox" v-model="notificationsEnabled">
                            <span class="slider round"></span>
                          </label>
                        </div>
                      </div>

                      <div class="setting-item">
                        <div class="setting-info">
                          <label class="setting-label">Notifications par email</label>
                          <p class="setting-description">Recevoir un résumé par email</p>
                        </div>
                        <div class="setting-control">
                          <label class="switch">
                            <input type="checkbox" v-model="emailNotifications">
                            <span class="slider round"></span>
                          </label>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Modal Edition Profil -->
    <div class="modal fade" :class="{ show: showEditModal }" :style="{ display: showEditModal ? 'block' : 'none' }">
      <div class="modal-dialog">
        <div class="modal-content">
          <form @submit.prevent="handleUpdateProfile">
            <div class="modal-header bg-primary text-white">
              <h5 class="modal-title"><i class="fas fa-edit mr-2"></i>Modifier le profil</h5>
              <button type="button" class="close text-white" @click="showEditModal = false">&times;</button>
            </div>
            <div class="modal-body">
              <div class="form-group">
                <label>Nom</label>
                <input type="text" class="form-control" v-model="editForm.nom" required>
              </div>
              <div class="form-group">
                <label>Prénom</label>
                <input type="text" class="form-control" v-model="editForm.prenom" required>
              </div>
              <div class="form-group">
                <label>Email</label>
                <input type="email" class="form-control" v-model="editForm.email" required>
              </div>
              <div class="form-group">
                <label>CIN</label>
                <input type="text" class="form-control" v-model="editForm.cin">
              </div>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" @click="showEditModal = false">Annuler</button>
              <button type="submit" class="btn btn-primary" :disabled="isUpdating">
                {{ isUpdating ? 'Enregistrement...' : 'Enregistrer' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
    <div class="modal-backdrop fade show" v-if="showEditModal"></div>

    <!-- Alerte -->
    <div v-if="alertMessage" :class="['alert-toast', alertType]">
      <i :class="alertType === 'success' ? 'fas fa-check-circle' : 'fas fa-exclamation-circle'"></i>
      {{ alertMessage }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import api from '@/Service/api'

const authStore = useAuthStore()

const user = computed(() => authStore.currentUser)
const profilePassword = ref('')
const profilePhotoUrl = ref<string | null>(null)
const showEditModal = ref(false)
const isUpdating = ref(false)
const alertMessage = ref('')
const alertType = ref<'success' | 'error'>('success')
const activeTab = ref<'informations' | 'parametres'>('informations')

// Paramètres thème
const darkMode = ref(false)
const selectedAccentColor = ref('#007bff')
const notificationsEnabled = ref(true)
const emailNotifications = ref(false)

// Mot de passe
const showPassword = ref(false)
const showNewPassword = ref(false)
const isChangingPassword = ref(false)
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const accentColors = [
  { name: 'Bleu', value: '#007bff' },
  { name: 'Vert', value: '#28a745' },
  { name: 'Rouge', value: '#dc3545' },
  { name: 'Orange', value: '#fd7e14' },
  { name: 'Violet', value: '#6f42c1' },
  { name: 'Rose', value: '#e83e8c' },
  { name: 'Cyan', value: '#17a2b8' },
  { name: 'Sombre', value: '#343a40' }
]

function toggleDarkMode() {
  if (darkMode.value) {
    document.body.classList.add('dark-mode')
    localStorage.setItem('darkMode', 'true')
  } else {
    document.body.classList.remove('dark-mode')
    localStorage.setItem('darkMode', 'false')
  }
}

function setAccentColor(color: string) {
  selectedAccentColor.value = color
  document.documentElement.style.setProperty('--accent-color', color)
  localStorage.setItem('accentColor', color)
}

function loadSettings() {
  // Charger thème sombre
  const savedDarkMode = localStorage.getItem('darkMode')
  if (savedDarkMode === 'true') {
    darkMode.value = true
    document.body.classList.add('dark-mode')
  }
  
  // Charger couleur d'accent
  const savedAccentColor = localStorage.getItem('accentColor')
  if (savedAccentColor) {
    selectedAccentColor.value = savedAccentColor
    document.documentElement.style.setProperty('--accent-color', savedAccentColor)
  }
}

const editForm = reactive({
  nom: '',
  prenom: '',
  email: '',
  cin: ''
})

function getInitials(): string {
  const u = user.value
  if (u) {
    return `${u.prenom?.charAt(0) || ''}${u.nom?.charAt(0) || ''}`.toUpperCase()
  }
  return 'U'
}

function getRoleBadge(role?: string): string {
  const badges: Record<string, string> = {
    ETUDIANT: 'badge badge-info',
    ENSEIGNANT: 'badge badge-primary',
    TECHNICIEN: 'badge badge-success'
  }
  return badges[role || ''] || 'badge badge-secondary'
}

function getRoleIcon(role?: string): string {
  const icons: Record<string, string> = {
    ETUDIANT: 'fas fa-user-graduate',
    ENSEIGNANT: 'fas fa-chalkboard-teacher',
    TECHNICIEN: 'fas fa-tools'
  }
  return icons[role || ''] || 'fas fa-user'
}

function formatRole(role?: string): string {
  const labels: Record<string, string> = {
    ETUDIANT: 'Étudiant',
    ENSEIGNANT: 'Enseignant',
    TECHNICIEN: 'Technicien'
  }
  return labels[role || ''] || role || 'N/A'
}

function formatDate(dateStr?: string): string {
  if (!dateStr) return 'N/A'
  return new Date(dateStr).toLocaleDateString('fr-FR', {
    day: '2-digit',
    month: 'long',
    year: 'numeric'
  })
}

// Récupérer le nom du département correctement
function getDepartementNom(): string {
  const u = user.value
  if (!u) return 'Non renseigné'
  
  // Vérifier plusieurs formats possibles
  if (u.departement?.nom) return u.departement.nom
  if (typeof u.departement === 'string') return u.departement
  if ((u as any).departementNom) return (u as any).departementNom
  
  return 'Non renseigné'
}

function getPasswordDisplay(): string {
  if (profilePassword.value) return profilePassword.value
  const u = user.value as any
  if (!u) return 'Non disponible'
  return u.password || u.motDePasse || u.mdp || u.plainPassword || 'Non disponible'
}

async function loadProfileDetails() {
  try {
    const res = await api.get('/users/profil')
    const data = res.data || {}

    profilePassword.value = data.password || data.motDePasse || data.mdp || data.plainPassword || ''

    if (authStore.currentUser) {
      Object.assign(authStore.currentUser as any, {
        password: data.password,
        motDePasse: data.motDePasse,
        mdp: data.mdp,
        plainPassword: data.plainPassword,
      })
    }
  } catch (error) {
    console.warn('Impossible de charger les détails du profil:', error)
  }
}

function showAlert(message: string, type: 'success' | 'error') {
  alertMessage.value = message
  alertType.value = type
  setTimeout(() => alertMessage.value = '', 3000)
}

async function handlePhotoUpload(event: Event) {
  const input = event.target as HTMLInputElement
  if (!input.files || !input.files[0]) return

  const file = input.files[0]
  
  // Vérifier la taille (max 5MB)
  if (file.size > 5 * 1024 * 1024) {
    showAlert('La photo ne doit pas dépasser 5MB', 'error')
    return
  }

  // Afficher la prévisualisation
  const reader = new FileReader()
  reader.onload = (e) => {
    profilePhotoUrl.value = e.target?.result as string
  }
  reader.readAsDataURL(file)

  // Upload vers le serveur
  try {
    const formData = new FormData()
    formData.append('photo', file)
    
    await api.post('/users/photo', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    showAlert('Photo mise à jour avec succès', 'success')
  } catch (error) {
    console.error('Erreur upload photo:', error)
    showAlert('Erreur lors de l\'upload de la photo', 'error')
  }
}

async function handleUpdateProfile() {
  isUpdating.value = true
  try {
    await api.put('/users/profil', {
      nom: editForm.nom,
      prenom: editForm.prenom,
      email: editForm.email,
      cin: editForm.cin
    })
    
    // Mettre à jour le store
    if (authStore.currentUser) {
      authStore.currentUser.nom = editForm.nom
      authStore.currentUser.prenom = editForm.prenom
      authStore.currentUser.email = editForm.email
      authStore.currentUser.cin = editForm.cin
    }
    
    showEditModal.value = false
    showAlert('Profil mis à jour avec succès', 'success')
  } catch (error) {
    console.error('Erreur mise à jour profil:', error)
    showAlert('Erreur lors de la mise à jour du profil', 'error')
  } finally {
    isUpdating.value = false
  }
}

async function handleChangePassword() {
  if (!passwordForm.currentPassword || !passwordForm.newPassword) {
    showAlert('Veuillez remplir tous les champs', 'error')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    showAlert('Les mots de passe ne correspondent pas', 'error')
    return
  }
  if (passwordForm.newPassword.length < 4) {
    showAlert('Le nouveau mot de passe doit contenir au moins 4 caractères', 'error')
    return
  }
  
  isChangingPassword.value = true
  try {
    await api.put('/users/change-password', {
      currentPassword: passwordForm.currentPassword,
      newPassword: passwordForm.newPassword
    })
    showAlert('Mot de passe modifié avec succès', 'success')
    passwordForm.currentPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } catch (error: any) {
    console.error('Erreur changement mot de passe:', error)
    const msg = error.response?.data?.message || 'Erreur lors du changement de mot de passe'
    showAlert(msg, 'error')
  } finally {
    isChangingPassword.value = false
  }
}

async function loadProfilePhoto() {
  try {
    const res = await api.get('/users/photo', { responseType: 'blob' })
    if (res.data && res.data.size > 0) {
      profilePhotoUrl.value = URL.createObjectURL(res.data)
    }
  } catch (error) {
    // Pas de photo de profil
    profilePhotoUrl.value = null
  }
}

onMounted(() => {
  // Initialiser le formulaire d'édition
  if (user.value) {
    editForm.nom = user.value.nom || ''
    editForm.prenom = user.value.prenom || ''
    editForm.email = user.value.email || ''
    editForm.cin = user.value.cin || ''
  }
  
  loadProfilePhoto()
  loadProfileDetails()
  loadSettings()
})
</script>

<style scoped>
.profil-container {
  padding: 20px;
}

.profile-avatar-container {
  position: relative;
  display: inline-block;
}

.profile-user-img {
  width: 128px;
  height: 128px;
  object-fit: cover;
  border: 3px solid #adb5bd;
}

.profile-initials {
  width: 128px;
  height: 128px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  font-weight: bold;
  margin: 0 auto;
}

.upload-btn {
  position: absolute;
  bottom: 5px;
  right: 5px;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #007bff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  border: 2px solid white;
}

.upload-btn:hover {
  background: #0056b3;
  transform: scale(1.1);
}

.box-profile {
  padding: 30px;
}

.profile-username {
  font-size: 1.4rem;
  font-weight: 600;
}

.list-group-item {
  border-left: 0;
  border-right: 0;
}

.modal.show {
  background: rgba(0, 0, 0, 0.5);
}

.alert-toast {
  position: fixed;
  bottom: 20px;
  right: 20px;
  padding: 15px 25px;
  border-radius: 8px;
  color: white;
  display: flex;
  align-items: center;
  gap: 10px;
  z-index: 9999;
  animation: slideIn 0.3s ease;
}

.alert-toast.success {
  background: #28a745;
}

.alert-toast.error {
  background: #dc3545;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(100px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* ================= SETTINGS STYLES ================= */
.settings-section {
  margin-bottom: 20px;
}

.settings-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 2px solid #eee;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;
}

.setting-item:last-child {
  border-bottom: none;
}

.setting-info {
  flex: 1;
}

.setting-label {
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
  display: block;
}

.setting-description {
  font-size: 0.85rem;
  color: #666;
  margin: 0;
}

.setting-control {
  margin-left: 20px;
}

/* Switch Toggle */
.switch {
  position: relative;
  display: inline-block;
  width: 50px;
  height: 26px;
}

.switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #ccc;
  transition: 0.4s;
}

.slider:before {
  position: absolute;
  content: "";
  height: 20px;
  width: 20px;
  left: 3px;
  bottom: 3px;
  background-color: white;
  transition: 0.4s;
}

input:checked + .slider {
  background-color: #007bff;
}

input:checked + .slider:before {
  transform: translateX(24px);
}

.slider.round {
  border-radius: 26px;
}

.slider.round:before {
  border-radius: 50%;
}

/* Color Options */
.color-options {
  display: flex;
  gap: 8px;
}

.color-btn {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 2px solid transparent;
  cursor: pointer;
  transition: all 0.2s;
}

.color-btn:hover {
  transform: scale(1.1);
}

.color-btn.active {
  border-color: #333;
  box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.3);
}
</style>
