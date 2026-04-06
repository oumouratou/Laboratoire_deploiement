<template>
  <div class="page-container">
    <section class="content-header">
      <div class="container-fluid">
        <h1><i class="fas fa-tools mr-2"></i> Mes Réclamations</h1>
        <p>Suivi des réclamations que vous avez envoyées</p>
      </div>
    </section>

    <section class="content">
      <div class="container-fluid">
        <div class="card">
          <div class="card-header">
            <h3 class="card-title">Liste de mes réclamations</h3>
          </div>

          <div class="card-body table-responsive">
            <table class="table table-bordered table-striped table-hover" v-if="reclamations.length">
              <thead class="bg-info text-white">
                <tr>
                  <th>Numéro</th>
                  <th>Laboratoire</th>
                  <th>Équipement</th>
                  <th>Description</th>
                  <th>Identifiant équipement</th>
                  <th>Priorité</th>
                  <th>État</th>
                  <th>Date</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                <tr 
                  v-for="(rec, index) in reclamations" 
                  :key="rec.id"
                  :class="{ 'highlight-row': highlightedId === rec.id }"
                  :ref="el => { if (highlightedId === rec.id) highlightedRow = el }"
                >
                  <td>{{ index + 1 }}</td>
                  <td>{{ rec.laboratoireNom || 'N/A' }}</td>
                  <td>{{ rec.equipementNom || 'N/A' }}</td>
                  <td>{{ rec.description }}</td>
                  <td>{{ getEquipementIdentifiant(rec) }}</td>
                  <td>
                    <span :class="getPrioriteBadge(getPriorite(rec))">
                      {{ formatPriorite(getPriorite(rec)) }}
                    </span>
                  </td>
                  <td>
                    <span :class="getEtatBadge(rec.etat)">
                      {{ formatEtat(rec.etat) }}
                    </span>
                  </td>
                  <td>{{ formatDate(rec.dateReclamation) }}</td>
                  <td class="d-flex gap-1">
                    <button
                      class="btn btn-warning btn-sm"
                      @click="openModal(rec)"
                      :disabled="isActionDisabled(rec)"
                      :title="getActionDisabledReason(rec)"
                    >
                      <i class="fas fa-edit mr-1"></i> Modifier
                    </button>
                    <button
                      class="btn btn-danger btn-sm"
                      @click="supprimerReclamation(rec)"
                      :disabled="isActionDisabled(rec)"
                      :title="getActionDisabledReason(rec)"
                    >
                      <i class="fas fa-trash mr-1"></i> Supprimer
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>

            <div v-else class="text-center text-muted p-4">
              <i class="fas fa-inbox fa-3x mb-3"></i>
              <p>Vous n'avez aucune réclamation pour le moment</p>
            </div>
          </div>

          <div class="card-footer">
            <span>{{ reclamations.length }} réclamation(s)</span>
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
              <h5 class="modal-title">Modifier la réclamation</h5>
              <button type="button" class="close" @click="closeModal">×</button>
            </div>

            <div class="modal-body">
              <div class="form-group">
                <label>Description</label>
                <textarea class="form-control" v-model="form.description" required></textarea>
              </div>
            </div>

            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" @click="closeModal">Annuler</button>
              <button type="submit" class="btn btn-success">Enregistrer</button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <div class="modal-backdrop fade show" v-if="showModal"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { getEquipements } from '@/Service/EquipementService'
import ReclamationService from '@/Service/ReclamationService'

const route = useRoute()

interface Reclamation {
  id: number
  titre: string
  description: string
  quantite: number
  priorite?: string
  etat: string
  motifRefus?: string
  dateReclamation: string
  laboratoireNom?: string
  equipementNom?: string
}

const reclamations = ref<Reclamation[]>([])
const equipementsById = ref<Record<number, any>>({})
const showModal = ref(false)
const form = reactive({ id: 0, description: '', quantite: 1 })
const highlightedId = ref<number | null>(null)
const highlightedRow = ref<any>(null)

// 🔹 Charger les réclamations de l'étudiant connecté (triées par date décroissante)
async function fetchReclamations() {
  try {
    const [reclamationsRes, equipementsRes] = await Promise.all([
      ReclamationService.getReclamationsEtudiantConnecte(),
      getEquipements()
    ])

    const allEquipements = Array.isArray(equipementsRes.data) ? equipementsRes.data : []
    equipementsById.value = allEquipements.reduce((acc: Record<number, any>, eq: any) => {
      const id = Number(eq?.id)
      if (Number.isFinite(id) && id > 0) acc[id] = eq
      return acc
    }, {})

    const res = reclamationsRes
    const data = Array.isArray(res.data) ? res.data : []
    // Trier par date décroissante (plus récent en premier)
    reclamations.value = data.sort((a: Reclamation, b: Reclamation) => {
      const dateA = new Date(a.dateReclamation || 0).getTime()
      const dateB = new Date(b.dateReclamation || 0).getTime()
      return dateB - dateA
    })
  } catch (error) {
    console.error('Erreur chargement réclamations:', error)
    reclamations.value = []
  }
}

