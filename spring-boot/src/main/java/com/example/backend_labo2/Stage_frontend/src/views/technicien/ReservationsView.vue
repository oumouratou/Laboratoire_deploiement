<template>
  <div>
    <!-- En-tête -->
    <section class="content-header">
      <div class="container-fluid">
        <div class="row mb-2">
          <div class="col-sm-6">
            <h1><i class="fas fa-calendar-check mr-2"></i> Gestion des Réservations</h1>
            <small class="text-muted">Gérer les demandes de réservation de laboratoires</small>
            <!-- Badge de filtre actif -->
            <div v-if="filterLaboId" class="mt-2">
              <span class="badge badge-info p-2">
                <i class="fas fa-filter mr-1"></i> Filtrées par : {{ filterLaboNom }}
                <button class="btn btn-sm btn-light ml-2" @click="clearFilter" style="padding: 0 5px;">
                  <i class="fas fa-times"></i>
                </button>
              </span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="content">
      <div class="container-fluid">

        <!-- Stats -->
        <div class="row mb-3">
          <div class="col-lg-3 col-6">
            <div class="small-box bg-warning">
              <div class="inner">
                <h3>{{ pendingCount }}</h3>
                <p>En attente</p>
              </div>
              <div class="icon"><i class="fas fa-clock"></i></div>
            </div>
          </div>

          <div class="col-lg-3 col-6">
            <div class="small-box bg-success">
              <div class="inner">
                <h3>{{ approvedCount }}</h3>
                <p>Approuvées</p>
              </div>
              <div class="icon"><i class="fas fa-check"></i></div>
            </div>
          </div>

          <div class="col-lg-3 col-6">
            <div class="small-box bg-danger">
              <div class="inner">
                <h3>{{ rejectedCount }}</h3>
                <p>Refusées</p>
              </div>
              <div class="icon"><i class="fas fa-times"></i></div>
            </div>
          </div>

          <div class="col-lg-3 col-6">
            <div class="small-box bg-info">
              <div class="inner">
                <h3>{{ reservations.length }}</h3>
                <p>Total</p>
              </div>
              <div class="icon"><i class="fas fa-calendar-alt"></i></div>
            </div>
          </div>
        </div>

        <!-- Tableau -->
        <div class="card card-outline card-primary">
          <div class="card-header">
            <h3 class="card-title">
              <i class="fas fa-list mr-1"></i> Liste des réservations
            </h3>
          </div>

          <div class="card-body p-0" style="overflow-x: auto;">
            <table class="table table-hover" style="min-width: 1200px;">
              <thead class="thead-light">
                <tr>
                  <th style="width: 50px;">Numéro</th>
                  <th style="width: 120px;">Demandeur</th>
                  <th style="width: 80px;">CIN</th>
                  <th style="width: 70px;">Rôle</th>
                  <th style="width: 100px;">Laboratoire</th>
                  <th style="width: 150px;">Motif</th>
                  <th style="width: 90px;">Date</th>
                  <th style="width: 100px;">Créneau</th>
                  <th style="width: 80px;">Statut</th>
                  <th style="width: 160px;" class="text-center">Actions</th>
                </tr>
              </thead>

              <tbody>
                <tr 
                  v-for="(reservation, index) in reservationsFiltrees" 
                  :key="reservation.id"
                  :class="{ 'highlight-row': highlightedId === reservation.id }"
                  :ref="el => { if (highlightedId === reservation.id) highlightedRow = el }"
                >
                  <td><strong>{{ index + 1 }}</strong></td>

                  <!-- Nom + Prénom -->
                  <td>
                    <strong>{{ getDemandeurNom(reservation) }}</strong>
                  </td>

                  <!-- CIN -->
                  <td>{{ getDemandeurCin(reservation) }}</td>

                  <!-- Rôle -->
                  <td>
                    <span :class="getRoleBadge(getDemandeurRole(reservation))">
                      {{ formatRole(getDemandeurRole(reservation)) }}
                    </span>
                  </td>

                  <!-- Nom du laboratoire -->
                  <td>
                    {{ reservation.laboratoireNom || 'Laboratoire non défini' }}
                  </td>

                  <!-- Description/Motif -->
                  <td>{{ truncateText(reservation.motif || reservation.description || '', 40) }}</td>

                  <td>{{ formatDate(reservation.dateReservation) }}</td>
                  <td>{{ reservation.heureDebut }} - {{ reservation.heureFin }}</td>

                  <td>
                    <span class="badge" :class="getStatusBadge(reservation.statut)">
                      <i :class="getStatusIcon(reservation.statut)" class="mr-1"></i>
                      {{ formatStatus(reservation.statut) }}
                    </span>
                  </td>

                  <!-- Boutons avec texte -->
                  <td class="text-center">
                    <div v-if="reservation.statut === 'EN_ATTENTE'" class="d-flex justify-content-center gap-2" style="gap: 8px;">
                      <button class="btn btn-success btn-sm" @click="handleApproveReservation(reservation.id)" style="white-space: nowrap;">
                        <i class="fas fa-check mr-1"></i> Approuver
                      </button>
                      <button class="btn btn-danger btn-sm" @click="openRejectModal(reservation)" style="white-space: nowrap;">
                        <i class="fas fa-times mr-1"></i> Refuser
                      </button>
                    </div>
                    <span v-else class="text-muted">Traitée</span>
                  </td>
                </tr>

                <tr v-if="reservationsFiltrees.length === 0">
                  <td colspan="10" class="text-center text-muted py-4">
                    <i class="fas fa-calendar-times fa-2x mb-2"></i>
                    <div>Aucune réservation trouvée</div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <div class="card-footer text-right">
            <strong>{{ reservationsFiltrees.length }}</strong> réservation(s)
            <span v-if="filterLaboId"> (filtrées)</span>
          </div>
        </div>

      </div>
    </section>

    <!-- Modal de refus avec motif -->
    <div class="modal fade" :class="{ show: showRejectModal }" :style="{ display: showRejectModal ? 'block' : 'none' }">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header bg-danger text-white">
            <h5 class="modal-title">
              <i class="fas fa-times-circle mr-2"></i>Refuser la réservation
            </h5>
            <button type="button" class="close text-white" @click="closeRejectModal">&times;</button>
          </div>
          <div class="modal-body">
            <div class="alert alert-warning">
              <i class="fas fa-exclamation-triangle mr-2"></i>
              Vous êtes sur le point de refuser la réservation de 
              <strong>{{ selectedReservation?.etudiantPrenom }} {{ selectedReservation?.etudiantNom }}</strong>
              pour le laboratoire <strong>{{ selectedReservation?.laboratoireNom }}</strong>.
            </div>
            <div class="form-group">
              <label><strong>Motif du refus <span class="text-danger">*</span></strong></label>
              <textarea 
                class="form-control" 
                v-model="rejectMotif" 
                rows="4" 
                placeholder="Veuillez expliquer la raison du refus (ex: laboratoire non disponible, conflit d'horaire, etc.)"
                required
              ></textarea>
              <small class="text-muted">Ce motif sera communiqué à l'étudiant dans sa notification.</small>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="closeRejectModal">
              <i class="fas fa-arrow-left mr-1"></i> Annuler
            </button>
            <button 
              type="button" 
              class="btn btn-danger" 
              @click="confirmReject"
              :disabled="!rejectMotif.trim()"
            >
              <i class="fas fa-times mr-1"></i> Confirmer le refus
            </button>
          </div>
        </div>
      </div>
    </div>
    <div class="modal-backdrop fade show" v-if="showRejectModal"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { getReservations, approveReservation, rejectReservation } from '@/Service/ReservationService'

