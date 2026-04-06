<template>
  <div>
    <!-- Accès refusé -->
    <div v-if="!isChefDepartement" class="content">
      <div class="container-fluid">
        <div class="alert alert-warning text-center mt-4">
          <i class="fas fa-lock fa-2x mb-2"></i>
          <h4>Accès réservé</h4>
          <p>Seuls les chefs de département peuvent gérer les réservations.</p>
          <router-link to="/enseignant" class="btn btn-primary mt-2">
            <i class="fas fa-arrow-left mr-1"></i> Retour au tableau de bord
          </router-link>
        </div>
      </div>
    </div>

    <!-- Contenu principal (chef de département uniquement) -->
    <div v-else>
    <!-- En-tête -->
    <section class="content-header">
      <div class="container-fluid">
        <div class="row mb-2">
          <div class="col-sm-6">
            <h1><i class="fas fa-calendar-check mr-2"></i> Gestion des Réservations</h1>
            <small class="text-muted">Gérer les demandes de réservation de laboratoires (Chef de département)</small>
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
                  <th style="width: 90px;" class="text-center">Détails</th>
                  <th style="width: 120px;">Demandeur</th>
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

                  <td class="text-center">
                    <button class="btn btn-info btn-sm" @click="openDetailsModal(reservation)">
                      <i class="fas fa-info-circle mr-1"></i> Détails
                    </button>
                  </td>

                  <!-- Nom + Prénom -->
                  <td>
                    <strong>{{ getDemandeurNom(reservation) }}</strong>
                  </td>

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
              <small class="text-muted">Ce motif sera communiqué au demandeur dans sa notification.</small>
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

    <!-- Modal détails étudiant -->
    <div class="modal fade" :class="{ show: showDetailsModal }" :style="{ display: showDetailsModal ? 'block' : 'none' }">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header bg-info text-white">
            <h5 class="modal-title"><i class="fas fa-user-graduate mr-2"></i>Détails de l'étudiant</h5>
            <button type="button" class="close text-white" @click="closeDetailsModal">&times;</button>
          </div>
          <div class="modal-body">
            <table class="table table-bordered mb-0" v-if="selectedDetailsReservation">
              <tbody>
                <tr>
                  <th style="width: 220px;">Nom complet</th>
                  <td>{{ getDemandeurNom(selectedDetailsReservation) }}</td>
                </tr>
                <tr>
                  <th>Niveau</th>
                  <td>{{ getEtudiantNiveau(selectedDetailsReservation) }}</td>
                </tr>
                <tr>
                  <th>Classe</th>
                  <td>{{ getEtudiantClasse(selectedDetailsReservation) }}</td>
                </tr>
                <tr>
                  <th>Département</th>
                  <td>{{ getEtudiantDepartement(selectedDetailsReservation) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="closeDetailsModal">Fermer</button>
          </div>
        </div>
      </div>
    </div>
    <div class="modal-backdrop fade show" v-if="showDetailsModal"></div>
    </div><!-- fin v-else chef de département -->
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { getReservationsByMonDepartement, approveReservation, rejectReservation } from '@/Service/ReservationService'
import api from '@/Service/api'

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
  etudiantId?: number
  etudiantNiveau?: string
  etudiantClasse?: string
  niveauEtudiant?: string
  classeEtudiant?: string
  departementEtudiant?: string
  departementNomEtudiant?: string
  niveauDemandeur?: string
  classeDemandeur?: string
  departementDemandeur?: string
  departementNomDemandeur?: string
  demandeurId?: number
  userId?: number
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
    id?: number
    nom?: string
    prenom?: string
    cin?: string
    role?: string
    niveau?: string
    classe?: string
    departement?: {
      nom?: string
    }
  }
  demandeur?: {
    id?: number
    nom?: string
    prenom?: string
    cin?: string
    role?: string
    niveau?: string
    classe?: string
    departement?: {
      nom?: string
    }
  }
}

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const isChefDepartement = computed(() => authStore.isChefDepartement)

const reservations = ref<Reservation[]>([])
const highlightedId = ref<number | null>(null)
const highlightedRow = ref<any>(null)
const userDetailsCache = new Map<number, any>()

// Modal de refus
const showRejectModal = ref(false)
const selectedReservation = ref<Reservation | null>(null)
const rejectMotif = ref('')
const showDetailsModal = ref(false)
const selectedDetailsReservation = ref<Reservation | null>(null)

