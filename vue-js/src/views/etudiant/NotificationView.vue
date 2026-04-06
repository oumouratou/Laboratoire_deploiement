<template>
  <div>
    <section class="content-header">
      <div class="container-fluid">
        <h1><i class="fas fa-bell mr-2"></i> Mes Notifications</h1>
      </div>
    </section>

    <section class="content">
      <div class="container-fluid">
        <!-- Onglets pour filtrer -->
        <div class="tabs-container mb-3">
          <button 
            class="btn mr-2" 
            :class="activeFilter === 'all' ? 'btn-primary' : 'btn-outline-primary'"
            @click="activeFilter = 'all'"
          >
            <i class="fas fa-list mr-1"></i> Toutes ({{ notifications.length }})
          </button>
          <button 
            class="btn mr-2" 
            :class="activeFilter === 'RESERVATION' ? 'btn-success' : 'btn-outline-success'"
            @click="activeFilter = 'RESERVATION'"
          >
            <i class="fas fa-calendar-check mr-1"></i> Réservations
          </button>
          <button 
            class="btn" 
            :class="activeFilter === 'RECLAMATION' ? 'btn-warning' : 'btn-outline-warning'"
            @click="activeFilter = 'RECLAMATION'"
          >
            <i class="fas fa-exclamation-triangle mr-1"></i> Réclamations
          </button>
        </div>

        <div class="card">
          <div class="card-header d-flex justify-content-between align-items-center">
            <h3 class="card-title">Liste des notifications</h3>
            <div>
              <button class="btn btn-sm btn-secondary mr-2" @click="markAllAsRead" v-if="unreadCount > 0">
                <i class="fas fa-check-double mr-1"></i> Tout marquer lu
              </button>
              <button class="btn btn-sm btn-primary" @click="fetchNotifications">
                <i class="fas fa-sync-alt mr-1"></i> Rafraîchir
              </button>
            </div>
          </div>

          <div class="card-body table-responsive">
            <table class="table table-hover">
              <thead>
                <tr>
                  <th>Type</th>
                  <th>Message</th>
                  <th>Motif refus</th>
                  <th>Date</th>
                  <th>Statut</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="notif in filteredNotifications" :key="notif.id" :class="{ 'table-light': notif.read }">
                  <td>
                    <span :class="typeBadge(notif.type)">
                      <i :class="typeIcon(notif.type)" class="mr-1"></i>
                      {{ notif.type === 'RESERVATION' ? 'Réservation' : 'Réclamation' }}
                    </span>
                  </td>
                  <td>
                    <div class="message-cell">{{ cleanMessage(notif.message) }}</div>
                  </td>
                  <td>
                    <span v-if="notif.motifRefus" class="text-danger">
                      <i class="fas fa-times-circle mr-1"></i>
                      {{ notif.motifRefus }}
                    </span>
                    <span v-else class="text-muted">—</span>
                  </td>
                  <td>{{ formatDate(notif.createdAt) }}</td>
                  <td>
                    <span :class="notif.read ? 'badge badge-secondary' : 'badge badge-primary'">
                      {{ notif.read ? 'Lu' : 'Non lu' }}
                    </span>
                  </td>
                  <td>
                    <button 
                      class="btn btn-info btn-sm"
                      @click="openNotification(notif)"
                    >
                      <i class="fas fa-eye mr-1"></i> Voir
                    </button>
                  </td>
                </tr>

                <tr v-if="filteredNotifications.length === 0">
                  <td colspan="6" class="text-center text-muted py-4">
                    <i class="fas fa-bell-slash fa-2x mb-2"></i>
                    <p class="mb-0">Aucune notification</p>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </section>

    <!-- Modal Détails Notification -->
    <div class="modal fade" :class="{ show: showDetailModal }" :style="{ display: showDetailModal ? 'block' : 'none' }">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header" :class="selectedNotif?.type === 'RESERVATION' ? 'bg-success text-white' : 'bg-warning'">
            <h5 class="modal-title">
              <i :class="selectedNotif?.type === 'RESERVATION' ? 'fas fa-calendar-check' : 'fas fa-exclamation-triangle'" class="mr-2"></i>
              Détails - {{ selectedNotif?.type === 'RESERVATION' ? 'Réservation' : 'Réclamation' }}
            </h5>
            <button type="button" class="close" @click="showDetailModal = false">&times;</button>
          </div>
          <div class="modal-body" v-if="selectedNotif">
            <dl class="row mb-0">
              <dt class="col-sm-4">Type :</dt>
              <dd class="col-sm-8">
                <span :class="typeBadge(selectedNotif.type)">
                  {{ selectedNotif.type === 'RESERVATION' ? 'Réservation' : 'Réclamation' }}
                </span>
              </dd>

              <dt class="col-sm-4">Message :</dt>
              <dd class="col-sm-8">{{ selectedNotif.message }}</dd>

              <dt class="col-sm-4" v-if="selectedNotif.laboratoireNom || selectedNotif.laboName">Laboratoire :</dt>
              <dd class="col-sm-8" v-if="selectedNotif.laboratoireNom || selectedNotif.laboName">{{ selectedNotif.laboratoireNom || selectedNotif.laboName }}</dd>

              <dt class="col-sm-4" v-if="selectedNotif.motifRefus">Motif de refus :</dt>
              <dd class="col-sm-8 text-danger" v-if="selectedNotif.motifRefus">
                <i class="fas fa-times-circle mr-1"></i> {{ selectedNotif.motifRefus }}
              </dd>

              <dt class="col-sm-4">Date :</dt>
              <dd class="col-sm-8">{{ formatDate(selectedNotif.createdAt) }}</dd>

              <dt class="col-sm-4">Statut :</dt>
              <dd class="col-sm-8">
                <span :class="selectedNotif.read ? 'badge badge-secondary' : 'badge badge-primary'">
                  {{ selectedNotif.read ? 'Lu' : 'Non lu' }}
                </span>
              </dd>
            </dl>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="showDetailModal = false">Fermer</button>
          </div>
        </div>
      </div>
    </div>
    <div class="modal-backdrop fade show" v-if="showDetailModal"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getNotificationsEtudiant, markNotificationAsRead } from '@/Service/NotificationService'
