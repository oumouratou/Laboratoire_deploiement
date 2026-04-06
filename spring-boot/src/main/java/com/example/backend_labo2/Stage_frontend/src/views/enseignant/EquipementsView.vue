<template>
  <div>
    <!-- En-tête -->
    <section class="content-header">
      <div class="container-fluid">
        <div class="row mb-2">
          <div class="col-sm-6">
            <h1><i class="fas fa-cogs mr-2"></i> Équipements</h1>
            <small class="text-muted">Voir les équipements de tous les laboratoires</small>
          </div>
        </div>
      </div>
    </section>

    <!-- Contenu -->
    <section class="content">
      <div class="container-fluid">

        <!-- Filtres par département et laboratoire -->
        <div class="card card-outline card-info mb-3">
          <div class="card-header">
            <h3 class="card-title"><i class="fas fa-filter mr-2"></i>Filtres</h3>
          </div>
          <div class="card-body">
            <div class="row">
              <!-- Filtre Département -->
              <div class="col-md-4">
                <div class="form-group">
                  <label>Département</label>
                  <select class="form-control" v-model="selectedDepartement" @change="onDepartementChange">
                    <option value="">-- Tous les départements --</option>
                    <option v-for="dept in departements" :key="dept.id" :value="dept.id">
                      {{ dept.nom }}
                    </option>
                  </select>
                </div>
              </div>

              <!-- Filtre Laboratoire -->
              <div class="col-md-4">
                <div class="form-group">
                  <label>Laboratoire</label>
                  <select class="form-control" v-model="selectedLaboratoire">
                    <option value="">-- Tous les laboratoires --</option>
                    <option v-for="labo in filteredLaboratoires" :key="labo.id" :value="labo.id">
                      {{ labo.nomLabo || labo.nom }}
                    </option>
                  </select>
                </div>
              </div>

              <!-- Info -->
              <div class="col-md-4 d-flex align-items-end">
                <div class="form-group w-100">
                  <span class="badge badge-info p-2">
                    <i class="fas fa-cog mr-1"></i> {{ filteredEquipements.length }} équipement(s) trouvé(s)
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Loading -->
        <div v-if="loading" class="text-center p-5">
          <i class="fas fa-spinner fa-spin fa-2x"></i>
          <p class="mt-2">Chargement des équipements...</p>
        </div>

        <!-- Liste des équipements -->
        <div class="row" v-else>
          <div class="col-md-4 mb-3" v-for="equip in filteredEquipements" :key="equip.id">
            <div class="card" :class="getCardClass(equip.etat)">
              <div class="card-header">
                <h3 class="card-title">{{ equip.nom }}</h3>
                <div class="card-tools">
                  <span :class="getEtatBadge(equip.etat)">{{ formatEtat(equip.etat) }}</span>
                </div>
              </div>

              <div class="card-body text-center">
                <div v-if="equip.imageUrl" class="mb-2">
                  <img :src="equip.imageUrl" alt="Image équipement" class="equip-image" />
                </div>

                <p><strong>Description:</strong> {{ equip.caracteristique || 'N/A' }}</p>
                <p><strong>Identifiant:</strong> <code class="text-primary">{{ equip.identifiant || 'N/A' }}</code></p>
                <p><strong>Laboratoire:</strong> {{ getLaboNom(equip.laboratoire?.id || equip.laboratoireId) }}</p>
                <p><strong>Date acquisition:</strong> {{ equip.dateAcquisition || 'N/A' }}</p>
              </div>

              <div class="card-footer text-center">
                <button class="btn btn-info btn-sm mr-2" @click="showDetails(equip)">
                  <i class="fas fa-eye mr-1"></i> Détails
                </button>
                <button 
                  class="btn btn-sm" 
                  :class="canReclamer(equip) ? 'btn-warning' : 'btn-secondary'"
                  @click="goToReclamation(equip)"
                  :disabled="!canReclamer(equip)"
                  :title="getReclamationTooltip(equip)"
                >
                  <i class="fas fa-exclamation-triangle mr-1"></i> 
                  {{ getReclamationButtonText(equip) }}
                </button>
              </div>
            </div>
          </div>

          <div v-if="filteredEquipements.length === 0" class="col-12 text-center text-muted p-5">
            <i class="fas fa-box-open fa-3x mb-3"></i>
            <p>Aucun équipement trouvé pour cette sélection.</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Modal Détails -->
    <div class="modal fade" :class="{ show: showDetailsModal }" :style="{ display: showDetailsModal ? 'block' : 'none' }">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header bg-info">
            <h4 class="modal-title">Détails de l'équipement</h4>
            <button type="button" class="close" @click="showDetailsModal = false">
              <span>&times;</span>
            </button>
          </div>
          <div class="modal-body" v-if="selectedEquip">
            <dl class="row">
              <dt class="col-sm-4">Nom:</dt>
              <dd class="col-sm-8">{{ selectedEquip.nom }}</dd>

              <dt class="col-sm-4">Identifiant:</dt>
              <dd class="col-sm-8"><code class="text-primary">{{ selectedEquip.identifiant || 'N/A' }}</code></dd>

              <dt class="col-sm-4">Description:</dt>
              <dd class="col-sm-8">{{ selectedEquip.caracteristique || 'N/A' }}</dd>

              <dt class="col-sm-4">Laboratoire:</dt>
              <dd class="col-sm-8">{{ getLaboNom(selectedEquip.laboratoire?.id || selectedEquip.laboratoireId) }}</dd>

              <dt class="col-sm-4">Département:</dt>
              <dd class="col-sm-8">{{ getDeptNom(getDeptIdFromEquip(selectedEquip)) }}</dd>

              <dt class="col-sm-4">État:</dt>
              <dd class="col-sm-8">
                <span :class="getEtatBadge(selectedEquip.etat)">{{ formatEtat(selectedEquip.etat) }}</span>
              </dd>

              <dt class="col-sm-4">Date acquisition:</dt>
              <dd class="col-sm-8">{{ selectedEquip.dateAcquisition || 'N/A' }}</dd>

              <dt class="col-sm-4" v-if="selectedEquip.imageUrl">Image:</dt>
              <dd class="col-sm-8" v-if="selectedEquip.imageUrl">
                <img :src="selectedEquip.imageUrl" alt="Image équipement" class="equip-image-large" />
              </dd>
            </dl>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="showDetailsModal = false">Fermer</button>
          </div>
        </div>
      </div>
    </div>
    <div class="modal-backdrop fade show" v-if="showDetailsModal"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getLaboratoires } from '@/Service/LaboratoireService'
