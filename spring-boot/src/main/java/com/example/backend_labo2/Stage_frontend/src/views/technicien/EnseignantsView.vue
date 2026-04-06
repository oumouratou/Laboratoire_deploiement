<template>
  <div>
    <!-- Header -->
    <section class="content-header">
      <h1><i class="fas fa-chalkboard-teacher mr-2"></i> Gestion des Enseignants</h1>
    </section>

    <!-- Filtre par département -->
    <section class="content">
      <div class="card card-outline card-info mb-3">
        <div class="card-header">
          <h3 class="card-title"><i class="fas fa-filter mr-2"></i>Filtrer par département</h3>
        </div>
        <div class="card-body">
          <div class="row">
            <div class="col-md-4">
              <select class="form-control" v-model="selectedDepartement">
                <option value="">Tous les départements</option>
                <option v-for="dept in departements" :key="dept.id" :value="dept.id">
                  {{ dept.nom }}
                </option>
              </select>
            </div>
            <div class="col-md-4">
              <input type="text" class="form-control" v-model="searchQuery" placeholder="Rechercher par nom, prénom ou CIN...">
            </div>
            <div class="col-md-4">
              <span class="badge badge-info p-2">
                <i class="fas fa-users mr-1"></i> {{ filteredEnseignants.length }} enseignant(s) trouvé(s)
              </span>
            </div>
          </div>
        </div>
      </div>

    <!-- Table -->
      <div class="card">
        <div class="card-header">
          <h3 class="card-title">Liste des enseignants</h3>
          <div class="card-tools">
            <button class="btn btn-success btn-sm" @click="openModal()">
              <i class="fas fa-plus mr-1"></i> Ajouter
            </button>
          </div>
        </div>

        <div class="card-body table-responsive">
          <table class="table table-bordered table-hover">
            <thead class="bg-info">
              <tr>
                <th>Numéro</th>
                <th>Nom</th>
                <th>Prénom</th>
                <th>Email</th>
                <th>CIN</th>
                <th>Département</th>
                <th>Date de création</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(ens, index) in filteredEnseignants" :key="ens.id">
                <td>{{ index + 1 }}</td>
                <td>{{ ens.nom }}</td>
                <td>{{ ens.prenom }}</td>
                <td>{{ ens.email }}</td>
                <td>{{ ens.cin }}</td>
                <td>{{ ens.departement?.nom || 'N/A' }}</td>
                <td>{{ formatDate(ens.createdAt) }}</td>
                <td class="text-center">
                  <div class="d-flex justify-content-center gap-2">
                    <button class="btn btn-warning btn-sm" @click="handleAlerter(ens)">
                      <i class="fas fa-bell mr-1"></i>Alerter
                    </button>
                  </div>
                </td>
              </tr>
              <tr v-if="filteredEnseignants.length === 0">
                <td colspan="8" class="text-center">Aucun enseignant trouvé</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </section>

    <!-- Modal scrollable -->
    <div class="modal fade" :class="{ show: showModal }" :style="{ display: showModal ? 'block' : 'none' }">
      <div class="modal-dialog modal-dialog-scrollable">
        <div class="modal-content">
          <div class="modal-header bg-info">
            <h4 class="modal-title">{{ isEdit ? 'Modifier' : 'Ajouter' }} un enseignant</h4>
            <button type="button" class="close" @click="closeModal">&times;</button>
          </div>

          <form @submit.prevent="handleSubmit">
            <div class="modal-body">
              <div class="form-group">
                <label>Nom</label>
                <input type="text" v-model="form.nom" class="form-control" required />
              </div>
              <div class="form-group">
                <label>Prénom</label>
                <input type="text" v-model="form.prenom" class="form-control" required />
              </div>
              <div class="form-group">
                <label>Email</label>
                <input type="email" v-model="form.email" class="form-control" required />
              </div>
              <div class="form-group">
                <label>Mot de passe</label>
                <input type="password" v-model="form.password" class="form-control" :required="!isEdit" />
              </div>
              <div class="form-group">
                <label>CIN</label>
                <input type="text" v-model="form.cin" class="form-control" required />
              </div>
              <div class="form-group">
                <label>Département</label>
                <select v-model="form.departementId" class="form-control" required>
                  <option value="">Sélectionner un département</option>
                  <option v-for="dept in departements" :key="dept.id" :value="dept.id">
                    {{ dept.nom }}
                  </option>
                </select>
              </div>
            </div>

            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" @click="closeModal">Annuler</button>
              <button type="submit" class="btn btn-info">
                {{ isEdit ? 'Modifier' : 'Ajouter' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <div class="modal-backdrop fade show" v-if="showModal"></div>

    <!-- Modal Alerter -->
    <div class="modal fade" :class="{ show: showAlertModal }" :style="{ display: showAlertModal ? 'block' : 'none' }">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header bg-info text-white">
            <h4 class="modal-title"><i class="fas fa-bell mr-2"></i>Alerter l'enseignant</h4>
            <button type="button" class="close text-white" @click="closeAlertModal">&times;</button>
          </div>
          <div class="modal-body">
            <div class="alert alert-info">
              <i class="fas fa-user mr-2"></i>
              Envoyer une alerte à <strong>{{ alertTarget?.prenom }} {{ alertTarget?.nom }}</strong>
            </div>
            <div class="form-group">
              <label><strong>Message de l'alerte <span class="text-danger">*</span></strong></label>
              <textarea 
                class="form-control" 
                v-model="alertMessage" 
                rows="4" 
                placeholder="Saisissez le message de l'alerte..."
                required
              ></textarea>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="closeAlertModal">Annuler</button>
            <button 
              type="button" 
              class="btn btn-info" 
              @click="sendAlert"
              :disabled="!alertMessage.trim()"
            >
              <i class="fas fa-paper-plane mr-1"></i> Envoyer
            </button>
          </div>
        </div>
      </div>
    </div>
    <div class="modal-backdrop fade show" v-if="showAlertModal"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue';
import { getUsers, createEnseignant, updateEnseignant } from '@/Service/UserService';
import { getDepartements } from '@/Service/departementService';
import { sendAlerteEnseignant } from '@/Service/NotificationService';

interface Departement {
  id: number
  nom: string
}

interface Enseignant {
  id: number
  nom: string
  prenom: string
  email: string
  cin: string
  createdAt?: string | null
  departement?: Departement
}

const enseignants = ref<Enseignant[]>([]);
const departements = ref<Departement[]>([]);
const showModal = ref(false);
const isEdit = ref(false);
const editId = ref<number | null>(null);
const selectedDepartement = ref<number | ''>('');
const searchQuery = ref('');
const showAlertModal = ref(false);
const alertTarget = ref<any>(null);
const alertMessage = ref('');

// Filtrer les enseignants par département et recherche
const filteredEnseignants = computed(() => {
  let result = enseignants.value;
  
  // Filtre par département
  if (selectedDepartement.value) {
    result = result.filter(e => e.departement?.id === selectedDepartement.value);
  }
  
  // Filtre par recherche textuelle
  if (searchQuery.value.trim()) {
    const query = searchQuery.value.toLowerCase().trim();
    result = result.filter(e => 
      e.nom?.toLowerCase().includes(query) ||
      e.prenom?.toLowerCase().includes(query) ||
      e.cin?.toLowerCase().includes(query) ||
      e.email?.toLowerCase().includes(query)
    );
  }
  
  return result;
});

const form = reactive({
  nom: '',
  prenom: '',
  email: '',
  password: '',
  cin: '',
  departementId: 0
});

async function loadEnseignants() {
  const res = await getUsers();
  const data = (res.data as any[]).filter(u => u.role === 'ENSEIGNANT');
  // Trier par ID décroissant (les plus récents en premier)
  enseignants.value = data.sort((a: Enseignant, b: Enseignant) => b.id - a.id);
}

async function loadDepartements() {
  const res = await getDepartements();
  departements.value = res.data as Departement[];
}

function openModal(ens?: any) {
  if (ens) {
    isEdit.value = true;
    editId.value = ens.id;
    form.nom = ens.nom;
    form.prenom = ens.prenom;
    form.email = ens.email;
    form.cin = ens.cin;
    form.departementId = ens.departement?.id || 0;
    form.password = '';
  } else {
    isEdit.value = false;
    editId.value = null;
    form.nom = '';
    form.prenom = '';
    form.email = '';
    form.password = '';
    form.cin = '';
    form.departementId = 0;
  }
  showModal.value = true;
}

function closeModal() {
  showModal.value = false;
}

async function handleSubmit() {
  const data = {
    nom: form.nom,
    prenom: form.prenom,
    email: form.email,
    password: form.password || undefined,
    cin: form.cin,
    departementId: form.departementId
  };

  if (isEdit.value && editId.value) {
    await updateEnseignant(editId.value, data);
  } else {
    await createEnseignant(data);
  }

  await loadEnseignants();
  closeModal();
}

async function handleAlerter(ens: Enseignant) {
  const motif = prompt(`Message d'alerte pour ${ens.prenom} ${ens.nom} :`, 'Veuillez consulter vos réclamations et notifications.')
  if (motif === null) return

  const message = motif.trim()
  if (!message) {
    alert('Le message d\'alerte ne peut pas être vide.')
    return
  }

  try {
    await sendAlerteEnseignant(ens.id, message)
    alert(`Notification envoyée à ${ens.prenom} ${ens.nom}.`)
  } catch (error: any) {
    console.error('Erreur envoi notification enseignant:', error)
    alert(error.response?.data?.message || "R Impossible d'envoyer la notification à l'enseignant.")
  }
}

function formatDate(dateStr?: string | null) {
  if (!dateStr) return 'N/A';
  return new Date(dateStr).toLocaleString();
}

function openAlertModal(ens: any) {
  alertTarget.value = ens;
  alertMessage.value = '';
  showAlertModal.value = true;
}

function closeAlertModal() {
  showAlertModal.value = false;
  alertTarget.value = null;
  alertMessage.value = '';
}

async function sendAlert() {
  if (!alertTarget.value || !alertMessage.value.trim()) return;
  try {
    await sendAlerteEnseignant(alertTarget.value.id, alertMessage.value);
    alert('Alerte envoyée avec succès à ' + alertTarget.value.prenom + ' ' + alertTarget.value.nom);
    closeAlertModal();
  } catch (error: any) {
    console.error('Erreur envoi alerte:', error);
    const apiError = error.response?.data;
    const errorMessage =
      apiError?.message ||
      apiError?.error ||
      (typeof apiError === 'string' ? apiError : null) ||
      error.message;
    alert('Erreur lors de l\'envoi de l\'alerte: ' + errorMessage);
  }
}

onMounted(async () => {
  await loadDepartements();
  await loadEnseignants();
});
</script>

<style scoped>
.modal.show { display: block; background: rgba(0,0,0,0.5); }
.modal-dialog { max-width: 600px; }
.modal-body { max-height: 70vh; overflow-y: auto; }

/* ---------- BOUTONS ACTIONS TABLEAU ---------- */
td.text-center {
  text-align: center;
  vertical-align: middle;
}

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
