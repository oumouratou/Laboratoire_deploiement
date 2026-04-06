<template>
  <div class="dashboard-container">
    <h2><i class="fas fa-tachometer-alt mr-2"></i> Dashboard Technicien</h2>

    <!-- Stats Cards -->
    <div class="stats-grid">
      <div class="stat-card bg-warning">
        <div class="stat-icon"><i class="fas fa-clock"></i></div>
        <div class="stat-info">
          <span class="stat-value">{{ reclamationsNonTraitees }}</span>
          <span class="stat-label">Réclamations en attente</span>
        </div>
      </div>
      <div class="stat-card bg-success">
        <div class="stat-icon"><i class="fas fa-check-circle"></i></div>
        <div class="stat-info">
          <span class="stat-value">{{ reclamationsTraitees }}</span>
          <span class="stat-label">Réclamations traitées</span>
        </div>
      </div>
      <div class="stat-card bg-primary">
        <div class="stat-icon"><i class="fas fa-list"></i></div>
        <div class="stat-info">
          <span class="stat-value">{{ reclamations.length }}</span>
          <span class="stat-label">Total réclamations</span>
        </div>
      </div>
      <div class="stat-card bg-info">
        <div class="stat-icon"><i class="fas fa-exclamation-triangle"></i></div>
        <div class="stat-info">
          <span class="stat-value">{{ reclamationsRefusees }}</span>
          <span class="stat-label">Refusées</span>
        </div>
      </div>
    </div>

    <!-- Réclamations récentes à traiter -->
    <div class="card">
      <div class="card-header d-flex justify-content-between align-items-center">
        <h3 class="card-title mb-0"><i class="fas fa-exclamation-triangle text-warning mr-2"></i> Réclamations à traiter</h3>
        <router-link to="/technicien/reclamations" class="btn btn-sm btn-outline-warning">Voir tout</router-link>
      </div>
      <div class="card-body table-responsive p-0">
        <table class="table table-hover mb-0" v-if="recentReclamationsNonTraitees.length">
          <thead class="bg-light">
            <tr>
              <th>Numéro</th>
              <th>Auteur</th>
              <th>CIN</th>
              <th>Rôle</th>
              <th>Laboratoire</th>
              <th>Équipement</th>
              <th>Description</th>
              <th>Date</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(rec, index) in recentReclamationsNonTraitees" :key="rec.id">
              <td><strong>{{ index + 1 }}</strong></td>
              <td>{{ getAuteurNom(rec) }}</td>
              <td>{{ rec.cinAuteur || 'N/A' }}</td>
              <td><span :class="getRoleBadge(rec.roleAuteur)">{{ formatRole(rec.roleAuteur) }}</span></td>
              <td>{{ rec.laboratoireNom || 'N/A' }}</td>
              <td>{{ rec.equipementNom || 'N/A' }}</td>
              <td>{{ truncate(rec.description, 30) }}</td>
              <td>{{ formatDate(rec.dateReclamation) }}</td>
              <td>
                <div class="btn-group">
                  <button class="btn btn-success btn-sm" @click="traiterReclamation(rec.id)" title="Approuver">
                    <i class="fas fa-check mr-1"></i> Approuver
                  </button>
                  <button class="btn btn-danger btn-sm" @click="annulerReclamation(rec.id)" title="Annuler">
                    <i class="fas fa-times mr-1"></i> Annuler
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-else class="text-center text-muted p-4">
          <i class="fas fa-inbox fa-3x mb-3"></i>
          <p>Aucune réclamation en attente</p>
        </div>
      </div>
    </div>

    <!-- Réclamations récentes traitées -->
    <div class="card mt-4">
      <div class="card-header">
        <h3 class="card-title mb-0"><i class="fas fa-history text-info mr-2"></i> Dernières réclamations traitées</h3>
      </div>
      <div class="card-body table-responsive p-0">
        <table class="table table-hover mb-0" v-if="recentReclamationsTraitees.length">
          <thead class="bg-light">
            <tr>
              <th>Numéro</th>
              <th>Auteur</th>
              <th>Description</th>
              <th>État</th>
              <th>Date</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(rec, index) in recentReclamationsTraitees" :key="rec.id">
              <td>{{ index + 1 }}</td>
              <td>{{ getAuteurNom(rec) }}</td>
              <td>{{ truncate(rec.description, 50) }}</td>
              <td><span :class="badgeEtat(rec.etat || rec.statut || '')">{{ formatEtat(rec.etat || rec.statut || '') }}</span></td>
              <td>{{ formatDate(rec.dateReclamation || rec.dateCreation) }}</td>
            </tr>
          </tbody>
        </table>
        <div v-else class="text-center text-muted p-4">
          <p>Aucune réclamation traitée récemment</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import ReclamationService from "@/Service/ReclamationService";

