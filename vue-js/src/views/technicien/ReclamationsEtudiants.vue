<template>
  <div class="page-container">
    <section class="content-header">
      <div class="container-fluid">
        <h1><i class="fas fa-tools mr-2"></i> Réclamations Étudiants</h1>
        <p>Suivi des réclamations des étudiants de votre département</p>
      </div>
    </section>

    <section class="content">
      <div class="container-fluid">
        <div class="card">
          <div class="card-header">
            <h3 class="card-title">Liste des réclamations</h3>
          </div>

          <div class="card-body table-responsive">
            <table class="table table-bordered table-striped table-hover" v-if="reclamations.length">
              <thead class="bg-info text-white">
                <tr>
                  <th>Numéro</th>
                  <th class="text-center">Détails</th>
                  <th>Étudiant</th>
                  <th>Laboratoire</th>
                  <th>Équipement</th>
                  <th>Description</th>
                  <th>Identifiant équipement</th>
                  <th>État</th>
                  <th>Date</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(rec, index) in reclamations" :key="rec.id">
                  <td>{{ index + 1 }}</td>
                  <td class="text-center">
                    <button class="btn btn-info btn-sm" @click="openDetailsModal(rec)">
                      <i class="fas fa-info-circle mr-1"></i> Détails
                    </button>
                  </td>
                  <td>{{ getAuteurNom(rec) }}</td>
                  <td>{{ rec.laboratoireNom || 'N/A' }}</td>
                  <td>{{ rec.equipementNom || 'N/A' }}</td>
                  <td>{{ truncateText(rec.description, 40) }}</td>
                  <td>{{ getEquipementIdentifiant(rec) }}</td>
                  <td>
                    <span :class="getEtatBadge(rec.etat)">
                      {{ formatEtat(rec.etat) }}
                    </span>
                  </td>
                  <td>{{ formatDate(rec.dateReclamation) }}</td>
                  <td class="d-flex gap-1">
                    <button
                      class="btn btn-success btn-sm"
                      @click="traiterReclamation(rec.id)"
                      :disabled="isActionDisabled(rec)"
                      :title="getActionDisabledReason(rec)"
                    >
                      <i class="fas fa-check mr-1"></i> Traiter
                    </button>
                    <button
                      class="btn btn-danger btn-sm"
                      @click="annulerReclamation(rec.id)"
                      :disabled="isActionDisabled(rec)"
                      :title="getActionDisabledReason(rec)"
                    >
                      <i class="fas fa-times mr-1"></i> Annuler
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>

            <div v-else class="text-center text-muted p-4">
              <i class="fas fa-inbox fa-3x mb-3"></i>
              <p>Aucune réclamation pour le moment</p>
            </div>
          </div>

          <div class="card-footer">
            <span>{{ reclamations.length }} réclamation(s)</span>
          </div>
        </div>
      </div>
    </section>

    <div class="modal fade" :class="{ show: showDetailsModal }" :style="{ display: showDetailsModal ? 'block' : 'none' }">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header bg-info text-white">
            <h5 class="modal-title"><i class="fas fa-user-circle mr-2"></i>Détails de l'étudiant</h5>
            <button type="button" class="close text-white" @click="closeDetailsModal">&times;</button>
          </div>
          <div class="modal-body">
            <table class="table table-bordered mb-0" v-if="selectedDetailsReclamation">
              <tbody>
                <tr>
                  <th style="width: 220px;">Nom complet</th>
                  <td>{{ getAuteurNom(selectedDetailsReclamation) }}</td>
                </tr>
                <tr>
                  <th>CIN</th>
                  <td>{{ getAuteurCin(selectedDetailsReclamation) }}</td>
                </tr>
                <tr>
                  <th>Niveau</th>
                  <td>{{ getAuteurNiveau(selectedDetailsReclamation) }}</td>
                </tr>
                <tr>
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
import { ref, onMounted } from 'vue'
import ReclamationService from '@/Service/ReclamationService'
import { getEquipements } from '@/Service/EquipementService'
import api from '@/Service/api'

