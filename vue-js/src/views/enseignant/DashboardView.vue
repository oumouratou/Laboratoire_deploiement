<template>
  <div class="dashboard-container">
    <!-- Header -->
    <h2><i class="fas fa-tachometer-alt mr-2"></i> Dashboard Enseignant</h2>

    <!-- Stats Cards -->
    <div class="stats-grid">
      <div class="stat-card bg-warning">
        <div class="stat-icon"><i class="fas fa-exclamation-circle"></i></div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.totalReclamations }}</span>
          <span class="stat-label">Total réclamations</span>
        </div>
      </div>
      <div class="stat-card bg-danger">
        <div class="stat-icon"><i class="fas fa-clock"></i></div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.pendingReclamations }}</span>
          <span class="stat-label">En attente</span>
        </div>
      </div>
      <div class="stat-card bg-success">
        <div class="stat-icon"><i class="fas fa-check-circle"></i></div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.resolvedReclamations || 0 }}</span>
          <span class="stat-label">Traitées</span>
        </div>
      </div>
      <div class="stat-card bg-info">
        <div class="stat-icon"><i class="fas fa-desktop"></i></div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.equipements }}</span>
          <span class="stat-label">Équipements</span>
        </div>
      </div>
    </div>

    <!-- Actions rapides -->
    <div class="card mb-4">
      <div class="card-header d-flex justify-content-between align-items-center">
        <h3 class="card-title mb-0"><i class="fas fa-bolt text-warning mr-2"></i> Actions rapides</h3>
      </div>
      <div class="card-body">
        <div class="row">
          <div class="col-md-3 mb-2">
            <router-link to="/enseignant/nouvelle-reclamation" class="btn btn-warning btn-block">
              <i class="fas fa-plus-circle mr-2"></i> Nouvelle réclamation
            </router-link>
          </div>
          <div class="col-md-3 mb-2">
            <router-link to="/enseignant/reclamations" class="btn btn-secondary btn-block">
              <i class="fas fa-list mr-2"></i> Mes réclamations
            </router-link>
          </div>
          <div class="col-md-3 mb-2">
            <router-link to="/enseignant/laboratoires" class="btn btn-primary btn-block">
              <i class="fas fa-flask mr-2"></i> Laboratoires
            </router-link>
          </div>
          <div class="col-md-3 mb-2">
            <router-link to="/enseignant/equipements" class="btn btn-info btn-block">
              <i class="fas fa-desktop mr-2"></i> Équipements
            </router-link>
          </div>
        </div>
      </div>
    </div>

    <!-- Réclamations récentes -->
    <div class="card mb-4">
      <div class="card-header d-flex justify-content-between align-items-center">
        <h3 class="card-title mb-0"><i class="fas fa-exclamation-triangle text-warning mr-2"></i> Mes réclamations récentes</h3>
        <router-link to="/enseignant/reclamations" class="btn btn-sm btn-outline-warning">Voir tout</router-link>
      </div>
      <div class="card-body table-responsive p-0">
        <table class="table table-hover mb-0" v-if="recentReclamations.length">
          <thead class="bg-light">
            <tr>
              <th>Numéro</th>
              <th>Équipement</th>
              <th>Description</th>
              <th>Statut</th>
              <th>Date</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(rec, index) in recentReclamations" :key="rec.id">
              <td><strong>{{ index + 1 }}</strong></td>
              <td>{{ rec.equipementNom || rec.equipement?.nom || 'N/A' }}</td>
              <td>{{ truncateText(rec.description, 40) }}</td>
              <td>
                <span :class="getReclamationBadge(rec.etat || rec.statut)">
                  {{ formatReclamationStatus(rec.etat || rec.statut) }}
                </span>
              </td>
              <td>{{ formatDate(rec.dateReclamation || rec.dateCreation) }}</td>
            </tr>
          </tbody>
        </table>
        <div v-else class="text-center text-muted p-4">
          <i class="fas fa-clipboard-check fa-3x mb-3"></i>
          <p>Aucune réclamation</p>
        </div>
      </div>
    </div>

    <!-- Laboratoires Section -->
    <div class="card mb-4">
      <div class="card-header d-flex justify-content-between align-items-center">
        <h3 class="card-title mb-0"><i class="fas fa-flask text-primary mr-2"></i> Laboratoires disponibles</h3>
        <router-link to="/enseignant/laboratoires" class="btn btn-sm btn-outline-primary">Voir tout</router-link>
      </div>
      <div class="card-body table-responsive p-0">
        <table class="table table-hover mb-0" v-if="recentLaboratoires.length">
          <thead class="bg-light">
            <tr>
              <th>Numéro</th>
              <th>Nom</th>
              <th>Département</th>
              <th>État</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(labo, index) in recentLaboratoires" :key="labo.id">
              <td><strong>{{ index + 1 }}</strong></td>
              <td>{{ labo.nomLabo || labo.nom }}</td>
              <td><i class="fas fa-building mr-1"></i> {{ labo.departement?.nom || 'N/A' }}</td>
              <td>
                <span :class="getLaboStateBadge(labo.etatLabo)">
                  {{ labo.etatLabo || 'N/A' }}
                </span>
              </td>
              <td>
                <router-link 
                  v-if="labo.etatLabo === 'ACTIF'"
                  :to="'/enseignant/nouvelle-reclamation?laboratoireId=' + labo.id" 
                  class="btn btn-warning btn-sm"
                >
                  <i class="fas fa-exclamation-triangle mr-1"></i> Réclamer
                </router-link>
                <span v-else class="badge badge-secondary">
                  <i class="fas fa-ban mr-1"></i> Indisponible
                </span>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-else class="text-center text-muted p-4">
          <i class="fas fa-flask fa-3x mb-3"></i>
          <p>Aucun laboratoire disponible</p>
        </div>
      </div>
    </div>

    <!-- Équipements en panne Section -->
    <div class="card">
      <div class="card-header d-flex justify-content-between align-items-center">
        <h3 class="card-title mb-0"><i class="fas fa-tools text-danger mr-2"></i> Équipements en panne</h3>
        <router-link to="/enseignant/equipements" class="btn btn-sm btn-outline-danger">Voir tout</router-link>
      </div>
      <div class="card-body table-responsive p-0">
        <table class="table table-hover mb-0" v-if="equipementsEnPanne.length">
          <thead class="bg-light">
            <tr>
              <th>Numéro</th>
              <th>Nom</th>
              <th>Laboratoire</th>
              <th>État</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(equip, index) in equipementsEnPanne" :key="equip.id">
              <td><strong>{{ index + 1 }}</strong></td>
              <td>{{ equip.nom }}</td>
              <td>{{ equip.laboratoire?.nomLabo || 'N/A' }}</td>
              <td>
                <span class="badge badge-danger">
                  <i class="fas fa-exclamation-circle mr-1"></i> {{ formatEquipementEtat(equip.etat) }}
                </span>
              </td>
              <td>
                <router-link 
                  :to="'/enseignant/nouvelle-reclamation?equipementId=' + equip.id + '&laboratoireId=' + (equip.laboratoire?.id || '')" 
                  class="btn btn-warning btn-sm"
                >
                  <i class="fas fa-exclamation-triangle mr-1"></i> Réclamer
                </router-link>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-else class="text-center text-muted p-4">
          <i class="fas fa-check-circle fa-3x mb-3 text-success"></i>
          <p>Tous les équipements sont fonctionnels</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { getLaboratoires, getMesLabos } from '@/Service/LaboratoireService'