interface Reservation {
  id: number
  dateReservation: string
  heureDebut: string
  heureFin: string
  statut: string
  etudiantNom?: string
  etudiantPrenom?: string
  etudiantCin?: string
  etudiantRole?: string
  cin?: string
  role?: string
  nomDemandeur?: string
  prenomDemandeur?: string
  cinDemandeur?: string
  roleDemandeur?: string
  laboratoireNom?: string
  laboratoireId?: number
  motif?: string
  description?: string
  etudiant?: {
    nom?: string
    prenom?: string
    cin?: string
    role?: string
  }
  demandeur?: {
    nom?: string
    prenom?: string
    cin?: string
    role?: string
  }
}

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const reservations = ref<Reservation[]>([])
const highlightedId = ref<number | null>(null)
const highlightedRow = ref<any>(null)

// Modal de refus
const showRejectModal = ref(false)
const selectedReservation = ref<Reservation | null>(null)
const rejectMotif = ref('')

// Filtres depuis les query params
const filterLaboId = computed(() => route.query.laboratoireId ? Number(route.query.laboratoireId) : null)
const filterLaboNom = computed(() => route.query.laboratoireNom as string || '')

// Réservations filtrées et triées par date décroissante
const reservationsFiltrees = computed(() => {
  let result = reservations.value
  if (filterLaboId.value) {
    result = result.filter(r => r.laboratoireId === filterLaboId.value)
  }
  // Trier par date décroissante (plus récent en premier)
  return result.sort((a, b) => {
    const dateA = new Date(a.dateReservation || 0).getTime()
    const dateB = new Date(b.dateReservation || 0).getTime()
    return dateB - dateA
  })
})

