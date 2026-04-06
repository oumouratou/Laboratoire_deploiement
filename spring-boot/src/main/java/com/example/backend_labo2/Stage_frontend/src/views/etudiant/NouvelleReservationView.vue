<template>
  <div>
    <section class="content-header">
      <div class="container-fluid">
        <div class="row mb-2">
          <div class="col-sm-6">
            <h1><i class="fas fa-plus-circle mr-2"></i> Nouvelle Réservation</h1>
          </div>
          <div class="col-sm-6">
            <router-link to="/etudiant/reservations" class="btn btn-secondary float-right">
              <i class="fas fa-arrow-left mr-1"></i> Retour
            </router-link>
          </div>
        </div>
      </div>
    </section>

    <section class="content">
      <div class="container-fluid">
        <div class="row">
          <div class="col-md-8 offset-md-2">
            <div class="card card-info">
              <div class="card-header">
                <h3 class="card-title">Formulaire de réservation</h3>
              </div>

              <form @submit.prevent="handleSubmit">
                <div class="card-body">
                  <!-- Département -->
                  <div class="form-group">
                    <label>Département *</label>
                    <select
                      class="form-control"
                      v-model="selectedDepartementId"
                      @change="onDepartementChange"
                      required
                    >
                      <option disabled value="">Sélectionner un département</option>
                      <option
                        v-for="dept in departements"
                        :key="dept.id"
                        :value="dept.id"
                      >
                        {{ dept.nom }}
                      </option>
                    </select>
                  </div>

                  <!-- Laboratoire (seuls les labos ACTIFS sont affichés) -->
                  <div class="form-group">
                    <label>Laboratoire *</label>
                    <select
                      class="form-control"
                      v-model="form.laboratoireId"
                      :disabled="!selectedDepartementId"
                      required
                    >
                      <option disabled value="">
                        {{ !selectedDepartementId ? 'Sélectionnez d\'abord un département' : 'Sélectionner un laboratoire' }}
                      </option>
                      <option
                        v-for="labo in laboratoiresActifs"
                        :key="labo.id"
                        :value="String(labo.id)"
                      >
                        {{ labo.nomLabo || labo.nom }}
                      </option>
                    </select>
                    <small class="text-muted" v-if="selectedDepartementId && laboratoiresInactifs > 0">
                      <i class="fas fa-info-circle mr-1"></i>
                      {{ laboratoiresInactifs }} laboratoire(s) inactif(s) masqué(s)
                    </small>
                  </div>

                  <!-- Date -->
                  <div class="form-group">
                    <label>Date de réservation *</label>
                    <input type="date" class="form-control" v-model="form.dateReservation" :min="minDate" required>
                  </div>

                  <!-- Heures -->
                  <div class="row">
                    <div class="col-md-6">
                      <label>Heure de début *</label>
                      <input type="time" class="form-control" v-model="form.heureDebut" required>
                    </div>
                    <div class="col-md-6">
                      <label>Heure de fin *</label>
                      <input type="time" class="form-control" v-model="form.heureFin" required>
                    </div>
                  </div>

                  <!-- Motif -->
                  <div class="form-group mt-3">
                    <label>Motif *</label>
                    <textarea class="form-control" v-model="form.motif" rows="3" required></textarea>
                  </div>

                </div>

                <div class="card-footer">
                  <button type="submit" class="btn btn-info">
                    <i class="fas fa-calendar-check mr-1"></i> Créer la réservation
                  </button>
                  <router-link to="/etudiant/reservations" class="btn btn-secondary ml-2">
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
import { reactive, ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getDepartements } from '@/Service/departementService'
import { getLaboratoires } from '@/Service/LaboratoireService'
import { createReservation } from '@/Service/ReservationService'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const minDate = new Date().toISOString().split('T')[0]

const currentUser = computed(() => authStore.currentUser)
const departements = ref<Array<{ id: number; nom: string }>>([])
const laboratoires = ref<any[]>([])
const selectedDepartementId = ref<number | ''>('')

// Filtrer les laboratoires ACTIFS uniquement
const laboratoiresActifs = computed(() => 
  laboratoires.value.filter(labo => {
    const laboDeptId = Number(labo.departementId || labo.departement?.id)
    return selectedDepartementId.value
      && laboDeptId === Number(selectedDepartementId.value)
      && labo.etatLabo === 'ACTIF'
  })
)

// Compter les labos inactifs masqués
const laboratoiresInactifs = computed(() => 
  laboratoires.value.filter(labo => {
    const laboDeptId = Number(labo.departementId || labo.departement?.id)
    return selectedDepartementId.value
      && laboDeptId === Number(selectedDepartementId.value)
      && labo.etatLabo !== 'ACTIF'
  }).length
)

const form = reactive({
  laboratoireId: '' as string | '',
  dateReservation: '',
  heureDebut: '08:00',
  heureFin: '10:00',
  motif: ''
})

function onDepartementChange() {
  form.laboratoireId = ''
}

// 🔹 Charger les départements + tous les laboratoires, puis filtrer selon département choisi
onMounted(async () => {
  try {
    if (!authStore.isAuthenticated) {
      router.push('/')
      return
    }

    const [departementsRes, labosRes] = await Promise.all([
      getDepartements(),
      getLaboratoires()
    ])
    departements.value = Array.isArray(departementsRes.data) ? departementsRes.data : []
    laboratoires.value = Array.isArray(labosRes.data) ? labosRes.data : []
    console.log('Labos du département:', laboratoires.value)
    
    // Pré-sélectionner le labo si passé en paramètre
    if (route.query.laboratoireId) {
      const laboId = route.query.laboratoireId.toString()
      const labo = laboratoires.value.find(l => l.id?.toString() === laboId)
      if (labo) {
        const deptId = Number(labo.departementId || labo.departement?.id)
        if (Number.isFinite(deptId) && deptId > 0) {
          selectedDepartementId.value = deptId
        }
        if (labo.etatLabo === 'ACTIF') {
          form.laboratoireId = laboId
        }
      }
    }
  } catch (e) {
    console.error('Erreur chargement laboratoires', e)
    alert("Erreur lors du chargement des laboratoires")
  }
})

async function handleSubmit() {
  if (!selectedDepartementId.value) {
    alert("Veuillez sélectionner un département")
    return
  }

  if (!form.laboratoireId) {
    alert("Veuillez sélectionner un laboratoire")
    return
  }

  if (form.heureDebut >= form.heureFin) {
    alert("Heure de fin invalide")
    return
  }

  if (!currentUser.value?.id) {
    alert("Vous devez être connecté pour faire une réservation")
    router.push('/')
    return
  }

  try {
    await createReservation({
      laboratoireId: Number(form.laboratoireId),
      etudiantId: currentUser.value.id, // ✅ Ajout de l'ID étudiant
      dateReservation: form.dateReservation,
      heureDebut: form.heureDebut,
      heureFin: form.heureFin,
      motif: form.motif
    })

    

    alert("Réservation envoyée !")
    router.push('/etudiant/reservations')

  } catch (error: any) {
    console.error('Erreur réservation:', error)
    alert("Erreur lors de la réservation: " + (error.response?.data?.message || error.message))
  }
}
</script>
