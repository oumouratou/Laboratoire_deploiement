<template>
  <div class="dashboard-container">
    <h2><i class="fas fa-tachometer-alt mr-2"></i> Tableau de bord Étudiant</h2>

    <!-- Stats Cards -->
    <div class="stats-grid">
      <div class="stat-card bg-primary">
        <div class="stat-icon"><i class="fas fa-calendar-check"></i></div>
        <div class="stat-info">
          <span class="stat-value">{{ reservations.length }}</span>
          <span class="stat-label">Mes réservations</span>
        </div>
      </div>
      <div class="stat-card bg-warning">
        <div class="stat-icon"><i class="fas fa-exclamation-triangle"></i></div>
        <div class="stat-info">
          <span class="stat-value">{{ reclamations.length }}</span>
          <span class="stat-label">Mes réclamations</span>
        </div>
      </div>
      <div class="stat-card bg-success">
        <div class="stat-icon"><i class="fas fa-check-circle"></i></div>
        <div class="stat-info">
          <span class="stat-value">{{ reservationsApprouvees }}</span>
          <span class="stat-label">Réservations approuvées</span>
        </div>
      </div>
      <div class="stat-card bg-info">
        <div class="stat-icon"><i class="fas fa-clock"></i></div>
        <div class="stat-info">
          <span class="stat-value">{{ reclamationsEnAttente }}</span>
          <span class="stat-label">Réclamations en attente</span>
        </div>
      </div>
    </div>

    <!-- Réservations récentes -->
    <div class="card mb-4">
      <div class="card-header d-flex justify-content-between align-items-center">
        <h3 class="card-title mb-0"><i class="fas fa-calendar text-primary mr-2"></i> Mes réservations récentes</h3>
        <router-link to="/etudiant/reservations" class="btn btn-sm btn-outline-primary">Voir tout</router-link>
      </div>
      <div class="card-body table-responsive p-0">
        <table class="table table-hover mb-0" v-if="recentReservations.length">
          <thead class="bg-light">
            <tr>
              <th>Laboratoire</th>
              <th>Date</th>
              <th>Horaire</th>
              <th>Description/Motif</th>
              <th>Statut</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="res in recentReservations" :key="res.id">
              <td>{{ res.laboratoireNom || 'N/A' }}</td>
              <td>{{ formatDate(res.dateReservation) }}</td>
              <td>{{ res.heureDebut }} - {{ res.heureFin }}</td>
              <td>{{ truncateText(res.motif || res.description, 30) }}</td>
              <td><span :class="getReservationBadge(res.statut)">{{ formatReservationStatus(res.statut) }}</span></td>
            </tr>
          </tbody>
        </table>
        <div v-else class="text-center text-muted p-4">
          <i class="fas fa-calendar-times fa-2x mb-2"></i>
          <p class="mb-0">Aucune réservation</p>
        </div>
      </div>
    </div>

    <!-- Réclamations récentes -->
    <div class="card">
      <div class="card-header d-flex justify-content-between align-items-center">
        <h3 class="card-title mb-0"><i class="fas fa-exclamation-circle text-warning mr-2"></i> Mes réclamations récentes</h3>
        <router-link to="/etudiant/reclamations" class="btn btn-sm btn-outline-warning">Voir tout</router-link>
      </div>
      <div class="card-body table-responsive p-0">
        <table class="table table-hover mb-0" v-if="recentReclamations.length">
          <thead class="bg-light">
            <tr>
              <th>Équipement</th>
              <th>Description</th>
              <th>Date</th>
              <th>Statut</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="rec in recentReclamations" :key="rec.id">
              <td>{{ rec.equipementNom || 'N/A' }}</td>
              <td>{{ truncateText(rec.description, 40) }}</td>
              <td>{{ formatDate(rec.dateReclamation) }}</td>
              <td><span :class="getReclamationBadge(rec.etat)">{{ formatReclamationStatus(rec.etat) }}</span></td>
            </tr>
          </tbody>
        </table>
        <div v-else class="text-center text-muted p-4">
          <i class="fas fa-inbox fa-2x mb-2"></i>
          <p class="mb-0">Aucune réclamation</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import ReclamationService from "@/Service/ReclamationService";
import { getReservationsByEtudiant, getMesReservations } from "@/Service/ReservationService";
import { useAuthStore } from "@/stores/auth";

const authStore = useAuthStore();

interface Reclamation {
  id: number;
  description: string;
  etat: string;
  equipementNom?: string;
  dateReclamation?: string;
}

