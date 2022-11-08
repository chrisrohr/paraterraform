<template>
  <q-layout view="hHh lpR fFf">
    <q-header elevated class="bg-primary text-white">
      <q-toolbar>
        <q-btn dense flat round icon="menu" @click="toggleLeftDrawer" />

        <q-toolbar-title>
          <q-avatar color="white">
            <img src="/dome.svg" alt="Paraterraform">
          </q-avatar>
          Paraterraform
        </q-toolbar-title>
      </q-toolbar>
    </q-header>

    <q-drawer
      v-model="leftDrawerOpen"
      show-if-above
      :mini="miniState"
      @mouseover="toggleMiniState"
      @mouseout="toggleMiniState"
      mini-to-overlay
      :width="200"
      :breakpoint="500"
      class="bg-grey-3"
      side="left"
      bordered>
      <q-scroll-area class="fit" :horizontal-thumb-style="{ opacity: 0 }">
        <q-list padding>
          <q-item clickable v-ripple to="/">
            <q-item-section avatar>
              <q-icon name="home" />
            </q-item-section>

            <q-item-section>
              Home
            </q-item-section>
          </q-item>

          <q-item clickable v-ripple to="/upload">
            <q-item-section avatar>
              <q-icon name="cloud_upload" />
            </q-item-section>

            <q-item-section>
              Upload
            </q-item-section>
          </q-item>

          <q-item clickable v-ripple to="/history">
            <q-item-section avatar>
              <q-icon name="history" />
            </q-item-section>

            <q-item-section>
              History
            </q-item-section>
          </q-item>
        </q-list>
      </q-scroll-area>
    </q-drawer>

    <q-page-container>
      <router-view />
    </q-page-container>
  </q-layout>

</template>

<script>
import { ref } from 'vue';

export default {
  setup() {
    const leftDrawerOpen = ref(false);
    const miniState = ref(true);

    return {
      leftDrawerOpen,
      miniState,
      toggleMiniState() {
        miniState.value = !miniState.value;
      },
      toggleLeftDrawer() {
        leftDrawerOpen.value = !leftDrawerOpen.value;
      },
    };
  },
};
</script>
