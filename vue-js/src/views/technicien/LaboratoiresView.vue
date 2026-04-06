<template>
  <div>
    <!-- Header -->
    <section class="content-header">
      <div class="container-fluid">
        <div class="d-flex justify-content-between align-items-center">
          <h1>Gestion des Laboratoires</h1>
          <!-- Filtre par département -->
          <div class="d-flex align-items-center">
            <label class="mr-2 mb-0">Filtrer par département :</label>
            <select class="form-control form-control-sm" v-model="selectedDepartementId" style="width: 200px;">
              <option value="">-- Tous --</option>
              <option v-for="d in departements" :key="d.id" :value="d.id">{{ d.nom }}</option>
            </select>
            <button v-if="selectedDepartementId" class="btn btn-sm btn-outline-secondary ml-2" @click="clearFilter">
              <i class="fas fa-times"></i> Réinitialiser
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- Content -->
    <section class="content">
      <div class="container-fluid">
        <div class="card">
          <!-- Card Header -->
          <div class="card-header d-flex align-items-center">
            <h3 class="card-title mb-0">
              Liste des laboratoires
              <span v-if="selectedDepartementId" class="badge badge-info ml-2">
                Filtrés par : {{ getDepartementNom(selectedDepartementId) }}
              </span>
            </h3>
            <button class="btn btn-success btn-sm ml-auto" @click="openModal()">Ajouter</button>
          </div>

          <!-- Card Body -->
          <div class="card-body table-responsive">
            <table class="table table-bordered table-hover">
              <thead class="thead-light">
                <tr>
                  <th>Numéro</th>
                  <th>Nom du labo</th>
                  <th>Département</th>
                  <th>État</th>
                  <th class="text-center" width="180">Consulter</th>
                  <th class="text-center" width="180">Gestion</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(labo, index) in labosFiltres" :key="labo.id">
                  <td>{{ index + 1 }}</td>
                  <td>{{ labo.nomLabo }}</td>
                  <td>{{ labo.departement?.nom || '' }}</td>
                  <td>
                    <span :class="labo.etatLabo === 'ACTIF' ? 'badge badge-success' : 'badge badge-secondary'">
                      {{ labo.etatLabo }}
                    </span>
                  </td>
                  <!-- Colonne Consulter -->
                  <td class="text-center">
                    <div class="d-flex flex-column align-items-center">
                      <button class="btn btn-info btn-sm btn-block mb-1" @click="voirReservations(labo)" title="Voir réservations">
                        <i class="fas fa-calendar-check mr-1"></i> Réservations
                      </button>
                      <button class="btn btn-warning btn-sm btn-block" @click="voirReclamations(labo)" title="Voir réclamations">
                        <i class="fas fa-exclamation-triangle mr-1"></i> Réclamations
                      </button>
                    </div>
                  </td>
                  <!-- Colonne Gestion -->
                  <td class="text-center">
                    <div class="d-flex flex-column align-items-center">
                      <button class="btn btn-primary btn-sm btn-block mb-1" @click="openModal(labo)">
                        <i class="fas fa-edit mr-1"></i> Modifier
                      </button>
                      <button class="btn btn-danger btn-sm btn-block" @click="handleDelete(labo.id)">
                        <i class="fas fa-trash mr-1"></i> Supprimer
                      </button>
                    </div>
                  </td>
                </tr>
                <tr v-if="labosFiltres.length === 0">
                  <td colspan="6" class="text-center text-muted">Aucun laboratoire trouvé</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </section>

    <!-- Modal -->
    <div class="modal fade" :class="{ show: showModal }" :style="{ display: showModal ? 'block' : 'none' }">
      <div class="modal-dialog">
        <div class="modal-content">
          <form @submit.prevent="handleSubmit">
            <div class="modal-header">
              <h5 class="modal-title">{{ isEdit ? 'Modifier le laboratoire' : 'Ajouter un laboratoire' }}</h5>
              <button type="button" class="close" @click="closeModal">×</button>
            </div>

            <div class="modal-body">
              <div class="form-group">
                <label>Nom du labo</label>
                <input type="text" class="form-control" v-model="form.nomLabo" required />
              </div>
              <div class="form-group">
                <label>Département</label>
                <select class="form-control" v-model="form.departementId" required>
                  <option value="">-- Sélectionner --</option>
                  <option v-for="d in departements" :key="d.id" :value="d.id">{{ d.nom }}</option>
                </select>
              </div>
              <div class="form-group">
                <label>État</label>
                <select class="form-control" v-model="form.etatLabo" required>
                  <option value="">-- Sélectionner --</option>
                  <option value="ACTIF">Actif</option>
                  <option value="INACTIF">Non actif</option>
                </select>
              </div>
            </div>

            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" @click="closeModal">Annuler</button>
              <button type="submit" class="btn btn-success">{{ isEdit ? 'Modifier' : 'Ajouter' }}</button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- Backdrop -->
    <div class="modal-backdrop fade show" v-if="showModal"></div>

    <!-- Alert messages -->
    <div v-if="alertMessage" :class="`alert mt-2 ${alertType}`" role="alert">
      {{ alertMessage }}
      <button type="button" class="close" @click="alertMessage = ''">×</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import type { Laboratoire, Departement } from '@/types'
