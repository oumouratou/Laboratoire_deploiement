<template>
  <div>
    <!-- Header -->
    <section class="content-header">
      <div class="container-fluid">
        <h1><i class="fas fa-calendar-alt mr-2"></i> Mes Réservations</h1>
      </div>
    </section>

    <!-- Content -->
    <section class="content">
      <div class="container-fluid">
        <div class="card">
          <!-- Card Header -->
          <div class="card-header d-flex align-items-center">
            <h3 class="card-title mb-0">Liste de mes réservations</h3>
          </div>

          <!-- Card Body -->
          <div class="card-body table-responsive">
            <table class="table table-bordered table-striped table-hover" v-if="mesReservations.length > 0">
              <thead class="bg-info">
                <tr>
                  <th>Numéro</th>
                  <th>Laboratoire</th>
                  <th>Date</th>
                  <th>Horaire</th>
                  <th>Motif</th>
                  <th>Statut</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                <tr 
                  v-for="(res, index) in mesReservations" 
                  :key="res.id"
                  :class="{ 'highlight-row': highlightedId === res.id }"
                  :ref="el => { if (highlightedId === res.id) highlightedRow = el }"
                >
                  <td>{{ index + 1 }}</td>
                  <td>{{ res.laboratoireNom || res.laboratoire?.nomLabo || res.laboratoire?.nom || 'N/A' }}</td>
                  <td>{{ res.dateReservation }}</td>
                  <td>{{ res.heureDebut }} - {{ res.heureFin }}</td>
                  <td>{{ res.motif }}</td>
                  <td>
                    <span :class="getStatutBadge(res.statut)">
                      {{ formatStatut(res.statut) }}
                    </span>
                  </td>
                  <td class="d-flex gap-1">
                    <button 
                      class="btn btn-warning btn-sm"
                      @click="openModal(res)"
                      :disabled="isReservationTraitee(res)"
                      :title="getReservationActionReason(res)"
                    >
                      <i class="fas fa-edit mr-1"></i> Modifier
                    </button>

                    <button 
                      class="btn btn-danger btn-sm"
                      @click="supprimerReservation(res)"
                      :disabled="isReservationTraitee(res)"
                      :title="getReservationActionReason(res)"
                    >
                      <i class="fas fa-trash mr-1"></i> Supprimer
                    </button>

    
                  </td>
                </tr>
              </tbody>
            </table>

            <div class="text-center text-muted p-4" v-else>
              <i class="fas fa-calendar-times fa-3x mb-3"></i>
              <p>Vous n'avez pas encore de réservation</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Modal de modification -->
    <div class="modal fade" :class="{ show: showModal }" :style="{ display: showModal ? 'block' : 'none' }">
      <div class="modal-dialog">
        <div class="modal-content">
          <form @submit.prevent="handleSubmit">
            <div class="modal-header">
              <h5 class="modal-title">Modifier la réservation</h5>
              <button type="button" class="close" @click="closeModal">×</button>
            </div>

            <div class="modal-body">
              <div class="form-group">
                <label>Date de réservation</label>
                <input type="date" class="form-control" v-model="form.dateReservation" required />
              </div>
              <div class="form-group">
                <label>Heure de début</label>
                <input type="time" class="form-control" v-model="form.heureDebut" required />
              </div>
              <div class="form-group">
                <label>Heure de fin</label>
                <input type="time" class="form-control" v-model="form.heureFin" required />
              </div>
              <div class="form-group">
                <label>Motif</label>
                <input type="text" class="form-control" v-model="form.motif" required />
              </div>
            </div>

            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" @click="closeModal">Annuler</button>
              <button type="submit" class="btn btn-success">Modifier</button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- Backdrop -->
    <div class="modal-backdrop fade show" v-if="showModal"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive, onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import api from '@/Service/api'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const authStore = useAuthStore()
const currentUser = computed(() => authStore.currentUser)

const reservations = ref<any[]>([])
const highlightedId = ref<number | null>(null)
const highlightedRow = ref<any>(null)

// x Modal détails et modal modification
const showModal = ref(false)
const form = reactive({
  id: 0,
  laboratoireId: 0,
  dateReservation: '',
  heureDebut: '',
  heureFin: '',
  motif: ''
})

// x Récupération des réservations
async function fetchReservations() {
  try {
    const response = await api.get('/reservations')
    reservations.value = response.data
    console.log('Réservations récupérées:', reservations.value)
  } catch (error) {
    console.error('Erreur lors du chargement des réservations', error)
  }
}

// x Filtre les réservations pour l'étudiant connecté (triées par date décroissante)
const mesReservations = computed(() => {
  if (!currentUser.value) return []
  const userId = Number(currentUser.value.id)
  return reservations.value
    .filter(res => {
      // Vérifier plusieurs formats possibles de l'API
      const etudiantId = res.etudiantId || res.etudiant?.id || res.utilisateur?.id || res.userId
      return Number(etudiantId) === userId
    })
    .sort((a, b) => {
      const dateA = new Date(a.dateReservation || 0).getTime()
      const dateB = new Date(b.dateReservation || 0).getTime()
      return dateB - dateA // Plus récent en premier
    })
})

// x Ouvre le modal pour modifier
function openModal(res: any) {
  if (isReservationTraitee(res)) {
    alert(getReservationActionReason(res))
    return
  }

  form.id = res.id
  form.laboratoireId = res.laboratoireId
  form.dateReservation = res.dateReservation
  form.heureDebut = res.heureDebut
  form.heureFin = res.heureFin
  form.motif = res.motif

  showModal.value = true
}

