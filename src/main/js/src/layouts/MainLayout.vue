<template>
  <q-layout>
    <div class="q-pa-md">
    <q-toolbar class="bg-purple text-white shadow-2 rounded-borders">
      <q-btn flat label="Foreground" />
      <q-space />

      <!--
        notice shrink property since we are placing it
        as child of QToolbar
      -->
      <q-tabs v-model="tab" shrink>
        <q-route-tab exact clickable to="/" name="tab3" label="Home" />
        <q-route-tab exact clickable to="/upload" name="tab1" label="Upload" />
        <q-route-tab exact clickable to="/history" name="tab2" label="History" />
      </q-tabs>
    </q-toolbar>
  </div>
  <q-page-container>
    <router-view />
  </q-page-container>
  </q-layout>

</template>

<script>
import { ref } from 'vue';

export default {
  setup() {
    return {
      tab: ref(''),
    };
  },
};
</script>
