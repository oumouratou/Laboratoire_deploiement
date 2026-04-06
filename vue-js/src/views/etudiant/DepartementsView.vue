<template>
  <div>
    <!-- En-tête -->
    <section class="content-header">
      <div class="container-fluid">
        <div class="row mb-2">
          <div class="col-sm-6">
            <h1><i class="fas fa-building mr-2"></i> Départements</h1>
            <small class="text-muted">Consulter la liste des départements</small>
          </div>
        </div>
      </div>
    </section>

    <!-- Contenu -->
    <section class="content">
      <div class="container-fluid">
        <div class="card card-outline card-info">
          <div class="card-header">
            <h3 class="card-title">
              <i class="fas fa-list mr-2"></i> Liste des départements
            </h3>
          </div>

          <div class="card-body table-responsive">
            <table class="table table-bordered table-striped table-hover" v-if="departements.length">
              <thead class="bg-info text-white">
                <tr>
                  <th style="width: 80px;">Numéro</th>
                  <th>Nom</th>
                  <th>Description</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(dept, index) in departements" :key="dept.id">
                  <td>{{ index + 1 }}</td>
                  <td><strong>{{ dept.nom }}</strong></td>
                  <td>{{ dept.description || '—' }}</td>
                </tr>
              </tbody>
            </table>

            <div v-else class="text-center text-muted p-4">
              <i class="fas fa-inbox fa-3x mb-3"></i>
              <p>Aucun département trouvé</p>
            </div>
          </div>

          <div class="card-footer">
            <span>{{ departements.length }} département(s)</span>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { getDepartements } from '@/Service/departementService'
import type { Departement } from '@/types'

const departements = ref<Departement[]>([])

onMounted(async () => {
  try {
    const res = await getDepartements()
    departements.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
    departements.value = []
  }
})
</script>
