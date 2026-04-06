<template>
  <table class="table table-bordered table-striped">
    <thead>
      <tr>
        <th>ID</th>
        <th>Nom Labo</th>
        <th>Département</th>
        <th>Actions</th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="labo in laboratoires" :key="labo.id">
        <td>{{ labo.id }}</td>
        <td>{{ labo.nomLabo }}</td>
        <td>{{ labo.departement }}</td>
        <td>
          <button class="btn btn-sm btn-primary">Modifier</button>
          <button class="btn btn-sm btn-danger">Supprimer</button>
        </td>
      </tr>
    </tbody>
  </table>
</template>

<script setup>
import { ref } from 'vue'

const laboratoires = ref([
  { id: 1, nomLabo: 'Informatique', departement: 'Sciences' },
  { id: 2, nomLabo: 'Chimie', departement: 'Sciences' },
  { id: 3, nomLabo: 'Physique', departement: 'Sciences' }
])
</script>

<style scoped>
</style>
