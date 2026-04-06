<template>
  <div>
    <!-- Header -->
    <section class="content-header">
      <div class="container-fluid">
        <div class="row mb-2">
          <div class="col-sm-6">
            <h1><i class="fas fa-exclamation-triangle mr-2"></i> Nouvelle Réclamation</h1>
          </div>
          <div class="col-sm-6">
            <router-link to="/etudiant/reclamations" class="btn btn-secondary float-right">
              <i class="fas fa-arrow-left mr-1"></i> Retour
            </router-link>
          </div>
        </div>
      </div>
    </section>

    <!-- Contenu -->
    <section class="content">
      <div class="container-fluid">
        <div class="row">
          <div class="col-md-8 offset-md-2">
            <div class="card card-warning">
              <div class="card-header">
                <h3 class="card-title">
                  <i class="fas fa-tools mr-2"></i>
                  Signaler un problème d'équipement
                </h3>
              </div>

              <form @submit.prevent="handleSubmit">
                <div class="card-body">
                  <!-- Alerte info -->
                  <div class="alert alert-info">
                    <i class="fas fa-info-circle mr-2"></i>
                    <strong>Information :</strong> Votre réclamation sera envoyée au technicien responsable du laboratoire concerné.
                  </div>

                  <!-- Département -->
                  <div class="form-group">
                    <label for="departement">
                      <i class="fas fa-building mr-1"></i> Département <span class="text-danger">*</span>
                    </label>
                    <select
                      id="departement"
                      class="form-control"
                      v-model="selectedDepartementId"
                      @change="onDepartementChange"
                      required
                    >
                      <option disabled value="">Sélectionnez un département</option>
                      <option v-for="dept in departements" :key="dept.id" :value="dept.id">
                        {{ dept.nom }}
                      </option>
                    </select>
                  </div>

                  <!-- Laboratoire (seuls les labos ACTIFS sont affichés) -->
                  <div class="form-group">
                    <label for="laboratoire">
                      <i class="fas fa-flask mr-1"></i> Laboratoire <span class="text-danger">*</span>
                    </label>
                    <select 
                      id="laboratoire"
                      class="form-control" 
                      v-model="selectedLaboId" 
                      @change="loadEquipements"
                      :disabled="!selectedDepartementId"
                      required
                    >
                      <option disabled value="">
                        {{ !selectedDepartementId ? 'Sélectionnez d\'abord un département' : 'Sélectionnez un laboratoire' }}
                      </option>
                      <option v-for="labo in laboratoiresActifs" :key="labo.id" :value="labo.id">
                        {{ labo.nomLabo || labo.nom }}
                      </option>
                    </select>
                    <small class="text-muted" v-if="selectedDepartementId && laboratoiresInactifs > 0">
                      <i class="fas fa-info-circle mr-1"></i>
                      {{ laboratoiresInactifs }} laboratoire(s) inactif(s) masqué(s)
                    </small>
                  </div>

                  <!-- Équipement -->
                  <div class="form-group">
                    <label for="equipement">
                      <i class="fas fa-cogs mr-1"></i> Équipement <span class="text-danger">*</span>
                    </label>
                    <select 
                      id="equipement"
                      class="form-control" 
                      v-model="selectedEquipementId"
                      :disabled="!selectedLaboId || equipements.length === 0"
                      required
                    >
                      <option disabled value="">
                        {{ !selectedLaboId ? 'Sélectionnez d\'abord un laboratoire' : (equipements.length === 0 ? 'Aucun équipement disponible' : 'Sélectionnez un équipement') }}
                      </option>
                      <option v-for="eq in equipementsReclamables" :key="eq.id" :value="eq.id">
                        {{ eq.nom }} 
                        <span v-if="eq.identifiant">({{ eq.identifiant }})</span>
                        - {{ formatEtat(eq.etat) }}
                      </option>
                    </select>
                  </div>

                  <!-- Titre -->
                  <div class="form-group">
                    <label for="titre">
                      <i class="fas fa-heading mr-1"></i> Titre de la réclamation <span class="text-danger">*</span>
                    </label>
                    <input
                      id="titre"
                      type="text"
                      class="form-control"
                      v-model="titre"
                      placeholder="Ex: PC ne démarre plus"
                      required
                    />
                  </div>

                  <!-- Description -->
                  <div class="form-group">
                    <label for="description">
                      <i class="fas fa-align-left mr-1"></i> Description du problème <span class="text-danger">*</span>
                    </label>
                    <textarea 
                      id="description"
                      class="form-control" 
                      v-model="description" 
                      rows="4"
                      placeholder="Décrivez le problème rencontré avec l'équipement (ex: l'écran ne s'allume pas, bruit anormal, etc.)"
                      required
                    ></textarea>
                  </div>

                  <!-- Priorité -->
                  <div class="form-group">
                    <label for="priorite">
                      <i class="fas fa-flag mr-1"></i> Priorité
                    </label>
                    <select id="priorite" class="form-control" v-model="priorite">
                      <option value="BASSE">Basse - Peut attendre</option>
                      <option value="MOYENNE">Moyenne - À traiter dans les prochains jours</option>
                      <option value="HAUTE">Haute - Urgent</option>
                    </select>
                  </div>

                </div>

                <div class="card-footer">
                  <button type="submit" class="btn btn-warning" :disabled="isSubmitting">
                    <span v-if="isSubmitting">
                      <i class="fas fa-spinner fa-spin mr-1"></i> Envoi en cours...
                    </span>
                    <span v-else>
                      <i class="fas fa-paper-plane mr-1"></i> Envoyer la réclamation
                    </span>
                  </button>
                  <router-link to="/etudiant/reclamations" class="btn btn-secondary ml-2">
                    Annuler
                  </router-link>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getLaboratoires } from '@/Service/LaboratoireService'