interface Reclamation {
  id: number;
  description: string;
  etat: string;
  statut?: string;
  nomAuteur: string;
  prenomAuteur: string;
  roleAuteur?: string;
  cinAuteur?: string;
  laboratoireNom?: string;
  equipementNom?: string;
  dateReclamation?: string;
  dateCreation?: string;
}

const reclamations = ref<Reclamation[]>([]);

// Statistiques calculées
const reclamationsNonTraitees = computed(() => 
  reclamations.value.filter(r => r.etat === 'NON_TRAITEE').length
);
const reclamationsTraitees = computed(() => 
  reclamations.value.filter(r => r.etat === 'TRAITEE').length
);
const reclamationsRefusees = computed(() => 
  reclamations.value.filter(r => r.etat === 'REFUSEE' || r.etat === 'ANNULEE').length
);

// Réclamations récentes non traitées (max 5)
const recentReclamationsNonTraitees = computed(() => 
  reclamations.value.filter(r => r.etat === 'NON_TRAITEE').slice(0, 5)
);

// Réclamations récentes traitées (max 5)
const recentReclamationsTraitees = computed(() => 
  reclamations.value.filter(r => 
    r.etat === 'TRAITEE' || r.etat === 'ANNULEE' || r.etat === 'RESOLUE' || r.etat === 'REFUSEE' ||
    r.statut === 'TRAITEE' || r.statut === 'RESOLUE' || r.statut === 'ANNULEE' || r.statut === 'REFUSEE'
  ).slice(0, 5)
);

// Fonctions d'extraction pour réclamations
function getAuteurNom(rec: Reclamation): string {
  return rec.prenomAuteur && rec.nomAuteur ? `${rec.prenomAuteur} ${rec.nomAuteur}` : 'N/A';
}

function badgeEtat(etat: string) {
  return { 
    NON_TRAITEE: "badge badge-warning", 
    NOUVELLE: "badge badge-info",
    EN_COURS: "badge badge-primary",
    TRAITEE: "badge badge-success", 
    RESOLUE: "badge badge-success",
    ANNULEE: "badge badge-secondary",
    REFUSEE: "badge badge-danger" 
  }[etat] || "badge badge-secondary";
}

function formatEtat(etat: string) {
  return { 
    NON_TRAITEE: "Non traitée", 
    NOUVELLE: "Nouvelle",
    EN_COURS: "En cours",
    TRAITEE: "Traitée", 
    RESOLUE: "Résolue",
    ANNULEE: "Annulée",
    REFUSEE: "Refusée" 
  }[etat] || etat;
}

function getRoleBadge(role?: string) {
  return {
    ETUDIANT: "badge badge-info",
    ENSEIGNANT: "badge badge-primary"
  }[role || ''] || "badge badge-secondary";
}

function formatRole(role?: string) {
  return { ETUDIANT: "Étudiant", ENSEIGNANT: "Enseignant" }[role || ''] || role || 'N/A';
}

function formatDate(date: string | undefined) {
  if (!date) return 'N/A';
  try {
    return new Date(date).toLocaleDateString('fr-FR', { 
      day: '2-digit', 
      month: '2-digit', 
      year: 'numeric'
    });
  } catch {
    return date.split('T')[0];
  }
}

function truncate(text?: string, length?: number) {
  if (!text) return '';
  const maxLen = length || 50;
  return text.length > maxLen ? text.substring(0, maxLen) + '...' : text;
}

