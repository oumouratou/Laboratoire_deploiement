<template>
  <div>
    <!-- En-tête -->
    <section class="content-header">
      <div class="container-fluid">
        <div class="row mb-2">
          <div class="col-sm-6">
            <h1><i class="fas fa-exclamation-triangle mr-2"></i> Mes Réclamations</h1>
            <small class="text-muted">Suivi des réclamations que vous avez envoyées</small>
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
                <p>Non traitées</p>
              </div>
              <div class="icon"><i class="fas fa-clock"></i></div>
            </div>
          </div>

          <div class="col-lg-3 col-6">
            <div class="small-box bg-success">
              <div class="inner">
                <h3>{{ treatedCount }}</h3>
                <p>Traitées</p>
              </div>
              <div class="icon"><i class="fas fa-check"></i></div>
            </div>
          </div>

          <div class="col-lg-3 col-6">
            <div class="small-box bg-danger">
              <div class="inner">
                <h3>{{ refusedCount }}</h3>
                <p>Refusées</p>
              </div>
              <div class="icon"><i class="fas fa-ban"></i></div>
            </div>
          </div>

          <div class="col-lg-3 col-6">
            <div class="small-box bg-secondary">
              <div class="inner">
                <h3>{{ cancelledCount }}</h3>
                <p>Annulées</p>
              </div>
              <div class="icon"><i class="fas fa-times"></i></div>
            </div>
          </div>
        </div>

        <!-- Tableau -->
        <div class="card card-outline card-primary">
          <div class="card-header">
            <h3 class="card-title">
              <i class="fas fa-list mr-1"></i> Liste de mes réclamations
            </h3>
          </div>

          <div class="card-body table-responsive p-0">
            <table class="table table-hover text-nowrap" v-if="reclamations.length > 0">
              <thead class="thead-light">
                <tr>
                  <th>Numéro</th>
                  <th>Laboratoire</th>
                  <th>Équipement</th>
                  <th>Description</th>
                  <th>Identifiant équipement</th>
                  <th>Priorité</th>
                  <th>Date</th>
                  <th>Statut</th>
                  <th class="text-center">Actions</th>
                </tr>
              </thead>

              <tbody>
                <tr 
                  v-for="(rec, index) in reclamations" 
                  :key="rec.id"
                  :class="{ 'highlight-row': highlightedId === rec.id }"
                  :ref="el => { if (highlightedId === rec.id) highlightedRow = el }"
                >
                  <td><strong>{{ index + 1 }}</strong></td>
                  <td>{{ rec.laboratoireNom || 'N/A' }}</td>
                  <td>{{ rec.equipementNom || 'N/A' }}</td>
                  <td>{{ truncateText(rec.description, 50) }}</td>
                  <td>{{ getEquipementIdentifiant(rec) }}</td>
                  <td>
                    <span :class="getPrioriteBadge(getPriorite(rec))">
                      {{ formatPriorite(getPriorite(rec)) }}
                    </span>
                  </td>
                  <td>{{ formatDate(rec.dateReclamation) }}</td>
                  <td>
                    <span :class="getEtatBadge(rec.etat)">
                      {{ formatEtat(rec.etat) }}
                    </span>
                  </td>
                  <td class="text-center">
                    <div class="btn-group">
                      <button
                        class="btn btn-info btn-sm"
                        @click="openModal(rec)"
                        :disabled="isActionDisabled(rec)"
                        :title="getActionDisabledReason(rec) || 'Modifier'"
                      >
                        <i class="fas fa-edit mr-1"></i> Modifier
                      </button>
                      <button
                        class="btn btn-danger btn-sm"
                        @click="supprimerReclamation(rec)"
                        :disabled="isActionDisabled(rec)"
                        :title="getActionDisabledReason(rec) || 'Annuler'"
                      >
                        <i class="fas fa-times mr-1"></i> Annuler
                      </button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>

            <div v-else class="text-center text-muted p-5">
              <i class="fas fa-inbox fa-3x mb-3"></i>
              <p>Aucune réclamation trouvée.</p>
            </div>
          </div>

          <div class="card-footer clearfix">
            <span class="float-left">Total: {{ reclamations.length }} réclamation(s)</span>
          </div>
        </div>
      </div>
    </section>

    <!-- MODAL MODIFICATION -->
    <div class="modal fade" :class="{ show: showModal }" :style="{ display: showModal ? 'block' : 'none' }">
      <div class="modal-dialog">
        <div class="modal-content">
          <form @submit.prevent="handleSubmit">
            <div class="modal-header bg-info">
              <h5 class="modal-title">Modifier la réclamation</h5>
              <button type="button" class="close" @click="closeModal">
                <span>&times;</span>
              </button>
            </div>

            <div class="modal-body">
              <div class="form-group">
                <label>Description</label>
                <textarea class="form-control" v-model="form.description" rows="4" required></textarea>
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
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { getEquipements } from '@/Service/EquipementService'
import ReclamationService from '@/Service/ReclamationService'

const route = useRoute()
const reclamations = ref<any[]>([])
const equipementsById = ref<Record<number, any>>({})
const showModal = ref(false)
const form = reactive({ id: 0, description: '', quantite: 1 })
const highlightedId = ref<number | null>(null)
const highlightedRow = ref<any>(null)

// Compteurs pour les stats
const pendingCount = computed(() => reclamations.value.filter(r => r.etat === 'NON_TRAITEE').length)
const treatedCount = computed(() => reclamations.value.filter(r => r.etat === 'TRAITEE').length)
const refusedCount = computed(() => reclamations.value.filter(r => r.etat === 'REFUSEE').length)
const cancelledCount = computed(() => reclamations.value.filter(r => r.etat === 'ANNULEE').length)