import { getDepartements } from '@/Service/departementService'
import ReclamationService from '@/Service/ReclamationService'

interface Laboratoire { id: number; nomLabo: string; nom?: string }
interface Equipement { id: number; nom: string; etat?: string; identifiant?: string }
interface Departement { id: number; nom: string }

const route = useRoute()
const router = useRouter()

const departements = ref<Departement[]>([])
const laboratoires = ref<Laboratoire[]>([])
const equipements = ref<Equipement[]>([])
const selectedDepartementId = ref<number | "">("")
const selectedLaboId = ref<number | "">("")
const selectedEquipementId = ref<number | "">("")
const titre = ref<string>("")
const description = ref<string>("")
const priorite = ref<string>("MOYENNE")
const isSubmitting = ref(false)

function isLaboActif(labo: any): boolean {
  // Spring/Front: etatLabo = 'ACTIF'|'INACTIF'
  if (typeof labo?.etatLabo === 'string') {
    return labo.etatLabo === 'ACTIF'
  }
  // Mock backend: etat = 'DISPONIBLE'|'OCCUPE'|'EN_MAINTENANCE'...
  if (typeof labo?.etat === 'string') {
    return !['INACTIF', 'INACTIVE'].includes(labo.etat)
  }
  return true
}

// Filtrer les laboratoires actifs
const laboratoiresActifs = computed(() => {
  if (!selectedDepartementId.value) return []
  return laboratoires.value.filter(l => {
    const laboAny = l as any
    const laboDeptId = laboAny.departementId || laboAny.departement?.id
    return laboDeptId === selectedDepartementId.value && isLaboActif(laboAny)
  })
})

// Compter les labos inactifs masqués
const laboratoiresInactifs = computed(() => {
  if (!selectedDepartementId.value) return 0
  return laboratoires.value.filter(l => {
    const laboAny = l as any
    const laboDeptId = laboAny.departementId || laboAny.departement?.id
    return laboDeptId === selectedDepartementId.value && !isLaboActif(laboAny)
  }).length
})

// Tous les équipements sont réclamables
const equipementsReclamables = computed(() => equipements.value)