interface Reservation {
  id: number;
  laboratoireNom?: string;
  dateReservation: string;
  heureDebut: string;
  heureFin: string;
  statut: string;
  motif?: string;
  description?: string;
}

const reclamations = ref<Reclamation[]>([]);
const reservations = ref<Reservation[]>([]);

// Stats calculées
const reservationsApprouvees = computed(() => reservations.value.filter(r => r.statut === 'APPROUVEE').length);
const reclamationsEnAttente = computed(() => reclamations.value.filter(r => r.etat === 'NON_TRAITEE').length);

// Récents (5 derniers) - triés par date décroissante (plus récent en premier)
const recentReservations = computed(() => {
  return [...reservations.value]
    .sort((a, b) => {
      const dateA = new Date(a.dateReservation || 0).getTime();
      const dateB = new Date(b.dateReservation || 0).getTime();
      return dateB - dateA; // Décroissant
    })
    .slice(0, 5);
});

const recentReclamations = computed(() => {
  return [...reclamations.value]
    .sort((a, b) => {
      const dateA = new Date(a.dateReclamation || 0).getTime();
      const dateB = new Date(b.dateReclamation || 0).getTime();
      return dateB - dateA; // Décroissant
    })
    .slice(0, 5);
});

function getReclamationBadge(etat: string) {
  return { 
    NON_TRAITEE: "badge badge-warning", 
    TRAITEE: "badge badge-success", 
    ANNULEE: "badge badge-danger" 
  }[etat] || "badge badge-secondary";
}

function formatReclamationStatus(etat: string) {
  return { NON_TRAITEE: "En attente", TRAITEE: "Traitée", ANNULEE: "Annulée" }[etat] || etat;
}

function getReservationBadge(statut: string) {
  return { 
    EN_ATTENTE: "badge badge-warning", 
    APPROUVEE: "badge badge-success", 
    REFUSEE: "badge badge-danger" 
  }[statut] || "badge badge-secondary";
}

function formatReservationStatus(statut: string) {
  return { EN_ATTENTE: "En attente", APPROUVEE: "Approuvée", REFUSEE: "Refusée" }[statut] || statut;
}

function formatDate(date: string | undefined) {
  if (!date) return 'N/A';
  try {
    return new Date(date).toLocaleDateString('fr-FR');
  } catch {
    return date.split('T')[0];
  }
}

function truncateText(text?: string, maxLength?: number) {
  if (!text) return '';
  const max = maxLength || 50;
  return text.length > max ? text.substring(0, max) + '...' : text;
}

const fetchReclamations = async () => {
  try {
    const res = await ReclamationService.getReclamationsEtudiantConnecte();
    reclamations.value = Array.isArray(res.data) ? res.data : [];
  } catch (e) {
    console.error("Erreur chargement réclamations:", e);
  }
};

const fetchReservations = async () => {
  try {
    // Essayer d'abord getMesReservations, puis getReservationsByEtudiant
    try {
      const res = await getMesReservations();
      reservations.value = Array.isArray(res.data) ? res.data : [];
    } catch {
      const userId = authStore.currentUser?.id;
      if (userId) {
        const res = await getReservationsByEtudiant(userId);
        reservations.value = Array.isArray(res.data) ? res.data : [];
      }
    }
  } catch (e) {
    console.error("Erreur chargement réservations:", e);
  }
};

onMounted(() => {
  fetchReclamations();
  fetchReservations();
});
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

.stat-card.bg-primary { border-left: 4px solid #007bff; }
.stat-card.bg-warning { border-left: 4px solid #ffc107; }
.stat-card.bg-success { border-left: 4px solid #28a745; }
.stat-card.bg-info { border-left: 4px solid #17a2b8; }

.stat-icon { font-size: 2rem; opacity: 0.8; }
.stat-card.bg-primary .stat-icon { color: #007bff; }
.stat-card.bg-warning .stat-icon { color: #ffc107; }
.stat-card.bg-success .stat-icon { color: #28a745; }
.stat-card.bg-info .stat-icon { color: #17a2b8; }

.stat-info { display: flex; flex-direction: column; }
.stat-value { font-size: 1.8rem; font-weight: bold; color: #333; }
.stat-label { font-size: 0.9rem; color: #666; }

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

.card-title { font-size: 1.1rem; font-weight: 600; }

.badge { padding: 5px 10px; border-radius: 4px; font-size: 0.75rem; }
</style>
