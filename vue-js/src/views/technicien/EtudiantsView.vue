<template>
  <div>
    <!-- En-tête -->
    <section class="content-header">
      <div class="container-fluid">
        <div class="row mb-2">
          <div class="col-sm-6">
            <h1><i class="fas fa-user-graduate mr-2"></i> Gestion des Étudiants</h1>
          </div>
          <div class="col-sm-6 text-right">
            <button class="btn btn-success" @click="openModal()">
              <i class="fas fa-plus mr-1"></i> Ajouter un étudiant
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- Filtre par département -->
    <section class="content">
      <div class="container-fluid">
        <div class="card card-outline card-success mb-3">
          <div class="card-header">
            <h3 class="card-title"><i class="fas fa-filter mr-2"></i>Filtrer par département</h3>
          </div>
          <div class="card-body">
            <div class="row">
              <div class="col-md-4">
                <select class="form-control" v-model="selectedDepartement">
                  <option value="">Tous les départements</option>
                  <option v-for="d in departements" :key="d.id" :value="d.id">
                    {{ d.nom }}
                  </option>
                </select>
              </div>
              <div class="col-md-4">
                <input type="text" class="form-control" v-model="searchQuery" placeholder="Rechercher par nom, prénom ou CIN...">
              </div>
              <div class="col-md-4">
                <span class="badge badge-info p-2">
                  <i class="fas fa-users mr-1"></i> {{ filteredEtudiants.length }} étudiant(s) trouvé(s)
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- Tableau -->
        <div class="card">
          <div class="card-body table-responsive">
            <table class="table table-bordered table-striped table-hover">
              <thead class="bg-success">
                <tr>
                  <th>Numéro</th>
                  <th>Nom</th>
                  <th>Prénom</th>
                  <th>Email</th>
                  <th>CIN</th>
                  <th>Département</th>
                  <th>Accès</th>
                  <th>Créé le</th>
                  <th>Statut</th>
                  <th class="text-center">Actions</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(e, index) in filteredEtudiants" :key="e.id">
                  <td>{{ index + 1 }}</td>
                  <td>{{ e.nom }}</td>
                  <td>{{ e.prenom }}</td>
                  <td>{{ e.email }}</td>
                  <td>{{ e.cin }}</td>
                  <td>{{ e.departement?.nom || 'N/A' }}</td>
                  <td>
                    <span :class="isEtudiantBlocked(e) ? 'badge badge-danger' : 'badge badge-success'">
                      {{ isEtudiantBlocked(e) ? 'Bloqué' : 'Actif' }}
                    </span>
                  </td>
                  <td>{{ formatDate(e.createdAt) }}</td>
                  <td>
                    <span :class="!e.active ? 'badge badge-danger' : 'badge badge-success'">
                      {{ !e.active ? 'Bloqué' : 'Actif' }}
                    </span>
                  </td>
                  <td class="text-center">
                    <div class="d-flex justify-content-center gap-2">
                      <button
                        v-if="!isEtudiantBlocked(e)"
                        class="btn btn-danger btn-sm"
                        @click="handleBlockEtudiant(e)"
                      >
                        <i class="fas fa-lock mr-1"></i>Bloquer
                      </button>
                      <button
                        v-else
                        class="btn btn-success btn-sm"
                        @click="handleUnblockEtudiant(e)"
                      >
                        <i class="fas fa-unlock mr-1"></i>Débloquer
                      </button>
                    </div>
                  </td>
                </tr>
                <tr v-if="filteredEtudiants.length === 0">
                  <td colspan="9" class="text-center">Aucun étudiant trouvé</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </section>

    <!-- MODAL -->
    <div
      class="modal fade"
      :class="{ show: showModal }"
      :style="{ display: showModal ? 'block' : 'none' }"
    >
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header bg-success">
            <h5 class="modal-title">{{ isEdit ? 'Modifier' : 'Ajouter' }} un étudiant</h5>
            <button type="button" class="close" @click="closeModal">&times;</button>
          </div>

          <form @submit.prevent="handleSubmit">
            <div class="modal-body">
              <div class="form-group">
                <label>Nom</label>
                <input class="form-control" v-model="form.nom" required />
              </div>

              <div class="form-group">
                <label>Prénom</label>
                <input class="form-control" v-model="form.prenom" required />
              </div>

              <div class="form-group">
                <label>Email</label>
                <input type="email" class="form-control" v-model="form.email" required />
              </div>

              <div class="form-group" v-if="!isEdit">
                <label>Mot de passe</label>
                <input type="password" class="form-control" v-model="form.password" required />
              </div>

              <div class="form-group">
                <label>CIN</label>
                <input class="form-control" v-model="form.cin" required />
              </div>

              <div class="form-group">
                <label>Département</label>
                <select class="form-control" v-model="form.departementId" required>
                  <option value="">-- Sélectionner --</option>
                  <option v-for="d in departements" :key="d.id" :value="d.id">
                    {{ d.nom }}
                  </option>
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

    <div class="modal-backdrop fade show" v-if="showModal"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import api from '@/Service/api'
import { getEtudiants, createEtudiant, updateEtudiant, blockEtudiant, unblockEtudiant } from '@/Service/UserService'

const etudiants = ref<any[]>([])
const departements = ref<any[]>([])
const showModal = ref(false)
const selectedDepartement = ref<number | ''>('')
const searchQuery = ref('')

