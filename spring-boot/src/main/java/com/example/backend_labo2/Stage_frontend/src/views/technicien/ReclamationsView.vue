<template>
  <div class="page-container">
    <section class="content-header">
      <div class="container-fluid">
        <h1><i class="fas fa-tools mr-2"></i> Gestion des Réclamations</h1>
        <p>Réclamations du laboratoire du technicien</p>
        <!-- Badge de filtre actif -->
        <div v-if="filterLaboId || filterEquipId" class="mt-2">
          <span class="badge badge-info p-2">
            <i class="fas fa-filter mr-1"></i> 
            Filtrées par : 
            <span v-if="filterLaboNom">Labo "{{ filterLaboNom }}"</span>
            <span v-if="filterEquipNom">Équipement "{{ filterEquipNom }}"</span>
            <button class="btn btn-sm btn-light ml-2" @click="clearFilter" style="padding: 0 5px;">
              <i class="fas fa-times"></i>
            </button>
          </span>
        </div>
      </div>
    </section>

    <section class="content">
      <div class="container-fluid">

        <!-- Loading / Error -->
        <div v-if="loading" class="text-center p-5">
          <i class="fas fa-spinner fa-spin fa-2x"></i>
          <p class="mt-2">Chargement des réclamations...</p>
        </div>

        <div v-else-if="error" class="alert alert-danger">
          <i class="fas fa-exclamation-circle mr-2"></i>{{ error }}
        </div>

        <template v-else>
          <!-- ================= ÉTUDIANTS ================= -->
          <div class="card mb-4">
            <div class="card-header bg-primary text-white">
              <h3 class="card-title">Réclamations Étudiants ({{ reclamationsEtudiantsFiltrees.length }})</h3>
            </div>

            <div class="card-body table-responsive">
              <table class="table table-bordered table-striped table-hover" v-if="reclamationsEtudiantsFiltrees.length">
              <thead class="bg-info text-white">
                <tr>
                  <th width="60">Numéro</th>
                  <th width="110" class="text-center">Détails</th>
                  <th>Auteur</th>
                  <th>Équipement</th>
                  <th>Identifiant</th>
                  <th>Description</th>
                  <th width="100">Priorité</th>
                  <th width="110">Statut</th>
                  <th width="100">Date</th>
                  <th width="200" class="text-center">Actions</th>
                </tr>
              </thead>
              <tbody>
                <tr 
                  v-for="(rec, index) in reclamationsEtudiantsFiltrees" 
                  :key="rec.id"
                  :class="{ 'highlight-row': highlightedId === rec.id }"
                  :ref="el => { if (highlightedId === rec.id) highlightedRow = el }"
                >
                  <td><strong>{{ index + 1 }}</strong></td>
                  <td class="text-center">
                    <button class="btn btn-info btn-sm" @click="openDetailsModal(rec)">
                      <i class="fas fa-info-circle mr-1"></i> Détails
                    </button>
                  </td>
                  <td>{{ getAuteurNom(rec) }}</td>
                  <td>{{ rec.equipement?.nom || rec.equipementNom || 'N/A' }}</td>
                  <td><code class="text-primary">{{ getEquipementIdentifiant(rec) }}</code></td>
                  <td>{{ truncateText(rec.description, 50) }}</td>
                  <td><span :class="getPrioriteBadge(getPriorite(rec))">{{ formatPriorite(getPriorite(rec)) }}</span></td>
                  <td>
                    <span :class="getStatutBadge(getStatut(rec))">
                      {{ formatStatut(getStatut(rec)) }}
                    </span>
                  </td>
                  <td>{{ formatDate(getDateReclamation(rec)) }}</td>
                  <td class="text-center">
                    <div class="btn-group" v-if="isEnAttente(rec)">
                      <button class="btn btn-success btn-sm" @click="traiterReclamation(rec.id)">
                        <i class="fas fa-check mr-1"></i> Approuver
                      </button>
                      <button class="btn btn-danger btn-sm" @click="openRejectModal(rec)">
                        <i class="fas fa-times mr-1"></i> Refuser
                      </button>
                    </div>
                    <span v-else class="text-muted"><i class="fas fa-check-circle"></i> Traitée</span>
                  </td>
                </tr>
              </tbody>
            </table>

            <div v-else class="text-center text-muted p-4">
              <i class="fas fa-inbox fa-2x mb-2"></i>
              <p>Aucune réclamation étudiant</p>
            </div>
          </div>
        </div>

        <!-- ================= ENSEIGNANTS ================= -->
        <div class="card">
          <div class="card-header bg-success text-white">
            <h3 class="card-title">Réclamations Enseignants ({{ reclamationsEnseignantsFiltrees.length }})</h3>
          </div>

          <div class="card-body table-responsive">
            <table class="table table-bordered table-striped table-hover" v-if="reclamationsEnseignantsFiltrees.length">
              <thead class="bg-info text-white">
                <tr>
                  <th width="60">Numéro</th>
                  <th width="110" class="text-center">Détails</th>
                  <th>Enseignant</th>
                  <th>Équipement</th>
                  <th>Identifiant</th>
                  <th>Description</th>
                  <th width="100">Priorité</th>
                  <th width="110">Statut</th>
                  <th width="100">Date</th>
                  <th width="200" class="text-center">Actions</th>
                </tr>
              </thead>
              <tbody>
                <tr 
                  v-for="(rec, index) in reclamationsEnseignantsFiltrees" 
                  :key="rec.id"
                  :class="{ 'highlight-row': highlightedId === rec.id }"
                  :ref="el => { if (highlightedId === rec.id) highlightedRow = el }"
                >
                  <td><strong>{{ index + 1 }}</strong></td>
                  <td class="text-center">
                    <button class="btn btn-info btn-sm" @click="openDetailsModal(rec)">
                      <i class="fas fa-info-circle mr-1"></i> Détails
                    </button>
                  </td>
                  <td>{{ getAuteurNom(rec) }}</td>
                  <td>{{ rec.equipement?.nom || rec.equipementNom || 'N/A' }}</td>
                  <td><code class="text-primary">{{ getEquipementIdentifiant(rec) }}</code></td>
                  <td>{{ truncateText(rec.description, 50) }}</td>
                  <td><span :class="getPrioriteBadge(getPriorite(rec))">{{ formatPriorite(getPriorite(rec)) }}</span></td>
                  <td>
                    <span :class="getStatutBadge(getStatut(rec))">
                      {{ formatStatut(getStatut(rec)) }}
                    </span>
                  </td>
                  <td>{{ formatDate(getDateReclamation(rec)) }}</td>
                  <td class="text-center">
                    <div class="btn-group" v-if="isEnAttente(rec)">
                      <button class="btn btn-success btn-sm" @click="traiterReclamation(rec.id)">
                        <i class="fas fa-check mr-1"></i> Approuver
                      </button>
                      <button class="btn btn-danger btn-sm" @click="openRejectModal(rec)">
                        <i class="fas fa-times mr-1"></i> Refuser
                      </button>
                    </div>
                    <span v-else class="text-muted"><i class="fas fa-check-circle"></i> Traitée</span>
                  </td>
                </tr>
              </tbody>
            </table>

            <div v-else class="text-center text-muted p-4">
              <i class="fas fa-inbox fa-2x mb-2"></i>
              <p>Aucune réclamation enseignant</p>
            </div>
          </div>
        </div>
        </template>

      </div>
    </section>

    <!-- Modal de refus avec motif -->
    <div class="modal fade" :class="{ show: showRejectModal }" :style="{ display: showRejectModal ? 'block' : 'none' }">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header bg-danger text-white">
            <h5 class="modal-title">
              <i class="fas fa-times-circle mr-2"></i>Refuser la réclamation
            </h5>
            <button type="button" class="close text-white" @click="closeRejectModal">&times;</button>
          </div>
          <div class="modal-body">
            <div class="alert alert-warning">
              <i class="fas fa-exclamation-triangle mr-2"></i>
              Vous êtes sur le point de refuser la réclamation de 
              <strong>{{ getAuteurNom(selectedReclamation || {}) }}</strong>
              concernant l'équipement <strong>{{ selectedReclamation?.equipementNom || selectedReclamation?.equipement?.nom || 'N/A' }}</strong>.
            </div>
            <div class="form-group">
              <label><strong>Motif du refus <span class="text-danger">*</span></strong></label>
              <textarea 
                class="form-control" 
                v-model="rejectMotif" 
                rows="4" 
                placeholder="Veuillez expliquer la raison du refus (ex: équipement déjà réparé, problème non confirmé, etc.)"
                required
              ></textarea>
              <small class="text-muted">Ce motif sera communiqué au demandeur dans sa notification.</small>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="closeRejectModal">
              <i class="fas fa-arrow-left mr-1"></i> Annuler
            </button>
            <button 
              type="button" 
              class="btn btn-danger" 
              @click="confirmReject"
              :disabled="!rejectMotif.trim()"
            >
              <i class="fas fa-times mr-1"></i> Confirmer le refus
            </button>
          </div>
        </div>
      </div>
    </div>
    <div class="modal-backdrop fade show" v-if="showRejectModal"></div>

    <!-- Modal détails demandeur -->
    <div class="modal fade" :class="{ show: showDetailsModal }" :style="{ display: showDetailsModal ? 'block' : 'none' }">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header bg-info text-white">
            <h5 class="modal-title"><i class="fas fa-user-circle mr-2"></i>Détails du demandeur</h5>
            <button type="button" class="close text-white" @click="closeDetailsModal">&times;</button>
          </div>
          <div class="modal-body">
            <table class="table table-bordered mb-0" v-if="selectedDetailsReclamation">
              <tbody>
                <tr>
                  <th style="width: 230px;">Nom complet</th>
                  <td>{{ getAuteurNom(selectedDetailsReclamation) }}</td>
                </tr>
                <tr>
                  <th>CIN</th>
                  <td>{{ getAuteurCin(selectedDetailsReclamation) }}</td>
                </tr>
                <tr v-if="!isEnseignantReclamation(selectedDetailsReclamation)">
                  <th>Niveau</th>
                  <td>{{ getAuteurNiveau(selectedDetailsReclamation) }}</td>
                </tr>
                <tr v-if="!isEnseignantReclamation(selectedDetailsReclamation)">
                  <th>Classe</th>
                  <td>{{ getAuteurClasse(selectedDetailsReclamation) }}</td>
                </tr>
                <tr>
                  <th>Département</th>
                  <td>{{ getAuteurDepartement(selectedDetailsReclamation) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="closeDetailsModal">Fermer</button>
          </div>
        </div>
      </div>
    </div>
    <div class="modal-backdrop fade show" v-if="showDetailsModal"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ReclamationService from '@/Service/ReclamationService'
import api from '@/Service/api'
import { getEquipements } from '@/Service/EquipementService'

const route = useRoute()
const router = useRouter()

const reclamationsEtudiants = ref<any[]>([])
const reclamationsEnseignants = ref<any[]>([])
const allEquipements = ref<any[]>([])
const loading = ref(true)
const error = ref('')
const highlightedId = ref<number | null>(null)
const highlightedRow = ref<any>(null)
const userDetailsCache = new Map<number, any>()

// Modal de refus
const showRejectModal = ref(false)
const selectedReclamation = ref<any | null>(null)
const rejectMotif = ref('')
const showDetailsModal = ref(false)
const selectedDetailsReclamation = ref<any | null>(null)

// Filtres depuis les query params
const filterLaboId = computed(() => route.query.laboratoireId ? Number(route.query.laboratoireId) : null)
const filterLaboNom = computed(() => route.query.laboratoireNom as string || '')
const filterEquipId = computed(() => route.query.equipementId ? Number(route.query.equipementId) : null)
const filterEquipNom = computed(() => route.query.equipementNom as string || '')

// Fonction de filtrage commune avec tri par date décroissante
function filterReclamations(reclamations: any[]) {
  return reclamations
    .filter(r => {
      // Filtre par laboratoire
      if (filterLaboId.value) {
        const laboId = r.laboratoire?.id || r.laboratoireId || r.equipement?.laboratoire?.id
        if (laboId !== filterLaboId.value) return false
      }
      // Filtre par équipement
      if (filterEquipId.value) {
        const equipId = r.equipement?.id || r.equipementId
        if (equipId !== filterEquipId.value) return false
      }
      return true
    })
    // Trier par date décroissante (plus récent en premier)
    .sort((a, b) => {
      const dateA = new Date(getDateReclamation(a) || 0).getTime()
      const dateB = new Date(getDateReclamation(b) || 0).getTime()
      return dateB - dateA
    })
}

// Réclamations filtrées
const reclamationsEtudiantsFiltrees = computed(() => filterReclamations(reclamationsEtudiants.value))
const reclamationsEnseignantsFiltrees = computed(() => filterReclamations(reclamationsEnseignants.value))

// Effacer le filtre
function clearFilter() {
  router.replace({ path: '/technicien/reclamations' })
}

function pickFirstText(...values: any[]): string | undefined {
  for (const value of values) {
    if (typeof value === 'string' && value.trim()) return value.trim()
    if (typeof value === 'number' && Number.isFinite(value)) return String(value)
  }
  return undefined
}

function extractAuteurId(rec: any): number | null {
  const rawId = rec?.auteurId ?? rec?.userId ?? rec?.enseignant?.id ?? rec?.auteur?.id
  const parsed = Number(rawId)
  return Number.isFinite(parsed) && parsed > 0 ? parsed : null
}

async function fetchUserDetailsById(userId: number): Promise<any | null> {
  if (userDetailsCache.has(userId)) return userDetailsCache.get(userId)

  try {
    const byId = await api.get(`/users/${userId}`)
    if (byId?.data) {
      userDetailsCache.set(userId, byId.data)
      return byId.data
    }
  } catch {
    // fallback sur /users si /users/{id} indisponible
  }

  try {
    const usersRes = await api.get('/users')
    const users = Array.isArray(usersRes?.data)
      ? usersRes.data
      : Array.isArray(usersRes?.data?.content)
        ? usersRes.data.content
        : []
    const found = users.find((u: any) => Number(u?.id) === userId) || null
    userDetailsCache.set(userId, found)
    return found
  } catch {
    userDetailsCache.set(userId, null)
    return null
  }
}

function mergeReclamationWithUser(rec: any, user: any) {
  const auteur = {
    ...(rec?.auteur || rec?.enseignant || {}),
    ...user,
    departement: user?.departement ?? rec?.auteur?.departement ?? rec?.enseignant?.departement,
  }

  return {
    ...rec,
    auteur,
    enseignant: rec?.enseignant ? auteur : rec?.enseignant,
    cinAuteur: rec?.cinAuteur ?? user?.cin,
    cin: rec?.cin ?? user?.cin,
    niveauAuteur: rec?.niveauAuteur ?? user?.niveau ?? user?.niveauEtude ?? user?.niveau_etude ?? user?.niveauScolaire,
    classeAuteur: rec?.classeAuteur ?? user?.classe ?? user?.classeEtudiant ?? user?.classe_etudiant ?? user?.groupe,
    departementAuteur: rec?.departementAuteur ?? user?.departementNom ?? user?.departement?.nom,
    departementNomAuteur: rec?.departementNomAuteur ?? user?.departementNom ?? user?.departement?.nom,
  }
}

function getEquipementIdentifiant(rec: any): string {
  const equipId = Number(rec?.equipementId || rec?.equipement?.id)
  if (rec?.equipementIdentifiant) return rec.equipementIdentifiant
  if (rec?.identifiantEquipement) return rec.identifiantEquipement
  if (rec?.equipement?.identifiant) return rec.equipement.identifiant
  if (Number.isFinite(equipId) && equipId > 0) {
    const equip = allEquipements.value.find((e: any) => Number(e?.id) === equipId)
    if (equip?.identifiant) return equip.identifiant
    return `EQ-${String(equipId).padStart(4, '0')}`
  }
  return 'N/A'
}

async function fetchReclamations() {
  loading.value = true
  error.value = ''
  try {
    // Charger les équipements pour les identifiants
    try {
      const eqRes = await getEquipements()
      allEquipements.value = Array.isArray(eqRes.data) ? eqRes.data : []
      console.log('Équipements chargés pour identifiants:', allEquipements.value.length, allEquipements.value.slice(0, 3))
    } catch (eqErr) {
      console.error('Erreur chargement équipements:', eqErr)
      allEquipements.value = []
    }

    console.log("Chargement des réclamations...")
    
    const res = await ReclamationService.getAllReclamations()
    const data = Array.isArray(res.data) ? res.data : []
    
    console.log("Réclamations reçues:", data)

    // Filtrer par le rôle de l'utilisateur (enseignant.role ou via userId)
    let etudiants = data.filter((r: any) => {
      // Vérifier si c'est un étudiant via enseignant/auteur ou via le rôle
      const role = r.enseignant?.role || r.auteur?.role || r.roleAuteur || ''
      return role.toUpperCase().includes('ETUDIANT')
    })
    
    let enseignants = data.filter((r: any) => {
      const role = r.enseignant?.role || r.auteur?.role || r.roleAuteur || ''
      return role.toUpperCase().includes('ENSEIGNANT')
    })

    etudiants = await Promise.all(
      etudiants.map(async (rec: any) => {
        const hasAcademic = Boolean(
          pickFirstText(
            rec?.niveauAuteur,
            rec?.auteur?.niveau,
            rec?.enseignant?.niveau,
          ) &&
          pickFirstText(
            rec?.classeAuteur,
            rec?.auteur?.classe,
            rec?.enseignant?.classe,
          ) &&
          pickFirstText(
            rec?.departementNomAuteur,
            rec?.departementAuteur,
            rec?.auteur?.departement?.nom,
            rec?.enseignant?.departement?.nom,
          )
        )

        const hasCin = Boolean(pickFirstText(rec?.cinAuteur, rec?.cin, rec?.auteur?.cin, rec?.enseignant?.cin))
        if (hasAcademic && hasCin) return rec

        const auteurId = extractAuteurId(rec)
        if (!auteurId) return rec

        const user = await fetchUserDetailsById(auteurId)
        if (!user) return rec

        return mergeReclamationWithUser(rec, user)
      }),
    )

    enseignants = await Promise.all(
      enseignants.map(async (rec: any) => {
        if (pickFirstText(rec?.cinAuteur, rec?.cin, rec?.auteur?.cin, rec?.enseignant?.cin)) return rec
        const auteurId = extractAuteurId(rec)
        if (!auteurId) return rec
        const user = await fetchUserDetailsById(auteurId)
        if (!user) return rec
        return mergeReclamationWithUser(rec, user)
      }),
    )

    reclamationsEtudiants.value = etudiants
    reclamationsEnseignants.value = enseignants

    console.log("Réclamations étudiants:", reclamationsEtudiants.value)
    console.log("Réclamations enseignants:", reclamationsEnseignants.value)
    
    // Si aucune réclamation filtrée mais qu'on a des données
    // C'est peut-être que le backend ne retourne pas le rôle
    // Dans ce cas, on affiche tout dans une seule liste
    if (data.length > 0 && reclamationsEtudiants.value.length === 0 && reclamationsEnseignants.value.length === 0) {
      console.warn("Aucun filtre de rôle trouvé - affichage de toutes les réclamations")
      // Mettre toutes les réclamations dans enseignants par défaut
      reclamationsEnseignants.value = data
    }
  } catch (e: any) {
    console.error("Erreur chargement reclamations", e)
    error.value = "Erreur lors du chargement: " + (e.response?.data?.message || e.message)
  } finally {
    loading.value = false
  }
}

async function traiterReclamation(id: number) {
  try {
    const current = [...reclamationsEtudiants.value, ...reclamationsEnseignants.value].find((r: any) => Number(r?.id) === Number(id))
    await ReclamationService.traiterReclamation(id)
    let processed = 0
    let failed = 0
    if (current) {
      const bulkResult = await ReclamationService.traiterReclamationsLiees(
        current,
        [...reclamationsEtudiants.value, ...reclamationsEnseignants.value],
      )
      processed = bulkResult.processed
      failed = bulkResult.failed
    }

    if (processed > 0 || failed > 0) {
      const successPart = processed > 0 ? `${processed} réclamation(s) liée(s) traitée(s)` : ''
      const failPart = failed > 0 ? `${failed} échec(s)` : ''
      const details = [successPart, failPart].filter(Boolean).join(' | ')
      alert(`Réclamation traitée. ${details}`)
    }

    fetchReclamations()
  } catch (e) {
    console.error("Erreur traitement réclamation", e)
    alert("Erreur lors du traitement de la réclamation")
  }
}

function openRejectModal(rec: any) {
  selectedReclamation.value = rec
  rejectMotif.value = ''
  showRejectModal.value = true
}

function closeRejectModal() {
  showRejectModal.value = false
  selectedReclamation.value = null
  rejectMotif.value = ''
}

function openDetailsModal(rec: any) {
  selectedDetailsReclamation.value = rec
  showDetailsModal.value = true
}

function closeDetailsModal() {
  showDetailsModal.value = false
  selectedDetailsReclamation.value = null
}

async function confirmReject() {
  if (!selectedReclamation.value || !rejectMotif.value.trim()) return
  
  try {
    await ReclamationService.refuserReclamationAvecMotif(selectedReclamation.value.id, rejectMotif.value)
    closeRejectModal()
    fetchReclamations()
  } catch (e) {
    console.error("Erreur refus réclamation", e)
    alert("Erreur lors du refus de la réclamation")
  }
}

function getAuteurNom(rec: any) {
  if (rec.enseignant) {
    return `${rec.enseignant.prenom || ''} ${rec.enseignant.nom || ''}`.trim() || 'N/A'
  }
  if (rec.auteur) {
    return `${rec.auteur.prenom || ''} ${rec.auteur.nom || ''}`.trim() || 'N/A'
  }
  return `${rec.prenomAuteur || ''} ${rec.nomAuteur || ''}`.trim() || 'N/A'
}

function getAuteurCin(rec: any): string {
  return pickFirstText(
    rec?.cinAuteur,
    rec?.cin,
    rec?.auteur?.cin,
    rec?.enseignant?.cin,
  ) || 'N/A'
}

function getAuteurNiveau(rec: any): string {
  return pickFirstText(
    rec?.niveauAuteur,
    rec?.auteur?.niveau,
    rec?.auteur?.niveauEtude,
    rec?.auteur?.niveau_etude,
    rec?.auteur?.niveauScolaire,
    rec?.enseignant?.niveau,
    rec?.enseignant?.niveauEtude,
    rec?.enseignant?.niveau_etude,
    rec?.enseignant?.niveauScolaire,
  ) || 'Non renseigné'
}

function getAuteurClasse(rec: any): string {
  return pickFirstText(
    rec?.classeAuteur,
    rec?.auteur?.classe,
    rec?.auteur?.classeEtudiant,
    rec?.auteur?.classe_etudiant,
    rec?.auteur?.groupe,
    rec?.enseignant?.classe,
    rec?.enseignant?.classeEtudiant,
    rec?.enseignant?.classe_etudiant,
    rec?.enseignant?.groupe,
  ) || 'Non renseigné'
}

function getAuteurDepartement(rec: any): string {
  return pickFirstText(
    rec?.departementNomAuteur,
    rec?.departementAuteur,
    rec?.auteur?.departement?.nom,
    typeof rec?.auteur?.departement === 'string' ? rec?.auteur?.departement : undefined,
    rec?.auteur?.departementNom,
    rec?.enseignant?.departement?.nom,
    typeof rec?.enseignant?.departement === 'string' ? rec?.enseignant?.departement : undefined,
    rec?.enseignant?.departementNom,
  ) || 'Non renseigné'
}

function isEnseignantReclamation(rec: any): boolean {
  const role = pickFirstText(rec?.roleAuteur, rec?.auteur?.role, rec?.enseignant?.role)
  return String(role || '').toUpperCase().includes('ENSEIGNANT')
}

function truncateText(text: string, maxLength: number) {
  if (!text) return ''
  return text.length > maxLength ? text.substring(0, maxLength) + '...' : text
}

function getStatutBadge(statut: string) {
  const badges: Record<string, string> = {
    NOUVELLE: 'badge badge-info',
    EN_COURS: 'badge badge-warning',
    RESOLUE: 'badge badge-success',
    TRAITEE: 'badge badge-success',
    ANNULEE: 'badge badge-danger',
    NON_TRAITEE: 'badge badge-warning'
  }
  return badges[statut] || 'badge badge-secondary'
}

function formatStatut(statut: string) {
  const labels: Record<string, string> = {
    NOUVELLE: 'Nouvelle',
    EN_COURS: 'En cours',
    RESOLUE: 'Résolue',
    TRAITEE: 'Traitée',
    ANNULEE: 'Annulée',
    NON_TRAITEE: 'Non traitée'
  }
  return labels[statut] || statut
}

function getPrioriteBadge(priorite: string) {
  const badges: Record<string, string> = {
    HAUTE: 'badge badge-danger',
    MOYENNE: 'badge badge-warning',
    BASSE: 'badge badge-info'
  }
  return badges[priorite] || 'badge badge-secondary'
}

function formatPriorite(priorite: string) {
  const labels: Record<string, string> = {
    HAUTE: 'Haute',
    MOYENNE: 'Moyenne',
    BASSE: 'Basse'
  }
  return labels[priorite] || priorite || 'N/A'
}

function formatDate(date: string) {
  if (!date) return 'N/A'
  try {
    return new Date(date).toLocaleDateString('fr-FR')
  } catch {
    return date.split('T')[0]
  }
}

// Fonctions helper pour gérer les différents noms de champs du backend
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

function getStatut(rec: any): string {
  return rec.statut || rec.etat || rec.status || 'NOUVELLE'
}

function getDateReclamation(rec: any): string {
  return rec.dateCreation || rec.dateReclamation || rec.createdAt || rec.date || ''
}

function isEnAttente(rec: any): boolean {
  const statut = getStatut(rec)
  return statut === 'NOUVELLE' || statut === 'EN_COURS' || statut === 'NON_TRAITEE'
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
      // Retirer le highlight après 3 secondes
      setTimeout(() => {
        highlightedId.value = null
      }, 3000)
    }, 100)
  }
})
</script>

<style scoped>
.page-container { max-width: 1400px; margin: auto; padding: 20px; }
.card { border-radius: 12px; }
.table th, .table td { vertical-align: middle; }
.badge { padding: 5px 10px; font-size: 12px; }

/* Highlight animation pour la ligne venant d'une notification */
.highlight-row {
  animation: highlightPulse 1s ease-in-out 3;
  background-color: #d1fae5 !important;
}

@keyframes highlightPulse {
  0%, 100% { background-color: #d1fae5; }
  50% { background-color: #a7f3d0; }
}

@media (max-width: 768px) {
  .page-container { padding: 8px; }
  .content-header h1 { font-size: 1.2rem; }
  .card { border-radius: 8px; }
  .table th, .table td { font-size: 12px; padding: 6px 4px; }
  .btn-sm { font-size: 11px; padding: 3px 6px; }
  .btn-group { display: flex; flex-direction: column; gap: 4px; }
  .btn-group .btn { border-radius: 4px !important; }
}
</style>