interface Reclamation {
  id: number
  description: string
  quantite: number
  etat: string
  dateReclamation: string
  equipementId?: number
  equipementIdentifiant?: string
  identifiantEquipement?: string
  laboratoireNom?: string
  equipementNom?: string
  nomAuteur?: string
  prenomAuteur?: string
  cinAuteur?: string
  cin?: string
  niveauAuteur?: string
  classeAuteur?: string
  departementAuteur?: string
  departementNomAuteur?: string
  auteur?: {
    id?: number
    role?: string
    cin?: string
    niveau?: string
    niveauEtude?: string
    niveau_etude?: string
    niveauScolaire?: string
    classe?: string
    classeEtudiant?: string
    classe_etudiant?: string
    groupe?: string
    departement?: {
      nom?: string
    } | string
    departementNom?: string
  }
  enseignant?: {
    id?: number
    role?: string
    cin?: string
    niveau?: string
    niveauEtude?: string
    niveau_etude?: string
    niveauScolaire?: string
    classe?: string
    classeEtudiant?: string
    classe_etudiant?: string
    groupe?: string
    departement?: {
      nom?: string
    } | string
    departementNom?: string
  }
  etudiant?: {
    id?: number
    role?: string
    cin?: string
    niveau?: string
    niveauEtude?: string
    niveau_etude?: string
    niveauScolaire?: string
    classe?: string
    classeEtudiant?: string
    classe_etudiant?: string
    groupe?: string
    departement?: {
      nom?: string
    } | string
    departementNom?: string
  }
}

const reclamations = ref<Reclamation[]>([])
const equipementsById = ref<Record<number, any>>({})
const showDetailsModal = ref(false)
const selectedDetailsReclamation = ref<Reclamation | null>(null)
const userDetailsCache = new Map<number, any>()

function pickFirstText(...values: any[]): string | undefined {
  for (const value of values) {
    if (typeof value === 'string' && value.trim()) return value.trim()
    if (typeof value === 'number' && Number.isFinite(value)) return String(value)
  }
  return undefined
}

function getAuteurCin(rec: Reclamation): string {
  return pickFirstText(rec.cinAuteur, rec.cin, rec.auteur?.cin, rec.etudiant?.cin, rec.enseignant?.cin) || 'N/A'
}

function getAuteurNiveau(rec: Reclamation): string {
  return pickFirstText(
    rec.niveauAuteur,
    rec.auteur?.niveau,
    rec.auteur?.niveauEtude,
    rec.auteur?.niveau_etude,
    rec.auteur?.niveauScolaire,
    rec.etudiant?.niveau,
    rec.etudiant?.niveauEtude,
    rec.etudiant?.niveau_etude,
    rec.etudiant?.niveauScolaire,
    rec.enseignant?.niveau,
    rec.enseignant?.niveauEtude,
    rec.enseignant?.niveau_etude,
    rec.enseignant?.niveauScolaire,
  ) || 'Non renseigné'
}

function getAuteurClasse(rec: Reclamation): string {
  return pickFirstText(
    rec.classeAuteur,
    rec.auteur?.classe,
    rec.auteur?.classeEtudiant,
    rec.auteur?.classe_etudiant,
    rec.auteur?.groupe,
    rec.etudiant?.classe,
    rec.etudiant?.classeEtudiant,
    rec.etudiant?.classe_etudiant,
    rec.etudiant?.groupe,
    rec.enseignant?.classe,
    rec.enseignant?.classeEtudiant,
    rec.enseignant?.classe_etudiant,
    rec.enseignant?.groupe,
  ) || 'Non renseigné'
}

function getAuteurDepartement(rec: Reclamation): string {
  return pickFirstText(
    rec.departementNomAuteur,
    rec.departementAuteur,
    rec.auteur?.departement && typeof rec.auteur.departement === 'string' ? rec.auteur.departement : undefined,
    rec.auteur?.departement && typeof rec.auteur.departement !== 'string' ? rec.auteur.departement.nom : undefined,
    rec.auteur?.departementNom,
    rec.etudiant?.departement && typeof rec.etudiant.departement === 'string' ? rec.etudiant.departement : undefined,
    rec.etudiant?.departement && typeof rec.etudiant.departement !== 'string' ? rec.etudiant.departement.nom : undefined,
    rec.etudiant?.departementNom,
    rec.enseignant?.departement && typeof rec.enseignant.departement === 'string' ? rec.enseignant.departement : undefined,
    rec.enseignant?.departement && typeof rec.enseignant.departement !== 'string' ? rec.enseignant.departement.nom : undefined,
    rec.enseignant?.departementNom,
  ) || 'Non renseigné'
}

