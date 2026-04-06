<template>
  <div>
    <!-- HEADER -->
    <section class="content-header">
      <div class="container-fluid">
        <h1>
          <i class="fas fa-flask mr-2"></i>
          Gestion des Laboratoires
        </h1>
      </div>
    </section>

    <!-- CONTENT -->
    <section class="content">
      <div class="container-fluid">

        <!-- x FILTRE D0PARTEMENT -->
        <div class="card mb-3">
          <div class="card-body">
            <label><strong>Filtrer par département :</strong></label>
            <select v-model="selectedDepartement" class="form-control w-25">
              <option value="">Tous les départements</option>
              <option
                v-for="dep in departements"
                :key="dep.id"
                :value="dep.nom"
              >
                {{ dep.nom }}
              </option>
            </select>
          </div>
        </div>

        <div class="card">
          <div class="card-body table-responsive p-0">
            <table class="table table-hover text-nowrap">
              <thead>
                <tr>
                  <th>Nom du laboratoire</th>
                  <th>Département</th>
                  <th>État</th>
                  <th>Équipements</th>
                  <th class="text-center">Actions</th>
                </tr>
              </thead>

              <tbody>
                <tr v-for="labo in labosFiltres" :key="labo.id">
                  <td>{{ labo.nomLabo }}</td>
                  <td>
                    <span class="badge badge-info">
                      {{ labo.departement?.nom || 'N/A' }}
                    </span>
                  </td>
                  <td>
                    <span class="badge" :class="etatBadge(labo.etatLabo)">
                      {{ labo.etatLabo }}
                    </span>
                  </td>
                  <td>
                    <button 
                      class="btn btn-sm btn-info"
                      @click="showEquipementsModal(labo)"
                      :disabled="!labo.equipements || labo.equipements.length === 0"
                    >
                      <i class="fas fa-cogs mr-1"></i>
                      {{ labo.equipements?.length || 0 }} équipement(s)
                    </button>
                  </td>
                  <td class="text-center">
                    <button 
                      class="btn btn-sm" 
                      :class="canReclamer(labo) ? 'btn-warning' : 'btn-secondary'"
                      @click="goToReclamation(labo)"
                      :disabled="!canReclamer(labo)"
                      :title="getReclamationTooltip(labo)"
                    >
                      <i class="fas fa-exclamation-triangle mr-1"></i> 
                      {{ canReclamer(labo) ? 'Réclamer' : 'Labo inactif' }}
                    </button>
                  </td>
                </tr>

                <tr v-if="labosFiltres.length === 0">
                  <td colspan="5" class="text-center text-muted">
                    Aucun laboratoire trouvé
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

      </div>
    </section>

    <!-- Modal Équipements du Laboratoire -->
    <div class="modal fade" :class="{ show: showEquipmentsModal }" :style="{ display: showEquipmentsModal ? 'block' : 'none' }">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header bg-info text-white">
            <h5 class="modal-title">
              <i class="fas fa-cogs mr-2"></i>
              Équipements de {{ selectedLabo?.nomLabo }}
            </h5>
            <button type="button" class="close text-white" @click="closeEquipmentsModal">&times;</button>
          </div>
          <div class="modal-body">
            <div v-if="selectedLabo?.equipements && selectedLabo.equipements.length > 0">
              <table class="table table-striped table-hover">
                <thead>
                  <tr>
                    <th>Numéro</th>
                    <th>Nom</th>
                    <th>État</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(equip, index) in selectedLabo.equipements" :key="equip.id">
                    <td>{{ Number(index) + 1 }}</td>
                    <td>{{ equip.nom }}</td>
                    <td>
                      <span :class="getEquipEtatBadge(equip.etat)">
                        {{ formatEquipEtat(equip.etat) }}
                      </span>
                    </td>
                    <td>
                      <button 
                        class="btn btn-warning btn-sm"
                        @click="goToReclamationEquip(selectedLabo, equip)"
                      >
                        <i class="fas fa-exclamation-triangle mr-1"></i> Réclamer
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
            <div v-else class="text-center text-muted p-4">
              <i class="fas fa-inbox fa-2x mb-2"></i>
              <p>Aucun équipement dans ce laboratoire</p>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="closeEquipmentsModal">Fermer</button>
          </div>
        </div>
      </div>
    </div>
    <div class="modal-backdrop fade show" v-if="showEquipmentsModal"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getLaboratoires } from '@/Service/LaboratoireService'