import { getEquipements } from '@/Service/EquipementService'
import ReclamationService from '@/Service/ReclamationService'
import api from '@/Service/api'
import { useAuthStore } from '@/stores/auth'

interface Reclamation {
  id: number
  titre?: string
  description: string
  statut: string
  etat?: string
  dateCreation?: string
  dateReclamation?: string
  equipementNom?: string
  laboratoireNom?: string
  equipement?: { id: number; nom: string }
  laboratoire?: { id: number; nomLabo?: string; nom?: string }
}

interface Laboratoire {
  id: number
  nomLabo?: string
  nom?: string
  capacite?: number
  etatLabo?: string
  departement?: { id: number; nom: string }
}

interface Equipement {
  id: number
  nom: string
  etat: string
  laboratoire?: { id: number; nomLabo?: string }
}

const authStore = useAuthStore()
const reclamations = ref<Reclamation[]>([])
const laboratoires = ref<Laboratoire[]>([])
const equipements = ref<Equipement[]>([])
const equipementsCount = ref(0)

// Compter uniquement les laboratoires actifs
const activeLaboratoires = computed(() => 
  laboratoires.value.filter(l => l.etatLabo !== 'INACTIF' && l.etatLabo !== 'EN_MAINTENANCE')
)

const stats = computed(() => ({
  totalReclamations: reclamations.value.length,
  pendingReclamations: reclamations.value.filter(r => r.etat === 'NON_TRAITEE' || r.statut === 'NOUVELLE' || r.statut === 'EN_COURS').length,
  resolvedReclamations: reclamations.value.filter(r => r.etat === 'TRAITEE' || r.statut === 'RESOLUE').length,
  laboratoires: laboratoires.value.length, // Compte TOUS les laboratoires
  equipements: equipementsCount.value
}))

