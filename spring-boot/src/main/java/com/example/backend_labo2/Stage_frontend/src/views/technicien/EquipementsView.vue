<template>
  <div>
    <!-- En-tête -->
    <section class="content-header">
      <div class="container-fluid">
        <div class="row mb-2">
          <div class="col-sm-6">
            <h1><i class="fas fa-cogs mr-2"></i> Gestion des Équipements</h1>
          </div>
          <div class="col-sm-6 text-right">
            <button class="btn btn-success" @click="openModal()">
              <i class="fas fa-plus mr-1"></i> Ajouter un équipement
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- Tableau des équipements -->
    <section class="content">
      <div class="container-fluid">
        <div class="card">
          <div class="card-body table-responsive">
            <table class="table table-bordered table-striped table-hover">
              <thead class="bg-success text-white">
                <tr>
                  <th>Numéro</th>
                  <th>Identifiant</th>
                  <th>Nom</th>
                  <th>Caractéristique</th>
                  <th>Laboratoire</th>
                  <th>État</th>
                  <th>Date acquisition</th>
                  <th class="text-center">Actions</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(equip, index) in equipementsTries" :key="equip.id">
                  <td>{{ index + 1 }}</td>
                  <td>
                    <code class="text-primary">{{ equip.identifiant || generateIdentifiant(equip) }}</code>
                  </td>
                  <td>{{ equip.nom }}</td>
                  <td>{{ truncateText(equip.caracteristique, 30) }}</td>
                  <td>{{ equip.laboratoire?.nomLabo || 'N/A' }}</td>
                  <td>
                    <span :class="getEtatBadge(equip.etat)">
                      {{ formatEtat(equip.etat) }}
                    </span>
                    <!-- Indicateur de réclamation en cours -->
                    <span v-if="equip.hasReclamationEnCours" class="badge badge-warning ml-1" title="Réclamation en cours">
                      <i class="fas fa-tools"></i>
                    </span>
                  </td>
                  <td>{{ equip.dateAcquisition }}</td>
                  <!-- Colonne Actions -->
                  <td class="text-center">
                    <div class="btn-group-vertical btn-group-sm">
                      <button class="btn btn-warning btn-sm mb-1" @click="voirReclamations(equip)">
                        <i class="fas fa-exclamation-triangle mr-1"></i> Réclamations
                      </button>
                      <button class="btn btn-primary btn-sm mb-1" @click="openModal(equip)">
                        <i class="fas fa-edit mr-1"></i> Modifier
                      </button>
                      <button class="btn btn-danger btn-sm" @click="handleDelete(equip.id)">
                        <i class="fas fa-trash mr-1"></i> Supprimer
                      </button>
                    </div>
                  </td>
                </tr>
                <tr v-if="equipements.length === 0">
                  <td colspan="8" class="text-center">Aucun équipement disponible</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </section>

    <!-- Modal Ajouter / Modifier -->
    <div
      class="modal fade"
      :class="{ show: showModal }"
      :style="{ display: showModal ? 'block' : 'none', paddingRight: '0px' }"
      tabindex="-1"
      role="dialog"
    >
      <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content">
          <div class="modal-header bg-success">
            <h5 class="modal-title">{{ isEdit ? 'Modifier' : 'Ajouter' }} un équipement</h5>
            <button type="button" class="close" @click="closeModal">&times;</button>
          </div>
          <form @submit.prevent="handleSubmit">
            <div class="modal-body" style="max-height: 70vh; overflow-y: auto;">
              <!-- Alerte si équipement existe déjà -->
              <div v-if="duplicateWarning" class="alert alert-warning">
                <i class="fas fa-exclamation-triangle mr-2"></i>
                {{ duplicateWarning }}
              </div>

              <!-- Département (sélection obligatoire avant le labo) -->
              <div class="form-group">
                <label><i class="fas fa-building mr-1"></i> Département <span class="text-danger">*</span></label>
                <select class="form-control" v-model="form.departementId" @change="onDepartementChange" required>
                  <option value="">-- Sélectionner un département --</option>
                  <option v-for="dept in departements" :key="dept.id" :value="dept.id">
                    {{ dept.nom }}
                  </option>
                </select>
              </div>

              <div class="form-group">
                <label>Identifiant unique</label>
                <input type="text" class="form-control" v-model="form.identifiant" placeholder="Ex: PC-001, MICRO-042" />
                <small class="text-muted">Laissez vide pour générer automatiquement</small>
              </div>

              <div class="form-group">
                <label>Nom</label>
                <input type="text" class="form-control" v-model="form.nom" ref="nomInput" required @blur="checkDuplicate" />
              </div>

              <div class="form-group">
                <label>Caractéristique</label>
                <textarea class="form-control" v-model="form.caracteristique" required></textarea>
              </div>

              <div class="form-group">
                <label><i class="fas fa-flask mr-1"></i> Laboratoire <span class="text-danger">*</span></label>
                <select class="form-control" v-model="form.laboratoireId" :disabled="!form.departementId" required>
                  <option value="">{{ !form.departementId ? '-- Sélectionnez d\'abord un département --' : '-- Sélectionner un laboratoire --' }}</option>
                  <option v-for="labo in filteredLaboratoires" :key="labo.id" :value="labo.id">
                    {{ labo.nomLabo }}
                  </option>
                </select>
                <small class="text-muted" v-if="form.departementId && filteredLaboratoires.length === 0">
                  <i class="fas fa-info-circle mr-1"></i> Aucun laboratoire dans ce département
                </small>
              </div>

              <!-- État: affiché seulement en mode modification -->
              <div class="form-group" v-if="isEdit">
                <label>État</label>
                <select class="form-control" v-model="form.etat" required>
                  <option value="FONCTIONNEL">Fonctionnel</option>
                  <option value="EN_PANNE">En panne</option>
                  <option value="EN_MAINTENANCE">En maintenance</option>
                </select>
              </div>

              <!-- Info état par défaut pour l'ajout -->
              <div class="form-group" v-else>
                <label>État</label>
                <div class="form-control-plaintext">
                  <span class="badge badge-success">
                    <i class="fas fa-check-circle mr-1"></i> Fonctionnel
                  </span>
                  <small class="text-muted d-block mt-1">
                    <i class="fas fa-info-circle mr-1"></i> Les nouveaux équipements sont automatiquement définis comme fonctionnels
                  </small>
                </div>
              </div>

              <div class="form-group">
                <label>Date acquisition</label>
                <input type="date" class="form-control" v-model="form.dateAcquisition" required />
              </div>

              <div class="form-group">
                <label>URL de l'image</label>
                <input 
                  type="text" 
                  class="form-control" 
                  v-model="form.imageUrl" 
                  placeholder="https://exemple.com/image.png" 
                />
                <small class="text-muted">
                  <i class="fas fa-info-circle mr-1"></i> Collez le lien de l'image
                </small>
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

    <!-- Backdrop Bootstrap -->
    <div class="modal-backdrop fade show" v-if="showModal"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getEquipements, createEquipement, updateEquipement, deleteEquipement } from '@/Service/EquipementService'