function closeModal() {
  showModal.value = false
}

// x Submit modification
async function handleSubmit() {
  try {
    if (form.heureFin <= form.heureDebut) {
      alert("L'heure de fin doit être après l'heure de début")
      return
    }

    await api.put(`/reservations/${form.id}`, {
      laboratoireId: form.laboratoireId,
      dateReservation: form.dateReservation,
      heureDebut: form.heureDebut,
      heureFin: form.heureFin,
      motif: form.motif
    })

    await fetchReservations()
    closeModal()
  } catch (error: any) {
    console.error('Erreur lors de la modification', error)
    alert('Impossible de modifier la réservation: ' + (error.response?.data?.message || error.message))
  }
}

// x Supprimer réservation (uniquement EN_ATTENTE)
async function supprimerReservation(res: any) {
  if (isReservationTraitee(res)) {
    alert(getReservationActionReason(res))
    return
  }

  if (!confirm("Voulez-vous supprimer définitivement cette réservation ?")) return
  try {
    await api.delete(`/reservations/${res.id}`)
    await fetchReservations()
  } catch (error: any) {
    const status = error?.response?.status
    if (status === 404 || status === 405) {
      try {
        await api.put(`/reservations/${res.id}/auto-annuler`)
        await fetchReservations()
        return
      } catch (fallbackError) {
        console.error('Erreur fallback suppression -> auto-annuler', fallbackError)
      }
    }
    console.error('Erreur lors de la suppression', error)
  }
}

function isReservationTraitee(res: any): boolean {
  return res?.statut !== 'EN_ATTENTE'
}

function isReservationEnAttente(res: any): boolean {
  return res?.statut === 'EN_ATTENTE'
}

async function approveReservation(res: any) {
  if (!confirm("Voulez-vous approuver cette réservation ?")) return
  try {
    await api.put(`/reservations/${res.id}/approuver`)
    await fetchReservations()
    alert('Réservation approuvée avec succès')
  } catch (error: any) {
    console.error('Erreur lors de l\'approbation', error)
    alert('Impossible d\'approuver la réservation: ' + (error.response?.data?.message || error.message))
  }
}

async function rejectReservation(res: any) {
  const motif = prompt('Veuillez indiquer un motif de refus:')
  if (!motif) return
  
  try {
    await api.put(`/reservations/${res.id}/refuser`, { motif })
    await fetchReservations()
    alert('Réservation refusée')
  } catch (error: any) {
    console.error('Erreur lors du refus', error)
    alert('Impossible de refuser la réservation: ' + (error.response?.data?.message || error.message))
  }
}

function getReservationActionReason(res: any): string {
  const statut = String(res?.statut || '').toUpperCase()
  if (statut === 'APPROUVEE' || statut === 'CONFIRMEE' || statut === 'RESERVEE') {
    return 'Réservation déjà traitée'
  }
  if (statut === 'ANNULEE') return 'Réservation déjà annulée'
  if (statut === 'REFUSEE') return 'Réservation déjà refusée'
  return statut === 'EN_ATTENTE' ? '' : 'Action indisponible'
}

// x Badges statut
function getStatutBadge(statut: string) {
  const badges: Record<string, string> = {
    RESERVEE: 'badge badge-success',
    EN_ATTENTE: 'badge badge-warning',
    ANNULEE: 'badge badge-secondary',
    CONFIRMEE: 'badge badge-primary',
    APPROUVEE: 'badge badge-success',
    REFUSEE: 'badge badge-danger'
  }
  return badges[statut] || 'badge badge-secondary'
}

function formatStatut(statut: string) {
  const labels: Record<string, string> = {
    RESERVEE: 'Reservée',
    EN_ATTENTE: 'En attente',
    ANNULEE: 'Annulée',
    CONFIRMEE: 'Confirmée',
    APPROUVEE: 'Approuvée',
    REFUSEE: 'Refusée'
  }
  return labels[statut] || statut
}

onMounted(async () => {
  await fetchReservations()
  
  // Vérifier si on doit highlight une réservation (venant d'une notification)
  if (route.query.highlight) {
    highlightedId.value = Number(route.query.highlight)
    
    await nextTick()
    setTimeout(() => {
      if (highlightedRow.value) {
        (highlightedRow.value as HTMLElement).scrollIntoView({ behavior: 'smooth', block: 'center' })
      }
      // Retirer le highlight après 3 secondes
      setTimeout(() => {
        highlightedId.value = null
      }, 3000)
    }, 100)
  }
})
</script>

<style scoped>
.modal.show { background-color: rgba(0,0,0,0.5); }
.badge { padding: 5px 10px; border-radius: 4px; }

/* Actions boutons - flex sans retour à la ligne */
td.d-flex {
  display: flex !important;
  flex-wrap: nowrap;
  gap: 4px;
  min-width: 180px;
}
td.d-flex button {
  white-space: nowrap;
}

/* Table responsive avec scroll horizontal */
.table-responsive {
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
}

.table {
  min-width: 800px;
}

/* Highlight animation pour la ligne venant d'une notification */
.highlight-row {
  animation: highlightPulse 1s ease-in-out 3;
  background-color: #d1ecf1 !important;
}

@keyframes highlightPulse {
  0%, 100% { background-color: #d1ecf1; }
  50% { background-color: #bee5eb; }
}
</style>