// Filtrer les étudiants par département et recherche
const filteredEtudiants = computed(() => {
  let result = etudiants.value
  
  // Filtre par département
  if (selectedDepartement.value) {
    result = result.filter(e => e.departement?.id === selectedDepartement.value)
  }
  
  // Filtre par recherche textuelle
  if (searchQuery.value.trim()) {
    const query = searchQuery.value.toLowerCase().trim()
    result = result.filter(e => 
      e.nom?.toLowerCase().includes(query) ||
      e.prenom?.toLowerCase().includes(query) ||
      e.cin?.toLowerCase().includes(query) ||
      e.email?.toLowerCase().includes(query)
    )
  }
  
  return result
})
const isEdit = ref(false)
const editId = ref<number | null>(null)

const form = reactive({
  nom: '',
  prenom: '',
  email: '',
  password: '',
  cin: '',
  departementId: ''
})

onMounted(async () => {
  await loadDepartements()
  await loadEtudiants()
})

async function loadEtudiants() {
  try {
    const res = await getEtudiants()
    const data = Array.isArray(res.data) ? res.data : []
    // Trier par ID décroissant (les plus récents en premier)
    etudiants.value = data.sort((a: any, b: any) => b.id - a.id)
  } catch (error) {
    console.error('Erreur chargement étudiants:', error)
  }
}

async function loadDepartements() {
  try {
    const res = await api.get('/departements')
    departements.value = res.data
  } catch (error) {
    console.error('Erreur chargement départements:', error)
  }
}

function openModal(e?: any) {
  if (e) {
    isEdit.value = true
    editId.value = e.id
    Object.assign(form, {
      nom: e.nom,
      prenom: e.prenom,
      email: e.email,
      cin: e.cin,
      password: '',
      departementId: e.departement?.id || ''
    })
  } else {
    isEdit.value = false
    editId.value = null
    Object.assign(form, {
      nom: '',
      prenom: '',
      email: '',
      password: '',
      cin: '',
      departementId: ''
    })
  }
  showModal.value = true
}

function closeModal() {
  showModal.value = false
}

async function handleSubmit() {
  const data = {
    nom: form.nom,
    prenom: form.prenom,
    email: form.email,
    password: form.password,
    cin: form.cin,
    departementId: Number(form.departementId)
  }

  try {
    if (isEdit.value && editId.value) {
      await updateEtudiant(editId.value, data)
    } else {
      await createEtudiant(data)
    }

    await loadEtudiants()
    closeModal()
  } catch (error: any) {
    console.error('Erreur backend:', error.response?.data || error.message)
    alert('Impossible de sauvegarder l"étudiant. Vérifie les champs et le département.')
  }
}

function isEtudiantBlocked(etudiant: any): boolean {
  if (!etudiant) return false
  if (etudiant.blocked === true || etudiant.isBlocked === true || etudiant.bloque === true) return true
  if (etudiant.active === false || etudiant.isActive === false || etudiant.enabled === false) return true
  const etat = String(etudiant.etat || etudiant.statut || etudiant.accountStatus || '').toUpperCase()
  return etat === 'BLOQUE' || etat === 'BLOCKED' || etat === 'INACTIF'
}

async function handleBlockEtudiant(etudiant: any) {
  if (!confirm(`Bloquer l'étudiant ${etudiant.prenom} ${etudiant.nom} ?`)) return
  try {
    await blockEtudiant(etudiant.id)
    await loadEtudiants()
  } catch (error: any) {
    console.error('Erreur blocage étudiant:', error)
    alert(error.response?.data?.message || 'Impossible de bloquer cet étudiant.')
  }
}

async function handleUnblockEtudiant(etudiant: any) {
  if (!confirm(`Débloquer l'étudiant ${etudiant.prenom} ${etudiant.nom} ?`)) return
  try {
    await unblockEtudiant(etudiant.id)
    await loadEtudiants()
  } catch (error: any) {
    console.error('Erreur déblocage étudiant:', error)
    alert(error.response?.data?.message || 'Impossible de débloquer cet étudiant.')
  }
}

async function handleBlock(id: number) {
  if (!confirm('`tes-vous sûr de vouloir bloquer cet étudiant ?')) return
  try {
    await blockEtudiant(id)
    await loadEtudiants()
  } catch (error: any) {
    console.error('Erreur blocage:', error)
    alert(error.response?.data?.message || 'Impossible de bloquer l\'étudiant.')
  }
}

async function handleUnblock(id: number) {
  if (!confirm('`tes-vous sûr de vouloir débloquer cet étudiant ?')) return
  try {
    await unblockEtudiant(id)
    await loadEtudiants()
  } catch (error: any) {
    console.error('Erreur déblocage:', error)
    alert(error.response?.data?.message || 'Impossible de débloquer l\'étudiant.')
  }
}

function formatDate(date: string) {
  return date ? new Date(date).toLocaleString() : '-'
}
</script>

<style scoped>
.modal.show {
  display: block;
  background: rgba(0, 0, 0, 0.5);
}

.modal-dialog {
  max-width: 600px;
}

.modal-body {
  max-height: 70vh;
  overflow-y: auto;
}

/* ---------- BOUTONS ACTIONS TABLEAU ---------- */
.d-flex {
  display: flex !important;
}

.justify-content-center {
  justify-content: center !important;
}

.gap-2 {
  gap: 0.5rem !important;
}

.btn-sm {
  white-space: nowrap;
}
</style>