// Traiter une réclamation (Approuver)
async function traiterReclamation(id: number) {
  try {
    console.log(`Traitement réclamation #${id}...`);
    const response = await ReclamationService.traiterReclamation(id);
    console.log("Réponse traitement:", response.data);
    alert("Réclamation approuvée avec succès ! Une notification a été envoyée.");
    await fetchData();
  } catch (error: any) {
    console.error("Erreur traitement:", error);
    console.error("Détails:", error.response?.data || error.message);
    alert("Erreur: " + (error.response?.data?.message || error.response?.data || error.message));
  }
}

// Annuler une réclamation (Refuser)
async function annulerReclamation(id: number) {
  const motif = prompt("Veuillez saisir le motif de refus (obligatoire) :");
  if (!motif || motif.trim() === "") {
    alert("Le motif de refus est obligatoire pour refuser une réclamation.");
    return;
  }
  try {
    console.log(`Annulation réclamation #${id} avec motif: ${motif}`);
    const response = await ReclamationService.refuserReclamationAvecMotif(id, motif);
    console.log("Réponse annulation:", response.data);
    alert("Réclamation refusée. Une notification a été envoyée.");
    await fetchData();
  } catch (error: any) {
    console.error("Erreur annulation:", error);
    console.error("Détails:", error.response?.data || error.message);
    alert("Erreur: " + (error.response?.data?.message || error.response?.data || error.message));
  }
}

const fetchData = async () => {
  try {
    console.log("Chargement des données technicien...");
    
    // Réclamations
    const resRec = await ReclamationService.getReclamationsPourTechnicien();
    console.log("Réponse réclamations:", resRec.data);
    reclamations.value = Array.isArray(resRec.data) ? resRec.data : [];
    
  } catch (e: any) {
    console.error("Erreur chargement données:", e);
    console.error("Détails:", e.response?.data || e.message);
  }
};

onMounted(fetchData);
</script>

<style scoped>
.dashboard-container { 
  max-width: 1400px; 
  margin: 0 auto; 
  padding: 20px; 
}

.stats-grid { 
  display: grid; 
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); 
  gap: 20px; 
  margin-bottom: 30px; 
}

.stat-card { 
  background: #fff; 
  padding: 20px; 
  border-radius: 12px; 
  display: flex; 
  align-items: center; 
  gap: 15px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.stat-card.bg-warning { border-left: 4px solid #ffc107; }
.stat-card.bg-success { border-left: 4px solid #28a745; }
.stat-card.bg-danger { border-left: 4px solid #dc3545; }
.stat-card.bg-info { border-left: 4px solid #17a2b8; }
.stat-card.bg-primary { border-left: 4px solid #007bff; }

.stat-icon { 
  font-size: 2rem; 
  opacity: 0.8; 
}

.stat-card.bg-warning .stat-icon { color: #ffc107; }
.stat-card.bg-success .stat-icon { color: #28a745; }
.stat-card.bg-danger .stat-icon { color: #dc3545; }
.stat-card.bg-info .stat-icon { color: #17a2b8; }
.stat-card.bg-primary .stat-icon { color: #007bff; }

.stat-info { 
  display: flex; 
  flex-direction: column; 
}

.stat-value { 
  font-size: 1.8rem; 
  font-weight: bold; 
  color: #333; 
}

.stat-label { 
  font-size: 0.9rem; 
  color: #666; 
}

.card { 
  background: #fff; 
  border-radius: 12px; 
  box-shadow: 0 2px 8px rgba(0,0,0,0.1); 
  overflow: hidden; 
}

.card-header { 
  padding: 15px 20px; 
  background: #f8f9fa; 
  border-bottom: 1px solid #eee; 
}

.card-title { 
  font-size: 1.1rem; 
  font-weight: 600; 
}

.table th { 
  font-weight: 600; 
  font-size: 0.85rem; 
  text-transform: uppercase; 
  color: #666; 
}

.table td { 
  vertical-align: middle; 
}

.badge { 
  padding: 5px 10px; 
  border-radius: 4px; 
  font-size: 0.75rem; 
}
</style>
