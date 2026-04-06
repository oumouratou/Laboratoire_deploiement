<template>
  <div>
    <!-- Header -->
    <section class="content-header">
      <div class="container-fluid">
        <h1>Gestion des Départements</h1>
      </div>
    </section>

    <!-- Content -->
    <section class="content">
      <div class="container-fluid">
        <div class="card">
          <!-- Card Header -->
          <div class="card-header d-flex align-items-center flex-wrap gap-2">
            <h3 class="card-title mb-0">Liste des départements</h3>
            <button class="btn btn-success btn-sm ml-auto" @click="openModal()">
              <i class="fas fa-plus mr-1"></i>Ajouter
            </button>
          </div>

          <!-- Card Body -->
          <div class="card-body table-responsive">
            <table class="table table-bordered table-hover dept-table">
              <thead class="thead-light">
                <tr>
                  <th>Numéro</th>
                  <th>Nom</th>
                  <th class="d-none d-md-table-cell">Description</th>
                  <th class="text-center">État</th>
                  <th class="text-center">Consulter</th>
                  <th class="text-center">Gestion</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(dept, index) in departements" :key="dept.id">
                  <td>{{ index + 1 }}</td>
                  <td><strong>{{ dept.nom }}</strong></td>
                  <td class="d-none d-md-table-cell">{{ dept.description || '' }}</td>
                  <td class="text-center">
                    <span :class="dept.actif !== false ? 'badge badge-success' : 'badge badge-danger'">
                      <i :class="dept.actif !== false ? 'fas fa-check-circle mr-1' : 'fas fa-ban mr-1'"></i>
                      {{ dept.actif !== false ? 'Actif' : 'Inactif' }}
                    </span>
                  </td>
                  <!-- Colonne Consulter -->
                  <td class="text-center">
                    <button class="btn btn-info btn-sm" @click="voirLaboratoires(dept)" title="Voir laboratoires">
                      <i class="fas fa-flask mr-1"></i><span class="d-none d-sm-inline"> Labos</span>
                    </button>
                  </td>
                  <!-- Colonne Gestion -->
                  <td class="text-center">
                    <div class="btn-actions">
                      <button class="btn btn-primary btn-sm" @click="openModal(dept)">
                        <i class="fas fa-edit mr-1"></i><span class="d-none d-sm-inline"> Modifier</span>
                      </button>
                      <button class="btn btn-danger btn-sm" @click="handleDelete(dept.id)">
                        <i class="fas fa-trash mr-1"></i><span class="d-none d-sm-inline"> Supprimer</span>
                      </button>
                    </div>
                  </td>
                </tr>
                <tr v-if="departements.length === 0">
                  <td colspan="6" class="text-center text-muted">Aucun département trouvé</td>
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
              <h5 class="modal-title">{{ isEdit ? 'Modifier le département' : 'Ajouter un département' }}</h5>
              <button type="button" class="close" @click="closeModal">×</button>
            </div>

            <div class="modal-body">
              <div class="form-group">
                <label>Nom <span class="text-danger">*</span></label>
                <input type="text" class="form-control" v-model="form.nom" required />
              </div>
              <div class="form-group">
                <label>Description</label>
                <textarea class="form-control" v-model="form.description" rows="3"></textarea>
              </div>
              <div class="form-group" v-if="isEdit">
                <label>État <span class="text-danger">*</span></label>
                <select class="form-control" v-model="form.actif">
                  <option :value="true">Actif (visible par tous les utilisateurs)</option>
                  <option :value="false">xa Inactif (masqué pour les étudiants/enseignants)</option>
                </select>
                <small class="form-text text-muted">
                  <i class="fas fa-info-circle mr-1"></i>
                  Un département inactif ne sera plus affiché aux étudiants et enseignants.
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

    <!-- Backdrop -->
    <div class="modal-backdrop fade show" v-if="showModal"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import type { Departement } from '@/types'
import { getDepartements, createDepartement, updateDepartement, deleteDepartement } from '@/Service/departementService'

const router = useRouter()
const departements = ref<Departement[]>([])
const showModal = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)

const form = reactive({ nom: '', description: '', actif: true as boolean })

onMounted(fetchDepartements)

async function fetchDepartements() {
  try {
    const res = await getDepartements()
    const data = Array.isArray(res.data) ? res.data : []
    // Trier par ID décroissant (les plus récents en premier)
    departements.value = data.sort((a: Departement, b: Departement) => b.id - a.id)
  } catch (e: any) {
    console.error('Erreur chargement départements', e)
    alert('Erreur chargement départements: ' + e.response?.data?.message || e.message)
  }
}

function openModal(dept?: Departement) {
  if (dept) {
    isEdit.value = true
    editId.value = dept.id
    form.nom = dept.nom
    form.description = dept.description || ''
    form.actif = dept.actif !== false  // true par défaut si undefined
  } else {
    isEdit.value = false
    editId.value = null
    form.nom = ''
    form.description = ''
    form.actif = true
  }
  showModal.value = true
}

function closeModal() {
  showModal.value = false
}

async function handleSubmit() {
  try {
    if (isEdit.value && editId.value !== null) {
      await updateDepartement(editId.value, form)
    } else {
      await createDepartement(form)
    }
    await fetchDepartements()
    closeModal()
  } catch (e: any) {
    console.error('Erreur sauvegarde', e)
    alert('Impossible de sauvegarder : ' + e.response?.data?.message || e.message)
  }
}

async function handleDelete(id: number) {
  if (!confirm('Supprimer ce département ?')) return
  try {
    await deleteDepartement(id)
    await fetchDepartements()
  } catch (e: any) {
    console.error('Erreur suppression', e)
    // Message d'erreur plus clair
    const errorMsg = e.response?.data?.message || e.response?.data?.error || e.message
    if (errorMsg.toLowerCase().includes('laboratoire') || errorMsg.toLowerCase().includes('labo')) {
      alert('R Impossible de supprimer ce département car il contient des laboratoires.\n\nx Vous devez d\'abord supprimer ou déplacer les laboratoires de ce département.')
    } else {
      alert('Impossible de supprimer : ' + errorMsg)
    }
  }
}

// Navigation vers les laboratoires filtrés par département
function voirLaboratoires(dept: Departement) {
  router.push({ 
    path: '/technicien/laboratoires', 
    query: { departementId: dept.id, departementNom: dept.nom } 
  })
}
</script>

<style scoped>
.modal.show { background-color: rgba(0,0,0,0.5); }

.btn-actions {
  display: flex;
  flex-direction: column;
  gap: 4px;
  align-items: center;
}

.gap-2 { gap: 0.5rem; }

.dept-table th, .dept-table td {
  vertical-align: middle;
}

.badge {
  font-size: 0.8rem;
  padding: 4px 8px;
}

@media (max-width: 576px) {
  .content-header h1 {
    font-size: 1.2rem;
  }
  .btn-actions {
    flex-direction: row;
    flex-wrap: wrap;
    justify-content: center;
  }
  .modal-dialog {
    margin: 0.5rem;
  }
}

@media (max-width: 768px) {
  .btn-actions {
    flex-direction: row;
    gap: 4px;
  }
}
</style>