import { getEquipements } from '@/Service/EquipementService'
import { getDepartements } from '@/Service/departementService'
import type { Equipement, Laboratoire, Departement } from '@/types'

const router = useRouter()
const equipements = ref<Equipement[]>([])
const laboratoires = ref<Laboratoire[]>([])
const departements = ref<Departement[]>([])
const selectedDepartement = ref<number | ''>('')
const selectedLaboratoire = ref<number | ''>('')
const showDetailsModal = ref(false)
const selectedEquip = ref<Equipement | null>(null)
const loading = ref(false)

// Charger les données au montage
onMounted(async () => {
  loading.value = true
  try {
    // Charger tous les départements
    const resDepts = await getDepartements()
    departements.value = Array.isArray(resDepts.data) ? resDepts.data : []

    // Charger tous les laboratoires
    const resLabos = await getLaboratoires()
    laboratoires.value = Array.isArray(resLabos.data) ? resLabos.data : []

    // Charger tous les équipements
    const resEquips = await getEquipements()
    equipements.value = Array.isArray(resEquips.data) ? resEquips.data : []

    console.log('Départements chargés:', departements.value)
    console.log('Laboratoires chargés:', laboratoires.value)
    console.log('Équipements chargés:', equipements.value)
  } catch (err) {
    console.error('Erreur chargement données:', err)
  } finally {
    loading.value = false
  }
})

// Laboratoires filtrés par département sélectionné
const filteredLaboratoires = computed(() => {
  if (!selectedDepartement.value) return laboratoires.value
  return laboratoires.value.filter(labo =>
    labo.departement?.id === selectedDepartement.value ||
    labo.departementId === selectedDepartement.value
  )
})