// Réclamations récentes triées par date décroissante (plus récentes en premier)
const recentReclamations = computed(() => {
  return [...reclamations.value]
    .sort((a, b) => {
      const dateA = new Date(a.dateReclamation || a.dateCreation || 0).getTime()
      const dateB = new Date(b.dateReclamation || b.dateCreation || 0).getTime()
      return dateB - dateA
    })
    .slice(0, 5)
})

// Laboratoires disponibles (tous, pour afficher l'état)
const recentLaboratoires = computed(() => {
  return laboratoires.value.slice(0, 5)
})

// Équipements en panne (pour le tableau)
const equipementsEnPanne = computed(() => {
  return equipements.value
    .filter(e => e.etat === 'EN_PANNE' || e.etat === 'EN_MAINTENANCE')
    .slice(0, 5)
})

const statLabels: Record<string, string> = {
  totalReclamations: 'Total réclamations',
  pendingReclamations: 'En attente',
  laboratoires: 'Laboratoires',
  equipements: 'Équipements'
}

const statIcons: Record<string, string> = {
  totalReclamations: 'fas fa-exclamation-circle',
  pendingReclamations: 'fas fa-clock',
  laboratoires: 'fas fa-flask',
  equipements: 'fas fa-desktop'
}

const statColors: Record<string, string> = {
  totalReclamations: 'orange',
  pendingReclamations: 'red',
  laboratoires: 'purple',
  equipements: 'blue'
}

function formatDate(dateStr?: string): string {
  if (!dateStr) return 'N/A'
  return new Date(dateStr).toLocaleDateString('fr-FR', { day: '2-digit', month: 'short' })
}

function formatReclamationStatus(statut: string): string {
  const map: Record<string, string> = {
    'NOUVELLE': 'Nouvelle',
    'EN_COURS': 'En cours',
    'RESOLUE': 'Résolue',
    'REJETEE': 'Rejetée',
    'NON_TRAITEE': 'Non traitée',
    'TRAITEE': 'Traitée',
    'ANNULEE': 'Refusée'
  }
  return map[statut] || statut
}

function getReclamationStatusClass(statut: string): string {
  const map: Record<string, string> = {
    'NOUVELLE': 'status-pending',
    'EN_COURS': 'status-progress',
    'RESOLUE': 'status-approved',
    'REJETEE': 'status-rejected',
    'NON_TRAITEE': 'status-pending',
    'TRAITEE': 'status-approved',
    'ANNULEE': 'status-rejected'
  }
  return map[statut] || ''
}

function getReclamationIconClass(statut: string): string {
  const map: Record<string, string> = {
    'NOUVELLE': 'pending',
    'EN_COURS': 'progress',
    'RESOLUE': 'success',
    'REJETEE': 'danger',
    'NON_TRAITEE': 'pending',
    'TRAITEE': 'success',
    'ANNULEE': 'danger'
  }
  return map[statut] || 'pending'
}

function getReclamationBadge(statut: string): string {
  const map: Record<string, string> = {
    'NOUVELLE': 'badge badge-warning',
    'EN_COURS': 'badge badge-info',
    'RESOLUE': 'badge badge-success',
    'REJETEE': 'badge badge-danger',
    'NON_TRAITEE': 'badge badge-warning',
    'TRAITEE': 'badge badge-success',
    'ANNULEE': 'badge badge-secondary'
  }
  return map[statut] || 'badge badge-secondary'
}