import { getDepartements } from '@/Service/departementService'

const router = useRouter()
const laboratoires = ref<any[]>([])
const departements = ref<any[]>([])
const selectedDepartement = ref('')
const showEquipmentsModal = ref(false)
const selectedLabo = ref<any>(null)

onMounted(async () => {
  try {
    const labosRes = await getLaboratoires()
    laboratoires.value = Array.isArray(labosRes.data) ? labosRes.data : []

    const depsRes = await getDepartements()
    departements.value = Array.isArray(depsRes.data) ? depsRes.data : []

  } catch (error) {
    console.error('Erreur chargement laboratoires ou départements', error)
    alert('Impossible de charger les laboratoires ou les départements.')
  }
})

/* x} LABOS FILTRES */
const labosFiltres = computed(() => {
  if (!selectedDepartement.value) return laboratoires.value
  return laboratoires.value.filter(
    l => l.departement?.nom === selectedDepartement.value
  )
})

/* x} BADGE ETAT */
function etatBadge(etat: string) {
  switch (etat) {
    case 'ACTIF':
      return 'badge-primary'
    case 'INACTIF':
      return 'badge-danger'
    default:
      return 'badge-secondary'
  }
}

// Afficher le modal des équipements
function showEquipementsModal(labo: any) {
  selectedLabo.value = labo
  showEquipmentsModal.value = true
}

// Fermer le modal
function closeEquipmentsModal() {
  showEquipmentsModal.value = false
  selectedLabo.value = null
}

// Badge état équipement
function getEquipEtatBadge(etat: string) {
  const badges: Record<string, string> = {
    FONCTIONNEL: 'badge badge-success',
    EN_PANNE: 'badge badge-danger',
    EN_MAINTENANCE: 'badge badge-warning'
  }
  return badges[etat] || 'badge badge-secondary'
}

// Format état équipement
function formatEquipEtat(etat: string) {
  const labels: Record<string, string> = {
    FONCTIONNEL: 'Fonctionnel',
    EN_PANNE: 'En panne',
    EN_MAINTENANCE: 'En maintenance'
  }
  return labels[etat] || etat
}

// Rediriger vers le formulaire de réclamation avec le labo pré-sélectionné
function goToReclamation(labo: any) {
  router.push({ 
    path: '/enseignant/nouvelle-reclamation', 
    query: { laboratoireId: labo.id, laboratoireNom: labo.nomLabo } 
  })
}

// Rediriger vers réclamation avec équipement pré-sélectionné
function goToReclamationEquip(labo: any, equip: any) {
  closeEquipmentsModal()
  router.push({ 
    path: '/enseignant/nouvelle-reclamation', 
    query: { 
      laboratoireId: labo.id, 
      laboratoireNom: labo.nomLabo,
      equipementId: equip.id,
      equipementNom: equip.nom
    } 
  })
}

// Vérifie si on peut réclamer sur ce labo
function canReclamer(labo: any): boolean {
  return labo.etatLabo !== 'INACTIF' && labo.etatLabo !== 'EN_MAINTENANCE'
}

// Tooltip pour le bouton réclamer
function getReclamationTooltip(labo: any): string {
  if (labo.etatLabo === 'INACTIF') return 'Ce laboratoire est inactif'
  if (labo.etatLabo === 'EN_MAINTENANCE') return 'Ce laboratoire est en maintenance'
  return 'Signaler un problème'
}
</script>

<style scoped>
.badge {
  font-size: 0.85rem;
}
.btn:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}
.modal.show {
  background: rgba(0, 0, 0, 0.5);
}
</style>