// Stats basées sur les réservations filtrées
const pendingCount = computed(() => reservationsFiltrees.value.filter(r => r.statut === 'EN_ATTENTE').length)
const approvedCount = computed(() => reservationsFiltrees.value.filter(r => r.statut === 'APPROUVEE').length)
const rejectedCount = computed(() => reservationsFiltrees.value.filter(r => r.statut === 'REFUSEE').length)

// Effacer le filtre
function clearFilter() {
  router.replace({ path: '/technicien/reservations' })
}

// Fonctions d'extraction des données
function getDemandeurNom(res: Reservation): string {
  const prenom = res.etudiantPrenom || res.prenomDemandeur || res.etudiant?.prenom || res.demandeur?.prenom || ''
  const nom = res.etudiantNom || res.nomDemandeur || res.etudiant?.nom || res.demandeur?.nom || ''
  return prenom || nom ? `${prenom} ${nom}`.trim() : 'N/A'
}

function getDemandeurCin(res: Reservation): string {
  return res.etudiantCin || res.cin || res.cinDemandeur || res.etudiant?.cin || res.demandeur?.cin || 'N/A'
}

function getDemandeurRole(res: Reservation): string {
  return res.etudiantRole || res.role || res.roleDemandeur || res.etudiant?.role || res.demandeur?.role || ''
}

function formatDate(dateStr: string) {
  return new Date(dateStr).toLocaleDateString('fr-FR')
}

function formatStatus(status: string) {
  return { EN_ATTENTE: 'En attente', APPROUVEE: 'Approuvée', REFUSEE: 'Refusée' }[status] || status
}

function getStatusIcon(status: string) {
  return { EN_ATTENTE: 'fas fa-clock', APPROUVEE: 'fas fa-check-circle', REFUSEE: 'fas fa-times-circle' }[status]
}

function getStatusBadge(status: string) {
  return { EN_ATTENTE: 'badge-warning', APPROUVEE: 'badge-success', REFUSEE: 'badge-danger' }[status] || 'badge-secondary'
}

function getRoleBadge(role: string) {
  return { ETUDIANT: 'badge badge-info', ENSEIGNANT: 'badge badge-primary' }[role] || 'badge badge-secondary'
}

function formatRole(role: string) {
  return { ETUDIANT: 'Étudiant', ENSEIGNANT: 'Enseignant' }[role] || role || 'N/A'
}

function truncateText(text: string, maxLength: number) {
  if (!text) return 'N/A'
  return text.length > maxLength ? text.substring(0, maxLength) + '...' : text
}

async function fetchReservations() {
  try {
    const res = await getReservations()
    reservations.value = res.data || []
    console.log("Réservations chargées:", reservations.value)
  } catch (err) {
    console.error('Erreur chargement reservations:', err)
    reservations.value = []
  }
}

async function handleApproveReservation(id: number) {
  try {
    await approveReservation(id)
    fetchReservations()
  } catch (err) {
    console.error('Erreur approbation:', err)
    alert('Erreur lors de l\'approbation')
  }
}

function openRejectModal(reservation: Reservation) {
  selectedReservation.value = reservation
  rejectMotif.value = ''
  showRejectModal.value = true
}

function closeRejectModal() {
  showRejectModal.value = false
  selectedReservation.value = null
  rejectMotif.value = ''
}

async function confirmReject() {
  if (!selectedReservation.value || !rejectMotif.value.trim()) return
  
  try {
    await rejectReservation(selectedReservation.value.id, rejectMotif.value)
    closeRejectModal()
    fetchReservations()
  } catch (err) {
    console.error('Erreur refus:', err)
    alert('Erreur lors du refus')
  }
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
.small-box {
  border-radius: 10px;
}

.small-box .icon {
  top: 10px;
  right: 15px;
  font-size: 45px;
  opacity: 0.3;
}

.card-title {
  font-weight: 600;
}

/* Highlight animation pour la ligne venant d'une notification */
.highlight-row {
  animation: highlightPulse 1s ease-in-out 3;
  background-color: #d1fae5 !important;
}

@keyframes highlightPulse {
  0%, 100% { background-color: #d1fae5; }
  50% { background-color: #a7f3d0; }
}
</style>