function truncateText(text: string, maxLength: number): string {
  if (!text) return ''
  return text.length > maxLength ? text.substring(0, maxLength) + '...' : text
}

function getLaboStateBadge(etat?: string): string {
  const map: Record<string, string> = {
    'ACTIF': 'badge badge-success',
    'INACTIF': 'badge badge-danger',
    'EN_MAINTENANCE': 'badge badge-warning'
  }
  return map[etat || ''] || 'badge badge-secondary'
}

function formatEquipementEtat(etat: string): string {
  const map: Record<string, string> = {
    'FONCTIONNEL': 'Fonctionnel',
    'EN_PANNE': 'En panne',
    'EN_MAINTENANCE': 'En maintenance'
  }
  return map[etat] || etat
}

async function fetchData() {
  try {
    // x Réclamations de l'enseignant connecté
    const resReclamations = await ReclamationService.getReclamationsEnseignantConnecte()
    console.log('Réclamations enseignant:', resReclamations.data)
    reclamations.value = Array.isArray(resReclamations.data) ? resReclamations.data : []

    // Laboratoires
    const labosRes = await getLaboratoires()
    laboratoires.value = Array.isArray(labosRes.data) ? labosRes.data : []

    // Équipements
    const equipRes = await getEquipements()
    const equipData = Array.isArray(equipRes.data) ? equipRes.data : []
    equipements.value = equipData
    equipementsCount.value = equipData.length

  } catch (error) {
    console.error("Erreur chargement dashboard:", error)
  }
}

onMounted(async () => {
  await fetchData()
  try {
    const res = await getMesLabos()
    if (Array.isArray(res.data) && res.data.length > 0) {
      laboratoires.value = res.data
    }
    console.log('Labos de mon département :', laboratoires.value)
  } catch (error) {
    console.error('Erreur chargement laboratoires', error)
  }
})

</script>

<style scoped>
.dashboard-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
  background: #f4f6f9;
}

.dashboard-container h2 {
  margin-bottom: 24px;
  color: #343a40;
}

/* Stats Cards - Style Technicien */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 28px;
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  border-left: 4px solid transparent;
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.12);
}

.stat-card.bg-warning {
  border-left-color: #ffc107;
  background: linear-gradient(to right, #fff9e6, #fff);
}

.stat-card.bg-danger {
  border-left-color: #dc3545;
  background: linear-gradient(to right, #ffe6e6, #fff);
}

.stat-card.bg-primary {
  border-left-color: #007bff;
  background: linear-gradient(to right, #e6f3ff, #fff);
}

.stat-card.bg-info {
  border-left-color: #17a2b8;
  background: linear-gradient(to right, #e6f7f9, #fff);
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: #fff;
}

.stat-card.bg-warning .stat-icon { background: #ffc107; }
.stat-card.bg-danger .stat-icon { background: #dc3545; }
.stat-card.bg-primary .stat-icon { background: #007bff; }
.stat-card.bg-info .stat-icon { background: #17a2b8; }

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #343a40;
}

.stat-label {
  font-size: 14px;
  color: #6c757d;
}

/* Cards */
.card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  overflow: hidden;
  border: 1px solid #e9ecef;
}

.card-header {
  background: #f8f9fa;
  border-bottom: 1px solid #e9ecef;
  padding: 12px 20px;
}

.card-header h3, .card-title {
  font-size: 16px;
  font-weight: 600;
  color: #343a40;
  margin: 0;
}

.card-body {
  padding: 16px 20px;
}

/* Table */
.table {
  margin-bottom: 0;
}

.table thead th {
  border-top: none;
  font-weight: 600;
  color: #495057;
  font-size: 13px;
  text-transform: uppercase;
}

.table tbody td {
  vertical-align: middle;
  color: #495057;
}

/* Badge styles */
.badge {
  padding: 5px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.badge-warning { background: #ffc107; color: #212529; }
.badge-info { background: #17a2b8; color: #fff; }
.badge-success { background: #28a745; color: #fff; }
.badge-danger { background: #dc3545; color: #fff; }
.badge-secondary { background: #6c757d; color: #fff; }

/* Buttons */
.btn-block {
  width: 100%;
  text-align: left;
}

/* Responsive */
@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>