import { getLaboratoires, createLaboratoire, updateLaboratoire, deleteLaboratoire } from '@/Service/LaboratoireService'
import { getDepartements } from '@/Service/departementService'

const router = useRouter()
const route = useRoute()
const laboratoires = ref<Laboratoire[]>([])
const departements = ref<Departement[]>([])
const selectedDepartementId = ref<number | ''>('')

const showModal = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)

const alertMessage = ref('')
const alertType = ref('alert-success')

const form = reactive({
  nomLabo: '',
  departementId: '' as number | '',
  etatLabo: ''
})

onMounted(async () => {
  await fetchLaboratoires()
  await fetchDepartements()
  
  // Si on arrive avec un paramètre departementId dans l'URL
  if (route.query.departementId) {
    selectedDepartementId.value = Number(route.query.departementId)
  }
})

// Computed pour filtrer et trier les laboratoires (les plus récents en premier)
const labosFiltres = computed(() => {
  let result = laboratoires.value
  if (selectedDepartementId.value) {
    result = result.filter(l => l.departement?.id === selectedDepartementId.value)
  }
  return [...result].sort((a, b) => b.id - a.id)
})

// Récupérer le nom du département
function getDepartementNom(id: number | '') {
  if (!id) return ''
  const dept = departements.value.find(d => d.id === id)
  return dept?.nom || ''
}

// Réinitialiser le filtre
function clearFilter() {
  selectedDepartementId.value = ''
  router.replace({ path: '/technicien/laboratoires' })
}

async function fetchLaboratoires() {
  try {
    const res = await getLaboratoires()
    laboratoires.value = res.data
  } catch (err) {
    console.error('Erreur fetchLaboratoires:', err)
    showAlert('Impossible de récupérer les laboratoires', 'alert-danger')
  }
}

async function fetchDepartements() {
  try {
    const res = await getDepartements()
    departements.value = res.data
  } catch (err) {
    console.error('Erreur fetchDepartements:', err)
    showAlert('Impossible de récupérer les départements', 'alert-danger')
  }
}

function openModal(labo?: Laboratoire) {
  if (labo) {
    isEdit.value = true
    editId.value = labo.id
    form.nomLabo = labo.nomLabo ?? ''
    form.departementId = labo.departement?.id || ''
    form.etatLabo = labo.etatLabo ?? ''
  } else {
    isEdit.value = false
    editId.value = null
    form.nomLabo = ''
    form.departementId = ''
    form.etatLabo = ''
  }
  showModal.value = true
}

function closeModal() {
  showModal.value = false
}

async function handleSubmit() {
  if (!form.nomLabo || !form.departementId || !form.etatLabo) {
    showAlert('Tous les champs sont obligatoires !', 'alert-warning')
    return
  }

  const data = {
    nomLabo: form.nomLabo,
    departement: { id: Number(form.departementId) },
    etatLabo: form.etatLabo
  }

  try {
    if (isEdit.value && editId.value) {
      await updateLaboratoire(editId.value, data)
      showAlert('Laboratoire modifié avec succès !', 'alert-success')
    } else {
      await createLaboratoire(data)
      showAlert('Laboratoire ajouté avec succès !', 'alert-success')
    }
    await fetchLaboratoires()
    closeModal()
  } catch (err: any) {
    console.error('Erreur handleSubmit:', err)
    showAlert(err?.response?.data || 'Une erreur est survenue', 'alert-danger')
  }
}

async function handleDelete(id: number) {
  if (!confirm('Supprimer ce laboratoire ?')) return
  try {
    await deleteLaboratoire(id)
    laboratoires.value = laboratoires.value.filter(labo => labo.id !== id)
    showAlert('Laboratoire supprimé avec succès !', 'alert-success')
  } catch (err: any) {
    console.error('Erreur handleDelete:', err)
    showAlert(err?.response?.data || 'Impossible de supprimer ce laboratoire', 'alert-danger')
  }
}

function showAlert(message: string, type: string) {
  alertMessage.value = message
  alertType.value = type
  setTimeout(() => alertMessage.value = '', 5000) // disparaît après 5s
}

// Navigation vers les réservations filtrées par laboratoire
function voirReservations(labo: Laboratoire) {
  router.push({ 
    path: '/technicien/reservations', 
    query: { laboratoireId: labo.id, laboratoireNom: labo.nomLabo } 
  })
}

// Navigation vers les réclamations filtrées par laboratoire
function voirReclamations(labo: Laboratoire) {
  router.push({ 
    path: '/technicien/reclamations', 
    query: { laboratoireId: labo.id, laboratoireNom: labo.nomLabo } 
  })
}
</script>

<style scoped>
.modal.show { background-color: rgba(0,0,0,0.5); }
.alert { position: fixed; top: 70px; right: 20px; z-index: 1050; min-width: 250px; }
.close { background: none; border: none; font-size: 1.2rem; line-height: 1; float: right; }
</style>
