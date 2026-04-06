<template>
  <div class="notification-wrapper">
    <!-- Bouton cloche - toujours cliquable -->
    <button class="action-btn" @click="toggleNotifDropdown" title="Notifications">
      <i class="fas fa-bell"></i>
      <span class="badge" v-if="unreadCount > 0">{{ unreadCount }}</span>
    </button>

    <!-- Dropdown notifications - accessible même sans nouvelles notifications -->
    <div class="notification-dropdown" v-if="showDropdown" :class="themeClass">
      <div class="notification-header">
        <h4>Notifications</h4>
        <span class="notification-count" v-if="unreadCount">{{ unreadCount }} nouvelle(s)</span>
        <span class="notification-count text-muted" v-else>Toutes lues</span>
      </div>

      <div class="notification-list" v-if="unreadNotifications.length > 0">
        <div
          v-for="notif in unreadNotifications"
          :key="notif.id"
          class="notification-item"
          :class="{ 'notification-unread': !notif.read }"
          @click="handleNotificationClick(notif)"
        >
          <div class="notification-icon" :class="getIconClass(notif)">
            <i :class="getIcon(notif)"></i>
          </div>
          <div class="notification-content">
            <p class="notification-message">{{ notif.message }}</p>
            <!-- Bouton pour voir le motif de refus -->
            <button 
              v-if="notif.motifRefus && (notif.message?.includes('refusée') || notif.message?.includes('rejetée'))" 
              class="btn btn-sm btn-link text-danger p-0"
              @click.stop="showMotifModal(notif)"
            >
              <i class="fas fa-eye mr-1"></i> Voir le motif de refus
            </button>
            <span class="notification-labo" v-if="notif.laboName || notif.laboratoireNom">
              <i class="fas fa-flask"></i> {{ notif.laboName || notif.laboratoireNom }}
            </span>
            <span class="notification-time">{{ formatDate(notif.createdAt) }}</span>
          </div>
        </div>
      </div>

      <div v-else class="notification-empty">
        <i class="fas fa-bell-slash"></i>
        <p>Aucune notification</p>
      </div>
    </div>

    <!-- Modal Motif de Refus -->
    <div class="modal fade" :class="{ show: showMotifRefusModal }" :style="{ display: showMotifRefusModal ? 'block' : 'none' }" @click.self="closeMotifModal">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header bg-danger text-white">
            <h5 class="modal-title">
              <i class="fas fa-times-circle mr-2"></i> Motif de refus
            </h5>
            <button type="button" class="close text-white" @click="closeMotifModal">&times;</button>
          </div>
          <div class="modal-body">
            <div class="alert alert-warning mb-3">
              <i class="fas fa-exclamation-triangle mr-2"></i>
              {{ selectedNotifForMotif?.message }}
            </div>
            <div class="motif-content p-3 bg-light rounded">
              <strong><i class="fas fa-comment-alt mr-2"></i>Motif :</strong>
              <p class="mt-2 mb-0">{{ selectedNotifForMotif?.motifRefus || 'Aucun motif spécifié' }}</p>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="closeMotifModal">Fermer</button>
            <button type="button" class="btn btn-primary" @click="goToElement(selectedNotifForMotif)">
              <i class="fas fa-arrow-right mr-1"></i> Voir les détails
            </button>
          </div>
        </div>
      </div>
    </div>
    <div class="modal-backdrop fade show" v-if="showMotifRefusModal" @click="closeMotifModal"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { 
  getNotificationsTechnicien,
  getNotificationsEtudiant,
  getNotificationsEnseignant,
  markNotificationAsRead,
  type NotificationDTO
} from '@/Service/NotificationService'
import { getReservationsByMonDepartement } from '@/Service/ReservationService'
import { getMesLabos } from '@/Service/LaboratoireService'

// Props rôle utilisateur
const props = defineProps<{
  role: 'TECHNICIEN' | 'ETUDIANT' | 'ENSEIGNANT'
}>()

const router = useRouter()
const authStore = useAuthStore()
const notifications = ref<NotificationDTO[]>([])
const showDropdown = ref(false)
const showMotifRefusModal = ref(false)
const selectedNotifForMotif = ref<NotificationDTO | null>(null)
let notificationInterval: number | null = null

// Nombre de notifications non lues
const unreadCount = computed(() => notifications.value.filter(n => !n.read).length)

// Notifications non lues uniquement (pour la cloche)
const unreadNotifications = computed(() => notifications.value.filter(n => !n.read))

function normalizeText(value: any): string {
  return String(value || '').trim().toLowerCase()
}