// Plus besoin de compter les équipements fonctionnels masqués
const equipementsFonctionnels = computed(() => [] as any[])

// Format état
function formatEtat(etat?: string): string {
  if (!etat) return ''
  const labels: Record<string, string> = {
    FONCTIONNEL: 'Fonctionnel',
    EN_PANNE: 'En panne',
    EN_MAINTENANCE: 'En maintenance'
  }
  return labels[etat] || etat
}

async function loadDepartements() {
  try {
    const res = await getDepartements()
    departements.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
    console.error("Erreur chargement départements:", e)
    departements.value = []
  }
}

// Charger tous les laboratoires
async function loadLaboratoires() {
  try {
    const res = await getLaboratoires()
    laboratoires.value = Array.isArray(res.data) ? res.data : []
    console.log("Laboratoires chargés:", laboratoires.value)
    
    // Si on arrive avec un laboratoireId dans l'URL, pré-sélectionner département et labo
    if (route.query.laboratoireId) {
      const laboId = Number(route.query.laboratoireId)
      const labo = laboratoires.value.find(l => l.id === laboId) as any
      const deptId = labo?.departementId || labo?.departement?.id
      if (deptId) {
        selectedDepartementId.value = deptId
      }
      selectedLaboId.value = laboId
      await loadEquipements()
      
      // Si on a aussi un equipementId, le pré-sélectionner
      if (route.query.equipementId) {
        selectedEquipementId.value = Number(route.query.equipementId)
      }
    }
  } catch (e) {
    console.error("Erreur chargement données:", e)
  }
}

function onDepartementChange() {
  selectedLaboId.value = ""
  selectedEquipementId.value = ""
  equipements.value = []
}

// Charger les équipements du labo sélectionné
async function loadEquipements() {
  if (!selectedLaboId.value) {
    equipements.value = []
    return
  }
  try {
    // Spring: /api/reclamations/by-labo/{laboId}
    const res = await ReclamationService.getEquipementsByLaboReclamation(Number(selectedLaboId.value))
    equipements.value = Array.isArray(res.data) ? res.data : []
    selectedEquipementId.value = "" // Reset la sélection
    console.log(`Équipements du labo ${selectedLaboId.value}:`, equipements.value)
  } catch (e) {
    console.error("Erreur chargement équipements:", e)
    equipements.value = []
  }
}

// Envoyer la réclamation
async function handleSubmit() {
  if (!selectedDepartementId.value || !selectedLaboId.value || !selectedEquipementId.value || !titre.value || !description.value) {
    alert("Veuillez remplir tous les champs obligatoires")
    return
  }

  isSubmitting.value = true

  const payload = {
    departementId: Number(selectedDepartementId.value),
    description: description.value,
    titre: titre.value,
    laboratoireId: Number(selectedLaboId.value),
    equipementId: Number(selectedEquipementId.value),
    quantite: 1,
    priorite: priorite.value
  }

  try {
    await ReclamationService.createReclamation(payload)
    alert("✅ Réclamation envoyée avec succès !")
    router.push('/etudiant/reclamations')
  } catch (e: any) {
    console.error("Erreur envoi réclamation:", e)
    const backendMessage = typeof e.response?.data === 'string'
      ? e.response.data
      : (e.response?.data?.message || e.message)
    alert("❌ Erreur lors de l'envoi de la réclamation: " + backendMessage)
  } finally {
    isSubmitting.value = false
  }
}

onMounted(() => {
  loadDepartements()
  loadLaboratoires()
})
</script>

<style scoped>
.card-warning .card-header {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  color: white;
}

.form-control:focus {
  border-color: #f59e0b;
  box-shadow: 0 0 0 0.2rem rgba(245, 158, 11, 0.25);
}

.btn-warning {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  border: none;
}

.btn-warning:hover:not(:disabled) {
  background: linear-gradient(135deg, #d97706 0%, #b45309 100%);
  transform: translateY(-1px);
}

.alert-info {
  background: #e0f2fe;
  border-color: #0ea5e9;
  color: #0369a1;
}
</style>