function getAuteurNom(rec: Reclamation): string {
  return `${rec.prenomAuteur || ''} ${rec.nomAuteur || ''}`.trim() || 'N/A'
}

function openDetailsModal(rec: Reclamation) {
  selectedDetailsReclamation.value = rec
  showDetailsModal.value = true
}

function closeDetailsModal() {
  showDetailsModal.value = false
  selectedDetailsReclamation.value = null
}

function extractAuteurId(rec: any): number | null {
  const rawId = rec?.auteurId ?? rec?.userId ?? rec?.etudiant?.id ?? rec?.auteur?.id ?? rec?.enseignant?.id
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
    // fallback vers /users si /users/{id} indisponible
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

function mergeReclamationWithUser(rec: any, user: any): Reclamation {
  const auteur = {
    ...(rec?.auteur || rec?.etudiant || rec?.enseignant || {}),
    ...user,
    departement: user?.departement ?? rec?.auteur?.departement ?? rec?.etudiant?.departement ?? rec?.enseignant?.departement,
  }

  return {
    ...rec,
    auteur,
    etudiant: rec?.etudiant ? auteur : rec?.etudiant,
    enseignant: rec?.enseignant ? auteur : rec?.enseignant,
    cinAuteur: rec?.cinAuteur ?? user?.cin,
    cin: rec?.cin ?? user?.cin,
    niveauAuteur: rec?.niveauAuteur ?? user?.niveau ?? user?.niveauEtude ?? user?.niveau_etude ?? user?.niveauScolaire,
    classeAuteur: rec?.classeAuteur ?? user?.classe ?? user?.classeEtudiant ?? user?.classe_etudiant ?? user?.groupe,
    departementAuteur: rec?.departementAuteur ?? user?.departementNom ?? user?.departement?.nom,
    departementNomAuteur: rec?.departementNomAuteur ?? user?.departementNom ?? user?.departement?.nom,
  }
}

// Charger les reclamations des etudiants du departement du technicien
async function fetchReclamations() {
  try {
    const [reclamationsRes, equipementsRes] = await Promise.all([
      ReclamationService.getReclamationsPourTechnicien(),
      getEquipements()
    ])

    const allEquipements = Array.isArray(equipementsRes.data) ? equipementsRes.data : []
    equipementsById.value = allEquipements.reduce((acc: Record<number, any>, eq: any) => {
      const id = Number(eq?.id)
      if (Number.isFinite(id) && id > 0) acc[id] = eq
      return acc
    }, {})

    const data = Array.isArray(reclamationsRes.data) ? reclamationsRes.data : []
    let etudiants = data.filter((r: any) => {
      const role = String(r?.roleAuteur || r?.auteur?.role || r?.etudiant?.role || r?.enseignant?.role || '').toUpperCase()
      return role.includes('ETUDIANT')
    })

    etudiants = await Promise.all(
      etudiants.map(async (rec: any) => {
        const hasAcademic = Boolean(
          pickFirstText(
            rec?.niveauAuteur,
            rec?.auteur?.niveau,
            rec?.etudiant?.niveau,
            rec?.enseignant?.niveau,
          ) &&
          pickFirstText(
            rec?.classeAuteur,
            rec?.auteur?.classe,
            rec?.etudiant?.classe,
            rec?.enseignant?.classe,
          ) &&
          pickFirstText(
            rec?.departementNomAuteur,
            rec?.departementAuteur,
            rec?.auteur?.departement?.nom,
            rec?.etudiant?.departement?.nom,
            rec?.enseignant?.departement?.nom,
            rec?.auteur?.departementNom,
            rec?.etudiant?.departementNom,
            rec?.enseignant?.departementNom,
          ),
        )

        if (hasAcademic) return rec

        const auteurId = extractAuteurId(rec)
        if (!auteurId) return rec

        const user = await fetchUserDetailsById(auteurId)
        if (!user) return rec

        return mergeReclamationWithUser(rec, user)
      }),
    )

    reclamations.value = etudiants
  } catch (error) {
    console.error('Erreur chargement reclamations:', error)
    reclamations.value = []
  }
}

// x Traiter une réclamation (Approuver) + auto-traiter les doublons
async function traiterReclamation(id: number) {
  const rec = reclamations.value.find(r => r.id === id) as any
  if (rec && isActionDisabled(rec)) {
    alert(getActionDisabledReason(rec))
    return
  }

  try {
    console.log(`Traitement réclamation #${id}...`);
    const response = await ReclamationService.traiterReclamation(id);
    console.log("Réponse traitement:", response.data);
    const bulkResult = await ReclamationService.traiterReclamationsLiees(rec, reclamations.value)
    if (bulkResult.processed > 0 || bulkResult.failed > 0) {
      const successPart = bulkResult.processed > 0 ? `${bulkResult.processed} réclamation(s) liée(s) traitée(s)` : ''
      const failPart = bulkResult.failed > 0 ? `${bulkResult.failed} échec(s)` : ''
      const details = [successPart, failPart].filter(Boolean).join(' | ')
      alert(`Réclamation approuvée avec succès ! ${details}`)
    } else {
      alert("Réclamation approuvée avec succès ! Une notification a été envoyée à l'étudiant.")
    }
    await fetchReclamations();
  } catch (error: any) {
    console.error('Erreur traitement:', error);
    console.error('Détails:', error.response?.data || error.message);
    alert("Erreur: " + (error.response?.data?.message || error.response?.data || error.message));
  }
}

// x Annuler une réclamation (Refuser)
async function annulerReclamation(id: number) {
  const rec = reclamations.value.find(r => r.id === id) as any
  if (rec && isActionDisabled(rec)) {
    alert(getActionDisabledReason(rec))
    return
  }

  // Demander le motif de refus (obligatoire)
  const motif = prompt("Veuillez indiquer le motif de refus (obligatoire) :");
  
  if (motif === null) {
    // L'utilisateur a annulé
    return;
  }
  
  if (!motif || motif.trim() === '') {
    alert("R Le motif de refus est obligatoire pour refuser une réclamation.");
    return;
  }
  
  if (!confirm(`Voulez-vous vraiment refuser cette réclamation ?\n\nMotif: ${motif}`)) return;
  
  try {
    console.log(`Refus réclamation #${id} avec motif: ${motif}`);
    const response = await ReclamationService.refuserReclamationAvecMotif(id, motif.trim());
    console.log("Réponse refus:", response.data);
    alert("Réclamation refusée. Une notification a été envoyée à l'étudiant.");
    await fetchReclamations();
  } catch (error: any) {
    console.error('Erreur refus:', error);
    console.error('Détails:', error.response?.data || error.message);
    alert("Erreur: " + (error.response?.data?.message || error.response?.data || error.message));
  }
}

// x Badge couleur selon l'état
function getEtatBadge(etat: string) {
  return {
    NON_TRAITEE: 'badge badge-warning',
    TRAITEE: 'badge badge-success',
    ANNULEE: 'badge badge-danger'
  }[etat] || 'badge badge-secondary'
}

// x Texte lisible pour l'état
function formatEtat(etat: string) {
  return {
    NON_TRAITEE: 'Non traitée',
    TRAITEE: 'Traitée',
    ANNULEE: 'Annulée'
  }[etat] || etat
}

// x Format date
function formatDate(date: string) {
  return date ? date.split('T')[0] : 'N/A'
}

// x Tronquer le texte
function truncateText(text: string, maxLength: number) {
  if (!text) return ''
  return text.length > maxLength ? text.substring(0, maxLength) + '...' : text
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

onMounted(fetchReclamations)
</script>

<style scoped>
.page-container { max-width: 1400px; margin: 0 auto; padding: 20px; }
.card { background: #fff; border-radius: 16px; box-shadow: 0 2px 6px rgba(0,0,0,0.08); overflow: hidden; margin-bottom: 20px; }
.card-header { padding: 20px; font-weight: 600; font-size: 18px; }
.table th, .table td { vertical-align: middle; }
.badge { padding: 5px 10px; border-radius: 4px; font-size: 12px; text-transform: uppercase; }
.d-flex.gap-1 button { margin-right: 4px; }
.card-footer { font-size: 14px; color: #64748b; padding: 12px 20px; }

@media (max-width: 768px) {
  .page-container { padding: 8px; }
  .card { border-radius: 8px; }
  .card-header { font-size: 15px; padding: 12px; }
  .table th, .table td { font-size: 12px; padding: 6px 4px; }
  .btn-sm { font-size: 11px; padding: 3px 6px; }
  .d-flex.gap-1 { flex-direction: column; gap: 4px !important; }
}
</style>