import { getLaboratoires } from '@/Service/LaboratoireService'
import { getDepartements } from '@/Service/departementService'
import ReclamationService from '@/Service/ReclamationService'

const router = useRouter()

const showModal = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)
const equipements = ref<any[]>([])
const laboratoires = ref<any[]>([])
const departements = ref<any[]>([])
const reclamations = ref<any[]>([])
const duplicateWarning = ref('')

// Laboratoires filtrés par département sélectionné
const filteredLaboratoires = computed(() => {
  if (!form.departementId) return []
  return laboratoires.value.filter(labo => 
    labo.departement?.id === Number(form.departementId) || 
    labo.departementId === Number(form.departementId)
  )
})

// Équipements triés par ID décroissant (les plus récents en premier)
const equipementsTries = computed(() => {
  return [...equipements.value].sort((a, b) => b.id - a.id)
})

const form = reactive({
  nom: '',
  identifiant: '',
  caracteristique: '',
  quantite: 1,
  departementId: '' as string | number,
  laboratoireId: '',
  etat: 'FONCTIONNEL',
  dateAcquisition: new Date().toISOString().split('T')[0],
  imageUrl: ''
})

const nomInput = ref<HTMLInputElement | null>(null)

onMounted(async () => {
  await fetchDepartements()
  await fetchLaboratoires()
  await fetchReclamations()
  await fetchEquipements()
})