async function getChefDepartementReservationScope(): Promise<{ reservationIds: Set<number>; laboNames: Set<string> }> {
  const reservationIds = new Set<number>()
  const laboNames = new Set<string>()

  try {
    const reservationsRes = await getReservationsByMonDepartement()
    const rows = Array.isArray(reservationsRes?.data) ? reservationsRes.data : []
    for (const row of rows) {
      const id = Number((row as any)?.id)
      if (Number.isFinite(id) && id > 0) reservationIds.add(id)
      const nomLabo = normalizeText((row as any)?.laboratoireNom || (row as any)?.laboratoire?.nomLabo || (row as any)?.laboratoire?.nom)
      if (nomLabo) laboNames.add(nomLabo)
    }
  } catch (error) {
    console.warn('Impossible de charger les réservations du département du chef:', error)
  }

  try {
    const labosRes = await getMesLabos()
    const labos = Array.isArray(labosRes?.data) ? labosRes.data : []
    for (const labo of labos) {
      const nomLabo = normalizeText((labo as any)?.nomLabo || (labo as any)?.nom)
      if (nomLabo) laboNames.add(nomLabo)
    }
  } catch (error) {
    console.warn('Impossible de charger les labos du chef:', error)
  }

  return { reservationIds, laboNames }
}

function filterReservationNotificationsForChef(
  rows: NotificationDTO[],
  scope: { reservationIds: Set<number>; laboNames: Set<string> },
): NotificationDTO[] {
  return rows.filter((notif) => {
    const type = String(notif?.type || '').toUpperCase()
    if (type !== 'RESERVATION') return true

    const reservationId = Number(notif?.reservationId)
    if (Number.isFinite(reservationId) && reservationId > 0 && scope.reservationIds.has(reservationId)) {
      return true
    }

    const laboNotif = normalizeText(notif?.laboName || notif?.laboratoireNom)
    if (laboNotif && scope.laboNames.has(laboNotif)) {
      return true
    }

    return false
  })
}

// Classe thème selon rôle
const themeClass = computed(() => {
  switch (props.role) {
    case 'TECHNICIEN': return 'theme-teal'
    case 'ETUDIANT': return 'theme-blue'
    case 'ENSEIGNANT': return 'theme-orange'
    default: return 'theme-teal'
  }
})

// Toggle dropdown
function toggleNotifDropdown() {
  showDropdown.value = !showDropdown.value
}

// Ouvrir le modal de motif de refus
function showMotifModal(notif: NotificationDTO) {
  selectedNotifForMotif.value = notif
  showMotifRefusModal.value = true
  showDropdown.value = false
}

// Fermer le modal de motif
function closeMotifModal() {
  showMotifRefusModal.value = false
  selectedNotifForMotif.value = null
}

// Aller vers l'élément concerné
async function goToElement(notif: NotificationDTO | null) {
  if (!notif) return
  closeMotifModal()
  await handleNotificationClick(notif)
}

// Récupération notifications (REST)
async function fetchNotifications() {
  try {
    let res
    switch (props.role) {
      case 'TECHNICIEN':
        res = await getNotificationsTechnicien()
        break
      case 'ETUDIANT':
        res = await getNotificationsEtudiant()
        break
      case 'ENSEIGNANT':
        res = await getNotificationsEnseignant()
        break
      default:
        res = await getNotificationsTechnicien()
    }
    notifications.value = Array.isArray(res.data) ? res.data : []

    if (props.role === 'ENSEIGNANT' && authStore.isChefDepartement) {
      const scope = await getChefDepartementReservationScope()
      notifications.value = filterReservationNotificationsForChef(notifications.value, scope)
    }
  } catch (err: any) {
    console.error('Erreur notifications:', err)
    notifications.value = []
  }
}

// Click sur notification - redirige vers l'élément spécifique
async function handleNotificationClick(notif: NotificationDTO) {
  try {
    await markNotificationAsRead(notif.id)
    // Marquer comme lu localement (disparaît de la cloche via unreadNotifications)
    const found = notifications.value.find(n => n.id === notif.id)
    if (found) found.read = true
    showDropdown.value = false

    // Redirection selon type avec l'ID de l'élément pour pointer dessus
    const basePath = `/${props.role.toLowerCase()}`
    if (notif.type === 'RESERVATION') {
      // Rediriger avec l'ID de la réservation en query param
      router.push({
        path: `${basePath}/reservations`,
        query: { highlight: notif.reservationId?.toString() }
      })
    } else if (notif.type === 'RECLAMATION') {
      // Rediriger avec l'ID de la réclamation en query param
      router.push({
        path: `${basePath}/reclamations`,
        query: { highlight: notif.reclamationId?.toString() }
      })
    }
  } catch (err) {
    console.error('Erreur traitement notification:', err)
  }
}

// Icône selon type / statut
function getIcon(notif: NotificationDTO): string {
  if (props.role === 'ETUDIANT' || props.role === 'ENSEIGNANT') {
    if (notif.message?.includes('approuvée') || notif.message?.includes('traitée') || notif.message?.includes('résolue')) 
      return 'fas fa-check-circle'
    if (notif.message?.includes('refusée') || notif.message?.includes('rejetée')) 
      return 'fas fa-times-circle'
  }
  return notif.type === 'RESERVATION' ? 'fas fa-calendar-check' : 'fas fa-exclamation-triangle'
}