// Filtres depuis les query params
const filterLaboId = computed(() => route.query.laboratoireId ? Number(route.query.laboratoireId) : null)
const filterLaboNom = computed(() => route.query.laboratoireNom as string || '')

// Réservations filtrées et triées par date décroissante
const reservationsFiltrees = computed(() => {
  let result = reservations.value
  if (filterLaboId.value) {
    result = result.filter(r => r.laboratoireId === filterLaboId.value)
  }
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
  router.replace({ path: '/enseignant/reservations' })
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

function pickFirstText(...values: any[]): string | undefined {
  for (const value of values) {
    if (typeof value === 'string' && value.trim()) return value.trim()
    if (typeof value === 'number' && Number.isFinite(value)) return String(value)
  }
  return undefined
}

function extractStudentId(res: Reservation): number | null {
  const rawId = (res as any).etudiantId ?? (res as any).demandeurId ?? (res as any).userId ?? res.etudiant?.id ?? res.demandeur?.id
  const parsed = Number(rawId)
  return Number.isFinite(parsed) && parsed > 0 ? parsed : null
}

async function fetchUserDetailsById(userId: number): Promise<any | null> {
  if (userDetailsCache.has(userId)) {
    return userDetailsCache.get(userId)
  }

  try {
    const byId = await api.get(`/users/${userId}`)
    if (byId?.data) {
      userDetailsCache.set(userId, byId.data)
      return byId.data
    }
  } catch {
    // Fallback vers la liste des users si l'endpoint /users/{id} n'est pas disponible
  }

  try {
    const usersRes = await api.get('/users')
    const users = Array.isArray(usersRes?.data)
      ? usersRes.data
      : Array.isArray(usersRes?.data?.content)
        ? usersRes.data.content
        : []
    const found = users.find((u: any) => Number(u?.id) === userId) || null
    userDetailsCache.set(userId, found)
    return found
  } catch {
    userDetailsCache.set(userId, null)
    return null
  }
}

function mergeReservationWithUser(reservation: Reservation, user: any): Reservation {
  const userDepartementNom = pickFirstText(
    user?.departement?.nom,
    typeof user?.departement === 'string' ? user.departement : undefined,
    user?.departementNom,
  )

  const existingEtudiant = reservation.etudiant || {}
  const existingDemandeur = reservation.demandeur || {}

  return {
    ...reservation,
    etudiantId: reservation.etudiantId ?? reservation.demandeurId ?? reservation.userId ?? user?.id,
    etudiant: {
      ...existingEtudiant,
      ...user,
      departement: user?.departement ?? existingEtudiant.departement,
    },
    demandeur: {
      ...existingDemandeur,
      ...user,
      departement: user?.departement ?? existingDemandeur.departement,
    },
    etudiantNiveau: pickFirstText(
      (reservation as any).etudiantNiveau,
      (reservation as any).niveauDemandeur,
      (reservation as any).niveauEtudiant,
      user?.niveau,
      user?.niveauEtude,
      user?.niveau_etude,
      user?.niveauScolaire,
    ),
    etudiantClasse: pickFirstText(
      (reservation as any).etudiantClasse,
      (reservation as any).classeDemandeur,
      (reservation as any).classeEtudiant,
      user?.classe,
      user?.classeEtudiant,
      user?.classe_etudiant,
      user?.groupe,
    ),
    departementNomDemandeur: pickFirstText(
      (reservation as any).departementNomDemandeur,
      (reservation as any).departementDemandeur,
      (reservation as any).departementNomEtudiant,
      (reservation as any).departementEtudiant,
      userDepartementNom,
    ),
  }
}

function getEtudiantNiveau(res: Reservation): string {
  return pickFirstText(
    (res as any).etudiantNiveau,
    (res as any).niveauDemandeur,
    (res as any).niveauEtudiant,
    res.etudiant?.niveau,
    (res.etudiant as any)?.niveauEtude,
    (res.etudiant as any)?.niveau_etude,
    (res.etudiant as any)?.niveauScolaire,
    res.demandeur?.niveau,
    (res.demandeur as any)?.niveauEtude,
    (res.demandeur as any)?.niveau_etude,
    (res.demandeur as any)?.niveauScolaire,
  ) || 'Non renseigné'
}

function getEtudiantClasse(res: Reservation): string {
  return pickFirstText(
    (res as any).etudiantClasse,
    (res as any).classeDemandeur,
    (res as any).classeEtudiant,
    res.etudiant?.classe,
    (res.etudiant as any)?.classeEtudiant,
    (res.etudiant as any)?.classe_etudiant,
    (res.etudiant as any)?.groupe,
    res.demandeur?.classe,
    (res.demandeur as any)?.classeEtudiant,
    (res.demandeur as any)?.classe_etudiant,
    (res.demandeur as any)?.groupe,
  ) || 'Non renseigné'
}

function getEtudiantDepartement(res: Reservation): string {
  return pickFirstText(
    (res as any).departementNomDemandeur,
    (res as any).departementDemandeur,
    (res as any).departementNomEtudiant,
    (res as any).departementEtudiant,
    res.etudiant?.departement?.nom,
    typeof (res.etudiant as any)?.departement === 'string' ? (res.etudiant as any).departement : undefined,
    (res.etudiant as any)?.departementNom,
    res.demandeur?.departement?.nom,
    typeof (res.demandeur as any)?.departement === 'string' ? (res.demandeur as any).departement : undefined,
    (res.demandeur as any)?.departementNom,
  ) || 'Non renseigné'
}

function formatDate(dateStr: string) {
  return new Date(dateStr).toLocaleDateString('fr-FR')
}

function formatStatus(status: string) {
  return { EN_ATTENTE: 'En attente', APPROUVEE: 'Approuvée', REFUSEE: 'Refusée', ANNULEE: 'Annulée' }[status] || status
}

function getStatusIcon(status: string) {
  return { EN_ATTENTE: 'fas fa-clock', APPROUVEE: 'fas fa-check-circle', REFUSEE: 'fas fa-times-circle', ANNULEE: 'fas fa-ban' }[status]
}

function getStatusBadge(status: string) {
  return { EN_ATTENTE: 'badge-warning', APPROUVEE: 'badge-success', REFUSEE: 'badge-danger', ANNULEE: 'badge-secondary' }[status] || 'badge-secondary'
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
    // Essayer l'endpoint dédié au département du chef
    const res = await getReservationsByMonDepartement()
    let data = res.data || []
    
    // Si le backend ne filtre pas par département, filtrer côté client
    // en utilisant les labos du département du chef
    if (data.length > 0 && authStore.currentUser?.departementId) {
      try {
        const labosRes = await api.get('/laboratoires/mes-labos')
        const mesLaboIds = (labosRes.data || []).map((l: any) => l.id)
        if (mesLaboIds.length > 0) {
          data = data.filter((r: any) => {
            const laboId = r.laboratoireId || r.laboratoire?.id
            return mesLaboIds.includes(laboId)
          })
        }
      } catch (e) {
        console.warn('Impossible de filtrer par département:', e)
      }
    }
    
    const enrichedData = await Promise.all(
      data.map(async (reservation: Reservation) => {
        const needsAcademicDetails =
          !pickFirstText(
            (reservation as any).etudiantNiveau,
            (reservation as any).niveauDemandeur,
            (reservation as any).niveauEtudiant,
            reservation.etudiant?.niveau,
            reservation.demandeur?.niveau,
          ) ||
          !pickFirstText(
            (reservation as any).etudiantClasse,
            (reservation as any).classeDemandeur,
            (reservation as any).classeEtudiant,
            reservation.etudiant?.classe,
            reservation.demandeur?.classe,
          ) ||
          !pickFirstText(
            (reservation as any).departementNomDemandeur,
            (reservation as any).departementDemandeur,
            (reservation as any).departementNomEtudiant,
            (reservation as any).departementEtudiant,
            reservation.etudiant?.departement?.nom,
            reservation.demandeur?.departement?.nom,
          )

        if (!needsAcademicDetails) return reservation

        const studentId = extractStudentId(reservation)
        if (!studentId) return reservation

        const userDetails = await fetchUserDetailsById(studentId)
        if (!userDetails) return reservation

        return mergeReservationWithUser(reservation, userDetails)
      }),
    )

    reservations.value = enrichedData
    console.log("Réservations du département chargées:", reservations.value.length)
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

function openDetailsModal(reservation: Reservation) {
  selectedDetailsReservation.value = reservation
  showDetailsModal.value = true
}

function closeDetailsModal() {
  showDetailsModal.value = false
  selectedDetailsReservation.value = null
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

.highlight-row {
  animation: highlightPulse 1s ease-in-out 3;
  background-color: #d1fae5 !important;
}

@keyframes highlightPulse {
  0%, 100% { background-color: #d1fae5; }
  50% { background-color: #a7f3d0; }
}
</style>