async function fetchReclamations() {
  try {
    const [reclamationsRes, equipementsRes] = await Promise.all([
      ReclamationService.getReclamationsEnseignantConnecte(),
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
    reclamations.value = data.sort((a: any, b: any) => {
      const dateA = new Date(a.dateReclamation || 0).getTime()
      const dateB = new Date(b.dateReclamation || 0).getTime()
      return dateB - dateA
    })
  } catch (error) {
    console.error('Erreur chargement réclamations:', error)
    reclamations.value = []
  }
}

function openModal(rec: any) {
  if (isActionDisabled(rec)) {
    alert(getActionDisabledReason(rec))
    return
  }

  form.id = rec.id
  form.description = rec.description
  form.quantite = rec.quantite
  showModal.value = true
}

function closeModal() { showModal.value = false }

async function handleSubmit() {
  try {
    console.log(`Modification réclamation #${form.id}...`);
    await ReclamationService.updateReclamation(form.id, {
      description: form.description
    });
    alert("Réclamation modifiée avec succès !");
    await fetchReclamations();
    closeModal();
  } catch (error: any) {
    console.error("Erreur modification :", error);
    alert("Erreur: " + (error.response?.data?.message || error.response?.data || error.message));
  }
}

async function supprimerReclamation(rec: any) {
  if (isActionDisabled(rec)) {
    alert(getActionDisabledReason(rec))
    return
  }

  if (!confirm("Voulez-vous supprimer cette réclamation ?")) return
  try {
    await ReclamationService.deleteReclamation(rec.id)
    await fetchReclamations()
  } catch (error: any) {
    console.error('Erreur suppression réclamation:', error)
    alert("Erreur: " + (error.response?.data?.message || error.message))
  }
}

function isActionDisabled(rec: any): boolean {
  return rec?.etat !== 'NON_TRAITEE'
}

function getActionDisabledReason(rec: any): string {
  const etat = String(rec?.etat || '').toUpperCase()
  if (etat === 'TRAITEE') return 'Réclamation déjà traitée'
  if (etat === 'ANNULEE') return 'Réclamation déjà annulée'
  if (etat === 'REFUSEE') return 'Réclamation déjà refusée'
  return etat === 'NON_TRAITEE' ? '' : 'Action indisponible'
}

// x Récupérer la priorité (gérer différents noms de champs)
function getPriorite(rec: any): string {
  const priorite = rec.priorite || rec.priority || rec.prioriteReclamation
  if (priorite) {
    const upper = String(priorite).toUpperCase()
    if (['HAUTE', 'HIGH', 'URGENTE'].includes(upper)) return 'HAUTE'
    if (['BASSE', 'LOW', 'FAIBLE'].includes(upper)) return 'BASSE'
    return 'MOYENNE'
  }
  return 'MOYENNE'
}

function getEquipementIdentifiant(rec: any): string {
  const equipementId = Number(rec?.equipementId || rec?.equipement?.id)
  const mappedEquipement = Number.isFinite(equipementId) ? equipementsById.value[equipementId] : null
  return rec?.equipementIdentifiant
    || rec?.identifiantEquipement
    || rec?.identifiant
    || mappedEquipement?.identifiant
    || rec?.equipement?.identifiant
    || ''
}

// x Styles badge pour priorité
function getPrioriteBadge(priorite?: string) {
  return {
    HAUTE: 'badge badge-danger',
    MOYENNE: 'badge badge-warning',
    BASSE: 'badge badge-info'
  }[priorite || 'MOYENNE'] || 'badge badge-secondary'
}

// x Texte lisible pour la priorité
function formatPriorite(priorite?: string) {
  return {
    HAUTE: 'Haute',
    MOYENNE: 'Moyenne',
    BASSE: 'Basse'
  }[priorite || 'MOYENNE'] || priorite || 'Moyenne'
}

function getEtatBadge(etat: string) {
  return {
    NON_TRAITEE: 'badge badge-warning',
    TRAITEE: 'badge badge-success',
    ANNULEE: 'badge badge-secondary',
    REFUSEE: 'badge badge-danger'
  }[etat] || 'badge badge-secondary'
}

function formatEtat(etat: string) {
  return {
    NON_TRAITEE: 'Non traitée',
    TRAITEE: 'Traitée',
    ANNULEE: 'Annulée',
    REFUSEE: 'Refusée'
  }[etat] || etat
}

function formatDate(date: string) {
  return date ? date.split('T')[0] : ''
}

function truncateText(text: string, maxLength: number) {
  if (!text) return ''
  return text.length > maxLength ? text.substring(0, maxLength) + '...' : text
}

onMounted(async () => {
  await fetchReclamations()
  
  // Vérifier si on doit highlight une réclamation (venant d'une notification)
  if (route.query.highlight) {
    highlightedId.value = Number(route.query.highlight)
    
    // Attendre le rendu puis scroller vers l'élément
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
.modal.show { background: rgba(0, 0, 0, 0.5); }
.table th, .table td { vertical-align: middle; }
.badge { padding: 5px 10px; font-size: 12px; }

/* Highlight animation pour la ligne venant d'une notification */
.highlight-row {
  animation: highlightPulse 1s ease-in-out 3;
  background-color: #fff3cd !important;
}

@keyframes highlightPulse {
  0%, 100% { background-color: #fff3cd; }
  50% { background-color: #ffeeba; }
}
</style>