// 🔹 Ouvrir le modal pour modifier une réclamation
function openModal(rec: Reclamation) {
  if (isActionDisabled(rec)) {
    alert(getActionDisabledReason(rec))
    return
  }

  form.id = rec.id
  form.description = rec.description
  form.quantite = rec.quantite
  showModal.value = true
}

// 🔹 Fermer le modal
function closeModal() { showModal.value = false }

// 🔹 Enregistrer modification
async function handleSubmit() {
  try {
    console.log(`Modification réclamation #${form.id}...`);
    await ReclamationService.updateReclamation(form.id, {
      description: form.description,
      quantite: form.quantite
    });
    alert("✅ Réclamation modifiée avec succès !");
    await fetchReclamations();
    closeModal();
  } catch (error: any) {
    console.error('Erreur modification:', error);
    alert("❌ Erreur: " + (error.response?.data?.message || error.response?.data || error.message));
  }
}

// 🔹 Annuler réclamation (par l'étudiant lui-même - sans notification)
async function supprimerReclamation(rec: Reclamation) {
  if (isActionDisabled(rec)) {
    alert(getActionDisabledReason(rec))
    return
  }

  if (!confirm("Voulez-vous supprimer cette réclamation ?")) return
  try {
    await ReclamationService.deleteReclamation(rec.id)
    alert("✅ Réclamation supprimée.")
    await fetchReclamations()
  } catch (error: any) {
    console.error('Erreur annulation:', error)
    alert("❌ Erreur: " + (error.response?.data?.message || error.message))
  }
}

function isActionDisabled(rec: Reclamation): boolean {
  return rec.etat !== 'NON_TRAITEE'
}

function getActionDisabledReason(rec: Reclamation): string {
  if (rec.etat === 'TRAITEE') return 'Réclamation déjà traitée'
  if (rec.etat === 'ANNULEE') return 'Réclamation déjà annulée'
  if (rec.etat === 'REFUSEE') return 'Réclamation déjà refusée'
  return rec.etat === 'NON_TRAITEE' ? '' : 'Action indisponible'
}

// 🔹 Récupérer la priorité (gérer différents noms de champs)
function getPriorite(rec: Reclamation): string {
  const priorite = rec.priorite || (rec as any).priority || (rec as any).prioriteReclamation
  // Normaliser en majuscules
  if (priorite) {
    const upper = String(priorite).toUpperCase()
    if (['HAUTE', 'HIGH', 'URGENTE'].includes(upper)) return 'HAUTE'
    if (['BASSE', 'LOW', 'FAIBLE'].includes(upper)) return 'BASSE'
    return 'MOYENNE'
  }
  return 'MOYENNE'
}

function getEquipementIdentifiant(rec: Reclamation): string {
  const recAny = rec as any
  const equipementId = Number(recAny.equipementId || recAny.equipement?.id)
  const mappedEquipement = Number.isFinite(equipementId) ? equipementsById.value[equipementId] : null
  return recAny.equipementIdentifiant
    || recAny.identifiantEquipement
    || recAny.identifiant
    || mappedEquipement?.identifiant
    || recAny.equipement?.identifiant
    || ''
}

// 🔹 Styles badge pour priorité
function getPrioriteBadge(priorite?: string) {
  return {
    HAUTE: 'badge badge-danger',
    MOYENNE: 'badge badge-warning',
    BASSE: 'badge badge-info'
  }[priorite || 'MOYENNE'] || 'badge badge-secondary'
}

// 🔹 Texte lisible pour la priorité
function formatPriorite(priorite?: string) {
  return {
    HAUTE: 'Haute',
    MOYENNE: 'Moyenne',
    BASSE: 'Basse'
  }[priorite || 'MOYENNE'] || priorite || 'Moyenne'
}

// 🔹 Styles badge
function getEtatBadge(etat: string) {
  return {
    NON_TRAITEE: 'badge badge-warning',
    TRAITEE: 'badge badge-success',
    ANNULEE: 'badge badge-secondary',
    REFUSEE: 'badge badge-danger'
  }[etat] || 'badge badge-secondary'
}

// 🔹 Texte lisible pour l'état
function formatEtat(etat: string) {
  return {
    NON_TRAITEE: 'Non traitée',
    TRAITEE: 'Traitée',
    ANNULEE: 'Annulée',
    REFUSEE: 'Refusée'
  }[etat] || etat
}

// 🔹 Format date
function formatDate(date: string) {
  return date ? date.split('T')[0] : ''
}

onMounted(async () => {
  await fetchReclamations()
  
  // Vérifier si on doit highlight une réclamation (venant d'une notification)
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
.page-container { max-width: 1400px; margin: 0 auto; padding: 20px; }
.card { background: #fff; border-radius: 16px; box-shadow: 0 2px 6px rgba(0,0,0,0.08); overflow: hidden; margin-bottom: 20px; }
.card-header { padding: 20px; font-weight: 600; font-size: 18px; }
.table th, .table td { vertical-align: middle; }
.badge { padding: 5px 10px; border-radius: 4px; font-size: 12px; text-transform: uppercase; }
.modal.show { background-color: rgba(0,0,0,0.5); }
.d-flex.gap-1 button { margin-right: 4px; }
.card-footer { font-size: 14px; color: #64748b; padding: 12px 20px; }

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