async function fetchDepartements() {
  try {
    const res = await getDepartements()
    departements.value = Array.isArray(res.data) ? res.data : []
    console.log('Départements chargés:', departements.value.length)
  } catch (err) {
    console.error('Erreur fetchDepartements:', err)
    departements.value = []
  }
}

// Quand le département change, reset le laboratoire
function onDepartementChange() {
  form.laboratoireId = ''
}

async function fetchLaboratoires() {
  try {
    const res = await getLaboratoires()
    laboratoires.value = res.data
  } catch (err) {
    console.error("Erreur fetchLaboratoires:", err)
    alert('Erreur lors du chargement des laboratoires')
  }
}

// Charger les réclamations pour compter par équipement
async function fetchReclamations() {
  try {
    const res = await ReclamationService.getReclamationsPourTechnicien()
    reclamations.value = Array.isArray(res.data) ? res.data : []
    console.log("Réclamations chargées:", reclamations.value.length)
  } catch (err) {
    console.error("Erreur chargement réclamations:", err)
    reclamations.value = []
  }
}

// Compter les réclamations pour un équipement
function countReclamationsForEquipement(equipId: number): number {
  return reclamations.value.filter(r => r.equipementId === equipId || r.equipement?.id === equipId).length
}

async function fetchEquipements() {
  try {
    const res = await getEquipements()
    console.log("Réponse API Equipements:", res.data)
    const dataArray = Array.isArray(res.data) ? res.data : []
    equipements.value = dataArray.map((e: any) => ({
      ...e,
      dateAcquisition: e.dateAcquisition ? e.dateAcquisition.split('T')[0] : '',
      reclamationsCount: countReclamationsForEquipement(e.id)
    }))
  } catch (err) {
    console.error("Erreur fetchEquipements:", err)
    alert("Erreur lors du chargement des équipements")
    equipements.value = []
  }
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

// Générer un identifiant unique pour un équipement
function generateIdentifiant(equip: any): string {
  const prefix = (equip.nom || 'EQ').substring(0, 3).toUpperCase()
  return `${prefix}-${String(equip.id).padStart(4, '0')}`
}

// Vérifier si un équipement avec le même nom existe déjà dans le même labo
function checkDuplicate() {
  if (!form.nom || !form.laboratoireId) {
    duplicateWarning.value = ''
    return
  }
  
  const existingEquip = equipements.value.find(e => 
    e.nom.toLowerCase() === form.nom.toLowerCase() &&
    e.laboratoire?.id === Number(form.laboratoireId) &&
    e.id !== editId.value
  )
  
  if (existingEquip) {
    duplicateWarning.value = `Un équipement "${existingEquip.nom}" existe déjà dans ce laboratoire (ID: ${existingEquip.identifiant || generateIdentifiant(existingEquip)})`
  } else {
    duplicateWarning.value = ''
  }
}

// Tronquer le texte
function truncateText(text: string, maxLength: number): string {
  if (!text) return ''
  return text.length > maxLength ? text.substring(0, maxLength) + '...' : text
}

function openModal(equip?: any) {
  duplicateWarning.value = ''
  if (equip) {
    isEdit.value = true
    editId.value = equip.id
    form.nom = equip.nom
    form.identifiant = equip.identifiant || ''
    form.caracteristique = equip.caracteristique
    form.quantite = equip.quantite
    // Récupérer le département du laboratoire pour pré-remplir
    const laboId = equip.laboratoire?.id
    const labo = laboratoires.value.find(l => l.id === laboId)
    form.departementId = labo?.departement?.id || labo?.departementId || ''
    form.laboratoireId = laboId || ''
    form.etat = equip.etat
    form.dateAcquisition = equip.dateAcquisition
    form.imageUrl = equip.imageUrl || ''
  } else {
    isEdit.value = false
    editId.value = null
    form.nom = ''
    form.identifiant = ''
    form.caracteristique = ''
    form.quantite = 1
    form.departementId = ''
    form.laboratoireId = ''
    form.etat = 'FONCTIONNEL'
    form.dateAcquisition = new Date().toISOString().split('T')[0]
    form.imageUrl = ''
  }

  showModal.value = true
  nextTick(() => nomInput.value?.focus())
}

function closeModal() {
  showModal.value = false
}

async function handleSubmit() {
  // Vérifier les doublons avant la soumission
  checkDuplicate()
  if (duplicateWarning.value && !confirm('Un équipement similaire existe déjà. Voulez-vous continuer ?')) {
    return
  }

  try {
    // Générer un identifiant unique si non fourni
    let identifiant = form.identifiant
    if (!identifiant) {
      const prefix = form.nom.substring(0, 3).toUpperCase().replace(/[^A-Z0-9]/g, 'X')
      const timestamp = Date.now().toString(36).toUpperCase()
      const random = Math.random().toString(36).substring(2, 6).toUpperCase()
      identifiant = `${prefix}-${timestamp}-${random}`
    }

    const data = {
      nom: form.nom,
      identifiant: identifiant,
      caracteristique: form.caracteristique,
      quantite: form.quantite,
      laboratoire: { id: Number(form.laboratoireId) },
      etat: form.etat,
      dateAcquisition: form.dateAcquisition,
      imageUrl: form.imageUrl
    }

    if (isEdit.value && editId.value) {
      await updateEquipement(editId.value, data)
    } else {
      await createEquipement(data)
    }

    await fetchEquipements()
    closeModal()
  } catch (err: any) {
    console.error("Erreur handleSubmit:", err)
    // Gérer l'erreur 409 (doublon)
    if (err.response?.status === 409) {
      const errorMsg = err.response?.data?.error || err.response?.data?.message || 'Un équipement avec cet identifiant ou ce nom existe déjà'
      alert('a️ Conflit : ' + errorMsg)
    } else {
      alert("Erreur lors de l'enregistrement : " + (err.response?.data?.error || err.response?.data?.message || err.message))
    }
  }
}

async function handleDelete(id: number) {
  if (confirm("Voulez-vous vraiment supprimer cet équipement ?")) {
    try {
      await deleteEquipement(id)
      await fetchEquipements()
    } catch (err) {
      console.error("Erreur handleDelete:", err)
      alert("Erreur lors de la suppression")
    }
  }
}

// Navigation vers les réclamations filtrées par équipement
function voirReclamations(equip: any) {
  router.push({ 
    path: '/technicien/reclamations', 
    query: { 
      equipementId: equip.id, 
      equipementNom: equip.nom,
      laboratoireId: equip.laboratoire?.id,
      laboratoireNom: equip.laboratoire?.nomLabo
    } 
  })
}
</script>

<style scoped>
.modal.show {
  display: block;
  background: rgba(0,0,0,0.5);
}
.modal-dialog {
  max-width: 700px;
}
.modal-body {
  max-height: 70vh;
  overflow-y: auto;
}
.badge {
  padding: 5px 10px;
  border-radius: 4px;
}
</style>