import api from '@/Service/api'

interface NotificationDTO {
  id: number
  message: string
  read: boolean
  createdAt: string
  reservationId: number | null
  reclamationId?: number | null
  laboName?: string
  laboratoireNom?: string
  type: string
  motifRefus?: string
}

const router = useRouter()
const notifications = ref<NotificationDTO[]>([])
const activeFilter = ref<'all' | 'RESERVATION' | 'RECLAMATION'>('all')
const showDetailModal = ref(false)
const selectedNotif = ref<NotificationDTO | null>(null)

const filteredNotifications = computed(() => {
  if (activeFilter.value === 'all') return notifications.value
  return notifications.value.filter(n => n.type === activeFilter.value)
})

const unreadCount = computed(() => notifications.value.filter(n => !n.read).length)

async function fetchNotifications() {
  try {
    console.log("Chargement des notifications étudiant...")
    const res = await getNotificationsEtudiant()
    
    if (res && res.data) {
      notifications.value = Array.isArray(res.data) ? res.data : []
    } else if (Array.isArray(res)) {
      notifications.value = res
    } else {
      notifications.value = []
    }
    
    // Trier par date décroissante
    notifications.value.sort((a, b) => 
      new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
    )
    
    console.log("Notifications étudiant chargées:", notifications.value.length)
  } catch (err) {
    console.error('Erreur chargement notifications', err)
    notifications.value = []
  }
}

async function openNotification(notif: NotificationDTO) {
  if (!notif.read) {
    await markNotificationAsRead(notif.id)
    notif.read = true
  }

  selectedNotif.value = notif
  showDetailModal.value = true
}

async function markAllAsRead() {
  try {
    await api.put('/notifications/read-all')
    notifications.value.forEach(n => n.read = true)
  } catch (err) {
    console.error('Erreur marquer tout lu', err)
  }
}

function typeBadge(type: string) {
  switch (type) {
    case 'RESERVATION': return 'badge badge-success'
    case 'RECLAMATION': return 'badge badge-warning'
    default: return 'badge badge-secondary'
  }
}

function cleanMessage(message: string) {
  if (!message) return ''
  return message
    .replace(/#\d+/g, '')
    .replace(/\b(id|ID)\s*[:=]?\s*\d+/gi, '')
    .replace(/\(\s*\)/g, '')
    .replace(/\s{2,}/g, ' ')
    .trim()
}

function typeIcon(type: string) {
  switch (type) {
    case 'RESERVATION': return 'fas fa-calendar-check'
    case 'RECLAMATION': return 'fas fa-exclamation-triangle'
    default: return 'fas fa-bell'
  }
}

function formatDate(dateStr: string) {
  if (!dateStr) return '—'
  return new Date(dateStr).toLocaleDateString('fr-FR', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(fetchNotifications)
</script>

<style scoped>
.tabs-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.message-cell {
  max-width: 300px;
  word-wrap: break-word;
}

.table-light {
  opacity: 0.7;
}

.modal.show { display: block; background: rgba(0,0,0,0.5); }
</style>