// Classe icône
function getIconClass(notif: NotificationDTO): string {
  if (props.role === 'ETUDIANT' || props.role === 'ENSEIGNANT') {
    if (notif.message?.includes('approuvée') || notif.message?.includes('traitée') || notif.message?.includes('résolue')) 
      return 'success'
    if (notif.message?.includes('refusée') || notif.message?.includes('rejetée')) 
      return 'danger'
    return 'info'
  }
  return notif.type === 'RESERVATION' ? 'reservation' : 'reclamation'
}

// Format date
function formatDate(dateStr: string): string {
  if (!dateStr) return ''
  const now = new Date()
  const notifDate = new Date(dateStr)
  const diffMs = now.getTime() - notifDate.getTime()
  const diffMins = Math.floor(diffMs / 60000)
  const diffHours = Math.floor(diffMs / 3600000)
  const diffDays = Math.floor(diffMs / 86400000)
  if (diffMins < 1) return 'À l\'instant'
  if (diffMins < 60) return `Il y a ${diffMins} min`
  if (diffHours < 24) return `Il y a ${diffHours}h`
  if (diffDays === 1) return 'Hier'
  return notifDate.toLocaleDateString('fr-FR', { day:'2-digit', month:'2-digit', hour:'2-digit', minute:'2-digit' })
}

// Mounted
onMounted(() => {
  fetchNotifications()

  // Polling REST toutes les 15 secondes
  notificationInterval = window.setInterval(fetchNotifications, 15000)
})

// Unmounted
onUnmounted(() => {
  if (notificationInterval) clearInterval(notificationInterval)
})
</script>

<style scoped>
.notification-wrapper {
  position: relative;
}

.action-btn {
  position: relative;
  width: 40px;
  height: 40px;
  border-radius: 10px;
  border: none;
  background: #f1f5f9;
  color: #64748b;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.action-btn:hover {
  background: #e2e8f0;
  color: #475569;
}

.action-btn .badge {
  position: absolute;
  top: -4px;
  right: -4px;
  background: #ef4444;
  color: #fff;
  font-size: 10px;
  font-weight: 600;
  padding: 2px 6px;
  border-radius: 10px;
  min-width: 18px;
  text-align: center;
}

.notification-dropdown {
  position: absolute;
  top: 50px;
  right: 0;
  width: 360px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(0,0,0,0.15);
  z-index: 1000;
  overflow: hidden;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f1f5f9;
}

.notification-header h4 {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.notification-count {
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 20px;
  font-weight: 600;
}

.theme-teal .notification-count { background: #d1fae5; color: #059669; }
.theme-blue .notification-count { background: #dbeafe; color: #2563eb; }
.theme-orange .notification-count { background: #fef3c7; color: #d97706; }

.notification-list {
  max-height: 400px;
  overflow-y: auto;
}

.notification-item {
  display: flex;
  gap: 14px;
  padding: 14px 20px;
  cursor: pointer;
  transition: background 0.2s;
  border-bottom: 1px solid #f8fafc;
}

.notification-item:hover {
  background: #f8fafc;
}

.notification-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  flex-shrink: 0;
}

.notification-icon.reservation { background: #dbeafe; color: #2563eb; }
.notification-icon.reclamation { background: #fef3c7; color: #d97706; }
.notification-icon.success { background: #d1fae5; color: #059669; }
.notification-icon.danger { background: #fee2e2; color: #dc2626; }
.notification-icon.info { background: #e0e7ff; color: #4f46e5; }

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-message {
  font-size: 14px;
  color: #1e293b;
  margin: 0 0 4px;
  line-height: 1.4;
}

.notification-labo {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #64748b;
  margin-bottom: 4px;
}

.notification-labo i {
  font-size: 10px;
}

.notification-motif {
  background: #fef3c7;
  border-left: 3px solid #f59e0b;
  padding: 6px 10px;
  margin: 6px 0;
  border-radius: 4px;
  font-size: 12px;
  color: #92400e;
}

.notification-motif.refus {
  background: #fee2e2;
  border-left-color: #dc2626;
  color: #991b1b;
}

.notification-time {
  font-size: 11px;
  color: #94a3b8;
}

.notification-empty {
  padding: 40px 20px;
  text-align: center;
  color: #94a3b8;
}

.notification-empty i {
  font-size: 32px;
  margin-bottom: 10px;
  opacity: 0.5;
}

.notification-empty p {
  margin: 0;
  font-size: 14px;
}

/* Notification non lue */
.notification-unread {
  background-color: #f0f9ff;
  border-left: 3px solid #3b82f6;
}

/* Modal styles */
.modal.show {
  background: rgba(0, 0, 0, 0.5);
}

.motif-content {
  border-left: 4px solid #dc2626;
}

/* Thèmes */
.theme-teal .notification-header { border-bottom-color: #d1fae5; }
.theme-blue .notification-header { border-bottom-color: #dbeafe; }
.theme-orange .notification-header { border-bottom-color: #fef3c7; }
</style>