// Équipements filtrés par laboratoire sélectionné (ou tous les labos du département)
const filteredEquipements = computed(() => {
  let result = equipements.value

  if (selectedDepartement.value) {
    result = result.filter(e => {
      const laboId = e.laboratoire?.id || e.laboratoireId
      if (!laboId) return false
      const labo = laboratoires.value.find(l => l.id === laboId)
      const deptId = labo?.departement?.id || labo?.departementId
      return deptId === selectedDepartement.value
    })
  }

  if (selectedLaboratoire.value) {
    result = result.filter(e =>
      e.laboratoire?.id === selectedLaboratoire.value ||
      e.laboratoireId === selectedLaboratoire.value
    )
  }

  return result
})

// Quand le département change
function onDepartementChange() {
  selectedLaboratoire.value = ''
}

function getDeptIdFromEquip(equip?: Equipement | null): number | undefined {
  if (!equip) return undefined
  const laboId = equip.laboratoire?.id || equip.laboratoireId
  if (!laboId) return undefined
  const labo = laboratoires.value.find(l => l.id === laboId)
  return labo?.departement?.id || labo?.departementId
}

function getLaboNom(id?: number) {
  if (!id) return 'N/A'
  const labo = laboratoires.value.find(l => l.id === id)
  return labo ? (labo.nomLabo || labo.nom) : 'N/A'
}

function getDeptNom(id?: number | '') {
  if (!id) return 'N/A'
  const dept = departements.value.find(d => d.id === id)
  return dept ? dept.nom : 'N/A'
}

function getCardClass(etat: string) {
  const classes: Record<string, string> = {
    FONCTIONNEL: 'card-outline card-success',
    EN_PANNE: 'card-outline card-danger',
    EN_MAINTENANCE: 'card-outline card-warning'
  }
  return classes[etat] || 'card-outline card-secondary'
}

function getEtatBadge(etat: string) {
  const badges: Record<string, string> = {
    FONCTIONNEL: 'badge badge-success',
    EN_PANNE: 'badge badge-danger',
    EN_MAINTENANCE: 'badge badge-warning'
  }
  return badges[etat] || 'badge badge-secondary'
}

function formatEtat(etat: string) {
  const labels: Record<string, string> = {
    FONCTIONNEL: 'Fonctionnel',
    EN_PANNE: 'En panne',
    EN_MAINTENANCE: 'En maintenance'
  }
  return labels[etat] || etat
}

function showDetails(equip: Equipement) {
  selectedEquip.value = equip
  showDetailsModal.value = true
}

// Vérifie si on peut réclamer cet équipement
// On peut toujours réclamer un équipement (même fonctionnel)
function canReclamer(equip: Equipement): boolean {
  return true
}

// Tooltip pour le bouton réclamer
function getReclamationTooltip(equip: Equipement): string {
  return 'Signaler un problème avec cet équipement'
}

// Texte du bouton réclamer
function getReclamationButtonText(equip: Equipement): string {
  return 'Réclamer'
}

// Rediriger vers le formulaire de réclamation avec l'équipement pré-sélectionné
function goToReclamation(equip: Equipement) {
  if (!canReclamer(equip)) return
  
  const laboId = equip.laboratoire?.id || equip.laboratoireId
  router.push({ 
    path: '/etudiant/nouvelle-reclamation', 
    query: { 
      equipementId: equip.id, 
      equipementNom: equip.nom,
      laboratoireId: laboId,
      laboratoireNom: getLaboNom(laboId)
    } 
  })
}
</script>

<style scoped>
.modal.show {
  background: rgba(0, 0, 0, 0.5);
}
.equip-image {
  max-width: 100px;
  max-height: 110px;
  object-fit: cover;
  border-radius: 5px;
  border: 1px solid #ddd;
  margin-bottom: 10px;
}
.equip-image-large {
  max-width: 200px;
  max-height: 200px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #ddd;
}
.card-title, 
.modal-body dt {
  font-weight: bold;
}
</style>
